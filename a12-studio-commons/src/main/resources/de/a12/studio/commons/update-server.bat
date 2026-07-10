@echo off
timeout /T 8 /nobreak
cd /d %~dp0

set retries=0

:waitforfile
del A12-Studio-Server.exe >nul 2>&1
if exist A12-Studio-Server.exe (
    set /a retries+=1
    if %retries% geq 15 (
        echo %date% %time% ERROR: Failed to delete A12-Studio-Server.exe after 15 retries >> a12-studio-server.log
        pause
        exit /b 1
    )
    timeout /T 2 /nobreak
    goto waitforfile
)

timeout /T 5 /nobreak

set retries=0

:update
tar -xf "A12-Studio-Server.zip"
if errorlevel 1 (
    set /a retries+=1
    if %retries% geq 20 (
        echo %date% %time% ERROR: Extraction failed after 20 retries >> a12-studio-server.log
        pause
        exit /b 1
    )
    timeout /T 3 /nobreak
    goto update
)
timeout /T 4 /nobreak
del A12-Studio-Server.zip
A12-Studio-Server.exe
exit
