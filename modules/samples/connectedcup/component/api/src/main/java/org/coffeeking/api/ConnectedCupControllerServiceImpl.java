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

package org.coffeeking.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.coffeeking.connectedcup.plugin.constants.ConnectedCupConstants;
import org.coffeeking.api.transport.ConnectedCupMQTTConnector;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorRecord;
import org.wso2.carbon.device.mgt.iot.service.IoTServerStartupListener;
import org.wso2.carbon.device.mgt.iot.transport.TransportHandlerException;

import javax.ws.rs.core.Response;

public class ConnectedCupControllerServiceImpl implements ConnectedCupControllerService {

    private static Log log = LogFactory.getLog(ConnectedCupControllerServiceImpl.class);
    private static ConnectedCupMQTTConnector connectedCupMQTTConnector;

    public ConnectedCupMQTTConnector getConnectedCupMQTTConnector() {
        return ConnectedCupControllerServiceImpl.connectedCupMQTTConnector;
    }

    public void setConnectedCupMQTTConnector(
            final ConnectedCupMQTTConnector connectedCupMQTTConnector) {

        Runnable connector = new Runnable() {
            public void run() {
                if (waitForServerStartup()) {
                    return;
                }
                ConnectedCupControllerServiceImpl.connectedCupMQTTConnector = connectedCupMQTTConnector;
                if (MqttConfig.getInstance().isEnabled()) {
                    connectedCupMQTTConnector.connect();
                } else {
                    log.warn("MQTT disabled in 'devicemgt-config.xml'. " +
                             "Hence, ConnectedCupMQTTConnector not started.");
                }
            }
        };
        Thread connectorThread = new Thread(connector);
        connectorThread.setDaemon(true);
        connectorThread.start();
    }

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

    public Response readCoffeeLevel(String owner, String deviceId) {
        if (log.isDebugEnabled()) {
            log.debug("Sending request to read liquid level value of device [" + deviceId + "] via MQTT");
        }

        try {
            String mqttResource = ConnectedCupConstants.LEVEL_CONTEXT.replace("/", "");
            connectedCupMQTTConnector.publishDeviceData(owner, deviceId, mqttResource, "");

            SensorRecord sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
                                                                           ConnectedCupConstants.SENSOR_LEVEL);
            return Response.ok().entity(sensorRecord).build();
        } catch (DeviceControllerException | TransportHandlerException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    public Response readTemperature(String owner, String deviceId) {

        if (log.isDebugEnabled()) {
            log.debug("Sending request to read connected cup temperature of device " + "[" + deviceId + "] via MQTT");
        }
        try {
            String mqttResource = ConnectedCupConstants.TEMPERATURE_CONTEXT.replace("/", "");
            connectedCupMQTTConnector.publishDeviceData(owner, deviceId, mqttResource, "");

            SensorRecord sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
                                                                           ConnectedCupConstants.SENSOR_TEMPERATURE);
            return Response.ok().entity(sensorRecord).build();
        } catch (DeviceControllerException | TransportHandlerException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    public Response orderCoffee(String deviceId, String deviceOwner) {
        log.info("Coffee ordered....!");

        if (log.isDebugEnabled()) {
            log.debug("Sending request to read liquid level value of device [" + deviceId + "] via MQTT");
        }
        return Response.ok().entity("Coffee ordered.").build();
    }
}