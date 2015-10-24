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
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.analytics.service.DeviceAnalyticsService;
import org.wso2.carbon.device.mgt.iot.sample.android.sense.plugin.constants.AndroidSenseConstants;
import org.wso2.carbon.device.mgt.iot.sample.android.sense.service.impl.util.DeviceJSON;
import org.wso2.carbon.device.mgt.iot.sample.android.sense.service.impl.util.SensorJSON;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class AndroidSenseControllerService {

	private static Log log = LogFactory.getLog(AndroidSenseControllerService.class);
	@Context  //injected response proxy supporting multiple thread
	private HttpServletResponse response;
	private static final String BATTERY_STREAM_DEFINITION = "org.wso2.iot.devices.battery";
	private static final String LIGHT_STREAM_DEFINITION = "org.wso2.iot.devices.light";
	private static final String GPS_STREAM_DEFINITION = "org.wso2.iot.devices.gps";
	private static final String MAGNETIC_STREAM_DEFINITION = "org.wso2.iot.devices.magnetic";

	/*    Service to push all the sensor data collected by the Android
		   Called by the Android device  */
	@Path("/sensordata")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void pushSensorData(final DeviceJSON dataMsg, @Context HttpServletResponse response) {


		PrivilegedCarbonContext.startTenantFlow();
		PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
		ctx.setTenantDomain("carbon.super", true);
		DeviceAnalyticsService deviceAnalyticsService = (DeviceAnalyticsService) ctx.getOSGiService(
				DeviceAnalyticsService.class, null);


		SensorJSON[] sensorData = dataMsg.values;
		String streamDef = null;
		Object payloadData[] = null;

		for(SensorJSON sensor: sensorData){
			switch (sensor.key){
				case "light" : streamDef = LIGHT_STREAM_DEFINITION;
					payloadData = new Object[]{Float.parseFloat(sensor.value)};
					break;
				case "battery" : streamDef = BATTERY_STREAM_DEFINITION;
					payloadData = new Object[]{Float.parseFloat(sensor.value)};
					break;
				case "GPS" : streamDef = GPS_STREAM_DEFINITION;
					String gpsValue =sensor.value;
					String gpsValues[]=gpsValue.split(",");
					Float gpsValuesF[]= new Float[2];
					gpsValuesF[0] = Float.parseFloat(gpsValues[0]);
					gpsValuesF[1] = Float.parseFloat(gpsValues[0]);
					payloadData = gpsValuesF;
					break;
				default :
					try {
						int androidSensorId = Integer.parseInt(sensor.key);

						if(androidSensorId==2){
							streamDef = MAGNETIC_STREAM_DEFINITION;
							String value =sensor.value;
							String valuesM[]=value.split(",");
							Float gValuesF[]= new Float[1];
							gValuesF[0] = Float.parseFloat(valuesM[0])*Float.parseFloat(valuesM[0])*Float.parseFloat(valuesM[0]);
							payloadData = gValuesF;
						}else if(androidSensorId==5){
							streamDef = LIGHT_STREAM_DEFINITION;
							payloadData = new Object[]{Float.parseFloat(sensor.value)};
						}
					}catch(NumberFormatException e){
						continue;
					}

			}
			Object metdaData[] = {dataMsg.owner, AndroidSenseConstants.DEVICE_TYPE, dataMsg.deviceId, sensor.time};

			if(streamDef!=null && payloadData!=null && payloadData.length > 0) {
				try {
					deviceAnalyticsService.publishEvent(streamDef, "1.0.0",
														metdaData, new Object[0], payloadData);
				} catch (DataPublisherConfigurationException e) {
					response.setStatus(Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
				}
			}

		}

	}

}
