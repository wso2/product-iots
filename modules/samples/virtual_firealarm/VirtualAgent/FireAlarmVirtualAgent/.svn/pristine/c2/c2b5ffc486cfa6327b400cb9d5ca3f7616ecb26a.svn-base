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

package org.wso2.carbon.device.mgt.iot.agent.firealarm.communication.mqtt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.communication.CommunicationHandler;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.communication.CommunicationHandlerException;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * This class contains the IoT-Server specific implementation for all the MQTT functionality.
 * This includes connecting to a MQTT Broker & subscribing to the appropriate MQTT-topic, action
 * plan upon losing connection or successfully delivering a message to the broker and processing
 * incoming messages. Makes use of the 'Paho-MQTT' library provided by Eclipse Org.
 * <p/>
 * It is an abstract class that implements the common interface "CommunicationHandler" and the
 * "MqttCallback". Whilst providing some methods which handle key MQTT relevant tasks, this class
 * implements only the most generic methods of the "CommunicationHandler" interface. The rest of
 * the methods are left for any extended concrete-class to implement as per its need.
 */
public abstract class MQTTCommunicationHandler
		implements MqttCallback, CommunicationHandler<MqttMessage> {
	private static final Log log = LogFactory.getLog(MQTTCommunicationHandler.class);

	public static final int DEFAULT_MQTT_QUALITY_OF_SERVICE = 0;

	private MqttClient client;
	private String clientId;
	private MqttConnectOptions options;
	private String clientWillTopic;

	protected String mqttBrokerEndPoint;
	protected int timeoutInterval;
	protected String subscribeTopic;

	/**
	 * Constructor for the MQTTCommunicationHandler which takes in the owner, type of the device
	 * and the MQTT Broker URL and the topic to subscribe.
	 *
	 * @param deviceOwner        the owner of the device.
	 * @param deviceType         the CDMF Device-Type of the device.
	 * @param mqttBrokerEndPoint the IP/URL of the MQTT broker endpoint.
	 * @param subscribeTopic     the MQTT topic to which the client is to be subscribed
	 */
	protected MQTTCommunicationHandler(String deviceOwner, String deviceType,
	                                   String mqttBrokerEndPoint,
	                                   String subscribeTopic) {
		this.clientId = deviceOwner + ":" + deviceType;
		this.subscribeTopic = subscribeTopic;
		this.clientWillTopic = deviceType + File.separator + "disconnection";
		this.mqttBrokerEndPoint = mqttBrokerEndPoint;
		this.timeoutInterval = DEFAULT_TIMEOUT_INTERVAL;
		this.initSubscriber();
	}

	/**
	 * Constructor for the MQTTCommunicationHandler which takes in the owner, type of the device
	 * and the MQTT Broker URL and the topic to subscribe. Additionally this constructor takes in
	 * the reconnection-time interval between successive attempts to connect to the broker.
	 *
	 * @param deviceOwner        the owner of the device.
	 * @param deviceType         the CDMF Device-Type of the device.
	 * @param mqttBrokerEndPoint the IP/URL of the MQTT broker endpoint.
	 * @param subscribeTopic     the MQTT topic to which the client is to be subscribed
	 * @param intervalInMillis   the time interval in MILLI-SECONDS between successive
	 *                           attempts to connect to the broker.
	 */
	protected MQTTCommunicationHandler(String deviceOwner, String deviceType,
	                                   String mqttBrokerEndPoint, String subscribeTopic,
	                                   int intervalInMillis) {
		this.clientId = deviceOwner + ":" + deviceType;
		this.subscribeTopic = subscribeTopic;
		this.clientWillTopic = deviceType + File.separator + "disconnection";
		this.mqttBrokerEndPoint = mqttBrokerEndPoint;
		this.timeoutInterval = intervalInMillis;
		this.initSubscriber();
	}

	public void setTimeoutInterval(int timeoutInterval) {
		this.timeoutInterval = timeoutInterval;
	}

	/**
	 * Initializes the MQTT-Client. Creates a client using the given MQTT-broker endpoint and the
	 * clientId (which is constructed by a concatenation of [deviceOwner]:[deviceType]). Also sets
	 * the client's options parameter with the clientWillTopic (in-case of connection failure) and
	 * other info. Also sets the call-back this current class.
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
		options.setWill(clientWillTopic, "Connection-Lost".getBytes(StandardCharsets.UTF_8), 2,
		                true);
		client.setCallback(this);
	}

	/**
	 * Checks whether the connection to the MQTT-Broker persists.
	 *
	 * @return true if the client is connected to the MQTT-Broker, else false.
	 */
	@Override
	public boolean isConnected() {
		return client.isConnected();
	}


	/**
	 * Connects to the MQTT-Broker and if successfully established connection.
	 *
	 * @throws CommunicationHandlerException in the event of 'Connecting to' the MQTT broker fails.
	 */
	protected void connectToQueue() throws CommunicationHandlerException {
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
					ex.getCause() + "\n\tException: " + ex;
			if (log.isDebugEnabled()) {
				log.debug(errorMsg);
			}
			throw new CommunicationHandlerException(errorMsg, ex);

		} catch (MqttException ex) {
			String errorMsg = "MQTT Exception when connecting to queue\n" + "\tReason:  " +
					ex.getReasonCode() + "\n\tMessage: " + ex.getMessage() +
					"\n\tLocalMsg: " + ex.getLocalizedMessage() + "\n\tCause: " +
					ex.getCause() + "\n\tException: " + ex;
			if (log.isDebugEnabled()) {
				log.debug(errorMsg);
			}
			throw new CommunicationHandlerException(errorMsg, ex);
		}
	}

	/**
	 * Subscribes to the MQTT-Topic specific to this MQTT Client. (The MQTT-Topic specific to the
	 * device is taken in as a constructor parameter of this class) .
	 *
	 * @throws CommunicationHandlerException in the event of 'Subscribing to' the MQTT broker
	 *                                       fails.
	 */
	protected void subscribeToQueue() throws CommunicationHandlerException {
		try {
			client.subscribe(subscribeTopic, 0);
			log.info("Subscriber '" + clientId + "' subscribed to topic: " + subscribeTopic);
		} catch (MqttException ex) {
			String errorMsg = "MQTT Exception when trying to subscribe to topic: " +
					subscribeTopic + "\n\tReason:  " + ex.getReasonCode() +
					"\n\tMessage: " + ex.getMessage() + "\n\tLocalMsg: " +
					ex.getLocalizedMessage() + "\n\tCause: " + ex.getCause() +
					"\n\tException: " + ex;
			if (log.isDebugEnabled()) {
				log.debug(errorMsg);
			}

			throw new CommunicationHandlerException(errorMsg, ex);
		}
	}


	/**
	 * This method is used to publish reply-messages for the control signals received.
	 * Invocation of this method calls its overloaded-method with a QoS equal to that of the
	 * default value.
	 *
	 * @param topic   the topic to which the reply message is to be published.
	 * @param payLoad the reply-message (payload) of the MQTT publish action.
	 */
	protected void publishToQueue(String topic, String payLoad)
			throws CommunicationHandlerException {
		publishToQueue(topic, payLoad, DEFAULT_MQTT_QUALITY_OF_SERVICE, false);
	}

	/**
	 * This is an overloaded method that publishes MQTT reply-messages for control signals
	 * received form the IoT-Server.
	 *
	 * @param topic   the topic to which the reply message is to be published
	 * @param payLoad the reply-message (payload) of the MQTT publish action.
	 * @param qos     the Quality-of-Service of the current publish action.
	 *                Could be 0(At-most once), 1(At-least once) or 2(Exactly once)
	 */
	protected void publishToQueue(String topic, String payLoad, int qos, boolean retained)
			throws CommunicationHandlerException {
		try {
			client.publish(topic, payLoad.getBytes(StandardCharsets.UTF_8), qos, retained);
			if (log.isDebugEnabled()) {
				log.debug("Message: " + payLoad + " to MQTT topic [" + topic +
						          "] published successfully");
			}
		} catch (MqttException ex) {
			String errorMsg =
					"MQTT Client Error" + "\n\tReason:  " + ex.getReasonCode() + "\n\tMessage: " +
							ex.getMessage() + "\n\tLocalMsg: " + ex.getLocalizedMessage() +
							"\n\tCause: " + ex.getCause() + "\n\tException: " + ex;
			log.info(errorMsg);
			throw new CommunicationHandlerException(errorMsg, ex);
		}
	}


	protected void publishToQueue(String topic, MqttMessage message)
			throws CommunicationHandlerException {
		try {
			client.publish(topic, message);
			if (log.isDebugEnabled()) {
				log.debug("Message: " + message.toString() + " to MQTT topic [" + topic +
						          "] published successfully");
			}
		} catch (MqttException ex) {
			String errorMsg =
					"MQTT Client Error" + "\n\tReason:  " + ex.getReasonCode() + "\n\tMessage: " +
							ex.getMessage() + "\n\tLocalMsg: " + ex.getLocalizedMessage() +
							"\n\tCause: " + ex.getCause() + "\n\tException: " + ex;
			log.info(errorMsg);
			throw new CommunicationHandlerException(errorMsg, ex);
		}
	}


	/**
	 * Callback method which is triggered once the MQTT client losers its connection to the broker.
	 * Spawns a new thread that executes necessary actions to try and reconnect to the endpoint.
	 *
	 * @param throwable a Throwable Object containing the details as to why the failure occurred.
	 */
	@Override
	public void connectionLost(Throwable throwable) {
		log.warn("Lost Connection for client: " + this.clientId +
				         " to " + this.mqttBrokerEndPoint + ".\nThis was due to - " +
				         throwable.getMessage());

		Thread reconnectThread = new Thread() {
			public void run() {
				connect();
			}
		};
		reconnectThread.setDaemon(true);
		reconnectThread.start();
	}

	/**
	 * Callback method which is triggered upon receiving a MQTT Message from the broker. Spawns a
	 * new thread that executes any actions to be taken with the received message.
	 *
	 * @param topic       the MQTT-Topic to which the received message was published to and the
	 *                    client was subscribed to.
	 * @param mqttMessage the actual MQTT-Message that was received from the broker.
	 */
	@Override
	public void messageArrived(final String topic, final MqttMessage mqttMessage) {
		if (log.isDebugEnabled()) {
			log.info("Got an MQTT message '" + mqttMessage.toString() + "' for topic '" + topic +
					         "'.");
		}

		Thread messageProcessorThread = new Thread() {
			public void run() {
				processIncomingMessage(mqttMessage);
			}
		};
		messageProcessorThread.setDaemon(true);
		messageProcessorThread.start();
	}

	/**
	 * Callback method which gets triggered upon successful completion of a message delivery to
	 * the broker.
	 *
	 * @param iMqttDeliveryToken the MQTT-DeliveryToken which includes the details about the
	 *                           specific message delivery.
	 */
	@Override
	public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
		String message = "";
		try {
			message = iMqttDeliveryToken.getMessage().toString();
		} catch (MqttException e) {
			log.error(
					"Error occurred whilst trying to read the message from the MQTT delivery " +
							"token.");
		}
		String topic = iMqttDeliveryToken.getTopics()[0];
		String client = iMqttDeliveryToken.getClient().getClientId();

		if (log.isDebugEnabled()) {
			log.debug("Message - '" + message + "' of client [" + client + "] for the topic (" +
					          topic + ") was delivered successfully.");
		}
	}

	/**
	 * Closes the connection to the MQTT Broker.
	 */
	public void closeConnection() throws MqttException {
		if (client != null && isConnected()) {
			client.disconnect();
		}
	}
}

