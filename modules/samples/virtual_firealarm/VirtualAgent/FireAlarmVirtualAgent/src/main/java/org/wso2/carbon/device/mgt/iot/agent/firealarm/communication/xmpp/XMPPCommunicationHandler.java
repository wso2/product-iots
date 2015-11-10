/*
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.iot.agent.firealarm.communication.xmpp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.FromContainsFilter;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.filter.ToContainsFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.communication.CommunicationHandler;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.communication.CommunicationHandlerException;

/**
 * This class contains the IoT-Server specific implementation for all the XMPP functionality.
 * This includes connecting to a XMPP Server & Login-In using the device's/server's XMPP-Account,
 * Setting listeners and filters on incoming XMPP messages and Sending XMPP replies for messages
 * received. Makes use of the 'Smack-XMPP' library provided by jivesoftware/igniterealtime.
 * <p/>
 * It is an abstract class that implements the common interface "CommunicationHandler". Whilst
 * providing some methods which handle key XMPP relevant tasks, this class implements only the
 * most generic methods of the "CommunicationHandler" interface. The rest of the methods are left
 * for any extended concrete-class to implement as per its need.
 */
public abstract class XMPPCommunicationHandler implements CommunicationHandler<Message> {
	private static final Log log = LogFactory.getLog(XMPPCommunicationHandler.class);

	protected String server;
	protected int timeoutInterval;    // millis

	private static final int DEFAULT_XMPP_PORT = 5222;
	private XMPPConnection connection;
	private int port;
	private ConnectionConfiguration config;
	private PacketFilter filter;
	private PacketListener listener;


	/**
	 * Constructor for XMPPCommunicationHandler passing only the server-IP.
	 *
	 * @param server the IP of the XMPP server.
	 */
	protected XMPPCommunicationHandler(String server) {
		this.server = server;
		this.port = DEFAULT_XMPP_PORT;
		this.timeoutInterval = DEFAULT_TIMEOUT_INTERVAL;
		initXMPPClient();
	}

	/**
	 * Constructor for XMPPCommunicationHandler passing server-IP and the XMPP-port.
	 *
	 * @param server the IP of the XMPP server.
	 * @param port   the XMPP server's port to connect to. (default - 5222)
	 */
	protected XMPPCommunicationHandler(String server, int port) {
		this.server = server;
		this.port = port;
		this.timeoutInterval = DEFAULT_TIMEOUT_INTERVAL;
		initXMPPClient();
	}

	/**
	 * Constructor for XMPPCommunicationHandler passing server-IP, the XMPP-port and the
	 * timeoutInterval used by listeners to the server and for reconnection schedules.
	 *
	 * @param server          the IP of the XMPP server.
	 * @param port            the XMPP server's port to connect to. (default - 5222)
	 * @param timeoutInterval the timeout interval to use for the connection and reconnection
	 */
	protected XMPPCommunicationHandler(String server, int port, int timeoutInterval) {
		this.server = server;
		this.port = port;
		this.timeoutInterval = timeoutInterval;
		initXMPPClient();
	}

	/**
	 * Sets the client's time-out-limit whilst waiting for XMPP-replies from server.
	 *
	 * @param millis the time in millis to be set as the time-out-limit whilst waiting for a
	 *               XMPP-reply.
	 */
	public void setTimeoutInterval(int millis) {
		this.timeoutInterval = millis;
	}

	/**
	 * Checks whether the connection to the XMPP-Server persists.
	 *
	 * @return true if the client is connected to the XMPP-Server, else false.
	 */
	@Override
	public boolean isConnected() {
		return connection.isConnected();
	}

	/**
	 * Initializes the XMPP Client. Sets the time-out-limit whilst waiting for XMPP-replies from
	 * server. Sets the XMPP configurations to connect to the server and creates the
	 * XMPPConnection object used for connecting and Logging-In.
	 */
	private void initXMPPClient() {
		log.info(String.format("Initializing connection to XMPP Server at %1$s via port " +
				                       "%2$d......",
		                       server, port));
		SmackConfiguration.setPacketReplyTimeout(timeoutInterval);
		config = new ConnectionConfiguration(server, port);
//		TODO:: Need to enable SASL-Authentication appropriately
		config.setSASLAuthenticationEnabled(false);
		config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
		connection = new XMPPConnection(config);
	}


	/**
	 * Connects to the XMPP-Server and if attempt unsuccessful, then throws exception.
	 *
	 * @throws CommunicationHandlerException in the event of 'Connecting to' the XMPP server fails.
	 */
	protected void connectToServer() throws CommunicationHandlerException {
		try {
			connection.connect();
			log.info(String.format(
					"Connection to XMPP Server at %1$s established successfully......", server));

		} catch (XMPPException xmppExcepion) {
			String errorMsg =
					"Connection attempt to the XMPP Server at " + server + " via port " + port +
							" failed.";
			log.info(errorMsg);
			throw new CommunicationHandlerException(errorMsg, xmppExcepion);
		}
	}

	/**
	 * If successfully established connection, then tries to Log in using the device's XMPP
	 * Account credentials.
	 *
	 * @param username the username of the device's XMPP-Account.
	 * @param password the password of the device's XMPP-Account.
	 * @param resource the resource the resource, specific to the XMPP-Account to which the login
	 *                 is made to
	 * @throws CommunicationHandlerException in the event of 'Logging into' the XMPP server fails.
	 */
	protected void loginToServer(String username, String password, String resource)
			throws CommunicationHandlerException {
		if (isConnected()) {
			try {
				if (resource == null) {
					connection.login(username, password);
					log.info(String.format("Logged into XMPP Server at %1$s as user %2$s......",
					                       server, username));
				} else {
					connection.login(username, password, resource);
					log.info(String.format(
							"Logged into XMPP Server at %1$s as user %2$s on resource %3$s......",
							server, username, resource));
				}
			} catch (XMPPException xmppException) {
				String errorMsg =
						"Login attempt to the XMPP Server at " + server + " with username - " +
								username + " failed.";
				log.info(errorMsg);
				throw new CommunicationHandlerException(errorMsg, xmppException);
			}
		} else {
			String errorMsg =
					"Not connected to XMPP-Server to attempt Login. Please 'connectToServer' " +
							"before Login";
			if (log.isDebugEnabled()) {
				log.debug(errorMsg);
			}
			throw new CommunicationHandlerException(errorMsg);
		}
	}


	/**
	 * Sets a filter for all the incoming XMPP-Messages on the Sender's JID (XMPP-Account ID).
	 * Also creates a listener for the incoming messages and connects the listener to the
	 * XMPPConnection alongside the set filter.
	 *
	 * @param senderJID the JID (XMPP-Account ID of the sender) to which the filter is to be set.
	 */
	protected void setFilterOnSender(String senderJID) {
		filter = new AndFilter(new PacketTypeFilter(Message.class), new FromContainsFilter(
				senderJID));
		listener = new PacketListener() {
			@Override
			public void processPacket(Packet packet) {
				if (packet instanceof Message) {
					final Message xmppMessage = (Message) packet;
					Thread msgProcessThread = new Thread() {
						public void run() {
							processIncomingMessage(xmppMessage);
						}
					};
					msgProcessThread.setDaemon(true);
					msgProcessThread.start();
				}
			}
		};

		connection.addPacketListener(listener, filter);
	}


	/**
	 * Sets a filter for all the incoming XMPP-Messages on the Receiver's JID (XMPP-Account ID).
	 * Also creates a listener for the incoming messages and connects the listener to the
	 * XMPPConnection alongside the set filter.
	 *
	 * @param receiverJID the JID (XMPP-Account ID of the receiver) to which the filter is to be
	 *                    set.
	 */
	protected void setFilterOnReceiver(String receiverJID) {
		filter = new AndFilter(new PacketTypeFilter(Message.class), new ToContainsFilter(
				receiverJID));
		listener = new PacketListener() {
			@Override
			public void processPacket(Packet packet) {
				if (packet instanceof Message) {
					final Message xmppMessage = (Message) packet;
					Thread msgProcessThread = new Thread() {
						public void run() {
							processIncomingMessage(xmppMessage);
						}
					};
					msgProcessThread.setDaemon(true);
					msgProcessThread.start();
				}
			}
		};

		connection.addPacketListener(listener, filter);
	}


	/**
	 * Sets a filter for all the incoming XMPP-Messages on the From-JID & To-JID (XMPP-Account IDs)
	 * passed in. Also creates a listener for the incoming messages and connects the listener to
	 * the XMPPConnection alongside the set filter.
	 *
	 * @param senderJID    the From-JID (XMPP-Account ID) to which the filter is to be set.
	 * @param receiverJID  the To-JID (XMPP-Account ID) to which the filter is to be set.
	 * @param andCondition if true: then filter is set with 'AND' operator (senderJID &&
	 *                     receiverJID),
	 *                     if false: then the filter is set with 'OR' operator (senderJID |
	 *                     receiverJID)
	 */
	protected void setMessageFilterAndListener(String senderJID, String receiverJID, boolean
			andCondition) {
		PacketFilter jidFilter;

		if (andCondition) {
			jidFilter = new AndFilter(new FromContainsFilter(senderJID), new ToContainsFilter(
					receiverJID));
		} else {
			jidFilter = new OrFilter(new FromContainsFilter(senderJID), new ToContainsFilter(
					receiverJID));
		}

		filter = new AndFilter(new PacketTypeFilter(Message.class), jidFilter);
		listener = new PacketListener() {
			@Override
			public void processPacket(Packet packet) {
				if (packet instanceof Message) {
					final Message xmppMessage = (Message) packet;
					Thread msgProcessThread = new Thread() {
						public void run() {
							processIncomingMessage(xmppMessage);
						}
					};
					msgProcessThread.setDaemon(true);
					msgProcessThread.start();
				}
			}
		};

		connection.addPacketListener(listener, filter);
	}


	/**
	 * Sends an XMPP message. Calls the overloaded method with Subject set to "Reply-From-Device"
	 *
	 * @param JID     the JID (XMPP Account ID) to which the message is to be sent to.
	 * @param message the XMPP-Message that is to be sent.
	 */
	protected void sendXMPPMessage(String JID, String message) {
		sendXMPPMessage(JID, message, "XMPP-Message");
	}


	/**
	 * Overloaded method to send an XMPP message. Includes the subject to be mentioned in the
	 * message that is sent.
	 *
	 * @param JID     the JID (XMPP Account ID) to which the message is to be sent to.
	 * @param message the XMPP-Message that is to be sent.
	 * @param subject the subject that the XMPP-Message would carry.
	 */
	protected void sendXMPPMessage(String JID, String message, String subject) {
		Message xmppMessage = new Message();
		xmppMessage.setTo(JID);
		xmppMessage.setSubject(subject);
		xmppMessage.setBody(message);
		xmppMessage.setType(Message.Type.chat);
		sendXMPPMessage(JID, xmppMessage);
	}


	/**
	 * Sends an XMPP message.
	 *
	 * @param JID         the JID (XMPP Account ID) to which the message is to be sent to.
	 * @param xmppMessage the XMPP-Message that is to be sent.
	 */
	protected void sendXMPPMessage(String JID, Message xmppMessage) {
		connection.sendPacket(xmppMessage);
		if (log.isDebugEnabled()) {
			log.debug("Message: '" + xmppMessage.getBody() + "' sent to XMPP JID [" + JID +
					          "] sent successfully");
		}
	}


	/**
	 * Disables default debugger provided by the XMPPConnection.
	 */
	protected void disableDebugger() {
		connection.DEBUG_ENABLED = false;
	}


	/**
	 * Closes the connection to the XMPP Server.
	 */
	public void closeConnection() {
		if (connection != null && isConnected()) {
			connection.disconnect();
		}
	}

}
