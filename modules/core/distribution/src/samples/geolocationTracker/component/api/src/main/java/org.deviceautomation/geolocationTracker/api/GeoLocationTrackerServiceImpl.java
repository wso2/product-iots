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

package org.deviceautomation.geolocationTracker.api;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.deviceautomation.geolocationTracker.api.dto.DeviceJSON;
import org.deviceautomation.geolocationTracker.api.dto.SensorRecord;
import org.deviceautomation.geolocationTracker.api.util.APIUtil;
import org.deviceautomation.geolocationTracker.api.util.Constants;
import org.deviceautomation.geolocationTracker.api.util.ZipUtil;
import org.deviceautomation.geolocationTracker.plugin.constants.GeoLocationTrackerConstants;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.SORT;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.SortByField;
import org.wso2.carbon.analytics.dataservice.commons.exception.AnalyticsIndexException;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceUtils;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.application.extension.APIManagementProviderService;
import org.wso2.carbon.apimgt.application.extension.dto.ApiApplicationKey;
import org.wso2.carbon.apimgt.application.extension.exception.APIManagerException;
import org.wso2.carbon.context.CarbonContext;
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

import javax.servlet.http.HttpServletResponse;

import javax.ws.rs.core.Context;
import javax.ws.rs.PathParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.DELETE;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.deviceautomation.geolocationTracker.api.util.APIUtil.getAnalyticsDataAPI;

/**
 * This is the API which is used to control and manage device type functionality
 */
@SuppressWarnings("NonJaxWsWebServices")
@API(name = "geolocationTracker", version = "1.0.0", context = "/geolocationTracker", tags = "geolocationTracker")
@DeviceType(value = "geolocationTracker")
public class GeoLocationTrackerServiceImpl implements GeoLocationTrackerService {

    private static final String KEY_TYPE = "PRODUCTION";
    private static Log log = LogFactory.getLog(GeoLocationTrackerService.class);
    private static volatile ApiApplicationKey apiApplicationKey;
    private ConcurrentHashMap<String, DeviceJSON> deviceToIpMap = new ConcurrentHashMap<>();

    private static String shortUUID() {
        UUID uuid = UUID.randomUUID();
        long l = ByteBuffer.wrap(uuid.toString().getBytes(StandardCharsets.UTF_8)).getLong();
        return Long.toString(l, Character.MAX_RADIX);
    }

    /**
     * This will store devices currently connected with iot server
     *
     * @param agentInfo device owner,id
     * @return true if device instance is added to map
     */
    @Path("device/register")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerDevice(final DeviceJSON agentInfo) {
        String deviceId = agentInfo.deviceId;
        if ((agentInfo.deviceId != null) && (agentInfo.owner != null)) {
            deviceToIpMap.put(deviceId, agentInfo);
            return Response.status(Response.Status.OK).build();
        }
        return Response.status(Response.Status.NOT_ACCEPTABLE).build();
    }

    /**
     * Set the name of the vehicle which this is installed
     *
     * @param deviceId    unique identifier for given device type instance
     * @param vehicleType name of this device instance
     * @return if message is passed to MQTT broker this will trigger ok with status code: 200 otherwise will return
     * respective error message
     */
    @Path("device/{deviceId}/set-vehicle-type")
    @POST
    @Feature(code = "set-vehicle-type", name = "Set vehicle type",
            description = "Set the name of the vehicle which this is installed")
    public Response setVehicleType(@PathParam("deviceId") String deviceId,
                                   @QueryParam("vehicleType") String vehicleType,
                                   @Context HttpServletResponse response) {
        try {
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(new DeviceIdentifier(deviceId,
                    GeoLocationTrackerConstants.DEVICE_TYPE))) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            String publishTopic = APIUtil.getAuthenticatedUserTenantDomain()
                    + "/" + GeoLocationTrackerConstants.DEVICE_TYPE + "/" + deviceId + "/"
                    + GeoLocationTrackerConstants.MQTT_TOPIC_ENDPOINT;
            Operation commandOp = new CommandOperation();
            commandOp.setCode("set-vehicle-type");
            commandOp.setType(Operation.Type.COMMAND);
            commandOp.setEnabled(true);
            commandOp.setPayLoad(vehicleType);
            Properties props = new Properties();
            props.setProperty(GeoLocationTrackerConstants.MQTT_ADAPTER_TOPIC_PROPERTY_NAME, publishTopic);
            commandOp.setProperties(props);
            List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
            deviceIdentifiers.add(new DeviceIdentifier(deviceId, GeoLocationTrackerConstants.DEVICE_TYPE));
            APIUtil.getDeviceManagementService().addOperation(GeoLocationTrackerConstants.DEVICE_TYPE, commandOp, deviceIdentifiers);
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
     * Retrieve Sensor data for the given time period
     *
     * @param deviceId unique identifier for given device type instance
     * @param from     starting time
     * @param to       ending time
     * @return response with List<SensorRecord> object which includes sensor data which is requested
     */
    @Path("device/stats/{deviceId}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getDeviceStats(@PathParam("deviceId") String deviceId, @QueryParam("from") long from,
                                   @QueryParam("to") long to) {
        String fromDate = String.valueOf(from);
        String toDate = String.valueOf(to);
        String query = GeoLocationTrackerConstants.DEVICE_META_INFO_DEVICE_ID + deviceId + " AND "
                + GeoLocationTrackerConstants.DEVICE_META_INFO_DEVICE_TYPE
                + GeoLocationTrackerConstants.DEVICE_TYPE + " AND " + GeoLocationTrackerConstants.DEVICE_META_INFO_TIME
                + " : [" + fromDate + " TO " + toDate + "]";
        String geolocationEventTable = GeoLocationTrackerConstants.GEOLOCATION_EVENT_TABLE;
        try {
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(new DeviceIdentifier(deviceId,
                    GeoLocationTrackerConstants.DEVICE_TYPE))) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            List<SortByField> sortByFields = new ArrayList<>();
            SortByField sortByField = new SortByField(GeoLocationTrackerConstants.DEVICE_META_INFO_TIME, SORT.ASC, true);
            sortByFields.add(sortByField);
            List<SensorRecord> sensorRecords = APIUtil.getAllEventsForDevice(geolocationEventTable, query, sortByFields);
            return Response.status(Response.Status.OK.getStatusCode()).entity(sensorRecords).build();
        } catch (AnalyticsException e) {
            String errorMsg = "Error on retrieving stats on table " + geolocationEventTable + " with query " + query;
            log.error(errorMsg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(errorMsg).build();
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Remove device type instance using device id
     *
     * @param deviceId unique identifier for given device type instance
     */
    @Path("/device/{deviceId}")
    @DELETE
    public Response removeDevice(@PathParam("deviceId") String deviceId) {
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(deviceId);
            deviceIdentifier.setType(GeoLocationTrackerConstants.DEVICE_TYPE);
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
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    /**
     * Update device instance name
     *
     * @param deviceId unique identifier for given device type instance
     * @param name     new name for the device type instance
     */
    @Path("/device/{deviceId}")
    @PUT
    public Response updateDevice(@PathParam("deviceId") String deviceId, @QueryParam("name") String name) {
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(deviceId);
            deviceIdentifier.setType(GeoLocationTrackerConstants.DEVICE_TYPE);
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(deviceIdentifier)) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            Device device = APIUtil.getDeviceManagementService().getDevice(deviceIdentifier);
            device.setDeviceIdentifier(deviceId);
            device.getEnrolmentInfo().setDateOfLastUpdate(new Date().getTime());
            device.setName(name);
            device.setType(GeoLocationTrackerConstants.DEVICE_TYPE);
            boolean updated = APIUtil.getDeviceManagementService().modifyEnrollment(device);
            if (updated) {
                return Response.ok().build();
            } else {
                return Response.status(Response.Status.NOT_ACCEPTABLE.getStatusCode()).build();
            }
        } catch (DeviceManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    /**
     * To get device information
     *
     * @param deviceId unique identifier for given device type instance
     * @return Device object which includes device specific information
     */
    @Path("/device/{deviceId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDevice(@PathParam("deviceId") String deviceId) {
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(deviceId);
            deviceIdentifier.setType(GeoLocationTrackerConstants.DEVICE_TYPE);
            if (!APIUtil.getDeviceAccessAuthorizationService().isUserAuthorized(deviceIdentifier)) {
                return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
            }
            Device device = APIUtil.getDeviceManagementService().getDevice(deviceIdentifier);
            return Response.ok().entity(device).build();
        } catch (DeviceManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        } catch (DeviceAccessAuthorizationException e) {
            log.error(e.getErrorMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    /**
     * Get all device type instance which belongs to user
     *
     * @return Array of devices which includes device's information
     */
    @Path("/devices")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllDevices() {
        try {
            List<Device> userDevices =
                    APIUtil.getDeviceManagementService().getDevicesOfUser(APIUtil.getAuthenticatedUser());
            ArrayList<Device> devicesList = new ArrayList<>();
            for (Device device : userDevices) {
                if (device.getType().equals(GeoLocationTrackerConstants.DEVICE_TYPE) &&
                        device.getEnrolmentInfo().getStatus().equals(EnrolmentInfo.Status.ACTIVE)) {
                    devicesList.add(device);
                }
            }
            Device[] devices = devicesList.toArray(new Device[]{});
            return Response.ok().entity(devices).build();
        } catch (DeviceManagementException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    /**
     * To download device type agent source code as zip file
     *
     * @param deviceName name for the device type instance
     * @param sketchType folder name where device type agent was installed into server
     * @return Agent source code as zip file
     */
    @Path("/device/download")
    @GET
    @Produces(Constants.APPLICATION_ZIP)
    public Response downloadSketch(@QueryParam("deviceName") String deviceName,
                                   @QueryParam("sketchType") String sketchType) {
        try {
            ZipArchive zipFile = createDownloadFile(APIUtil.getAuthenticatedUser(), deviceName, sketchType);
            Response.ResponseBuilder response = Response.ok(FileUtils.readFileToByteArray(zipFile.getZipFile()));
            response.status(Response.Status.OK);
            response.type(Constants.APPLICATION_ZIP);
            response.header(Constants.CONTENT_DISPOSITION, Constants.ATTACHMENT_FILENAME + zipFile.getFileName() + "\"");
            Response requestedFile = response.build();
            File file = zipFile.getZipFile();
            if (!file.delete()) {
                String message = file.exists() ? "is in use by another process" : "does not exist";
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
     *
     * @param deviceId unique identifier for given device type instance
     * @param name     name for the device type instance
     * @return check whether device is installed into cdmf
     */
    private boolean register(String deviceId, String name) {
        try {
            DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(deviceId);
            deviceIdentifier.setType(GeoLocationTrackerConstants.DEVICE_TYPE);
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
            device.setType(GeoLocationTrackerConstants.DEVICE_TYPE);
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

    /**
     * This will retrieve stats from given table for given time range from Data Analytics Server(DAS)
     *
     * @param tableName datasource name
     * @param timeFrom  starting time
     * @param timeTo    ending time
     * @param count     maximum number of elements need to be retrieved
     * @return sensor data which were retrieved from DAS
     */
    @Path("device/stats/by-range")
    @GET
    @Produces({"application/json"})
    public Response getStatsByRange(@QueryParam("tableName") String tableName, @QueryParam("timeFrom") long timeFrom
            , @QueryParam("timeTo") long timeTo, @QueryParam("count") int count) {
        String fromDate = String.valueOf(timeFrom);
        String toDate = String.valueOf(timeTo);
        String query = GeoLocationTrackerConstants.DEVICE_META_INFO_TIME + " :[" + fromDate + " TO " + toDate + "]";
        int eventCount;
        List<SortByField> sortByFields = new ArrayList<>();
        SortByField sortByField = new SortByField(GeoLocationTrackerConstants.DEVICE_META_INFO_TIME, SORT.ASC, true);
        sortByFields.add(sortByField);
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            AnalyticsDataAPI analyticsDataAPI = getAnalyticsDataAPI();
            eventCount = analyticsDataAPI.searchCount(tenantId, tableName, query);
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode rootNode = mapper.createObjectNode();
            ArrayNode dataSet = mapper.createArrayNode();
            if (eventCount == 0) {
                rootNode.put(GeoLocationTrackerConstants.RESPONSE_STATUS, GeoLocationTrackerConstants.SUCCESS_MESSAGE);
                rootNode.put(GeoLocationTrackerConstants.RESPONSE_MESSAGE, dataSet);
                return Response.ok(rootNode.toString()).build();
            }
            if (eventCount > count) {
                eventCount = count;
            }
            List<SearchResultEntry> resultEntries = analyticsDataAPI.search(tenantId, tableName, query, 0, eventCount
                    , sortByFields);
            List<String> recordIds = getRecordIds(resultEntries);
            AnalyticsDataResponse response = analyticsDataAPI.get(tenantId, tableName, 1, null, recordIds);
            List<Record> records = AnalyticsDataServiceUtils.listRecords(analyticsDataAPI, response);
            for (Object recodeObject : records) {
                Record record = (Record) recodeObject;
                Map<String, Object> row = record.getValues();
                Iterator<Map.Entry<String, Object>> entries = row.entrySet().iterator();
                ObjectNode sonsorData = mapper.createObjectNode();
                while (entries.hasNext()) {
                    Map.Entry<String, Object> entry = entries.next();
                    sonsorData.put(entry.getKey(), "" + entry.getValue());
                }
                dataSet.add(sonsorData);
            }
            rootNode.put(GeoLocationTrackerConstants.RESPONSE_STATUS, GeoLocationTrackerConstants.SUCCESS_MESSAGE);
            rootNode.put(GeoLocationTrackerConstants.RESPONSE_MESSAGE, dataSet);
            return Response.ok(rootNode.toString()).build();
        } catch (AnalyticsIndexException e) {
            String errorMsg = "Error on retrieving stats on table " + tableName + " with query " + query;
            log.error(errorMsg);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).entity(errorMsg).build();
        } catch (AnalyticsException e) {
            log.error("Error while retrieving data from DAS, " + e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private List<String> getRecordIds(List<SearchResultEntry> searchResults) {
        ArrayList<String> ids = new ArrayList<>();
        for (SearchResultEntry searchResult : searchResults) {
            ids.add(searchResult.getId());
        }
        return ids;
    }

    /**
     * Get schema definition of given datasource
     *
     * @param dataSource data source name
     * @return schema definition
     */
    @Path("device/stats/schema")
    @GET
    @Produces({"application/json"})
    public Response getSchema(@QueryParam("dataSource") String dataSource) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        ObjectNode columnsNodes = mapper.createObjectNode();
        ArrayNode primaryKeyNodes = mapper.createArrayNode();
        ObjectNode dataSourcesList = mapper.createObjectNode();
        Response tenantId;
        try {
            int tenantId1 = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            AnalyticsDataAPI analyticsDataAPI = getAnalyticsDataAPI();
            AnalyticsSchema tableSchema = analyticsDataAPI.getTableSchema(tenantId1, dataSource);
            Map<String, ColumnDefinition> columns = tableSchema.getColumns();
            if (columns == null) {
                log.error("Table doesn't contains eny columns");
                rootNode.put(GeoLocationTrackerConstants.RESPONSE_STATUS, GeoLocationTrackerConstants.FAILURE_MESSAGE);
                rootNode.put(GeoLocationTrackerConstants.RESPONSE_MESSAGE, "Table doesn't contains eny columns");
                return Response.status(Response.Status.NO_CONTENT).entity(rootNode).build();
            }
            for (String column : columns.keySet()) {
                ObjectNode key = mapper.createObjectNode();
                ColumnDefinition columnDefinition = columns.get(column);
                key.put(GeoLocationTrackerConstants.SCHEMA_DEFINITION_COLUMN_NAME, columnDefinition.getName());
                key.put(GeoLocationTrackerConstants.SCHEMA_DEFINITION_COLUMN_TYPE
                        , columnDefinition.getType().toString());
                key.put(GeoLocationTrackerConstants.SCHEMA_DEFINITION_COLUMN_INDEXED, columnDefinition.isIndexed());
                key.put(GeoLocationTrackerConstants.SCHEMA_DEFINITION_COLUMN_SCOREPARAM
                        , columnDefinition.isScoreParam());
                dataSourcesList.put(column, key);
            }
            List<String> primaryKeys = tableSchema.getPrimaryKeys();
            for (String primaryKey : primaryKeys) {
                primaryKeyNodes.add(primaryKey);
            }
            rootNode.put(GeoLocationTrackerConstants.RESPONSE_STATUS, GeoLocationTrackerConstants.SUCCESS_MESSAGE);
            columnsNodes.put(GeoLocationTrackerConstants.SCHEMA_DEFINITION_COLUMN_PRIMARYKEYS, primaryKeyNodes);
            columnsNodes.put(GeoLocationTrackerConstants.SCHEMA_DEFINITION_COLUMNS, dataSourcesList);
            rootNode.put(GeoLocationTrackerConstants.RESPONSE_MESSAGE, columnsNodes);
            return Response.ok(rootNode.toString()).build();
        } catch (AnalyticsException e) {
            log.error("Error while retrieving data from DAS, " + e);
            rootNode.put(GeoLocationTrackerConstants.RESPONSE_STATUS, GeoLocationTrackerConstants.FAILURE_MESSAGE);
            rootNode.put(GeoLocationTrackerConstants.RESPONSE_MESSAGE, e.getMessage());
            tenantId = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(rootNode).build();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return tenantId;
    }

    private ZipArchive createDownloadFile(String owner, String deviceName, String sketchType)
            throws DeviceManagementException, JWTClientException, APIManagerException, UserStoreException {
        String deviceId = shortUUID();
        if (apiApplicationKey == null) {
            String applicationUsername = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm()
                    .getRealmConfiguration().getAdminUserName();
            applicationUsername = applicationUsername + "@" + APIUtil.getAuthenticatedUserTenantDomain();
            APIManagementProviderService apiManagementProviderService = APIUtil.getAPIManagementProviderService();
            String[] tags = {GeoLocationTrackerConstants.DEVICE_TYPE};
            apiApplicationKey = apiManagementProviderService.generateAndRetrieveApplicationKeys(
                    GeoLocationTrackerConstants.DEVICE_TYPE, tags, KEY_TYPE, applicationUsername, true);
        }
        JWTClient jwtClient = APIUtil.getJWTClientManagerService().getJWTClient();
        String scopes = "cdmf/" + GeoLocationTrackerConstants.DEVICE_TYPE + "/" + deviceId;
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
}