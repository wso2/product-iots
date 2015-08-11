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

package org.wso2.carbon.device.mgt.iot.sample.sensebot.service.impl;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.iot.sample.sensebot.plugin.constants.SensebotConstants;
import org.wso2.carbon.device.mgt.iot.common.util.ZipUtil;
import org.wso2.carbon.device.mgt.iot.common.util.ZipArchive;
import org.wso2.carbon.device.mgt.iot.common.DeviceManagement;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

public class SensebotManagerService {

	private static Log log = LogFactory.getLog(SensebotManagerService.class);

	@Path("/device/register")
	@PUT
	public boolean register(@QueryParam("deviceId") String deviceId,
							@QueryParam("name") String name, @QueryParam("owner") String owner) {

		DeviceManagement deviceManagement = new DeviceManagement();

		DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
		deviceIdentifier.setId(deviceId);
		deviceIdentifier.setType(SensebotConstants.DEVICE_TYPE);
		try {
			if (deviceManagement.getDeviceManagementService().isEnrolled(deviceIdentifier)) {
				Response.status(HttpStatus.SC_CONFLICT).build();
				return false;
			}

			Device device = new Device();
			device.setDeviceIdentifier(deviceId);
			EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
			enrolmentInfo.setDateOfEnrolment(new Date().getTime());
			enrolmentInfo.setDateOfLastUpdate(new Date().getTime());
			enrolmentInfo.setStatus(EnrolmentInfo.Status.ACTIVE);

			device.setName(name);
			device.setType(SensebotConstants.DEVICE_TYPE);
			enrolmentInfo.setOwner(owner);
			enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
			device.setEnrolmentInfo(enrolmentInfo);
			boolean added = deviceManagement.getDeviceManagementService().enrollDevice(device);
			if (added) {
				Response.status(HttpStatus.SC_OK).build();


			} else {
				Response.status(HttpStatus.SC_EXPECTATION_FAILED).build();


			}

			return added;
		} catch (DeviceManagementException e) {
			log.error(e.getErrorMessage());
			return false;
		}
	}

	@Path("/device/remove/{device_id}")
	@DELETE
	public void removeDevice(@PathParam("device_id") String deviceId,
							 @Context HttpServletResponse response) {

		DeviceManagement deviceManagement = new DeviceManagement();
		DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
		deviceIdentifier.setId(deviceId);
		deviceIdentifier.setType(SensebotConstants.DEVICE_TYPE);
		try {
			boolean removed = deviceManagement.getDeviceManagementService().disenrollDevice(deviceIdentifier);
			if (removed) {
				response.setStatus(HttpStatus.SC_OK);

			} else {
				response.setStatus(HttpStatus.SC_EXPECTATION_FAILED);

			}
		} catch (DeviceManagementException e) {
			log.error(e.getErrorMessage());

		}


	}

	@Path("/device/update/{device_id}")
	@POST
	public boolean updateDevice(@PathParam("device_id") String deviceId,
								@QueryParam("name") String name,
								@Context HttpServletResponse response) {

		DeviceManagement deviceManagement = new DeviceManagement();

		DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
		deviceIdentifier.setId(deviceId);
		deviceIdentifier.setType(SensebotConstants.DEVICE_TYPE);
		try {
			Device device = deviceManagement.getDeviceManagementService().getDevice(deviceIdentifier);
			device.setDeviceIdentifier(deviceId);

			// device.setDeviceTypeId(deviceTypeId);
			device.getEnrolmentInfo().setDateOfLastUpdate(new Date().getTime());

			device.setName(name);
			device.setType(SensebotConstants.DEVICE_TYPE);

			boolean updated = deviceManagement.getDeviceManagementService().modifyEnrollment(device);


			if (updated) {
				response.setStatus(HttpStatus.SC_OK);

			} else {
				response.setStatus(HttpStatus.SC_EXPECTATION_FAILED);

			}
			return updated;
		} catch (DeviceManagementException e) {
			log.error(e.getErrorMessage());
			return false;
		}

	}

	@Path("/device/{device_id}")
	@GET
	@Consumes("application/json")
	@Produces("application/json")
	public Device getDevice(@PathParam("device_id") String deviceId) {

		DeviceManagement deviceManagement = new DeviceManagement();
		DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
		deviceIdentifier.setId(deviceId);
		deviceIdentifier.setType(SensebotConstants.DEVICE_TYPE);

		try {
			Device device = deviceManagement.getDeviceManagementService().getDevice(deviceIdentifier);

			return device;
		} catch (DeviceManagementException ex) {
			log.error("Error occurred while retrieving device with Id " + deviceId + "\n" + ex);
			return null;
		}

	}

	@Path("/device/{sketch_type}/download")
	@GET
	@Produces("application/octet-stream")
	public Response downloadSketch(@QueryParam("owner") String owner, @PathParam("sketch_type") String
			sketchType) {

		if (owner == null) {
			return Response.status(400).build();//bad request
		}

		//create new device id
		String deviceId = shortUUID();

		//create token
		String token = UUID.randomUUID().toString();
		String refreshToken = UUID.randomUUID().toString();
		//adding registering data

		boolean status = register(deviceId,
								  owner + "s_" + sketchType + "_" + deviceId.substring(0, 3),
								  owner);
		if (!status) {
			return Response.status(500).entity(
					"Error occurred while registering the device with " + "id: " + deviceId
							+ " owner:" + owner).build();

		}

		ZipUtil ziputil = new ZipUtil();
		ZipArchive zipFile = null;
		try {
			zipFile = ziputil.downloadSketch(owner, sketchType, deviceId,
											 token,refreshToken);
		} catch (DeviceManagementException ex) {
			return Response.status(500).entity("Error occurred while creating zip file").build();
		}

		Response.ResponseBuilder rb = Response.ok(zipFile.getZipFile());
		rb.header("Content-Disposition", "attachment; filename=\"" + zipFile.getFileName() + "\"");
		return rb.build();
	}

	private static String shortUUID() {
		UUID uuid = UUID.randomUUID();
		long l = ByteBuffer.wrap(uuid.toString().getBytes(StandardCharsets.UTF_8)).getLong();
		return Long.toString(l, Character.MAX_RADIX);
	}

}
