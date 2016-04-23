/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.homeautomation.droneanalyzer.plugin.impl;

import org.homeautomation.droneanalyzer.plugin.constants.DroneConstants;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.DeviceManager;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManager;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import java.util.List;

public class DroneAnalyzerManagerService implements DeviceManagementService {
    private DeviceManager deviceManager;
    private final static String DEVICE_TYPE_PROVIDER_DOMAIN = "carbon.super";

    @Override
    public String getType() {
        return DroneConstants.DEVICE_TYPE;
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
    public void notifyOperationToDevices(Operation operation, List<DeviceIdentifier> list)
            throws DeviceManagementException {

    }

    @Override
    public Application[] getApplications(String s, int i, int i1) throws ApplicationManagementException {
        return new Application[0];
    }

    @Override
    public void updateApplicationStatus(DeviceIdentifier deviceIdentifier, Application application, String s)
            throws ApplicationManagementException {

    }

    @Override
    public String getApplicationStatus(DeviceIdentifier deviceIdentifier, Application application)
            throws ApplicationManagementException {
        return null;
    }

    @Override
    public void installApplicationForDevices(Operation operation, List<DeviceIdentifier> list)
            throws ApplicationManagementException {

    }

    @Override
    public void installApplicationForUsers(Operation operation, List<String> list)
            throws ApplicationManagementException {

    }

    @Override
    public void installApplicationForUserRoles(Operation operation, List<String> list)
            throws ApplicationManagementException {

    }

    @Override
    public String getProviderTenantDomain() {
        return DEVICE_TYPE_PROVIDER_DOMAIN;
    }

    @Override
    public boolean isSharedWithAllTenants() {
        return true;
    }

}
