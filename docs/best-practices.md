# 運動拍檔 RaceShot 攝影師 API 最佳實踐指南

本文檔提供使用運動拍檔 RaceShot 攝影師 API 的最佳實踐建議，幫助您優化應用程式性能並避免常見問題。

## 目錄

- [基本原則](#基本原則)
- [錯誤處理與重試機制](#錯誤處理與重試機制)
- [安全性建議](#安全性建議)
- [效能優化](#效能優化)

## 基本原則

### 1. 遵循 API 規範

- 確保所有必要欄位都已提供（`eventId`、`location`、`price`）
- 確保價格設置大於最低限制（60）
- 使用正確的 `Content-Type`（`multipart/form-data`）

### 2. 合理使用 API Token

- 不要在客戶端代碼中硬編碼 Token
- 定期更換 Token
- 每個攝影師使用自己的 Token，不要共享

### 3. 圖片處理

- 上傳前檢查圖片格式是否支援（JPEG、PNG）
- 確保圖片檔案完整且未損壞
- 考慮在上傳前進行基本的圖片優化（如壓縮）

## 錯誤處理與重試機制

### 常見錯誤類型

| 狀態碼 | 說明 | 處理方式 |
|--------|------|---------|
| 400 | 請求參數錯誤 | 檢查請求參數是否完整和正確 |
| 401 | 認證失敗 | 檢查 API Token 是否有效 |
| 403 | 權限不足 | 確認 Token 對應的用戶有上傳權限 |
| 413 | 檔案過大 | 壓縮圖片或分批上傳 |
| 429 | 請求過於頻繁 | 實施退避策略，減少請求頻率 |
| 500 | 伺服器內部錯誤 | 稍後重試 |

### 重試策略

```javascript
// JavaScript 重試範例
async function uploadWithRetry(imageFile, eventId, bibNumber, location, price, maxRetries = 3) {
  let retries = 0;
  
  while (retries < maxRetries) {
    try {
      // 發送上傳請求
      const response = await fetch('https://api.raceshot.app/api/photographer/upload', {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${apiToken}` },
        body: formData
      });
      
      if (response.ok) {
        return await response.json();
      }
      
      // 如果是 429 或 5xx 錯誤，進行重試
      if (response.status === 429 || response.status >= 500) {
        retries++;
        
        // 指數退避策略
        const delay = Math.pow(2, retries) * 1000;
        console.log(`上傳失敗，將在 ${delay}ms 後重試 (${retries}/${maxRetries})...`);
        await new Promise(resolve => setTimeout(resolve, delay));
      } else {
        // 其他錯誤不重試
        throw new Error(`上傳失敗: ${response.status} ${response.statusText}`);
      }
    } catch (error) {
      if (retries >= maxRetries) {
        throw error;
      }
      retries++;
      const delay = Math.pow(2, retries) * 1000;
      console.log(`發生錯誤，將在 ${delay}ms 後重試 (${retries}/${maxRetries})...`);
      await new Promise(resolve => setTimeout(resolve, delay));
    }
  }
}
```

```python
# Python 重試範例
import time
import requests

def upload_with_retry(image_path, event_id, bib_number, location, price, api_token, max_retries=3):
    retries = 0
    
    while retries < max_retries:
        try:
            files = {"image": open(image_path, "rb")}
            data = {
                "eventId": event_id,
                "bibNumber": bib_number,
                "location": location,
                "price": str(price)
            }
            headers = {"Authorization": f"Bearer {api_token}"}
            
            response = requests.post(
                "https://api.raceshot.app/api/photographer/upload",
                headers=headers,
                files=files,
                data=data
            )
            
            if response.status_code == 200:
                return response.json()
            
            # 如果是 429 或 5xx 錯誤，進行重試
            if response.status_code == 429 or response.status_code >= 500:
                retries += 1
                
                # 指數退避策略
                delay = 2 ** retries
                print(f"上傳失敗，將在 {delay}秒 後重試 ({retries}/{max_retries})...")
                time.sleep(delay)
            else:
                # 其他錯誤不重試
                response.raise_for_status()
        except Exception as e:
            if retries >= max_retries:
                raise
            retries += 1
            delay = 2 ** retries
            print(f"發生錯誤: {e}，將在 {delay}秒 後重試 ({retries}/{max_retries})...")
            time.sleep(delay)
        finally:
            # 確保關閉文件
            if "files" in locals() and "image" in files:
                files["image"].close()
```

## 安全性建議

### Token 管理

- 將 Token 存儲在安全的位置，如環境變數或加密的配置文件
- 不要將 Token 包含在版本控制系統中
- 如果懷疑 Token 洩露，立即生成新的 Token

### 資料傳輸

- 確保使用 HTTPS 進行所有 API 請求
- 不要在 URL 中包含敏感資訊
- 考慮使用 HTTPS 代理來記錄和監控 API 請求

### 錯誤處理

- 在生產環境中不要向用戶顯示詳細的錯誤信息
- 實施適當的日誌記錄機制
- 定期檢查錯誤日誌以識別潛在問題

## 效能優化

### 批量處理

如果需要上傳大量圖片，考慮以下策略：

1. **分批處理**：每批處理 5-10 張圖片
2. **並行限制**：同時處理的請求不超過 5 個
3. **間隔控制**：批次之間添加短暫延遲（約 500ms）

### 網路優化

1. **保持連接**：使用 HTTP Keep-Alive
2. **減少請求頭**：只發送必要的標頭
3. **使用 HTTP/2**：如果可能，使用 HTTP/2 提高效率

### 記憶體管理

1. **及時釋放資源**：處理完圖片後立即釋放
2. **避免大型 base64 編碼**：直接使用二進制數據
3. **流式處理**：對於大型文件，考慮使用流式處理

---

© 2025 運動拍檔 RaceShot. 保留所有權利。
