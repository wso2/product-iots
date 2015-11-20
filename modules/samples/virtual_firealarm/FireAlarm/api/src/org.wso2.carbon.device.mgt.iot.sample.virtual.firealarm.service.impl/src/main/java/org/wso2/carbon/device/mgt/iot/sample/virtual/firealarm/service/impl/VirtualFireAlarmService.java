/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.certificate.mgt.core.dto.SCEPResponse;
import org.wso2.carbon.certificate.mgt.core.exception.KeystoreException;
import org.wso2.carbon.certificate.mgt.core.service.CertificateManagementService;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.iot.common.DeviceManagement;
import org.wso2.carbon.device.mgt.iot.common.DeviceValidator;
import org.wso2.carbon.device.mgt.iot.common.apimgt.AccessTokenInfo;
import org.wso2.carbon.device.mgt.iot.common.apimgt.TokenClient;
import org.wso2.carbon.device.mgt.iot.common.controlqueue.xmpp.XmppAccount;
import org.wso2.carbon.device.mgt.iot.common.controlqueue.xmpp.XmppConfig;
import org.wso2.carbon.device.mgt.iot.common.controlqueue.xmpp.XmppServerClient;
import org.wso2.carbon.device.mgt.iot.common.exception.AccessTokenException;
import org.wso2.carbon.device.mgt.iot.common.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.common.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.common.sensormgt.SensorRecord;
import org.wso2.carbon.device.mgt.iot.common.util.ZipArchive;
import org.wso2.carbon.device.mgt.iot.common.util.ZipUtil;
import org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.plugin.constants.VirtualFireAlarmConstants;
import org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.service.impl.dto.DeviceJSON;
import org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.service.impl.exception.VirtualFireAlarmException;
import org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.service.impl.transport.VirtualFireAlarmMQTTSubscriber;
import org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.service.impl.transport.VirtualFireAlarmXMPPConnector;
import org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.service.impl.util.VerificationManager;
import org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.service.impl.util.VirtualFireAlarmServiceUtils;
import org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.service.impl.util.scep.ContentType;
import org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.service.impl.util.scep.SCEPOperation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class VirtualFireAlarmService {

    private static Log log = LogFactory.getLog(VirtualFireAlarmService.class);

    //TODO; replace this tenant domain
    private static final String SUPER_TENANT = "carbon.super";

    @Context  //injected response proxy supporting multiple thread
    private HttpServletResponse response;

    public static final String XMPP_PROTOCOL = "XMPP";
    public static final String HTTP_PROTOCOL = "HTTP";
    public static final String MQTT_PROTOCOL = "MQTT";

    private VerificationManager verificationManager;
    private VirtualFireAlarmMQTTSubscriber virtualFireAlarmMQTTSubscriber;
    private VirtualFireAlarmXMPPConnector virtualFireAlarmXMPPConnector;
    private ConcurrentHashMap<String, String> deviceToIpMap = new ConcurrentHashMap<>();


    public void setVerificationManager(
            VerificationManager verificationManager) {
        this.verificationManager = verificationManager;
        verificationManager.initVerificationManager();
    }

    public void setVirtualFireAlarmXMPPConnector(
            final VirtualFireAlarmXMPPConnector virtualFireAlarmXMPPConnector) {
        this.virtualFireAlarmXMPPConnector = virtualFireAlarmXMPPConnector;

        Runnable mqttStarter = new Runnable() {
            @Override
            public void run() {
                virtualFireAlarmXMPPConnector.initConnector();
                virtualFireAlarmXMPPConnector.connectAndLogin();
            }
        };

        Thread mqttStarterThread = new Thread(mqttStarter);
        mqttStarterThread.setDaemon(true);
        mqttStarterThread.start();
    }

    public void setVirtualFireAlarmMQTTSubscriber(
            final VirtualFireAlarmMQTTSubscriber virtualFireAlarmMQTTSubscriber) {
        this.virtualFireAlarmMQTTSubscriber = virtualFireAlarmMQTTSubscriber;

        Runnable xmppStarter = new Runnable() {
            @Override
            public void run() {
                virtualFireAlarmMQTTSubscriber.initConnector();
                virtualFireAlarmMQTTSubscriber.connectAndSubscribe();
            }
        };

        Thread xmppStarterThread = new Thread(xmppStarter);
        xmppStarterThread.setDaemon(true);
        xmppStarterThread.start();
    }

    public VerificationManager getVerificationManager() {
        return verificationManager;
    }

    public VirtualFireAlarmXMPPConnector getVirtualFireAlarmXMPPConnector() {
        return virtualFireAlarmXMPPConnector;
    }

    public VirtualFireAlarmMQTTSubscriber getVirtualFireAlarmMQTTSubscriber() {
        return virtualFireAlarmMQTTSubscriber;
    }

    /*	---------------------------------------------------------------------------------------
                                Device management specific APIs
             Also contains utility methods required for the execution of these APIs
         ---------------------------------------------------------------------------------------	*/
    @Path("manager/device/register")
    @PUT
    public boolean register(@QueryParam("deviceId") String deviceId,
                            @QueryParam("name") String name, @QueryParam("owner") String owner) {

        DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);

        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(VirtualFireAlarmConstants.DEVICE_TYPE);
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
            device.setType(VirtualFireAlarmConstants.DEVICE_TYPE);
            enrolmentInfo.setOwner(owner);
            device.setEnrolmentInfo(enrolmentInfo);
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
        } finally {
            deviceManagement.endTenantFlow();
        }
    }

    @Path("manager/device/remove/{device_id}")
    @DELETE
    public void removeDevice(@PathParam("device_id") String deviceId, @Context HttpServletResponse response) {

        DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(VirtualFireAlarmConstants.DEVICE_TYPE);
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

    @Path("manager/device/update/{device_id}")
    @POST
    public boolean updateDevice(@PathParam("device_id") String deviceId,
                                @QueryParam("name") String name,
                                @Context HttpServletResponse response) {

        DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);

        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(VirtualFireAlarmConstants.DEVICE_TYPE);
        try {
            Device device = deviceManagement.getDeviceManagementService().getDevice(
                    deviceIdentifier);
            device.setDeviceIdentifier(deviceId);

            // device.setDeviceTypeId(deviceTypeId);
            device.getEnrolmentInfo().setDateOfLastUpdate(new Date().getTime());

            device.setName(name);
            device.setType(VirtualFireAlarmConstants.DEVICE_TYPE);

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

    @Path("manager/device/{device_id}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Device getDevice(@PathParam("device_id") String deviceId) {

        DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);
        DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId(deviceId);
        deviceIdentifier.setType(VirtualFireAlarmConstants.DEVICE_TYPE);

        try {
            return deviceManagement.getDeviceManagementService().getDevice(deviceIdentifier);

        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return null;
        } finally {
            deviceManagement.endTenantFlow();
        }

    }

    @Path("manager/devices/{username}")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Device[] getFirealarmDevices(@PathParam("username") String username) {

        DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);

        try {
            List<Device> userDevices =
                    deviceManagement.getDeviceManagementService().getDevicesOfUser(
                            username);
            ArrayList<Device> userDevicesforFirealarm = new ArrayList<>();
            for (Device device : userDevices) {

                if (device.getType().equals(VirtualFireAlarmConstants.DEVICE_TYPE) &&
                        device.getEnrolmentInfo().getStatus().equals(
                                EnrolmentInfo.Status.ACTIVE)) {
                    userDevicesforFirealarm.add(device);

                }
            }

            return userDevicesforFirealarm.toArray(new Device[]{});
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return null;
        } finally {
            deviceManagement.endTenantFlow();
        }

    }

    @Path("manager/device/{sketch_type}/download")
    @GET
    @Produces("application/octet-stream")
    public Response downloadSketch(@QueryParam("owner") String owner,
                                   @QueryParam("deviceName") String customDeviceName,
                                   @PathParam("sketch_type") String sketchType) {
        //TODO:: null check customDeviceName at UI level
        try {
            ZipArchive zipFile = createDownloadFile(owner, customDeviceName, sketchType);
            Response.ResponseBuilder rb = Response.ok(zipFile.getZipFile());
            rb.header("Content-Disposition",
                      "attachment; filename=\"" + zipFile.getFileName() + "\"");
            return rb.build();
        } catch (IllegalArgumentException ex) {
            return Response.status(400).entity(ex.getMessage()).build();//bad request
        } catch (DeviceManagementException ex) {
            return Response.status(500).entity(ex.getMessage()).build();
        } catch (AccessTokenException ex) {
            return Response.status(500).entity(ex.getMessage()).build();
        } catch (DeviceControllerException ex) {
            return Response.status(500).entity(ex.getMessage()).build();
        }

    }

    @Path("manager/device/{sketch_type}/generate_link")
    @GET
    public Response generateSketchLink(@QueryParam("owner") String owner,
                                       @QueryParam("deviceName") String customDeviceName,
                                       @PathParam("sketch_type") String sketchType) {

        try {
            ZipArchive zipFile = createDownloadFile(owner, customDeviceName, sketchType);
            Response.ResponseBuilder rb = Response.ok(zipFile.getDeviceId());
            return rb.build();
        } catch (IllegalArgumentException ex) {
            return Response.status(400).entity(ex.getMessage()).build();//bad request
        } catch (DeviceManagementException ex) {
            return Response.status(500).entity(ex.getMessage()).build();
        } catch (AccessTokenException ex) {
            return Response.status(500).entity(ex.getMessage()).build();
        } catch (DeviceControllerException ex) {
            return Response.status(500).entity(ex.getMessage()).build();
        }

    }

    private ZipArchive createDownloadFile(String owner, String customDeviceName, String sketchType)
            throws DeviceManagementException, AccessTokenException, DeviceControllerException {
        if (owner == null) {
            throw new IllegalArgumentException("Error on createDownloadFile() Owner is null!");
        }

        //create new device id
        String deviceId = shortUUID();

        TokenClient accessTokenClient = new TokenClient(VirtualFireAlarmConstants.DEVICE_TYPE);
        AccessTokenInfo accessTokenInfo = accessTokenClient.getAccessToken(owner, deviceId);

        //create token
        String accessToken = accessTokenInfo.getAccess_token();
        String refreshToken = accessTokenInfo.getRefresh_token();
        //adding registering data

        XmppAccount newXmppAccount = new XmppAccount();
        newXmppAccount.setAccountName(owner + "_" + deviceId);
        newXmppAccount.setUsername(deviceId);
        newXmppAccount.setPassword(accessToken);
        newXmppAccount.setEmail(deviceId + "@wso2.com");

        XmppServerClient xmppServerClient = new XmppServerClient();
        xmppServerClient.initControlQueue();
        boolean status;

        if (XmppConfig.getInstance().isEnabled()) {
            status = xmppServerClient.createXMPPAccount(newXmppAccount);
            if (!status) {
                String msg =
                        "XMPP Account was not created for device - " + deviceId + " of owner - " +
                                owner +
                                ".XMPP might have been disabled in org.wso2.carbon.device.mgt.iot" +
                                ".common.config.server.configs";
                log.warn(msg);
                throw new DeviceManagementException(msg);
            }
        }

        //Register the device with CDMF
        String deviceName = customDeviceName + "_" + deviceId;
        status = register(deviceId, deviceName, owner);

        if (!status) {
            String msg = "Error occurred while registering the device with " + "id: " + deviceId
                    + " owner:" + owner;
            throw new DeviceManagementException(msg);
        }


        ZipUtil ziputil = new ZipUtil();
        ZipArchive zipFile = ziputil.downloadSketch(owner, SUPER_TENANT, sketchType, deviceId, deviceName,
                                         accessToken, refreshToken);
        zipFile.setDeviceId(deviceId);
        return zipFile;
    }

    private static String shortUUID() {
        UUID uuid = UUID.randomUUID();
        long l = ByteBuffer.wrap(uuid.toString().getBytes(StandardCharsets.UTF_8)).getLong();
        return Long.toString(l, Character.MAX_RADIX);
    }

    /*	---------------------------------------------------------------------------------------
                    Device specific APIs - Control APIs + Data-Publishing APIs
            Also contains utility methods required for the execution of these APIs
         ---------------------------------------------------------------------------------------	*/
    @Path("controller/register/{owner}/{deviceId}/{ip}/{port}")
    @POST
    public String registerDeviceIP(@PathParam("owner") String owner,
                                   @PathParam("deviceId") String deviceId,
                                   @PathParam("ip") String deviceIP,
                                   @PathParam("port") String devicePort,
                                   @Context HttpServletResponse response,
                                   @Context HttpServletRequest request) {

        //TODO:: Need to get IP from the request itself
        String result;

        log.info("Got register call from IP: " + deviceIP + " for Device ID: " + deviceId +
                         " of owner: " + owner);

        String deviceHttpEndpoint = deviceIP + ":" + devicePort;
        deviceToIpMap.put(deviceId, deviceHttpEndpoint);

        result = "Device-IP Registered";
        response.setStatus(Response.Status.OK.getStatusCode());

        if (log.isDebugEnabled()) {
            log.debug(result);
        }

        return result;
    }

    /*    Service to switch "ON" and "OFF" the Virtual FireAlarm bulb
           Called by an external client intended to control the Virtual FireAlarm bulb */
    @Path("controller/bulb/{state}")
    @POST
    public void switchBulb(@HeaderParam("owner") String owner,
                           @HeaderParam("deviceId") String deviceId,
                           @HeaderParam("protocol") String protocol,
                           @PathParam("state") String state,
                           @Context HttpServletResponse response) {

        try {
            DeviceValidator deviceValidator = new DeviceValidator();
            if (!deviceValidator.isExist(owner, SUPER_TENANT, new DeviceIdentifier(deviceId,
                                                                                   VirtualFireAlarmConstants
                                                                                           .DEVICE_TYPE))) {
                response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
                return;
            }
        } catch (DeviceManagementException e) {
            log.error("DeviceValidation Failed for deviceId: " + deviceId + " of user: " + owner);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return;
        }

        String switchToState = state.toUpperCase();

        if (!switchToState.equals(VirtualFireAlarmConstants.STATE_ON) && !switchToState.equals(
                VirtualFireAlarmConstants.STATE_OFF)) {
            log.error("The requested state change shoud be either - 'ON' or 'OFF'");
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return;
        }

        String protocolString = protocol.toUpperCase();
        String callUrlPattern = VirtualFireAlarmConstants.BULB_CONTEXT + switchToState;

        if (log.isDebugEnabled()) {
            log.debug("Sending request to switch-bulb of device [" + deviceId + "] via " +
                              protocolString);
        }

        try {
            switch (protocolString) {
                case HTTP_PROTOCOL:
                    String deviceHTTPEndpoint = deviceToIpMap.get(deviceId);
                    if (deviceHTTPEndpoint == null) {
                        response.setStatus(Response.Status.PRECONDITION_FAILED.getStatusCode());
                        return;
                    }

                    VirtualFireAlarmServiceUtils.sendCommandViaHTTP(deviceHTTPEndpoint, callUrlPattern, true);
                    break;
                case MQTT_PROTOCOL:
                    String mqttMessage = VirtualFireAlarmConstants.BULB_CONTEXT.replace("/", "");
                    VirtualFireAlarmServiceUtils.sendCommandViaMQTT(owner, deviceId, mqttMessage, switchToState);
                    break;
                case XMPP_PROTOCOL:
                    VirtualFireAlarmServiceUtils.sendCommandViaXMPP(owner, deviceId,
                                                                    VirtualFireAlarmConstants.BULB_CONTEXT,
                                                                    switchToState, virtualFireAlarmXMPPConnector);
                    break;
                default:
                    response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());
                    return;
            }
        } catch (DeviceManagementException e) {
            log.error("Failed to send switch-bulb request to device [" + deviceId + "] via " + protocolString);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return;
        }

        response.setStatus(Response.Status.OK.getStatusCode());
    }


    @Path("controller/readsonar")
    @GET
    public String requestSonarReading(@HeaderParam("owner") String owner,
                                      @HeaderParam("deviceId") String deviceId,
                                      @HeaderParam("protocol") String protocol,
                                      @Context HttpServletResponse response) {
        String replyMsg = "";

        DeviceValidator deviceValidator = new DeviceValidator();
        try {
            if (!deviceValidator.isExist(owner, SUPER_TENANT, new DeviceIdentifier(deviceId,
                                                                                   VirtualFireAlarmConstants
                                                                                           .DEVICE_TYPE))) {
                response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
                return "Unauthorized Access";
            }
        } catch (DeviceManagementException e) {
            replyMsg = e.getErrorMessage();
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return replyMsg;
        }

        String protocolString = protocol.toUpperCase();

        if (log.isDebugEnabled()) {
            log.debug("Sending request to read sonar value of device [" + deviceId + "] via " +
                              protocolString);
        }

        try {
            switch (protocolString) {
                case HTTP_PROTOCOL:
                    String deviceHTTPEndpoint = deviceToIpMap.get(deviceId);
                    if (deviceHTTPEndpoint == null) {
                        replyMsg =
                                "IP not registered for device: " + deviceId + " of owner: " + owner;
                        response.setStatus(Response.Status.PRECONDITION_FAILED.getStatusCode());
                        return replyMsg;
                    }

                    replyMsg = VirtualFireAlarmServiceUtils.sendCommandViaHTTP(deviceHTTPEndpoint,
                                                                               VirtualFireAlarmConstants.SONAR_CONTEXT,
                                                                               false);
                    break;

                case MQTT_PROTOCOL:
                    String mqttMessage = VirtualFireAlarmConstants.BULB_CONTEXT.replace("/", "");
                    VirtualFireAlarmServiceUtils.sendCommandViaMQTT(owner, deviceId, mqttMessage, "");
                    break;

                case XMPP_PROTOCOL:
                    VirtualFireAlarmServiceUtils.sendCommandViaXMPP(owner, deviceId,
                                                                    VirtualFireAlarmConstants.SONAR_CONTEXT, "",
                                                                    virtualFireAlarmXMPPConnector);
                    break;

                default:
                    replyMsg = "Requested protocol '" + protocolString + "' is not supported";
                    response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());
                    return replyMsg;
            }
        } catch (DeviceManagementException e) {
            replyMsg = e.getErrorMessage();
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            return replyMsg;
        }

        response.setStatus(Response.Status.OK.getStatusCode());
        replyMsg = "The current sonar reading of the device is " + replyMsg;
        return replyMsg;
    }


    @Path("controller/readtemperature")
    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public SensorRecord requestTemperature(@HeaderParam("owner") String owner,
                                           @HeaderParam("deviceId") String deviceId,
                                           @HeaderParam("protocol") String protocol,
                                           @Context HttpServletResponse response) {
        SensorRecord sensorRecord = null;

        DeviceValidator deviceValidator = new DeviceValidator();
        try {
            if (!deviceValidator.isExist(owner, SUPER_TENANT, new DeviceIdentifier(deviceId,
                                                                                   VirtualFireAlarmConstants
                                                                                           .DEVICE_TYPE))) {
                response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
            }
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

        String protocolString = protocol.toUpperCase();

        if (log.isDebugEnabled()) {
            log.debug(
                    "Sending request to read virtual-firealarm-temperature of device [" + deviceId +
                            "] via " + protocolString);
        }

        try {
            switch (protocolString) {
                case HTTP_PROTOCOL:
                    String deviceHTTPEndpoint = deviceToIpMap.get(deviceId);
                    if (deviceHTTPEndpoint == null) {
                        response.setStatus(Response.Status.PRECONDITION_FAILED.getStatusCode());
                    }

                    String temperatureValue = VirtualFireAlarmServiceUtils.sendCommandViaHTTP(deviceHTTPEndpoint,
                                                                                              VirtualFireAlarmConstants.TEMPERATURE_CONTEXT,
                                                                                              false);

                    SensorDataManager.getInstance().setSensorRecord(deviceId,
                                                                    VirtualFireAlarmConstants.SENSOR_TEMPERATURE,
                                                                    temperatureValue,
                                                                    Calendar.getInstance().getTimeInMillis());
                    break;

                case MQTT_PROTOCOL:
                    String mqttMessage = VirtualFireAlarmConstants.BULB_CONTEXT.replace("/", "");
                    VirtualFireAlarmServiceUtils.sendCommandViaMQTT(owner, deviceId, mqttMessage, "");
                    break;

                case XMPP_PROTOCOL:
                    VirtualFireAlarmServiceUtils.sendCommandViaXMPP(owner, deviceId,
                                                                    VirtualFireAlarmConstants.TEMPERATURE_CONTEXT, "",
                                                                    virtualFireAlarmXMPPConnector);
                    break;

                default:
                    response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());
            }
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId,
                                                                           VirtualFireAlarmConstants
                                                                                   .SENSOR_TEMPERATURE);
        } catch (DeviceManagementException | DeviceControllerException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

        response.setStatus(Response.Status.OK.getStatusCode());
        return sensorRecord;
    }

    @Path("controller/push_temperature")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void pushTemperatureData(final DeviceJSON dataMsg,
                                    @Context HttpServletResponse response) {
        String deviceId = dataMsg.deviceId;
        String deviceIp = dataMsg.reply;
        float temperature = dataMsg.value;

        String registeredIp = deviceToIpMap.get(deviceId);

        if (registeredIp == null) {
            log.warn("Unregistered IP: Temperature Data Received from an un-registered IP " +
                             deviceIp + " for device ID - " + deviceId);
            response.setStatus(Response.Status.PRECONDITION_FAILED.getStatusCode());
            return;
        } else if (!registeredIp.equals(deviceIp)) {
            log.warn("Conflicting IP: Received IP is " + deviceIp + ". Device with ID " +
                             deviceId +
                             " is already registered under some other IP. Re-registration " +
                             "required");
            response.setStatus(Response.Status.CONFLICT.getStatusCode());
            return;
        }
        SensorDataManager.getInstance().setSensorRecord(deviceId,
                                                        VirtualFireAlarmConstants
                                                                .SENSOR_TEMPERATURE,
                                                        String.valueOf(temperature),
                                                        Calendar.getInstance().getTimeInMillis());

        if (!VirtualFireAlarmServiceUtils.publishToDAS(dataMsg.owner, dataMsg.deviceId, dataMsg.value)) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }


    @GET
    @Path("controller/scep")
    public Response scepRequest(@QueryParam("operation") String operation, @QueryParam("message") String message) {

        if (log.isDebugEnabled()) {
            log.debug("Invoking SCEP operation " + operation);
        }

        if (SCEPOperation.GET_CA_CERT.getValue().equals(operation)) {

            if (log.isDebugEnabled()) {
                log.debug("Invoking GetCACert");
            }

            try {
                CertificateManagementService certificateManagementService =
                        VirtualFireAlarmServiceUtils.getCertificateManagementService();
                SCEPResponse scepResponse = certificateManagementService.getCACertSCEP();
                Response.ResponseBuilder responseBuilder;

                switch (scepResponse.getResultCriteria()) {
                    case CA_CERT_FAILED:
                        log.error("CA cert failed");
                        responseBuilder = Response.serverError();
                        break;
                    case CA_CERT_RECEIVED:

                        if (log.isDebugEnabled()) {
                            log.debug("CA certificate received in GetCACert");
                        }

                        responseBuilder = Response.ok(scepResponse.getEncodedResponse(),
                                                      ContentType.X_X509_CA_CERT);
                        break;
                    case CA_RA_CERT_RECEIVED:

                        if (log.isDebugEnabled()) {
                            log.debug("CA and RA certificates received in GetCACert");
                        }

                        responseBuilder = Response.ok(scepResponse.getEncodedResponse(),
                                                      ContentType.X_X509_CA_RA_CERT);
                        break;
                    default:
                        log.error("Invalid SCEP request");
                        responseBuilder = Response.serverError();
                        break;
                }

                return responseBuilder.build();
            } catch (VirtualFireAlarmException e) {
                log.error("Error occurred while enrolling the iOS device", e);
            } catch (KeystoreException e) {
                log.error("Keystore error occurred while enrolling the iOS device", e);
            }

        } else if (SCEPOperation.GET_CA_CAPS.getValue().equals(operation)) {

            if (log.isDebugEnabled()) {
                log.debug("Invoking GetCACaps");
            }

            try {
                CertificateManagementService certificateManagementService = VirtualFireAlarmServiceUtils.
                        getCertificateManagementService();
                byte caCaps[] = certificateManagementService.getCACapsSCEP();

                return Response.ok(caCaps, MediaType.TEXT_PLAIN).build();

            } catch (VirtualFireAlarmException e) {
                log.error("Error occurred while enrolling the device", e);
            }

        } else {
            log.error("Invalid SCEP operation " + operation);
        }

        return Response.serverError().build();
    }

    @POST
    @Path("controller/scep")
    public Response scepRequestPost(@QueryParam("operation") String operation, InputStream inputStream) {

        if (log.isDebugEnabled()) {
            log.debug("Invoking SCEP operation " + operation);
        }

        if (SCEPOperation.PKI_OPERATION.getValue().equals(operation)) {

            if (log.isDebugEnabled()) {
                log.debug("Invoking PKIOperation");
            }

            try {
                CertificateManagementService certificateManagementService = VirtualFireAlarmServiceUtils.
                        getCertificateManagementService();
                byte pkiMessage[] = certificateManagementService.getPKIMessageSCEP(inputStream);

                return Response.ok(pkiMessage, ContentType.X_PKI_MESSAGE).build();

            } catch (VirtualFireAlarmException e) {
                log.error("Error occurred while enrolling the device", e);
            } catch (KeystoreException e) {
                log.error("Keystore error occurred while enrolling the device", e);
            }
        }
        return Response.serverError().build();
    }
}
