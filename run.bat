@echo off
REM ==========================================================
REM Smart Grocery Assistant - Windows Startup Script
REM ==========================================================

echo Starting Smart Grocery Assistant...
echo Java Version:
java -version

echo.
echo Launching Spring Boot Application...
"C:\Program Files\apache-maven-3.9.11\bin\mvn.cmd" spring-boot:run
pause
