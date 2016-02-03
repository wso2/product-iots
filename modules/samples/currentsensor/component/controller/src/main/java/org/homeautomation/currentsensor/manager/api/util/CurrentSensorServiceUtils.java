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

package org.homeautomation.currentsensor.manager.api.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.homeautomation.currentsensor.plugin.constants.CurrentSensorConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.analytics.service.DeviceAnalyticsService;

public class CurrentSensorServiceUtils {
    private static final Log log = LogFactory.getLog(CurrentSensorServiceUtils.class);

    //TODO; replace this tenant domain
    private static final String SUPER_TENANT = "carbon.super";
    private static final String CURRENT_STREAM_DEFINITION = "org.wso2.iot.devices.current";
    private static final String POWER_STREAM_DEFINITION = "org.wso2.iot.devices.power";

    public static boolean publishToDASCurrent(String owner, String deviceId, float current) {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        ctx.setTenantDomain(SUPER_TENANT, true);
        DeviceAnalyticsService deviceAnalyticsService = (DeviceAnalyticsService) ctx.getOSGiService(
                DeviceAnalyticsService.class, null);
        Object metdaData[] = {owner, CurrentSensorConstants.DEVICE_TYPE, deviceId, System.currentTimeMillis()};
        Object payloadCurrent[] = {current};

        try {
            deviceAnalyticsService.publishEvent(CURRENT_STREAM_DEFINITION, "1.0.0", metdaData, new Object[0], payloadCurrent);
        } catch (DataPublisherConfigurationException e) {
            return false;
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return true;
    }

    public static boolean publishToDASPower(String owner, String deviceId, float power) {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        ctx.setTenantDomain(SUPER_TENANT, true);
        DeviceAnalyticsService deviceAnalyticsService = (DeviceAnalyticsService) ctx.getOSGiService(
                DeviceAnalyticsService.class, null);
        Object metdaData[] = {owner, CurrentSensorConstants.DEVICE_TYPE, deviceId, System.currentTimeMillis()};
        Object payloadPower[] = {power};

        try {
            deviceAnalyticsService.publishEvent(POWER_STREAM_DEFINITION, "1.0.0", metdaData, new Object[0], payloadPower);
        } catch (DataPublisherConfigurationException e) {
            return false;
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return true;
    }
}
