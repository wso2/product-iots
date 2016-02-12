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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.coffeeking.controller.service.transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.coffeeking.connectedcup.plugin.constants.ConnectedCupConstants;
import org.coffeeking.controller.service.util.ConnectedCupServiceUtils;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.wso2.carbon.device.mgt.iot.config.server.DeviceManagementConfigurationManager;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.transport.TransportHandlerException;
import org.wso2.carbon.device.mgt.iot.transport.mqtt.MQTTTransportHandler;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.UUID;

@SuppressWarnings("no JAX-WS annotation")
public class ConnectedCupMQTTConnector extends MQTTTransportHandler {
    private static Log log = LogFactory.getLog(ConnectedCupMQTTConnector.class);

    private static String serverName = DeviceManagementConfigurationManager.getInstance().
            getDeviceManagementServerInfo().getName();

    private static String subscribeTopic = "wso2" + File.separator + "+" + File.separator +
                                           ConnectedCupConstants.DEVICE_TYPE + File.separator + "+" + File.separator
                                           + "connected_publisher";

    private static String iotServerSubscriber = UUID.randomUUID().toString().substring(0, 5);

    private ConnectedCupMQTTConnector() {
        super(iotServerSubscriber, ConnectedCupConstants.DEVICE_TYPE,
              MqttConfig.getInstance().getMqttQueueEndpoint(), subscribeTopic);
    }

    @Override
    public void connect() {
        Runnable connector = new Runnable() {
            public void run() {
                while (!isConnected()) {
                    try {
                        connectToQueue();
                        subscribeToQueue();
                    } catch (TransportHandlerException e) {
                        log.warn("Connection/Subscription to MQTT Broker at: " + mqttBrokerEndPoint + " failed");
                        try {
                            Thread.sleep(timeoutInterval);
                        } catch (InterruptedException ex) {
                            log.error("MQTT-Subscriber: Thread Sleep Interrupt Exception.", ex);
                        }
                    }
                }
            }
        };

        Thread connectorThread = new Thread(connector);
        connectorThread.setDaemon(true);
        connectorThread.start();
    }



    @Override
    public void publishDeviceData(String... publishData) throws TransportHandlerException {
        if (publishData.length != 4) {
            String errorMsg = "Incorrect number of arguments received to SEND-MQTT Message. " +
                              "Need to be [owner, deviceId, resource{BULB/TEMP}, state{ON/OFF or null}]";
            log.error(errorMsg);
            throw new TransportHandlerException(errorMsg);
        }

        String deviceOwner = publishData[0];
        String deviceId = publishData[1];
        String resource = publishData[2];
        String state = publishData[3];

        MqttMessage pushMessage = new MqttMessage();
        String publishTopic =
                "wso2" + File.separator + deviceOwner + File.separator +
                ConnectedCupConstants.DEVICE_TYPE + File.separator + deviceId;

        try {

            String actualMessage = resource + ":" + state;

            pushMessage.setPayload(actualMessage.getBytes(StandardCharsets.UTF_8));
            pushMessage.setQos(DEFAULT_MQTT_QUALITY_OF_SERVICE);
            pushMessage.setRetained(false);

            publishToQueue(publishTopic, pushMessage);

        } catch (Exception e) {
            String errorMsg = "Preparing payload failed for device - [" + deviceId + "] of owner - " +
                              "[" + deviceOwner + "].";
            log.error(errorMsg);
            throw new TransportHandlerException(errorMsg, e);
        }
    }


    @Override
    public void processIncomingMessage(MqttMessage mqttMessage, String... strings) throws TransportHandlerException {
        String topic = strings[0];


        String ownerAndId = topic.replace("wso2" + File.separator, "");
        ownerAndId = ownerAndId.replace(File.separator + ConnectedCupConstants.DEVICE_TYPE + File.separator, ":");
        ownerAndId = ownerAndId.replace(File.separator + "connected_publisher", "");

        String owner = ownerAndId.split(":")[0];
        String deviceId = ownerAndId.split(":")[1];

//        String actualMessage = mqttMessage.toString();
        String[] messageData = mqttMessage.toString().split(":");
        Float value = Float.valueOf(messageData[1]);

//        if (actualMessage.contains("PUBLISHER")) {
//            float temperature = Float.parseFloat(actualMessage.split(":")[2]);
//
//            if (!ConnectedCupServiceUtils.publishToDAS(owner, deviceId, messageData[0], value)) {
//                log.error("MQTT Subscriber: Publishing data to DAS failed.");
//            }
//
//            if (log.isDebugEnabled()) {
//                log.debug("MQTT Subscriber: Published data to DAS successfully.");
//            }
//
//        } else if (actualMessage.contains("TEMPERATURE")) {
//            String temperatureValue = actualMessage.split(":")[1];
//            SensorDataManager.getInstance().setSensorRecord(deviceId, ConnectedCupConstants.SENSOR_TEMPERATURE,
//                                                            temperatureValue,
//                                                            Calendar.getInstance().getTimeInMillis());
//        }
//
//

        switch(messageData[0]) {
            case "temperature":
                SensorDataManager.getInstance().setSensorRecord(deviceId, ConnectedCupConstants.SENSOR_TEMPERATURE,
                                                                                String.valueOf(messageData[1]),
                                                                                Calendar.getInstance().getTimeInMillis());
                break;
            case "coffeelevel":
                SensorDataManager.getInstance().setSensorRecord(deviceId, ConnectedCupConstants.SENSOR_LEVEL,
                                                                                String.valueOf(messageData[1]),
                                                                                Calendar.getInstance().getTimeInMillis());
                break;
        }


        ConnectedCupServiceUtils.publishToDAS(owner, deviceId, messageData[0], value);

        if (log.isDebugEnabled()) {
            log.debug("Received MQTT message for OWNER: " + owner + " DEVICE.ID: " + deviceId + " | Command: " +
                      messageData[0] +" " + messageData[1] );
        }
    }


    @Override
    public void disconnect() {
        Runnable stopConnection = new Runnable() {
            public void run() {
                while (isConnected()) {
                    try {
                        closeConnection();
                    } catch (MqttException e) {
                        if (log.isDebugEnabled()) {
                            log.warn("Unable to 'STOP' MQTT connection at broker at: " + mqttBrokerEndPoint
                                     + " for device-type - " + ConnectedCupConstants.DEVICE_TYPE, e);
                        }

                        try {
                            Thread.sleep(timeoutInterval);
                        } catch (InterruptedException e1) {
                            log.error("MQTT-Terminator: Thread Sleep Interrupt Exception at device-type - " +
                                      ConnectedCupConstants.DEVICE_TYPE, e1);
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
    public void publishDeviceData() {
        // nothing to do
    }


    @Override
    public void publishDeviceData(MqttMessage publishData) throws TransportHandlerException {
        // nothing to do
    }

    @Override
    public void processIncomingMessage() {
        // nothing to do
    }

    @Override
    public void processIncomingMessage(MqttMessage message) throws TransportHandlerException {
        // nothing to do
    }
}
