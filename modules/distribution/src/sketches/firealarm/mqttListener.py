#!/usr/bin/env python

"""
/**
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import iotUtils
#import RPi.GPIO as GPIO
import paho.mqtt.client as mqtt


# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, flags, rc):
    	print("Connected with result code "+str(rc))
        
    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
        print ("Subscribing with topic " + TOPIC)
    	client.subscribe(TOPIC)



# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):
    	print(msg.topic+" "+str(msg.payload))
	
	request = str(msg.payload)

	resource = request.split(":")[0].upper()
	state = request.split(":")[1].upper()

	print "Resource: " + resource 

	if resource == "TEMP":
		pass
		#request.send_response(200)
		#request.send_header("Content-type", "text/plain")
		#request.end_headers()
		#request.wfile.write(LAST_TEMP)
		# return 

	elif resource == "BULB":
                iotUtils.switchBulb(state)
#		print "Requested Switch State: " + state
#		if state == "ON":
#			GPIO.output(BULB_PIN, True)
#			print "BULB Switched ON"
#		elif state == "OFF":
#			GPIO.output(BULB_PIN, False)
#			print "BULB Switched OFF"

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       The Main method of the server script
#			This method is invoked from RaspberryStats.py on a new thread
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def main():
       
    MQTT_ENDPOINT = iotUtils.MQTT_EP.split(":")
    MQTT_IP = MQTT_ENDPOINT[0]
    MQTT_PORT = MQTT_ENDPOINT[1]
        
    DEV_OWNER = iotUtils.DEVICE_OWNER
    DEV_ID = iotUtils.DEVICE_ID
    
    global TOPIC
    TOPIC = "wso2/iot/" + DEV_OWNER + "/firealarm/" + DEV_ID
    
    print ("MQTT_ENDPOINT: " + str(MQTT_ENDPOINT))
    print ("MQTT_TOPIC: " + TOPIC)
    
    mqttClient = mqtt.Client()
    mqttClient.on_connect = on_connect
    mqttClient.on_message = on_message

    while True:
        try:
            mqttClient.connect(MQTT_IP, MQTT_PORT, 60)
            print time.asctime(), "Connected to MQTT Broker - %s:%s" % (MQTT_IP, MQTT_PORT)
            
            # Blocking call that processes network traffic, dispatches callbacks and
            # handles reconnecting.
            # Other loop*() functions are available that give a threaded interface and a
            # manual interface.
            mqttClient.loop_forever()
        
        except (KeyboardInterrupt, Exception) as e:
            print "Exception in MQTTServerThread (either KeyboardInterrupt or Other):"
            print str(e)
            
            mqttClient.disconnect()
            print time.asctime(), "Connection to Broker closed - %s:%s" % (MQTT_IP, MQTT_PORT)
            print '~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~'
            pass


if __name__ == '__main__':
	iotUtils.setUpGPIOPins()
	main()
	
