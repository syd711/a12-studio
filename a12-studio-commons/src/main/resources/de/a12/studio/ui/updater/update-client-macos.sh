#!/bin/sh
sleep 4
echo "Unzipping jar..." >> '{{MAC_WRITE_PATH}}Logs/a12-studio-ui.log' 2>&1
unzip -o '{{MAC_WRITE_PATH}}/A12-Studio.zip' -d '{{MAC_WRITE_PATH}}_updatefolder' >> '{{MAC_WRITE_PATH}}Logs/a12-studio-ui.log' 2>&1
echo "Removing zip..." >> '{{MAC_WRITE_PATH}}Logs/a12-studio-ui.log' 2>&1
rm A12-Studio.zip >> '{{MAC_WRITE_PATH}}Logs/a12-studio-ui.log' 2>&1
echo "Closing app..." >> '{{MAC_WRITE_PATH}}Logs/a12-studio-ui.log' 2>&1
killall A12-Studio >> '{{MAC_WRITE_PATH}}Logs/a12-studio-ui.log' 2>&1
echo "Moving jar..." >> '{{MAC_WRITE_PATH}}Logs/a12-studio-ui.log' 2>&1
cp -vf '{{MAC_WRITE_PATH}}_updatefolder/a12-studio-ui.jar' '{{MAC_JAR_PATH}}' >> '{{MAC_WRITE_PATH}}Logs/a12-studio-ui.log' 2>&1
echo "Removing _updatefolder..." >> '{{MAC_WRITE_PATH}}Logs/a12-studio-ui.log' 2>&1
rm -rf '{{MAC_WRITE_PATH}}_updatefolder' >> '{{MAC_WRITE_PATH}}Logs/a12-studio-ui.log' 2>&1
echo "Restarting client..." >> '{{MAC_WRITE_PATH}}Logs/a12-studio-ui.log' 2>&1
open -n {{MAC_APP_PATH}} >> '{{MAC_WRITE_PATH}}Logs/a12-studio-ui.log' 2>&1
