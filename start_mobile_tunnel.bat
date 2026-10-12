@echo off
REM ==========================================================
REM Smart Grocery Assistant - Cloudflare Tunnel for Mobile Access
REM ==========================================================

echo Starting Cloudflare Tunnel to expose http://localhost:8080 to mobile phones...
echo.
echo Look for the link ending in .trycloudflare.com below.
echo Open that link on your mobile phone via 4G/5G or Wi-Fi!
echo.
"%~dp0cloudflared.exe" tunnel --url http://localhost:8080
pause
