/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.iot.integration.common;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;

/**
 * This is a mqtt subscriber client to receive .
 */
public class MqttSubscriberClient implements MqttCallback {
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
        connectOptions.setKeepAliveInterval(300);
        client.connect(connectOptions);
        client.subscribe(topicName);
    }

    public ArrayList<MqttMessage> getMqttMessages() {
        return mqttMessages;
    }

}
