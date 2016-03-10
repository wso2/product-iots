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

package org.homeautomation.firealarm.controller.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.homeautomation.firealarm.controller.api.dto.DeviceJSON;
import org.homeautomation.firealarm.controller.api.exception.DeviceTypeException;
import org.homeautomation.firealarm.controller.api.transport.MQTTConnector;
import org.homeautomation.firealarm.plugin.constants.DeviceTypeConstants;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.device.DeviceType;
import org.wso2.carbon.apimgt.annotations.device.feature.Feature;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.DeviceManagement;
import org.wso2.carbon.device.mgt.iot.DeviceValidator;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorRecord;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This is the controller API which is used to control agent side functionality
 */
@SuppressWarnings("NonJaxWsWebServices")
@API(name = "firealarm", version = "1.0.0", context = "/firealarm")
@DeviceType(value = "firealarm")
public class ControllerService {

    private static Log log = LogFactory.getLog(ControllerService.class);
    private MQTTConnector mqttConnector;

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
                ControllerService.this.mqttConnector = MQTTConnector;
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

    /**
     * @param agentInfo device owner,id and sensor value
     * @return device registration status
     */
    @Path("controller/register")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerDevice(final DeviceJSON agentInfo) {
        if ((agentInfo.deviceId != null) && (agentInfo.owner != null)) {
            return Response.status(Response.Status.OK).entity("Device has been registered successfully").build();
        }
        return Response.status(Response.Status.NOT_ACCEPTABLE).entity("Message body not " +
                                                                      "well-formed and still invalid").build();
    }

    /**
     * @param owner    device owner
     * @param deviceId unique identifier for given device type
     * @param protocol name of supported protocol. here MQTT is used
     * @param response to request
     * @return sensor record
     */
    @Path("controller/read-temperature")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Feature(code = "read-temperature", name = "Temperature", type = "monitor",
            description = "Request temperature reading from device")
    public SensorRecord readTemperature(@HeaderParam("owner") String owner,
                                        @HeaderParam("deviceId") String deviceId,
                                        @HeaderParam("protocol") String protocol,
                                        @Context HttpServletResponse response) {
        SensorRecord sensorRecord = null;
        if (isPermitted(owner, deviceId, response)) {
            try {
                sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
                                                                               DeviceTypeConstants.SENSOR_TEMPERATURE);
            } catch (DeviceControllerException e) {
                response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            }
            response.setStatus(Response.Status.OK.getStatusCode());
        }
        return sensorRecord;
    }

    /**
     * @param owner    device owner
     * @param deviceId unique identifier for given device type
     * @param protocol name of supported protocol. here MQTT is used
     * @param response to request
     * @return sensor record
     */
    @Path("controller/read-humidity")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Feature(code = "read-humidity", name = "Humidity", type = "monitor",
            description = "Request humidity reading from device")
    public SensorRecord readHumidity(@HeaderParam("owner") String owner,
                                     @HeaderParam("deviceId") String deviceId,
                                     @HeaderParam("protocol") String protocol,
                                     @Context HttpServletResponse response) {
        SensorRecord sensorRecord = null;
        if (isPermitted(owner, deviceId, response)) {
            try {
                sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
                                                                               DeviceTypeConstants.SENSOR_HUMIDITY);
            } catch (DeviceControllerException e) {
                response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            }
            response.setStatus(Response.Status.OK.getStatusCode());
        }
        return sensorRecord;
    }

    /**
     * @param owner    device owner
     * @param deviceId unique identifier for given device type
     * @param protocol name of supported protocol. Here MQTT is used
     * @param state    change status of buzzer: on/off
     * @param response to request
     */
    @Path("controller/change-status")
    @POST
    @Feature(code = "change-status", name = "Buzzer: on/off", type = "operation",
            description = "Switch on/off Fire Alarm Buzzer. (On / Off)")
    public void changeBuzzerState(@HeaderParam("owner") String owner,
                                  @HeaderParam("deviceId") String deviceId,
                                  @HeaderParam("protocol") String protocol,
                                  @FormParam("state") String state,
                                  @Context HttpServletResponse response) {
        if (isPermitted(owner, deviceId, response)) {
            try {
                mqttConnector.sendCommandViaMQTT(owner, deviceId, "buzzer:", state.toUpperCase());
                response.setStatus(Response.Status.OK.getStatusCode());
            } catch (DeviceManagementException e) {
                log.error(e);
                response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
            } catch (DeviceTypeException e) {
                log.error(e);
                response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            }
        }
    }

    private boolean isPermitted(String owner, String deviceId, HttpServletResponse response) {
        DeviceValidator deviceValidator = new DeviceValidator();
        try {
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            if (!deviceValidator.isExist(owner, tenantDomain, new DeviceIdentifier(
                    deviceId, DeviceTypeConstants.DEVICE_TYPE))) {
                response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
            } else {
                return true;
            }
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
        return false;
    }
}