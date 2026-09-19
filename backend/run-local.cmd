@echo off
setlocal EnableExtensions DisableDelayedExpansion

set "SCRIPT_DIR=%~dp0"
set "ENV_FILE=%SCRIPT_DIR%..\docker\.env"

if not exist "%ENV_FILE%" (
    echo [ERROR] docker\.env was not found.
    echo Run: powershell -NoProfile -ExecutionPolicy Bypass -File docker\setup-local-env.ps1
    exit /b 1
)

for /f "usebackq eol=# tokens=1,* delims==" %%A in ("%ENV_FILE%") do (
    if /I "%%A"=="DB_URL" set "DB_URL=%%B"
    if /I "%%A"=="DB_USERNAME" set "DB_USERNAME=%%B"
    if /I "%%A"=="DB_PASSWORD" set "DB_PASSWORD=%%B"
)

if not defined DB_URL set "DB_URL=jdbc:postgresql://127.0.0.1:5432/ai_interview"

if not defined DB_USERNAME (
    echo [ERROR] DB_USERNAME is missing from docker\.env.
    exit /b 1
)

if not defined DB_PASSWORD (
    echo [ERROR] DB_PASSWORD is missing from docker\.env.
    exit /b 1
)

pushd "%SCRIPT_DIR%" >nul
call gradlew.bat bootRun %*
set "EXIT_CODE=%ERRORLEVEL%"
popd >nul

exit /b %EXIT_CODE%
