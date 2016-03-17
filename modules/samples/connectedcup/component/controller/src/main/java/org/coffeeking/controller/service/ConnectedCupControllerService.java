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

package org.coffeeking.controller.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.coffeeking.connectedcup.plugin.constants.ConnectedCupConstants;
import org.coffeeking.controller.service.dto.DeviceJSON;
import org.coffeeking.controller.service.transport.ConnectedCupMQTTConnector;
import org.coffeeking.controller.service.util.ConnectedCupServiceUtils;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.device.DeviceType;
import org.wso2.carbon.apimgt.annotations.device.feature.Feature;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.DeviceValidator;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorRecord;
import org.wso2.carbon.device.mgt.iot.transport.TransportHandlerException;
import org.wso2.carbon.device.mgt.iot.DeviceManagement;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Calendar;

@API(name = "connectedcup", version = "1.0.0", context = "/connectedcup")
@DeviceType(value = "connectedcup")
public class ConnectedCupControllerService {

    private static Log log = LogFactory.getLog(ConnectedCupControllerService.class);
    private static final String SUPER_TENANT = "carbon.super";
    private static ConnectedCupMQTTConnector connectedCupMQTTConnector;

    public ConnectedCupMQTTConnector connectedCupMQTTConnector() {
        return ConnectedCupControllerService.connectedCupMQTTConnector;
    }

    public void setconnectedCupMQTTConnector(final ConnectedCupMQTTConnector connectedCupMQTTConnector) {
        Runnable connector = new Runnable() {
            public void run() {
                if (waitForServerStartup()) {
                    return;
                }
                ConnectedCupControllerService.connectedCupMQTTConnector = connectedCupMQTTConnector;
                if (MqttConfig.getInstance().isEnabled()) {
                    connectedCupMQTTConnector.connect();
                } else {
                    log.warn("MQTT disabled in 'devicemgt-config.xml'. Hence, ConnectedCupMQTTConnector not started.");
                }
            }
        };
        Thread connectorThread = new Thread(connector);
        connectorThread.setDaemon(true);
        connectorThread.start();
    }

    private boolean waitForServerStartup() {
        while (!DeviceManagement.isServerReady()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param deviceId
     * @param owner
     */
    @Path("controller/coffeelevel")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Feature(code = "coffeelevel", name = "Coffee Level", type = "monitor",
            description = "Request Coffee Level from Connected cup")
    public SensorRecord readCoffeeLevel(@HeaderParam("owner") String owner,
                                        @HeaderParam("deviceId") String deviceId,
                                        @Context HttpServletResponse response) {
        SensorRecord sensorRecord = null;
        DeviceValidator deviceValidator = new DeviceValidator();
        try {
            if (!deviceValidator.isExist(owner, SUPER_TENANT, new DeviceIdentifier(
                    deviceId, ConnectedCupConstants.DEVICE_TYPE))) {
                response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
            }
            if (log.isDebugEnabled()) {
                log.debug("Sending request to read liquid level value of device [" + deviceId + "] via MQTT");
            }
            String mqttResource = ConnectedCupConstants.LEVEL_CONTEXT.replace("/", "");
            connectedCupMQTTConnector.publishDeviceData(owner, deviceId, mqttResource, "");
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId, ConnectedCupConstants.SENSOR_LEVEL);
        } catch (DeviceControllerException | TransportHandlerException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        response.setStatus(Response.Status.OK.getStatusCode());
        return sensorRecord;
    }

    @Path("controller/temperature")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Feature(code = "temperature", name = "Temperature", type = "monitor",
            description = "Request Temperature reading from Connected cup")
    public SensorRecord readTemperature(@HeaderParam("owner") String owner,
                                        @HeaderParam("deviceId") String deviceId,
                                        @Context HttpServletResponse response) {
        SensorRecord sensorRecord = null;
        DeviceValidator deviceValidator = new DeviceValidator();
        try {
            if (!deviceValidator.isExist(owner, SUPER_TENANT,
                                         new DeviceIdentifier(deviceId, ConnectedCupConstants.DEVICE_TYPE))) {
                response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
            }
            if (log.isDebugEnabled()) {
                log.debug("Sending request to read connected cup temperature of device [" + deviceId + "] via MQTT");
            }
            String mqttResource = ConnectedCupConstants.TEMPERATURE_CONTEXT.replace("/", "");
            connectedCupMQTTConnector.publishDeviceData(owner, deviceId, mqttResource, "");
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
                                                                           ConnectedCupConstants.SENSOR_TEMPERATURE);
        } catch (DeviceControllerException | TransportHandlerException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        response.setStatus(Response.Status.OK.getStatusCode());
        return sensorRecord;
    }

    @Path("controller/ordercoffee")
    @POST
    public void orderCoffee(@QueryParam("deviceId") String deviceId, @QueryParam("deviceOwner") String deviceOwner,
                            @Context HttpServletResponse response) {
        DeviceValidator deviceValidator = new DeviceValidator();
        try {
            if (!deviceValidator.isExist(deviceOwner, SUPER_TENANT, new DeviceIdentifier(
                    deviceId, ConnectedCupConstants.DEVICE_TYPE))) {
                response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
            }
            response.setStatus(Response.Status.ACCEPTED.getStatusCode());
            log.info("Coffee ordered....!");
            if (log.isDebugEnabled()) {
                log.debug("Sending request to read liquid level value of device [" + deviceId + "] via MQTT");
            }
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

}
