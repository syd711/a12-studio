#!/bin/bash
sleep 4
unzip -o -j A12-Studio.zip a12-studio-ui.jar -d lib/app
rm A12-Studio.zip
./bin/A12-Studio &
