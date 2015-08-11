package org.wso2.carbon.device.mgt.iot.sample.sensebot.plugin.impl;

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.DeviceManager;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManager;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.iot.sample.sensebot.plugin.constants.SensebotConstants;

import java.util.List;

public class SensebotManagerService implements DeviceManagementService {
	private DeviceManager deviceManager;
	@Override
	public String getType() {
		return SensebotConstants.DEVICE_TYPE;
	}

	@Override
	public void init() throws DeviceManagementException {
		deviceManager=new SensebotManager();
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
	public void installApplication(Operation operation, List<DeviceIdentifier> deviceIdentifiers)
			throws ApplicationManagementException {

	}
}
