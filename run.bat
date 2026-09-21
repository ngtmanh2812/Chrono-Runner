@echo off
rem ---- Chay Chrono Runner ----
setlocal
cd /d "%~dp0"

if not exist out\chronorunner\Main.class (
    echo Chua co ban bien dich, dang build...
    call build.bat
    if errorlevel 1 exit /b 1
)

java -cp out chronorunner.Main
endlocal
