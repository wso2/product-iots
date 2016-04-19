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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.homeautomation.doormanager.api;

import org.homeautomation.doormanager.api.dto.UserInfo;
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

@SuppressWarnings("NonJaxWsWebServices")
@API(name = "doormanager", version = "1.0.0", context = "/doormanager", tags = {"doormanager"})
@DeviceType(value = "doormanager")
public interface DoorManagerControllerService {

    /**
     * Assign new user to lock
     *
     * @param owner        owner of the device
     * @param deviceId     unique identifier for given device
     * @param protocol     transport protocol which is being using here MQTT
     * @param cardNumber   RFID card number
     * @param userName     user name of RFID card owner
     * @param emailAddress email address of RFID card owner
     */
    @Path("device/assign-user")
    @POST
    @Feature(code = "assign_user", name = "Assign new user to lock", type = "operation",
            description = "Add new access card to user to control the lock ")
    Response assignUserToLock(@HeaderParam("owner") String owner,
                                     @HeaderParam("deviceId") String deviceId,
                                     @HeaderParam("protocol") String protocol,
                                     @FormParam("cardNumber") String cardNumber,
                                     @FormParam("userName") String userName,
                                     @FormParam("emailAddress") String emailAddress);


    /**
     * Change status of door lock safe: LOCK/UNLOCK
     *
     * @param owner    owner of the device
     * @param deviceId unique identifier for given device
     * @param protocol transport protocol which is being using here MQTT
     * @param state    status of lock safe: lock/unlock
     */
    @Path("device/change-status")
    @POST
    @Feature(code = "change-status", name = "Change status of door lock safe: LOCK/UNLOCK", type = "operation",
            description = "Change status of door lock safe: LOCK/UNLOCK")
    Response changeStatusOfDoorLockSafe(@HeaderParam("owner") String owner,
                                               @HeaderParam("deviceId") String deviceId,
                                               @HeaderParam("protocol") String protocol,
                                               @FormParam("state") String state);

    /**
     * @param userInfo user information which are required to test given user is authorized to open requested door
     * @return if user is authorized open the the door allow to open it
     */
    @GET
    @Path("device/get-user-info")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @SuppressWarnings("unchecked")
    //This is to avoid unchecked call to put(k, v) into jsonObject. org.json.simple
    // library uses raw type collections internally.
    Response get_user_info(final UserInfo userInfo);

}
