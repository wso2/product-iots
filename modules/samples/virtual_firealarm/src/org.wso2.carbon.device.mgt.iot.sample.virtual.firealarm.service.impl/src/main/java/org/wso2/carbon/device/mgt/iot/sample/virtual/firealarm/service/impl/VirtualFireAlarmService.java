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
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.jivesoftware.smack.packet.Message;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.analytics.service.DeviceAnalyticsService;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.iot.common.DeviceController;
import org.wso2.carbon.device.mgt.iot.common.DeviceManagement;
import org.wso2.carbon.device.mgt.iot.common.DeviceValidator;
import org.wso2.carbon.device.mgt.iot.common.apimgt.AccessTokenInfo;
import org.wso2.carbon.device.mgt.iot.common.apimgt.TokenClient;
import org.wso2.carbon.device.mgt.iot.common.controlqueue.xmpp.XmppAccount;
import org.wso2.carbon.device.mgt.iot.common.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.common.controlqueue.xmpp.XmppConfig;
import org.wso2.carbon.device.mgt.iot.common.controlqueue.xmpp.XmppServerClient;
import org.wso2.carbon.device.mgt.iot.common.exception.AccessTokenException;
import org.wso2.carbon.device.mgt.iot.common.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.common.util.ZipArchive;
import org.wso2.carbon.device.mgt.iot.common.util.ZipUtil;
import org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.plugin.constants
		.VirtualFireAlarmConstants;
import org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.service.impl.util.DeviceJSON;
import org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.service.impl.util.mqtt.MQTTClient;
import org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.service.impl.util.xmpp.XMPPClient;
import org.wso2.carbon.utils.CarbonUtils;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

//@Path("/VirtualFireAlarmDeviceManager")
public class VirtualFireAlarmService {

	private static Log log = LogFactory.getLog(VirtualFireAlarmService.class);

//TODO; replace this tenant domain
	private final String SUPER_TENANT = "carbon.super";

	@Context  //injected response proxy supporting multiple thread
	private HttpServletResponse response;

	private static final String TEMPERATURE_STREAM_DEFINITION = "org.wso2.iot.devices.temperature";

	private static final String URL_PREFIX = "http://";
	private static final String BULB_CONTEXT = "/BULB/";
	private static final String SONAR_CONTEXT = "/SONAR/";
	private static final String TEMPERATURE_CONTEXT = "/TEMPERATURE/";

	public static final String XMPP_PROTOCOL = "XMPP";
	public static final String HTTP_PROTOCOL = "HTTP";
	public static final String MQTT_PROTOCOL = "MQTT";

	private static ConcurrentHashMap<String, String> deviceToIpMap = new ConcurrentHashMap<String, String>();
	private static XMPPClient xmppClient;
	private static MQTTClient mqttClient;
	private static final String mqttServerSubscribeTopic = "wso2/iot/+/" + VirtualFireAlarmConstants.DEVICE_TYPE + "/+/reply";
	private static final String iotServerSubscriber = "IoT-Server";

	static{
		String xmppServer = XmppConfig.getInstance().getXmppControlQueue().getServerURL();
		int indexOfChar = xmppServer.lastIndexOf('/');
		if (indexOfChar != -1) {
			xmppServer = xmppServer.substring((indexOfChar + 1), xmppServer.length());
		}

		int xmppPort = Integer.parseInt(XmppConfig.getInstance().getSERVER_CONNECTION_PORT());
		xmppClient = new XMPPClient(xmppServer, xmppPort) {
			@Override
			protected void processXMPPMessage(Message xmppMessage) {

			}
		};

		String xmppUsername = XmppConfig.getInstance().getXmppUsername();
		String xmppPassword = XmppConfig.getInstance().getXmppPassword();

		try {
			xmppClient.connectAndLogin(xmppUsername, xmppPassword, "iotServer");
		} catch (DeviceManagementException e) {
			e.printStackTrace();
		}

		xmppClient.setMessageFilterAndListener("");

		String mqttEndpoint = MqttConfig.getInstance().getMqttQueueEndpoint();
		mqttClient = new MQTTClient(iotServerSubscriber, VirtualFireAlarmConstants.DEVICE_TYPE, mqttEndpoint, mqttServerSubscribeTopic) {
			@Override
			protected void postMessageArrived(String topic, MqttMessage message) {

			}
		};

		try {
			mqttClient.connectAndSubscribe();
		} catch (DeviceManagementException e) {
			e.printStackTrace();
		}
	}


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
	public void removeDevice(@PathParam("device_id") String deviceId,
							 @Context HttpServletResponse response) {

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
			ArrayList<Device> userDevicesforFirealarm = new ArrayList<Device>();
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
								   @PathParam("sketch_type") String sketchType) {

		ZipArchive zipFile = null;
		try {
			zipFile = createDownloadFile(owner, sketchType);
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
									   @PathParam("sketch_type") String sketchType) {

		ZipArchive zipFile = null;
		try {
			zipFile = createDownloadFile(owner, sketchType);
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

	private ZipArchive createDownloadFile(String owner, String sketchType)
			throws DeviceManagementException, AccessTokenException, DeviceControllerException {
		if (owner == null) {
			throw new IllegalArgumentException("Error on createDownloadFile() Owner is null!");
		}

		//create new device id
		String deviceId = shortUUID();

		TokenClient accessTokenClient = new TokenClient(VirtualFireAlarmConstants.DEVICE_TYPE);
		AccessTokenInfo accessTokenInfo = null;

		accessTokenInfo = accessTokenClient.getAccessToken(owner, deviceId);

		//create token
		String accessToken = accessTokenInfo.getAccess_token();
		String refreshToken = accessTokenInfo.getRefresh_token();
		//adding registering data

		XmppAccount newXmppAccount = new XmppAccount();
		newXmppAccount.setAccountName(owner + "_" + deviceId);
		newXmppAccount.setUsername(deviceId);
		newXmppAccount.setPassword(accessToken);

		String xmppEndPoint = XmppConfig.getInstance().getXmppControlQueue().getServerURL();

		int indexOfChar = xmppEndPoint.lastIndexOf('/');

		if (indexOfChar != -1) {
			xmppEndPoint = xmppEndPoint.substring((indexOfChar + 1), xmppEndPoint.length());
		}

		newXmppAccount.setEmail(deviceId + "@wso2.com");

		XmppServerClient xmppServerClient = new XmppServerClient();
		xmppServerClient.initControlQueue();
		boolean status;
		if(XmppConfig.getInstance().isEnabled()) {
			status = xmppServerClient.createXMPPAccount(newXmppAccount);

			if (!status) {
				String msg =
						"XMPP Account was not created for device - " + deviceId + " of owner - " +
								owner +
								". XMPP might have been disabled in org.wso2.carbon.device.mgt.iot.common.config.server.configs";
				log.warn(msg);
				throw new DeviceManagementException(msg);
			}
		}
		status = register(deviceId, owner + "s_" + sketchType + "_" + deviceId.substring(0,
																								 3),
								  owner);
		if (!status) {
			String msg = "Error occurred while registering the device with " + "id: " + deviceId
					+ " owner:" + owner;
			throw new DeviceManagementException(msg);
		}


		ZipUtil ziputil = new ZipUtil();
		ZipArchive zipFile = null;

		zipFile = ziputil.downloadSketch(owner,SUPER_TENANT, sketchType, deviceId, accessToken, refreshToken);
		zipFile.setDeviceId(deviceId);
		return zipFile;
	}

	private static String shortUUID() {
		UUID uuid = UUID.randomUUID();
		long l = ByteBuffer.wrap(uuid.toString().getBytes(StandardCharsets.UTF_8)).getLong();
		return Long.toString(l, Character.MAX_RADIX);
	}

	@Path("controller/register/{owner}/{deviceId}/{ip}")
	@POST
	public String registerDeviceIP(@PathParam("owner") String owner,
								   @PathParam("deviceId") String deviceId,
								   @PathParam("ip") String deviceIP,
								   @Context HttpServletResponse response) {
		String result;

		log.info("Got register call from IP: " + deviceIP + " for Device ID: " + deviceId +
						 " of owner: " + owner);

		deviceToIpMap.put(deviceId, deviceIP);

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
																				   VirtualFireAlarmConstants.DEVICE_TYPE))) {
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

		String deviceIP = deviceToIpMap.get(deviceId);
		if (deviceIP == null) {
			response.setStatus(Response.Status.PRECONDITION_FAILED.getStatusCode());
			return;
		}

		String protocolString = protocol.toUpperCase();
		String callUrlPattern = BULB_CONTEXT + switchToState;

		log.info("Sending command: '" + callUrlPattern + "' to virtual-firealarm at: " + deviceIP + " " +
						 "via" + " " + protocolString);

		try {
			switch (protocolString) {
				case HTTP_PROTOCOL:
					sendCommandViaHTTP(deviceIP, 80, callUrlPattern, true);
					break;
				case MQTT_PROTOCOL:
					sendCommandViaMQTT(owner, deviceId, BULB_CONTEXT.replace("/", ""),
									   switchToState);
					break;
				case XMPP_PROTOCOL:
//					requestBulbChangeViaXMPP(switchToState, response);
					sendCommandViaXMPP(owner, deviceId, BULB_CONTEXT, switchToState);
					break;
				default:
					if (protocolString == null) {
						sendCommandViaHTTP(deviceIP, 80, callUrlPattern, true);
					} else {
						response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());
						return;
					}
					break;
			}
		} catch (DeviceManagementException e) {
			log.error("Failed to send command '" + callUrlPattern + "' to: " + deviceIP + " via" +
							  " " + protocol);
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

		String deviceIp = deviceToIpMap.get(deviceId);

		if (deviceIp == null) {
			replyMsg = "IP not registered for device: " + deviceId + " of owner: " + owner;
			response.setStatus(Response.Status.PRECONDITION_FAILED.getStatusCode());
			return replyMsg;
		}

		try {
			switch (protocol) {
				case HTTP_PROTOCOL:
					log.info("Sending request to read sonar value at : " + deviceIp +
									 " via " + HTTP_PROTOCOL);

					replyMsg = sendCommandViaHTTP(deviceIp, 80, SONAR_CONTEXT, false);
					break;

				case XMPP_PROTOCOL:
					log.info("Sending request to read sonar value at : " + deviceIp +
									 " via " +
									 XMPP_PROTOCOL);
					replyMsg = sendCommandViaXMPP(owner, deviceId, SONAR_CONTEXT, ".");
					break;

				default:
					if (protocol == null) {
						log.info("Sending request to read sonar value at : " + deviceIp +
										 " via " + HTTP_PROTOCOL);

						replyMsg = sendCommandViaHTTP(deviceIp, 80, SONAR_CONTEXT, false);
					} else {
						replyMsg = "Requested protocol '" + protocol + "' is not supported";
						response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());
						return replyMsg;
					}
					break;
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
	public String requestTemperature(@HeaderParam("owner") String owner,
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

		String deviceIp = deviceToIpMap.get(deviceId);

		if (deviceIp == null) {
			replyMsg = "IP not registered for device: " + deviceId + " of owner: " + owner;
			response.setStatus(Response.Status.PRECONDITION_FAILED.getStatusCode());
			return replyMsg;
		}

		try {
			switch (protocol) {
				case HTTP_PROTOCOL:
					log.info("Sending request to read virtual-firealarm-temperature at : " + deviceIp +
									 " via " + HTTP_PROTOCOL);

					replyMsg = sendCommandViaHTTP(deviceIp, 80, TEMPERATURE_CONTEXT, false);
					break;

				case XMPP_PROTOCOL:
					log.info("Sending request to read virtual-firealarm-temperature at : " + deviceIp +
									 " via " +
									 XMPP_PROTOCOL);
					replyMsg = sendCommandViaXMPP(owner, deviceId, TEMPERATURE_CONTEXT, ".");
//					replyMsg = requestTemperatureViaXMPP(response);
					break;

				default:
					if (protocol == null) {
						log.info("Sending request to read virtual-firealarm-temperature at : " + deviceIp +
										 " via " + HTTP_PROTOCOL);

						replyMsg = sendCommandViaHTTP(deviceIp, 80, TEMPERATURE_CONTEXT, false);
					} else {
						replyMsg = "Requested protocol '" + protocol + "' is not supported";
						response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());
						return replyMsg;
					}
					break;
			}
		} catch (DeviceManagementException e) {
			replyMsg = e.getErrorMessage();
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return replyMsg;
		}

		response.setStatus(Response.Status.OK.getStatusCode());
		replyMsg = "The current temperature of the device is " + replyMsg;
		return replyMsg;
	}


//	public String requestTemperatureViaXMPP(@Context HttpServletResponse response) {
//		String replyMsg = "";
//
//		String sep = File.separator;
//		String scriptsFolder = "repository" + sep + "resources" + sep + "scripts";
//		String scriptPath = CarbonUtils.getCarbonHome() + sep + scriptsFolder + sep
//				+ "xmpp_client.py -r Temperature";
//		String command = "python " + scriptPath;
//
//		replyMsg = executeCommand(command);
//
//		response.setStatus(HttpStatus.SC_OK);
//		return replyMsg;
//	}


//	public String requestSonarViaXMPP(@Context HttpServletResponse response) {
//		String replyMsg = "";
//
//		String sep = File.separator;
//		String scriptsFolder = "repository" + sep + "resources" + sep + "scripts";
//		String scriptPath = CarbonUtils.getCarbonHome() + sep + scriptsFolder + sep
//				+ "xmpp_client.py -r Sonar";
//		String command = "python " + scriptPath;
//
//		replyMsg = executeCommand(command);
//
//		response.setStatus(HttpStatus.SC_OK);
//		return replyMsg;
//	}


//	public String requestBulbChangeViaXMPP(String state,
//										   @Context HttpServletResponse response) {
//		String replyMsg = "";
//
//		String sep = File.separator;
//		String scriptsFolder = "repository" + sep + "resources" + sep + "scripts";
//		String scriptPath = CarbonUtils.getCarbonHome() + sep + scriptsFolder + sep
//				+ "xmpp_client.py -r Bulb -s " + state;
//		String command = "python " + scriptPath;
//
//		replyMsg = executeCommand(command);
//
//		response.setStatus(HttpStatus.SC_OK);
//		return replyMsg;
//	}

	@Path("controller/push_temperature")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void pushTemperatureData(
			final DeviceJSON dataMsg, @Context HttpServletResponse response) {
		boolean result;
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
							 deviceId + " is already registered under some other IP. Re-registration " + "required");
			response.setStatus(Response.Status.CONFLICT.getStatusCode());
			return;
		}

		PrivilegedCarbonContext.startTenantFlow();
		PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
		ctx.setTenantDomain(SUPER_TENANT, true);
		DeviceAnalyticsService deviceAnalyticsService = (DeviceAnalyticsService) ctx
				.getOSGiService(DeviceAnalyticsService.class, null);
		Object metdaData[] = {dataMsg.owner, VirtualFireAlarmConstants.DEVICE_TYPE, dataMsg.deviceId,
				System.currentTimeMillis()};
		Object payloadData[] = {temperature};
		try {
			deviceAnalyticsService.publishEvent(TEMPERATURE_STREAM_DEFINITION, "1.0.0",
												metdaData, new Object[0], payloadData);
		} catch (DataPublisherConfigurationException e) {
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

		} finally {
			PrivilegedCarbonContext.endTenantFlow();
		}


	}


	/*    Service to push all the sensor data collected by the FireAlarm
		   Called by the FireAlarm device  */

//	@Path("/pushalarmdata")
//	@POST
//	@Consumes(MediaType.APPLICATION_JSON)
//	public void pushAlarmData(final DeviceJSON dataMsg, @Context HttpServletResponse response) {
//		boolean result;
//		String sensorValues = dataMsg.value;
//		log.info("Recieved Sensor Data Values: " + sensorValues);
//
//		String sensors[] = sensorValues.split(":");
//		try {
//			if (sensors.length == 3) {
//				String temperature = sensors[0];
//				String bulb = sensors[1];
//				String sonar = sensors[2];

//				sensorValues = "Temperature:" + temperature + "C\tBulb Status:" + bulb +
//						"\t\tSonar Status:" + sonar;
//				log.info(sensorValues);
//				DeviceController deviceController = new DeviceController();
//				result = deviceController.pushBamData(dataMsg.owner, FireAlarmConstants
//															  .DEVICE_TYPE,
//													  dataMsg.deviceId,
//													  System.currentTimeMillis(), "DeviceData",
//													  temperature, "TEMPERATURE");
//
//				if (!result) {
//					response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
//					log.error("Error whilst pushing temperature: " + sensorValues);
//					return;
//				}
//
//				result = deviceController.pushBamData(dataMsg.owner, FireAlarmConstants
//															  .DEVICE_TYPE,
//													  dataMsg.deviceId,
//													  System.currentTimeMillis(), "DeviceData",
//													  bulb,
//													  "BULB");
//
//				if (!result) {
//					response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
//					log.error("Error whilst pushing Bulb data: " + sensorValues);
//					return;
//				}
//
//				result = deviceController.pushBamData(dataMsg.owner, FireAlarmConstants
//															  .DEVICE_TYPE,
//													  dataMsg.deviceId,
//													  System.currentTimeMillis(), "DeviceData",
//													  sonar,
//													  "SONAR");
//
//				if (!result) {
//					response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
//					log.error("Error whilst pushing Sonar data: " + sensorValues);
//				}
//
//			} else {
//				DeviceController deviceController = new DeviceController();
//				result = deviceController.pushBamData(dataMsg.owner, FireAlarmConstants
//															  .DEVICE_TYPE,
//													  dataMsg.deviceId,
//													  System.currentTimeMillis(), "DeviceData",
//													  dataMsg.value, dataMsg.reply);
//				if (!result) {
//					response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
//					log.error("Error whilst pushing sensor data: " + sensorValues);
//				}
//			}
//
//		} catch (UnauthorizedException e) {
//			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
//			log.error("Data Push Attempt Failed at Publisher: " + e.getMessage());
//		}
//	}


	private String sendCommandViaXMPP(String deviceOwner, String deviceId, String resource,
									  String state) throws DeviceManagementException {

		String replyMsg = "";
		String scriptArguments = "";
		String command = "";

		String seperator = File.separator;
		String xmppServerURL = XmppConfig.getInstance().getXmppEndpoint();
		int indexOfChar = xmppServerURL.lastIndexOf(seperator);
		if (indexOfChar != -1) {
			xmppServerURL = xmppServerURL.substring((indexOfChar + 1), xmppServerURL.length());
		}

		indexOfChar = xmppServerURL.indexOf(":");
		if (indexOfChar != -1) {
			xmppServerURL = xmppServerURL.substring(0, indexOfChar);
		}

		String xmppAdminUName = XmppConfig.getInstance().getXmppUsername();
		String xmppAdminPass = XmppConfig.getInstance().getXmppPassword();

		String xmppAdminUserLogin = xmppAdminUName + "@" + xmppServerURL + seperator + deviceOwner;
		String clientToConnect = deviceId + "@" + xmppServerURL + seperator + deviceOwner;

		String scriptsFolder = "repository" + seperator + "resources" + seperator + "scripts";
		String scriptPath = CarbonUtils.getCarbonHome() + seperator + scriptsFolder + seperator
				+ "xmpp_client.py ";

		scriptArguments =
				"-j " + xmppAdminUserLogin + " -p " + xmppAdminPass + " -c " + clientToConnect +
						" -r " + resource + " -s " + state;
		command = "python " + scriptPath + scriptArguments;

		if (log.isDebugEnabled()) {
			log.debug("Connecting to XMPP Server via Admin credentials: " + xmppAdminUserLogin);
			log.debug("Trying to contact xmpp device account: " + clientToConnect);
			log.debug("Arguments used for the scripts: '" + scriptArguments + "'");
			log.debug("Command exceuted: '" + command + "'");
		}

//		switch (resource) {
//			case BULB_CONTEXT:
//				scriptArguments = "-r Bulb -s " + state;
//				command = "python " + scriptPath + scriptArguments;
//				break;
//			case SONAR_CONTEXT:
//				scriptArguments = "-r Sonar";
//				command = "python " + scriptPath + scriptArguments;
//				break;
//			case TEMPERATURE_CONTEXT:
//				scriptArguments = "-r Temperature";
//				command = "python " + scriptPath + scriptArguments;
//				break;
//		}

		replyMsg = executeCommand(command);
		return replyMsg;
	}


	private String executeCommand(String command) {
		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader =
					new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			log.info(e.getMessage(), e);
		}

		return output.toString();

	}


	private boolean sendCommandViaMQTT(String deviceOwner, String deviceId, String resource,
									   String state) throws DeviceManagementException {

		boolean result = false;
		DeviceController deviceController = new DeviceController();

		try {
			result = deviceController.publishMqttControl(deviceOwner,
														 VirtualFireAlarmConstants.DEVICE_TYPE,
														 deviceId, resource, state);
		} catch (DeviceControllerException e) {
			String errorMsg = "Error whilst trying to publish to MQTT Queue";
			log.error(errorMsg);
			throw new DeviceManagementException(errorMsg, e);
		}
		return result;
	}


	private String sendCommandViaHTTP(final String deviceIp, int deviceServerPort,
									  String callUrlPattern,
									  boolean fireAndForgot)
			throws DeviceManagementException {

		if (deviceServerPort == 0) {
			deviceServerPort = 80;
		}

		String responseMsg = "";
		String urlString = URL_PREFIX + deviceIp + ":" + deviceServerPort + callUrlPattern;

		if (log.isDebugEnabled()) {
			log.debug(urlString);
		}

		if (!fireAndForgot) {
			HttpURLConnection httpConnection = getHttpConnection(urlString);

			try {
				httpConnection.setRequestMethod(HttpMethod.GET);
			} catch (ProtocolException e) {
				String errorMsg =
						"Protocol specific error occurred when trying to set method to GET" +
								" for:" + urlString;
				log.error(errorMsg);
				throw new DeviceManagementException(errorMsg, e);
			}

			responseMsg = readResponseFromGetRequest(httpConnection);

		} else {
			CloseableHttpAsyncClient httpclient = null;
			try {

				httpclient = HttpAsyncClients.createDefault();
				httpclient.start();
				HttpGet request = new HttpGet(urlString);
				final CountDownLatch latch = new CountDownLatch(1);
				Future<HttpResponse> future = httpclient.execute(
						request, new FutureCallback<HttpResponse>() {
							@Override
							public void completed(HttpResponse httpResponse) {
								latch.countDown();
							}

							@Override
							public void failed(Exception e) {
								latch.countDown();
							}

							@Override
							public void cancelled() {
								latch.countDown();
							}
						});

				latch.await();

			} catch (InterruptedException e) {
				if (log.isDebugEnabled()) {
					log.debug("Sync Interrupted");
				}
			} finally {
				try {
					if (httpclient != null) {
						httpclient.close();

					}
				} catch (IOException e) {
					if (log.isDebugEnabled()) {
						log.debug("Failed on close");
					}
				}
			}

		}

		return responseMsg;
	}

    /* Utility methods relevant to creating and sending http requests */

	/* This methods creates and returns a http connection object */

	private HttpURLConnection getHttpConnection(String urlString) throws
																  DeviceManagementException {

		URL connectionUrl = null;
		HttpURLConnection httpConnection = null;

		try {
			connectionUrl = new URL(urlString);
			httpConnection = (HttpURLConnection) connectionUrl.openConnection();
		} catch (MalformedURLException e) {
			String errorMsg =
					"Error occured whilst trying to form HTTP-URL from string: " + urlString;
			log.error(errorMsg);
			throw new DeviceManagementException(errorMsg, e);
		} catch (IOException e) {
			String errorMsg = "Error occured whilst trying to open a connection to: " +
					connectionUrl.toString();
			log.error(errorMsg);
			throw new DeviceManagementException(errorMsg, e);
		}

		return httpConnection;
	}

	/* This methods reads and returns the response from the connection */

	private String readResponseFromGetRequest(HttpURLConnection httpConnection)
			throws DeviceManagementException {
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(
					httpConnection.getInputStream()));
		} catch (IOException e) {
			String errorMsg =
					"There is an issue with connecting the reader to the input stream at: " +
							httpConnection.getURL();
			log.error(errorMsg);
			throw new DeviceManagementException(errorMsg, e);
		}

		String responseLine;
		StringBuffer completeResponse = new StringBuffer();

		try {
			while ((responseLine = bufferedReader.readLine()) != null) {
				completeResponse.append(responseLine);
			}
		} catch (IOException e) {
			String errorMsg =
					"Error occured whilst trying read from the connection stream at: " +
							httpConnection.getURL();
			log.error(errorMsg);
			throw new DeviceManagementException(errorMsg, e);
		}
		try {
			bufferedReader.close();
		} catch (IOException e) {
			log.error(
					"Could not succesfully close the bufferedReader to the connection at: " +
							httpConnection.getURL());
		}

		return completeResponse.toString();
	}

}
