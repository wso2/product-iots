/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.coffeeking.manager.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.coffeeking.connectedcup.plugin.constants.ConnectedCupConstants;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.device.DeviceType;
import org.wso2.carbon.apimgt.webapp.publisher.KeyGenerationUtil;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.iot.DeviceManagement;
import org.wso2.carbon.device.mgt.iot.apimgt.AccessTokenInfo;
import org.wso2.carbon.device.mgt.iot.apimgt.TokenClient;
import org.wso2.carbon.device.mgt.iot.exception.AccessTokenException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@API( name="connectedcup_mgt", version="1.0.0", context="/connectedcup_mgt")
public class    ConnectedCupManagerService {
    private static Log log = LogFactory.getLog(ConnectedCupManagerService.class);
    private static final String SUPER_TENANT = "carbon.super";

    @Context
    private HttpServletResponse response;
    /**
     * @param name
     * @param owner
     * @return
     */
    @Path("cup/register")
    @POST
    public boolean register(@QueryParam("name") String name, @QueryParam("owner") String owner) {


        DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);
        String deviceId = shortUUID();

        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(ConnectedCupConstants.DEVICE_TYPE);

        try {
            if (deviceManagement.getDeviceManagementService().isEnrolled(deviceIdentifier)) {
                response.setStatus(Response.Status.CONFLICT.getStatusCode());
                return false;
            }

            Device device = new Device();
            device.setDeviceIdentifier(deviceId);
            EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
            enrolmentInfo.setDateOfEnrolment(new Date().getTime());
            enrolmentInfo.setDateOfLastUpdate(new Date().getTime());
            enrolmentInfo.setStatus(EnrolmentInfo.Status.ACTIVE);
            enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
            device.setName(name);
            device.setType(ConnectedCupConstants.DEVICE_TYPE);
            enrolmentInfo.setOwner(owner);
            device.setEnrolmentInfo(enrolmentInfo);

            KeyGenerationUtil.createApplicationKeys(ConnectedCupConstants.DEVICE_TYPE);

            TokenClient accessTokenClient = new TokenClient(ConnectedCupConstants.DEVICE_TYPE);
            AccessTokenInfo accessTokenInfo = accessTokenClient.getAccessToken(owner, deviceId);

            //create token
            String accessToken = accessTokenInfo.getAccess_token();
            String refreshToken = accessTokenInfo.getRefresh_token();
            List<Device.Property> properties = new ArrayList<>();

            Device.Property accessTokenProperty = new Device.Property();
            accessTokenProperty.setName("accessToken");
            accessTokenProperty.setValue(accessToken);

            Device.Property refreshTokenProperty = new Device.Property();
            refreshTokenProperty.setName("refreshToken");
            refreshTokenProperty.setValue(refreshToken);

            properties.add(accessTokenProperty);
            properties.add(refreshTokenProperty);
            device.setProperties(properties);

            boolean added = deviceManagement.getDeviceManagementService().enrollDevice(device);
            if (added) {
                response.setStatus(Response.Status.OK.getStatusCode());
            } else {
                response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());
            }

            return added;
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return false;
        } catch (AccessTokenException e) {
            e.printStackTrace();
        } finally {
            deviceManagement.endTenantFlow();
        }
        return true;

    }

    @Path("/device/remove/{device_id}")
    @DELETE
    public void removeDevice(@PathParam("device_id") String deviceId,
                             @Context HttpServletResponse response) {

        DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(ConnectedCupConstants.DEVICE_TYPE);

        try {
            boolean removed = deviceManagement.getDeviceManagementService().disenrollDevice(
                    deviceIdentifier);
            if (removed) {
                response.setStatus(Response.Status.OK.getStatusCode());

            } else {
                response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());

            }
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        } finally {
            deviceManagement.endTenantFlow();
        }


    }

    @Path("/device/update/{device_id}")
    @POST
    public boolean updateDevice(@PathParam("device_id") String deviceId,
                                @QueryParam("name") String name,
                                @Context HttpServletResponse response) {

        DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);

        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(ConnectedCupConstants.DEVICE_TYPE);

        try {
            Device device = deviceManagement.getDeviceManagementService().getDevice(
                    deviceIdentifier);
            device.setDeviceIdentifier(deviceId);

            // device.setDeviceTypeId(deviceTypeId);
            device.getEnrolmentInfo().setDateOfLastUpdate(new Date().getTime());

            device.setName(name);
            device.setType(ConnectedCupConstants.DEVICE_TYPE);

            boolean updated = deviceManagement.getDeviceManagementService().modifyEnrollment(
                    device);


            if (updated) {
                response.setStatus(Response.Status.OK.getStatusCode());

            } else {
                response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());

            }
            return updated;
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return false;
        } finally {
            deviceManagement.endTenantFlow();
        }

    }

    @Path("/device/{device_id}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Device getDevice(@PathParam("device_id") String deviceId) {

        DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(ConnectedCupConstants.DEVICE_TYPE);

        try {
            Device device = deviceManagement.getDeviceManagementService().getDevice(
                    deviceIdentifier);

            return device;
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return null;
        } finally {
            deviceManagement.endTenantFlow();
        }

    }

    private static String shortUUID() {
        UUID uuid = UUID.randomUUID();
        long l = ByteBuffer.wrap(uuid.toString().getBytes(StandardCharsets.UTF_8)).getLong();
        return Long.toString(l, Character.MAX_RADIX);
    }

}