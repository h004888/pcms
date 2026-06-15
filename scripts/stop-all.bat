@echo off
REM =====================================================
REM PCMS - Stop all running services (Windows)
REM =====================================================

echo Stopping all PCMS services...
taskkill /F /FI "WINDOWTITLE eq PCMS-*" 2>nul
echo Done.
