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

package org.wso2.connectedlap.api;

import org.wso2.connectedlap.api.dto.DeviceJSON;
import org.wso2.connectedlap.api.dto.SensorRecord;
import org.wso2.connectedlap.api.util.APIUtil;
import org.wso2.connectedlap.api.util.ZipUtil;
import org.wso2.connectedlap.plugin.constants.DeviceTypeConstants;
import org.wso2.connectedlap.api.DeviceTypeService;
import org.wso2.connectedlap.plugin.impl.uti.ConnectedLapResponse;
import org.wso2.connectedlap.plugin.impl.util.ConnectedLapDevice;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.DeviceType;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.Feature;
import org.wso2.carbon.device.mgt.iot.util.ZipArchive;
import org.wso2.carbon.identity.jwt.client.extension.JWTClient;
import org.wso2.carbon.identity.jwt.client.extension.dto.AccessTokenInfo;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.user.api.UserStoreException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import java.util.UUID;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;


/**
 * This is the API which is used to control and manage device type functionality
 */
@SuppressWarnings("NonJaxWsWebServices")
@API(name = "CONNECTEDLAP", version = "1.0.0", context = "/CONNECTEDLAP", tags = "CONNECTEDLAP")
@DeviceType(value = "CONNECTEDLAP")
public class DeviceTypeServiceImpl implements DeviceTypeService {

    private static final String KEY_TYPE = "PRODUCTION";
    private static Log log = LogFactory.getLog(DeviceTypeService.class);
    private static ApiApplicationKey apiApplicationKey;
    private ConcurrentHashMap<String, DeviceJSON> deviceToIpMap = new ConcurrentHashMap<>();

    /**
     * Retreive device metadata data
     * This data inclueds.
     * 1.) Device owner
     * 2.) Device Id(Mac address)
     * 3.) Device Name
     * 4.) Device disc space
     * 5.) Device memory
     * 6.) Device cpuinfo
     * 7.) Device network type
     * 8.) Number of cpus.
     * */
    @Path("device/register")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerDevice(final DeviceJSON agentInfo) {
        if (agentInfo.deviceId != null) {

            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(agentInfo.deviceId);
            deviceIdentifier.setType(DeviceTypeConstants.DEVICE_TYPE);

            try {
                //implement this part
                if (APIUtil.getDeviceManagementService().isEnrolled(deviceIdentifier)) {
                    //add code if needed.
                    return Response.status(Response.Status.OK.getStatusCode()).entity(new String("Device Already Enrolled").toString())
                     .build();
                }

                Device device = new Device();
                device.setDeviceIdentifier(agentInfo.deviceId);
                EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
                enrolmentInfo.setDateOfEnrolment(new Date().getTime());
                enrolmentInfo.setDateOfLastUpdate(new Date().getTime());
                enrolmentInfo.setStatus(EnrolmentInfo.Status.ACTIVE);
                enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
                device.setName(agentInfo.deviceName);
                device.setType(DeviceTypeConstants.DEVICE_TYPE);
                enrolmentInfo.setOwner(APIUtil.getAuthenticatedUser());
                device.setEnrolmentInfo(enrolmentInfo);

                boolean added = APIUtil.getDeviceManagementService().enrollDevice(device);

                if (added) {
                    APIUtil.registerApiAccessRoles(APIUtil.getAuthenticatedUser());
                    ConnectedLapDevice connectedLapDevice = new ConnectedLapDevice(agentInfo.deviceId, agentInfo.discSpace, agentInfo.memory, agentInfo.cpuInfo, agentInfo.networkType, agentInfo.cpucorecount);
                    //TODO:add validation in here
                    APIUtil.getDeviceDataManagerService().getConnectedLapDatManager().addDeviceInfo(connectedLapDevice);

                    if (apiApplicationKey == null) {
                        String applicationUsername = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                                .getRealmConfiguration().getAdminUserName();
                        applicationUsername = applicationUsername + "@" + APIUtil.getAuthenticatedUserTenantDomain();
                        APIManagementProviderService apiManagementProviderService = APIUtil.getAPIManagementProviderService();
                        String[] tags = {DeviceTypeConstants.DEVICE_TYPE};
                        apiApplicationKey = apiManagementProviderService.generateAndRetrieveApplicationKeys(
                                DeviceTypeConstants.DEVICE_TYPE, tags, KEY_TYPE, applicationUsername, true);
                    }
                    JWTClient jwtClient = APIUtil.getJWTClientManagerService().getJWTClient();
                    String scopes = "device_type_" + DeviceTypeConstants.DEVICE_TYPE + " device_" + agentInfo.deviceId;
                    AccessTokenInfo accessTokenInfo = jwtClient.getAccessToken(apiApplicationKey.getConsumerKey(),
                            apiApplicationKey.getConsumerSecret(), agentInfo.owner + "@" + APIUtil.getAuthenticatedUserTenantDomain(), scopes);

                    //create token
                    String accessToken = accessTokenInfo.getAccessToken();
                    String refreshToken = accessTokenInfo.getRefreshToken();

                    ConnectedLapResponse connectedLapResponse = new ConnectedLapResponse(accessToken, refreshToken);
                    return Response.ok(connectedLapResponse, MediaType.APPLICATION_JSON).build();

                    //TODO: send the reresh token here
                    //return Response.ok().entity(device).build();
                    //return Response.status(Response.Status.OK).build();
                } else {
                    return Response.status(Response.Status.NOT_ACCEPTABLE.getStatusCode()).entity(false).build();
                }
            } catch (DeviceManagementException deviceManagementException) {
                log.error(deviceManagementException.getErrorMessage(), deviceManagementException);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(false).build();
            } catch (JWTClientException jwtClientException) {
                //log.error(jwtClientException.getErrorMessage(), jwtClientException);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(false).build();
            } catch (APIManagerException apiManagerException) {
                log.error(apiManagerException.getErrorMessage(), apiManagerException);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(false).build();
            } catch (UserStoreException userStoreException) {
                //log.error(userStoreException.getErrorMessage(), userStoreException);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(false).build();
            }
        } else {
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }
    }

    /**
     * Retrieve Sensor data for the given time period
     * @param deviceId unique identifier for given device type instance
     * @param from  starting time
     * @param to    ending time
     * @return  response with List<SensorRecord> object which includes sensor data which is requested
     */
    @Path("stats/{deviceId}/{sensorName}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getSensorStats(@PathParam("deviceId") String deviceId, @PathParam("sensorName") String sensor, @QueryParam("from") long from,
                                   @QueryParam("to") long to) {
        String fromDate = String.valueOf(from);
        String toDate = String.valueOf(to);
        String query = "deviceId:" + deviceId + " AND deviceType:" +
                DeviceTypeConstants.DEVICE_TYPE + " AND time : [" + fromDate + " TO " + toDate + "]";
        String sensorTableName = getSummeryTableName(sensor);//DeviceTypeConstants.SENSOR_EVENT_TABLE;
        try {
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(new DeviceIdentifier(deviceId,
                    DeviceTypeConstants.DEVICE_TYPE))) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            if (sensorTableName != null) {
                List<SortByField> sortByFields = new ArrayList<>();
                SortByField sortByField = new SortByField("time", SORT.ASC, false);
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
     * To get device information
     * @param deviceId  unique identifier for given device type instance
     * @return
     */
    @Path("/device/{deviceId}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDevice(@PathParam("deviceId") String deviceId) {
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(deviceId);
            deviceIdentifier.setType(DeviceTypeConstants.DEVICE_TYPE);
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
     * To download device type agent source code as zip file
     * @param deviceName   name for the device type instance
     * @param sketchType   folder name where device type agent was installed into server
     * @return  Agent source code as zip file
     * */

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
            Response resp = response.build();
            zipFile.getZipFile().delete();
            return resp;
         } catch (IllegalArgumentException ex) {
            return Response.status(400).entity(ex.getMessage()).build();//bad request
         } catch (DeviceManagementException ex) {
            log.error(ex.getMessage(), ex);
            return Response.status(500).entity(ex.getMessage()).build();
         } catch (JWTClientException ex) {
            log.error(ex.getMessage(), ex);
            return Response.status(500).entity(ex.getMessage()).build();
         } catch (APIManagerException ex) {
            log.error(ex.getMessage(), ex);
            return Response.status(500).entity(ex.getMessage()).build();
         } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            return Response.status(500).entity(ex.getMessage()).build();
         } catch (UserStoreException ex) {
            log.error(ex.getMessage(), ex);
            return Response.status(500).entity(ex.getMessage()).build();
         }
     }

     private static String shortUUID() {
         UUID uuid = UUID.randomUUID();
         long l = ByteBuffer.wrap(uuid.toString().getBytes(StandardCharsets.UTF_8)).getLong();
         return Long.toString(l, Character.MAX_RADIX);
     }

     private ZipArchive createDownloadFile(String owner, String deviceName, String sketchType)
            throws DeviceManagementException, JWTClientException, APIManagerException,
            UserStoreException {

         String deviceId = shortUUID();

         if (apiApplicationKey == null) {
            String applicationUsername = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                .getRealmConfiguration().getAdminUserName();
            applicationUsername = applicationUsername + "@" + APIUtil.getAuthenticatedUserTenantDomain();
            APIManagementProviderService apiManagementProviderService = APIUtil.getAPIManagementProviderService();
            String[] tags = {DeviceTypeConstants.DEVICE_TYPE};
            apiApplicationKey = apiManagementProviderService.generateAndRetrieveApplicationKeys(
            DeviceTypeConstants.DEVICE_TYPE, tags, KEY_TYPE, applicationUsername, true);
         }
         JWTClient jwtClient = APIUtil.getJWTClientManagerService().getJWTClient();
         String scopes = "device_type_" + DeviceTypeConstants.DEVICE_TYPE;
         AccessTokenInfo accessTokenInfo = jwtClient.getAccessToken(apiApplicationKey.getConsumerKey(),
         apiApplicationKey.getConsumerSecret(), owner + "@" + APIUtil.getAuthenticatedUserTenantDomain(), scopes);

         //create token
         String accessToken = accessTokenInfo.getAccessToken();
         String refreshToken = accessTokenInfo.getRefreshToken();

         //TODO: add this token info to file

         ZipUtil ziputil = new ZipUtil();
         ZipArchive zipFile = ziputil.createZipFile(owner, APIUtil.getTenantDomainOftheUser(), sketchType,
                 deviceId, deviceName, accessToken, refreshToken);
         return zipFile;
     }

    private String getSummeryTableName(String sensorName) {
        String summeryTableName;
        switch (sensorName) {
            case "battery" :
                return DeviceTypeConstants.DEVICE_BATTERY_STATS;
            case "charger":
                return DeviceTypeConstants.DEVICE_CHARGER_STATS;
            case "cpu":
                return DeviceTypeConstants.DEVICE_CPU_STATS;
            case "network":
                return DeviceTypeConstants.DEVICE_NETWORK_STATS;
            case "memory":
                return DeviceTypeConstants.DEVICE_MEMORY_STATS;
            case "harddisc":
                return DeviceTypeConstants.DEVICE_HARD_DISC_STATS;
            default:
               return null;
        }
        summeryTableName= summeryTableName.replaceAll("\\u200B","");
        return summeryTableName;
    }
}
