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

package org.homeautomation;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.annotations.device.DeviceType;
import org.wso2.carbon.apimgt.webapp.publisher.KeyGenerationUtil;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.iot.DeviceManagement;
import org.wso2.carbon.device.mgt.iot.apimgt.AccessTokenInfo;
import org.wso2.carbon.device.mgt.iot.apimgt.TokenClient;
import org.wso2.carbon.device.mgt.iot.currentsensor.plugin.constants.CurrentSensorConstants;
import org.wso2.carbon.device.mgt.iot.exception.AccessTokenException;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.util.ZipArchive;
import org.wso2.carbon.device.mgt.iot.util.ZipUtil;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@DeviceType(value = "currentsensor")
public class CurrentSensorManagerService {

	private static Log log = LogFactory.getLog(CurrentSensorManagerService.class);
	//TODO; replace this tenant domain
	private final String SUPER_TENANT = "carbon.super";
	@Context  //injected response proxy supporting multiple thread
	private HttpServletResponse response;

	@Path("manager/device/register")
	@PUT
	public boolean register(@QueryParam("deviceId") String deviceId,
							@QueryParam("name") String name, @QueryParam("owner") String owner) {

		DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);

		DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
		deviceIdentifier.setId(deviceId);
		deviceIdentifier.setType(CurrentSensorConstants.DEVICE_TYPE);
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
			device.setType(CurrentSensorConstants.DEVICE_TYPE);
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
	public void removeDevice(@PathParam("device_id") String deviceId,
							 @Context HttpServletResponse response) {

		DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);
		DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
		deviceIdentifier.setId(deviceId);
		deviceIdentifier.setType(CurrentSensorConstants.DEVICE_TYPE);
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
		deviceIdentifier.setType(CurrentSensorConstants.DEVICE_TYPE);
		try {
			Device device = deviceManagement.getDeviceManagementService().getDevice(deviceIdentifier);
			device.setDeviceIdentifier(deviceId);

			// device.setDeviceTypeId(deviceTypeId);
			device.getEnrolmentInfo().setDateOfLastUpdate(new Date().getTime());

			device.setName(name);
			device.setType(CurrentSensorConstants.DEVICE_TYPE);

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

	@Path("manager/device/{device_id}")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Device getDevice(@PathParam("device_id") String deviceId) {

		DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);
		DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
		deviceIdentifier.setId(deviceId);
		deviceIdentifier.setType(CurrentSensorConstants.DEVICE_TYPE);

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
								   @PathParam("sketch_type") String
										   sketchType) {

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

		KeyGenerationUtil.createApplicationKeys("currentsensor");

		TokenClient accessTokenClient = new TokenClient(CurrentSensorConstants.DEVICE_TYPE);
		AccessTokenInfo accessTokenInfo = accessTokenClient.getAccessToken(owner, deviceId);

		//create token
		String accessToken = accessTokenInfo.getAccess_token();
		String refreshToken = accessTokenInfo.getRefresh_token();
		//adding registering data

		boolean status;

		//Register the device with CDMF
		status = register(deviceId, deviceName, owner);

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
