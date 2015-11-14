package org.wso2.carbon.device.mgt.iot.sample.firealarm.service.impl.transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.common.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.common.controlqueue.mqtt.MqttSubscriber;
import org.wso2.carbon.device.mgt.iot.common.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.sample.firealarm.plugin.constants.FireAlarmConstants;
import org.wso2.carbon.device.mgt.iot.sample.firealarm.service.impl.FireAlarmService;

import java.io.File;
import java.util.Calendar;
import java.util.UUID;

public class FireAlarmMQTTSubscriber extends MqttSubscriber {
	private static Log log = LogFactory.getLog(FireAlarmMQTTSubscriber.class);

	private static final String subscribeTopic =
			"wso2" + File.separator + "iot" + File.separator + "+" + File.separator +
					FireAlarmConstants.DEVICE_TYPE + File.separator + "+" + File.separator +
					"publisher";

	private static final String iotServerSubscriber = UUID.randomUUID().toString().substring(0, 5);
	private static String mqttEndpoint;

	private FireAlarmMQTTSubscriber() {
		super(iotServerSubscriber, FireAlarmConstants.DEVICE_TYPE,
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
		String ownerAndId = topic.replace("wso2" + File.separator + "iot" + File.separator, "");
		ownerAndId = ownerAndId.replace(File.separator + FireAlarmConstants.DEVICE_TYPE + File.separator, ":");
		ownerAndId = ownerAndId.replace(File.separator + "publisher", "");

		String owner = ownerAndId.split(":")[0];
		String deviceId = ownerAndId.split(":")[1];

		log.info("Received MQTT message for: {OWNER-" + owner + "} & {DEVICE.ID-" + deviceId + "}");

		if (message.toString().contains("PUBLISHER")) {
			log.info("MQTT: Publisher Message [" + message.toString() + "] topic: [" + topic + "]");

			float temperature = Float.parseFloat(message.toString().split(":")[2]);
			if(!FireAlarmService.publishToDAS(owner, deviceId, temperature)) {
				log.error("MQTT Subscriber: Publishing data to DAS failed.");
			}

			if(log.isDebugEnabled()) {
				log.debug("MQTT Subscriber: Published data to DAS successfully.");
			}
        } else if (message.toString().contains("TEMPERATURE")) {
            log.info("MQTT: Reply Message [" + message.toString() + "] topic: [" + topic + "]");
            String temperatureValue = message.toString().split(":")[1];
            SensorDataManager.getInstance().setSensorRecord(deviceId, FireAlarmConstants.SENSOR_TEMPERATURE, temperatureValue, Calendar.getInstance().getTimeInMillis());
        } else {
            log.info("MQTT: Message [" + message.toString() + "] topic: [" + topic + "]");
        }
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
							FireAlarmMQTTSubscriber.super.connectAndSubscribe();
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
