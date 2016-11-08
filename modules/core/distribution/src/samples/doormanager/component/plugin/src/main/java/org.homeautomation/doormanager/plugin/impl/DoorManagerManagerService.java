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

package org.homeautomation.doormanager.plugin.impl;

import org.homeautomation.doormanager.plugin.constants.DoorManagerConstants;
import org.homeautomation.doormanager.plugin.internal.DoorManagerManagementDataHolder;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.DeviceManager;
import org.wso2.carbon.device.mgt.common.ProvisioningConfig;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManager;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationConfig;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.iot.devicetype.config.DeviceManagementConfiguration;

import java.util.HashMap;
import java.util.Map;

public class DoorManagerManagerService implements DeviceManagementService {
    private DeviceManager deviceManager;
    private PushNotificationConfig pushNotificationConfig;

    @Override
    public String getType() {
        return DoorManagerConstants.DEVICE_TYPE;
    }

    @Override
    public void init() throws DeviceManagementException {
        this.deviceManager = new DoorManagerManager();
        this.pushNotificationConfig = this.populatePushNotificationConfig();

    }

    private PushNotificationConfig populatePushNotificationConfig() {
        DeviceManagementConfiguration deviceManagementConfiguration = DoorManagerManagementDataHolder.getInstance()
                .getDeviceTypeConfigService().getConfiguration(DoorManagerConstants.DEVICE_TYPE,
                        DoorManagerConstants.DEVICE_TYPE_PROVIDER_DOMAIN);
        org.wso2.carbon.device.mgt.iot.devicetype.config.PushNotificationConfig sourceConfig =
                deviceManagementConfiguration.getPushNotificationConfig();
        Map<String, String> staticProps = new HashMap<>();
        for (org.wso2.carbon.device.mgt.iot.devicetype.config.PushNotificationConfig.Property
                property : sourceConfig.getProperties()) {
            staticProps.put(property.getName(), property.getValue());
        }
        return new PushNotificationConfig(sourceConfig.getPushNotificationProvider(), staticProps);
    }


    @Override
    public DeviceManager getDeviceManager() {
        return deviceManager;
    }

    @Override
    public ApplicationManager getApplicationManager() {
        return null;
    }

    @Override
    public ProvisioningConfig getProvisioningConfig() {
        DeviceManagementConfiguration deviceManagementConfiguration = DoorManagerManagementDataHolder.getInstance()
                .getDeviceTypeConfigService().getConfiguration(DoorManagerConstants.DEVICE_TYPE,
                        DoorManagerConstants.DEVICE_TYPE_PROVIDER_DOMAIN);
        boolean sharedWithAllTenants = deviceManagementConfiguration.getDeviceManagementConfigRepository()
                .getProvisioningConfig().isSharedWithAllTenants();
        return new ProvisioningConfig(DoorManagerConstants.DEVICE_TYPE_PROVIDER_DOMAIN, sharedWithAllTenants);

    }

    @Override
    public PushNotificationConfig getPushNotificationConfig() {
        return pushNotificationConfig;
    }

}
