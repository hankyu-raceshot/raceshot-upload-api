# Java 上傳照片範例

本目錄包含使用 Java 調用 RaceShot 攝影師 API 上傳照片的範例代碼。

## 目錄

- [基本用法](#基本用法)
- [範例文件](#範例文件)
- [安裝與執行](#安裝與執行)
- [常見問題](#常見問題)

## 基本用法

使用 RaceShot 攝影師 API 上傳照片的基本流程非常簡單：

```java
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.UUID;

public class SimpleUpload {
    public static void main(String[] args) throws IOException, InterruptedException {
        // 設置 API 配置
        String apiUrl = "https://api.raceshot.com/api/photographer/upload";
        String apiToken = "YOUR_API_TOKEN";
        String imagePath = "photo.jpg";
        String eventId = "event-123";
        String bibNumber = "123";
        String location = "終點線";
        int price = 100;

        // 讀取圖片檔案
        File imageFile = new File(imagePath);
        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());

        // 創建 HttpClient
        HttpClient client = HttpClient.newBuilder().build();

        // 創建 multipart 請求
        String boundary = UUID.randomUUID().toString();
        
        // 構建請求體
        String requestBody = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"image\"; filename=\"" + imageFile.getName() + "\"\r\n" +
                "Content-Type: image/jpeg\r\n\r\n";
        
        // 添加圖片數據
        byte[] requestBodyBytes = requestBody.getBytes();
        byte[] endBoundaryBytes = ("\r\n--" + boundary + "\r\n").getBytes();
        
        // 添加其他欄位
        String otherFields = 
                "Content-Disposition: form-data; name=\"eventId\"\r\n\r\n" + eventId + "\r\n" +
                "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"bibNumber\"\r\n\r\n" + bibNumber + "\r\n" +
                "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"location\"\r\n\r\n" + location + "\r\n" +
                "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"price\"\r\n\r\n" + price + "\r\n" +
                "--" + boundary + "--\r\n";
        
        byte[] otherFieldsBytes = otherFields.getBytes();
        
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
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        // 處理回應
        if (response.statusCode() == 200) {
            System.out.println("圖片上傳成功！");
            System.out.println("回應: " + response.body());
        } else {
            System.out.println("上傳失敗: " + response.body());
        }
    }
}
```

## 範例文件

本目錄包含以下範例文件：

1. `SimpleUpload.java` - 基本的照片上傳範例
2. `RaceShotApiClient.java` - 封裝的 API 客戶端

## 安裝與執行

### 環境要求

- Java 11 或更高版本
- Maven 或 Gradle（用於管理依賴）

### 使用 Maven

在 `pom.xml` 中添加依賴：

```xml
<dependencies>
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.10.1</version>
    </dependency>
</dependencies>
```

### 使用 Gradle

在 `build.gradle` 中添加依賴：

```gradle
dependencies {
    implementation 'com.google.code.gson:gson:2.10.1'
}
```

### 執行範例

在執行範例之前，請確保您已經：

1. 在 RaceShot 攝影師中心的「API Token 管理」頁面生成了 API Token
2. 修改範例代碼中的配置（API Token、圖片路徑等）

然後編譯並執行：

```bash
javac SimpleUpload.java
java SimpleUpload
```

使用 Maven：

```bash
mvn compile
mvn exec:java -Dexec.mainClass="com.raceshot.example.SimpleUpload"
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
