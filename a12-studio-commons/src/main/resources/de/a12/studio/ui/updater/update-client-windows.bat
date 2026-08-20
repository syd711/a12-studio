@echo off
timeout /T 4 /nobreak
cd /d %~dp0

:update
tar -xf "A12-Studio.zip"
timeout /T 4 /nobreak
del A12-Studio.zip
A12-Studio.exe
exit
