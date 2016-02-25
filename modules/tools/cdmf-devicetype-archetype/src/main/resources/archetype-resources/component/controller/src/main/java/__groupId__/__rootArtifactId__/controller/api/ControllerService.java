package ${groupId}.${rootArtifactId}.controller.api;
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
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ${groupId}.${rootArtifactId}.controller.api.dto.DeviceJSON;
import ${groupId}.${rootArtifactId}.controller.api.util.ServiceUtils;
import ${groupId}.${rootArtifactId}.plugin.constants.DeviceTypeConstants;
import ${groupId}.${rootArtifactId}.controller.api.exception.DeviceTypeException;
import ${groupId}.${rootArtifactId}.controller.api.transport.MQTTConnector;

import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.device.DeviceType;
import org.wso2.carbon.apimgt.annotations.device.feature.Feature;
import org.wso2.carbon.device.mgt.iot.DeviceManagement;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorRecord;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This is the controller API which is used to control agent side functionality
 */
@API(name = "${deviceType}", version = "1.0.0", context = "/${deviceType}")
@DeviceType(value = "${deviceType}")
public class ControllerService {
    private MQTTConnector mqttConnector;
    private ConcurrentHashMap<String, DeviceJSON> deviceToIpMap = new ConcurrentHashMap<>();
    private static Log log = LogFactory.getLog(ControllerService.class);

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

    public MQTTConnector getMQTTConnector() {
        return mqttConnector;
    }

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
     * @return
     */
    @Path("controller/register")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerDevice(final DeviceJSON agentInfo) {
        String deviceId = agentInfo.deviceId;
        if ((agentInfo.deviceId != null) && (agentInfo.owner != null)) {
            deviceToIpMap.put(deviceId, agentInfo);
            return Response.status(Response.Status.OK).entity("Device has been registered successfully").build();
        }
        return Response.status(Response.Status.NOT_ACCEPTABLE).entity("Message body not " +
                "well-formed and still invalid").build();
    }

    /**
     * @param owner device owner
     * @param deviceId unique identifier for given device type
     * @param protocol name of supported protocol. here MQTT is used
     * @param response
     * @return
     */
    @Path("controller/read-current-status")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Feature(code = "read-current-status", name = "Sensor", type = "monitor",
            description = "Request current status of sensor from device")
    public SensorRecord readCurrentStatus(@HeaderParam("owner") String owner,
                                       @HeaderParam("deviceId") String deviceId,
                                       @HeaderParam("protocol") String protocol,
                                       @Context HttpServletResponse response) {
        SensorRecord sensorRecord = null;
        try {
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
                    DeviceTypeConstants.SENSOR_READING);
        } catch (DeviceControllerException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
        response.setStatus(Response.Status.OK.getStatusCode());
        return sensorRecord;
    }

    /**
     * @param agentInfo receive current status of sensor and device metadata
     * @param response
     */
    @Path("controller/push-sensor-value")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void pushData(final DeviceJSON agentInfo, @Context HttpServletResponse response) {
        if (!ServiceUtils.publishToDASSensorValue(agentInfo.owner, agentInfo.deviceId, agentInfo.sensorValue)) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.warn("An error occurred whilst trying to publish pin data of go Data with ID [" + agentInfo.deviceId +
                    "] of owner [" + agentInfo.owner + "]");
        }
    }

    /**
     * @param owner device owner
     * @param deviceId unique identifier for given device type
     * @param protocol name of supported protocol. Here MQTT is used
     * @param state change status of sensor: on/off
     * @param response
     */
    @Path("controller/change-status")
    @POST
    @Feature(code = "change-status", name = "Change status of sensor: on/off", type = "operation",
            description = "Change status of sensor: on/off")
    public void changeLockerState(@HeaderParam("owner") String owner, @HeaderParam("deviceId") String deviceId,
                                  @HeaderParam("protocol") String protocol, @FormParam("state") String state,
                                  @Context HttpServletResponse response) {
        try {
            mqttConnector.sendCommandViaMQTT(owner, deviceId, "Sensor:", state.toUpperCase());
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