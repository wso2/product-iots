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

import org.apache.commons.logging.LogFactory;
import org.homeautomation.droneanalyzer.api.util.DroneAnalyzerServiceUtils;
import org.homeautomation.droneanalyzer.plugin.controller.DroneController;
import org.homeautomation.droneanalyzer.plugin.controller.impl.DroneControllerImpl;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import javax.ws.rs.core.Response;
import java.util.concurrent.ConcurrentHashMap;

public class DroneControllerServiceImpl implements DroneControllerService {

    private static org.apache.commons.logging.Log log = LogFactory.getLog(DroneControllerServiceImpl.class);
    private ConcurrentHashMap<String, String> deviceToIpMap = new ConcurrentHashMap<>();
    private DroneController droneController = new DroneControllerImpl();

    public Response registerDeviceIP(String deviceId, String deviceIP, String devicePort) {
        String result;
        String deviceHttpEndpoint = deviceIP + ":" + devicePort;
        deviceToIpMap.put(deviceId, deviceHttpEndpoint);
        result = "Device-IP Registered";
        if (log.isDebugEnabled()) {
            log.debug(result);
        }
        return Response.ok(Response.Status.OK.getStatusCode()).build();
    }

    public Response droneController(String deviceId, String action, String duration, String speed) {
        try {
            DroneAnalyzerServiceUtils.sendControlCommand(droneController, deviceId, action, Double.valueOf(speed),
                                                         Double.valueOf(duration));
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (DeviceManagementException e) {
            log.error("Drone command didn't success. Try again, \n" + e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
