package org.wso2.carbon.device.mgt.iot.sample.digitaldisplay.service.impl.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.wso2.carbon.device.mgt.iot.common.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.sample.digitaldisplay.plugin.constants.DigitalDisplayConstants;
import org.wso2.carbon.device.mgt.iot.sample.digitaldisplay.service.impl.communication.CommunicationHandlerException;
import org.wso2.carbon.device.mgt.iot.sample.digitaldisplay.service.impl.communication.mqtt.MQTTCommunicationHandler;
import org.wso2.carbon.device.mgt.iot.sample.digitaldisplay.service.impl.websocket.DigitalDisplayWebSocketServerEndPoint;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;


/**
 * Created by nuwan on 11/16/15.
 */

public class DigitalDisplayMqttCommunicationHandler extends MQTTCommunicationHandler {

    private static Log log = LogFactory.getLog(DigitalDisplayMqttCommunicationHandler.class);

    private static final String subscribeTopic =
            "wso2"+ File.separator+"iot"+File.separator+"+"+File.separator+
                    DigitalDisplayConstants.DEVICE_TYPE+File.separator+"+"+File.separator+
                    "digital_display_publisher";

    private static String iotServerSubscriber = UUID.randomUUID().toString().substring(0,5);

    private ScheduledFuture<?> dataPushServiceHandler;

    private DigitalDisplayMqttCommunicationHandler() {
        super(iotServerSubscriber, DigitalDisplayConstants.DEVICE_TYPE,
                MqttConfig.getInstance().getMqttQueueEndpoint(), subscribeTopic);
    }

    public ScheduledFuture<?> getDataPushServiceHandler() {
        return dataPushServiceHandler;
    }

    @Override
    public void connect() {
        Runnable connect = new Runnable() {
            @Override
            public void run() {
                while (!isConnected()){
                    try {
                        log.info("Trying to Connect..");
                        connectToQueue();
                        subscribeToQueue();

                    } catch (CommunicationHandlerException e) {
                        log.warn("Connection/Subscription to MQTT Broker at: " +
                                mqttBrokerEndPoint + " failed");

                        try {
                            Thread.sleep(timeoutInterval);
                        } catch (InterruptedException ex) {
                            log.error("MQTT-Subscriber: Thread Sleep Interrupt Exception");
                        }
                    }
                }

                log.info("Connected..");

            }
        };

        Thread connectorThread = new Thread(connect);
        connectorThread.setDaemon(true);
        connectorThread.start();

    }

    @Override
    public void processIncomingMessage(MqttMessage message, String... messageParams) {

        String topic = messageParams[0];

        String ownerAndId = topic.replace("wso2"+File.separator+"iot"+File.separator,"");
        ownerAndId = ownerAndId.replace(File.separator+ DigitalDisplayConstants.DEVICE_TYPE+File.separator,":");
        ownerAndId = ownerAndId.replace(File.separator+"digital_display_publisher","");

        String owner = ownerAndId.split(":")[0];
        String deviceId = ownerAndId.split(":")[1];

        String [] messageData = message.toString().split(":");


        log.info("Received MQTT message for: {OWNER-" + owner + "} & {DEVICE.ID-" + deviceId + "}");

        if(messageData.length == 3){
                String randomId = messageData[0];
                String requestMessage = messageData[1];
                String result = messageData[2];
                log.info("Return result " + result + " for Request " + requestMessage);
                DigitalDisplayWebSocketServerEndPoint.sendMessage(randomId, result);
        }

    }

    @Override
    public void processIncomingMessage() {

    }

    public void publishToDigitalDisplay(String topic, String payLoad, int qos, boolean retained) throws CommunicationHandlerException {
            log.info(topic + " " + payLoad);
            publishToQueue(topic, payLoad, qos, retained);
    }

    @Override
    public void disconnect() {
        Runnable stopConnection = new Runnable() {
            public void run() {
                while (isConnected()) {
                    try {
                        dataPushServiceHandler.cancel(true);
                        closeConnection();

                    } catch (MqttException e) {
                        if (log.isDebugEnabled()) {
                            log.warn("Unable to 'STOP' MQTT connection at broker at: " +
                                    mqttBrokerEndPoint);
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

}
