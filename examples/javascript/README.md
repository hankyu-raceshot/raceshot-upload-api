# JavaScript 上傳照片範例

本目錄包含使用 JavaScript 調用 運動拍檔 RaceShot 攝影師 API 上傳照片的範例代碼。

## 目錄

- [基本用法](#基本用法)
- [範例文件](#範例文件)
- [安裝與執行](#安裝與執行)
- [常見問題](#常見問題)

## 基本用法

使用 RaceShot 攝影師 API 上傳照片的基本流程非常簡單：

```javascript
// 引入所需模組
const fs = require('fs');
const FormData = require('form-data');
const fetch = require('node-fetch');

// 創建 FormData
const formData = new FormData();
formData.append('image', fs.readFileSync('photo.jpg'), { filename: 'photo.jpg' });
formData.append('eventId', '00000');
formData.append('bibNumber', '123');
formData.append('location', '終點線');
formData.append('price', '100');

// 發送 API 請求
const response = await fetch('https://api.raceshot.com/api/photographer/upload', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer YOUR_API_TOKEN`
  },
  body: formData
});

// 處理回應
const result = await response.json();
console.log(result);
```

## 範例文件

本目錄包含以下範例文件：

1. `simple-upload.js` - 基本的照片上傳範例

## 安裝與執行

### 安裝依賴

```bash
# 使用 npm
npm install form-data node-fetch

# 或使用 yarn
yarn add form-data node-fetch
```

### 執行範例

在執行範例之前，請確保您已經：

1. 在運動拍檔 RaceShot 攝影師中心的「[API Token 管理](https://raceshot.com/photographer/api-token)」頁面生成了 API Token
2. 修改範例代碼中的配置（API Token、圖片路徑等）

然後執行：

```bash
node simple-upload.js
```

## 常見問題

### 如何獲取 API Token？

1. 登入運動拍檔 RaceShot 攝影師中心
2. 前往「[API Token 管理](https://raceshot.com/photographer/api-token)」頁面
3. 選擇 Token 有效期並生成 Token
4. 複製並安全保存生成的 Token

### 支援哪些圖片格式？

運動拍檔 RaceShot API 支援以下圖片格式：
- JPEG
- PNG

### 上傳失敗怎麼辦？

常見的上傳失敗原因包括：

1. API Token 無效或過期
2. 缺少必要的表單欄位
3. 價格設置低於最低限制（60）
4. 圖片格式不支援或圖片損壞

如果您遇到上傳失敗的問題，請檢查 API 回應中的錯誤訊息，它通常會提供有用的錯誤診斷資訊。

---

© 2025 運動拍檔 RaceShot. 保留所有權利。
