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

#This script can be used to observe drone attribute (state) changes and push those status changes to xmpp server.

import argparse
import time

from dronekit import connect, VehicleMode
import xmppClient

CONNECTION_TARGET = "/dev/ttyACM0"
PUSH_INTERVAL = 1
BAUD = 57600


parser = argparse.ArgumentParser(description='Connects to drone on ')
parser.add_argument("-c", "--connect", default='/dev/ttyACM0',help="vehicle connection target. Default '/dev/ttyACM0'")
parser.add_argument("-b", '--baud', type=int ,default=57600,help="Serial communication speed. Default 57600")
parser.add_argument("-i", "--push_interval", type=int, default=1,help="This is the interval which is used to push drone"
                                                                      " status to xmpp server")
args = parser.parse_args()

if args.connect:
    CONNECTION_TARGET = args.connect

if args.push_interval:
    PUSH_INTERVAL = args.push_interval

if args.baud:
    BAUD = args.baud

isConnected = xmppClient.connectToXMPPServer()
if isConnected:
    print "\nConnecting to vehicle on: %s" % CONNECTION_TARGET
    print "----------------------------------------------------------------------"
    vehicle = connect(CONNECTION_TARGET, wait_ready=True, baud=BAUD)
    while (True):
        print " Attitude: %s" % vehicle.attitude.yaw
        current_state = "{\"quatanium_val\":["+str(vehicle.attitude.yaw)+","+str(vehicle.attitude.pitch)+ \
                        ", "+str(vehicle.attitude.roll)+"],\"basicParam\":{\"velocity\":["+str(vehicle.velocity[0])+"," \
                        +str(vehicle.velocity[1])+","+str(vehicle.velocity[2])+"],\"global_location\":["+ \
                        str(vehicle.location.global_relative_frame.alt)+","+str(vehicle.location.global_relative_frame.lat)+ \
                        ","+str(vehicle.location.global_relative_frame.lon)+"]},\"battery_level\":"+ \
                        str(vehicle.battery.level)+",\"battery_voltage\":"+str(vehicle.battery.voltage)+ \
                        ",\"device_type\":\"IRIS_DRONE\"}";
        #current_state =  "{\"quatanium_val\":[0.345, 0.567, 0.456, 0.6345],\"basicParam\":{\"velocity\":[3, 2, 1], " \
        #"\"global_location\":[0.567, 2.345, 0.456]},\"battery_level\":56,\"battery_voltage\":34,\"device_type\":\"SIMULATOR\"}";
        xmppClient.sendMessage(current_state)
        time.sleep(PUSH_INTERVAL)
        print "-------------------------------------------------------------------------------------------------"

print "\nClose vehicle object"
vehicle.close()
print("Completed")