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


import logging, logging.handlers
import sys, os, signal, argparse
import httplib, time  
import threading
import Adafruit_DHT             # Adafruit library required for temperature sensing

import iotUtils
import httpServer               # python script used to start a http-server to listen for operations (includes the TEMPERATURE global variable)
import xmppServer               # python script used to communicate with xmpp server
import mqttListener             # python script used to accept messages via mqtt



PUSH_INTERVAL = 300           # time interval between successive data pushes in seconds
logging_enabled = True

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       Endpoint specific settings to which the data is pushed
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
DC_IP = '204.232.188.214' #'192.168.57.128'
DC_PORT = 8281                          
HOST = DC_IP + ':' + `DC_PORT`

DC_ENDPOINT = '/firealarm/1.0/controller' #'/firealarm/1.0/'
PUSH_ENDPOINT = DC_ENDPOINT + '/push_temperature'
REGISTER_ENDPOINT = DC_ENDPOINT + '/register'
### ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~



# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       Logger defaults
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
LOG_FILENAME = "/usr/local/src/RaspberryAgent/logs/RaspberryStats.log"
LOG_LEVEL = logging.INFO  # Could be e.g. "DEBUG" or "WARNING"
### ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~



# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       Define and parse command line arguments
#       If the log file is specified on the command line then override the default
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
parser = argparse.ArgumentParser(description="Python service to push RPi info to the Device Cloud")
parser.add_argument("-l", "--log", help="file to write log to (default '" + LOG_FILENAME + "')")

help_string = "time interval between successive data pushes (default '" + str(PUSH_INTERVAL) + "')"
parser.add_argument("-i", "--interval", type=int, help=help_string)

args = parser.parse_args()
if args.log:
        LOG_FILENAME = args.log

if args.interval:
        PUSH_INTERVAL = args.interval
### ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       A class we can use to capture stdout and sterr in the log
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
class IOTLogger(object):
        def __init__(self, logger, level):
                """Needs a logger and a logger level."""
                self.logger = logger
                self.level = level

        def write(self, message):
                if message.rstrip() != "":                                     # Only log if there is a message (not just a new line)
                        self.logger.log(self.level, message.rstrip())
### ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~



# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       Configure logging to log to a file, 
#               making a new file at midnight and keeping the last 3 day's data
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def configureLogger(loggerName):
        logger = logging.getLogger(loggerName)
        logger.setLevel(LOG_LEVEL)                                                                              # Set the log level to LOG_LEVEL
        handler = logging.handlers.TimedRotatingFileHandler(LOG_FILENAME, when="midnight", backupCount=3)       # Handler that writes to a file, 
                                                                                                                # ~~~make new file at midnight and keep 3 backups
        formatter = logging.Formatter('%(asctime)s %(levelname)-8s %(message)s')                                # Format each log message like this
        handler.setFormatter(formatter)                                                                         # Attach the formatter to the handler
        logger.addHandler(handler)                                                                              # Attach the handler to the logger

	if(logging_enabled):        
	        sys.stdout = IOTLogger(logger, logging.INFO)                                            # Replace stdout with logging to file at INFO level
	        sys.stderr = IOTLogger(logger, logging.ERROR)                                           # Replace stderr with logging to file at ERROR level
### ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       This method registers the DevieIP in the Device-Cloud
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def registerDeviceIP():
        dcConncection = httplib.HTTPConnection(DC_IP, DC_PORT)
        dcConncection.set_debuglevel(1)
        dcConncection.connect()

        registerURL = REGISTER_ENDPOINT + '/' + iotUtils.DEVICE_OWNER + '/' + iotUtils.DEVICE_ID + '/' + iotUtils.HOST_NAME
        
        dcConncection.putrequest('POST', registerURL)
        dcConncection.putheader('Authorization', 'Bearer ' + iotUtils.AUTH_TOKEN)
        dcConncection.endheaders()
        
        dcConncection.send('')    
	dcResponse = dcConncection.getresponse()

        print '~~~~~~~~~~~~~~~~~~~~~~~~ Device Registration ~~~~~~~~~~~~~~~~~~~~~~~~~'
        print registerURL
        print dcResponse.status, dcResponse.reason
        print dcResponse.msg

        dcConncection.close()
        print '~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~'
### ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~



# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       This method connects to the Device-Cloud and pushes data
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def connectAndPushData():
        dcConncection = httplib.HTTPConnection(DC_IP, DC_PORT)
        dcConncection.set_debuglevel(1)

        dcConncection.connect()

        request = dcConncection.putrequest('POST', PUSH_ENDPOINT)

        headers = {}
        headers['Authorization'] = 'Bearer ' + iotUtils.AUTH_TOKEN
        headers['Content-Type'] = 'application/json'

        ### ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        ###       Read the Temperature and Load info of RPi and construct payload
        ### ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        # rPiTemperature=getCPUTemp()                                                           # Can be used if required to push CPU Temperature
        # rPiLoad = getCPULoad()                                                                # Can be used if required to push CPU Load
        
        ### ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        
        rPiTemperature = iotUtils.LAST_TEMP                                                 # Push the last read temperature value
        PUSH_DATA = iotUtils.DEVICE_INFO + iotUtils.DEVICE_IP.format(ip=iotUtils.HOST_NAME) + iotUtils.DEVICE_DATA.format(temperature=rPiTemperature)
        PUSH_DATA += '}'

        print PUSH_DATA

        headers['Content-Length'] = len(PUSH_DATA)

        for k in headers:
            dcConncection.putheader(k, headers[k])
        dcConncection.endheaders()

        dcConncection.send(PUSH_DATA)                           # Push the data
        dcResponse = dcConncection.getresponse()
    
	print dcResponse.status, dcResponse.reason
        print dcResponse.msg

        dcConncection.close()
        print '~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~'
	
        if (dcResponse.status == 409 or dcResponse.status == 412):
            print 'Re-registering Device IP'
            registerDeviceIP()   

### ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       This is a Thread object for reading temperature continuously
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
class TemperatureReaderThread(object):
    def __init__(self, interval=3):
        self.interval = interval
        
        thread = threading.Thread(target=self.run, args=())
        thread.daemon = True                            # Daemonize thread
        thread.start()                                  # Start the execution
    
    def run(self):
        TEMP_PIN = 4
        TEMP_SENSOR_TYPE = 11

        # Try to grab a sensor reading.  Use the read_retry method which will retry up
        # to 15 times to get a sensor reading (waiting 2 seconds between each retry).
        while True:
            try:
                humidity, temperature = Adafruit_DHT.read_retry(TEMP_SENSOR_TYPE, TEMP_PIN)

                if temperature != iotUtils.LAST_TEMP:
                    iotUtils.LAST_TEMP = temperature
                    connectAndPushData()
                
                iotUtils.LAST_TEMP = temperature
                print 'Temp={0:0.1f}*C  Humidity={1:0.1f}%'.format(temperature, humidity)
        
            except Exception, e:
                print "Exception in TempReaderThread: Could not successfully read Temperature"
                print str(e)
                print '~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~'
                pass
                
                time.sleep(self.interval)
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~



# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       This is a Thread object for listening for MQTT Messages
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
class UtilsThread(object):
    def __init__(self):
        thread = threading.Thread(target=self.run, args=())
        thread.daemon = True                            # Daemonize thread
        thread.start()                                  # Start the execution
    
    def run(self):
        iotUtils.main()
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~




# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       This is a Thread object for HTTP-Server that listens for operations on RPi
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
class ListenHTTPServerThread(object):
    def __init__(self):
        thread = threading.Thread(target=self.run, args=())
        thread.daemon = True                            # Daemonize thread
        thread.start()                                  # Start the execution
 
    def run(self):
        httpServer.main()
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~



# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       This is a Thread object for Server that listens for XMPP Messages
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
class ListenXMPPServerThread(object):
    def __init__(self):
        thread = threading.Thread(target=self.run, args=())
        thread.daemon = True                            # Daemonize thread
        thread.start()                                  # Start the execution

    def run(self):
        xmppServer.main()
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~



# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       This is a Thread object for listening for MQTT Messages
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
class ListenMQTTThread(object):
    def __init__(self):
        thread = threading.Thread(target=self.run, args=())
        thread.daemon = True                            # Daemonize thread
        thread.start()                                  # Start the execution
    
    def run(self):
        mqttListener.main()
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~



# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       The Main method of the RPi Agent 
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def main():
    configureLogger("WSO2IOT_RPiStats")
#    iotUtils.setUpGPIOPins()

    UtilsThread()
    registerDeviceIP()                                      # Call the register endpoint and register Device IP
    TemperatureReaderThread()                               # initiates and runs the thread to continuously read temperature from DHT Sensor
    ListenHTTPServerThread()                                    # starts an HTTP Server that listens for operational commands to switch ON/OFF Led
    ListenXMPPServerThread()
    ListenMQTTThread()
    
    while True:
        try:
            if iotUtils.LAST_TEMP > 0:                 # Push data only if there had been a successful temperature read
                connectAndPushData()                   # Push Sensor (Temperature) data to WSO2 BAM
		time.sleep(PUSH_INTERVAL)
        except (KeyboardInterrupt, Exception) as e:
            print "Exception in RaspberryAgentThread (either KeyboardInterrupt or Other):"
            print str(e)
            print '~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~'
            pass
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                

if __name__ == "__main__":
        main()
