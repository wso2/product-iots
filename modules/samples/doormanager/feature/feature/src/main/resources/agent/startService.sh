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
echo "|		                 WSO2 IOT Door Manager				      "
echo "|		                         Agent				              "
echo "|	                     ---------------------				      "
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

cp deviceConfig.properties ./src
chmod +x ./src/DoorManagerAgent.py
sudo python ./src/DoorManagerAgent.py

if [ $? -ne 0 ]; then
	echo "Could not start the service..."
	exit;
fi

echo "--------------------------------------"
echo "|			Successfully Started		"
echo "|		   --------------------------	"
