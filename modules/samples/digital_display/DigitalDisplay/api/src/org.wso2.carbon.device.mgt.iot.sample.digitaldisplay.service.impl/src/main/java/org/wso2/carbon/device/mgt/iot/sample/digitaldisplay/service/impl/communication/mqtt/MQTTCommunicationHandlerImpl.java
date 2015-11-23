package org.wso2.carbon.device.mgt.iot.sample.digitaldisplay.service.impl.communication.mqtt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.wso2.carbon.device.mgt.iot.sample.digitaldisplay.service.impl.communication.CommunicationHandlerException;

import java.util.concurrent.ScheduledFuture;

public class MQTTCommunicationHandlerImpl extends MQTTCommunicationHandler {

    private static final Log log = LogFactory.getLog(MQTTCommunicationHandlerImpl.class);

    private ScheduledFuture<?> dataPushServiceHandler;

    private static MQTTCommunicationHandlerImpl mqttCommunicationHandler;

    private MQTTCommunicationHandlerImpl(String deviceOwner, String deviceType,
                                           String mqttBrokerEndPoint, String subscribeTopic) {
        super(deviceOwner, deviceType, mqttBrokerEndPoint, subscribeTopic);
        connect();

    }

    private MQTTCommunicationHandlerImpl(String deviceOwner, String deviceType,
                                        String mqttBrokerEndPoint, String subscribeTopic,int intervalInMills){
        super( deviceOwner, deviceType, mqttBrokerEndPoint, subscribeTopic, intervalInMills);
        connect();
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
    public void processIncomingMessage(MqttMessage message) {

        String messageState = message.toString().split(":")[0];
        String messageContent = message.toString().split(":")[1];

        log.info("State : " + messageState);
        log.info("Content : " + messageContent);

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

    public static MQTTCommunicationHandlerImpl getInstance(String deviceOwner, String deviceType,
                                                           String mqttBrokerEndPoint, String subscribeTopic){

        if(mqttCommunicationHandler == null){
            mqttCommunicationHandler = new MQTTCommunicationHandlerImpl(deviceOwner, deviceType, mqttBrokerEndPoint, subscribeTopic);
        }
        return MQTTCommunicationHandlerImpl.mqttCommunicationHandler;
    }

}
