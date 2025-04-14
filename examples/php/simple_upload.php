<?php
/**
 * 運動拍檔 RaceShot 攝影師 API 上傳照片範例
 * 
 * 此範例展示如何使用 PHP 調用 運動拍檔 RaceShot 攝影師 API 上傳照片
 */

// 配置
$config = [
    // 替換為您的 API Token
    'api_token' => 'YOUR_API_TOKEN',
    
    // API 端點
    'api_url' => 'https://api.raceshot.com/api/photographer/upload',
    
    // 替換為您要上傳的圖片路徑
    'image_path' => __DIR__ . '/../../assets/sample-image.jpg',
    
    // 活動 ID
    'event_id' => '00000',
    
    // 號碼布號碼（可選）
    'bib_number' => '123',
    
    // 拍攝地點
    'location' => '終點線',
    
    // 價格
    'price' => 100
];

/**
 * 上傳圖片到 RaceShot
 */
function uploadPhoto($config) {
    try {
        echo "正在讀取圖片...\n";
        
        // 檢查圖片是否存在
        if (!file_exists($config['image_path'])) {
            echo "錯誤: 找不到圖片文件 {$config['image_path']}\n";
            return;
        }
        
        // 獲取檔案名稱
        $file_name = basename($config['image_path']);
        echo "正在上傳圖片: {$file_name}\n";
        echo "活動 ID: {$config['event_id']}, 號碼布: {$config['bib_number']}\n";
        
        // 創建 cURL 請求
        $curl = curl_init();
        
        // 設置 cURL 選項
        curl_setopt_array($curl, [
            CURLOPT_URL => $config['api_url'],
            CURLOPT_RETURNTRANSFER => true,
            CURLOPT_POST => true,
            CURLOPT_HTTPHEADER => [
                'Authorization: Bearer ' . $config['api_token']
            ],
            CURLOPT_POSTFIELDS => [
                'image' => new CURLFile($config['image_path']),
                'eventId' => $config['event_id'],
                'bibNumber' => $config['bib_number'],
                'location' => $config['location'],
                'price' => $config['price']
            ]
        ]);
        
        // 發送請求
        echo "正在發送 API 請求...\n";
        $response = curl_exec($curl);
        $http_code = curl_getinfo($curl, CURLINFO_HTTP_CODE);
        
        // 檢查 cURL 錯誤
        if (curl_errno($curl)) {
            echo "cURL 錯誤: " . curl_error($curl) . "\n";
            curl_close($curl);
            return;
        }
        
        // 關閉 cURL 請求
        curl_close($curl);
        
        // 解析回應
        $result = json_decode($response, true);
        
        if ($http_code == 200 && isset($result['success']) && $result['success']) {
            echo "✅ 圖片上傳成功!\n";
            echo "圖片 ID: " . $result['photoId'] . "\n";
            echo "原始檔案 ID: " . $result['originalFileId'] . "\n";
            echo "訊息: " . $result['message'] . "\n";
        } else {
            echo "❌ 上傳失敗:\n";
            echo "狀態碼: {$http_code}\n";
            echo "錯誤: " . (isset($result['error']) ? $result['error'] : '未知錯誤') . "\n";
        }
    } catch (Exception $e) {
        echo "發生錯誤: " . $e->getMessage() . "\n";
    }
}

// 執行上傳
uploadPhoto($config);

/**
 * 使用說明:
 * 
 * 1. 確保 PHP 環境已安裝 cURL 擴展
 * 
 * 2. 替換 $config 中的 api_token 為您的 API Token
 *    API Token 可以在運動拍檔 RaceShot 攝影師中心的「[API Token 管理](https://raceshot.com/photographer/api-token)」頁面生成
 * 
 * 3. 替換 image_path 為您要上傳的圖片路徑
 * 
 * 4. 修改其他參數（event_id, bib_number, location, price）
 *    - event_id: 活動 ID，必填
 *    - bib_number: 號碼布號碼，可選
 *    - location: 拍攝地點，必填
 *    - price: 價格，必須大於 60
 * 
 * 5. 執行腳本:
 *    php simple_upload.php
 * 
 * 注意:
 * - 圖片必須是有效的圖片檔案（JPEG, PNG）
 * - 價格必須大於 60
 */
?>
