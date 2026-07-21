$services = @(
    # 1. CORE SERVICES (Bắt buộc chạy trước)
    "config-server",
    "discovery-server",
    "api-gateway",

    # 2. USER & AUTH
    "user-service",
    "customer-service",

    # 3. PRODUCT & CATALOG
    "category-service",
    "catalog-service",

    # 4. INVENTORY & ORDER
    "inventory-service",
    "order-service",
    "payment-service",

    # 5. CLINICAL & PHARMACY
    "prescription-service",
    "pharmacist-workbench-service",
    "health-tools-service",

    # 6. OPERATIONS & MANAGEMENT
    "branch-service",
    "supplier-service",
    "report-service",
    "notification-service",
    "ecom-ops-service",

    # 7. PORTALS
    "customer-portal-service"
)

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "🚀 Đang khởi động TOÀN BỘ 19 MICROSERVICES..." -ForegroundColor Red
Write-Host "CẢNH BÁO: Quá trình này sẽ tốn RẤT NHIỀU RAM (khoảng 16GB - 24GB)." -ForegroundColor Yellow
Write-Host "=========================================" -ForegroundColor Cyan

# Thiết lập biến môi trường mật khẩu MySQL cho TẤT CẢ các service
$env:MYSQL_PASSWORD = "123456"

foreach ($service in $services) {
    Write-Host "Đang mở cửa sổ chạy: $service" -ForegroundColor Green
    
    # Bọc chuỗi command trong ngoặc kép để tránh lỗi cú pháp CMD
    Start-Process cmd -ArgumentList "/c `"title $service & cd $service & mvn spring-boot:run & pause`""

    
    # Đợi các service lõi lên trước
    if ($service -eq "config-server") { Start-Sleep -Seconds 15 }
    elseif ($service -eq "discovery-server") { Start-Sleep -Seconds 25 }
    elseif ($service -eq "api-gateway") { Start-Sleep -Seconds 15 }
    else { Start-Sleep -Seconds 3 } # Giãn cách 3 giây giữa các service thường để CPU không bị quá tải
}

Write-Host "✅ Đã gửi lệnh khởi động toàn bộ 19 services!" -ForegroundColor Cyan
Write-Host "Bạn sẽ thấy 19 cửa sổ đen (CMD) hiện lên trên màn hình." -ForegroundColor Yellow
Write-Host "Để tắt ứng dụng, bạn hãy tắt (X) từng cửa sổ CMD đi nhé." -ForegroundColor Yellow
Pause
