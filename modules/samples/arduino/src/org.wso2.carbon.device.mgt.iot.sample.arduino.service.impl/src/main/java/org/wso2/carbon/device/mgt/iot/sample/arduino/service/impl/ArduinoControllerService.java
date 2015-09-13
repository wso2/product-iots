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

package org.wso2.carbon.device.mgt.iot.sample.arduino.service.impl;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.sample.arduino.service.impl.util.DeviceJSON;
import org.wso2.carbon.device.mgt.iot.sample.arduino.service.impl.util.MqttArduinoSubscriber;
import org.wso2.carbon.device.mgt.iot.sample.arduino.plugin.constants.ArduinoConstants;
import org.wso2.carbon.device.mgt.iot.common.DeviceController;
import org.wso2.carbon.device.mgt.iot.common.datastore.impl.DataStreamDefinitions;
import org.wso2.carbon.device.mgt.iot.common.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.common.exception.UnauthorizedException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.*;

public class ArduinoControllerService {

	private static Log log = LogFactory.getLog(ArduinoControllerService.class);

	private static Map<String, LinkedList<String>> replyMsgQueue = new HashMap<>();
	private static Map<String, LinkedList<String>> internalControlsQueue = new HashMap<>();
	private static MqttArduinoSubscriber mqttArduinoSubscriber;


	public void setMqttArduinoSubscriber(MqttArduinoSubscriber mqttArduinoSubscriber) {
		ArduinoControllerService.mqttArduinoSubscriber = mqttArduinoSubscriber;
		try {
			mqttArduinoSubscriber.subscribe();
		} catch (DeviceManagementException e) {
			log.error(e.getErrorMessage());
		}
	}

	public MqttArduinoSubscriber getMqttArduinoSubscriber() {
		return mqttArduinoSubscriber;
	}

	public static Map<String, LinkedList<String>> getReplyMsgQueue() {
		return replyMsgQueue;
	}

	public static Map<String, LinkedList<String>> getInternalControlsQueue() {
		return internalControlsQueue;
	}

	/*    Service to switch arduino bulb (pin 13) between "ON" and "OFF"
			   Called by an external client intended to control the Arduino */
	@Path("/bulb/{deviceId}/{state}")
	@POST
	public void switchBulb(@QueryParam("owner") String owner,
						   @PathParam("deviceId") String deviceId,
						   @PathParam("state") String state,
						   @Context HttpServletResponse response) {

		String switchToState = state.toUpperCase();

		if (!switchToState.equals(ArduinoConstants.STATE_ON) && !switchToState.equals(
				ArduinoConstants.STATE_OFF)) {
			log.error("The requested state change shoud be either - 'ON' or 'OFF'");
			response.setStatus(HttpStatus.SC_BAD_REQUEST);
			return;
		}

		try {
			DeviceController deviceController = new DeviceController();
			boolean result = deviceController.publishMqttControl(owner,
																 ArduinoConstants.DEVICE_TYPE,
																 deviceId, "BULB", switchToState);
			if (result) {
				response.setStatus(HttpStatus.SC_ACCEPTED);

			} else {
				response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);

			}

		} catch (DeviceControllerException e) {
			response.setStatus(HttpStatus.SC_UNAUTHORIZED);

		}

	}

	/*    Service to poll the control-queue for the controls sent to the Arduino
			   Called by the Arduino device  */
	@Path("/readcontrols/{deviceId}")
	@GET
	public String readControls(@QueryParam("owner") String owner,
							   @PathParam("deviceId") String deviceId,
							   @Context HttpServletResponse response) {
		String result;
		LinkedList<String> deviceControlList = internalControlsQueue.get(deviceId);

		if (deviceControlList == null) {
			result = "No controls have been set for device " + deviceId + " of owner " + owner;
			response.setStatus(HttpStatus.SC_NO_CONTENT);
		} else {
			try {
				result = deviceControlList.remove(); //returns the  head value
				response.setStatus(HttpStatus.SC_ACCEPTED);

			} catch (NoSuchElementException ex) {
				result = "There are no more controls for device " + deviceId + " of owner " +
						owner;
				response.setStatus(HttpStatus.SC_NO_CONTENT);
			}
		}
		if (log.isDebugEnabled()) {
			log.debug(result);
		}

		return result;
	}


	/*    Service to push all the sensor data collected by the Arduino
		   Called by the Arduino device  */
	@Path("/pushdata")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void pushData(final DeviceJSON dataMsg, @Context HttpServletResponse response) {

		String temperature = dataMsg.value;                            //TEMP
		log.info("Recieved Sensor Data Values: " + temperature);

		if (log.isDebugEnabled()) {
			log.debug("Recieved Temperature Data Value: " + temperature + " degrees C");
		}
		try {
			DeviceController deviceController = new DeviceController();
			boolean result = deviceController.pushBamData(dataMsg.owner,
														  ArduinoConstants.DEVICE_TYPE,
														  dataMsg.deviceId,
														  System.currentTimeMillis(), "DeviceData",
														  temperature,
														  DataStreamDefinitions.StreamTypeLabel
																  .TEMPERATURE);

			if (!result) {
				response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			}

		} catch (UnauthorizedException e) {
			response.setStatus(HttpStatus.SC_UNAUTHORIZED);

		}
	}

	@Path("/test/{value}")
	@POST
	public void pushtData( @PathParam("value") String value,@Context HttpServletResponse response) {


		try {
			DeviceController deviceController = new DeviceController();
			deviceController.pushBamData("ayyoob", "firealarm", "deviceID1",
										 System.currentTimeMillis(), "DeviceData" ,value, DataStreamDefinitions.StreamTypeLabel
												 .TEMPERATURE);



		} catch (UnauthorizedException e) {
			response.setStatus(HttpStatus.SC_UNAUTHORIZED);

		}
	}
}
