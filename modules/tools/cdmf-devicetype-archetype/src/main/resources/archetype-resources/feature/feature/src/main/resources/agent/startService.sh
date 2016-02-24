#"""
#/**
#* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
echo "|		                 WSO2 IOT Sample				          "
echo "|		                      Agent				              "
echo "|	                     ----------------				          "
echo "|                ....initializing startup-script	              "
echo "----------------------------------------------------------------"

currentDir=$PWD

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

cd $currentDir

#while true; do
read -p "Whats the time-interval (in seconds) between successive Data-Pushes to the WSO2-DC (ex: '60' indicates 1 minute) > " input
if [ $input -eq $input 2>/dev/null ]
then
   echo "Setting data-push interval to $input seconds."
else
   echo "Input needs to be an integer indicating the number seconds between successive data-pushes. 15 will be taken as default value"
   $input=15
fi
#done
cp deviceConfig.properties ./src
chmod +x ./src/agent.py
./src/agent.py -i $input

if [ $? -ne 0 ]; then
	echo "Could not start the service..."
	exit;
fi


echo "--------------------------------------------------------------------------"
echo "|			Successfully Started		"
echo "|		   --------------------------	"
