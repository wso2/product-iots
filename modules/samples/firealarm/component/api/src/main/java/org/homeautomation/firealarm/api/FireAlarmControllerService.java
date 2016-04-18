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

import org.homeautomation.firealarm.api.dto.DeviceJSON;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.DeviceType;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.Feature;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * This is the controller API which is used to control agent side functionality
 */
@SuppressWarnings("NonJaxWsWebServices")
@API(name = "firealarm", version = "1.0.0", context = "/firealarm" ,tags = {"firealarm"})
@DeviceType(value = "firealarm")
public interface FireAlarmControllerService {

    /**
     * @param agentInfo device owner,id and sensor value
     * @return device registration status
     */
    @Path("device/register")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response registerDevice(final DeviceJSON agentInfo);

    /**
     * @param owner    device owner
     * @param deviceId unique identifier for given device type
     * @param protocol name of supported protocol. here MQTT is used
     * @return sensor record
     */
    @Path("device/read-temperature")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Feature(code = "read-temperature", name = "Temperature", type = "monitor",
            description = "Request temperature reading from device")
    Response readTemperature(@HeaderParam("owner") String owner,
                                    @HeaderParam("deviceId") String deviceId,
                                    @HeaderParam("protocol") String protocol);

    /**
     * @param owner    device owner
     * @param deviceId unique identifier for given device type
     * @param protocol name of supported protocol. here MQTT is used
     * @return sensor record
     */
    @Path("device/read-humidity")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Feature(code = "read-humidity", name = "Humidity", type = "monitor",
            description = "Request humidity reading from device")
    Response readHumidity(@HeaderParam("owner") String owner,
                                 @HeaderParam("deviceId") String deviceId,
                                 @HeaderParam("protocol") String protocol);

    /**
     * @param owner    device owner
     * @param deviceId unique identifier for given device type
     * @param protocol name of supported protocol. Here MQTT is used
     * @param state    change status of buzzer: on/off
     */
    @Path("device/change-status")
    @POST
    @Feature(code = "change-status", name = "Buzzer: on/off", type = "operation",
            description = "Switch on/off Fire Alarm Buzzer. (On / Off)")
    Response changeBuzzerState(@HeaderParam("owner") String owner,
                                      @HeaderParam("deviceId") String deviceId,
                                      @HeaderParam("protocol") String protocol,
                                      @FormParam("state") String state);

}
