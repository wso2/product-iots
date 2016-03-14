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

package org.homeautomation.doormanager.manager.api;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.homeautomation.doormanager.manager.api.util.APIUtil;
import org.homeautomation.doormanager.manager.api.util.ResponsePayload;
import org.homeautomation.doormanager.plugin.constants.DoorManagerConstants;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.device.DeviceType;
import org.wso2.carbon.apimgt.webapp.publisher.KeyGenerationUtil;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.iot.apimgt.AccessTokenInfo;
import org.wso2.carbon.device.mgt.iot.apimgt.TokenClient;
import org.wso2.carbon.device.mgt.iot.exception.AccessTokenException;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.util.ZipArchive;
import org.wso2.carbon.device.mgt.iot.util.ZipUtil;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.DELETE;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("NonJaxWsWebServices")
@DeviceType(value = "doormanager")
@API(name = "doormanager_mgt", version = "1.0.0", context = "/doormanager_mgt")
public class DoorManagerManagerService {

    private static Log log = LogFactory.getLog(DoorManagerManagerService.class);
    @Context  //injected response proxy supporting multiple thread
    private HttpServletResponse response;

    /**
     * Generate UUID
     *
     * @return generated UUID
     */
    private static String shortUUID() {
        UUID uuid = UUID.randomUUID();
        long l = ByteBuffer.wrap(uuid.toString().getBytes(StandardCharsets.UTF_8)).getLong();
        return Long.toString(l, Character.MAX_RADIX);
    }

    /**
     * Register new device into IoT Server
     *
     * @param name name of new device
     * @return registration status
     */
    @Path("manager/device/register")
    @POST
    public boolean register(@QueryParam("deviceId") String deviceId,
                            @QueryParam("name") String name) {

        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(DoorManagerConstants.DEVICE_TYPE);
        try {
            if (APIUtil.getDeviceManagementService().isEnrolled(deviceIdentifier)) {
                response.setStatus(Response.Status.CONFLICT.getStatusCode());
                return false;
            }
            String owner = APIUtil.getAuthenticatedUser();
            Device device = new Device();
            device.setDeviceIdentifier(deviceId);
            EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
            enrolmentInfo.setDateOfEnrolment(new Date().getTime());
            enrolmentInfo.setDateOfLastUpdate(new Date().getTime());
            enrolmentInfo.setStatus(EnrolmentInfo.Status.ACTIVE);
            enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
            device.setName(name);
            device.setType(DoorManagerConstants.DEVICE_TYPE);
            enrolmentInfo.setOwner(owner);
            device.setEnrolmentInfo(enrolmentInfo);
            KeyGenerationUtil.createApplicationKeys(DoorManagerConstants.DEVICE_TYPE);
            TokenClient accessTokenClient = new TokenClient(DoorManagerConstants.DEVICE_TYPE);
            AccessTokenInfo accessTokenInfo = accessTokenClient.getAccessToken(owner, deviceId);

            //create token
            String accessToken = accessTokenInfo.getAccess_token();
            String refreshToken = accessTokenInfo.getRefresh_token();
            List<Device.Property> properties = new ArrayList<>();

            Device.Property accessTokenProperty = new Device.Property();
            accessTokenProperty.setName(DoorManagerConstants.DEVICE_PLUGIN_PROPERTY_ACCESS_TOKEN);
            log.warn("locker access Token :" + accessToken);
            accessTokenProperty.setValue(accessToken);

            Device.Property refreshTokenProperty = new Device.Property();
            refreshTokenProperty.setName(DoorManagerConstants.DEVICE_PLUGIN_PROPERTY_REFRESH_TOKEN);
            refreshTokenProperty.setValue(refreshToken);

            properties.add(accessTokenProperty);
            properties.add(refreshTokenProperty);
            device.setProperties(properties);

            boolean added = APIUtil.getDeviceManagementService().enrollDevice(device);
            if (added) {
                response.setStatus(Response.Status.OK.getStatusCode());
            } else {
                response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());
            }
            return added;
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return false;
        } catch (AccessTokenException e) {
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return false;
        }
    }

    /**
     * Remove installed device
     *
     * @param deviceId unique identifier for device
     * @param response to request
     */
    @Path("manager/device/remove/{device_id}")
    @DELETE
    public void removeDevice(@PathParam("device_id") String deviceId,
                             @Context HttpServletResponse response) {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(DoorManagerConstants.DEVICE_TYPE);
        try {
            boolean removed = APIUtil.getDeviceManagementService().disenrollDevice(
                    deviceIdentifier);
            if (removed) {
                response.setStatus(Response.Status.OK.getStatusCode());
            } else {
                response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());
            }
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
    }

    /**
     * Update device name
     *
     * @param deviceId unique identifier for device
     * @param name     new name of the device
     * @param response to request
     * @return update status
     */
    @Path("manager/device/update/{device_id}")
    @POST
    public boolean updateDevice(@PathParam("device_id") String deviceId,
                                @QueryParam("name") String name,
                                @Context HttpServletResponse response) {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(DoorManagerConstants.DEVICE_TYPE);
        try {
            Device device = APIUtil.getDeviceManagementService().getDevice(deviceIdentifier);
            device.setDeviceIdentifier(deviceId);
            device.getEnrolmentInfo().setDateOfLastUpdate(new Date().getTime());
            device.setName(name);
            device.setType(DoorManagerConstants.DEVICE_TYPE);
            boolean updated = APIUtil.getDeviceManagementService().modifyEnrollment(device);
            if (updated) {
                response.setStatus(Response.Status.OK.getStatusCode());
            } else {
                response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());
            }
            return updated;
        } catch (DeviceManagementException e) {
            log.error(e.getErrorMessage());
            return false;
        }
    }

    /**
     * Get device information
     *
     * @param deviceId unique identifier for device
     * @return device
     */
    @Path("manager/device/{device_id}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Device getDevice(@PathParam("device_id") String deviceId) {
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(DoorManagerConstants.DEVICE_TYPE);
        try {
            return APIUtil.getDeviceManagementService().getDevice(deviceIdentifier);
        } catch (DeviceManagementException ex) {
            log.error("Error occurred while retrieving device with Id " + deviceId + "\n" + ex);
            return null;
        }
    }

    /**
     * This will download the agent for given device type
     *
     * @param deviceName name of the device which is to be created
     * @param sketchType name of sketch type
     * @return agent archive
     */
    @Path("manager/device/{sketch_type}/download")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response downloadSketch(@QueryParam("deviceName") String deviceName,
                                   @PathParam("sketch_type") String sketchType) {
        try {
            ZipArchive zipFile = createDownloadFile(APIUtil.getAuthenticatedUser(), deviceName, sketchType);
            Response.ResponseBuilder response = Response.ok(FileUtils.readFileToByteArray(zipFile.getZipFile()));
            response.type("application/zip");
            response.header("Content-Disposition", "attachment; filename=\"" + zipFile.getFileName() + "\"");

            return response.build();
        } catch (IllegalArgumentException ex) {
            return Response.status(400).entity(ex.getMessage()).build();//bad request
        } catch (DeviceManagementException ex) {
            return Response.status(500).entity(ex.getMessage()).build();
        } catch (AccessTokenException ex) {
            return Response.status(500).entity(ex.getMessage()).build();
        } catch (DeviceControllerException ex) {
            return Response.status(500).entity(ex.getMessage()).build();
        } catch (IOException ex) {
            return Response.status(500).entity(ex.getMessage()).build();
        }
    }

    /**
     * This will give link to generated agent
     *
     * @param deviceName name of the device which is to be created
     * @param sketchType name of sketch type
     * @return link to generated agent
     */
    @Path("manager/device/{sketch_type}/generate_link")
    @GET
    public Response generateSketchLink(@QueryParam("deviceName") String deviceName,
                                       @PathParam("sketch_type") String sketchType) {
        try {
            ZipArchive zipFile = createDownloadFile(APIUtil.getAuthenticatedUser(), deviceName, sketchType);
            ResponsePayload responsePayload = new ResponsePayload();
            responsePayload.setStatusCode(HttpStatus.SC_OK);
            responsePayload.setMessageFromServer("Sending Requested sketch by type: " + sketchType +
                    " and id: " + zipFile.getDeviceId() + ".");
            responsePayload.setResponseContent(zipFile.getDeviceId());
            return Response.status(HttpStatus.SC_OK).entity(responsePayload).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(HttpStatus.SC_BAD_REQUEST).entity(ex.getMessage()).build();
        } catch (DeviceManagementException ex) {
            log.error("Error occurred while creating device with name " + deviceName + "\n", ex);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        } catch (AccessTokenException ex) {
            log.error(ex.getMessage(), ex);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        } catch (DeviceControllerException ex) {
            log.error(ex.getMessage(), ex);
            return Response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }

    /**
     * This will give link to generated agent
     *
     * @param deviceName name of the device which is to be created
     * @param sketchType name of sketch type
     * @return link to generated agent
     */
    private ZipArchive createDownloadFile(String owner, String deviceName, String sketchType)
            throws DeviceManagementException, AccessTokenException, DeviceControllerException {
        if (owner == null) {
            throw new IllegalArgumentException("Error on createDownloadFile() Owner is null!");
        }
        //create new device id
        String deviceId = shortUUID();
        KeyGenerationUtil.createApplicationKeys(DoorManagerConstants.DEVICE_TYPE);
        TokenClient accessTokenClient = new TokenClient(DoorManagerConstants.DEVICE_TYPE);
        AccessTokenInfo accessTokenInfo = accessTokenClient.getAccessToken(owner, deviceId);
        //create token
        String accessToken = accessTokenInfo.getAccess_token();
        String refreshToken = accessTokenInfo.getRefresh_token();
        //adding registering data
        boolean status;
        //Register the device with CDMF
        status = register(deviceId, deviceName);
        if (!status) {
            String msg = "Error occurred while registering the device with " + "id: " + deviceId + " owner:" + owner;
            throw new DeviceManagementException(msg);
        }
        ZipUtil ziputil = new ZipUtil();
        ZipArchive zipFile = ziputil.createZipFile(owner, APIUtil.getTenantDomainOfUser(), sketchType,
                deviceId, deviceName, accessToken, refreshToken);
        zipFile.setDeviceId(deviceId);
        return zipFile;
    }

}
