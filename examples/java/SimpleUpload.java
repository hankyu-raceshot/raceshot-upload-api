import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 運動拍檔 RaceShot 攝影師 API 上傳照片範例
 * 此類別展示如何使用 Java 調用 運動拍檔 RaceShot 攝影師 API 上傳照片
 */
public class SimpleUpload {
    
    // API 配置
    private final String apiToken;
    private final String apiUrl;
    private final Path imagePath;
    private final String eventId;
    private final String bibNumber;
    private final String location;
    private final int price;
    
    /**
     * 建構函數
     * 
     * @param apiToken API Token
     * @param imagePath 圖片路徑
     * @param eventId 活動 ID
     * @param bibNumber 號碼布號碼（可選）
     * @param location 拍攝地點
     * @param price 價格（須大於 60）
     */
    public SimpleUpload(String apiToken, String imagePath, String eventId, String bibNumber, String location, int price) {
        this.apiToken = apiToken;
        this.apiUrl = "https://api.raceshot.com/api/photographer/upload";
        this.imagePath = Paths.get(imagePath);
        this.eventId = eventId;
        this.bibNumber = bibNumber != null ? bibNumber : "unknown";
        this.location = location;
        this.price = price;
        
        // 驗證參數
        if (apiToken == null || apiToken.isEmpty()) {
            throw new IllegalArgumentException("API Token 不能為空");
        }
        
        if (!Files.exists(this.imagePath)) {
            throw new IllegalArgumentException("圖片檔案不存在: " + imagePath);
        }
        
        if (eventId == null || eventId.isEmpty()) {
            throw new IllegalArgumentException("活動 ID 不能為空");
        }
        
        if (location == null || location.isEmpty()) {
            throw new IllegalArgumentException("拍攝地點不能為空");
        }
        
        if (price < 60) {
            throw new IllegalArgumentException("價格必須大於 60");
        }
    }
    
    /**
     * 同步上傳圖片
     * 
     * @return 上傳結果
     * @throws IOException 如果發生 I/O 錯誤
     * @throws InterruptedException 如果操作被中斷
     */
    public UploadResult upload() throws IOException, InterruptedException {
        System.out.println("正在讀取圖片...");
        
        // 獲取檔案名稱
        String fileName = imagePath.getFileName().toString();
        System.out.println("正在上傳圖片: " + fileName);
        System.out.println("活動 ID: " + eventId + ", 號碼布: " + bibNumber);
        
        // 讀取圖片檔案
        byte[] imageBytes = Files.readAllBytes(imagePath);
        
        // 創建 HttpClient
        HttpClient client = HttpClient.newBuilder().build();
        
        // 創建 multipart 請求
        String boundary = UUID.randomUUID().toString();
        
        // 構建請求體
        StringBuilder requestBodyBuilder = new StringBuilder();
        
        // 添加圖片部分
        requestBodyBuilder.append("--").append(boundary).append("\r\n");
        requestBodyBuilder.append("Content-Disposition: form-data; name=\"image\"; filename=\"").append(fileName).append("\"\r\n");
        
        // 根據檔案擴展名設置內容類型
        String contentType;
        if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
            contentType = "image/jpeg";
        } else if (fileName.toLowerCase().endsWith(".png")) {
            contentType = "image/png";
        } else if (fileName.toLowerCase().endsWith(".heif") || fileName.toLowerCase().endsWith(".heic")) {
            contentType = "image/heif";
        } else {
            contentType = "application/octet-stream";
        }
        
        requestBodyBuilder.append("Content-Type: ").append(contentType).append("\r\n\r\n");
        
        byte[] requestBodyBytes = requestBodyBuilder.toString().getBytes();
        byte[] endBoundaryBytes = ("\r\n--" + boundary + "\r\n").getBytes();
        
        // 添加其他欄位
        StringBuilder otherFieldsBuilder = new StringBuilder();
        
        // 添加 eventId
        otherFieldsBuilder.append("Content-Disposition: form-data; name=\"eventId\"\r\n\r\n");
        otherFieldsBuilder.append(eventId).append("\r\n");
        otherFieldsBuilder.append("--").append(boundary).append("\r\n");
        
        // 添加 bibNumber
        otherFieldsBuilder.append("Content-Disposition: form-data; name=\"bibNumber\"\r\n\r\n");
        otherFieldsBuilder.append(bibNumber).append("\r\n");
        otherFieldsBuilder.append("--").append(boundary).append("\r\n");
        
        // 添加 location
        otherFieldsBuilder.append("Content-Disposition: form-data; name=\"location\"\r\n\r\n");
        otherFieldsBuilder.append(location).append("\r\n");
        otherFieldsBuilder.append("--").append(boundary).append("\r\n");
        
        // 添加 price
        otherFieldsBuilder.append("Content-Disposition: form-data; name=\"price\"\r\n\r\n");
        otherFieldsBuilder.append(price).append("\r\n");
        otherFieldsBuilder.append("--").append(boundary).append("--\r\n");
        
        byte[] otherFieldsBytes = otherFieldsBuilder.toString().getBytes();
        
        // 合併所有部分
        byte[] fullRequestBody = new byte[requestBodyBytes.length + imageBytes.length + endBoundaryBytes.length + otherFieldsBytes.length];
        System.arraycopy(requestBodyBytes, 0, fullRequestBody, 0, requestBodyBytes.length);
        System.arraycopy(imageBytes, 0, fullRequestBody, requestBodyBytes.length, imageBytes.length);
        System.arraycopy(endBoundaryBytes, 0, fullRequestBody, requestBodyBytes.length + imageBytes.length, endBoundaryBytes.length);
        System.arraycopy(otherFieldsBytes, 0, fullRequestBody, requestBodyBytes.length + imageBytes.length + endBoundaryBytes.length, otherFieldsBytes.length);
        
        // 創建 HTTP 請求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .header("Authorization", "Bearer " + apiToken)
                .POST(HttpRequest.BodyPublishers.ofByteArray(fullRequestBody))
                .build();
        
        // 發送請求
        System.out.println("正在發送 API 請求...");
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        // 處理回應
        int statusCode = response.statusCode();
        String responseBody = response.body();
        
        if (statusCode == 200) {
            // 解析 JSON 回應
            UploadResult result = parseSuccessResponse(responseBody);
            
            if (result.isSuccess()) {
                System.out.println("✅ 圖片上傳成功!");
                System.out.println("圖片 ID: " + result.getPhotoId());
                System.out.println("原始檔案 ID: " + result.getOriginalFileId());
                System.out.println("訊息: " + result.getMessage());
            } else {
                System.out.println("❌ 上傳失敗: API 回應成功但結果不成功");
            }
            
            return result;
        } else {
            System.out.println("❌ 上傳失敗: HTTP 狀態碼 " + statusCode);
            System.out.println("回應內容: " + responseBody);
            
            // 解析錯誤回應
            String errorMessage = parseErrorResponse(responseBody);
            
            UploadResult errorResult = new UploadResult();
            errorResult.setSuccess(false);
            errorResult.setMessage(errorMessage);
            
            return errorResult;
        }
    }
    
    /**
     * 非同步上傳圖片
     * 
     * @return 包含上傳結果的 CompletableFuture
     */
    public CompletableFuture<UploadResult> uploadAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return upload();
            } catch (Exception e) {
                UploadResult errorResult = new UploadResult();
                errorResult.setSuccess(false);
                errorResult.setMessage("上傳失敗: " + e.getMessage());
                return errorResult;
            }
        });
    }
    
    /**
     * 解析成功回應
     * 
     * @param responseBody 回應內容
     * @return 上傳結果
     */
    private UploadResult parseSuccessResponse(String responseBody) {
        // 這裡使用簡單的字符串處理來解析 JSON
        // 在實際應用中，建議使用 JSON 庫（如 Gson 或 Jackson）
        UploadResult result = new UploadResult();
        
        if (responseBody.contains("\"success\":true")) {
            result.setSuccess(true);
            
            // 解析 photoId
            int photoIdStart = responseBody.indexOf("\"photoId\":") + 11;
            int photoIdEnd = responseBody.indexOf("\"", photoIdStart);
            if (photoIdStart > 10 && photoIdEnd > photoIdStart) {
                result.setPhotoId(responseBody.substring(photoIdStart, photoIdEnd));
            }
            
            // 解析 originalFileId
            int originalFileIdStart = responseBody.indexOf("\"originalFileId\":") + 17;
            int originalFileIdEnd = responseBody.indexOf("\"", originalFileIdStart);
            if (originalFileIdStart > 16 && originalFileIdEnd > originalFileIdStart) {
                result.setOriginalFileId(responseBody.substring(originalFileIdStart, originalFileIdEnd));
            }
            
            // 解析 cloudflareId
            int cloudflareIdStart = responseBody.indexOf("\"cloudflareId\":") + 15;
            int cloudflareIdEnd = responseBody.indexOf("\"", cloudflareIdStart);
            if (cloudflareIdStart > 14 && cloudflareIdEnd > cloudflareIdStart) {
                result.setCloudflareId(responseBody.substring(cloudflareIdStart, cloudflareIdEnd));
            }
            
            // 解析 message
            int messageStart = responseBody.indexOf("\"message\":") + 11;
            int messageEnd = responseBody.indexOf("\"", messageStart);
            if (messageStart > 10 && messageEnd > messageStart) {
                result.setMessage(responseBody.substring(messageStart, messageEnd));
            }
        } else {
            result.setSuccess(false);
            result.setMessage("解析回應失敗");
        }
        
        return result;
    }
    
    /**
     * 解析錯誤回應
     * 
     * @param responseBody 回應內容
     * @return 錯誤訊息
     */
    private String parseErrorResponse(String responseBody) {
        // 解析錯誤訊息
        int errorStart = responseBody.indexOf("\"error\":") + 9;
        int errorEnd = responseBody.indexOf("\"", errorStart);
        
        if (errorStart > 8 && errorEnd > errorStart) {
            return responseBody.substring(errorStart, errorEnd);
        } else {
            return "未知錯誤";
        }
    }
    
    /**
     * 主方法，用於測試
     * 
     * @param args 命令行參數
     */
    public static void main(String[] args) {
        try {
            // 配置
            String apiToken = "YOUR_API_TOKEN";
            String imagePath = "../../../assets/sample-image.jpg";
            String eventId = "00000";
            String bibNumber = "123";
            String location = "終點線";
            int price = 100;
            
            // 檢查 API Token
            if ("YOUR_API_TOKEN".equals(apiToken)) {
                System.out.println("警告: 請替換 apiToken 為您的實際 API Token");
                System.out.println("API Token 可以在運動拍檔 RaceShot 攝影師中心的「[API Token 管理](https://raceshot.com/photographer/api-token)」頁面生成");
                return;
            }
            
            // 創建上傳實例
            SimpleUpload uploader = new SimpleUpload(apiToken, imagePath, eventId, bibNumber, location, price);
            
            // 同步上傳
            UploadResult result = uploader.upload();
            
            // 顯示結果
            if (result.isSuccess()) {
                System.out.println("\n上傳成功摘要:");
                System.out.println("圖片 ID: " + result.getPhotoId());
                System.out.println("原始檔案 ID: " + result.getOriginalFileId());
                System.out.println("Cloudflare ID: " + result.getCloudflareId());
            } else {
                System.out.println("\n上傳失敗摘要:");
                System.out.println("錯誤訊息: " + result.getMessage());
            }
            
            // 非同步上傳示例
            System.out.println("\n非同步上傳示例:");
            CompletableFuture<UploadResult> future = uploader.uploadAsync();
            
            // 添加回調
            future.thenAccept(asyncResult -> {
                if (asyncResult.isSuccess()) {
                    System.out.println("非同步上傳成功: " + asyncResult.getPhotoId());
                } else {
                    System.out.println("非同步上傳失敗: " + asyncResult.getMessage());
                }
            });
            
            // 等待非同步上傳完成
            try {
                future.get();
            } catch (ExecutionException e) {
                System.out.println("非同步上傳發生錯誤: " + e.getCause().getMessage());
            }
            
        } catch (Exception e) {
            System.out.println("發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 上傳結果類別
     */
    public static class UploadResult {
        private boolean success;
        private String photoId;
        private String originalFileId;
        private String cloudflareId;
        private String message;
        
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getPhotoId() {
            return photoId;
        }
        
        public void setPhotoId(String photoId) {
            this.photoId = photoId;
        }
        
        public String getOriginalFileId() {
            return originalFileId;
        }
        
        public void setOriginalFileId(String originalFileId) {
            this.originalFileId = originalFileId;
        }
        
        public String getCloudflareId() {
            return cloudflareId;
        }
        
        public void setCloudflareId(String cloudflareId) {
            this.cloudflareId = cloudflareId;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}

/**
 * 使用說明:
 * 
 * 1. 替換 apiToken 為您的 API Token
 *    API Token 可以在運動拍檔 RaceShot 攝影師中心的「[API Token 管理](https://raceshot.com/photographer/api-token)」頁面生成
 * 
 * 2. 替換 imagePath 為您要上傳的圖片路徑
 * 
 * 3. 修改其他參數（eventId, bibNumber, location, price）
 *    - eventId: 活動 ID，必填
 *    - bibNumber: 號碼布號碼，可選
 *    - location: 拍攝地點，必填
 *    - price: 價格，必須大於 60
 * 
 * 4. 編譯並執行:
 *    javac SimpleUpload.java
 *    java SimpleUpload
 * 
 * 注意:
 * - 圖片必須是有效的圖片檔案（JPEG, PNG）
 * - 價格必須大於 60
 * - 在實際應用中，建議使用 JSON 庫（如 Gson 或 Jackson）來解析 JSON 回應
 */
