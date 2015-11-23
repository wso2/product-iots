package org.wso2.carbon.device.mgt.iot.sample.digitaldisplay.service.impl.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.common.DeviceController;
import org.wso2.carbon.device.mgt.iot.common.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.common.controlqueue.mqtt.MqttSubscriber;
import org.wso2.carbon.device.mgt.iot.common.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.sample.digitaldisplay.plugin.constants.DigitalDisplayConstants;

import java.io.File;
import java.util.UUID;

/**
 * Created by nuwan on 11/16/15.
 */

public class MqttDigitalDisplaySubscriber extends MqttSubscriber{

    private static Log log = LogFactory.getLog(MqttDigitalDisplaySubscriber.class);

    private static final String subscribeTopic =
            "wso2"+ File.separator+"iot"+File.separator+"+"+File.separator+
                    DigitalDisplayConstants.DEVICE_TYPE+File.separator+"+"+File.separator+
                    "digital_display_publisher";

    private static String iotServerSubscriber = UUID.randomUUID().toString().substring(0,5);

    private MqttDigitalDisplaySubscriber() {
        super(iotServerSubscriber, DigitalDisplayConstants.DEVICE_TYPE,
                MqttConfig.getInstance().getMqttQueueEndpoint(), subscribeTopic);
    }

    @Override
    protected void postMessageArrived(String topic, MqttMessage mqttMessage) {

        String ownerAndId = topic.replace("wso2"+File.separator+"iot"+File.separator,"");
        ownerAndId = ownerAndId.replace(File.separator+ DigitalDisplayConstants.DEVICE_TYPE+File.separator,":");
        ownerAndId = ownerAndId.replace(File.separator+"digital_display_publisher","");

        String owner = ownerAndId.split(":")[0];
        String deviceId = ownerAndId.split(":")[1];

        String sessionId = mqttMessage.toString().split(":")[0];

        log.info("Received MQTT message for: {OWNER-" + owner + "} & {DEVICE.ID-" + deviceId + "}");

        DigitalDisplayWebSocketServerEndPoint.sendMessage(sessionId,"Sucess..");

        /*try {
            boolean result = sendCommandViaMQTT(owner,deviceId,"Hello","World");
            log.info("Result : " + result);

        } catch (DeviceManagementException e) {
            log.error(e);
        }*/

    }

    private boolean sendCommandViaMQTT(String deviceOwner, String deviceId, String resource,
                                       String state) throws DeviceManagementException {

        boolean result;
        DeviceController deviceController = new DeviceController();

        try {
            result = deviceController.publishMqttControl(deviceOwner,
                    DigitalDisplayConstants.DEVICE_TYPE,
                    deviceId, resource, state);
        } catch (DeviceControllerException e) {
            String errorMsg = "Error whilst trying to publish to MQTT Queue";
            log.error(errorMsg);

            throw new DeviceManagementException(errorMsg, e);
        }
        return result;
    }

}
