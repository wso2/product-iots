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

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.homeautomation.doormanager.api.dto.SensorRecord;
import org.homeautomation.doormanager.api.dto.UserInfo;
import org.homeautomation.doormanager.api.util.APIUtil;
import org.homeautomation.doormanager.api.util.ZipUtil;
import org.homeautomation.doormanager.plugin.constants.DoorManagerConstants;
import org.homeautomation.doormanager.plugin.exception.DoorManagerDeviceMgtPluginException;
import org.homeautomation.doormanager.plugin.impl.DoorManagerManager;
import org.homeautomation.doormanager.plugin.impl.dao.DoorLockSafe;
import org.wso2.carbon.analytics.dataservice.commons.SORT;
import org.wso2.carbon.analytics.dataservice.commons.SortByField;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.application.extension.APIManagementProviderService;
import org.wso2.carbon.apimgt.application.extension.dto.ApiApplicationKey;
import org.wso2.carbon.apimgt.application.extension.exception.APIManagerException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.operation.mgt.CommandOperation;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.DeviceType;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.Feature;
import org.wso2.carbon.device.mgt.iot.util.ZipArchive;
import org.wso2.carbon.identity.jwt.client.extension.JWTClient;
import org.wso2.carbon.identity.jwt.client.extension.dto.AccessTokenInfo;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;


/**
 * This is the API which is used to control and manage device type functionality
 */
@SuppressWarnings("NonJaxWsWebServices")
@API(name = "doormanager", version = "1.0.0", context = "/doormanager", tags = "doormanager")
@DeviceType(value = "doormanager")
public class DoorManagerServiceImpl implements DoorManagerService {

    private static final String KEY_TYPE = "PRODUCTION";
    private static Log log = LogFactory.getLog(DoorManagerService.class);
    private static volatile ApiApplicationKey apiApplicationKey;
    private DoorManagerManager doorManagerManager;
    private static String shortUUID() {
        UUID uuid = UUID.randomUUID();
        long l = ByteBuffer.wrap(uuid.toString().getBytes(StandardCharsets.UTF_8)).getLong();
        return Long.toString(l, Character.MAX_RADIX);
    }

    DoorManagerServiceImpl(){
        doorManagerManager = new DoorManagerManager();
    }

    /**
     * @param deviceId  unique identifier for given device type instance
     * @param state     change status of sensor: on/off
     */
    @Path("device/change-locker-status/{deviceId}")
    @POST
    @Feature(code = "change-locker-status", name = "Change status of door lock: lock/unlock",
            description = "Change status of door lock: lock/unlock")
    public Response changeStatusOfDoorLockSafe(@PathParam("deviceId") String deviceId,
                                 @QueryParam("state") String state,
                                 @Context HttpServletResponse response) {
        try {
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(new DeviceIdentifier(deviceId,
                    DoorManagerConstants.DEVICE_TYPE))) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            String sensorState = state.toUpperCase();
            if (!sensorState.equals(DoorManagerConstants.STATE_LOCK) && !sensorState.equals(
                    DoorManagerConstants.STATE_UNLOCK)) {
                log.error("The requested state change should be either - 'lock' or 'unlock'");
                return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).build();
            }
            String publishTopic = APIUtil.getAuthenticatedUserTenantDomain()
                    + "/" + DoorManagerConstants.DEVICE_TYPE + "/" + deviceId + DoorManagerConstants.MQTT_TOPIC_FOR_LOCK;
            Operation commandOp = new CommandOperation();
            commandOp.setCode("change-locker-status");
            commandOp.setType(Operation.Type.COMMAND);
            commandOp.setEnabled(true);
            commandOp.setPayLoad(sensorState);
            Properties props = new Properties();
            props.setProperty(DoorManagerConstants.MQTT_ADAPTER_TOPIC_PROPERTY_NAME, publishTopic);
            commandOp.setProperties(props);
            List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
            deviceIdentifiers.add(new DeviceIdentifier(deviceId, DoorManagerConstants.DEVICE_TYPE));
            APIUtil.getDeviceManagementService().addOperation(DoorManagerConstants.DEVICE_TYPE, commandOp, deviceIdentifiers);
            return Response.ok().build();
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (OperationManagementException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * @param deviceId  unique identifier for given device type instance
     * @param currentValue     change voltage
     */
    @Path("device/change-light-intensity/{deviceId}")
    @POST
    @Feature(code = "change-light-intensity", name = "Change bulb intensity",
            description = "Change status of door lock: lock/unlock")
    public Response changeLightIntensity(@PathParam("deviceId") String deviceId,
                                               @QueryParam("currentValue") String currentValue,
                                               @Context HttpServletResponse response) {
        try {
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(new DeviceIdentifier(deviceId,
                    DoorManagerConstants.DEVICE_TYPE))) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            String publishTopic = APIUtil.getAuthenticatedUserTenantDomain()
                    + "/" + DoorManagerConstants.DEVICE_TYPE + "/" + deviceId + DoorManagerConstants.MQTT_TOPIC_FOR_BULB;
            Operation commandOp = new CommandOperation();
            commandOp.setCode("change-light-intensity");
            commandOp.setType(Operation.Type.COMMAND);
            commandOp.setEnabled(true);
            commandOp.setPayLoad(currentValue);
            Properties props = new Properties();
            props.setProperty(DoorManagerConstants.MQTT_ADAPTER_TOPIC_PROPERTY_NAME, publishTopic);
            commandOp.setProperties(props);
            List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
            deviceIdentifiers.add(new DeviceIdentifier(deviceId, DoorManagerConstants.DEVICE_TYPE));
            APIUtil.getDeviceManagementService().addOperation(DoorManagerConstants.DEVICE_TYPE, commandOp, deviceIdentifiers);
            return Response.ok().build();
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (OperationManagementException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * @param deviceId  unique identifier for given device type instance
     * @param state     change status of sensor: on/off
     */
    @Path("device/change-fan-status/{deviceId}")
    @POST
    @Feature(code = "change-fan-state", name = "Change status of door lock: lock/unlock",
            description = "Change status of door lock: lock/unlock")
    public Response changeFanState(@PathParam("deviceId") String deviceId,
                                               @QueryParam("state") String state,
                                               @Context HttpServletResponse response) {
        try {
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(new DeviceIdentifier(deviceId,
                    DoorManagerConstants.DEVICE_TYPE))) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            String sensorState = state.toUpperCase();
            if (!sensorState.equals(DoorManagerConstants.STATE_BULB_ON) && !sensorState.equals(DoorManagerConstants.STATE_BULB_OFF)){
                log.error("The requested state change should be either - 'ON' or 'OFF'");
                return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).build();
            }

            String publishTopic = APIUtil.getAuthenticatedUserTenantDomain()
                    + "/" + DoorManagerConstants.DEVICE_TYPE + "/" + deviceId + DoorManagerConstants.MQTT_TOPIC_FOR_FAN;
            Operation commandOp = new CommandOperation();
            commandOp.setCode("change-fan-state");
            commandOp.setType(Operation.Type.COMMAND);
            commandOp.setEnabled(true);
            commandOp.setPayLoad(sensorState);
            Properties props = new Properties();
            props.setProperty(DoorManagerConstants.MQTT_ADAPTER_TOPIC_PROPERTY_NAME, publishTopic);
            commandOp.setProperties(props);
            List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
            deviceIdentifiers.add(new DeviceIdentifier(deviceId, DoorManagerConstants.DEVICE_TYPE));
            APIUtil.getDeviceManagementService().addOperation(DoorManagerConstants.DEVICE_TYPE, commandOp, deviceIdentifiers);
            return Response.ok().build();
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (OperationManagementException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path("device/assign-user/{deviceId}")
    @POST
    @Feature(code = "assign-user", name = "Assign new user to lock",
            description = "Add new access card to user to control the lock ")
    public Response assignUserToLock(@QueryParam("cardNumber") String cardNumber,
                                     @QueryParam("userName") String userName,
                                     @QueryParam("emailAddress") String emailAddress,
                                     @PathParam("deviceId") String deviceId) {
            try {
                if (userName != null && cardNumber != null) {
                    UserStoreManager userStoreManager = APIUtil.getUserStoreManager();
                    DoorLockSafe doorLockSafe = new DoorLockSafe();
                    String applicationUsername = null;
                    if (userStoreManager.isExistingUser(userName)) {
                        if (apiApplicationKey == null) {
                            applicationUsername = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                                    .getRealmConfiguration().getAdminUserName();
                            applicationUsername = applicationUsername + "@" + APIUtil.getAuthenticatedUserTenantDomain();
                            APIManagementProviderService apiManagementProviderService
                                    = APIUtil.getAPIManagementProviderService();
                            String[] tags = {DoorManagerConstants.DEVICE_TYPE};
                            apiApplicationKey = apiManagementProviderService.generateAndRetrieveApplicationKeys(
                                    DoorManagerConstants.DEVICE_TYPE, tags, KEY_TYPE, applicationUsername, true);
                        }
                        JWTClient jwtClient = APIUtil.getJWTClientManagerService().getJWTClient();
                        String scopes = DoorManagerConstants.DEVICE_SCOPE_INFO_DEVICE_TYPE
                                + DoorManagerConstants.DEVICE_TYPE + DoorManagerConstants.DEVICE_SCOPE_INFO_DEVICE_ID
                                + deviceId;
                        AccessTokenInfo accessTokenInfo = jwtClient.getAccessToken(apiApplicationKey.getConsumerKey(),
                                apiApplicationKey.getConsumerSecret(), APIUtil.getAuthenticatedUser() + "@"
                                        + APIUtil.getAuthenticatedUserTenantDomain(), scopes);
                        String accessToken = accessTokenInfo.getAccessToken();
                        if (accessToken == null) {
                            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
                        }
                        doorLockSafe.setAccessToken(accessTokenInfo.getAccessToken());
                        doorLockSafe.setRefreshToken(accessTokenInfo.getRefreshToken());
                        doorLockSafe.setDeviceId(deviceId);
                        doorLockSafe.setOwner(applicationUsername);
                        doorLockSafe.setEmailAddress(emailAddress);
                        doorLockSafe.setUIDofUser(cardNumber);
                        doorLockSafe.setSerialNumber(deviceId);
                        //DoorManagerManager doorManager = APIUtil.getDoorManagerManagerService();
                        if (doorManagerManager.assignUserToLock(doorLockSafe)) {
                            return Response.ok().build();
                        } else {
                            return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).build();
                        }
                    } else {
                        return Response.status(Response.Status.NOT_FOUND.getStatusCode()).build();
                    }
                } else {
                    return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).build();
                }
                } catch (UserStoreException e) {
                    return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
                } catch (JWTClientException e) {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
                } catch (APIManagerException e) {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
                } catch (DoorManagerDeviceMgtPluginException e) {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
                }
    }


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
    public Response getSensorStats(@PathParam("deviceId") String deviceId, @QueryParam("from") long from,
                                   @QueryParam("to") long to, @QueryParam("sensorType") String sensorType) {
        String fromDate = String.valueOf(from);
        String toDate = String.valueOf(to);
        String query = DoorManagerConstants.DEVICE_META_INFO_DEVICE_ID + deviceId + " AND " + DoorManagerConstants.DEVICE_META_INFO_DEVICE_TYPE +
                DoorManagerConstants.DEVICE_TYPE + " AND "+ DoorManagerConstants.DEVICE_META_INFO_TIME +" : [" + fromDate + " TO " + toDate + "]";
        String sensorTableName = null;
        if(sensorType.equals(DoorManagerConstants.SENSOR_TYPE1)){
            sensorTableName = DoorManagerConstants.SENSOR_TYPE1_EVENT_TABLE;
        }else if(sensorType.equals(DoorManagerConstants.SENSOR_TYPE2)){
            sensorTableName = DoorManagerConstants.SENSOR_TYPE2_EVENT_TABLE;
        }
        try {
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(new DeviceIdentifier(deviceId,
                    DoorManagerConstants.DEVICE_TYPE))) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            if (sensorTableName != null) {
                List<SortByField> sortByFields = new ArrayList<>();
                SortByField sortByField = new SortByField(DoorManagerConstants.DEVICE_META_INFO_TIME, SORT.ASC, false);
                sortByFields.add(sortByField);
                List<SensorRecord> sensorRecords = APIUtil.getAllEventsForDevice(sensorTableName, query, sortByFields);
                return Response.status(Response.Status.OK.getStatusCode()).entity(sensorRecords).build();
            }
        } catch (AnalyticsException e) {
            String errorMsg = "Error on retrieving stats on table " + sensorTableName + " with query " + query;
            log.error(errorMsg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(errorMsg).build();
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    /**
     * Remove device type instance using device id
     * @param deviceId  unique identifier for given device type instance
     */
    @Path("/device/{deviceId}")
    @DELETE
    public Response removeDevice(@PathParam("deviceId") String deviceId) {
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(deviceId);
            deviceIdentifier.setType(DoorManagerConstants.DEVICE_TYPE);
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(deviceIdentifier)) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            boolean removed = APIUtil.getDeviceManagementService().disenrollDevice(
                    deviceIdentifier);
            if (removed) {
                return Response.ok().build();
            } else {
                return Response.status(Response.Status.NOT_ACCEPTABLE.getStatusCode()).build();
            }
        } catch (DeviceManagementException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    /**
     * Update device instance name
     * @param deviceId  unique identifier for given device type instance
     * @param name      new name for the device type instance
     */
    @Path("/device/{deviceId}")
    @PUT
    public Response updateDevice(@PathParam("deviceId") String deviceId, @QueryParam("name") String name) {
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(deviceId);
            deviceIdentifier.setType(DoorManagerConstants.DEVICE_TYPE);
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(deviceIdentifier)) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            Device device = APIUtil.getDeviceManagementService().getDevice(deviceIdentifier);
            device.setDeviceIdentifier(deviceId);
            device.getEnrolmentInfo().setDateOfLastUpdate(new Date().getTime());
            device.setName(name);
            device.setType(DoorManagerConstants.DEVICE_TYPE);
            boolean updated = APIUtil.getDeviceManagementService().modifyEnrollment(device);
            if (updated) {
                return Response.ok().build();
            } else {
                return Response.status(Response.Status.NOT_ACCEPTABLE.getStatusCode()).build();
            }
        } catch (DeviceManagementException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    /**
     * To get device information
     * @param deviceId  unique identifier for given device type instance
     * @return return Device object which carries all information related to a managed device
     */
    @Path("/device/{deviceId}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDevice(@PathParam("deviceId") String deviceId) {
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(deviceId);
            deviceIdentifier.setType(DoorManagerConstants.DEVICE_TYPE);
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(deviceIdentifier)) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            Device device = APIUtil.getDeviceManagementService().getDevice(deviceIdentifier);
            return Response.ok().entity(device).build();
        } catch (DeviceManagementException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    /**
     * Get all device type instance which belongs to user
     * @return  Array of devices which includes device's information
     */
    @Path("/devices")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllDevices() {
        try {
            List<Device> userDevices =
                    APIUtil.getDeviceManagementService().getDevicesOfUser(APIUtil.getAuthenticatedUser());
            ArrayList<Device> deviceList = new ArrayList<>();
            for (Device device : userDevices) {
                if (device.getType().equals(DoorManagerConstants.DEVICE_TYPE) &&
                        device.getEnrolmentInfo().getStatus().equals(EnrolmentInfo.Status.ACTIVE)) {
                    deviceList.add(device);
                }
            }
            Device[] devices = deviceList.toArray(new Device[]{});
            return Response.ok().entity(devices).build();
        } catch (DeviceManagementException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    /**
     * To download device type agent source code as zip file
     * @param deviceName   name for the device type instance
     * @param sketchType   folder name where device type agent was installed into server
     * @return  Agent source code as zip file
     */
    @Path("/device/download")
    @GET
    @Produces("application/zip")
    public Response downloadSketch(@QueryParam("deviceName") String deviceName,
                                   @QueryParam("sketchType") String sketchType) {
        try {
            ZipArchive zipFile = createDownloadFile(APIUtil.getAuthenticatedUser(), deviceName, sketchType);
            Response.ResponseBuilder response = Response.ok(FileUtils.readFileToByteArray(zipFile.getZipFile()));
            response.status(Response.Status.OK);
            response.type("application/zip");
            response.header("Content-Disposition", "attachment; filename=\"" + zipFile.getFileName() + "\"");
            Response requestedFile = response.build();
            File file = zipFile.getZipFile();
            if (!file.delete()) {
                String message = file.exists() ? "is in use by another process" : "does not exist " + sketchType;
                throw new IOException("Cannot delete file, because file " + message + ".");
            } else {
                return requestedFile;
            }
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage()).build();
        } catch (DeviceManagementException ex) {
            log.error(ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        } catch (JWTClientException ex) {
            log.error(ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        } catch (APIManagerException ex) {
            log.error(ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        } catch (UserStoreException ex) {
            log.error(ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }

    /**
     * Register device into device management service
     * @param deviceId unique identifier for given device type instance
     * @param name  name for the device type instance
     * @return check whether device is installed into cdmf
     */
    private boolean register(String deviceId, String name) {
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(deviceId);
            deviceIdentifier.setType(DoorManagerConstants.DEVICE_TYPE);
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
            device.setType(DoorManagerConstants.DEVICE_TYPE);
            enrolmentInfo.setOwner(APIUtil.getAuthenticatedUser());
            device.setEnrolmentInfo(enrolmentInfo);
            boolean added = APIUtil.getDeviceManagementService().enrollDevice(device);
            if (added) {
                APIUtil.registerApiAccessRoles(APIUtil.getAuthenticatedUser());
            }
            return added;
        } catch (DeviceManagementException e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    private ZipArchive createDownloadFile(String owner, String deviceName, String sketchType)
            throws DeviceManagementException, JWTClientException, APIManagerException,
            UserStoreException {
        //create new device id
        String deviceId = shortUUID();
        if (apiApplicationKey == null) {
            String applicationUsername = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                    .getRealmConfiguration().getAdminUserName();
            applicationUsername = applicationUsername + "@" + APIUtil.getAuthenticatedUserTenantDomain();
            APIManagementProviderService apiManagementProviderService = APIUtil.getAPIManagementProviderService();
            String[] tags = {DoorManagerConstants.DEVICE_TYPE};
            apiApplicationKey = apiManagementProviderService.generateAndRetrieveApplicationKeys(
                    DoorManagerConstants.DEVICE_TYPE, tags, KEY_TYPE, applicationUsername, true);
        }
        JWTClient jwtClient = APIUtil.getJWTClientManagerService().getJWTClient();
        String scopes = DoorManagerConstants.DEVICE_SCOPE_INFO_DEVICE_TYPE
                + DoorManagerConstants.DEVICE_TYPE + DoorManagerConstants.DEVICE_SCOPE_INFO_DEVICE_ID
                + deviceId;
        AccessTokenInfo accessTokenInfo = jwtClient.getAccessToken(apiApplicationKey.getConsumerKey(),
                apiApplicationKey.getConsumerSecret(), owner + "@" + APIUtil.getAuthenticatedUserTenantDomain(), scopes);
        String accessToken = accessTokenInfo.getAccessToken();
        String refreshToken = accessTokenInfo.getRefreshToken();
        boolean status = register(deviceId, deviceName);
        if (!status) {
            String msg = "Error occurred while registering the device with " + "id: " + deviceId + " owner:" + owner;
            throw new DeviceManagementException(msg);
        }
        ZipUtil ziputil = new ZipUtil();
        return ziputil.createZipFile(owner, APIUtil.getTenantDomainOftheUser(), sketchType,
                deviceId, deviceName, accessToken, refreshToken, apiApplicationKey.toString());
    }

    /**
     * @param userInfo user information which are required to test given user is authorized to open requested door
     * @return if user is authorized open the the door allow to open it
     */
    @POST
    @Path("/device/is-user-authorized")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @SuppressWarnings("unchecked") //This is to avoid unchecked call to put(k, v) into jsonObject. org.json.simple
    // library uses raw type collections internally.
    public Response isUserAuthorized(final UserInfo userInfo) {
        if (userInfo.cardNumber != null && userInfo.deviceId != null) {
            try {
                DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
                deviceIdentifier.setId(userInfo.deviceId);
                deviceIdentifier.setType(DoorManagerConstants.DEVICE_TYPE);
                if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(deviceIdentifier)) {
                    return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
                }
                if (doorManagerManager.checkCardDoorAssociation(userInfo.cardNumber, userInfo.deviceId)) {
                    return Response.status(Response.Status.OK).build();
                } else {
                    return Response.status(Response.Status.UNAUTHORIZED).build();
                }
            }  catch (DeviceAccessAuthorizationException e) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            } catch (DoorManagerDeviceMgtPluginException e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }


   /* private void sendCEPEvent(String deviceId, String cardId, boolean accessStatus){
        String cepEventReciever = "http://localhost:9768/endpoints/LockEventReciever";

		HttpClient httpClient = new SystemDefaultHttpClient();
		HttpPost method = new HttpPost(cepEventReciever);
		JsonObject event = new JsonObject();
		JsonObject metaData = new JsonObject();

		metaData.addProperty("deviceID", deviceId);
		metaData.addProperty("cardID", cardId);

		event.add("metaData", metaData);

		String eventString = "{\"event\": " + event + "}";

		try {
			StringEntity entity = new StringEntity(eventString);
			method.setEntity(entity);
			if (cepEventReciever.startsWith("https")) {
				method.setHeader("Authorization", "Basic " + Base64.encode(("admin" + ":" + "admin").getBytes()));
			}
			httpClient.execute(method).getEntity().getContent().close();
		} catch (UnsupportedEncodingException e) {
			log.error("Error while constituting CEP event"+ e.getMessage());
		} catch (ClientProtocolException e) {
			log.error("Error while sending message to CEP "+ e.getMessage());
		} catch (IOException e) {
			log.error("Error while sending message to CEP "+ e.getMessage());
		}
	}*/
}