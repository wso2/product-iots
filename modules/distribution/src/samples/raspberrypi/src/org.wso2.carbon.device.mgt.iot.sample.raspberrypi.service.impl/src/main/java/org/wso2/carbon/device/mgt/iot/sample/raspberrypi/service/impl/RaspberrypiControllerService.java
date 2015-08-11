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

package org.wso2.carbon.device.mgt.iot.sample.raspberrypi.service.impl;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.iot.common.datastore.impl.DataStreamDefinitions;
import org.wso2.carbon.device.mgt.iot.sample.raspberrypi.service.impl.util.DeviceJSON;
import org.wso2.carbon.device.mgt.iot.sample.raspberrypi.plugin.constants.RaspberrypiConstants;
import org.wso2.carbon.device.mgt.iot.common.DeviceController;

import org.wso2.carbon.device.mgt.iot.common.exception.UnauthorizedException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

public class RaspberrypiControllerService {

	private static Log log = LogFactory.getLog(RaspberrypiControllerService.class);

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
														  RaspberrypiConstants.DEVICE_TYPE,
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
}
