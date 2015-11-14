package org.wso2.carbon.device.mgt.iot.sample.firealarm.service.impl.transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jivesoftware.smack.packet.Message;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.common.controlqueue.xmpp.XmppConfig;
import org.wso2.carbon.device.mgt.iot.common.controlqueue.xmpp.XmppConnector;
import org.wso2.carbon.device.mgt.iot.common.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.sample.firealarm.plugin.constants
		.FireAlarmConstants;
import org.wso2.carbon.device.mgt.iot.sample.firealarm.service.impl.FireAlarmService;

import java.util.Calendar;

public class FireAlarmXMPPConnector extends XmppConnector {
	private static Log log = LogFactory.getLog(FireAlarmXMPPConnector.class);

	private static String xmppServerIP;
	//	private static int xmppServerPort;
	private static String xmppAdminUsername;
	private static String xmppAdminPassword;
	private static String xmppAdminAccountJID;

	private FireAlarmXMPPConnector() {
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
		String subject = xmppMessage.getSubject();
		String message = xmppMessage.getBody();

		int indexOfAt = from.indexOf("@");
		int indexOfSlash = from.indexOf("/");

		String deviceId = from.substring(0, indexOfAt);
		String owner = from.substring(indexOfSlash + 1, from.length());

		log.info("Received XMPP message for: {OWNER-" + owner + "} & {DEVICE.ID-" + deviceId + "}");

		if (subject.equals("PUBLISHER")) {
			log.info("XMPP: Publisher Message [" + message + "] from [" + from + "]");

			float temperature = Float.parseFloat(message.split(":")[1]);
			if(!FireAlarmService.publishToDAS(owner, deviceId, temperature)) {
				log.error("XMPP Connector: Publishing data to DAS failed.");
			}

			if(log.isDebugEnabled()) {
				log.debug("XMPP Connector: Published data to DAS successfully.");
			}
		} else if(subject.equals("CONTROL-REPLY")) {
			log.info("XMPP: Reply Message [" + message + "] from [" + from + "]");
			String temperature = message.split(":")[1];
			SensorDataManager.getInstance().setSensorRecord(deviceId,FireAlarmConstants.SENSOR_TEMPERATURE, temperature, Calendar.getInstance().getTimeInMillis());
		} else {
			log.info("SOME XMPP Message [" + message + "] from " + from + "]");
		}

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
							FireAlarmXMPPConnector.super.connectAndLogin(xmppAdminUsername,
							                                                    xmppAdminPassword,
							                                                    null);
							FireAlarmXMPPConnector.super.setMessageFilterOnReceiver(
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
