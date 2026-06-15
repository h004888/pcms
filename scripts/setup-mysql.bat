@echo off
REM =====================================================
REM PCMS - Setup MySQL user + 12 databases (Windows)
REM Run: scripts\setup-mysql.bat
REM =====================================================

echo ======================================
echo   PCMS - MySQL Setup
echo ======================================
echo.
echo This script will create:
echo   - User: pcms_user (password: pcms_pass)
echo   - 12 databases: pcms_user, pcms_branch, ..., pcms_report
echo.
echo It will prompt for MySQL root password.
echo.

mysql -u root -p < "%~dp0setup-mysql.sql"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Setup failed. Please check your MySQL root password.
    exit /b 1
)

echo.
echo MySQL setup complete!
echo.
echo Verify connection with new user:
echo   mysql -u pcms_user -ppcms_pass -e "SHOW DATABASES LIKE 'pcms_%';"
