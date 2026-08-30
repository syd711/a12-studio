@echo off
setlocal enabledelayedexpansion

rem Sets the project version used by the installer (build.gradle -> subprojects { version = ... }).
rem Usage: set-version.bat 2026.06-ext0-0.0.1

if "%~1"=="" (
    echo Usage: %~n0 ^<version^>
    echo Example: %~n0 2026.06-ext0-0.0.1
    exit /b 1
)

set "NEW_VERSION=%~1"
set "BUILD_GRADLE=%~dp0build.gradle"

if not exist "%BUILD_GRADLE%" (
    echo build.gradle not found at "%BUILD_GRADLE%"
    exit /b 1
)

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$path = '%BUILD_GRADLE%';" ^
    "$pattern = '(?m)^(\s*version\s*=\s*)''[^'']*''';" ^
    "$content = Get-Content -Raw -LiteralPath $path;" ^
    "if ($content -notmatch $pattern) { Write-Error 'version assignment not found in build.gradle'; exit 1 }" ^
    "$updated = [regex]::Replace($content, $pattern, ('${1}''%NEW_VERSION%'''));" ^
    "Set-Content -LiteralPath $path -Value $updated -NoNewline"

if errorlevel 1 (
    echo Failed to update version in "%BUILD_GRADLE%"
    exit /b 1
)

echo Updated build.gradle version to %NEW_VERSION%
