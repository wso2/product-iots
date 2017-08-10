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

import time
import paho.mqtt.client as mqtt
import iotUtils
from token_updater import RefreshToken

global mqttClient
mqttClient = mqtt.Client(client_id='codebasedclient')

agent_connected = False
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       The callback for when the client receives a CONNACK response from the server.
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def on_connect(mqttClient, userdata, flags, rc):

    if rc == 0:
        global agent_connected
        agent_connected = True
        # Subscribing in on_connect() means that if we lose the connection and
        # reconnect then subscriptions will be renewed.
        print ("MQTT_LISTENER: Subscribing with topic " + TOPIC_TO_SUBSCRIBE)
        mqttClient.subscribe(TOPIC_TO_SUBSCRIBE)

    elif rc == 4:
        token = RefreshToken()
        response = token.updateTokens()
        newAccessToken = response['access_token']
        mqttClient.username_pw_set(newAccessToken, password="")
    else:

        global agent_connected
        agent_connected = False

    print("MQTT_LISTENER: Connected with result code " + str(rc))


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       The callback for when a PUBLISH message is received from the server.
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def on_message(mqttClient, userdata, msg):
    print("MQTT_LISTENER: " + msg.topic + " " + str(msg.payload))

    print 'MQTT_LISTENER: Message Received by Device'
    print("MQTT_LISTENER: " + msg.topic + " --> " + str(msg.payload))

    request = str(msg.payload)

    resource = request.split(":")[0].upper()
    state = request.split(":")[1].upper()

    print "MQTT_LISTENER: Resource- " + resource
    print "MQTT_LISTENER: State- " + state

    if resource == "BULB":
        iotUtils.switchBulb(state=state)


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       The callback for when a PUBLISH message to the server
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def on_publish(mqttClient, stream1PlayLoad):
    mqttClient.publish(TOPIC_TO_PUBLISH_STREAM1, stream1PlayLoad)



def sendSensorValue(stream1PlayLoad):
    global mqttClient
    on_publish(mqttClient, stream1PlayLoad)


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

#  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       The Main method of the server script
#			This method is invoked from Agent.py on a new thread
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def main():
    MQTT_ENDPOINT = iotUtils.MQTT_EP.split(":")
    MQTT_IP = MQTT_ENDPOINT[1].replace('//', '')
    MQTT_PORT = int(MQTT_ENDPOINT[2])

    DEV_OWNER = iotUtils.DEVICE_OWNER
    DEV_ID = iotUtils.DEVICE_ID
    DEV_TYPE = iotUtils.DEVICE_TYPE
    TANENT_DOMAIN = iotUtils.SERVER_NAME
    global TOPIC_TO_SUBSCRIBE
    TOPIC_TO_SUBSCRIBE = TANENT_DOMAIN + "/" + DEV_TYPE + "/" + DEV_ID + "/command"
    global TOPIC_TO_PUBLISH_STREAM1
    TOPIC_TO_PUBLISH_STREAM1 = TANENT_DOMAIN + "/" + DEV_TYPE + "/" + DEV_ID + "/sensor_temp"


    print ("MQTT_LISTENER: MQTT_ENDPOINT is " + str(MQTT_ENDPOINT))
    print ("MQTT_LISTENER: MQTT_TOPIC is " + TOPIC_TO_SUBSCRIBE)
    global mqttClient
    mqttClient.username_pw_set(iotUtils.AUTH_TOKEN, password="")
    mqttClient.on_connect = on_connect
    mqttClient.on_message = on_message

    while True:
        try:
            print "MQTT CONNECT CALLED"
            mqttClient.connect(MQTT_IP, MQTT_PORT, 180)
            print "MQTT_LISTENER: " + time.asctime(), "Connected to MQTT Broker - %s:%s" % (
                MQTT_IP, MQTT_PORT)
            mqttClient.loop_forever()

        except (KeyboardInterrupt, Exception) as e:
            print "MQTT_LISTENER: Exception in MQTTServerThread (either KeyboardInterrupt or Other)"
            print ("MQTT_LISTENER: " + str(e))

            mqttClient.disconnect()
            print "MQTT_LISTENER: " + time.asctime(), "Connection to Broker closed - %s:%s" % (
                MQTT_IP, MQTT_PORT)
            print '~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~'
            pass


if __name__ == '__main__':
    main()
