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

import argparse
import calendar
import httplib
import logging
import logging.handlers
import random
import signal
import ssl
import sys
import threading
import time
from functools import wraps

import iotUtils
import mqttHandler


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
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
PUSH_INTERVAL = 2  # time interval between successive data pushes in seconds

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       Logger defaults
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
LOG_FILENAME = "agent.log"
logging_enabled = False
LOG_LEVEL = logging.INFO  # Could be e.g. "DEBUG" or "WARNING"

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       Python version
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
if sys.version_info<(2,6,0):
    sys.stderr.write("You need python 2.6.0 or later to run this script\n")
    exit(1)
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       Define and parse command line arguments
#       If the log file is specified on the command line then override the default
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
PUSH_INTERVAL = 1

parser = argparse.ArgumentParser(description='Connects to drone on ')
parser.add_argument("-i", "--push_interval", type=int, default=1,help="This is the interval which is used to push drone")

args = parser.parse_args()

if args.push_interval:
    PUSH_INTERVAL = args.push_interval


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       Endpoint specific settings to connect with the IoT Server
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
SERVER_ENDPOINT = iotUtils.HTTPS_EP.split(":")
SERVER_IP = SERVER_ENDPOINT[1].replace('//', '')
SERVER_PORT = int(SERVER_ENDPOINT[2])
API_ENDPOINT_CONTEXT = iotUtils.CONTROLLER_CONTEXT
REGISTER_ENDPOINT = str(API_ENDPOINT_CONTEXT) + '/device/register'
INIT_LATITUDE = 19.99
INIT_LONGITUDE = 73.78

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       A class we can use to capture stdout and sterr in the log
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
class IOTLogger(object):
    def __init__(self, logger, level):
        """Needs a logger and a logger level."""
        self.logger = logger
        self.level = level

    def write(self, message):
        if message.rstrip() != "":  # Only log if there is a message (not just a new line)
            self.logger.log(self.level, message.rstrip())


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       Configure logging to log to a file,
#               making a new file at midnight and keeping the last 3 day's data
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def configureLogger(loggerName):
    logger = logging.getLogger(loggerName)
    logger.setLevel(LOG_LEVEL)  # Set the log level to LOG_LEVEL
    handler = logging.handlers.TimedRotatingFileHandler(LOG_FILENAME, when="midnight",
                                                        backupCount=3)  # Handler that writes to a file,
    # ~~~make new file at midnight and keep 3 backups
    formatter = logging.Formatter('%(asctime)s %(levelname)-8s %(message)s')  # Format each log message like this
    handler.setFormatter(formatter)  # Attach the formatter to the handler
    logger.addHandler(handler)  # Attach the handler to the logger

    if (logging_enabled):
        sys.stdout = IOTLogger(logger, logging.INFO)  # Replace stdout with logging to file at INFO level
        sys.stderr = IOTLogger(logger, logging.ERROR)  # Replace stderr with logging to file at ERROR level


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       This method is for register the sensor agent into the Device-Cloud
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def registerAgent():
    ssl.wrap_socket = sslwrap(ssl.wrap_socket) # using the overridden sslwrap that uses TLSv1
    if sys.version_info<(2,7,9):
        dcConncection = httplib.HTTPSConnection(host=SERVER_IP, port=SERVER_PORT)
    else:
        dcConncection = httplib.HTTPSConnection(host=SERVER_IP, port=SERVER_PORT
                                                , context=ssl._create_unverified_context())
    dcConncection.set_debuglevel(1)
    dcConncection.connect()
    PUSH_DATA = iotUtils.DEVICE_INFO
    print PUSH_DATA
    registerURL = str(REGISTER_ENDPOINT)
    dcConncection.putrequest('POST', registerURL)
    dcConncection.putheader('Authorization', 'Bearer ' + iotUtils.AUTH_TOKEN)
    dcConncection.putheader('Content-Type', 'application/json')
    dcConncection.putheader('Content-Length', len(PUSH_DATA))
    dcConncection.endheaders()
    dcConncection.send(PUSH_DATA)
    dcResponse = dcConncection.getresponse()
    if(dcResponse.status < 400):
        iotUtils.IS_REGISTERED = True
        print "Your device has been registered with IoT Server"
    else:
        iotUtils.IS_REGISTERED = False
        print "Your device hasn't been registered with IoT Server"
        print ('agentStats: ' + str(dcResponse.status))
        print ('agentStats: ' + str(dcResponse.reason))
    print '~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~'
    print ('agentStats: ' + str(registerURL))
    print ('agentStats: Response Message')
    print str(dcResponse.msg)
    dcConncection.close()
    print '~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~'

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       This is a Thread object for listening for MQTT Messages
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
class ListenMQTTThread(object):
    def __init__(self):
        thread = threading.Thread(target=self.run, args=())
        thread.daemon = True  # Daemonize thread
        thread.start()  # Start the execution

    def run(self):
        mqttHandler.main()
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

def getSensorValue():
    return iotUtils.generateRandomSensorValues()

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       When sysvinit sends the TERM signal, cleanup before exiting
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def sigterm_handler(_signo, _stack_frame):
    print("[] received signal {}, exiting...".format(_signo))
    sys.exit(0)
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

def generateLatitudeLongitudeValue(lat, lon, num_rows):
    for _ in xrange(num_rows):
        hex1 = '%012x' % random.randrange(16**12) # 12 char random string
        flt = float(random.randint(0,100))
        dec_lat = random.random()/100
        dec_lon = random.random()/100
    return lon+dec_lon, lat+dec_lat

#  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

signal.signal(signal.SIGTERM, sigterm_handler)


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       The Main method of the Agent
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def main():
    configureLogger("agent")
    ListenMQTTThread()
    registerAgent()  # Call the register endpoint and register Device I
    print "----------------------------------------------------------------------"
    while (True):
        if(iotUtils.IS_REGISTERED):
            print "-------------------------------------------------------------------------------------------------"
            currentTime = calendar.timegm(time.gmtime())
            randomLatitudeLongitudeValue = generateLatitudeLongitudeValue(INIT_LATITUDE, INIT_LONGITUDE, 5)
            print randomLatitudeLongitudeValue[1]
            PUSH_DATA = iotUtils.SENSOR_STATS + '"time":'+str(currentTime)+'},"payloadData":{"quatanium_val_q1":34,' \
                                                                           '"quatanium_val_q2":'+str(random.randint(15, 40))+',"quatanium_val_q3":' \
                        +str(random.randint(15, 40))+',"quatanium_val_q4":'+str(random.randint(5, 40))+',"velocity_x":' \
                        +str(random.randint(1, 5))+',"velocity_y":'+str(random.randint(1, 5))+',"velocity_z":' \
                        +str(random.randint(1, 5))+',"global_location_lat":'+str(randomLatitudeLongitudeValue[0]) \
                        +',"global_location_alt":45,"global_location_lon":'+str(randomLatitudeLongitudeValue[1]) \
                        +',"battery_level":'+ str(random.randint(15, 100))+',"battery_voltage":' \
                        +str(random.randint(15, 100))+',"pitch":'+str(random.randint(15, 40))+',"roll":' \
                        +str(random.randint(15, 40))+',"yaw":'+str(random.randint(15, 40))+'}}}';
            mqttHandler.sendSensorValue(PUSH_DATA)
            print '~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Publishing Device-Data ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~'
            print ('PUBLISHED DATA: ' + PUSH_DATA)
            time.sleep(PUSH_INTERVAL)
        else:
            registerAgent()
            time.sleep(PUSH_INTERVAL)
        print "-------------------------------------------------------------------------------------------------"
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

if __name__ == "__main__":
    main()



