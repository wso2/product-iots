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
import org.homeautomation.droneanalyzer.api.util.APIUtil;
import org.homeautomation.droneanalyzer.plugin.constants.DroneConstants;
import org.wso2.carbon.apimgt.application.extension.APIManagementProviderService;
import org.wso2.carbon.apimgt.application.extension.dto.ApiApplicationKey;
import org.wso2.carbon.apimgt.application.extension.exception.APIManagerException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppAccount;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppConfig;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppServerClient;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.util.ZipArchive;
import org.wso2.carbon.device.mgt.iot.util.ZipUtil;
import org.wso2.carbon.identity.jwt.client.extension.JWTClient;
import org.wso2.carbon.identity.jwt.client.extension.JWTClientManager;
import org.wso2.carbon.identity.jwt.client.extension.dto.AccessTokenInfo;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.user.api.UserStoreException;

import javax.ws.rs.core.Response;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class DroneManagerServiceImpl implements DroneManagerService {

    private static org.apache.commons.logging.Log log = LogFactory.getLog(DroneManagerServiceImpl.class);
    private static final String KEY_TYPE = "PRODUCTION";
    private static ApiApplicationKey apiApplicationKey;

    private boolean register(String deviceId, String name) {
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(deviceId);
            deviceIdentifier.setType(DroneConstants.DEVICE_TYPE);
            if (APIUtil.getDeviceManagementService().isEnrolled(deviceIdentifier)) {
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
            device.setType(DroneConstants.DEVICE_TYPE);
            enrolmentInfo.setOwner(APIUtil.getAuthenticatedUser());
            device.setEnrolmentInfo(enrolmentInfo);
            boolean added = APIUtil.getDeviceManagementService().enrollDevice(device);
            return added;
        } catch (DeviceManagementException e) {
            return false;
        }
    }

    public Response removeDevice(String deviceId) {
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(deviceId);
            deviceIdentifier.setType(DroneConstants.DEVICE_TYPE);
            boolean removed = APIUtil.getDeviceManagementService().disenrollDevice(deviceIdentifier);
            if (removed) {
                return Response.ok().build();
            } else {
                return Response.status(Response.Status.NOT_ACCEPTABLE.getStatusCode()).build();
            }
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    public Response updateDevice(String deviceId, String name) {
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(deviceId);
            deviceIdentifier.setType(DroneConstants.DEVICE_TYPE);
            Device device = APIUtil.getDeviceManagementService().getDevice(deviceIdentifier);
            device.setDeviceIdentifier(deviceId);
            device.getEnrolmentInfo().setDateOfLastUpdate(new Date().getTime());
            device.setName(name);
            device.setType(DroneConstants.DEVICE_TYPE);
            boolean updated = APIUtil.getDeviceManagementService().modifyEnrollment(device);
            if (updated) {
                return Response.ok().build();
            } else {
                return Response.status(Response.Status.NOT_ACCEPTABLE.getStatusCode()).build();
            }
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    public Response getDevice(String deviceId) {
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(deviceId);
            deviceIdentifier.setType(DroneConstants.DEVICE_TYPE);
            Device device = APIUtil.getDeviceManagementService().getDevice(deviceIdentifier);
            return Response.ok().entity(device).build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    public Response getDroneDevices() {
        try {
            List<Device> userDevices = APIUtil.getDeviceManagementService().getDevicesOfUser(APIUtil.getAuthenticatedUser());
            ArrayList<Device> userDevicesforDrone = new ArrayList<>();
            for (Device device : userDevices) {
                if (device.getType().equals(DroneConstants.DEVICE_TYPE) &&
                    device.getEnrolmentInfo().getStatus().equals(
                            EnrolmentInfo.Status.ACTIVE)) {
                    userDevicesforDrone.add(device);
                }
            }
            Device[] devices = userDevicesforDrone.toArray(new Device[]{});
            return Response.ok().entity(devices).build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    public Response downloadSketch(String deviceName, String sketchType) {

        //create new device id
        String deviceId = shortUUID();
        //create token
        String token = UUID.randomUUID().toString();
        String refreshToken = UUID.randomUUID().toString();
        //adding registering data
        boolean status = register(deviceId, deviceName);
        if (!status) {
            return Response.status(500).entity(
                    "Error occurred while registering the device with " + "id: " + deviceId + " owner:" +
                    APIUtil.getAuthenticatedUser()).build();

        }
        ZipUtil ziputil = new ZipUtil();
        ZipArchive zipFile;
        try {
            zipFile = ziputil.createZipFile(APIUtil.getAuthenticatedUser(), APIUtil.getTenantDomainOftheUser(),
                                            sketchType, deviceId, deviceName, token, refreshToken);
        } catch (DeviceManagementException ex) {
            return Response.status(500).entity("Error occurred while creating zip file").build();
        }
        Response.ResponseBuilder rb = Response.ok(zipFile.getZipFile());
        rb.header("Content-Disposition", "attachment; filename=\"" + zipFile.getFileName() + "\"");
        return rb.build();

    }

    public Response generateSketchLink(String deviceName, String sketchType) {
        try {
            ZipArchive zipFile = createDownloadFile(deviceName, sketchType);
            Response.ResponseBuilder rb = Response.ok(zipFile.getDeviceId());
            return rb.build();
        } catch (IllegalArgumentException ex) {
            return Response.status(400).entity(ex.getMessage()).build();
        } catch (DeviceManagementException ex) {
            return Response.status(500).entity(ex.getMessage()).build();
        } catch (JWTClientException ex) {
            return Response.status(500).entity(ex.getMessage()).build();
        } catch (DeviceControllerException ex) {
            return Response.status(500).entity(ex.getMessage()).build();
        } catch (APIManagerException ex) {
            return Response.status(500).entity(ex.getMessage()).build();
        } catch (UserStoreException ex) {
            return Response.status(500).entity(ex.getMessage()).build();
        }
    }

    private ZipArchive createDownloadFile(String deviceName, String sketchType)
            throws DeviceManagementException, JWTClientException, APIManagerException, DeviceControllerException,
                   UserStoreException {
        //create new device id
        String deviceId = shortUUID();
        if (apiApplicationKey == null) {
            String applicationUsername = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration()
                    .getAdminUserName();
            APIManagementProviderService apiManagementProviderService = APIUtil.getAPIManagementProviderService();
            String[] tags = {DroneConstants.DEVICE_TYPE};
            apiApplicationKey = apiManagementProviderService.generateAndRetrieveApplicationKeys(
                    DroneConstants.DEVICE_TYPE, tags, KEY_TYPE, applicationUsername, true);
        }
        JWTClient jwtClient = JWTClientManager.getInstance().getJWTClient();
        String owner = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        String scopes = "device_type_" + DroneConstants.DEVICE_TYPE + " device_" + deviceId;
        AccessTokenInfo accessTokenInfo = jwtClient.getAccessToken(apiApplicationKey.getConsumerKey(),
                                                                   apiApplicationKey.getConsumerSecret(), owner, scopes);
        //create token
        String accessToken = accessTokenInfo.getAccess_token();
        String refreshToken = accessTokenInfo.getRefresh_token();
        //adding registering data
        XmppAccount newXmppAccount = new XmppAccount();
        newXmppAccount.setAccountName(APIUtil.getAuthenticatedUser() + "_" + deviceId);
        newXmppAccount.setUsername(deviceId);
        newXmppAccount.setPassword(accessToken);
        newXmppAccount.setEmail(deviceId + "@wso2.com");
        XmppServerClient xmppServerClient = new XmppServerClient();
        xmppServerClient.initControlQueue();
        boolean status;
        if (XmppConfig.getInstance().isEnabled()) {
            status = xmppServerClient.createXMPPAccount(newXmppAccount);
            if (!status) {
                String msg = "XMPP Account was not created for device - " + deviceId + " of owner - " +
                             APIUtil.getAuthenticatedUser() + ".XMPP might have been disabled in " +
                             "org.wso2.carbon.device.mgt.iot.common.config.server.configs";
                log.warn(msg);
                throw new DeviceManagementException(msg);
            }
        }
        //Register the device with CDMF
        status = register(deviceId, deviceName);
        if (!status) {
            String msg = "Error occurred while registering the device with " + "id: " + deviceId
                         + " owner:" + APIUtil.getAuthenticatedUser();
            throw new DeviceManagementException(msg);
        }
        ZipUtil ziputil = new ZipUtil();
        ZipArchive zipFile = ziputil.createZipFile(APIUtil.getAuthenticatedUser(), APIUtil.getTenantDomainOftheUser(),
                                                   sketchType, deviceId, deviceName, accessToken, refreshToken);
        zipFile.setDeviceId(deviceId);
        return zipFile;
    }

    private static String shortUUID() {
        UUID uuid = UUID.randomUUID();
        long l = ByteBuffer.wrap(uuid.toString().getBytes(StandardCharsets.UTF_8)).getLong();
        return Long.toString(l, Character.MAX_RADIX);
    }
}
