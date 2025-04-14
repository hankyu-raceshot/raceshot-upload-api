/**
 * RaceShot 攝影師 API 上傳照片範例
 * 
 * 此範例展示如何使用 JavaScript 調用 RaceShot 攝影師 API 上傳照片
 */

// 引入所需模組 (Node.js 環境)
const fs = require('fs');
const path = require('path');
const FormData = require('form-data');
const fetch = require('node-fetch');

// 配置
const config = {
  // 替換為您的 API Token
  apiToken: 'YOUR_API_TOKEN',
  
  // API 端點
  apiUrl: 'https://api.raceshot.com/api/photographer/upload',
  
  // 替換為您要上傳的圖片路徑
  imagePath: path.join(__dirname, '../../assets/sample-image.jpg'),
  
  // 活動 ID
  eventId: '00000',
  
  // 號碼布號碼（可選）
  bibNumber: '123',
  
  // 拍攝地點
  location: '終點線',
  
  // 價格
  price: 100
};

/**
 * 上傳圖片到 RaceShot
 */
async function uploadPhoto() {
  try {
    console.log('正在讀取圖片...');
    
    // 讀取圖片檔案
    const imageBuffer = fs.readFileSync(config.imagePath);
    const fileName = path.basename(config.imagePath);
    
    console.log(`正在上傳圖片: ${fileName}`);
    console.log(`活動 ID: ${config.eventId}, 號碼布: ${config.bibNumber}`);
    
    // 創建 FormData
    const formData = new FormData();
    formData.append('image', imageBuffer, {
      filename: fileName,
      contentType: 'image/jpeg' // 根據實際圖片類型調整
    });
    formData.append('eventId', config.eventId);
    formData.append('bibNumber', config.bibNumber);
    formData.append('location', config.location);
    formData.append('price', config.price.toString());
    
    // 發送 API 請求
    const response = await fetch(config.apiUrl, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${config.apiToken}`
      },
      body: formData
    });
    
    // 解析回應
    const result = await response.json();
    
    if (response.ok) {
      console.log('✅ 圖片上傳成功!');
      console.log(`圖片 ID: ${result.photoId}`);
      console.log(`原始檔案 ID: ${result.originalFileId}`);
      console.log(`訊息: ${result.message}`);
    } else {
      console.error('❌ 上傳失敗:');
      console.error(result.error);
    }
  } catch (error) {
    console.error('發生錯誤:', error.message);
  }
}

// 執行上傳
uploadPhoto().catch(console.error);

/**
 * 使用說明:
 * 
 * 1. 安裝依賴:
 *    npm install form-data node-fetch
 * 
 * 2. 替換 config 中的 apiToken 為您的 API Token
 *    API Token 可以在 RaceShot 攝影師中心的「API Token 管理」頁面生成
 * 
 * 3. 替換 imagePath 為您要上傳的圖片路徑
 * 
 * 4. 修改其他參數（eventId, bibNumber, location, price）
 *    - eventId: 活動 ID，必填
 *    - bibNumber: 號碼布號碼，可選
 *    - location: 拍攝地點，必填
 *    - price: 價格，必須大於 60
 * 
 * 5. 執行腳本:
 *    node simple-upload.js
 * 
 * 注意:
 * - 圖片必須是有效的圖片檔案（JPEG, PNG, HEIF）
 * - 價格必須大於 60
 */
