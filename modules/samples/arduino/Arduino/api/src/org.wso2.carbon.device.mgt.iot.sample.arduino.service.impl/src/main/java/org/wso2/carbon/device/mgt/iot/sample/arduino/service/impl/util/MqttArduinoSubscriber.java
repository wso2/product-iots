/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.device.mgt.iot.sample.arduino.service.impl.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.wso2.carbon.device.mgt.iot.sample.arduino.service.impl.ArduinoControllerService;
import org.wso2.carbon.device.mgt.iot.sample.arduino.plugin.constants.ArduinoConstants;
import org.wso2.carbon.device.mgt.iot.common.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.common.controlqueue.mqtt.MqttSubscriber;

import java.io.File;
import java.util.LinkedList;
import java.util.UUID;

public class MqttArduinoSubscriber extends MqttSubscriber {

    private static Log log = LogFactory.getLog(MqttArduinoSubscriber.class);
    private static final String iotServerSubscriber = UUID.randomUUID().toString().substring(0,5);
    private static final String subscribetopic =
            "wso2" + File.separator + "iot" + File.separator + "+" + File.separator +
                    ArduinoConstants.DEVICE_TYPE + File.separator + "#";


    //make it singleton
    private MqttArduinoSubscriber() {
        super(iotServerSubscriber, ArduinoConstants.DEVICE_TYPE, MqttConfig.getInstance().getMqttQueueEndpoint(),
                subscribetopic);
    }

    @Override
    protected void postMessageArrived(final String topic, final MqttMessage message) {
        int lastIndex = topic.lastIndexOf("/");
        String deviceId = topic.substring(lastIndex + 1);

        lastIndex = message.toString().lastIndexOf(":");
        String msgContext = message.toString().substring(lastIndex + 1);

        LinkedList<String> deviceControlList = null;
        LinkedList<String> replyMessageList = null;

        if (msgContext.equals("IN") || msgContext.equals(ArduinoConstants.STATE_ON) || msgContext
                .equals(ArduinoConstants.STATE_OFF)) {
            log.info("Recieved a control message: ");
            log.info("Control message topic: " + topic);
            log.info("Control message: " + message.toString());
            synchronized (ArduinoControllerService.getInternalControlsQueue()) {
                deviceControlList = ArduinoControllerService.getInternalControlsQueue().get(deviceId);
                if (deviceControlList == null) {
                    ArduinoControllerService.getInternalControlsQueue()
                            .put(deviceId, deviceControlList = new LinkedList<String>());
                }
            }
            deviceControlList.add(message.toString());
        } else if (msgContext.equals("OUT")) {
            log.info("Recieved reply from a device: ");
            log.info("Reply message topic: " + topic);
            log.info("Reply message: " + message.toString().substring(0, lastIndex));
            synchronized (ArduinoControllerService.getReplyMsgQueue()) {
                replyMessageList = ArduinoControllerService.getReplyMsgQueue().get(deviceId);
                if (replyMessageList == null) {
                    ArduinoControllerService.getReplyMsgQueue()
                            .put(deviceId, replyMessageList = new LinkedList<String>());
                }
            }
            replyMessageList.add(message.toString());
        }

    }
}
