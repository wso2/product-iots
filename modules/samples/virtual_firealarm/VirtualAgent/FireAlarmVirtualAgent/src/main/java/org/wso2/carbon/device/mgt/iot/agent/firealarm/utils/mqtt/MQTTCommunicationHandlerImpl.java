package org.wso2.carbon.device.mgt.iot.agent.firealarm.utils.mqtt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.communication.CommunicationHandlerException;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.communication.mqtt.MQTTCommunicationHandler;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.core.AgentConstants;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.core.AgentManager;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MQTTCommunicationHandlerImpl extends MQTTCommunicationHandler {

    private static final Log log = LogFactory.getLog(MQTTCommunicationHandlerImpl.class);

    private static final AgentManager agentManager = AgentManager.getInstance();
    private ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
    private ScheduledFuture<?> dataPushServiceHandler;

    public MQTTCommunicationHandlerImpl(String deviceOwner, String deviceType,
                                        String mqttBrokerEndPoint, String subscribeTopic) {
        super(deviceOwner, deviceType, mqttBrokerEndPoint, subscribeTopic);
    }

    public MQTTCommunicationHandlerImpl(String deviceOwner, String deviceType,
                                        String mqttBrokerEndPoint, String subscribeTopic,
                                        int intervalInMillis) {
        super(deviceOwner, deviceType, mqttBrokerEndPoint, subscribeTopic, intervalInMillis);
    }

    public ScheduledFuture<?> getDataPushServiceHandler() {
        return dataPushServiceHandler;
    }

    @Override
    public void connect() {
        Runnable connector = new Runnable() {
            public void run() {
                while (!isConnected()) {
                    try {
                        connectToQueue();
                        subscribeToQueue();
                        agentManager.updateAgentStatus("Connected to MQTT Queue");
                        publishDeviceData(agentManager.getPushInterval());

                    } catch (CommunicationHandlerException e) {
                        log.warn(AgentConstants.LOG_APPENDER +
                                         "Connection/Subscription to MQTT Broker at: " +
                                         mqttBrokerEndPoint + " failed");

                        try {
                            Thread.sleep(timeoutInterval);
                        } catch (InterruptedException ex) {
                            log.error(AgentConstants.LOG_APPENDER +
                                              "MQTT-Subscriber: Thread Sleep Interrupt Exception");
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
    public void processIncomingMessage(MqttMessage message) {
        log.info(AgentConstants.LOG_APPENDER + "Message " + message.toString() + " was received");

        String deviceOwner = agentManager.getAgentConfigs().getDeviceOwner();
        String deviceID = agentManager.getAgentConfigs().getDeviceId();
        String replyMessage;

        String[] controlSignal = message.toString().split(":");
//		log.info("########## Incoming Message : " + controlSignal[0]);
        // message- "<SIGNAL_TYPE>:<SIGNAL_MODE>" format.(ex: "BULB:ON", "TEMPERATURE", "HUMIDITY")



            switch (controlSignal[0].toUpperCase()) {
                case AgentConstants.BULB_CONTROL:
                    boolean stateToSwitch = controlSignal[1].equals(AgentConstants.CONTROL_ON);

                    agentManager.changeAlarmStatus(stateToSwitch);
                    log.info(AgentConstants.LOG_APPENDER + "Bulb was switched to state: '" +
                                     controlSignal[1] + "'");
                    break;

                case AgentConstants.TEMPERATURE_CONTROL:
                    int currentTemperature = agentManager.getTemperature();

                    String replyTemperature =
                            "Current temperature was read as: '" + currentTemperature + "C'";
                    log.info(AgentConstants.LOG_APPENDER + replyTemperature);

                    String tempPublishTopic = String.format(
                            AgentConstants.MQTT_PUBLISH_TOPIC, deviceOwner, deviceID);
                    replyMessage = AgentConstants.TEMPERATURE_CONTROL + ":" + currentTemperature;

                    try {
                        publishToQueue(tempPublishTopic, replyMessage);
                    } catch (CommunicationHandlerException e) {
                        log.error(AgentConstants.LOG_APPENDER +
                                          "MQTT - Publishing, reply message to the MQTT Queue " +
                                          "at:" +
                                          " " +
                                          agentManager.getAgentConfigs().getMqttBrokerEndpoint() +
                                          "failed");
                    }
                    break;

                case AgentConstants.HUMIDITY_CONTROL:
                    int currentHumidity = agentManager.getHumidity();

                    String replyHumidity =
                            "Current humidity was read as: '" + currentHumidity + "%'";
                    log.info(AgentConstants.LOG_APPENDER + replyHumidity);

                    String humidPublishTopic = String.format(
                            AgentConstants.MQTT_PUBLISH_TOPIC, deviceOwner, deviceID);
                    replyMessage = AgentConstants.HUMIDITY_CONTROL + ":" + currentHumidity;

                    try {
                        publishToQueue(humidPublishTopic, replyMessage);
                    } catch (CommunicationHandlerException e) {
                        log.error(AgentConstants.LOG_APPENDER +
                                          "MQTT - Publishing, reply message to the MQTT Queue " +
                                          "at:" +
                                          " " +
                                          agentManager.getAgentConfigs().getMqttBrokerEndpoint() +
                                          "failed");
                    }
                    break;

                default:
                    log.warn(AgentConstants.LOG_APPENDER + "'" + controlSignal[0] +
                                     "' is invalid and not-supported for " + "this device-type");
                    break;
            }

    }


    @Override
    public void publishDeviceData(int publishInterval) {
        Runnable pushDataRunnable = new Runnable() {
            @Override
            public void run() {
                int currentTemperature = agentManager.getTemperature();
                String payLoad =
                        "PUBLISHER:" + AgentConstants.TEMPERATURE_CONTROL + ":" +
                                currentTemperature;

                MqttMessage pushMessage = new MqttMessage();
                pushMessage.setPayload(payLoad.getBytes(StandardCharsets.UTF_8));
                pushMessage.setQos(DEFAULT_MQTT_QUALITY_OF_SERVICE);
                pushMessage.setRetained(true);

                String topic = String.format(AgentConstants.MQTT_PUBLISH_TOPIC,
                                             agentManager.getAgentConfigs().getDeviceOwner(),
                                             agentManager.getAgentConfigs().getDeviceId());

                try {
                    publishToQueue(topic, pushMessage);
                    log.info(AgentConstants.LOG_APPENDER + "Message: '" + pushMessage +
                                     "' published to MQTT Queue at [" +
                                     agentManager.getAgentConfigs().getMqttBrokerEndpoint() +
                                     "] under topic [" + topic + "]");

                } catch (CommunicationHandlerException e) {
                    log.warn(AgentConstants.LOG_APPENDER + "Data Publish attempt to topic - [" +
                                     AgentConstants.MQTT_PUBLISH_TOPIC + "] failed for payload [" +
                                     payLoad + "]");
                }
            }
        };

        dataPushServiceHandler = service.scheduleAtFixedRate(pushDataRunnable, publishInterval,
                                                             publishInterval, TimeUnit.SECONDS);
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
                            log.warn(AgentConstants.LOG_APPENDER +
                                             "Unable to 'STOP' MQTT connection at broker at: " +
                                             mqttBrokerEndPoint);
                        }

                        try {
                            Thread.sleep(timeoutInterval);
                        } catch (InterruptedException e1) {
                            log.error(AgentConstants.LOG_APPENDER +
                                              "MQTT-Terminator: Thread Sleep Interrupt Exception");
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
    public void processIncomingMessage() {

    }

}
