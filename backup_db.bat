@echo off
REM ==========================================================
REM Smart Grocery Assistant - MySQL Backup Script
REM ==========================================================

set BACKUP_DIR=backups
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

for /f "tokens=2-4 delims=/ " %%a in ('date /t') do (set mydate=%%c-%%a-%%b)
for /f "tokens=1-2 delims=/:" %%a in ("%TIME%") do (set mytime=%%a%%b)
set BACKUP_FILE=%BACKUP_DIR%\grocery_db_backup_%mydate%_%mytime%.sql

echo Backing up database 'grocery_db' to %BACKUP_FILE%...
"C:\Program Files\MySQL\MySQL Workbench 8.0 CE\mysqldump.exe" -u root -p grocery_db > "%BACKUP_FILE%"

if %ERRORLEVEL% equ 0 (
    echo Backup completed successfully: %BACKUP_FILE%
) else (
    echo Backup failed with error code %ERRORLEVEL%
)
pause
