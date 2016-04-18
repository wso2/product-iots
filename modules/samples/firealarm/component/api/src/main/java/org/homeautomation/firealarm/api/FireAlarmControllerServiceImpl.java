/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.homeautomation.firealarm.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.homeautomation.firealarm.api.dto.DeviceJSON;
import org.homeautomation.firealarm.api.exception.DeviceTypeException;
import org.homeautomation.firealarm.api.transport.MQTTConnector;
import org.homeautomation.firealarm.plugin.constants.DeviceTypeConstants;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorRecord;
import org.wso2.carbon.device.mgt.iot.service.IoTServerStartupListener;
import javax.ws.rs.core.Response;

public class FireAlarmControllerServiceImpl implements FireAlarmControllerService {

    private static Log log = LogFactory.getLog(FireAlarmControllerServiceImpl.class);
    private MQTTConnector mqttConnector;

    private boolean waitForServerStartup() {
        while (!IoTServerStartupListener.isServerReady()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    public MQTTConnector getMQTTConnector() {
        return mqttConnector;
    }

    @SuppressWarnings("unused")
    public void setMQTTConnector(final MQTTConnector MQTTConnector) {
        Runnable connector = new Runnable() {
            public void run() {
                if (waitForServerStartup()) {
                    return;
                }
                FireAlarmControllerServiceImpl.this.mqttConnector = MQTTConnector;
                if (MqttConfig.getInstance().isEnabled()) {
                    mqttConnector.connect();
                } else {
                    log.warn("MQTT disabled in 'devicemgt-config.xml'. Hence, MQTTConnector" +
                             " not started.");
                }
            }
        };
        Thread connectorThread = new Thread(connector);
        connectorThread.setDaemon(true);
        connectorThread.start();
    }

    public Response registerDevice(final DeviceJSON agentInfo) {
        if ((agentInfo.deviceId != null) && (agentInfo.owner != null)) {
            return Response.status(Response.Status.OK).entity("Device has been registered successfully").build();
        }
        return Response.status(Response.Status.NOT_ACCEPTABLE).entity("Message body not " +
                                                                      "well-formed and still invalid").build();
    }

    public Response readTemperature(String owner, String deviceId, String protocol) {
        SensorRecord sensorRecord = null;
        try {
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
                                                                           DeviceTypeConstants.SENSOR_TEMPERATURE);
        } catch (DeviceControllerException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
        return Response.ok().entity(sensorRecord).build();
    }

    public Response readHumidity(String owner, String deviceId, String protocol) {
        SensorRecord sensorRecord = null;
        try {
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
                                                                           DeviceTypeConstants.SENSOR_HUMIDITY);
        } catch (DeviceControllerException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
        return Response.ok().entity(sensorRecord).build();
    }

    public Response changeBuzzerState(String owner, String deviceId, String protocol, String state) {
        try {
            mqttConnector.sendCommandViaMQTT(owner, deviceId, "buzzer:", state.toUpperCase());
            return Response.ok().build();
        } catch (DeviceManagementException e) {
            log.error(e);
            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
        } catch (DeviceTypeException e) {
            log.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

}