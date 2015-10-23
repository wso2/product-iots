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

package org.wso2.carbon.device.mgt.iot.sample.android.sense.service.impl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.iot.sample.android.sense.service.impl.util.DeviceJSON;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class AndroidSenseControllerService {

	private static Log log = LogFactory.getLog(AndroidSenseControllerService.class);
	@Context  //injected response proxy supporting multiple thread
	private HttpServletResponse response;

	/*    Service to push all the sensor data collected by the Android
		   Called by the Android device  */
	@Path("/sensordata")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void pushSensorData(final DeviceJSON dataMsg, @Context HttpServletResponse response) {




		String temperature = dataMsg.owner;                            //TEMP
		log.info("Recieved Sensor Data Values: " + temperature);

		if (log.isDebugEnabled()) {
			log.debug("Recieved Temperature Data Value: " + temperature + " degrees C");
		}
		//try {
			boolean result = false;
// result=DeviceController.pushBamData(dataMsg.owner, AndroidSenseConstants.DEVICE_TYPE,
//													   dataMsg.deviceId,
//													   System.currentTimeMillis(), "DeviceData",
//													   temperature, "TEMPERATURE");

			if (!result) {
				response.setStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode());

			}
			//return result;
//		} catch (UnauthorizedException e) {
//			response.setStatus(HttpStatus.SC_UNAUTHORIZED);
//
//		}
	}
}
