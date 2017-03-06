#"""
#/**
#* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
#*
#* WSO2 Inc. licenses this file to you under the Apache License,
#* Version 2.0 (the "License"); you may not use this file except
#* in compliance with the License.
#* You may obtain a copy of the License at
#*
#* http://www.apache.org/licenses/LICENSE-2.0
#*
#* Unless required by applicable law or agreed to in writing,
#* software distributed under the License is distributed on an
#* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#* KIND, either express or implied. See the License for the
#* specific language governing permissions and limitations
#* under the License.
#**/
#"""

#!/bin/bash

echo "----------------------------------------------------------------"
echo "|		  WSO2 IOT Sample				"
echo "|		  Drone Analyzer				"
echo "|	       ----------------				"
echo "|    ....initializing startup-script	"
echo "----------------------------------------------------------------"

currentDir=$PWD


while true; do
    read -p "Do you wish to run 'apt-get update' and continue? [Yes/No] " yn
    case $yn in
        [Yy]* ) sudo apt-get update;
				break;;
        [Nn]* ) echo "Continuing without apt-get update...";
				break;;
        * ) echo "Please answer yes or no.";
    esac
done

for f in ./deviceConfig.properties; do
    ## Check if the glob gets expanded to existing files.
    ## If not, f here will be exactly the pattern above
    ## and the exists test will evaluate to false.
    if [ -e "$f" ]; then
    	echo "Configuration file found......"
    else
    	echo "'deviceConfig.properties' file does not exist in current path. \nExiting installation...";
    	exit;
    fi
    ## This is all we needed to know, so we can break after the first iteration
    break
done

#installing dependencies
sudo apt-get install python-pip python-dev
git clone git://git.eclipse.org/gitroot/paho/org.eclipse.paho.mqtt.python.git
cd org.eclipse.paho.mqtt.python
sudo python setup.py install
cd ../
cp deviceConfig.properties ./src

while true; do
    read -p "Do you wish to run this as simulator if it is so enter yes and continue? [Yes/No] " yn
    case $yn in
        [Yy]* ) echo "Continuing with simulator...";
                while true; do
                    read -p "Whats the time-interval (in seconds) between successive Data-Pushes to the DAS  (ex: '60' indicates 1 minute) > " push_interval
                    if [ $input -eq $input 2>/dev/null ]
                    then
                        echo "Setting data-push interval to $push_interval seconds."
                        break
                    else
                        echo "Input needs to be an integer indicating the number seconds between successive data-pushes."
                    fi
                done
                python ./src/droneSimulator.py -i $push_interval
				break;;
        [Nn]* ) echo "Continuing with real device...";
                sudo pip install dronekit
                while true; do
                    read -p "Whats the time-interval (in seconds) between successive Data-Pushes to the DAS  (ex: '60' indicates 1 minute) > " push_interval
                    read -p "Vehicle connection target. Default '/dev/ttyACM0' :" connection_target
                    read -p "Serial communication speed. Default 57600 :" baud

                    if [ $input -eq $input 2>/dev/null ]
                    then
                        echo "Setting data-push interval to $push_interval seconds."
                        break
                    else
                        echo "Input needs to be an integer indicating the number seconds between successive data-pushes."
                    fi
                done
                python ./src/drone.py -i $push_interval -b $baud -c $connection_target
				break;;
        * ) echo "Please answer yes or no.";
    esac
done

echo "Could not start the service..."
exit;
