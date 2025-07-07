using System;
using System.IO;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Threading.Tasks;
using System.Text.Json;

namespace RaceShotApiExample
{
    /// <summary>
    /// RaceShot 攝影師 API 上傳照片範例
    /// 此類別提供使用 C# 調用 RaceShot 攝影師 API 上傳照片的方法
    /// </summary>
    public class SimpleUpload
    {
        // API 配置
        private readonly string _apiToken;
        private readonly string _apiUrl;
        private readonly string _imagePath;
        private readonly string _eventId;
        private readonly string _bibNumber;
        private readonly string _location;
        private readonly decimal _price;

        /// <summary>
        /// 初始化 SimpleUpload 類別的新實例
        /// </summary>
        /// <param name="apiToken">API Token</param>
        /// <param name="imagePath">圖片路徑</param>
        /// <param name="eventId">活動 ID</param>
        /// <param name="bibNumber">號碼布號碼（可選）</param>
        /// <param name="location">拍攝地點</param>
        /// <param name="price">價格（須大於 60）</param>
        public SimpleUpload(
            string apiToken,
            string imagePath,
            string eventId,
            string bibNumber,
            string location,
            decimal price)
        {
            _apiToken = apiToken ?? throw new ArgumentNullException(nameof(apiToken));
            _imagePath = imagePath ?? throw new ArgumentNullException(nameof(imagePath));
            _eventId = eventId ?? throw new ArgumentNullException(nameof(eventId));
            _bibNumber = bibNumber ?? "unknown";
            _location = location ?? throw new ArgumentNullException(nameof(location));
            _price = price;
            _apiUrl = "https://api.raceshot.app/api/photographer/upload";

            // 驗證價格
            if (price < 60)
            {
                throw new ArgumentException("價格必須大於 60", nameof(price));
            }

            // 驗證圖片檔案存在
            if (!File.Exists(imagePath))
            {
                throw new FileNotFoundException("找不到圖片檔案", imagePath);
            }
        }

        /// <summary>
        /// 上傳圖片到 RaceShot
        /// </summary>
        /// <returns>上傳結果</returns>
        public async Task<UploadResult> UploadAsync()
        {
            Console.WriteLine("正在讀取圖片...");
            
            // 獲取檔案名稱
            string fileName = Path.GetFileName(_imagePath);
            Console.WriteLine($"正在上傳圖片: {fileName}");
            Console.WriteLine($"活動 ID: {_eventId}, 號碼布: {_bibNumber}");
            
            try
            {
                // 創建 HttpClient
                using var httpClient = new HttpClient();
                
                // 設置授權標頭
                httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", _apiToken);
                
                // 創建 MultipartFormDataContent
                using var formData = new MultipartFormDataContent();
                
                // 添加圖片
                byte[] imageData = await File.ReadAllBytesAsync(_imagePath);
                var imageContent = new ByteArrayContent(imageData);
                
                // 設置內容類型（根據檔案擴展名）
                string extension = Path.GetExtension(_imagePath).ToLower();
                string contentType = extension switch
                {
                    ".jpg" or ".jpeg" => "image/jpeg",
                    ".png" => "image/png",
                    ".heif" or ".heic" => "image/heif",
                    _ => "application/octet-stream"
                };
                
                imageContent.Headers.ContentType = new MediaTypeHeaderValue(contentType);
                formData.Add(imageContent, "image", fileName);
                
                // 添加其他欄位
                formData.Add(new StringContent(_eventId), "eventId");
                formData.Add(new StringContent(_bibNumber), "bibNumber");
                formData.Add(new StringContent(_location), "location");
                formData.Add(new StringContent(_price.ToString()), "price");
                
                // 發送請求
                Console.WriteLine("正在發送 API 請求...");
                HttpResponseMessage response = await httpClient.PostAsync(_apiUrl, formData);
                string responseContent = await response.Content.ReadAsStringAsync();
                
                // 處理回應
                if (response.IsSuccessStatusCode)
                {
                    // 解析 JSON 回應
                    var options = new JsonSerializerOptions
                    {
                        PropertyNameCaseInsensitive = true
                    };
                    
                    var result = JsonSerializer.Deserialize<UploadResult>(responseContent, options);
                    
                    if (result?.Success == true)
                    {
                        Console.WriteLine("✅ 圖片上傳成功!");
                        Console.WriteLine($"圖片 ID: {result.PhotoId}");
                        Console.WriteLine($"原始檔案 ID: {result.OriginalFileId}");
                        Console.WriteLine($"訊息: {result.Message}");
                        return result;
                    }
                    else
                    {
                        Console.WriteLine("❌ 上傳失敗: API 回應成功但結果不成功");
                        return new UploadResult
                        {
                            Success = false,
                            Message = "API 回應成功但結果不成功"
                        };
                    }
                }
                else
                {
                    Console.WriteLine($"❌ 上傳失敗: HTTP 狀態碼 {(int)response.StatusCode}");
                    Console.WriteLine($"回應內容: {responseContent}");
                    
                    // 嘗試解析錯誤訊息
                    try
                    {
                        var errorResponse = JsonSerializer.Deserialize<ErrorResult>(responseContent);
                        return new UploadResult
                        {
                            Success = false,
                            Message = errorResponse?.Error ?? "未知錯誤"
                        };
                    }
                    catch
                    {
                        return new UploadResult
                        {
                            Success = false,
                            Message = $"HTTP 錯誤: {(int)response.StatusCode}"
                        };
                    }
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine($"發生錯誤: {ex.Message}");
                return new UploadResult
                {
                    Success = false,
                    Message = ex.Message
                };
            }
        }
    }

    /// <summary>
    /// 上傳結果模型
    /// </summary>
    public class UploadResult
    {
        /// <summary>
        /// 上傳是否成功
        /// </summary>
        public bool Success { get; set; }
        
        /// <summary>
        /// 圖片 ID
        /// </summary>
        public string PhotoId { get; set; }
        
        /// <summary>
        /// 原始檔案 ID
        /// </summary>
        public string OriginalFileId { get; set; }
        
        /// <summary>
        /// Cloudflare ID
        /// </summary>
        public string CloudflareId { get; set; }
        
        /// <summary>
        /// 回應訊息
        /// </summary>
        public string Message { get; set; }
    }

    /// <summary>
    /// 錯誤回應模型
    /// </summary>
    public class ErrorResult
    {
        /// <summary>
        /// 錯誤訊息
        /// </summary>
        public string Error { get; set; }
    }
}
