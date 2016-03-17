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

package org.homeautomation.currentsensor.manager.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.homeautomation.currentsensor.manager.api.dto.DeviceJSON;
import org.homeautomation.currentsensor.manager.api.util.CurrentSensorServiceUtils;
import org.homeautomation.currentsensor.plugin.constants.CurrentSensorConstants;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.device.DeviceType;
import org.wso2.carbon.apimgt.annotations.device.feature.Feature;
import org.wso2.carbon.device.mgt.iot.DeviceManagement;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorRecord;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.DeviceValidator;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;

@API(name = "currentsensor", version = "1.0.0", context = "/currentsensor")
@DeviceType(value = "currentsensor")
public class CurrentSensorControllerService {

    private static Log log = LogFactory.getLog(CurrentSensorControllerService.class);
    private ConcurrentHashMap<String, String> deviceToIpMap = new ConcurrentHashMap<>();

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

    @Path("controller/register/{owner}/{deviceId}/{ip}/{port}")
    @POST
    public String registerDeviceIP(@PathParam("owner") String owner,
                                   @PathParam("deviceId") String deviceId,
                                   @PathParam("ip") String deviceIP,
                                   @PathParam("port") String devicePort,
                                   @Context HttpServletResponse response,
                                   @Context HttpServletRequest request) {
        //TODO:: Need to get IP from the request itself
        String result;
        if (log.isDebugEnabled()) {
            log.debug("Got register call from IP: " + deviceIP + " for Device ID: " + deviceId + " of owner: " + owner);
        }
        String deviceHttpEndpoint = deviceIP + ":" + devicePort;
        deviceToIpMap.put(deviceId, deviceHttpEndpoint);
        result = "Device-IP Registered";
        response.setStatus(Response.Status.OK.getStatusCode());
        if (log.isDebugEnabled()) {
            log.debug(result);
        }
        return result;
    }

    /**
     * @param owner
     * @param deviceId
     * @param protocol
     * @param response
     * @return
     */
    @Path("controller/read-current")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Feature(code = "read-current", name = "Current", type = "monitor",
            description = "Request current reading from Arduino agent")
    public SensorRecord requestCurrent(@HeaderParam("owner") String owner,
                                       @HeaderParam("deviceId") String deviceId,
                                       @HeaderParam("protocol") String protocol,
                                       @Context HttpServletResponse response) {
        SensorRecord sensorRecord = null;
        try {
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
                                                                           CurrentSensorConstants.SENSOR_CURRENT);
        } catch (DeviceControllerException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
        response.setStatus(Response.Status.OK.getStatusCode());
        return sensorRecord;
    }

    /**
     * @param owner
     * @param deviceId
     * @param protocol
     * @param response
     * @return
     */
    @Path("controller/read-power")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Feature(code = "read-power", name = "Power", type = "monitor",
            description = "Request power reading from Arduino agent")
    public SensorRecord requestPower(@HeaderParam("owner") String owner,
                                     @HeaderParam("deviceId") String deviceId,
                                     @HeaderParam("protocol") String protocol,
                                     @Context HttpServletResponse response) {
        SensorRecord sensorRecord = null;
        try {
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
                                                                           CurrentSensorConstants.SENSOR_POWER);
        } catch (DeviceControllerException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
        response.setStatus(Response.Status.OK.getStatusCode());
        return sensorRecord;
    }

    /**
     * @param owner
     * @param deviceId
     * @param protocol
     * @param response
     * @return
     */
    @Path("controller/read-flowrate")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Feature(code = "read-flowrate", name = "Flow Rate", type = "monitor",
            description = "Request flow rate reading from Arduino agent")
    public SensorRecord requestFlowRate(@HeaderParam("owner") String owner,
                                        @HeaderParam("deviceId") String deviceId,
                                        @HeaderParam("protocol") String protocol,
                                        @Context HttpServletResponse response) {
        SensorRecord sensorRecord = null;
        try {
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
                                                                           CurrentSensorConstants.SENSOR_FLOWRATE);
        } catch (DeviceControllerException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
        response.setStatus(Response.Status.OK.getStatusCode());
        return sensorRecord;
    }

    /**
     * @param dataMsg
     * @param response
     */
    @Path("controller/push-data")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void pushData(final DeviceJSON dataMsg, @Context HttpServletResponse response) {
        String owner = dataMsg.owner;
        String deviceId = dataMsg.deviceId;
        String deviceIp = dataMsg.reply;
        float current = dataMsg.current;
        float flow_rate = dataMsg.flow_rate;

        String registeredIp = deviceToIpMap.get(deviceId);
        if (registeredIp == null) {
            log.warn("Unregistered IP: Temperature Data Received from an un-registered IP " + deviceIp +
                     " for device ID - " + deviceId);
            response.setStatus(Response.Status.PRECONDITION_FAILED.getStatusCode());
            return;
        } else if (!registeredIp.equals(deviceIp)) {
            log.warn("Conflicting IP: Received IP is " + deviceIp + ". Device with ID " + deviceId +
                     " is already registered under some other IP. Re-registration required");
            response.setStatus(Response.Status.CONFLICT.getStatusCode());
            return;
        }
        SensorDataManager.getInstance().setSensorRecord(deviceId, CurrentSensorConstants.SENSOR_CURRENT,
                                                        String.valueOf(current),
                                                        Calendar.getInstance().getTimeInMillis());
        SensorDataManager.getInstance().setSensorRecord(deviceId, CurrentSensorConstants.SENSOR_POWER,
                                                        String.valueOf(current * 230),
                                                        Calendar.getInstance().getTimeInMillis());
        SensorDataManager.getInstance().setSensorRecord(deviceId, CurrentSensorConstants.SENSOR_FLOWRATE,
                                                        String.valueOf(flow_rate),
                                                        Calendar.getInstance().getTimeInMillis());
        if (!CurrentSensorServiceUtils.publishToDASCurrent(dataMsg.deviceId, current)) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.warn("An error occured whilst trying to publish pin data of Current Sensor Data with ID [" +
                     deviceId + "] of owner [" + owner + "]");
        }
        if (!CurrentSensorServiceUtils.publishToDASPower(dataMsg.deviceId, current * 230)) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.warn("An error occured whilst trying to publish pin data of Power Sensor Data with ID [" +
                     deviceId + "] of owner [" + owner + "]");
        }
        if (!CurrentSensorServiceUtils.publishToDASFlowRate(dataMsg.deviceId, flow_rate)) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.warn("An error occured whilst trying to publish pin data of Current Sensor Data with ID [" +
                     deviceId + "] of owner [" + owner + "]");
        }
    }

}
