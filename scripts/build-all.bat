@echo off
REM =====================================================
REM PCMS - Build all 15 services (Windows)
REM Run from project root: scripts\build-all.bat
REM =====================================================

echo ======================================
echo   PCMS - Building all services...
echo ======================================

call mvn clean install -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo BUILD FAILED
    exit /b 1
)

echo.
echo All services built successfully!
echo   - JAR files: */target/*.jar
echo   - Run with: scripts\run-local.bat OR docker-compose up
