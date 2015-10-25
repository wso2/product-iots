#!/bin/bash

echo "----------------------------------------------------------------"
echo "|		WSO2 IOT Sample				"
echo "|		  RaspiAlarm				"
echo "|	       ----------------				"
echo "|    ....initializing startup-script	"
echo "----------------------------------------------------------------"


while true; do
read -p "Whats the time-interval (in seconds) between successive Data-Pushes to the WSO2-IoT-Server (ex: '60' indicates 1 minute) > " interval

if [ $interval -eq $interval 2>/dev/null ]
then
echo "Setting data-push interval to " $interval " seconds."
sed s/^push-interval=.*/push-interval=$interval/ deviceConfig.properties > myTmp
mv -f myTmp deviceConfig.properties
break;
else
echo "Input needs to be an integer indicating the number seconds between successive data-pushes."
fi
done

sudo java -jar wso2-firealarm-real-agent.jar