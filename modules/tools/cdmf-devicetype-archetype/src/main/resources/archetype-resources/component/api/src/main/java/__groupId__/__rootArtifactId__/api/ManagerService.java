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

import org.wso2.carbon.apimgt.annotations.api.API;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("enrollment")
@API(name = "${deviceType}_mgt", version = "1.0.0", context = "/${deviceType}_mgt", tags = "${deviceType}")
public interface ManagerService {

    @Path("/devices/{device_id}")
    @DELETE
    Response removeDevice(@PathParam("device_id") String deviceId);

    @Path("/devices/{device_id}")
    @PUT
    Response updateDevice(@PathParam("device_id") String deviceId, @QueryParam("name") String name);

    @Path("/devices/{device_id}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response getDevice(@PathParam("device_id") String deviceId);

    @Path("/devices")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response getAllDevices();

    @Path("/devices/download")
    @GET
    @Produces("application/zip")
    Response downloadSketch(@QueryParam("deviceName") String deviceName, @QueryParam("sketchType") String sketchType);

}