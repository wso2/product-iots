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

import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.DeviceType;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@API(name = "drone_analyzer_mgt", version = "1.0.0", context = "/drone_analyzer_mgt", tags = {"drone_analyzer"})
@DeviceType(value = "drone_analyzer")
public interface DroneManagerService {

    @Path("devices/{device_id}")
    @DELETE
    Response removeDevice(@PathParam("device_id") String deviceId);

    @Path("devices/{device_id}")
    @PUT
    Response updateDevice(@PathParam("device_id") String deviceId, @QueryParam("name") String name);

    @Path("devices/{device_id}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    Response getDevice(@PathParam("device_id") String deviceId);

    @Path("devices")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    Response getDroneDevices();

    @Path("devices/{sketch_type}/download")
    @GET
    @Produces("application/octet-stream")
    Response downloadSketch(@QueryParam("deviceName") String deviceName, @PathParam("sketch_type") String sketchType);

    @Path("devices/{sketch_type}/generate_link")
    @GET
    Response generateSketchLink(@QueryParam("deviceName") String deviceName,
                                @PathParam("sketch_type") String sketchType);

}

