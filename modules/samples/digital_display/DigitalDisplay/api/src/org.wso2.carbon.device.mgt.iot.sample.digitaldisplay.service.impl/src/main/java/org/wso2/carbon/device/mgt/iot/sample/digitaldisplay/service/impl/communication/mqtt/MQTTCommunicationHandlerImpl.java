package org.wso2.carbon.device.mgt.iot.sample.digitaldisplay.service.impl.communication.mqtt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.wso2.carbon.device.mgt.iot.sample.digitaldisplay.service.impl.communication.CommunicationHandlerException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public class MQTTCommunicationHandlerImpl extends MQTTCommunicationHandler {

    private static final Log log = LogFactory.getLog(MQTTCommunicationHandlerImpl.class);

    private ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
    private ScheduledFuture<?> dataPushServiceHandler;

    public MQTTCommunicationHandlerImpl(String deviceOwner, String deviceType,
                                           String mqttBrokerEndPoint, String subscribeTopic) {
        super(deviceOwner, deviceType, mqttBrokerEndPoint, subscribeTopic);

    }

    public MQTTCommunicationHandlerImpl(String deviceOwner, String deviceType,
                                        String mqttBrokerEndPoint, String subscribeTopic,int intervalInMills){
        super( deviceOwner, deviceType, mqttBrokerEndPoint, subscribeTopic, intervalInMills);

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

            }
        };

        Thread connectorThread = new Thread(connect);
        connectorThread.setDaemon(true);
        connectorThread.start();

    }

    @Override
    public void processIncomingMessage(MqttMessage message) {

        log.info("New Message Received : " + message);


        //Add Code what we want to do

        try {
            publishToQueue("wso2/iot/Nuwan/digital_display/2000/digital_display_subscriber","Sucess");
        } catch (CommunicationHandlerException e) {
            log.error(e);
        }

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
