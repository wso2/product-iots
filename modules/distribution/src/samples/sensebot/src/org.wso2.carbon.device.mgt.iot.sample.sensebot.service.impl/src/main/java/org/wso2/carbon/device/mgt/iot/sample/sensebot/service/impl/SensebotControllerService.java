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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.wso2.carbon.device.mgt.iot.common.DeviceController;
import org.wso2.carbon.device.mgt.iot.common.datastore.impl.DataStreamDefinitions;
import org.wso2.carbon.device.mgt.iot.common.exception.UnauthorizedException;
import org.wso2.carbon.device.mgt.iot.sample.sensebot.service.impl.util.DeviceJSON;
import org.wso2.carbon.device.mgt.iot.sample.sensebot.plugin.constants.SensebotConstants;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

public class SensebotControllerService {

	private static Log log = LogFactory.getLog(SensebotControllerService.class);

	private static final Map<String, String> deviceIPList = new HashMap<>();

	private static final String URL_PREFIX = "http://";
	private static final String FORWARD_URL = "/move/F";
	private static final String BACKWARD_URL = "/move/B";
	private static final String LEFT_URL = "/move/L";
	private static final String RIGHT_URL = "/move/R";
	private static final String STOP_URL = "/move/S";
	private static CloseableHttpAsyncClient httpclient;

	static {
		httpclient = HttpAsyncClients.createDefault();
		httpclient.start();
	}

	@Path("/forward")
	@POST
	public void moveForward(@HeaderParam("owner") String owner,
							@HeaderParam("deviceId") String deviceId,
							@FormParam("ip") String deviceIp,
							@FormParam("port") int deviceServerPort) {

		sendCommand(deviceIp, deviceServerPort, FORWARD_URL);
	}

	@Path("/backward")
	@POST
	public void moveBackward(@HeaderParam("owner") String owner,
							 @HeaderParam("deviceId") String deviceId,
							 @FormParam("ip") String deviceIp,
							 @FormParam("port") int deviceServerPort) {

		sendCommand(deviceIp, deviceServerPort, BACKWARD_URL);
	}

	@Path("/left")
	@POST
	public void turnLeft(@HeaderParam("owner") String owner,
						 @HeaderParam("deviceId") String deviceId, @FormParam("ip") String
                                     deviceIp,
						 @FormParam("port") int deviceServerPort) {

		sendCommand(deviceIp, deviceServerPort, LEFT_URL);
	}

	@Path("/right")
	@POST
	public void turnRight(@HeaderParam("owner") String owner,
						  @HeaderParam("deviceId") String deviceId,
						  @FormParam("ip") String deviceIp,
						  @FormParam("port") int deviceServerPort) {

		sendCommand(deviceIp, deviceServerPort, RIGHT_URL);
	}

	@Path("/stop")
	@POST
	public void stop(@HeaderParam("owner") String owner, @HeaderParam("deviceId") String deviceId,
					 @FormParam("ip") String deviceIp, @FormParam("port") int deviceServerPort) {

		sendCommand(deviceIp, deviceServerPort, STOP_URL);
	}

	@Path("/pushsensordata")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void pushSensorData(
			final DeviceJSON dataMsg, @Context HttpServletResponse response) {
		boolean result = false;

		String sensorValues = dataMsg.value;                            //TEMP-PIR-SONAR-LDR
		log.info("Recieved Sensor Data Values: " + sensorValues);

		String sensors[] = sensorValues.split(":");
		try {
			if (sensors.length == 4) {
				String temperature = sensors[0];
				String motion = sensors[1];
				String sonar = sensors[2];
				String light = sensors[3];

				if (sonar.equals("-1")) {
					sonar = "No Object";
				}

				sensorValues = "Temperature:" + temperature + "C\t\tMotion:" + motion +
                        "\tSonar:" +
						sonar + "\tLight:"
						+ light;

				if (log.isDebugEnabled()) {
					log.debug(sensorValues);
				}
				DeviceController deviceController = new DeviceController();
				result = deviceController.pushBamData(dataMsg.owner, SensebotConstants.DEVICE_TYPE,
													  dataMsg.deviceId,
													  System.currentTimeMillis(), "DeviceData",
													  temperature,
													  DataStreamDefinitions.StreamTypeLabel.TEMPERATURE);

				if (!result) {
					response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
					return;
				}

				result = deviceController.pushBamData(dataMsg.owner, SensebotConstants.DEVICE_TYPE,
													  dataMsg.deviceId,
													  System.currentTimeMillis(), "DeviceData",
													  motion,
													  DataStreamDefinitions.StreamTypeLabel.MOTION);

				if (!result) {
					response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
					return;
				}

				if (!sonar.equals("No Object")) {
					result = deviceController.pushBamData(dataMsg.owner,
														  SensebotConstants.DEVICE_TYPE,
														  dataMsg.deviceId,
														  System.currentTimeMillis(), "DeviceData",
														  sonar,
														  DataStreamDefinitions.StreamTypeLabel.SONAR);

					if (!result) {
						response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
						return;
					}

				}

				result = deviceController.pushBamData(dataMsg.owner, SensebotConstants.DEVICE_TYPE,
													  dataMsg.deviceId,
													  System.currentTimeMillis(), "DeviceData",
													  light,
													  DataStreamDefinitions.StreamTypeLabel.LIGHT);

				if (!result) {
					response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				}

			} else {
				response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			}

		} catch (UnauthorizedException e) {
			response.setStatus(HttpStatus.SC_UNAUTHORIZED);
		}

	}

	@Path("/pushtempdata")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void pushTempData(
			final DeviceJSON dataMsg, @Context HttpServletResponse response) {

		String temperature = dataMsg.value;                            //TEMP
		log.info("Recieved Sensor Data Values: " + temperature);

		if (log.isDebugEnabled()) {
			log.debug("Recieved Temperature Data Value: " + temperature + " degrees C");
		}
		try {
			DeviceController deviceController = new DeviceController();
			boolean result = deviceController.pushBamData(dataMsg.owner,
														  SensebotConstants.DEVICE_TYPE,
														  dataMsg.deviceId,
														  System.currentTimeMillis(), "DeviceData",
														  temperature,
														  DataStreamDefinitions.StreamTypeLabel.TEMPERATURE);

			if (!result) {
				response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			}

		} catch (UnauthorizedException e) {
			response.setStatus(HttpStatus.SC_UNAUTHORIZED);

		}
	}

	@Path("/pushpirdata")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void pushPIRData(final DeviceJSON dataMsg,
							@Context HttpServletResponse response) {

		String motion = dataMsg.value;                            //PIR
		log.info("Recieved Sensor Data Values: " + motion);

		if (log.isDebugEnabled()) {
			log.debug("Recieved PIR (Motion) Sensor Data Value: " + motion);
		}
		try {
			DeviceController deviceController = new DeviceController();
			boolean result = deviceController.pushBamData(dataMsg.owner,
														  SensebotConstants.DEVICE_TYPE,
														  dataMsg.deviceId,
														  System.currentTimeMillis(), "DeviceData",
														  motion,
														  DataStreamDefinitions.StreamTypeLabel.MOTION);

			if (!result) {
				response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			}

		} catch (UnauthorizedException e) {
			response.setStatus(HttpStatus.SC_UNAUTHORIZED);

		}

	}

	@Path("/pushsonardata")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void pushSonarData(
			final DeviceJSON dataMsg, @Context HttpServletResponse response) {

		String sonar = dataMsg.value;                            //SONAR
		log.info("Recieved Sensor Data Values: " + sonar);

		if (sonar.equals("-1")) {
			if (log.isDebugEnabled()) {
				log.debug("Recieved a 'No Obstacle' Sonar value. (Means there are no abstacles " +
								  "within 30cm)");
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Recieved Sonar Sensor Data Value: " + sonar + " cm");
			}
			try {

				DeviceController deviceController = new DeviceController();
				boolean result = deviceController.pushBamData(dataMsg.owner,
															  SensebotConstants.DEVICE_TYPE,
															  dataMsg.deviceId,
															  System.currentTimeMillis(),
															  "DeviceData", sonar,
															  DataStreamDefinitions.StreamTypeLabel.SONAR);

				if (!result) {
					response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				}

			} catch (UnauthorizedException e) {
				response.setStatus(HttpStatus.SC_UNAUTHORIZED);

			}

		}

	}

	@Path("/pushlightdata")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void pushlightData(
			final DeviceJSON dataMsg, @Context HttpServletResponse response) {

		String light = dataMsg.value;                            //LDR
		log.info("Recieved Sensor Data Values: " + light);

		if (log.isDebugEnabled()) {
			log.debug("Recieved LDR (Light) Sensor Data Value: " + light);
		}

		try {
			DeviceController deviceController = new DeviceController();
			boolean result = deviceController.pushBamData(dataMsg.owner,
														  SensebotConstants.DEVICE_TYPE,
														  dataMsg.deviceId,
														  System.currentTimeMillis(), "DeviceData",
														  light,
														  DataStreamDefinitions.StreamTypeLabel.LIGHT);

			if (!result) {
				response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			}

		} catch (UnauthorizedException e) {
			response.setStatus(HttpStatus.SC_UNAUTHORIZED);

		}

	}

	private void sendCommand(String deviceIp, int deviceServerPort, String motionType) {

		if (deviceServerPort == 0) {
			deviceServerPort = 80;
		}

		String urlString = URL_PREFIX + deviceIp + ":" + deviceServerPort + motionType;
		if (log.isDebugEnabled()) {
			log.debug(urlString);
		}
		HttpGet request = new HttpGet(urlString);
		httpclient.execute(request, null);
	}

}
