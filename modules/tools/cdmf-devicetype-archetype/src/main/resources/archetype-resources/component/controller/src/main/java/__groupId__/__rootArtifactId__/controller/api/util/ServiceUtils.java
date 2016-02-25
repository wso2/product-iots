package ${groupId}.${rootArtifactId}.controller.api.util;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ${groupId}.${rootArtifactId}.plugin.constants.DeviceTypeConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.analytics.service.DeviceAnalyticsService;

public class ServiceUtils {
    private static final Log log = LogFactory.getLog(ServiceUtils.class);

    //TODO; replace this tenant domain
    private static final String SUPER_TENANT = "carbon.super";
    private static final String SENSOR_STREAM_DEFINITION = "org.wso2.iot.devices.${nameOfTheSensor}";
    private static final String SENSOR_STREAM_VERSION = "1.0.0";

    /**
     * sensor data are published to DAS
     * @param owner name of device owner
     * @param deviceId unique identifier of the device
     * @param sensorValue current value of sensor which is set at agent side
     * @return
     */
    public static boolean publishToDASSensorValue(String owner, String deviceId, float sensorValue) {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        ctx.setTenantDomain(SUPER_TENANT, true);
        DeviceAnalyticsService deviceAnalyticsService = (DeviceAnalyticsService) ctx.getOSGiService(
                DeviceAnalyticsService.class, null);
        Object metdaData[] = {owner, DeviceTypeConstants.DEVICE_TYPE, deviceId, System.currentTimeMillis()};
        Object payloadCurrent[] = {sensorValue};
        try {
            deviceAnalyticsService.publishEvent(SENSOR_STREAM_DEFINITION, SENSOR_STREAM_VERSION, metdaData,
                    new Object[0], payloadCurrent);
        } catch (DataPublisherConfigurationException e) {
            return false;
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return true;
    }
}
