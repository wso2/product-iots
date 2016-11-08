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

package org.homeautomation.droneanalyzer.api;

import org.homeautomation.droneanalyzer.api.dto.DeviceJSON;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.api.Permission;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.DeviceType;

import javax.ws.rs.PathParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.DELETE;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * This is the API which is used to control and manage device type functionality
 */
@SuppressWarnings("NonJaxWsWebServices")
@API(name = "drone", version = "1.0.0", context = "/drone", tags = "drone")
@DeviceType(value = "drone")
public interface DroneAnalyzerService {

    /**
     * @param agentInfo device owner,id
     * @return true if device instance is added to map
     */
    @Path("device/register")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Permission(scope = "drone_user", permissions = {"/permission/admin/device-mgt/user/register"})
    Response registerDevice(final DeviceJSON agentInfo);

    /**
     * Retrieve Sensor data for the given time period
     * @param deviceId unique identifier for given device type instance
     * @param from  starting time
     * @param to    ending time
     * @return  response with List<SensorRecord> object which includes sensor data which is requested
     */
    @Path("device/stats/{deviceId}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    @Permission(scope = "drone_user", permissions = {"/permission/admin/device-mgt/stats"})
    Response getSensorStats(@PathParam("deviceId") String deviceId, @QueryParam("from") long from,
                            @QueryParam("to") long to);

    /**
     * Remove device type instance using device id
     * @param deviceId  unique identifier for given device type instance
     */
    @Path("/device/{device_id}")
    @DELETE
    @Permission(scope = "drone_user", permissions = {"/permission/admin/device-mgt/removeDevice"})
    Response removeDevice(@PathParam("device_id") String deviceId);

    /**
     * Update device instance name
     * @param deviceId  unique identifier for given device type instance
     * @param name      new name for the device type instance
     */
    @Path("/device/{device_id}")
    @PUT
    @Permission(scope = "drone_user", permissions = {"/permission/admin/device-mgt/updateDevice"})
    Response updateDevice(@PathParam("device_id") String deviceId, @QueryParam("name") String name);

    /**
     * To get device information
     * @param deviceId  unique identifier for given device type instance
     * @return return Device object which carries all information related to a managed device
     */
    @Path("/device/{device_id}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Permission(scope = "drone_user", permissions = {"/permission/admin/device-mgt/updateDevice"})
    Response getDevice(@PathParam("device_id") String deviceId);

    /**
     * Get all device type instance which belongs to user
     * @return  Array of devices which includes device's information
     */
    @Path("/devices")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Permission(scope = "drone_user", permissions = {"/permission/admin/device-mgt/devices"})
    Response getAllDevices();

    /**
     * To download device type agent source code as zip file
     * @param deviceName   name for the device type instance
     * @param sketchType   folder name where device type agent was installed into server
     * @return  Agent source code as zip file
     */
    @Path("/device/download")
    @GET
    @Produces("application/zip")
    @Permission(scope = "drone_user", permissions = {"/permission/admin/device-mgt/download"})
    Response downloadSketch(@QueryParam("deviceName") String deviceName, @QueryParam("sketchType") String sketchType);
}