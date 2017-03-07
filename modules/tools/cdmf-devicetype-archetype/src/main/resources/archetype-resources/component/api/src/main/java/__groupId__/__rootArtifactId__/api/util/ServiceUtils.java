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

package ${groupId}.${rootArtifactId}.api.util;

import ${groupId}.${rootArtifactId}.plugin.constants.DeviceTypeConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.device.mgt.analytics.data.publisher.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.analytics.data.publisher.service.EventsPublisherService;
import org.wso2.carbon.context.PrivilegedCarbonContext;


public class ServiceUtils {
    private static final Log log = LogFactory.getLog(ServiceUtils.class);

    /**
     * Sensor data are published to DAS
     *
     * @param deviceId    unique identifier of the device
     * @param sensorValue current value of sensor which is set at agent side
     * @return
     */
    public static boolean publishToDAS(String deviceId, float sensorValue) {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        EventsPublisherService deviceAnalyticsService = (EventsPublisherService) ctx.getOSGiService(
                EventsPublisherService.class, null);
        String owner = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        Object metdaData[] = {owner, DeviceTypeConstants.DEVICE_TYPE, deviceId, System.currentTimeMillis()};
        Object payloadData[] = {sensorValue};
        try {
            deviceAnalyticsService.publishEvent(DeviceTypeConstants.SENSOR_STREAM_DEFINITION,
                    DeviceTypeConstants.SENSOR_STREAM_DEFINITION_VERSION, metdaData, new Object[0], payloadData);
        } catch (DataPublisherConfigurationException e) {
            return false;
        }
        return true;
    }
}
