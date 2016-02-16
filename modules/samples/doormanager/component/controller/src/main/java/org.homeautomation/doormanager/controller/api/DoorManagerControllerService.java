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

package org.homeautomation.doormanager.controller.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.homeautomation.doormanager.controller.api.dto.DeviceJSON;
import org.homeautomation.doormanager.controller.api.exception.DoorManagerException;
import org.homeautomation.doormanager.controller.api.util.DoorManagerMQTTConnector;
import org.homeautomation.doormanager.plugin.constants.DoorManagerConstants;
import org.homeautomation.doormanager.plugin.exception.DoorManagerDeviceMgtPluginException;
import org.homeautomation.doormanager.plugin.impl.dao.DoorLockSafe;
import org.homeautomation.doormanager.plugin.impl.dao.DoorManagerDAO;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.device.DeviceType;
import org.wso2.carbon.apimgt.annotations.device.feature.Feature;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.DeviceManagement;
import org.wso2.carbon.device.mgt.iot.DeviceValidator;
import org.wso2.carbon.device.mgt.iot.apimgt.AccessTokenInfo;
import org.wso2.carbon.device.mgt.iot.apimgt.TokenClient;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.exception.AccessTokenException;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.sensormgt.SensorRecord;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@API(name = "doormanager", version = "1.0.0", context = "/doormanager")
@DeviceType(value = "doormanager")
public class DoorManagerControllerService {

    private static final DoorManagerDAO DOOR_MANAGER_DAO = new DoorManagerDAO();
    @Context  //injected response proxy supporting multiple thread

    private static Log log = LogFactory.getLog(DoorManagerControllerService.class);
    private HttpServletResponse response;
    private DoorManagerMQTTConnector doorManagerMQTTConnector;
    private ConcurrentHashMap<String, DeviceJSON> deviceToIpMap = new ConcurrentHashMap<>();

    private PrivilegedCarbonContext ctx;

    private UserStoreManager getUserStoreManager() throws UserStoreException {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        PrivilegedCarbonContext.startTenantFlow();
        ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        ctx.setTenantDomain(tenantDomain, true);
        if (log.isDebugEnabled()) {
            log.debug("Getting thread local carbon context for tenant domain: " + tenantDomain);
        }
        RealmService realmService = (RealmService) ctx.getOSGiService(RealmService.class, null);
        return realmService.getTenantUserRealm(ctx.getTenantId()).getUserStoreManager();
    }

    /**
     * Ends tenant flow.
     */
    private void endTenantFlow() {
        PrivilegedCarbonContext.endTenantFlow();
        ctx = null;
        if (log.isDebugEnabled()) {
            log.debug("Tenant flow ended");
        }
    }

    private boolean waitForServerStartup() {
        while (!DeviceManagement.isServerReady()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return true;
            }
        }
        return false;
    }

    public DoorManagerMQTTConnector getDoorManagerMQTTConnector() {
        return doorManagerMQTTConnector;
    }

    public void setDoorManagerMQTTConnector(final DoorManagerMQTTConnector MQTTConnector) {
        Runnable connector = new Runnable() {
            public void run() {
                if (waitForServerStartup()) {
                    return;
                }
                DoorManagerControllerService.this.doorManagerMQTTConnector = MQTTConnector;
                if (MqttConfig.getInstance().isEnabled()) {
                    doorManagerMQTTConnector.connect();
                } else {
                    log.warn("MQTT disabled in 'devicemgt-config.xml'. Hence, DoorManagerMQTTConnector" +
                             " not started.");
                }
            }
        };
        Thread connectorThread = new Thread(connector);
        connectorThread.setDaemon(true);
        connectorThread.start();
    }

    @Path("controller/assign_user")
    @POST
    @Feature(code = "assign_user", name = "Assign new user to lock", type = "operation",
            description = "Add new access card to user to control the lock with given policy")
    public void assignUseToLock(@HeaderParam("owner") String owner,
                                @HeaderParam("deviceId") String deviceId,
                                @HeaderParam("protocol") String protocol,
                                @FormParam("policy") String policy,
                                @FormParam("cardNumber") String cardNumber,
                                @FormParam("userName") String userName,
                                @Context HttpServletResponse response){
        try {
                if (userName != null && cardNumber != null && deviceId != null) {
                    try {
                        UserStoreManager userStoreManager = this.getUserStoreManager();
                        if (userStoreManager.isExistingUser(userName)) {
                            TokenClient accessTokenClient = new TokenClient(DoorManagerConstants.DEVICE_TYPE);
                            AccessTokenInfo accessTokenInfo = accessTokenClient.getAccessToken(deviceId, userName);
                            String accessToken = accessTokenInfo.getAccess_token();
                            if (accessToken == null) {
                                response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
                                return;
                            }
                            Map<String, String> claims = new HashMap<>();
                            claims.put("http://wso2.org/claims/lock/accesstoken", accessToken);
                            claims.put("http://wso2.org/claims/lock/refreshtoken", accessTokenInfo.getRefresh_token());
                            claims.put("http://wso2.org/claims/lock/cardnumber", cardNumber);
                            userStoreManager.setUserClaimValues(userName, claims, null);
                            //TODO: Add content to dto
                        } else {
                            response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
                        }
                    } catch (UserStoreException e) {
                        response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                        log.error(e);
                    }
                }
                else {
                    response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
                }

        } catch (AccessTokenException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            log.error(e);
        }
    }

    @Path("controller/registerNewUser")
    @POST
    @Feature(code = "registerNewUser", name = "Assign to new user", type = "operation",
            description = "Assign to new user")
    public void registerDoorLockSafe(@HeaderParam("owner") String owner,
                                     @HeaderParam("deviceId") String deviceId,
                                     @HeaderParam("protocol") String protocol,
                                     @FormParam("policy") String policy,
                                     @FormParam("UIDofRFID") String UIDofRFID,
                                     @FormParam("userName") String userName,
                                     @FormParam("emailAddress") String emailAddress,
                                     @Context HttpServletResponse response) {
        try {
            TokenClient accessTokenClient = new TokenClient(DoorManagerConstants.DEVICE_TYPE);
            AccessTokenInfo accessTokenInfo = accessTokenClient.getAccessToken(deviceId, UIDofRFID);
            DoorLockSafe doorLockSafe = new DoorLockSafe();
            String accessToken = accessTokenInfo.getAccess_token();
            if (accessToken == null) {
                response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
            } else {
                if (emailAddress != null && UIDofRFID != null && deviceId != null) {
                    boolean status;
                    doorLockSafe.setAccessToken(accessTokenInfo.getAccess_token());
                    doorLockSafe.setRefreshToken(accessTokenInfo.getRefresh_token());
                    doorLockSafe.setDeviceId(deviceId);
                    doorLockSafe.setOwner(owner);
                    doorLockSafe.setEmailAddress(emailAddress);
                    doorLockSafe.setUIDofUser(UIDofRFID);
                    doorLockSafe.setPolicy(policy);
                    doorLockSafe.setSerialNumber(deviceId);
                    try {
                        DoorManagerDAO.beginTransaction();
                        status = DOOR_MANAGER_DAO.getAutomaticDoorLockerDeviceDAO().registerDoorLockSafe(doorLockSafe);
                        DoorManagerDAO.commitTransaction();
                        if (status) {
                            response.setStatus(Response.Status.OK.getStatusCode());
                        } else {
                            response.setStatus(Response.Status.FORBIDDEN.getStatusCode());
                        }
                    } catch (DoorManagerDeviceMgtPluginException e) {
                        try {
                            DoorManagerDAO.rollbackTransaction();
                        } catch (DoorManagerDeviceMgtPluginException e1) {
                            String msg = "Error while updating the enrollment of the Door Manager Locker device : "
                                         + doorLockSafe.getOwner();
                            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                            log.error(msg, e);
                        }
                    }
                } else {
                    response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
                }
            }
        } catch (AccessTokenException e) {
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            log.error(e);
        }
    }

    /*@Path("controller/registerNewUser")
    @POST
    @Feature(code = "registerNewUser", name = "Assign to new user", type = "operation",
            description = "Assign to new user")
    public void registerDoorLockSafe(@HeaderParam("owner") String owner,
                                     @HeaderParam("deviceId") String deviceId,
                                     @HeaderParam("protocol") String protocol,
                                     @FormParam("policy") String policy,
                                     @FormParam("cardNumber") String cardNumber,
                                     @FormParam("userName") String userName,
                                     @FormParam("emailAddress") String emailAddress,
                                     @Context HttpServletResponse response) {
        try {
            TokenClient accessTokenClient = new TokenClient(DoorManagerConstants.DEVICE_TYPE);
            AccessTokenInfo accessTokenInfo = accessTokenClient.getAccessToken(deviceId, userName);
            DoorLockSafe doorLockSafe = new DoorLockSafe();
            String accessToken = accessTokenInfo.getAccess_token();
            if (accessToken == null) {
                response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
            } else {
                if (emailAddress != null && cardNumber != null && deviceId != null) {
                    boolean status;
                    doorLockSafe.setAccessToken(accessTokenInfo.getAccess_token());
                    doorLockSafe.setRefreshToken(accessTokenInfo.getRefresh_token());
                    doorLockSafe.setDeviceId(deviceId);
                    doorLockSafe.setOwner(owner);
                    doorLockSafe.setEmailAddress(emailAddress);
                    doorLockSafe.setUIDofUser(cardNumber);
                    doorLockSafe.setPolicy(policy);
                    doorLockSafe.setSerialNumber(deviceId);
                    try {
                        UserStoreManager userStoreManager = this.getUserStoreManager();
                        if (userStoreManager.isExistingUser(userName)) {
                            if (accessToken == null) {
                                response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
                                return;
                            }
                            Map<String, String> claims = new HashMap<>();
                            claims.put("http://wso2.org/claims/lock/accesstoken", accessToken);
                            claims.put("http://wso2.org/claims/lock/refreshtoken", accessTokenInfo.getRefresh_token());
                            claims.put("http://wso2.org/claims/lock/cardnumber", cardNumber);
                            userStoreManager.setUserClaimValues(userName, claims, null);
                        } else {
                            response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
                        }
                        DoorManagerDAO.beginTransaction();
                        status = DOOR_MANAGER_DAO.getAutomaticDoorLockerDeviceDAO().registerDoorLockSafe(doorLockSafe);
                        DoorManagerDAO.commitTransaction();
                        if (status) {
                            response.setStatus(Response.Status.OK.getStatusCode());
                        } else {
                            response.setStatus(Response.Status.FORBIDDEN.getStatusCode());
                        }
                    } catch (DoorManagerDeviceMgtPluginException e) {
                        try {
                            DoorManagerDAO.rollbackTransaction();
                        } catch (DoorManagerDeviceMgtPluginException e1) {
                            String msg = "Error while updating the enrollment of the Door Manager Locker device : "
                                    + doorLockSafe.getOwner();
                            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                            log.error(msg, e);
                        }
                    } catch (UserStoreException e) {
                        log.error(e);
                    }
                } else {
                    response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
                }
            }
        } catch (AccessTokenException e) {
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            log.error(e);
        }
    }*/

    @Path("controller/changeStatusOfDoorLockSafe")
    @POST
    @Feature(code = "changeStatusOfDoorLockSafe", name = "Change status of door lock safe: LOCK/UNLOCK", type = "operation",
            description = "Change status of door lock safe: LOCK/UNLOCK")
    public void changeStatusOfDoorLockSafe(@HeaderParam("owner") String owner,
                                           @HeaderParam("deviceId") String deviceId,
                                           @HeaderParam("protocol") String protocol,
                                           @FormParam("state") String state,
                                           @Context HttpServletResponse response) {
        try {
            int lockerCurrentState;
            if(state.toUpperCase().equals("LOCK")){
                lockerCurrentState = 0;
            }else{
                lockerCurrentState = 1;
            }
            SensorDataManager.getInstance().setSensorRecord(deviceId, "door_locker_state",
                        String.valueOf(lockerCurrentState), Calendar.getInstance().getTimeInMillis());
            doorManagerMQTTConnector.sendCommandViaMQTT(owner, deviceId,"DoorManager:", state.toUpperCase());
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        } catch (DoorManagerException e) {
            log.error(e);
        }
    }

    /*@Path("controller/shareDoorLockSafe")
    @POST
    @Feature(code = "shareDoorLockSafe", name = "Share lock safe with new user", type = "operation",
            description = "Share lock safe with new user")
    public void shareDoorLockSafe(@HeaderParam("owner") String owner, @HeaderParam("deviceId") String deviceId,
                             @HeaderParam("protocol") String protocol,
                                  @FormParam("UIDofUser") String UIDofUser,@FormParam("policy") String policy,
                                  @Context HttpServletResponse response) {
        try {
            DoorLockSafe doorLockSafe = new DoorLockSafe();
            if(deviceId != null && UIDofUser != null && policy != null){
                boolean status;
                doorLockSafe.setDeviceId(deviceId);
                doorLockSafe.setOwner(owner);
                doorLockSafe.setPolicy(policy);
                doorLockSafe.setSerialNumber(deviceId);
                doorLockSafe.setUIDofUser(UIDofUser);
                try{
                    DoorManagerDAO.beginTransaction();
                    status = DOOR_MANAGER_DAO.getAutomaticDoorLockerDeviceDAO().shareDoorLockSafe(doorLockSafe);
                    DoorManagerDAO.commitTransaction();
                    if(status){
                        response.setStatus(Response.Status.OK.getStatusCode());
                    }else{
                        response.setStatus(Response.Status.FORBIDDEN.getStatusCode());
                    }
                }
                catch (DoorManagerDeviceMgtPluginException e) {
                    try {
                        DoorManagerDAO.rollbackTransaction();
                    } catch (DoorManagerDeviceMgtPluginException e1) {
                        String msg = "Error while sharing the enrollment of the Door Manager Locker device : "
                                + doorLockSafe.getOwner();
                        response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                        log.error(msg, e);
                    }
                }
            }else{
                response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            }
        } catch (Exception e) {
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            log.error(e);
        }
    }*/

    @Path("controller/addDoorOpenedUser")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addDoorOpenedUser(final DeviceJSON lockSafeInfo) {
        try {
            if (lockSafeInfo.serialNumber != null && lockSafeInfo.UIDofUser != null) {
                log.warn(lockSafeInfo.serialNumber);
                log.warn(lockSafeInfo.UIDofUser);
                deviceToIpMap.put(lockSafeInfo.serialNumber, lockSafeInfo);
                return Response.ok(Response.Status.OK.getStatusCode()).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
            }
        } catch (Exception e) {
            log.error(e);
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).build();
        }
    }

    @GET
    @Path("controller/requestStatusOfDoorLockSafe")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Feature(code = "requestStatusOfDoorLockSafe", name = "Door Locker Status", type = "monitor",
            description = "Request door locker current status")
    public SensorRecord requestStatusOfDoorLockSafe(@HeaderParam("owner") String owner,
                                                    @HeaderParam("deviceId") String deviceId,
                                                    @HeaderParam("protocol") String protocol,
                                                    @Context HttpServletResponse response) {
        SensorRecord sensorRecord = null;
        DeviceValidator deviceValidator = new DeviceValidator();
        try {
            if (!deviceValidator.isExist(owner, CarbonContext.getThreadLocalCarbonContext().getTenantDomain(),
                                         new DeviceIdentifier(deviceId, DoorManagerConstants.DEVICE_TYPE))) {
                response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
            }
        } catch (DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
        try {
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId, "door_locker_state");
        } catch (DeviceControllerException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
        response.setStatus(Response.Status.OK.getStatusCode());
        return sensorRecord;
    }

    @GET
    @Path("controller/getRegisteredDoorLockSafe")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRegisteredDoorLocks(@HeaderParam("owner") String owner,
                                           @HeaderParam("deviceId") String deviceId,
                                           @Context HttpServletResponse response) {
        List<String> doorLockSafes;
        try {
            DoorManagerDAO.beginTransaction();
            doorLockSafes = DOOR_MANAGER_DAO.getAutomaticDoorLockerDeviceDAO().getRegisteredDoorLocks(deviceId);
            DoorManagerDAO.commitTransaction();
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DoorManagerDeviceMgtPluginException e) {
            try {
                DoorManagerDAO.rollbackTransaction();
            } catch (DoorManagerDeviceMgtPluginException e1) {
                String msg = "Error while updating the enrollment of the Door Manager Locker device : " + deviceId;
                response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                log.error(msg, e);
            }
        }
        return Response.ok().build();
    }
}
