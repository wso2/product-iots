#!/bin/bash
ps aux|grep wso2server.py|awk '{print $2}'|xargs kill -9
ps aux|grep httpserver.py|awk '{print $2}'|xargs kill -9
#xdotool mousemove 0 0
#xdotool search -name LXTerminal windowunmap
#cd ~/
cd ./DigitalDisplay/
mkdir -p tmp/dd-kernel-test
python displayagent.py
while true; do
sleep 100
done
