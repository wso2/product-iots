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

package org.homeautomation.currentsensor.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.homeautomation.currentsensor.api.dto.DeviceJSON;
import org.homeautomation.currentsensor.api.util.CurrentSensorServiceUtils;
import org.homeautomation.currentsensor.plugin.constants.CurrentSensorConstants;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.service.IoTServerStartupListener;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;

public class CurrentSensorControllerServiceImpl implements CurrentSensorControllerService{

    private static Log log = LogFactory.getLog(CurrentSensorControllerServiceImpl.class);
    private ConcurrentHashMap<String, String> deviceToIpMap = new ConcurrentHashMap<>();

    @Path("device/register/{owner}/{deviceId}/{ip}/{port}")
    @POST
    public Response registerDeviceIP(@PathParam("owner") String owner, @PathParam("deviceId") String deviceId,
                              @PathParam("ip") String deviceIP, @PathParam("port") String devicePort) {
        //TODO:: Need to get IP from the request itself
        String result;

        if (log.isDebugEnabled()) {
            log.debug("Got register call from IP: " + deviceIP + " for Device ID: " + deviceId + " of owner: " + owner);
        }
        String deviceHttpEndpoint = deviceIP + ":" + devicePort;
        deviceToIpMap.put(deviceId, deviceHttpEndpoint);
        result = "Device-IP Registered";
        if (log.isDebugEnabled()) {
            log.debug(result);
        }
        return Response.ok().entity(result).build();
    }

    @Path("device/push-data")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response pushData(final DeviceJSON dataMsg) {
        String owner = dataMsg.owner;
        String deviceId = dataMsg.deviceId;
        String deviceIp = dataMsg.reply;
        float current = dataMsg.current;
        float flow_rate = dataMsg.flow_rate;
        String registeredIp = deviceToIpMap.get(deviceId);

        if (registeredIp == null) {
            log.warn("Unregistered IP: Temperature Data Received from an un-registered IP " + deviceIp +
                     " for device ID - " + deviceId);
            return Response.status(Response.Status.PRECONDITION_FAILED.getStatusCode()).build();
        } else if (!registeredIp.equals(deviceIp)) {
            log.warn("Conflicting IP: Received IP is " + deviceIp + ". Device with ID " + deviceId +
                     " is already registered under some other IP. Re-registration required");
            return Response.status(Response.Status.CONFLICT.getStatusCode()).build();
        }
        if (!CurrentSensorServiceUtils.publishToDASCurrent(dataMsg.deviceId, current)) {
            log.warn("An error occured whilst trying to publish pin data of Current Sensor Data with ID [" + deviceId +
                     "] of owner [" + owner + "]");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        if (!CurrentSensorServiceUtils.publishToDASPower(dataMsg.deviceId, current * 230)) {
            log.warn("An error occured whilst trying to publish pin data of Power Sensor Data with ID [" + deviceId +
                     "] of owner [" + owner + "]");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        if (!CurrentSensorServiceUtils.publishToDASFlowRate(dataMsg.deviceId, flow_rate)) {
            log.warn("An error occured whilst trying to publish pin data of Current Sensor Data with ID [" + deviceId +
                     "] of owner [" + owner + "]");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.ok().build();
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

}
