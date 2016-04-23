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
import xmpp
import util

XMPP_ENDPOINT = util.XMPP_EP.split(":")
XMPP_IP = XMPP_ENDPOINT[1].replace('//', '')
XMPP_PORT = int(XMPP_ENDPOINT[2])
MESSAGE_TO = util.DEVICE_OWNER
XMPP_PWD = util.DEVICE_PASSWORD
XMPP_OWN = util.DEVICE_ID

XMPP_RESOURCE = "drone_current_status"
XMPP_JID = MESSAGE_TO + "@" + XMPP_IP + "/" + XMPP_RESOURCE


global droneClient
droneClient = xmpp.Client(XMPP_IP, debug=[])

def loginToXMPPServer():
    auth = droneClient.auth(XMPP_OWN, XMPP_PWD, resource=XMPP_RESOURCE)
    if not auth:
        print 'could not authenticate!'
        return 0
    print 'authenticated using', auth
    droneClient.sendInitPresence()
    return 1


def connectToXMPPServer():
    connection = droneClient.connect(server=(XMPP_IP, XMPP_PORT))
    if not connection:
        print 'could not connect to xmpp server at ' + str(XMPP_IP)
        return 0

    print 'connected with', connection
    response = loginToXMPPServer()
    if response:
        return 1
    else:
        return 0


def sendMessage(message):
    xmpp_message = xmpp.Message(XMPP_JID, message)
    xmpp_message.setAttr('type', 'chat')
    droneClient.send(xmpp_message)
    print message

