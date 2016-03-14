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
import org.homeautomation.doormanager.controller.api.dto.UserInfo;
import org.homeautomation.doormanager.controller.api.exception.DoorManagerException;
import org.homeautomation.doormanager.controller.api.transport.DoorManagerMQTTConnector;
import org.homeautomation.doormanager.plugin.constants.DoorManagerConstants;
import org.homeautomation.doormanager.plugin.exception.DoorManagerDeviceMgtPluginException;
import org.homeautomation.doormanager.plugin.impl.DoorManager;
import org.homeautomation.doormanager.plugin.impl.dao.DoorLockSafe;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.device.DeviceType;
import org.wso2.carbon.apimgt.annotations.device.feature.Feature;
import org.wso2.carbon.context.CarbonContext;
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

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("NonJaxWsWebServices")
@API(name = "doormanager", version = "1.0.0", context = "/doormanager")
@DeviceType(value = "doormanager")
public class DoorManagerControllerService {

    private static Log log = LogFactory.getLog(DoorManagerControllerService.class);
    private static String CURRENT_STATUS = "doorLockerCurrentStatus";
    private DoorManager doorManager;
    private DoorManagerMQTTConnector doorManagerMQTTConnector;

    DoorManagerControllerService() {
        doorManager = new DoorManager();
    }

    @Context  //injected response proxy supporting multiple thread
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
    @Path("controller/assign_user")
    @POST
    @Feature(code = "assign_user", name = "Assign new user to lock", type = "operation",
            description = "Add new access card to user to control the lock ")
    public void assignUserToLock(@HeaderParam("owner") String owner,
                                 @HeaderParam("deviceId") String deviceId,
                                 @HeaderParam("protocol") String protocol,
                                 @FormParam("cardNumber") String cardNumber,
                                 @FormParam("userName") String userName,
                                 @FormParam("emailAddress") String emailAddress,
                                 @Context HttpServletResponse response) {

        if (userName != null && cardNumber != null && deviceId != null) {
            try {
                UserStoreManager userStoreManager = doorManager.getUserStoreManager();
                DoorLockSafe doorLockSafe = new DoorLockSafe();
                if (userStoreManager.isExistingUser(userName)) {
                    TokenClient accessTokenClient = new TokenClient(DoorManagerConstants.DEVICE_TYPE);
                    AccessTokenInfo accessTokenInfo = accessTokenClient.getAccessToken(deviceId, userName);
                    String accessToken = accessTokenInfo.getAccess_token();
                    if (accessToken == null) {
                        response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
                    }
                    Map<String, String> claims = new HashMap<>();
                    claims.put(DoorManagerConstants.DEVICE_CLAIMS_ACCESS_TOKEN, accessToken);
                    claims.put(DoorManagerConstants.DEVICE_CLAIMS_REFRESH_TOKEN,
                            accessTokenInfo.getRefresh_token());
                    claims.put(DoorManagerConstants.DEVICE_CLAIMS_CARD_NUMBER, cardNumber);
                    userStoreManager.setUserClaimValues(userName, claims, null);
                    doorLockSafe.setAccessToken(accessTokenInfo.getAccess_token());
                    doorLockSafe.setRefreshToken(accessTokenInfo.getRefresh_token());
                    doorLockSafe.setDeviceId(deviceId);
                    doorLockSafe.setOwner(owner);
                    doorLockSafe.setEmailAddress(emailAddress);
                    doorLockSafe.setUIDofUser(cardNumber);
                    doorLockSafe.setSerialNumber(deviceId);
                    if (doorManager.assignUserToLock(doorLockSafe)) {
                        response.setStatus(Response.Status.OK.getStatusCode());
                    } else {
                        response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
                    }
                } else {
                    response.setStatus(Response.Status.NOT_FOUND.getStatusCode());
                }
            } catch (UserStoreException e) {
                response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                log.error(e);
            } catch (DoorManagerDeviceMgtPluginException | AccessTokenException e) {
                log.error(e);
                response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            }
        } else {
            response.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    /**
     * Change status of door lock safe: LOCK/UNLOCK
     *
     * @param owner    owner of the device
     * @param deviceId unique identifier for given device
     * @param protocol transport protocol which is being using here MQTT
     * @param state    status of lock safe: lock/unlock
     */
    @Path("controller/change-status")
    @POST
    @Feature(code = "change-status", name = "Change status of door lock safe: LOCK/UNLOCK", type = "operation",
            description = "Change status of door lock safe: LOCK/UNLOCK")
    public void changeStatusOfDoorLockSafe(@HeaderParam("owner") String owner,
                                           @HeaderParam("deviceId") String deviceId,
                                           @HeaderParam("protocol") String protocol,
                                           @FormParam("state") String state,
                                           @Context HttpServletResponse response) {
        try {
            int lockerCurrentState;
            if (state.toUpperCase().equals("LOCK")) {
                lockerCurrentState = 0;
            } else {
                lockerCurrentState = 1;
            }
            SensorDataManager.getInstance().setSensorRecord(deviceId, CURRENT_STATUS,
                    String.valueOf(lockerCurrentState), Calendar.getInstance().getTimeInMillis());
            doorManagerMQTTConnector.sendCommandViaMQTT(owner, deviceId, "DoorManager:", state.toUpperCase());
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        } catch (DoorManagerException e) {
            log.error(e);
        }
    }

    /**
     * Request current status of door lock safe
     *
     * @param owner    owner of the device
     * @param deviceId unique identifier for given device
     * @param protocol transport protocol which is being using here MQTT
     * @param response http servlet response object
     */
    @GET
    @Path("controller/current-status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Feature(code = "current-status", name = "Door Locker Status", type = "monitor",
            description = "Request current status of door safe")
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
            sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId, CURRENT_STATUS);
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceControllerException | DeviceManagementException e) {
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
        return sensorRecord;
    }

    /**
     * @param userInfo user information which are required to test given user is authorized to open requested door
     * @return if user is authorized open the the door allow to open it
     */
    @POST
    @Path("controller/get_user_info")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @SuppressWarnings("unchecked") //This is to avoid unchecked call to put(k, v) into jsonObject. org.json.simple
    // library uses raw type collections internally.
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
            } catch (JSONException e) {
                log.error(e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    /*
    private void sendCEPEvent(String deviceId, String cardId, boolean accessStatus){
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
