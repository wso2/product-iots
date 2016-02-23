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

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.homeautomation.doormanager.manager.api.dto.UserInfo;
import org.homeautomation.doormanager.plugin.constants.DoorManagerConstants;
import org.homeautomation.doormanager.plugin.exception.DoorManagerDeviceMgtPluginException;
import org.homeautomation.doormanager.plugin.impl.dao.DoorManagerDAO;

import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.apimgt.annotations.device.DeviceType;
import org.wso2.carbon.apimgt.webapp.publisher.KeyGenerationUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.iot.DeviceManagement;
import org.wso2.carbon.device.mgt.iot.apimgt.AccessTokenInfo;
import org.wso2.carbon.device.mgt.iot.apimgt.TokenClient;
import org.wso2.carbon.device.mgt.iot.exception.AccessTokenException;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.util.ZipArchive;
import org.wso2.carbon.device.mgt.iot.util.ZipUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

@DeviceType(value = "doormanager")
@API( name="doormanager_mgt", version="1.0.0", context="/doormanager_mgt")
public class DoorManagerManagerService {

	private static Log log = LogFactory.getLog(DoorManagerManagerService.class);
	//TODO; replace this tenant domain
    private static final DoorManagerDAO doorManagerDAO = new DoorManagerDAO();
	private final String SUPER_TENANT = "carbon.super";
	@Context  //injected response proxy supporting multiple thread
	private HttpServletResponse response;
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

	@Path("manager/device/register")
	@POST
	public boolean register(@QueryParam("name") String name, @QueryParam("owner") String owner,
							@QueryParam("serialNumber") String serialNumber) {

		log.warn("---------------------------------------");
		log.warn(serialNumber);
		DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);
		String deviceId = serialNumber;

		DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
		deviceIdentifier.setId(deviceId);
		deviceIdentifier.setType(DoorManagerConstants.DEVICE_TYPE);

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
			log.warn("locker access Token :"+ accessToken);
			accessTokenProperty.setValue(accessToken);

			Device.Property refreshTokenProperty = new Device.Property();
			refreshTokenProperty.setName(DoorManagerConstants.DEVICE_PLUGIN_PROPERTY_REFRESH_TOKEN);
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

	@Path("manager/device/remove/{device_id}")
	@DELETE
	public void removeDevice(@PathParam("device_id") String deviceId,
							 @Context HttpServletResponse response) {
		DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);
		DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
		deviceIdentifier.setId(deviceId);
		deviceIdentifier.setType(DoorManagerConstants.DEVICE_TYPE);
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
		deviceIdentifier.setType(DoorManagerConstants.DEVICE_TYPE);
		try {
			Device device = deviceManagement.getDeviceManagementService().getDevice(deviceIdentifier);
			device.setDeviceIdentifier(deviceId);
			device.getEnrolmentInfo().setDateOfLastUpdate(new Date().getTime());
			device.setName(name);
			device.setType(DoorManagerConstants.DEVICE_TYPE);
			boolean updated = deviceManagement.getDeviceManagementService().modifyEnrollment(device);
			if (updated) {
				response.setStatus(Response.Status.OK.getStatusCode());
			} else {
				response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());
			}
			return updated;
		} catch (DeviceManagementException e) {
			log.error(e.getErrorMessage());
			return false;
		} finally {
			deviceManagement.endTenantFlow();
		}
	}

	@POST
	@Path("manager/getUserCredentials")
	@Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
	public Response getUserCredentials(final UserInfo userInfo, @Context HttpServletRequest request,  @Context HttpServletResponse response) {
        /*try{
            String accessToken, refreshToken;
            DoorManagerDAO.beginTransaction();
            if(doorManagerDAO.getAutomaticDoorLockerDeviceDAO().isDoorLockSafeRegistered(userInfo.serialNumber,
                    userInfo.deviceId)){
                if(doorManagerDAO.getAutomaticDoorLockerDeviceDAO().isUserAllowed(userInfo.serialNumber,
                        userInfo.UIDofUser, userInfo.deviceId)){
                    List<String> userCredentials = doorManagerDAO.getAutomaticDoorLockerDeviceDAO()
                            .getUserCredentials(userInfo.deviceId, userInfo.UIDofUser);
                    DoorManagerDAO.commitTransaction();
                    if(!userCredentials.isEmpty()){
                        accessToken = userCredentials.get(0);
                        refreshToken = userCredentials.get(1);
                        if(accessToken != null && refreshToken != null){
                            JSONObject credentials     = new JSONObject();
                            credentials.put("accessToken", accessToken);
                            credentials.put("refreshToken", refreshToken);
                            return Response.ok(credentials, MediaType.APPLICATION_JSON_TYPE).build();
                        }else{
                            return Response.status(Response.Status.UNAUTHORIZED)
                                    .entity("{You have not been registered yet}").build();
                        }
                    }else{
                        return Response.status(Response.Status.UNAUTHORIZED)
                                .entity("{You have not been registered yet}").build();
                    }
                }else{
                    return Response.status(Response.Status.UNAUTHORIZED)
                            .entity("{You are not allowed open this door}").build();
                }
            }else{
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{This door hasn't been registered yet}").build();
            }
        }catch (DoorManagerDeviceMgtPluginException e) {
            try {
                DoorManagerDAO.rollbackTransaction();
            } catch (DoorManagerDeviceMgtPluginException e1) {
                String msg = "Error while retrieving the user credentials of " + userInfo.deviceId;
                log.error(msg, e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{Internal server error has occurred.}").build();
            }
        }
        return Response.status(Response.Status.NOT_ACCEPTABLE).build();*/
        try {
            log.warn("=========================================");
            log.warn("get info");
            log.warn(userInfo.userName);
            log.warn(userInfo.cardNumber);
            log.warn(userInfo.deviceId);
            response.addHeader("REMOTE_USER", "ppppppppppppppppppppp");
            //response.addDateHeader("Authentication","55555555555555555555");
            if (userInfo.userName != null && userInfo.cardNumber != null && userInfo.deviceId != null) {
                try {
                    UserStoreManager userStoreManager = this.getUserStoreManager();
                    if (userStoreManager.isExistingUser(userInfo.userName)) {
                        String accessToken = userStoreManager.getUserClaimValue(userInfo.userName, "http://wso2.org/claims/lock/accesstoken", null);
                        String cardNumber = userStoreManager.getUserClaimValue(userInfo.userName, "http://wso2.org/claims/lock/cardnumber", null);
                        if(cardNumber.equals(userInfo.cardNumber)){
                            if(accessToken != null){
                                JSONObject credentials = new JSONObject();
                                credentials.put(DoorManagerConstants.DEVICE_PLUGIN_PROPERTY_ACCESS_TOKEN, accessToken);
                                return Response.ok(credentials, MediaType.APPLICATION_JSON_TYPE).build();
                            }
                        }
                    } else {
                        return Response.status(Response.Status.UNAUTHORIZED).build();
                    }
                } catch (UserStoreException e) {
                    log.error(e);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }
            }
            else {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

        } catch (Exception e) {
            log.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	}

	@POST
	@Path("manager/get_user_info")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response get_user_info(final UserInfo userInfo) {
        try {
            if (userInfo.userName != null && userInfo.cardNumber != null && userInfo.deviceId != null) {
                try {
                    UserStoreManager userStoreManager = this.getUserStoreManager();
                    if (userStoreManager.isExistingUser(userInfo.userName)) {
                        String accessToken = userStoreManager.getUserClaimValue(userInfo.userName, "http://wso2.org/claims/lock/accesstoken", null);
                        String cardNumber = userStoreManager.getUserClaimValue(userInfo.userName, "http://wso2.org/claims/lock/cardnumber", null);
                        if(cardNumber.equals(userInfo.cardNumber)){
                            if(accessToken != null){
                                JSONObject credentials     = new JSONObject();
                                credentials.put(DoorManagerConstants.DEVICE_PLUGIN_PROPERTY_ACCESS_TOKEN, accessToken);
                                return Response.ok(credentials, MediaType.APPLICATION_JSON_TYPE).build();
                            }
                        }
                    } else {
                        return Response.status(Response.Status.UNAUTHORIZED).build();
                    }
                } catch (UserStoreException e) {
                    log.error(e);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                }
            }
            else {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

        } catch (Exception e) {
            log.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();

	}

	@Path("manager/device/{device_id}")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Device getDevice(@PathParam("device_id") String deviceId) {
		DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);
		DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
		deviceIdentifier.setId(deviceId);
		deviceIdentifier.setType(DoorManagerConstants.DEVICE_TYPE);
		try {
			return deviceManagement.getDeviceManagementService().getDevice(deviceIdentifier);
		} catch (DeviceManagementException ex) {
			log.error("Error occurred while retrieving device with Id " + deviceId + "\n" + ex);
			return null;
		} finally {
			deviceManagement.endTenantFlow();
		}
	}

	@Path("manager/device/{sketch_type}/download")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response downloadSketch(@QueryParam("owner") String owner,
								   @QueryParam("deviceName") String deviceName,
								   @PathParam("sketch_type") String sketchType) {
		try {
			ZipArchive zipFile = createDownloadFile(owner, deviceName, sketchType);
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
		//status = register(deviceId, deviceName, owner);
		status = true;
		if (!status) {
			String msg = "Error occurred while registering the device with " + "id: " + deviceId + " owner:" + owner;
			throw new DeviceManagementException(msg);
		}
		ZipUtil ziputil = new ZipUtil();
		ZipArchive zipFile = ziputil.createZipFile(owner, SUPER_TENANT, sketchType, deviceId, deviceName, accessToken,
				refreshToken);
		zipFile.setDeviceId(deviceId);
		return zipFile;
	}

	private static String shortUUID() {
		UUID uuid = UUID.randomUUID();
		long l = ByteBuffer.wrap(uuid.toString().getBytes(StandardCharsets.UTF_8)).getLong();
		return Long.toString(l, Character.MAX_RADIX);
	}

}
