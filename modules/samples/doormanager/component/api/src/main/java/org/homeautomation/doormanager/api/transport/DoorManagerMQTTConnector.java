/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.homeautomation.doormanager.api.transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.homeautomation.doormanager.api.exception.DoorManagerException;
import org.homeautomation.doormanager.api.util.DoorManagerServiceUtils;
import org.homeautomation.doormanager.plugin.constants.DoorManagerConstants;
import org.wso2.carbon.device.mgt.analytics.data.publisher.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.transport.TransportHandlerException;
import org.wso2.carbon.device.mgt.iot.transport.mqtt.MQTTTransportHandler;

import java.io.File;
import java.util.Calendar;
import java.util.UUID;

/**
 * MQTT is used as transport protocol. So this will provide basic functional requirement in order to communicate over
 * MQTT
 */
@SuppressWarnings("no JAX-WS annotation")
public class DoorManagerMQTTConnector extends MQTTTransportHandler {

    private static Log log = LogFactory.getLog(DoorManagerMQTTConnector.class);
    private static String PUBLISHER_CONTEXT = "publisher";
    private static String SUBCRIBER_TOPIC = "wso2/" + DoorManagerConstants.DEVICE_TYPE + "/+/publisher";
    private static String PUBLISHER_TOPIC = "wso2/"+ DoorManagerConstants.DEVICE_TYPE + "/%s/subscriber";
    private static String SUBSCRIBER = UUID.randomUUID().toString().substring(0, 5);

    private DoorManagerMQTTConnector() {
        super(SUBSCRIBER, DoorManagerConstants.DEVICE_TYPE,
              MqttConfig.getInstance().getMqttQueueEndpoint(), SUBCRIBER_TOPIC);
    }

    /**
     * This method will initialize connection with message broker
     */
    @Override
    public void connect() {
        Runnable connector = new Runnable() {
            public void run() {
                while (!isConnected()) {
                    try {
                        String brokerUsername = MqttConfig.getInstance().getMqttQueueUsername();
                        String brokerPassword = MqttConfig.getInstance().getMqttQueuePassword();
                        setUsernameAndPassword(brokerUsername, brokerPassword);
                        connectToQueue();
                    } catch (TransportHandlerException e) {
                        log.error("Connection to MQTT Broker at: " + mqttBrokerEndPoint + " failed", e);
                        try {
                            Thread.sleep(timeoutInterval);
                        } catch (InterruptedException ex) {
                            log.error("MQTT-Connector: Thread Sleep Interrupt Exception.", ex);
                        }
                    }
                    try {
                        subscribeToQueue();
                    } catch (TransportHandlerException e) {
                        log.warn("Subscription to MQTT Broker at: " + mqttBrokerEndPoint + " failed", e);
                    }
                }
            }
        };
        Thread connectorThread = new Thread(connector);
        connectorThread.setDaemon(true);
        connectorThread.start();
    }

    /**
     * This callback function will be called by message broker when some messages available to subscribed topic
     *
     * @param message       mqtt message which is coming form agent side
     * @param messageParams metadata of mqtt message
     */
    @Override
    public void processIncomingMessage(MqttMessage message, String... messageParams) {
        String topic = messageParams[0];
        String ownerAndId = topic.replace("wso2" + File.separator + "iot" + File.separator, "");
        ownerAndId = ownerAndId.replace(File.separator + DoorManagerConstants.DEVICE_TYPE
                                        + File.separator, ":");
        ownerAndId = ownerAndId.replace(File.separator + PUBLISHER_CONTEXT, "");
        String owner = ownerAndId.split(":")[0];
        String deviceId = ownerAndId.split(":")[1];
        log.warn(deviceId);
        String[] messageData = message.toString().split(":");
        if (log.isDebugEnabled()) {
            log.debug("Received MQTT message for: [OWNER-" + owner + "] & [DEVICE.ID-" + deviceId + "]");
        }
        if (messageData.length == 2) {
            String lockerCurrentState = messageData[1];
            float lockerStatus;
            if (lockerCurrentState.equals("LOCKED")) {
                lockerStatus = 0;
            } else {
                lockerStatus = 1;
            }
            try {
                SensorDataManager.getInstance().setSensorRecord(deviceId, "doorLockerCurrentStatus",
                                                                String.valueOf(lockerStatus), Calendar.getInstance().getTimeInMillis());
                if (!DoorManagerServiceUtils.publishToDASLockerStatus(owner, deviceId, lockerStatus)) {
                    log.warn("An error occurred while trying to publish  with ID [" + deviceId + "] of owner ["
                             + owner + "]");
                }
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    /**
     * Publish a MQTT message to device through message broker
     *
     * @param topic    mqtt topic which will be used to uniquely identify who are the subscribers to this topic
     * @param payLoad  message is to be published
     * @param qos      level of qos(quality of service):1,2,3
     * @param retained klkkl
     * @throws TransportHandlerException
     */
    private void publishToAutomaticDoorLocker(String topic, String payLoad, int qos, boolean retained)
            throws TransportHandlerException {
        if (log.isDebugEnabled()) {
            log.debug("Publishing message [" + payLoad + "to topic [" + topic + "].");
        }
        publishToQueue(topic, payLoad, qos, retained);
    }

    /**
     * Publish a MQTT message to device through message broker
     *
     * @param deviceOwner person who own the device
     * @param deviceId    unique identifier for each device
     * @param operation   command is to executed at agent side e.g: off, on
     * @param param       additional payload
     * @throws DeviceManagementException
     * @throws DoorManagerException
     */
    public void sendCommandViaMQTT(String deviceOwner, String deviceId, String operation, String param)
            throws DeviceManagementException, DoorManagerException {
        String topic = String.format(PUBLISHER_TOPIC, deviceOwner, deviceId);
        String payload = operation + param;
        try {
            publishToAutomaticDoorLocker(topic, payload, 2, false);
            if (param.toUpperCase().equals("LOCK")) {
                if (!DoorManagerServiceUtils.publishToDASLockerStatus(deviceOwner, deviceId, 0)) {
                    log.warn("An error occurred whilst trying to publish  with ID [" + deviceId + "] of owner [" +
                             deviceOwner + "]");
                }
            } else if (param.toUpperCase().equals("UNLOCK")) {
                if (!DoorManagerServiceUtils.publishToDASLockerStatus(deviceOwner, deviceId, 1)) {
                    log.warn("An error occurred whilst trying to publish  with ID [" + deviceId + "] of owner [" +
                             deviceOwner + "]");
                }
            }
        } catch (TransportHandlerException e) {
            String errorMessage = "Error publishing data to device with ID " + deviceId;
            throw new DoorManagerException(errorMessage, e);
        } catch (DataPublisherConfigurationException e) {
            String errorMessage = "Error publishing data to DAS with ID " + deviceId;
            throw new DoorManagerException(errorMessage, e);
        }
    }

    /**
     * Connection with message broker can be terminated
     */
    @Override
    public void disconnect() {
        Runnable stopConnection = new Runnable() {
            public void run() {
                while (isConnected()) {
                    try {
                        closeConnection();
                    } catch (MqttException e) {
                        if (log.isDebugEnabled()) {
                            log.warn("Unable to 'STOP' MQTT connection at broker at: " + mqttBrokerEndPoint);
                        }
                        try {
                            Thread.sleep(timeoutInterval);
                        } catch (InterruptedException e1) {
                            log.error("MQTT-Terminator: Thread Sleep Interrupt Exception");
                        }
                    }
                }
            }
        };
        Thread terminatorThread = new Thread(stopConnection);
        terminatorThread.setDaemon(true);
        terminatorThread.start();
    }


    @Override
    public void publishDeviceData() throws TransportHandlerException {

    }

    @Override
    public void publishDeviceData(MqttMessage publishData) throws TransportHandlerException {

    }

    @Override
    public void publishDeviceData(String... publishData) throws TransportHandlerException {

    }

    @Override
    public void processIncomingMessage() {

    }

    @Override
    public void processIncomingMessage(MqttMessage message) throws TransportHandlerException {

    }

}
