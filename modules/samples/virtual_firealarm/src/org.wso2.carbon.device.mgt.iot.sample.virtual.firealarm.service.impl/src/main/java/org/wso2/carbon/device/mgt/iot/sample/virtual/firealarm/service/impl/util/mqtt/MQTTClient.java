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

package org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.service.impl.util.mqtt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class contains the Agent specific implementation for all the MQTT functionality. This
 * includes connecting to a MQTT Broker & subscribing to the appropriate MQTT-topic, action plan
 * upon losing connection or successfully delivering a message to the broker and processing
 * incoming messages. Makes use of the 'Paho-MQTT' library provided by Eclipse Org.
 * <p/>
 * It is an abstract class with an abstract method 'postMessageArrived' allowing the user to have
 * their own implementation of the actions to be taken upon receiving a message to the subscribed
 * MQTT-Topic.
 */
public abstract class MQTTClient implements MqttCallback {
	private static final Log log = LogFactory.getLog(MQTTClient.class);

	private MqttClient client;
	private String clientId;
	private MqttConnectOptions options;
	private String subscribeTopic;
	private String clientWillTopic;
	private String mqttBrokerEndPoint;
	private int reConnectionInterval;

	/**
	 * Constructor for the MQTTClient which takes in the owner, type of the device and the MQTT
	 * Broker URL and the topic to subscribe.
	 *
	 * @param deviceOwner        the owner of the device.
	 * @param deviceType         the CDMF Device-Type of the device.
	 * @param mqttBrokerEndPoint the IP/URL of the MQTT broker endpoint.
	 * @param subscribeTopic     the MQTT topic to which the client is to be subscribed
	 */
	protected MQTTClient(String deviceOwner, String deviceType, String mqttBrokerEndPoint,
	                     String subscribeTopic) {
		this.clientId = deviceOwner + ":" + deviceType;
		this.subscribeTopic = subscribeTopic;
		this.clientWillTopic = deviceType + File.separator + "disconnection";
		this.mqttBrokerEndPoint = mqttBrokerEndPoint;
		this.reConnectionInterval = 5000;
		this.initSubscriber();
	}

	/**
	 * Constructor for the MQTTClient which takes in the owner, type of the device and the MQTT
	 * Broker URL and the topic to subscribe. Additionally this constructor takes in the
	 * reconnection-time interval between successive attempts to connect to the broker.
	 *
	 * @param deviceOwner          the owner of the device.
	 * @param deviceType           the CDMF Device-Type of the device.
	 * @param mqttBrokerEndPoint   the IP/URL of the MQTT broker endpoint.
	 * @param subscribeTopic       the MQTT topic to which the client is to be subscribed
	 * @param reConnectionInterval time interval in SECONDS between successive attempts to connect
	 *                             to the broker.
	 */
	protected MQTTClient(String deviceOwner, String deviceType, String mqttBrokerEndPoint,
	                     String subscribeTopic, int reConnectionInterval) {
		this.clientId = deviceOwner + ":" + deviceType;
		this.subscribeTopic = subscribeTopic;
		this.clientWillTopic = deviceType + File.separator + "disconnection";
		this.mqttBrokerEndPoint = mqttBrokerEndPoint;
		this.reConnectionInterval = reConnectionInterval;
		this.initSubscriber();
	}

	/**
	 * Initializes the MQTT-Client.
	 * Creates a client using the given MQTT-broker endpoint and the clientId (which is
	 * constructed by a concatenation of [deviceOwner]:[deviceType]). Also sets the client's
	 * options parameter with the clientWillTopic (in-case of connection failure) and other info.
	 * Also sets the call-back this current class.
	 */
	private void initSubscriber() {
		try {
			client = new MqttClient(this.mqttBrokerEndPoint, clientId, null);
			log.info("MQTT subscriber was created with ClientID : " + clientId);
		} catch (MqttException ex) {
			String errorMsg = "MQTT Client Error\n" + "\tReason:  " + ex.getReasonCode() +
					"\n\tMessage: " + ex.getMessage() + "\n\tLocalMsg: " +
					ex.getLocalizedMessage() + "\n\tCause: " + ex.getCause() +
					"\n\tException: " + ex;
			log.error(errorMsg);
		}

		options = new MqttConnectOptions();
		options.setCleanSession(false);
		options.setWill(clientWillTopic, "connection crashed".getBytes(StandardCharsets.UTF_8), 2,
		                true);
		client.setCallback(this);
	}

	/**
	 * Checks whether the connection to the MQTT-Broker persists.
	 *
	 * @return true if the client is connected to the MQTT-Broker, else false.
	 */
	public boolean isConnected() {
		return client.isConnected();
	}

	/**
	 * Connects to the MQTT-Broker and if successfully established connection, then tries to
	 * subscribe to the MQTT-Topic specific to the device. (The MQTT-Topic specific to the
	 * device is created is taken in as a constructor parameter of this class) .
	 *
	 * @throws DeviceManagementException in the event of 'Connecting to' or 'Subscribing to' the
	 *                                     MQTT broker fails.
	 */
	public void connectAndSubscribe() throws DeviceManagementException {
		try {
			client.connect(options);

			if (log.isDebugEnabled()) {
				log.debug("Subscriber connected to queue at: " + this.mqttBrokerEndPoint);
			}
		} catch (MqttSecurityException ex) {
			String errorMsg = "MQTT Security Exception when connecting to queue\n" + "\tReason: " +
					" " +
					ex.getReasonCode() + "\n\tMessage: " + ex.getMessage() +
					"\n\tLocalMsg: " + ex.getLocalizedMessage() + "\n\tCause: " +
					ex.getCause() + "\n\tException: " + ex; //throw
			if (log.isDebugEnabled()) {
				log.debug(errorMsg);
			}
			throw new DeviceManagementException(errorMsg, ex);

		} catch (MqttException ex) {
			String errorMsg = "MQTT Exception when connecting to queue\n" + "\tReason:  " +
					ex.getReasonCode() + "\n\tMessage: " + ex.getMessage() +
					"\n\tLocalMsg: " + ex.getLocalizedMessage() + "\n\tCause: " +
					ex.getCause() + "\n\tException: " + ex; //throw
			if (log.isDebugEnabled()) {
				log.debug(errorMsg);
			}
			throw new DeviceManagementException(errorMsg, ex);
		}

		try {
			client.subscribe(subscribeTopic, 0);

			log.info("Subscriber - " + clientId + " subscribed to topic: " + subscribeTopic);
		} catch (MqttException ex) {
			String errorMsg = "MQTT Exception when trying to subscribe to topic: " +
					subscribeTopic + "\n\tReason:  " + ex.getReasonCode() +
					"\n\tMessage: " + ex.getMessage() + "\n\tLocalMsg: " +
					ex.getLocalizedMessage() + "\n\tCause: " + ex.getCause() +
					"\n\tException: " + ex;
			if (log.isDebugEnabled()) {
				log.debug(errorMsg);
			}
			throw new DeviceManagementException(errorMsg, ex);
		}
	}

	/**
	 * Callback method which is triggered once the MQTT client losers its connection to the broker.
	 * A scheduler thread is spawned to continuously re-attempt and connect to the broker and
	 * subscribe to the device's topic. This thread is scheduled to execute after every break
	 * equal to that of the 'reConnectionInterval' of the MQTTClient.
	 *
	 * @param throwable a Throwable Object containing the details as to why the failure occurred.
	 */
	public void connectionLost(Throwable throwable) {
		log.warn("Lost Connection for client: " + this.clientId + " to " + this.mqttBrokerEndPoint + ".\nThis was due to - " + throwable.getMessage());

		Runnable reSubscriber = new Runnable() {
			@Override
			public void run() {
				if (!isConnected()) {
					if (log.isDebugEnabled()) {
						log.debug("Subscriber reconnecting to queue........");
					}
					try {
						connectAndSubscribe();
					} catch (DeviceManagementException e) {
						if (log.isDebugEnabled()) {
							log.debug("Could not reconnect and subscribe to ControlQueue.");
						}
					}
				} else {
					return;
				}
			}
		};

		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.scheduleAtFixedRate(reSubscriber, 0, this.reConnectionInterval, TimeUnit.SECONDS);
	}

	/**
	 * Callback method which is triggered upon receiving a MQTT Message from the broker. Spawns a
	 * new thread that executes any actions to be taken with the received message.
	 *
	 * @param topic       the MQTT-Topic to which the received message was published to and the
	 *                    client was subscribed to.
	 * @param mqttMessage the actual MQTT-Message that was received from the broker.
	 */
	public void messageArrived(final String topic, final MqttMessage mqttMessage) {
		Thread subscriberThread = new Thread() {
			public void run() {
				postMessageArrived(topic, mqttMessage);
			}
		};
		subscriberThread.start();
	}

	/**
	 * Callback method which gets triggered upon successful completion of a message delivery to
	 * the broker.
	 *
	 * @param iMqttDeliveryToken the MQTT-DeliveryToken which includes the details about the
	 *                           specific message delivery.
	 */
	public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
		String message = "";
		try {
			message = iMqttDeliveryToken.getMessage().toString();
		} catch (MqttException e) {
			log.error("Error occurred whilst trying to read the message from the MQTT  delivery token.");
		}
		String topic = iMqttDeliveryToken.getTopics()[0];
		String client = iMqttDeliveryToken.getClient().getClientId();
		log.info("Message - '" + message + "' of client [" + client + "] for the topic (" + topic + ") was delivered successfully.");
	}

	/**
	 * This is an abstract method used for post processing the received MQTT-message. This
	 * method will be implemented as per requirement at the time of creating an object of this
	 * class.
	 *
	 * @param topic   The topic for which the message was received for.
	 * @param message The message received for the subscription to the above topic.
	 */
	protected abstract void postMessageArrived(String topic, MqttMessage message);

	/**
	 * Gets the MQTTClient object.
	 *
	 * @return the MQTTClient object.
	 */
	public MqttClient getClient() {
		return client;
	}

}

