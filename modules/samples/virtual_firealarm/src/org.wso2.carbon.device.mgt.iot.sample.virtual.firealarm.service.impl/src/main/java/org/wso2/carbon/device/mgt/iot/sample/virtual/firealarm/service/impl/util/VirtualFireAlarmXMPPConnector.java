package org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.service.impl.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.packet.Message;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.common.controlqueue.xmpp.XmppConfig;
import org.wso2.carbon.device.mgt.iot.common.controlqueue.xmpp.XmppConnector;

public class VirtualFireAlarmXmppConnector extends XmppConnector {
	private static Log log = LogFactory.getLog(VirtualFireAlarmXmppConnector.class);

	private static String xmppServerIP;
	//	private static int xmppServerPort;
	private static String xmppAdminUsername;
	private static String xmppAdminPassword;
	private static String xmppAdminAccountJID;

	private VirtualFireAlarmXmppConnector() {
		super(XmppConfig.getInstance().getXmppServerIP(),
		      XmppConfig.getInstance().getSERVER_CONNECTION_PORT());
	}

	public void initConnector() {
		xmppServerIP = XmppConfig.getInstance().getXmppServerIP();
		xmppAdminUsername = XmppConfig.getInstance().getXmppUsername();
		xmppAdminPassword = XmppConfig.getInstance().getXmppPassword();
		xmppAdminAccountJID = xmppAdminUsername + "@" + xmppServerIP;
	}

	public void connectAndLogin() {
		try {
			super.connectAndLogin(xmppAdminUsername, xmppAdminPassword, null);
			super.setMessageFilterOnReceiver(xmppAdminAccountJID);
		} catch (DeviceManagementException e) {
			log.error("Connect/Login attempt to XMPP Server at: " + xmppServerIP + " failed");
			retryXMPPConnection();
		}
	}

	@Override
	protected void processXMPPMessage(Message xmppMessage) {
		String from = xmppMessage.getFrom();
		String message = xmppMessage.getBody();
		log.info("Received XMPP message '" + message + "' from " + from);
	}

	private void retryXMPPConnection() {
		Thread retryToConnect = new Thread() {
			@Override
			public void run() {

				while (true) {
					if (!isConnected()) {
						if (log.isDebugEnabled()) {
							log.debug("Re-trying to reach XMPP Server....");
						}

						try {
							VirtualFireAlarmXmppConnector.super.connectAndLogin(xmppAdminUsername,
							                                                    xmppAdminPassword,
							                                                    null);
							VirtualFireAlarmXmppConnector.super.setMessageFilterOnReceiver(
									xmppAdminAccountJID);
						} catch (DeviceManagementException e1) {
							if (log.isDebugEnabled()) {
								log.debug("Attempt to re-connect to XMPP-Server failed");
							}
						}
					} else {
						break;
					}

					try {
						Thread.sleep(5000);
					} catch (InterruptedException e1) {
						log.error("XMPP: Thread Sleep Interrupt Exception");
					}
				}
			}
		};

		retryToConnect.setDaemon(true);
		retryToConnect.start();
	}


}
