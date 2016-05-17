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

import ${groupId}.${rootArtifactId}.api.dto.DeviceJSON;

import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.api.Permission;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.DeviceType;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.Feature;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * This is the controller API which is used to control agent side functionality
 */
@SuppressWarnings("NonJaxWsWebServices")
@API(name = "${deviceType}", version = "1.0.0", context = "/${deviceType}", tags = "${deviceType}")
@DeviceType(value = "${deviceType}")
public interface DeviceTypeService {

    /**
     * @param agentInfo device owner,id and sensor value
     * @return
     */
    @Path("device/register")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Permission(scope = "${deviceType}_user", permissions = {"/permission/admin/device-mgt/user/register"})
    Response registerDevice(final DeviceJSON agentInfo);

    /**
     * @param deviceId unique identifier for given device type
     * @param state    change status of sensor: on/off
     * @param response
     */
    @Path("device/{deviceId}/change-status")
    @POST
    @Feature(code = "change-status", name = "Change status of sensor: on/off",
            description = "Change status of sensor: on/off")
    @Permission(scope = "${deviceType}_user", permissions = {"/permission/admin/device-mgt/change-status"})
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
    @Permission(scope = "${deviceType}_user", permissions = {"/permission/admin/device-mgt/stats"})
    Response getSensorStats(@PathParam("deviceId") String deviceId, @QueryParam("from") long from,
                            @QueryParam("to") long to);

    @Path("/device/{device_id}")
    @DELETE
    @Permission(scope = "${deviceType}_user", permissions = {"/permission/admin/device-mgt/removeDevice"})
    Response removeDevice(@PathParam("device_id") String deviceId);

    @Path("/device/{device_id}")
    @PUT
    @Permission(scope = "${deviceType}_user", permissions = {"/permission/admin/device-mgt/updateDevice"})
    Response updateDevice(@PathParam("device_id") String deviceId, @QueryParam("name") String name);

    @Path("/device/{device_id}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Permission(scope = "${deviceType}_user", permissions = {"/permission/admin/device-mgt/updateDevice"})
    Response getDevice(@PathParam("device_id") String deviceId);

    @Path("/devices")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Permission(scope = "${deviceType}_user", permissions = {"/permission/admin/device-mgt/devices"})
    Response getAllDevices();

    @Path("/device/download")
    @GET
    @Produces("application/zip")
    @Permission(scope = "${deviceType}_user", permissions = {"/permission/admin/device-mgt/download"})
    Response downloadSketch(@QueryParam("deviceName") String deviceName, @QueryParam("sketchType") String sketchType);
}