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
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorRecord;
import org.wso2.carbon.device.mgt.iot.service.IoTServerStartupListener;

import javax.ws.rs.core.Response;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;

public class CurrentSensorControllerServiceImpl implements CurrentSensorControllerService{

    private static Log log = LogFactory.getLog(CurrentSensorControllerServiceImpl.class);
    private ConcurrentHashMap<String, String> deviceToIpMap = new ConcurrentHashMap<>();

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

    public Response registerDeviceIP(String owner, String deviceId, String deviceIP, String devicePort) {
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

    public Response requestCurrent(String deviceId) {
        SensorRecord sensorRecord = null;
        try {
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
                                                                           CurrentSensorConstants.SENSOR_CURRENT);
        } catch (DeviceControllerException e) {
            Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }

        return Response.ok().entity(sensorRecord).build();
    }

    public Response requestPower(String deviceId) {
        SensorRecord sensorRecord = null;
        try {
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
                                                                           CurrentSensorConstants.SENSOR_POWER);
        } catch (DeviceControllerException e) {
            Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
        return Response.ok().entity(sensorRecord).build();
    }

    public Response requestFlowRate(String deviceId) {
        SensorRecord sensorRecord = null;
        try {
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
                                                                           CurrentSensorConstants.SENSOR_FLOWRATE);
        } catch (DeviceControllerException e) {
            Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
        return Response.ok().entity(sensorRecord).build();
    }

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

}
