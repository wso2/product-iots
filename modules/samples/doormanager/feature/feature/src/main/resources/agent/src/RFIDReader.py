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

import signal

import RPi.GPIO as GPIO

import MFRC522
import MQTTHandler
import iotUtils
import argparse
import httplib
import logging
import logging.handlers
import signal
import ssl
import sys
import threading
import time
from functools import wraps

CONTINUE_READING = True

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#      Overriding the default SSL version used in some of the Python (2.7.x) versions
#           This is a known issue in earlier Python releases
#               But was fixed in later versions. Ex-2.7.11
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def sslwrap(func):
    @wraps(func)
    def bar(*args, **kw):
        kw['ssl_version'] = ssl.PROTOCOL_TLSv1
        return func(*args, **kw)
    return bar


# Capture SIGINT for cleanup when the script is aborted
def end_read(signal,frame):
    global CONTINUE_READING
    print "Ctrl+C captured, ending read."
    CONTINUE_READING = False
    GPIO.cleanup()

# Hook the SIGINT
signal.signal(signal.SIGINT, end_read)

# Create an object of the class MFRC522
MIFAREReader = MFRC522.MFRC522()
global RFID_CARD_UID
RFID_CARD_UID = ""


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       Endpoint specific settings to connect with the IoT Server
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
SERVER_ENDPOINT = iotUtils.HTTPS_EP.split(":")
SERVER_IP = SERVER_ENDPOINT[1].replace('//', '')
SERVER_PORT = int(SERVER_ENDPOINT[2])
API_ENDPOINT_CONTEXT = iotUtils.CONTROLLER_CONTEXT
REGISTER_ENDPOINT = str(API_ENDPOINT_CONTEXT) + '/register'
APIM_SERVER_ENDPOINT = iotUtils.API_EP.split(":")
APIM_SERVER_IP = APIM_SERVER_ENDPOINT[1].replace('//', '')
APIM_SERVER_PORT = int(APIM_SERVER_ENDPOINT[2])
MANAGER_API_CONTEXT = iotUtils.MANAGER_CONTEXT
### ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       This method check whether the UID of RFID card is registered or not
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def isRFIDCardRegistered( RFID_CARD_UID ):
    ssl.wrap_socket = sslwrap(ssl.wrap_socket) # using the overridden sslwrap that uses TLSv1
    print "Waiting to get UID of RFID card..."
    if sys.version_info<(2,7,9):
        dcConncection = httplib.HTTPSConnection(host=SERVER_IP, port=SERVER_PORT)
    else:
        dcConncection = httplib.HTTPSConnection(host=SERVER_IP, port=SERVER_PORT, context=ssl._create_unverified_context())
    dcConncection.set_debuglevel(1)
    dcConncection.connect()
    PUSH_DATA = iotUtils.DEVICE_INFO_WITH_OWNER + iotUtils.DEVICE_DATA + RFID_CARD_UID
    PUSH_DATA += '"}'
    print PUSH_DATA
    APIURL = str(MANAGER_API_CONTEXT)
    dcConncection.putrequest('POST', APIURL)
    dcConncection.putheader('Content-Type', 'application/json')
    dcConncection.putheader('Authorization', 'Bearer ' + iotUtils.AUTH_TOKEN)
    dcConncection.putheader('Content-Length', len(PUSH_DATA))
    dcConncection.endheaders()
    dcConncection.send(PUSH_DATA)
    dcResponse = dcConncection.getresponse()
    if(dcResponse.status < 250):
        print "You are allowed to open the door"
        isRegisteredUser = True
    else:
        isRegisteredUser = False
        print "You are not allowed to open the door"
        print ('DoorManager: ' + str(dcResponse.status))
        print ('DoorManager: ' + str(dcResponse.reason))
    print '~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~'
    print ('DoorManager: ' + str(APIURL))
    print ('DoorManager: Response Message')
    print str(dcResponse.msg)
    dcConncection.close()
    print '~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~'
    return isRegisteredUser
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~------
#       Read RFID card and if user is authorized to given RFID card, then open the lock
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~------
def readUIDofRFID():
    print "-----------------------------------Starting MFRC522 chip reader---------------------------------------------"
    # This loop keeps checking for chips. If one is near it will get the UID and authenticate
    while CONTINUE_READING:
        # Scan for cards
        (status,TagType) = MIFAREReader.MFRC522_Request(MIFAREReader.PICC_REQIDL)
        # If a card is found
        # Get the UID of the card
        (status,uid) = MIFAREReader.MFRC522_Anticoll()
        # If we have the UID, continue
        if status == MIFAREReader.MI_OK:
             global RFID_CARD_UID
             RFID_CARD_UID = str(uid[0])+":"+str(uid[1])+":"+str(uid[2])+":"+str(uid[3])
             print "Card read UID: " + RFID_CARD_UID
             # This is the default key for authentication
             key = [0xFF,0xFF,0xFF,0xFF,0xFF,0xFF]
             # Select the scanned tag
             MIFAREReader.MFRC522_SelectTag(uid)
             # Authenticate
             status = MIFAREReader.MFRC522_Auth(MIFAREReader.PICC_AUTHENT1A, 8, key, uid)
             # Check if authenticated
             if status == MIFAREReader.MI_OK:
                 MIFAREReader.MFRC522_Read(8)
                 MIFAREReader.MFRC522_StopCrypto1()
             if(isRFIDCardRegistered(RFID_CARD_UID)):
                 print "Authentication successful"
                 GPIO.output(iotUtils.LOCK_STATE_OFF_NOTIFY_PORT, GPIO.LOW)
                 GPIO.output(iotUtils.LOCK_STATE_ON_NOTIFY_PORT, GPIO.HIGH)
                 GPIO.output(iotUtils.DOOR_LOCKER_2_PORT, GPIO.LOW)
                 GPIO.output(iotUtils.DOOR_LOCKER_1_PORT, GPIO.LOW)
                 MQTTHandler.on_publish(MQTTHandler.mqttClient,"Locker:UNLOCKED")
    return True

def main():
    global RFID_CARD_UID
    RFID_CARD_UID = ""
    readUIDofRFID()

if __name__ == '__main__':
	main()

