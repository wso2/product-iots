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

package org.homeautomation.doormanager.api;

import org.homeautomation.doormanager.api.dto.DeviceJSON;
import org.homeautomation.doormanager.api.dto.UserInfo;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.api.Permission;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.DeviceType;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.Feature;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * This is the API which is used to control and manage device type functionality
 */
@SuppressWarnings("NonJaxWsWebServices")
@API(name = "doormanager", version = "1.0.0", context = "/doormanager", tags = "doormanager")
@DeviceType(value = "doormanager")
public interface DoorManagerService {

    /**
     * @param deviceId  unique identifier for given device type instance
     * @param state     change status of sensor: on/off
     */
    @Path("device/{deviceId}/change-status")
    @POST
    @Feature(code = "change-status", name = "Change status of door lock: lock/unlock",
            description = "Change status of door lock: lock/unlock")
    Response changeStatusOfDoorLockSafe(@PathParam("deviceId") String deviceId,
                                               @QueryParam("state") String state,
                                               @Context HttpServletResponse response);

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
    @Permission(scope = "doormanager_user", permissions = {"/permission/admin/device-mgt/stats"})
    Response getSensorStats(@PathParam("deviceId") String deviceId, @QueryParam("from") long from,
                            @QueryParam("to") long to, @QueryParam("sensorType") String sensorType);

    /**
     * Remove device type instance using device id
     * @param deviceId  unique identifier for given device type instance
     */
    @Path("/device/{device_id}")
    @DELETE
    @Permission(scope = "doormanager_user", permissions = {"/permission/admin/device-mgt/removeDevice"})
    Response removeDevice(@PathParam("device_id") String deviceId);

    /**
     * Update device instance name
     * @param deviceId  unique identifier for given device type instance
     * @param name      new name for the device type instance
     */
    @Path("/device/{device_id}")
    @PUT
    @Permission(scope = "doormanager_user", permissions = {"/permission/admin/device-mgt/updateDevice"})
    Response updateDevice(@PathParam("device_id") String deviceId, @QueryParam("name") String name);

    /**
     * To get device information
     * @param deviceId  unique identifier for given device type instance
     * @return Device object which carries all information related to a managed device
     */
    @Path("/device/{device_id}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Permission(scope = "doormanager_user", permissions = {"/permission/admin/device-mgt/updateDevice"})
    Response getDevice(@PathParam("device_id") String deviceId);

    /**
     * Get all device type instance which belongs to user
     * @return  Array of devices which includes device's information
     */
    @Path("/devices")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Permission(scope = "doormanager_user", permissions = {"/permission/admin/device-mgt/devices"})
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
    @Permission(scope = "doormanager_user", permissions = {"/permission/admin/device-mgt/download"})
    Response downloadSketch(@QueryParam("deviceName") String deviceName, @QueryParam("sketchType") String sketchType);

    @Path("device/assign-user/{deviceId}")
    @POST
    @Feature(code = "assign-user", name = "Assign new user to lock",
            description = "Add new access card to user to control the lock ")
    Response assignUserToLock(@QueryParam("cardNumber") String cardNumber,
                                     @QueryParam("userName") String userName,
                                     @QueryParam("emailAddress") String emailAddress,
                                     @PathParam("deviceId") String deviceId);

    /**
     * @param userInfo user information which are required to test given user is authorized to open requested door
     * @return if user is authorized open the the door allow to open it
     */
    @POST
    @Path("/device/is-user-authorized")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Response isUserAuthorized(final UserInfo userInfo);
}