package ${groupId}.${rootArtifactId}.plugin.impl;
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
import ${groupId}.${rootArtifactId}.plugin.constants.DeviceTypeConstants;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.DeviceManager;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManager;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;

import java.util.List;

public class DeviceTypeManagerService implements DeviceManagementService{
	private DeviceManager deviceManager;

	@Override
	public String getType() {
		return DeviceTypeConstants.DEVICE_TYPE;
	}

	@Override
	public String getProviderTenantDomain() {
		return "carbon.super";
	}

	@Override
	public boolean isSharedWithAllTenants() {
		return true;
	}

	@Override
	public String[] getSharedTenantsDomain() {
		return new String[0];
	}

	@Override
	public void init() throws DeviceManagementException {
		deviceManager= new DeviceTypeManager();
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
	public void notifyOperationToDevices(Operation operation, List<DeviceIdentifier> list) throws
			DeviceManagementException {
	}

	@Override
	public Application[] getApplications(String domain, int pageNumber, int size)
			throws ApplicationManagementException {
		return new Application[0];
	}

	@Override
	public void updateApplicationStatus(DeviceIdentifier deviceId, Application application,
										String status) throws ApplicationManagementException {

	}

	@Override
	public String getApplicationStatus(DeviceIdentifier deviceId, Application application)
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
}
