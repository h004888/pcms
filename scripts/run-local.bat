@echo off
REM =====================================================
REM PCMS - Start all services (Windows, local dev)
REM =====================================================

set BASE_DIR=%~dp0..
set LOG_DIR=%BASE_DIR%\logs
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

echo ======================================
echo   PCMS - Starting services in order...
echo ======================================

echo [1/3] Starting config-server...
start "PCMS-Config" /B java -jar "%BASE_DIR%\config-server\target\config-server-1.0.0-SNAPSHOT.jar" --spring.profiles.active=native > "%LOG_DIR%\config-server.log" 2>&1
timeout /t 15 /nobreak >nul

echo [2/3] Starting discovery-server...
start "PCMS-Discovery" /B java -jar "%BASE_DIR%\discovery-server\target\discovery-server-1.0.0-SNAPSHOT.jar" > "%LOG_DIR%\discovery-server.log" 2>&1
timeout /t 10 /nobreak >nul

echo [3/3] Starting api-gateway + 12 business services...
start "PCMS-Gateway" /B java -jar "%BASE_DIR%\api-gateway\target\api-gateway-1.0.0-SNAPSHOT.jar" > "%LOG_DIR%\api-gateway.log" 2>&1

for %%s in (user-service branch-service catalog-service category-service supplier-service ^
            inventory-service customer-service order-service payment-service ^
            prescription-service notification-service report-service) do (
  start "PCMS-%%s" /B java -jar "%BASE_DIR%\%%s\target\%%s-1.0.0-SNAPSHOT.jar" > "%LOG_DIR%\%%s.log" 2>&1
  timeout /t 3 /nobreak >nul
)

echo.
echo ======================================
echo   All 15 services starting...
echo ======================================
echo   Eureka Dashboard:  http://localhost:8761
echo   API Gateway:       http://localhost:8080
echo   Logs:              %LOG_DIR%\
echo.
echo Stop with: scripts\stop-all.bat
