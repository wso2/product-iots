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

package org.homeautomation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.device.DeviceType;
import org.wso2.carbon.apimgt.annotations.device.feature.Feature;
import org.wso2.carbon.device.mgt.iot.DeviceManagement;
import org.wso2.carbon.device.mgt.iot.currentsensor.controller.api.dto.DeviceJSON;
import org.wso2.carbon.device.mgt.iot.currentsensor.controller.api.util.CurrentSensorServiceUtils;
import org.wso2.carbon.device.mgt.iot.currentsensor.plugin.constants.CurrentSensorConstants;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorRecord;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Calendar;

@API(name = "currentsensor", version = "1.0.0", context = "/currentsensor")
@DeviceType(value = "currentsensor")
public class CurrentSensorControllerService {


    private static Log log = LogFactory.getLog(CurrentSensorControllerService.class);

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
    @Feature( code="read-current", name="Current", type="monitor",
            description="Request current reading from Arduino agent")
    public SensorRecord requestCurrent(@HeaderParam("owner") String owner,
                                           @HeaderParam("deviceId") String deviceId,
                                           @HeaderParam("protocol") String protocol,
                                           @Context HttpServletResponse response) {
        SensorRecord sensorRecord = null;

        try {
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
                    CurrentSensorConstants.SENSOR_CURRENT);
        } catch ( DeviceControllerException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

        response.setStatus(Response.Status.OK.getStatusCode());
        return sensorRecord;
    }

    /**
     * @param dataMsg
     * @param response
     */
    @Path("controller/pushcurrent")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void pushCurrent(final DeviceJSON dataMsg, @Context HttpServletResponse response) {

        String owner = dataMsg.owner;
        String deviceId = dataMsg.deviceId;
        float pinData = dataMsg.value;

        SensorDataManager.getInstance().setSensorRecord(deviceId, CurrentSensorConstants.SENSOR_CURRENT,
                String.valueOf(pinData),
                Calendar.getInstance().getTimeInMillis());

        SensorDataManager.getInstance().setSensorRecord(deviceId, CurrentSensorConstants.SENSOR_POWER,
                String.valueOf(pinData * 230),
                Calendar.getInstance().getTimeInMillis());


        if (!CurrentSensorServiceUtils.publishToDAS(dataMsg.owner, dataMsg.deviceId, dataMsg.value)) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.warn("An error occured whilst trying to publish pin data of Current Sensor Data with ID [" + deviceId +
                    "] of owner [" + owner + "]");
        }

        if (!CurrentSensorServiceUtils.publishToDAS(dataMsg.owner, dataMsg.deviceId, dataMsg.value * 230)) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.warn("An error occured whilst trying to publish pin data of Current Sensor Data with ID [" + deviceId +
                    "] of owner [" + owner + "]");
        }
    }

}
