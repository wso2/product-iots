package org.homeautomation.doormanager.controller.api.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.homeautomation.doormanager.controller.api.exception.DoorManagerException;
import org.homeautomation.doormanager.plugin.constants.DoorManagerConstants;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.transport.TransportHandlerException;
import org.wso2.carbon.device.mgt.iot.transport.mqtt.MQTTTransportHandler;

import java.io.File;
import java.util.Calendar;
import java.util.UUID;

@SuppressWarnings("no JAX-WS annotation")
public class DoorManagerMQTTConnector extends MQTTTransportHandler {

    private static Log log = LogFactory.getLog(DoorManagerMQTTConnector.class);
    private static final String subscribeTopic =
            "wso2" + File.separator + "iot" + File.separator + "+" + File.separator +
                    DoorManagerConstants.DEVICE_TYPE + File.separator + "+" + File.separator +
                    "lockStatusPublisher"+ File.separator;
    private static String iotServerSubscriber = UUID.randomUUID().toString().substring(0, 5);

    private DoorManagerMQTTConnector() {
        super(iotServerSubscriber, DoorManagerConstants.DEVICE_TYPE,
              MqttConfig.getInstance().getMqttQueueEndpoint(), subscribeTopic);
    }

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

    @Override
    public void processIncomingMessage(MqttMessage message, String... messageParams) {
        String topic = messageParams[0];
        String ownerAndId = topic.replace("wso2" + File.separator + "iot" + File.separator, "");
        ownerAndId = ownerAndId.replace(File.separator + DoorManagerConstants.DEVICE_TYPE
                + File.separator, ":");
        ownerAndId = ownerAndId.replace(File.separator + "lockStatusPublisher", "");
        String owner = ownerAndId.split(":")[0];
        String deviceId = ownerAndId.split(":")[1];
        log.warn(deviceId);

        String[] messageData = message.toString().split(":");

        if (log.isDebugEnabled()){
            log.debug("Received MQTT message for: [OWNER-" + owner + "] & [DEVICE.ID-" + deviceId + "]");
        }
        if (messageData.length == 2) {
            log.warn("-------------------------------------------");
            log.warn(messageData[2]);
            log.warn(messageData[1]);
            String lockerCurrentState = messageData[1];
            try {
                SensorDataManager.getInstance().setSensorRecord(deviceId, "door_locker_state",
                        String.valueOf(1), Calendar.getInstance().getTimeInMillis());
            } catch(Exception e){
               log.error(e);
            }
            if(log.isDebugEnabled()){
                //log.debug("Return result " + result + " for Request " + requestMessage);
            }
        }
    }

    private void publishToAutomaticDoorLocker(String topic, String payLoad, int qos, boolean retained)
            throws TransportHandlerException {
        if(log.isDebugEnabled()){
            log.debug("Publishing message [" + payLoad + "to topic [" + topic + "].");
        }
        publishToQueue(topic, payLoad, qos, retained);
    }

    public void sendCommandViaMQTT(String deviceOwner, String deviceId, String operation, String param)
            throws DeviceManagementException, DoorManagerException {
        String PUBLISH_TOPIC = "wso2/iot/%s/doormanager/%s/lockController";
        String topic = String.format(PUBLISH_TOPIC, deviceOwner, deviceId);
        String payload = operation + param;
        try {
            publishToAutomaticDoorLocker(topic, payload, 2, false);
        } catch (TransportHandlerException e) {
            String errorMessage = "Error publishing data to device with ID " + deviceId;
            throw new DoorManagerException(errorMessage, e);
        }
    }

    @Override
    public void disconnect() {
        Runnable stopConnection = new Runnable() {
            public void run() {
                while (isConnected()) {
                    try {
                        closeConnection();
                    }
                    catch (MqttException e) {
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
