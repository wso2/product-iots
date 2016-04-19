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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.homeautomation.doormanager.api.exception.DoorManagerException;
import org.homeautomation.doormanager.api.transport.DoorManagerMQTTConnector;
import org.homeautomation.doormanager.api.dto.UserInfo;
import org.homeautomation.doormanager.api.util.APIUtil;
import org.homeautomation.doormanager.plugin.constants.DoorManagerConstants;
import org.homeautomation.doormanager.plugin.exception.DoorManagerDeviceMgtPluginException;
import org.homeautomation.doormanager.plugin.impl.DoorManager;
import org.homeautomation.doormanager.plugin.impl.dao.DoorLockSafe;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.application.extension.APIManagementProviderService;
import org.wso2.carbon.apimgt.application.extension.dto.ApiApplicationKey;
import org.wso2.carbon.apimgt.application.extension.exception.APIManagerException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.DeviceType;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.service.IoTServerStartupListener;
import org.wso2.carbon.identity.jwt.client.extension.JWTClient;
import org.wso2.carbon.identity.jwt.client.extension.dto.AccessTokenInfo;
import org.wso2.carbon.identity.jwt.client.extension.exception.JWTClientException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DoorManagerControllerServiceImpl implements DoorManagerControllerService {

    private static Log log = LogFactory.getLog(DoorManagerControllerServiceImpl.class);
    private static String CURRENT_STATUS = "doorLockerCurrentStatus";
    private DoorManager doorManager;
    private DoorManagerMQTTConnector doorManagerMQTTConnector;
    private static final String KEY_TYPE = "PRODUCTION";
    private static ApiApplicationKey apiApplicationKey;

    DoorManagerControllerServiceImpl() {
        doorManager = new DoorManager();
    }

    private boolean waitForServerStartup() {
        while (!IoTServerStartupListener.isServerReady()) {
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
                DoorManagerControllerServiceImpl.this.doorManagerMQTTConnector = MQTTConnector;
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

    @Path("device/assign-user")
    @POST
    public Response assignUserToLock(@HeaderParam("owner") String owner,
                                     @HeaderParam("deviceId") String deviceId,
                                     @HeaderParam("protocol") String protocol,
                                     @FormParam("cardNumber") String cardNumber,
                                     @FormParam("userName") String userName,
                                     @FormParam("emailAddress") String emailAddress) {

        if (userName != null && cardNumber != null && deviceId != null) {
            try {
                UserStoreManager userStoreManager = doorManager.getUserStoreManager();
                DoorLockSafe doorLockSafe = new DoorLockSafe();
                if (userStoreManager.isExistingUser(userName)) {
                    if (apiApplicationKey == null) {
                        String applicationUsername = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration()
                                .getAdminUserName();
                        APIManagementProviderService apiManagementProviderService = APIUtil.getAPIManagementProviderService();
                        String[] tags = {DoorManagerConstants.DEVICE_TYPE};
                        apiApplicationKey = apiManagementProviderService.generateAndRetrieveApplicationKeys(
                                DoorManagerConstants.DEVICE_TYPE, tags, KEY_TYPE, applicationUsername, true);
                    }
                    JWTClient jwtClient = APIUtil.getJWTClientManagerService().getJWTClient();
                    String scopes = "device_type_" + DoorManagerConstants.DEVICE_TYPE + " device_" + deviceId;
                    AccessTokenInfo accessTokenInfo = jwtClient.getAccessToken(apiApplicationKey.getConsumerKey(),
                                                                               apiApplicationKey.getConsumerSecret(), owner, scopes);
                    String accessToken = accessTokenInfo.getAccessToken();
                    if (accessToken == null) {
                        return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
                    }
                    Map<String, String> claims = new HashMap<>();
                    claims.put(DoorManagerConstants.DEVICE_CLAIMS_ACCESS_TOKEN, accessToken);
                    claims.put(DoorManagerConstants.DEVICE_CLAIMS_REFRESH_TOKEN,
                               accessTokenInfo.getRefreshToken());
                    claims.put(DoorManagerConstants.DEVICE_CLAIMS_CARD_NUMBER, cardNumber);
                    userStoreManager.setUserClaimValues(userName, claims, null);
                    doorLockSafe.setAccessToken(accessTokenInfo.getAccessToken());
                    doorLockSafe.setRefreshToken(accessTokenInfo.getRefreshToken());
                    doorLockSafe.setDeviceId(deviceId);
                    doorLockSafe.setOwner(owner);
                    doorLockSafe.setEmailAddress(emailAddress);
                    doorLockSafe.setUIDofUser(cardNumber);
                    doorLockSafe.setSerialNumber(deviceId);
                    if (doorManager.assignUserToLock(doorLockSafe)) {
                        return Response.ok().build();
                    } else {
                        return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).build();
                    }
                } else {
                    return Response.status(Response.Status.NOT_FOUND.getStatusCode()).build();
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
        } else {
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode()).build();
        }
    }

    @Path("device/change-status")
    @POST
    public Response changeStatusOfDoorLockSafe(@HeaderParam("owner") String owner,
                                        @HeaderParam("deviceId") String deviceId,
                                        @HeaderParam("protocol") String protocol,
                                        @FormParam("state") String state) {
        try {
            int lockerCurrentState;
            if (state.toUpperCase().equals("LOCK")) {
                lockerCurrentState = 0;
            } else {
                lockerCurrentState = 1;
            }
            doorManagerMQTTConnector.sendCommandViaMQTT(owner, deviceId, "DoorManager:", state.toUpperCase());
            return Response.ok().build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
        } catch (DoorManagerException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    @GET
    @Path("device/get-user-info")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @SuppressWarnings("unchecked")
    public Response get_user_info(final UserInfo userInfo) {
        if (userInfo.userName != null && userInfo.cardNumber != null && userInfo.deviceId != null) {
            try {
                UserStoreManager userStoreManager = doorManager.getUserStoreManager();
                if (userStoreManager.isExistingUser(userInfo.userName)) {
                    String accessToken = userStoreManager.getUserClaimValue(userInfo.userName,
                                                                            DoorManagerConstants.DEVICE_CLAIMS_ACCESS_TOKEN, null);
                    String cardNumber = userStoreManager.getUserClaimValue(userInfo.userName,
                                                                           DoorManagerConstants.DEVICE_CLAIMS_CARD_NUMBER, null);
                    if (cardNumber != null) {
                        if (cardNumber.equals(userInfo.cardNumber)) {
                            if (accessToken != null) {
                                JSONObject credentials = new JSONObject();
                                credentials.put(DoorManagerConstants.DEVICE_PLUGIN_PROPERTY_ACCESS_TOKEN, accessToken);
                                //return Response.ok(credentials, MediaType.APPLICATION_JSON_TYPE).build();
                                return Response.status(Response.Status.OK).build();
                            }
                        }
                        return Response.status(Response.Status.UNAUTHORIZED).build();
                    }

                } else {
                    return Response.status(Response.Status.UNAUTHORIZED).build();
                }
            } catch (UserStoreException e) {
                log.error(e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

}
