package org.wso2.iot.integration.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;

/**
 * This is a mqtt subscriber client to receive .
 */
public class MqttSubscriberClient implements MqttCallback {
    private static Log log = LogFactory.getLog(MqttSubscriberClient.class);
    private ArrayList<MqttMessage> mqttMessages;

    @Override
    public void connectionLost(Throwable throwable) {
        // Not implemented
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        mqttMessages.add(mqttMessage);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        // Not implemented
    }

    /**
     *
     * @param serverAddress Mqtt broker address
     * @param clientId Client ID
     * @param topicName Topic Name
     * @throws MqttException Mqtt Exception
     */
    public MqttSubscriberClient(String serverAddress, String clientId, String topicName, String accessToken) throws
            MqttException {
        mqttMessages = new ArrayList<>();
        MemoryPersistence persistence = new MemoryPersistence();
        MqttClient client = new MqttClient(serverAddress, clientId, persistence);
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        client.setCallback(this);
        connectOptions.setUserName(accessToken);
        connectOptions.setPassword("".toCharArray());
        connectOptions.setCleanSession(true);
        client.connect(connectOptions);
        client.subscribe(topicName);
    }

    public ArrayList<MqttMessage> getMqttMessages() {
        return mqttMessages;
    }

}
