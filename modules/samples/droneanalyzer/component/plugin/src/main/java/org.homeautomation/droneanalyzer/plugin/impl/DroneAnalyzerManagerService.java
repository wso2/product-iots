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

package org.homeautomation.droneanalyzer.plugin.impl;

import org.homeautomation.droneanalyzer.plugin.constants.DroneAnalyzerConstants;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.ProvisioningConfig;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.DeviceManager;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManager;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationConfig;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;

import java.util.List;

public class DroneAnalyzerManagerService implements DeviceManagementService {
    private DeviceManager deviceManager;

    @Override
    public String getType() {
        return DroneAnalyzerConstants.DEVICE_TYPE;
    }

    @Override
    public void init() throws DeviceManagementException {
        this.deviceManager = new DroneAnalyzerManager();
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
        return new ProvisioningConfig(DroneAnalyzerConstants.DEVICE_TYPE_PROVIDER_DOMAIN, false);
    }

    @Override
    public PushNotificationConfig getPushNotificationConfig() {
        return null;
    }

}
