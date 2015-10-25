/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.iot.common.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.common.sensormgt.SensorDataManager;
import org.wso2.carbon.device.mgt.iot.common.sensormgt.SensorRecord;
import org.wso2.carbon.device.mgt.iot.sample.android.sense.plugin.constants.AndroidSenseConstants;
import org.wso2.carbon.device.mgt.iot.common.DeviceManagement;
import org.wso2.carbon.device.mgt.iot.sample.android.sense.service.impl.util.DeviceJSON;
import org.wso2.carbon.device.mgt.iot.sample.android.sense.service.impl.util.SensorJSON;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;

public class AndroidSenseService {



	private static Log log = LogFactory.getLog(AndroidSenseService.class);
	//TODO; replace this tenant domain
	private final String SUPER_TENANT = "carbon.super";
	private static final String BATTERY_STREAM_DEFINITION = "org.wso2.iot.devices.battery";
	private static final String LIGHT_STREAM_DEFINITION = "org.wso2.iot.devices.light";
	private static final String GPS_STREAM_DEFINITION = "org.wso2.iot.devices.gps";
	private static final String MAGNETIC_STREAM_DEFINITION = "org.wso2.iot.devices.magnetic";

	private static final String SENSOR_LIGHT = "light";
	private static final String SENSOR_GPS = "gps";
	private static final String SENSOR_BATTERY = "battery";
	private static final String SENSOR_MAGNETIC = "magnetic";

	@Context  //injected response proxy supporting multiple thread
	private HttpServletResponse response;



	@Path("manager/device")
	@PUT
	public boolean register(@FormParam("deviceId") String deviceId,
							@FormParam("owner") String owner) {


		DeviceManagement deviceManagement = new DeviceManagement(SUPER_TENANT);

		DeviceIdentifier deviceIdentifier = new DeviceIdentifier();
		deviceIdentifier.setId(deviceId);
		deviceIdentifier.setType(AndroidSenseConstants.DEVICE_TYPE);
		try {
			if (deviceManagement.getDeviceManagementService().isEnrolled(deviceIdentifier)) {
				response.setStatus(Response.Status.CONFLICT.getStatusCode());
				return true;
			}
			Device device = new Device();
			device.setDeviceIdentifier(deviceId);
			EnrolmentInfo enrolmentInfo = new EnrolmentInfo();

			enrolmentInfo.setDateOfEnrolment(new Date().getTime());
			enrolmentInfo.setDateOfLastUpdate(new Date().getTime());
			enrolmentInfo.setStatus(EnrolmentInfo.Status.ACTIVE);
			enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.BYOD);
			String name = owner + " android " + deviceId;
			device.setName(name);
			device.setType(AndroidSenseConstants.DEVICE_TYPE);
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
		deviceIdentifier.setType(AndroidSenseConstants.DEVICE_TYPE);
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
		deviceIdentifier.setType(AndroidSenseConstants.DEVICE_TYPE);
		try {
			Device device = deviceManagement.getDeviceManagementService().getDevice(
					deviceIdentifier);
			device.setDeviceIdentifier(deviceId);

			// device.setDeviceTypeId(deviceTypeId);
			device.getEnrolmentInfo().setDateOfLastUpdate(new Date().getTime());

			device.setName(name);
			device.setType(AndroidSenseConstants.DEVICE_TYPE);

			boolean updated = deviceManagement.getDeviceManagementService().modifyEnrollment(device);

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
		deviceIdentifier.setType(AndroidSenseConstants.DEVICE_TYPE);

		try {
			return deviceManagement.getDeviceManagementService().getDevice(
					deviceIdentifier);

		} catch (DeviceManagementException e) {
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return null;
		} finally {
			deviceManagement.endTenantFlow();
		}

	}

	/*    Service to push all the sensor data collected by the Android
		   Called by the Android device  */
	@Path("controller/sensordata")
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
		String sensorName = null;

		for(SensorJSON sensor: sensorData){
			switch (sensor.key){
				case "battery" : streamDef = BATTERY_STREAM_DEFINITION;
					payloadData = new Object[]{Float.parseFloat(sensor.value)};
					sensorName = SENSOR_BATTERY;
					break;
				case "GPS" : streamDef = GPS_STREAM_DEFINITION;
					String gpsValue =sensor.value;
					String gpsValues[]=gpsValue.split(",");
					Float gpsValuesF[]= new Float[2];
					gpsValuesF[0] = Float.parseFloat(gpsValues[0]);
					gpsValuesF[1] = Float.parseFloat(gpsValues[0]);
					payloadData = gpsValuesF;
					sensorName = SENSOR_GPS;
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
							sensorName = SENSOR_MAGNETIC;
						}else if(androidSensorId==5){
							streamDef = LIGHT_STREAM_DEFINITION;
							sensorName = SENSOR_LIGHT;
							payloadData = new Object[]{Float.parseFloat(sensor.value)};
						}
					}catch(NumberFormatException e){
						continue;
					}

			}
			Object metdaData[] = {dataMsg.owner, AndroidSenseConstants.DEVICE_TYPE, dataMsg.deviceId, sensor.time};

			if(streamDef!=null && payloadData!=null && payloadData.length > 0) {
				try {
					SensorDataManager.getInstance().setSensorRecord(dataMsg.deviceId, sensorName, sensor.value, sensor.time);
					deviceAnalyticsService.publishEvent(streamDef, "1.0.0", metdaData, new Object[0], payloadData);
				} catch (DataPublisherConfigurationException e) {
					response.setStatus(Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
				}
			}

		}

	}


	//TODO below enpoints needs to be removed and cep websocket have to be added
	@Path("controller/readlight")
	@GET
	@Consumes("application/json")
	@Produces("application/json")
	public SensorRecord readLight(@HeaderParam("owner") String owner,
								  @HeaderParam("deviceId") String deviceId,
								  @Context HttpServletResponse response) {
		SensorRecord sensorRecord = null;

		try{
			sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId, SENSOR_LIGHT);
		}  catch (DeviceControllerException e){
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}

		response.setStatus(Response.Status.OK.getStatusCode());
		return sensorRecord;
	}

	@Path("controller/readbattery")
	@GET
	@Consumes("application/json")
	@Produces("application/json")
	public SensorRecord readBattery(@HeaderParam("owner") String owner,
									@HeaderParam("deviceId") String deviceId,
									@Context HttpServletResponse response) {
		SensorRecord sensorRecord = null;

		try{
			sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId, SENSOR_BATTERY);
		}  catch (DeviceControllerException e){
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}

		response.setStatus(Response.Status.OK.getStatusCode());
		return sensorRecord;
	}

	@Path("controller/readgps")
	@GET
	@Consumes("application/json")
	@Produces("application/json")
	public SensorRecord readGPS(@HeaderParam("owner") String owner,
								@HeaderParam("deviceId") String deviceId,
								@Context HttpServletResponse response) {
		SensorRecord sensorRecord = null;

		try{
			sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId, SENSOR_GPS);
		}  catch (DeviceControllerException e){
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}

		response.setStatus(Response.Status.OK.getStatusCode());
		return sensorRecord;
	}

	@Path("controller/readmagnetic")
	@GET
	@Consumes("application/json")
	@Produces("application/json")
	public SensorRecord readMagnetic(@HeaderParam("owner") String owner,
									 @HeaderParam("deviceId") String deviceId,
									 @Context HttpServletResponse response) {
		SensorRecord sensorRecord = null;

		try{
			sensorRecord = SensorDataManager.getInstance().getSensorRecord(deviceId, SENSOR_MAGNETIC);
		}  catch (DeviceControllerException e){
			response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
		}

		response.setStatus(Response.Status.OK.getStatusCode());
		return sensorRecord;
	}

}
