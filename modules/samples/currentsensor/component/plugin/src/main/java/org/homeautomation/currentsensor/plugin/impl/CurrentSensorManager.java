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

package org.homeautomation.currentsensor.plugin.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.homeautomation.currentsensor.plugin.exception.CurrentSensorDeviceMgtPluginException;
import org.homeautomation.currentsensor.plugin.impl.dao.CurrentSensorDAOUtil;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.configuration.mgt.TenantConfiguration;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManagementException;
import java.util.List;

/**
 * This represents the Current Sensor implementation of DeviceManagerService.
 */
public class CurrentSensorManager implements DeviceManager {

    private static final Log log = LogFactory.getLog(CurrentSensorManager.class);
    private static final CurrentSensorDAOUtil CURRENT_SENSOR_DAO_UTIL = new CurrentSensorDAOUtil();

    @Override
    public FeatureManager getFeatureManager() {
        return null;
    }

    @Override
    public boolean saveConfiguration(TenantConfiguration tenantConfiguration)
            throws DeviceManagementException {
        //TODO implement this
        return false;
    }

    @Override
    public TenantConfiguration getConfiguration() throws DeviceManagementException {
        //TODO implement this
        return null;
    }

    @Override
    public boolean enrollDevice(Device device) throws DeviceManagementException {
        boolean status;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Enrolling a new Connected Cup device : " + device.getDeviceIdentifier());
            }
            CurrentSensorDAOUtil.beginTransaction();
            status = CURRENT_SENSOR_DAO_UTIL.getCurrentSensorDeviceDAO().addDevice(device);
            CurrentSensorDAOUtil.commitTransaction();
        } catch (CurrentSensorDeviceMgtPluginException e) {
            try {
                CurrentSensorDAOUtil.rollbackTransaction();
            } catch (CurrentSensorDeviceMgtPluginException iotDAOEx) {
                String msg = "Error occurred while roll back the device enrol transaction :" + device.toString();
                log.warn(msg, iotDAOEx);
            }
            String msg = "Error while enrolling the Connected Cup device : " + device.getDeviceIdentifier();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return status;
    }

    @Override
    public boolean modifyEnrollment(Device device) throws DeviceManagementException {
        boolean status;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Modifying the Connected Cup device enrollment data");
            }
            CurrentSensorDAOUtil.beginTransaction();
            status = CURRENT_SENSOR_DAO_UTIL.getCurrentSensorDeviceDAO().updateDevice(device);
            CurrentSensorDAOUtil.commitTransaction();
        } catch (CurrentSensorDeviceMgtPluginException e) {
            try {
                CurrentSensorDAOUtil.rollbackTransaction();
            } catch (CurrentSensorDeviceMgtPluginException iotDAOEx) {
                String msg = "Error occurred while roll back the update device transaction :" + device.toString();
                log.warn(msg, iotDAOEx);
            }
            String msg = "Error while updating the enrollment of the Connected Cup device : " +
                         device.getDeviceIdentifier();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return status;
    }

    @Override
    public boolean disenrollDevice(DeviceIdentifier deviceId) throws DeviceManagementException {
        boolean status;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Dis-enrolling Connected Cup device : " + deviceId);
            }
            CurrentSensorDAOUtil.beginTransaction();
            status = CURRENT_SENSOR_DAO_UTIL.getCurrentSensorDeviceDAO().deleteDevice(deviceId.getId());
            CurrentSensorDAOUtil.commitTransaction();
        } catch (CurrentSensorDeviceMgtPluginException e) {
            try {
                CurrentSensorDAOUtil.rollbackTransaction();
            } catch (CurrentSensorDeviceMgtPluginException iotDAOEx) {
                String msg = "Error occurred while roll back the device dis enrol transaction :" + deviceId.toString();
                log.warn(msg, iotDAOEx);
            }
            String msg = "Error while removing the Connected Cup device : " + deviceId.getId();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return status;
    }

    @Override
    public boolean isEnrolled(DeviceIdentifier deviceId) throws DeviceManagementException {
        boolean isEnrolled = false;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Checking the enrollment of Connected Cup device : " + deviceId.getId());
            }
            Device iotDevice = CURRENT_SENSOR_DAO_UTIL.getCurrentSensorDeviceDAO().getDevice(deviceId.getId());
            if (iotDevice != null) {
                isEnrolled = true;
            }
        } catch (CurrentSensorDeviceMgtPluginException e) {
            String msg = "Error while checking the enrollment status of Connected Cup device : " +
                         deviceId.getId();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return isEnrolled;
    }

    @Override
    public boolean isActive(DeviceIdentifier deviceId) throws DeviceManagementException {
        return true;
    }

    @Override
    public boolean setActive(DeviceIdentifier deviceId, boolean status)
            throws DeviceManagementException {
        return true;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceId) throws DeviceManagementException {
        Device device;
        try {if (log.isDebugEnabled()) {
            log.debug("Getting the details of Connected Cup device : " + deviceId.getId());
        }
            device = CURRENT_SENSOR_DAO_UTIL.getCurrentSensorDeviceDAO().getDevice(deviceId.getId());

        } catch (CurrentSensorDeviceMgtPluginException e) {
            String msg = "Error while fetching the Connected Cup device : " + deviceId.getId();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return device;
    }

    @Override
    public boolean setOwnership(DeviceIdentifier deviceId, String ownershipType)
            throws DeviceManagementException {
        return true;
    }

    public boolean isClaimable(DeviceIdentifier deviceIdentifier) throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean setStatus(DeviceIdentifier deviceId, String currentOwner,
                             EnrolmentInfo.Status status) throws DeviceManagementException {
        return false;
    }

    @Override
    public License getLicense(String s) throws LicenseManagementException {
        return null;
    }

    @Override
    public void addLicense(License license) throws LicenseManagementException {

    }

    @Override
    public boolean requireDeviceAuthorization() {
        return false;
    }

    @Override
    public boolean updateDeviceInfo(DeviceIdentifier deviceIdentifier, Device device) throws DeviceManagementException {
        boolean status;
        try {
            if (log.isDebugEnabled()) {
                log.debug(
                        "updating the details of Connected Cup device : " + deviceIdentifier);
            }
            CurrentSensorDAOUtil.beginTransaction();
            status = CURRENT_SENSOR_DAO_UTIL.getCurrentSensorDeviceDAO().updateDevice(device);
            CurrentSensorDAOUtil.commitTransaction();
        } catch (CurrentSensorDeviceMgtPluginException e) {
            try {
                CurrentSensorDAOUtil.rollbackTransaction();
            } catch (CurrentSensorDeviceMgtPluginException iotDAOEx) {
                String msg = "Error occurred while roll back the update device info transaction :" + device.toString();
                log.warn(msg, iotDAOEx);
            }
            String msg =
                    "Error while updating the Connected Cup device : " + deviceIdentifier;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return status;
    }

    @Override
    public List<Device> getAllDevices() throws DeviceManagementException {
        List<Device> devices = null;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Fetching the details of all Connected Cup devices");
            }
            devices = CURRENT_SENSOR_DAO_UTIL.getCurrentSensorDeviceDAO().getAllDevices();
        } catch (CurrentSensorDeviceMgtPluginException e) {
            String msg = "Error while fetching all Connected Cup devices.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return devices;
    }

}