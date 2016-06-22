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

import time

#import RPi.GPIO as GPIO
import paho.mqtt.client as mqtt
import iotUtils

global mqttClient
mqttClient = mqtt.Client()

global TOPIC_TO_PUBLISH_STREAM1
global TOPIC_TO_PUBLISH_STREAM2


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       The callback for when the client receives a CONNACK response from the server.
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def on_connect(mqttClient, userdata, flags, rc):
    print("MQTT_LISTENER: Connected with result code " + str(rc))
    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    print ("MQTT_LISTENER: Subscribing with topic " + SUBSCRIBE_FOR_LOCKER_COMMAND)
    mqttClient.subscribe(SUBSCRIBE_FOR_LOCKER_COMMAND)
    print ("MQTT_LISTENER: Subscribing with topic " + SUBSCRIBE_FOR_FAN_COMMAND)
    mqttClient.subscribe(SUBSCRIBE_FOR_FAN_COMMAND)
    print ("MQTT_LISTENER: Subscribing with topic " + SUBSCRIBE_FOR_BULB_COMMAND)
    mqttClient.subscribe(SUBSCRIBE_FOR_BULB_COMMAND)
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       The callback for when a PUBLISH message is received from the server.
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def on_message(mqttClient, userdata, msg):
    print("MQTT_LISTENER: " + msg.topic + " " + str(msg.payload))
    if msg.topic == SUBSCRIBE_FOR_LOCKER_COMMAND:
        request = str(msg.payload)
        resource = ""
        state = request.upper()
        if state == "LOCK":
            # GPIO.output(iotUtils.DOOR_LOCKER_2_PORT, GPIO.HIGH)
            # GPIO.output(iotUtils.DOOR_LOCKER_1_PORT, GPIO.HIGH)
            # GPIO.output(iotUtils.LOCK_STATE_OFF_NOTIFY_PORT, GPIO.HIGH)
            # GPIO.output(iotUtils.LOCK_STATE_ON_NOTIFY_PORT, GPIO.LOW)
            #mqttClient.publish(TOPIC_TO_PUBLISH, "Locker:LOCKED")
            print "Door is locked"
        elif state == "UNLOCK":
            # GPIO.output(iotUtils.DOOR_LOCKER_2_PORT, GPIO.LOW)
            # GPIO.output(iotUtils.DOOR_LOCKER_1_PORT, GPIO.LOW)
            # GPIO.output(iotUtils.LOCK_STATE_OFF_NOTIFY_PORT, GPIO.LOW)
            # GPIO.output(iotUtils.LOCK_STATE_ON_NOTIFY_PORT, GPIO.HIGH)
            #mqttClient.publish(TOPIC_TO_PUBLISH, "Locker:UNLOCKED")
            print "Door is unlocked"
        else:
            print "MQTT message in the wrong format"
            print "MQTT_LISTENER: Resource- " + resource
    elif  msg.topic == SUBSCRIBE_FOR_FAN_COMMAND:
        request = str(msg.payload)
        resource = ""
        state = request.upper()
        if state == "ON":
            # GPIO.output(iotUtils.DOOR_LOCKER_2_PORT, GPIO.HIGH)
            # GPIO.output(iotUtils.DOOR_LOCKER_1_PORT, GPIO.HIGH)
            # GPIO.output(iotUtils.LOCK_STATE_OFF_NOTIFY_PORT, GPIO.HIGH)
            # GPIO.output(iotUtils.LOCK_STATE_ON_NOTIFY_PORT, GPIO.LOW)
            #mqttClient.publish(TOPIC_TO_PUBLISH, "Locker:LOCKED")
            print "Fan is locked"
        elif state == "OFF":
            # GPIO.output(iotUtils.DOOR_LOCKER_2_PORT, GPIO.LOW)
            # GPIO.output(iotUtils.DOOR_LOCKER_1_PORT, GPIO.LOW)
            # GPIO.output(iotUtils.LOCK_STATE_OFF_NOTIFY_PORT, GPIO.LOW)
            # GPIO.output(iotUtils.LOCK_STATE_ON_NOTIFY_PORT, GPIO.HIGH)
            #mqttClient.publish(TOPIC_TO_PUBLISH, "Locker:UNLOCKED")
            print "Fan is off"
        else:
            print "MQTT message in the wrong format"
            print "MQTT_LISTENER: Resource- " + resource
    elif  msg.topic == SUBSCRIBE_FOR_BULB_COMMAND:
        request = str(msg.payload)
        print "bulb is to blow"
        resource = ""
        print request
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       The callback for when a PUBLISH message to the server when door is open or close
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def on_publish(mqttClient, topic, msg):
    mqttClient.publish(topic, msg)


def sendLockerStatus(msg):
    global mqttClient
    on_publish(mqttClient, msg)
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       The Main method of the server script
#			This method is invoked from DoorLockerAgent.py on a new thread
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def main():
    MQTT_ENDPOINT = iotUtils.MQTT_EP.split(":")
    MQTT_IP = MQTT_ENDPOINT[1].replace('//', '')
    MQTT_PORT = int(MQTT_ENDPOINT[2])

    DEV_OWNER = iotUtils.DEVICE_OWNER
    DEV_ID = iotUtils.DEVICE_ID
    DEV_TYPE =iotUtils.DEVICE_TYPE
    TANENT_DOMAIN = iotUtils.SERVER_NAME

    global SUBSCRIBE_FOR_LOCKER_COMMAND
    SUBSCRIBE_FOR_LOCKER_COMMAND = TANENT_DOMAIN + "/" + DEV_TYPE + "/" + DEV_ID + "/command"
    global SUBSCRIBE_FOR_FAN_COMMAND
    SUBSCRIBE_FOR_FAN_COMMAND = TANENT_DOMAIN + "/" + DEV_TYPE + "/" + DEV_ID + "/fan/command"
    global SUBSCRIBE_FOR_BULB_COMMAND
    SUBSCRIBE_FOR_BULB_COMMAND = TANENT_DOMAIN + "/" + DEV_TYPE + "/" + DEV_ID + "/bulb/command"
    global TOPIC_TO_PUBLISH_STREAM1
    TOPIC_TO_PUBLISH_STREAM1 = TANENT_DOMAIN + "/" + DEV_TYPE + "/" + DEV_ID + "/smartLock"
    global TOPIC_TO_PUBLISH_STREAM2
    TOPIC_TO_PUBLISH_STREAM2 = TANENT_DOMAIN + "/" + DEV_TYPE + "/" + DEV_ID + "/smartFan"

    print ("MQTT_LISTENER: MQTT_ENDPOINT is " + str(MQTT_ENDPOINT))
    print ("MQTT_LISTENER: MQTT_TOPIC is " + SUBSCRIBE_FOR_LOCKER_COMMAND)
    global mqttClient
    mqttClient.username_pw_set(iotUtils.AUTH_TOKEN, password = "")
    mqttClient.on_connect = on_connect
    mqttClient.on_message = on_message

    while True:
        try:
            mqttClient.connect(MQTT_IP, MQTT_PORT, 60)
            print "MQTT_LISTENER: " + time.asctime(), "Connected to MQTT Broker - %s:%s" % (MQTT_IP, MQTT_PORT)
            # Blocking call that processes network traffic, dispatches callbacks and
            # handles reconnecting.
            # Other loop*() functions are available that give a threaded interface and a
            # manual interface.
            mqttClient.loop_forever()

        except (KeyboardInterrupt, Exception) as e:
            print "MQTT_LISTENER: Exception in MQTTServerThread (either KeyboardInterrupt or Other)"
            print ("MQTT_LISTENER: " + str(e))

            mqttClient.disconnect()
            print "MQTT_LISTENER: " + time.asctime(), "Connection to Broker closed - %s:%s" % (MQTT_IP, MQTT_PORT)
            print '~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~'
            pass

if __name__ == '__main__':
    main()
