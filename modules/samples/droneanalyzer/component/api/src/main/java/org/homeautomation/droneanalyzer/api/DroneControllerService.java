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

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@API(name = "drone_analyzer", version = "1.0.0", context = "/drone_analyzer", tags = {"drone_analyzer"})
@DeviceType(value = "drone_analyzer")
public interface DroneControllerService {

    @Path("device/register/{deviceId}/{ip}/{port}")
    @POST
    Response registerDeviceIP(@PathParam("deviceId") String deviceId, @PathParam("ip") String deviceIP,
                              @PathParam("port") String devicePort);

    @Path("device/{deviceId}/send_command")
    @POST
    /*@Feature( code="send_command", name="Send Command", type="operation",
            description="Send Commands to Drone")*/
    Response droneController(@PathParam("deviceId") String deviceId, @FormParam("action") String action,
                             @FormParam("duration") String duration, @FormParam("speed") String speed);

}