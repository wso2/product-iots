package org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.service.impl.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.common.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.common.controlqueue.mqtt.MqttSubscriber;
import org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.plugin.constants
		.VirtualFireAlarmConstants;

import java.io.File;
import java.util.UUID;

public class VirtualFireAlarmMqttSubscriber extends MqttSubscriber {
	private static Log log = LogFactory.getLog(VirtualFireAlarmMqttSubscriber.class);

	private static final String subscribeTopic =
			"wso2" + File.separator + "iot" + File.separator + "+" + File.separator +
					VirtualFireAlarmConstants.DEVICE_TYPE + File.separator + "+" + File.separator +
					"reply";
	private static final String iotServerSubscriber = UUID.randomUUID().toString().substring(0, 5);
	private static String mqttEndpoint;

	private VirtualFireAlarmMqttSubscriber() {
		super(iotServerSubscriber, VirtualFireAlarmConstants.DEVICE_TYPE,
		      MqttConfig.getInstance().getMqttQueueEndpoint(), subscribeTopic);
	}

	public void initConnector() {
		mqttEndpoint = MqttConfig.getInstance().getMqttQueueEndpoint();
	}

	public void connectAndSubscribe() {
		try {
			super.connectAndSubscribe();
		} catch (DeviceManagementException e) {
			log.error("Subscription to MQTT Broker at: " + mqttEndpoint + " failed");
			retryMQTTSubscription();
		}
	}

	@Override
	protected void postMessageArrived(String topic, MqttMessage message) {
		log.info("Message " + message.toString() + " was received for topic: " + topic);
	}

	private void retryMQTTSubscription() {
		Thread retryToSubscribe = new Thread() {
			@Override
			public void run() {
				while (true) {
					if (!isConnected()) {
						if (log.isDebugEnabled()) {
							log.debug("Subscriber re-trying to reach MQTT queue....");
						}

						try {
							VirtualFireAlarmMqttSubscriber.super.connectAndSubscribe();
						} catch (DeviceManagementException e1) {
							if (log.isDebugEnabled()) {
								log.debug("Attempt to re-connect to MQTT-Queue failed");
							}
						}
					} else {
						break;
					}

					try {
						Thread.sleep(5000);
					} catch (InterruptedException e1) {
						log.error("MQTT: Thread S;eep Interrupt Exception");
					}
				}
			}
		};

		retryToSubscribe.setDaemon(true);
		retryToSubscribe.start();
	}
}
