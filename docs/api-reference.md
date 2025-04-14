# 運動拍檔 RaceShot 攝影師 API 參考文件

本文檔提供運動拍檔 RaceShot 攝影師 API 的詳細說明，包括端點、請求參數、回應格式和錯誤處理。

## 目錄

- [認證機制](#認證機制)
- [API 端點](#api-端點)
- [錯誤處理](#錯誤處理)
- [限制說明](#限制說明)
- [安全性考量](#安全性考量)

## 認證機制

所有 API 請求都需要使用 Bearer Token 進行認證。

### 獲取 Token

1. 登入運動拍檔 RaceShot 攝影師中心
2. 前往「[API Token 管理](https://raceshot.com/photographer/api-token)」頁面
3. 設定 Token 有效期並生成 Token

### 使用 Token

在所有 API 請求的標頭中添加：

```
Authorization: Bearer YOUR_API_TOKEN
```

## API 端點

### 上傳照片

**端點**：`POST https://api.raceshot.com/api/photographer/upload`

**Content-Type**：`multipart/form-data`

**請求參數**：

| 參數名 | 類型 | 必填 | 說明 |
|--------|------|------|------|
| image | File | 是 | 圖片檔案 |
| eventId | String | 是 | 活動ID |
| bibNumber | String | 否 | 號碼布號碼 |
| location | String | 是 | 拍攝地點 |
| price | Number | 是 | 價格（須大於 60） |

**請求範例**：

```
POST https://api.raceshot.com/api/photographer/upload HTTP/1.1
Host: api.raceshot.com
Authorization: Bearer YOUR_API_TOKEN
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW

------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="image"; filename="photo.jpg"
Content-Type: image/jpeg

(圖片二進制數據)
------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="eventId"

00000
------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="bibNumber"

123
------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="location"

終點線
------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="price"

100
------WebKitFormBoundary7MA4YWxkTrZu0gW--
```

**回應格式**：

```json
{
  "success": true,
  "photoId": "unique-photo-id-123",
  "message": "圖片上傳成功"
}
```

## 錯誤處理

API 使用標準的 HTTP 狀態碼表示請求結果。常見的錯誤碼包括：

| 狀態碼 | 說明 |
|--------|------|
| 400 | 請求參數錯誤 |
| 401 | 認證失敗 |
| 403 | 權限不足 |
| 404 | 資源不存在 |
| 413 | 檔案過大 |
| 429 | 請求過於頻繁 |
| 500 | 伺服器內部錯誤 |

錯誤回應格式：

```json
{
  "error": "錯誤訊息"
}
```

常見錯誤訊息：

| 錯誤訊息 | 說明 |
|----------|------|
| 只接受 POST 請求 | 請求方法不是 POST |
| 缺少或無效的授權標頭 | 未提供 API Token 或格式不正確 |
| 無效的 API Token 或未授權 | API Token 無效、過期或權限不足 |
| 缺少圖片檔案 | 未提供圖片檔案 |
| 缺少活動 ID | 未提供 eventId 參數 |
| 缺少拍攝地點 | 未提供 location 參數 |
| 價格必須大於 60 | price 參數小於 60 |

## 限制說明

- 檔案大小上限：20MB
- 支援的圖片格式：JPEG、PNG
- 每分鐘請求數上限：60 次
- Token 有效期：最長 365 天

## 安全性考量

1. **Token 安全**
   - 請勿在客戶端代碼中硬編碼 Token
   - 定期更換 Token
   - 設置合理的 Token 有效期

2. **資料傳輸**
   - 所有 API 請求都通過 HTTPS 加密傳輸
   - 敏感資訊不應包含在 URL 中

3. **錯誤處理**
   - 在生產環境中不要向用戶顯示詳細的錯誤信息
   - 實現適當的日誌記錄機制

---

© 2025 運動拍檔 RaceShot. 保留所有權利。
