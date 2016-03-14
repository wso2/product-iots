#!/usr/bin/env python

"""
/**
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
**/
"""

import ConfigParser
import os


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       Device specific info when pushing data to server
#       Read from a file "deviceConfig.properties" in the same folder level
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
configParser = ConfigParser.RawConfigParser()
configFilePath = os.path.join(os.path.dirname(__file__), './deviceConfig.properties')
configParser.read(configFilePath)

DEVICE_OWNER = configParser.get('Device-Configurations', 'owner')
DEVICE_ID = configParser.get('Device-Configurations', 'deviceId')
SERIAL_NUMBER = configParser.get('Device-Configurations', 'deviceId')
MQTT_EP = configParser.get('Device-Configurations', 'mqtt-ep')
AUTH_TOKEN = configParser.get('Device-Configurations', 'auth-token')
USER_NAME = configParser.get('Device-Configurations', 'username')
CONTROLLER_CONTEXT = configParser.get('Device-Configurations', 'controller-context')
DEVICE_INFO = '{"owner":"' + DEVICE_OWNER + '","deviceId":"' + DEVICE_ID + '",'
HTTPS_EP = configParser.get('Device-Configurations', 'https-ep')
HTTP_EP = configParser.get('Device-Configurations', 'http-ep')
DEVICE_INFO_WITH_OWNER = '{"serialNumber":"' + SERIAL_NUMBER + '", "userName":"'+ USER_NAME +'","deviceId":"' + SERIAL_NUMBER + '",'
API_EP = configParser.get('Device-Configurations', 'apim-ep')
MANAGER_CONTEXT = configParser.get('Device-Configurations', 'manager-context')
DEVICE_DATA = '"cardNumber":"'
### ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~



global IS_REGISTERED
IS_REGISTERED = False

DOOR_LOCKER_1_PORT = 16
DOOR_LOCKER_2_PORT = 18
LOCK_STATE_ON_NOTIFY_PORT = 11
LOCK_STATE_OFF_NOTIFY_PORT = 13

def isEmpty (string):
    if string and string.strip():
        #string is not None AND string is not empty or blank
        return False
    #string is None OR string is empty or blank
    return True



