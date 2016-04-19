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

package org.homeautomation.doormanager.api.util;

import org.wso2.carbon.apimgt.application.extension.APIManagementProviderService;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.identity.jwt.client.extension.service.JWTClientManagerService;

/**
 * This class provides utility functions used by REST-API.
 */
public class APIUtil {


    public static String getAuthenticatedUser() {
        PrivilegedCarbonContext threadLocalCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String username = threadLocalCarbonContext.getUsername();
        String tenantDomain = threadLocalCarbonContext.getTenantDomain();
        if (username.endsWith(tenantDomain)) {
            return username.substring(0, username.lastIndexOf("@"));
        }
        return username;
    }

    public static String getTenantDomainOftheUser() {
        PrivilegedCarbonContext threadLocalCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String tenantDomain = threadLocalCarbonContext.getTenantDomain();
        return tenantDomain;
    }

    public static DeviceManagementProviderService getDeviceManagementService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        DeviceManagementProviderService deviceManagementProviderService =
                (DeviceManagementProviderService) ctx.getOSGiService(DeviceManagementProviderService.class, null);
        if (deviceManagementProviderService == null) {
            String msg = "Device Management service has not initialized.";
            throw new IllegalStateException(msg);
        }
        return deviceManagementProviderService;
    }

    public static APIManagementProviderService getAPIManagementProviderService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        APIManagementProviderService apiManagementProviderService =
                (APIManagementProviderService) ctx.getOSGiService(DeviceManagementProviderService.class, null);
        if (apiManagementProviderService == null) {
            String msg = "API management provider service has not initialized.";
            throw new IllegalStateException(msg);
        }
        return apiManagementProviderService;
    }

    public static JWTClientManagerService getJWTClientManagerService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        JWTClientManagerService jwtClientManagerService =
                (JWTClientManagerService) ctx.getOSGiService(JWTClientManagerService.class, null);
        if (jwtClientManagerService == null) {
            String msg = "JWT Client manager service has not initialized.";
            throw new IllegalStateException(msg);
        }
        return jwtClientManagerService;
    }

}
