using System;
using System.IO;
using System.Threading.Tasks;

namespace RaceShotApiExample
{
    /// <summary>
    /// 主程式入口點
    /// </summary>
    class Program
    {
        static async Task Main(string[] args)
        {
            Console.OutputEncoding = System.Text.Encoding.UTF8;
            Console.WriteLine("運動拍檔 RaceShot 攝影師 API 上傳照片範例");
            Console.WriteLine("===================================");
            
            try
            {
                // 配置
                var config = new
                {
                    // 替換為您的 API Token
                    ApiToken = "YOUR_API_TOKEN",
                    
                    // 替換為您要上傳的圖片路徑
                    ImagePath = Path.Combine(Directory.GetParent(Directory.GetCurrentDirectory()).Parent.Parent.Parent.Parent.FullName, "assets", "sample-image.jpg"),
                    
                    // 活動 ID
                    EventId = "00000",
                    
                    // 號碼布號碼（可選）
                    BibNumber = "123",
                    
                    // 拍攝地點
                    Location = "終點線",
                    
                    // 價格
                    Price = 100m
                };
                
                // 檢查配置
                if (config.ApiToken == "YOUR_API_TOKEN")
                {
                    Console.WriteLine("警告: 請替換 ApiToken 為您的實際 API Token");
                    Console.WriteLine("API Token 可以在運動拍檔 RaceShot 攝影師中心的「[API Token 管理](https://raceshot.com/photographer/api-token)」頁面生成");
                    return;
                }
                
                // 檢查圖片路徑
                if (!File.Exists(config.ImagePath))
                {
                    Console.WriteLine($"錯誤: 找不到圖片文件 {config.ImagePath}");
                    Console.WriteLine("請確保圖片文件存在，或修改 ImagePath 為正確的路徑");
                    return;
                }
                
                // 創建上傳實例
                var uploader = new SimpleUpload(
                    config.ApiToken,
                    config.ImagePath,
                    config.EventId,
                    config.BibNumber,
                    config.Location,
                    config.Price
                );
                
                // 執行上傳
                var result = await uploader.UploadAsync();
                
                // 顯示結果
                if (result.Success)
                {
                    Console.WriteLine("\n上傳成功摘要:");
                    Console.WriteLine($"圖片 ID: {result.PhotoId}");
                    Console.WriteLine($"原始檔案 ID: {result.OriginalFileId}");
                    Console.WriteLine($"Cloudflare ID: {result.CloudflareId}");
                }
                else
                {
                    Console.WriteLine("\n上傳失敗摘要:");
                    Console.WriteLine($"錯誤訊息: {result.Message}");
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine($"程式發生錯誤: {ex.Message}");
                if (ex.InnerException != null)
                {
                    Console.WriteLine($"內部錯誤: {ex.InnerException.Message}");
                }
            }
            
            Console.WriteLine("\n按任意鍵結束...");
            Console.ReadKey();
        }
    }
}

/**
 * 使用說明:
 * 
 * 1. 安裝依賴:
 *    dotnet add package System.Text.Json
 * 
 * 2. 替換 ApiToken 為您的 API Token
 *    API Token 可以在運動拍檔 RaceShot 攝影師中心的「[API Token 管理](https://raceshot.com/photographer/api-token)」頁面生成
 * 
 * 3. 替換 ImagePath 為您要上傳的圖片路徑
 * 
 * 4. 修改其他參數（EventId, BibNumber, Location, Price）
 *    - EventId: 活動 ID，必填
 *    - BibNumber: 號碼布號碼，可選
 *    - Location: 拍攝地點，必填
 *    - Price: 價格，必須大於 60
 * 
 * 5. 執行程式:
 *    dotnet run
 * 
 * 注意:
 * - 圖片必須是有效的圖片檔案（JPEG, PNG）
 * - 價格必須大於 60
 */
