@echo off
rem ---- Bien dich Chrono Runner bang javac (khong can Maven/Gradle) ----
setlocal
cd /d "%~dp0"

where javac >nul 2>nul
if errorlevel 1 (
    echo [LOI] Khong tim thay javac. Hay cai JDK 17+ va them vao PATH.
    exit /b 1
)

if not exist out mkdir out
dir /s /b src\*.java > sources.txt

echo Dang bien dich...
javac -encoding UTF-8 -d out @sources.txt
if errorlevel 1 (
    echo.
    echo [LOI] Bien dich that bai.
    exit /b 1
)

del sources.txt >nul 2>nul
echo.
echo [OK] Bien dich thanh cong. Chay run.bat de choi.
endlocal
