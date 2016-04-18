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

import org.homeautomation.currentsensor.api.dto.DeviceJSON;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.DeviceType;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.Feature;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@API(name = "currentsensor", version = "1.0.0", context = "/currentsensor", tags = {"currentsensor"})
@DeviceType(value = "currentsensor")
public interface CurrentSensorControllerService {

    @Path("device/register/{owner}/{deviceId}/{ip}/{port}")
    @POST
    Response registerDeviceIP(@PathParam("owner") String owner, @PathParam("deviceId") String deviceId,
                              @PathParam("ip") String deviceIP, @PathParam("port") String devicePort);

    /**
     * @param deviceId
     * @return
     */
    @Path("device/read-current")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Feature(code = "read-current", name = "Current", type = "monitor",
            description = "Request current reading from Arduino agent")
    Response requestCurrent(@HeaderParam("deviceId") String deviceId);

    /**
     * @param deviceId
     * @return
     */
    @Path("device/read-power")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Feature(code = "read-power", name = "Power", type = "monitor",
            description = "Request power reading from Arduino agent")
    Response requestPower(@HeaderParam("deviceId") String deviceId);

    /**
     * @param deviceId
     * @return
     */
    @Path("device/read-flowrate")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Feature(code = "read-flowrate", name = "Flow Rate", type = "monitor",
            description = "Request flow rate reading from Arduino agent")
    Response requestFlowRate(@HeaderParam("deviceId") String deviceId);

    /**
     * @param dataMsg
     */
    @Path("device/push-data")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response pushData(final DeviceJSON dataMsg);

}
