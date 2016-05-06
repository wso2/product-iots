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

package ${groupId}.${rootArtifactId}.api;

import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ${groupId}.${rootArtifactId}.api.dto.DeviceJSON;
import ${groupId}.${rootArtifactId}.api.transport.MQTTConnector;

import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.DeviceType;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.Feature;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.service.IoTServerStartupListener;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * This is the controller API which is used to control agent side functionality
 */
@SuppressWarnings("NonJaxWsWebServices")
@API(name = "${deviceType}", version = "1.0.0", context = "/${deviceType}", tags = "${deviceType}")
@DeviceType(value = "${deviceType}")
public interface ControllerService {

    /**
     * @param agentInfo device owner,id and sensor value
     * @return
     */
    @Path("device/register")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response registerDevice(final DeviceJSON agentInfo);

    /**
     * @param deviceId unique identifier for given device type
     * @param state    change status of sensor: on/off
     * @param response
     */
    @Path("device/{deviceId}/change-status")
    @POST
    @Feature(code = "change-status", name = "Change status of sensor: on/off", type = "operation",
            description = "Change status of sensor: on/off")
    Response changeStatus(@PathParam("deviceId") String deviceId,
                             @QueryParam("state") String state,
                             @Context HttpServletResponse response);

    /**
     * Retrieve Sensor data for the ${deviceType}
     */
    @Path("device/stats/{deviceId}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    Response getSensorStats(@PathParam("deviceId") String deviceId, @QueryParam("from") long from,
                                      @QueryParam("to") long to);


}