#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
"""
import logging
import sys
from optparse import OptionParser
import socket
from urllib import urlopen

import sleekxmpp
from sleekxmpp.exceptions import IqError, IqTimeout

# Python versions before 3.0 do not use UTF-8 encoding
# by default. To ensure that Unicode is handled properly
# throughout SleekXMPP, we will set the default encoding
# ourselves to UTF-8.
if sys.version_info < (3, 0):
    from sleekxmpp.util.misc_ops import setdefaultencoding

    setdefaultencoding('utf8')
else:
    raw_input = input

# from sleekxmpp.plugins.xep_0323.device import Device
PRINT_HEADER_LENGTH = 40


class IoT_TestDevice(sleekxmpp.ClientXMPP):
    """
    A simple IoT device that can act as client

    This script can act as a "client" an IoT device or other party that would like to get data from
    another device.

    Setup the command line arguments.

    python xmpp_client.py -j "bob@yourdomain.com" -p "password" -c "alice@yourdomain.com/device1" {--[debug|quiet]}
    python xmpp_client.py -j "bob@127.0.0.1" -p "password" -c "alice@127.0.0.1/device1" {--[debug|quiet]}
    """

    def __init__(self, jid, password, sensorjid, sensorType):
        sleekxmpp.ClientXMPP.__init__(self, jid, password)
        self.add_event_handler("session_start", self.session_start)
        self.add_event_handler("message", self.message)
        self.device = None
        self.releaseMe = False
        self.target_jid = sensorjid
        self.requestType = sensorType
        # self.beServer=True
        # self.clientJID=None

    def datacallback(self, from_jid, result, nodeId=None, timestamp=None, fields=None,
                     error_msg=None):
        """
        This method will be called when you ask another IoT device for data with the xep_0323
        se script below for the registration of the callback
        """
        logging.debug("we got data %s from %s", str(result), from_jid)
        if (result == "fields"):
            header = 'XEP 302 Sensor Data'
            logging.info('-' * PRINT_HEADER_LENGTH)

            gap = ' ' * ((PRINT_HEADER_LENGTH - len(header)) / 2)

            logging.info(gap + header)
            logging.info('-' * PRINT_HEADER_LENGTH)

            logging.debug("RECV:" + str(fields))

            if len(fields) > 0:
                logging.info("Name\t\tType\tValue\tUnit")
            for field in fields:
                logging.info("  - " + field["name"] + "\t" + field["typename"] + "\t" + field[
                    "value"] + "\t" + field["unit"])

                if self.requestType in ("/" + field["name"].upper() + "/."):
                    print field["value"]

            logging.info('-' * PRINT_HEADER_LENGTH)
            self.disconnect()

    def testForRelease(self):
        # todo thread safe
        return self.releaseMe

    def doReleaseMe(self):
        # todo thread safe
        self.releaseMe = True

    def addDevice(self, device):
        self.device = device

    def session_start(self, event):
        self.send_presence()
        self.get_roster()
        # tell your preffered friend that you are alive using generic xmpp chat protocol
        # self.send_message(mto='jocke@jabber.sust.se', mbody=self.boundjid.bare +' is now online use xep_323 stanza to talk to me')

        # -------------------------------------------------------------------------------------------
        #   Service Discovery
        # -------------------------------------------------------------------------------------------
        try:
            # By using block=True, the result stanza will be
            # returned. Execution will block until the reply is
            # received. Non-blocking options would be to listen
            # for the disco_info event, or passing a handler
            # function using the callback parameter.
            info = self['xep_0030'].get_info(jid=self.target_jid,
                                             node=None,
                                             block=True)
        except IqError as e:
            logging.error("Entity returned an error: %s" % e.iq['error']['condition'])
        except IqTimeout:
            logging.error("No response received.")
        else:
            header = 'XMPP Service Discovery'
            logging.info('-' * PRINT_HEADER_LENGTH)
            gap = ' ' * ((PRINT_HEADER_LENGTH - len(header)) / 2)
            logging.info(gap + header)
            logging.info('-' * PRINT_HEADER_LENGTH)

            logging.info("Device: %s" % self.target_jid)

            for feature in info['disco_info']['features']:
                logging.info('  - %s' % feature)

        # -------------------------------------------------------------------------------------------
        #   Requesting data through XEP0323
        # -------------------------------------------------------------------------------------------

        logging.info('-' * PRINT_HEADER_LENGTH)
        logging.info("Sending Request: %s to %s", self.requestType, self.target_jid)

        if self.requestType in ('/TEMPERATURE/.', '/SONAR/.'):
            session = self['xep_0323'].request_data(self.boundjid.full, self.target_jid,
                                                    self.datacallback, flags={"momentary": "true"})
        else:
            self.send_message(mto=self.target_jid,
                              mbody=self.requestType,
                              mtype='chat')

            # Using wait=True ensures that the send queue will be
            # emptied before ending the session.
            self.disconnect(wait=True)
            print ("Bulb state switched - " + self.requestType)

    def message(self, msg):
        if msg['type'] in ('chat', 'normal'):
            logging.info("got normal chat message" + str(msg))
            ipPublic = urlopen('http://icanhazip.com').read()
            ipSocket = socket.gethostbyname(socket.gethostname())
            msg.reply(
                "Hi I am " + self.boundjid.full + " and I am on IP " + ipSocket + " use xep_323 stanza to talk to me").send()
        else:
            logging.info("got unknown message type %s", str(msg['type']))

if __name__ == '__main__':

    # Setup the command line arguments.
    #
    # This script can act both as
    #   "server" an IoT device that can provide sensorinformation
    #   python IoT_TestDevice.py -j "serverjid@yourdomain.com" -p "password" -n "TestIoT" --debug
    #
    #   "client" an IoT device or other party that would like to get data from another device

    optp = OptionParser()

    # Output verbosity options.
    optp.add_option('-q', '--quiet', help='set logging to ERROR',
                    action='store_const', dest='loglevel',
                    const=logging.ERROR, default=logging.INFO)
    optp.add_option('-d', '--debug', help='set logging to DEBUG',
                    action='store_const', dest='loglevel',
                    const=logging.DEBUG, default=logging.INFO)
    optp.add_option('-v', '--verbose', help='set logging to COMM',
                    action='store_const', dest='loglevel',
                    const=5, default=logging.INFO)
    optp.add_option('-t', '--pingto', help='set jid to ping',
                    action='store', type='string', dest='pingjid',
                    default=None)

    # JID and password options.
    optp.add_option("-j", "--jid", dest="jid",
                    help="JID to use")
    optp.add_option("-p", "--password", dest="password",
                    help="password to use")

    # IoT test
    optp.add_option("-c", "--sensorjid", dest="sensorjid",
                    help="Another device to call for data on", default=None)

    optp.add_option("-r", "--sensorType", dest="sensorType",
                    help="The type of the sensor info requested", default="/TEMPERATURE/")

    optp.add_option("-s", "--sensorState", dest="sensorState",
                    help="The state of the sensor to switch to", default="")

    opts, args = optp.parse_args()

    # Setup logging.
    logging.basicConfig(level=opts.loglevel,
                        format='%(levelname)-8s %(message)s')

    if opts.jid is None:
        opts.jid = "admin@204.232.188.215/admin"
    if opts.password is None:
        opts.password = "admin"
    # if opts.sensorjid is None:
    #     opts.sensorjid = "t4ibkqs8t7ox@204.232.188.215/admin"

    # -------------------------------------------------------------------------------------------
    #   Starting XMPP with XEP0030, XEP0323, XEP0325
    # -------------------------------------------------------------------------------------------

    requestState = opts.sensorType + opts.sensorState

    xmpp = IoT_TestDevice(opts.jid, opts.password, opts.sensorjid, requestState)
    xmpp.register_plugin('xep_0030')
    xmpp.register_plugin('xep_0323')
    xmpp.register_plugin('xep_0325')

    if opts.sensorjid:
        logging.debug("Will try to call another device for data")
        # xmpp.beClientOrServer(server=False,clientJID=opts.sensorjid)
        xmpp.connect()
        xmpp.process(block=True)
        logging.debug("ready ending")

    else:
        print "ID of the client to communicate-to not given..."
