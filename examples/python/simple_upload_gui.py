# -*- coding: utf-8 -*-
"""
運動拍檔 RaceShot 攝影師 API 上傳照片 GUI 版

此程式提供圖形化介面，讓使用者選擇圖片、填寫 event_id、location、price 並上傳。
"""

import tkinter as tk
from tkinter import filedialog, messagebox
import requests
import os
import json

API_URL = "https://api.raceshot.app/api/photographer/upload"

# 主要上傳邏輯，支援多檔案上傳
# =====================
def upload_photos(api_token, image_paths, event_id, location, price, bib_number=""):
    """上傳多張圖片到 RaceShot，回傳每張結果"""
    results = []
    # 參數檢查
    if not api_token.strip():
        return [(False, "請輸入 API Token")]
    if not image_paths:
        return [(False, "請選擇至少一張圖片")]
    if not event_id.strip():
        return [(False, "請輸入活動 ID (event_id)")]
    if not location.strip():
        return [(False, "請輸入拍攝地點 (location)")]
    try:
        price_val = int(price)
        if price_val <= 60:
            return [(False, "價格必須大於 60")]
    except ValueError:
        return [(False, "價格必須為數字")]

    headers = {"Authorization": f"Bearer {api_token}"}

    for image_path in image_paths:
        if not os.path.exists(image_path):
            results.append((False, f"找不到圖片文件: {image_path}"))
            continue
        file_name = os.path.basename(image_path)
        files = {"image": (file_name, open(image_path, "rb"))}
        data = {
            "eventId": event_id,
            "bibNumber": bib_number,
            "location": location,
            "price": str(price)
        }
        try:
            response = requests.post(API_URL, headers=headers, files=files, data=data)
            result = response.json()
            if response.status_code == 200 and result.get("success"):
                msg = f"✅ {file_name} 上傳成功！圖片 ID: {result.get('photoId')}, 訊息: {result.get('message')}"
                results.append((True, msg))
            else:
                msg = f"❌ {file_name} 上傳失敗: {result.get('error', '未知錯誤')} (狀態碼: {response.status_code})"
                results.append((False, msg))
        except requests.exceptions.RequestException as e:
            results.append((False, f"{file_name} 網路請求錯誤: {e}"))
        except json.JSONDecodeError:
            results.append((False, f"{file_name} 無法解析 API 回應"))
        except Exception as e:
            results.append((False, f"{file_name} 發生錯誤: {e}"))
        finally:
            files["image"][1].close()
    return results

# GUI 介面設計
# =============
class UploadApp:
    def __init__(self, master):
        self.master = master
        master.title("RaceShot 攝影師照片上傳工具")
        master.geometry("480x400")
        master.resizable(False, False)

        # 重要節點：API Token
        self.api_token = tk.StringVar()
        self.token_file = ".raceshot_token"  # 本地儲存 Token 的檔案
        self._load_token()  # 啟動時自動載入

        # 重要節點：檔案路徑（多選）
        self.image_paths = []
        self.image_paths_str = tk.StringVar()
        # 重要節點：欄位
        self.event_id = tk.StringVar()
        self.location = tk.StringVar()
        self.price = tk.StringVar()
        self.bib_number = tk.StringVar()

        # API Token
        tk.Label(master, text="API Token:").grid(row=0, column=0, sticky="e", padx=10, pady=10)
        tk.Entry(master, textvariable=self.api_token, width=40, show="*").grid(row=0, column=1, columnspan=2)

        # 圖片選擇（多選）
        tk.Label(master, text="選擇圖片(可多選):").grid(row=1, column=0, sticky="e", padx=10, pady=10)
        tk.Entry(master, textvariable=self.image_paths_str, width=32, state="readonly").grid(row=1, column=1)
        tk.Button(master, text="瀏覽...", command=self.browse_images).grid(row=1, column=2, padx=5)

        # event_id
        tk.Label(master, text="活動 ID:").grid(row=2, column=0, sticky="e", padx=10, pady=5)
        tk.Entry(master, textvariable=self.event_id, width=30).grid(row=2, column=1, columnspan=2)

        # location
        tk.Label(master, text="拍攝地點:").grid(row=3, column=0, sticky="e", padx=10, pady=5)
        tk.Entry(master, textvariable=self.location, width=30).grid(row=3, column=1, columnspan=2)

        # price
        tk.Label(master, text="價格:").grid(row=4, column=0, sticky="e", padx=10, pady=5)
        tk.Entry(master, textvariable=self.price, width=30).grid(row=4, column=1, columnspan=2)

        # bib_number (可選)
        tk.Label(master, text="號碼布(選填):").grid(row=5, column=0, sticky="e", padx=10, pady=5)
        tk.Entry(master, textvariable=self.bib_number, width=30).grid(row=5, column=1, columnspan=2)

        # 上傳按鈕
        tk.Button(master, text="上傳", command=self.do_upload, width=12, bg="#4CAF50", fg="white").grid(row=6, column=1, pady=15)

        # 狀態訊息
        self.status = tk.Label(master, text="", fg="blue", wraplength=420, justify="left")
        self.status.grid(row=7, column=0, columnspan=3, pady=10)

    def browse_images(self):
        paths = filedialog.askopenfilenames(
            title="選擇圖片(可多選)",
            filetypes=[("Image Files", "*.jpg;*.jpeg;*.png")]
        )
        if paths:
            self.image_paths = list(paths)
            # 顯示檔名（多個用逗號隔開）
            names = [os.path.basename(p) for p in self.image_paths]
            self.image_paths_str.set(", ".join(names))
        else:
            self.image_paths = []
            self.image_paths_str.set("")

    def do_upload(self):
        api_token = self.api_token.get()
        imgs = self.image_paths
        eid = self.event_id.get()
        loc = self.location.get()
        price = self.price.get()
        bib = self.bib_number.get()
        self.status.config(text="正在上傳，請稍候...", fg="blue")
        self.master.update()
        results = upload_photos(api_token, imgs, eid, loc, price, bib)
        msg = "\n".join([r[1] for r in results])
        if all(r[0] for r in results):
            self.status.config(text=msg, fg="green")
            messagebox.showinfo("全部上傳成功", msg)
            # 重要節點：上傳成功後自動儲存 Token
            self._save_token(api_token)
        else:
            self.status.config(text=msg, fg="red")
            messagebox.showerror("上傳失敗", msg)

# 主程式進入點
    # 讀取本地 Token 檔
    def _load_token(self):
        try:
            if os.path.exists(self.token_file):
                with open(self.token_file, "r", encoding="utf-8") as f:
                    token = f.read().strip()
                    self.api_token.set(token)
        except Exception as e:
            print(f"讀取 Token 檔失敗: {e}")

    # 儲存 Token 到本地檔
    def _save_token(self, token):
        try:
            with open(self.token_file, "w", encoding="utf-8") as f:
                f.write(token.strip())
        except Exception as e:
            print(f"儲存 Token 檔失敗: {e}")

if __name__ == "__main__":
    root = tk.Tk()
    app = UploadApp(root)
    root.mainloop()

"""
打包為 .exe 步驟：
1. 安裝 pyinstaller：
   pip install pyinstaller
2. 執行以下指令（請在本檔案目錄下）：
   pyinstaller --noconsole --onefile simple_upload_gui.py
3. 產生的 .exe 會在 dist/ 資料夾內

注意：請先 pip install requests
"""
