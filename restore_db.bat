@echo off
REM ==========================================================
REM Smart Grocery Assistant - MySQL Restore Script
REM ==========================================================

if "%~1"=="" (
    echo Usage: restore_db.bat ^<path_to_backup_file.sql^>
    echo Example: restore_db.bat backups\grocery_db_backup_2026-10-12_0830.sql
    exit /b 1
)

set BACKUP_FILE=%~1

if not exist "%BACKUP_FILE%" (
    echo Error: Backup file '%BACKUP_FILE%' does not exist.
    exit /b 1
)

echo Restoring database 'grocery_db' from %BACKUP_FILE%...
"C:\Program Files\MySQL\MySQL Workbench 8.0 CE\mysql.exe" -u root -p grocery_db < "%BACKUP_FILE%"

if %ERRORLEVEL% equ 0 (
    echo Database restored successfully from %BACKUP_FILE%
) else (
    echo Restore failed with error code %ERRORLEVEL%
)
pause
