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

package org.homeautomation.doormanager.controller.api.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.homeautomation.doormanager.plugin.constants.DoorManagerConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.analytics.service.DeviceAnalyticsService;

public class DoorManagerServiceUtils {
    private static final Log log = LogFactory.getLog(DoorManagerServiceUtils.class);
    private static final String STREAM_DEFINITION = "org.wso2.iot.devices.smartLock";
    private static final String STREAM_DEFINITION_VERSION = "1.0.0";

    /**
     * Publish door locker current status to DAS
     *
     * @param owner    owner of the device
     * @param deviceId unique identifier of device
     * @param status   current status of lock:- 1: open, 0: close
     * @return status
     */
    public static boolean publishToDASLockerStatus(String owner, String deviceId, float status) throws
            DataPublisherConfigurationException {
        Object payloadCurrent[] = {status};
        return publishToDAS(owner, deviceId, payloadCurrent, STREAM_DEFINITION);
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
        Object metaData[] = {owner, DoorManagerConstants.DEVICE_TYPE, deviceId, System.currentTimeMillis()};
        try {
            deviceAnalyticsService.publishEvent(definition, STREAM_DEFINITION_VERSION, metaData,
                    new Object[0], payloadCurrent);
        } catch (DataPublisherConfigurationException e) {
            log.error(e);
            return false;
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return true;
    }
}
