# PHP 上傳照片範例

本目錄包含使用 PHP 調用 RaceShot 攝影師 API 上傳照片的範例代碼。

## 目錄

- [基本用法](#基本用法)
- [範例文件](#範例文件)
- [安裝與執行](#安裝與執行)
- [常見問題](#常見問題)

## 基本用法

使用 RaceShot 攝影師 API 上傳照片的基本流程非常簡單：

```php
<?php
// 設置 API 配置
$apiUrl = 'https://api.raceshot.com/api/photographer/upload';
$apiToken = 'YOUR_API_TOKEN';

// 準備表單數據
$imagePath = 'photo.jpg';
$eventId = 'event-123';
$bibNumber = '123';
$location = '終點線';
$price = 100;

// 創建 cURL 請求
$curl = curl_init();

// 設置 cURL 選項
curl_setopt_array($curl, [
    CURLOPT_URL => $apiUrl,
    CURLOPT_RETURNTRANSFER => true,
    CURLOPT_POST => true,
    CURLOPT_HTTPHEADER => [
        'Authorization: Bearer ' . $apiToken
    ],
    CURLOPT_POSTFIELDS => [
        'image' => new CURLFile($imagePath),
        'eventId' => $eventId,
        'bibNumber' => $bibNumber,
        'location' => $location,
        'price' => $price
    ]
]);

// 發送請求
$response = curl_exec($curl);
$httpCode = curl_getinfo($curl, CURLINFO_HTTP_CODE);

// 關閉 cURL 請求
curl_close($curl);

// 處理回應
$result = json_decode($response, true);

if ($httpCode == 200 && $result['success']) {
    echo "圖片上傳成功！\n";
    echo "圖片 ID: " . $result['photoId'] . "\n";
} else {
    echo "上傳失敗: " . ($result['error'] ?? '未知錯誤') . "\n";
}
?>
```

## 範例文件

本目錄包含以下範例文件：

1. `simple_upload.php` - 基本的照片上傳範例

## 安裝與執行

### 環境要求

- PHP 7.0 或更高版本
- cURL 擴展

### 執行範例

在執行範例之前，請確保您已經：

1. 在 RaceShot 攝影師中心的「API Token 管理」頁面生成了 API Token
2. 修改範例代碼中的配置（API Token、圖片路徑等）

然後執行：

```bash
php simple_upload.php
```

## 常見問題

### 如何獲取 API Token？

1. 登入 RaceShot 攝影師中心
2. 前往「API Token 管理」頁面
3. 選擇 Token 有效期並生成 Token
4. 複製並安全保存生成的 Token

### 支援哪些圖片格式？

RaceShot API 支援以下圖片格式：
- JPEG
- PNG
- HEIF

### 上傳失敗怎麼辦？

常見的上傳失敗原因包括：

1. API Token 無效或過期
2. 缺少必要的表單欄位
3. 價格設置低於最低限制（60）
4. 圖片格式不支援或圖片損壞

如果您遇到上傳失敗的問題，請檢查 API 回應中的錯誤訊息，它通常會提供有用的錯誤診斷資訊。

---

© 2025 RaceShot. 保留所有權利。
