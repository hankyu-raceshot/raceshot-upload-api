# C# 上傳照片範例

本目錄包含使用 C# 調用運動拍檔 RaceShot 攝影師 API 上傳照片的範例代碼。

## 目錄

- [基本用法](#基本用法)
- [範例文件](#範例文件)
- [安裝與執行](#安裝與執行)
- [常見問題](#常見問題)

## 基本用法

使用 RaceShot 攝影師 API 上傳照片的基本流程非常簡單：

```csharp
using System;
using System.IO;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Threading.Tasks;
using Newtonsoft.Json;

namespace RaceShotApiExample
{
    class Program
    {
        static async Task Main(string[] args)
        {
            // 設置 API 配置
            string apiUrl = "https://api.raceshot.com/api/photographer/upload";
            string apiToken = "YOUR_API_TOKEN";
            string imagePath = "photo.jpg";
            string eventId = "00000";
            string bibNumber = "123";
            string location = "終點線";
            decimal price = 100;

            // 創建 HttpClient
            using (var httpClient = new HttpClient())
            {
                // 設置授權標頭
                httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", apiToken);

                // 創建 MultipartFormDataContent
                using (var formData = new MultipartFormDataContent())
                {
                    // 添加圖片
                    var imageContent = new ByteArrayContent(File.ReadAllBytes(imagePath));
                    imageContent.Headers.ContentType = new MediaTypeHeaderValue("image/jpeg");
                    formData.Add(imageContent, "image", Path.GetFileName(imagePath));

                    // 添加其他欄位
                    formData.Add(new StringContent(eventId), "eventId");
                    formData.Add(new StringContent(bibNumber), "bibNumber");
                    formData.Add(new StringContent(location), "location");
                    formData.Add(new StringContent(price.ToString()), "price");

                    // 發送請求
                    var response = await httpClient.PostAsync(apiUrl, formData);
                    var responseContent = await response.Content.ReadAsStringAsync();

                    // 處理回應
                    if (response.IsSuccessStatusCode)
                    {
                        var result = JsonConvert.DeserializeObject<UploadResponse>(responseContent);
                        Console.WriteLine("圖片上傳成功！");
                        Console.WriteLine($"圖片 ID: {result.PhotoId}");
                        Console.WriteLine($"原始檔案 ID: {result.OriginalFileId}");
                        Console.WriteLine($"訊息: {result.Message}");
                    }
                    else
                    {
                        Console.WriteLine($"上傳失敗: {responseContent}");
                    }
                }
            }
        }
    }

    // 回應模型
    class UploadResponse
    {
        [JsonProperty("success")]
        public bool Success { get; set; }

        [JsonProperty("photoId")]
        public string PhotoId { get; set; }

        [JsonProperty("originalFileId")]
        public string OriginalFileId { get; set; }

        [JsonProperty("cloudflareId")]
        public string CloudflareId { get; set; }

        [JsonProperty("message")]
        public string Message { get; set; }
    }
}
```

## 範例文件

本目錄包含以下範例文件：

1. `SimpleUpload.cs` - 基本的照片上傳範例
2. `Program.cs` - 主程式入口點

## 安裝與執行

### 環境要求

- .NET 6.0 或更高版本
- Visual Studio 2022 或 Visual Studio Code

### 安裝依賴

使用 NuGet 安裝所需的套件：

```bash
dotnet add package Newtonsoft.Json
```

### 執行範例

在執行範例之前，請確保您已經：

1. 在運動拍檔 RaceShot 攝影師中心的「[API Token 管理](https://raceshot.com/photographer/api-token)」頁面生成了 API Token
2. 修改範例代碼中的配置（API Token、圖片路徑等）

然後執行：

```bash
dotnet run
```

## 常見問題

### 如何獲取 API Token？

1. 登入運動拍檔 RaceShot 攝影師中心
2. 前往「[API Token 管理](https://raceshot.com/photographer/api-token)」頁面
3. 選擇 Token 有效期並生成 Token
4. 複製並安全保存生成的 Token

### 支援哪些圖片格式？

運動拍檔 RaceShot 攝影師 API 支援以下圖片格式：
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
