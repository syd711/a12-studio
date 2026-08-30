if exist Output rmdir /s /q Output
call .\gradlew.bat :a12-studio-ui:buildInstaller -x test -x check --no-parallel
