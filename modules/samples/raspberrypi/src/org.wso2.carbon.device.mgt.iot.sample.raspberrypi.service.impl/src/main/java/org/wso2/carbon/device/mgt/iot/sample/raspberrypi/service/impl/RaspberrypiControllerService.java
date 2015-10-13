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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.analytics.service.DeviceAnalyticsService;
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
import javax.ws.rs.core.Response;

public class RaspberrypiControllerService {

	private static Log log = LogFactory.getLog(RaspberrypiControllerService.class);
	private static final String TEMPERATURE_STREAM_DEFINITION = "org.wso2.iot.devices.temperature";
	//TODO; replace this tenant domain
	private final String SUPER_TENANT = "carbon.super";
	@Context  //injected response proxy supporting multiple thread
	private HttpServletResponse response;


	/*    Service to push all the sensor data collected by the Arduino
		   Called by the Arduino device  */
	@Path("/pushdata")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void pushData(final DeviceJSON dataMsg, @Context HttpServletResponse response) {

		float temperature = dataMsg.value;                            //TEMP
		log.info("Recieved Sensor Data Values: " + temperature);

		if (log.isDebugEnabled()) {
			log.debug("Recieved Temperature Data Value: " + temperature + " degrees C");
		}

		PrivilegedCarbonContext.startTenantFlow();
		PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
		ctx.setTenantDomain(SUPER_TENANT, true);
		DeviceAnalyticsService deviceAnalyticsService = (DeviceAnalyticsService) ctx
				.getOSGiService(DeviceAnalyticsService.class, null);
		Object metdaData[] = {dataMsg.owner, RaspberrypiConstants.DEVICE_TYPE, dataMsg.deviceId,
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
}
