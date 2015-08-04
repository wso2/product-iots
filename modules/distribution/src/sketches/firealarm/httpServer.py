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
import BaseHTTPServer
import iotUtils


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       HOST and PORT info of the HTTP Server that gets started
#			HOST_NAME is initialised in the main() method
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#global HOST_NAME
#HOST_NAME = "0.0.0.0"

SERVER_PORT = 80 # Maybe set this to 9000.
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       Class that handles HTTP GET requests for operations on the RPi
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
class MyHandler(BaseHTTPServer.BaseHTTPRequestHandler):
	def do_GET(request):
		# """Respond to a GET request."""

		if not processURLPath(request.path):
			return			

		resource = request.path.split("/")[1].upper()
		state = request.path.split("/")[2].upper()
		print "Resource: " + resource 

		if resource == "TEMP":
			request.send_response(200)
			request.send_header("Content-type", "text/plain")
			request.end_headers()
			request.wfile.write(iotUtils.LAST_TEMP)

		elif resource == "BULB":
	                iotUtils.switchBulb(state)
			print "Requested Switch State: " + state

		elif resource == "SONAR":
			request.send_response(200)
			request.send_header("Content-type", "text/plain")
			request.end_headers()
			request.wfile.write(iotUtils.LAST_DISTANCE)

		print '~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~'
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~



# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       Check the URL string of the request and validate
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def processURLPath(path):
	if path.count("/") != 2 and not "favicon" in path:
		print "Invalid URL String: " + path
		return False
	
	resource = path.split("/")[1]

	if not iequal("BULB", resource) and not iequal("TEMP", resource) and not iequal("FAN", resource) and not iequal("SONAR", resource):
		if not "favicon" in resource:
			print "Invalid resource: " + resource + " to execute operation"
		return False

	return True
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       Case-Insensitive check on whether two string are similar
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def iequal(a, b):
	try:
		return a.upper() == b.upper()
	except AttributeError:
		return a == b
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#       The Main method of the server script
#			This method is invoked from RaspberryStats.py on a new thread
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
def main():
	HOST_NAME = iotUtils.getDeviceIP()
        server_class = BaseHTTPServer.HTTPServer

	while True:
	    	try:
    			httpd = server_class((HOST_NAME, SERVER_PORT), MyHandler)
    			print time.asctime(), "Server Starts - %s:%s" % (HOST_NAME, SERVER_PORT)
                
       	 		httpd.serve_forever()
    		except (KeyboardInterrupt, Exception) as e:
    			print "Exception in ServerThread (either KeyboardInterrupt or Other):"
    			print str(e)
                
#            		GPIO.output(BULB_PIN, False)
                	iotUtils.switchBulb("OFF")
    			httpd.server_close()
    			print time.asctime(), "Server Stops - %s:%s" % (HOST_NAME, SERVER_PORT)
    			print '~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~'
    			pass

# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~



if __name__ == '__main__':
	main()
	
