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
import csv
import httplib
import logging
import logging.handlers
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
INIT_LATITUDE = 51.58224901044347
INIT_LONGITUDE = 0.1117214017060059
SPEED_LIMIT_MIN = 0.005
SPEED_LIMIT_MAX = 0.00005
HEADING_MAX_LIMIT =  98
HEADING_MIN_LIMIT = 100

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
parser = argparse.ArgumentParser(description="Python service to push RPi info to the Device Cloud")
parser.add_argument("-l", "--log", help="file to write log to (default '" + LOG_FILENAME + "')")

help_string_for_data_push_interval = "time interval between successive locker status push to server(default '" + \
                                     str(PUSH_INTERVAL) + "')"
parser.add_argument("-i", "--interval", type=int, help=help_string_for_data_push_interval)

args = parser.parse_args()
if args.log:
    LOG_FILENAME = args.log

if args.interval:
    PUSH_INTERVAL = args.interval


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       Endpoint specific settings to connect with the IoT Server
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
SERVER_ENDPOINT = iotUtils.HTTPS_EP.split(":")
SERVER_IP = SERVER_ENDPOINT[1].replace('//', '')
SERVER_PORT = int(SERVER_ENDPOINT[2])
API_ENDPOINT_CONTEXT = iotUtils.CONTROLLER_CONTEXT
REGISTER_ENDPOINT = str(API_ENDPOINT_CONTEXT) + '/device/register'
PUSH_SENSOR_VALUE_ENDPOINT = str(API_ENDPOINT_CONTEXT) + '/push-sensor-value'


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
    #TODO need to get server certificate when initializing https connection
    dcConncection.set_debuglevel(1)
    dcConncection.connect()
    PUSH_DATA = iotUtils.DEVICE_INFO + iotUtils.DEVICE_DATA.format(sensorValue=0.0)
    PUSH_DATA += '}'
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


#       When sysvinit sends the TERM signal, cleanup before exiting
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def sigterm_handler(_signo, _stack_frame):
    print("[] received signal {}, exiting...".format(_signo))
    sys.exit(0)
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

signal.signal(signal.SIGTERM, sigterm_handler)

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       The Main method of the Agent
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def main():
    configureLogger("agent")
    ListenMQTTThread()
    registerAgent()  # Call the register endpoint and register Device I
    while True:
        try:
            if(iotUtils.IS_REGISTERED):
                currentTime = calendar.timegm(time.gmtime())
                latitudeLongitudeValue = iotUtils.generateLatitudeLongitudeValue(1)
                PUSH_DATA_TO_STREAM = iotUtils.SENSOR_STATS.format(currentTime
                   , latitudeLongitudeValue[1],latitudeLongitudeValue[0], iotUtils.VEHICLE_TYPE
                   , iotUtils.generateRandomValues(SPEED_LIMIT_MIN, SPEED_LIMIT_MAX)
                   ,iotUtils.generateRandomValues(HEADING_MIN_LIMIT, HEADING_MAX_LIMIT)
                   , "normal", "This is a sample device type", True)
                mqttHandler.sendSensorValue(PUSH_DATA_TO_STREAM)
                print '~~~~~~~~~~~~~~~~~~~~~~~~ Publishing Device-Data ~~~~~~~~~~~~~~~~~~~~~~~~~'
                print ('PUBLISHED DATA STREAM : ' + PUSH_DATA_TO_STREAM)
                time.sleep(PUSH_INTERVAL)
            else:
                registerAgent()
                time.sleep(PUSH_INTERVAL)
            time.sleep(PUSH_INTERVAL)
        except (KeyboardInterrupt, Exception) as e:
            print "agentStats: Exception in AgentThread (either KeyboardInterrupt or Other)"
            print ("agentStats: " + str(e))
            print '~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~'
            pass
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

if __name__ == "__main__":
    main()

