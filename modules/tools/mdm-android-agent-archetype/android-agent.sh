#!/bin/sh
#
# Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
#
# WSO2 Inc. licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file except
# in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

#
# Script to generate WSO2 MDM Android agent
#

clear
echo "Please provide the ClientKey: "
read clientKey
echo "Please provide the ClientSecret: "
read clientSecret

# Default Target Android SDK version
DEFAULT_TARGET_SDK="21"

echo "************** Prerequisites verification started **************"

echo "Verifying the Apache Maven installation"
if [ -d "$M2_HOME" ];then
echo $M2_HOME
else
echo "Apache Maven is not installed in the system. Please install Apache Maven 3.1.1 or higher and set M2_HOME environment variable."
exit -1
fi

echo "Verifying the Android SDK installation"
if [ -d "$ANDROID_HOME" ];then
echo $ANDROID_HOME
else
echo "Android SDK is not installed in the system. Please install the Android SDK and set ANDROID_HOME environment variable."
exit -1
fi

echo "************** Prerequisites verification completed **************"

echo "************** Verifying the arguments **************"

if [ -n "$clientKey" ];then
echo "Setting the clientKey to $clientKey"
else
echo "Client-Key is not defined."
exit 1;
fi

if [ -n "$clientSecret" ];then
echo "Setting the clientSecret to $clientSecret"
else
echo "Client-Secret is not defined."
exit 1;
fi

echo "************** Argument verification completed **************"

echo "************** Installing WSO2 MDM Android Agent Archetype to the maven repo **************"

mvn install:install-file -Dfile=mdm-android-agent-archetype-1.0.0-SNAPSHOT.jar -DgroupId=org.wso2.mdmr -DartifactId=mdm-android-agent-archetype -Dversion=1.0.0-SNAPSHOT -Dpackaging=jar -DgeneratePom=true


echo "************** Generating WSO2 MDM Android agent project ***************"
mvn archetype:generate -B -DarchetypeGroupId=org.wso2.mdm -DarchetypeArtifactId=mdm-android-agent-archetype -DarchetypeVersion=1.0.0-SNAPSHOT -DgroupId=org.wso2.mdm -DartifactId=mdm-android-client -DclientKey=$clientKey -DclientSecret=$clientSecret


echo "************** Adding IDPProxy to maven repo ***************"
mvn -f mdm-android-client/src/main/plugins/IDPProxy/pom.xml clean install


echo "************** Building MDM Android Agent ***************"
mvn -f mdm-android-client/pom.xml clean install

exit 0