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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.homeautomation.firealarm.controller.api.util;

import org.homeautomation.firealarm.plugin.constants.DeviceTypeConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.analytics.service.DeviceAnalyticsService;

public class ServiceUtils {

    private static final String ORG_WSO2_IOT_DEVICES_TEMPERATURE = "org.wso2.iot.devices.temperature";
    private static final String ORG_WSO2_IOT_DEVICES_HUMIDITY = "org.wso2.iot.devices.humidity";
    private static final String SENSOR_STREAM_VERSION = "1.0.0";

    /**
     * sensor data are published to DAS
     *
     * @param owner       name of device owner
     * @param deviceId    unique identifier of the device
     * @param sensorValue current value of sensor which is set at agent side
     * @return status
     */
    public static boolean publishTemperatureToDAS(String owner, String deviceId,
                                                  String sensorValue) {
        float temperature = Float.parseFloat(sensorValue);
        Object payloadCurrent[] = {temperature};
        return publishToDAS(owner, deviceId, payloadCurrent, ORG_WSO2_IOT_DEVICES_TEMPERATURE);
    }

    /**
     * sensor data are published to DAS
     *
     * @param owner       name of device owner
     * @param deviceId    unique identifier of the device
     * @param sensorValue current value of sensor which is set at agent side
     * @return status
     */
    public static boolean publishHumidityToDAS(String owner, String deviceId,
                                               String sensorValue) {
        float humidity = Float.parseFloat(sensorValue);
        Object payloadCurrent[] = {humidity};
        return publishToDAS(owner, deviceId, payloadCurrent, ORG_WSO2_IOT_DEVICES_HUMIDITY);
    }

    private static boolean publishToDAS(String owner, String deviceId, Object[] payloadCurrent,
                                        String definition) {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        ctx.setUsername(owner);
        if (ctx.getTenantDomain(true) == null) {
            ctx.setTenantDomain("carbon.super", true);
        }
        DeviceAnalyticsService deviceAnalyticsService = (DeviceAnalyticsService) ctx.getOSGiService(
                DeviceAnalyticsService.class, null);
        Object metaData[] = {owner, DeviceTypeConstants.DEVICE_TYPE, deviceId, System.currentTimeMillis()};
        try {
            deviceAnalyticsService.publishEvent(definition, SENSOR_STREAM_VERSION, metaData,
                                                new Object[0], payloadCurrent);
        } catch (DataPublisherConfigurationException e) {
            return false;
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return true;
    }
}
