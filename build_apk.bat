@echo off
REM ==========================================================
REM Smart Grocery Assistant - Build Android APK
REM ==========================================================

echo Building Smart Grocery Assistant Android APK...
cd /d "%~dp0android_app"

call "C:\Users\victus\.gradle\wrapper\dists\gradle-8.13-all\54h0s9kvb6g2sinako7ub77ku\gradle-8.13\bin\gradle.bat" assembleDebug

if %ERRORLEVEL% equ 0 (
    echo.
    echo Copying generated APK to project root...
    copy /Y "%~dp0android_app\app\build\outputs\apk\debug\app-debug.apk" "%~dp0SmartGroceryAssistant.apk"
    echo.
    echo ==========================================================
    echo SUCCESS: APK is ready at:
    echo %~dp0SmartGroceryAssistant.apk
    echo ==========================================================
) else (
    echo.
    echo Build failed. Please review errors above.
)

pause
