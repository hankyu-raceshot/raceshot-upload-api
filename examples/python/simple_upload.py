#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
運動拍檔 RaceShot 攝影師 API 上傳照片範例

此範例展示如何使用 Python 調用 運動拍檔 RaceShot 攝影師 API 上傳照片
"""

import os
import sys
import json
import requests
from pathlib import Path

# 配置
config = {
    # 替換為您的 API Token
    "api_token": "YOUR_API_TOKEN",
    
    # API 端點
    "api_url": "https://api.raceshot.com/api/photographer/upload",
    
    # 替換為您要上傳的圖片路徑
    "image_path": str(Path(__file__).parent.parent.parent / "assets" / "sample-image.jpg"),
    
    # 活動 ID
    "event_id": "00000",
    
    # 號碼布號碼（可選）
    "bib_number": "123",
    
    # 拍攝地點
    "location": "終點線",
    
    # 價格
    "price": 100
}

def upload_photo():
    """上傳圖片到 RaceShot"""
    try:
        print("正在讀取圖片...")
        
        # 檢查圖片是否存在
        if not os.path.exists(config["image_path"]):
            print(f"錯誤: 找不到圖片文件 {config['image_path']}")
            return
        
        # 獲取檔案名稱
        file_name = os.path.basename(config["image_path"])
        print(f"正在上傳圖片: {file_name}")
        print(f"活動 ID: {config['event_id']}, 號碼布: {config['bib_number']}")
        
        # 準備表單數據
        files = {
            "image": (file_name, open(config["image_path"], "rb"))
        }
        
        data = {
            "eventId": config["event_id"],
            "bibNumber": config["bib_number"],
            "location": config["location"],
            "price": str(config["price"])
        }
        
        # 設置請求標頭
        headers = {
            "Authorization": f"Bearer {config['api_token']}"
        }
        
        # 發送 API 請求
        print("正在發送 API 請求...")
        response = requests.post(
            config["api_url"], 
            headers=headers, 
            files=files, 
            data=data
        )
        
        # 處理回應
        result = response.json()
        
        if response.status_code == 200 and result.get("success"):
            print("✅ 圖片上傳成功!")
            print(f"圖片 ID: {result.get('photoId')}")
            print(f"原始檔案 ID: {result.get('originalFileId')}")
            print(f"訊息: {result.get('message')}")
        else:
            print("❌ 上傳失敗:")
            print(result.get("error", "未知錯誤"))
            print(f"狀態碼: {response.status_code}")
    
    except requests.exceptions.RequestException as e:
        print(f"網路請求錯誤: {e}")
    except json.JSONDecodeError:
        print("無法解析 API 回應")
    except Exception as e:
        print(f"發生錯誤: {e}")
    finally:
        # 確保關閉文件
        if "files" in locals() and "image" in files:
            files["image"][1].close()

if __name__ == "__main__":
    upload_photo()

"""
使用說明:

1. 安裝依賴:
   pip install requests

2. 替換 config 中的 api_token 為您的 API Token
   API Token 可以在運動拍檔 RaceShot 攝影師中心的「[API Token 管理](https://raceshot.com/photographer/api-token)」頁面生成

3. 替換 image_path 為您要上傳的圖片路徑

4. 修改其他參數（event_id, bib_number, location, price）
   - event_id: 活動 ID，必填
   - bib_number: 號碼布號碼，可選
   - location: 拍攝地點，必填
   - price: 價格，必須大於 60

5. 執行腳本:
   python simple_upload.py

注意:
- 圖片必須是有效的圖片檔案（JPEG, PNG）
- 價格必須大於 60

---

© 2025 運動拍檔 RaceShot. 保留所有權利。
"""
