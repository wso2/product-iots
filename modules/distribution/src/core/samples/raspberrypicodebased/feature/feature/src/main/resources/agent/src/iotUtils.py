#!/usr/bin/env python

"""
/**
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import random

import running_mode

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       Device specific info when pushing data to server
#       Read from a file "deviceConfig.properties" in the same folder level
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
configParser = ConfigParser.RawConfigParser()
configFilePath = os.path.join(os.path.dirname(__file__), './deviceConfig.properties')
configParser.read(configFilePath)

DEVICE_OWNER = configParser.get('Device-Configurations', 'owner')
DEVICE_ID = configParser.get('Device-Configurations', 'deviceId')
DEVICE_NAME = configParser.get('Device-Configurations', 'device-name')
DEVICE_TYPE = configParser.get('Device-Configurations', 'device-type')
SERVER_NAME = configParser.get('Device-Configurations', 'server-name')
MQTT_EP = configParser.get('Device-Configurations', 'mqtt-ep')
AUTH_TOKEN = configParser.get('Device-Configurations', 'auth-token')
REFRESH_TOKEN = configParser.get('Device-Configurations', 'refresh-token')
APPLICATION_KEY = configParser.get('Device-Configurations', 'application-key')
CONTROLLER_CONTEXT = configParser.get('Device-Configurations', 'controller-context')
DEVICE_INFO = '{"owner":"' + DEVICE_OWNER + '","deviceId":"' + DEVICE_ID + '",'
HTTPS_EP = configParser.get('Device-Configurations', 'https-ep')
HTTP_EP = configParser.get('Device-Configurations', 'http-ep')
APIM_EP = configParser.get('Device-Configurations', 'apim-ep')
DEVICE_DATA = '"sensorValue":"{sensorValue}"'
SENSOR_STATS_SENSOR1 = '{{"event":{{"metaData":{{"owner":"' + DEVICE_OWNER + '","deviceType":"' + DEVICE_TYPE \
                       + '","deviceId":"' + DEVICE_ID + '","time":{}}},"payloadData":{{"sensor_temp":{:.2f}}}}}}}'



global LAST_TEMP
LAST_TEMP = 25


TEMPERATURE_READING_INTERVAL_REAL_MODE = 3
TEMPERATURE_READING_INTERVAL_VIRTUAL_MODE = 60
TEMP_PIN = 4
TEMP_SENSOR_TYPE = 11
BULB_PIN = 18


def isEmpty(string):
    if string and string.strip():
        # string is not None AND string is not empty or blank
        return False
    # string is None OR string is empty or blank
    return True

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# Performs the state changing operation of the LED
#
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def switchBulb(state):
    print '~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~'
    print "Requested Switch State: " + state

    if running_mode == "N":

        GPIO.setmode(GPIO.BCM)
        GPIO.setwarnings(False)
        GPIO.setup(BULB_PIN,GPIO.OUT)

        if state == "ON":

            GPIO.output(BULB_PIN,GPIO.HIGH)
            print "BULB Switched ON"

        elif state == "OFF":

            GPIO.output(BULB_PIN,GPIO.LOW)
            print "BULB Switched OFF"

    else:

        if state == "ON":
            print "BULB Switched ON"
        elif state == "OFF":
            print "BULB Switched OFF"

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
# This method generate a random sensor value between 15 and 40
#
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def generateRandomSensorValues():
    return random.randint(15, 40), random.randint(15, 40)
