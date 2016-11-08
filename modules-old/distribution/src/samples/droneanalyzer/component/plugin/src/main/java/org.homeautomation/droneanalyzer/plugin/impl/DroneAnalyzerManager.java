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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.homeautomation.droneanalyzer.plugin.exception.DeviceMgtPluginException;
import org.homeautomation.droneanalyzer.plugin.impl.dao.DroneAnalyzerDAO;
import org.homeautomation.droneanalyzer.plugin.impl.feature.DroneAnalyzerFeatureManager;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManagementException;

import java.util.List;


/**
 * This represents the ggg implementation of DeviceManagerService.
 */
public class DroneAnalyzerManager implements DeviceManager {

    private static final Log log = LogFactory.getLog(DroneAnalyzerManager.class);
    private static final DroneAnalyzerDAO deviceTypeDAO = new DroneAnalyzerDAO();
    private FeatureManager featureManager = new DroneAnalyzerFeatureManager();

    @Override
    public FeatureManager getFeatureManager() {
        return featureManager;
    }

    @Override
    public boolean saveConfiguration(PlatformConfiguration platformConfiguration) throws DeviceManagementException {
        return false;
    }

    @Override
    public PlatformConfiguration getConfiguration() throws DeviceManagementException {
        return null;
    }

    @Override
    public boolean enrollDevice(Device device) throws DeviceManagementException {
        boolean status;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Enrolling a new droneanalyzer device : " + device.getDeviceIdentifier());
            }
            DroneAnalyzerDAO.beginTransaction();
            status = deviceTypeDAO.getDroneAnalyzerDAO().addDevice(device);
            DroneAnalyzerDAO.commitTransaction();
        } catch (DeviceMgtPluginException e) {
            try {
                DroneAnalyzerDAO.rollbackTransaction();
            } catch (DeviceMgtPluginException iotDAOEx) {
                log.warn("Error occurred while roll back the device enrol transaction :" + device.toString(), iotDAOEx);
            }
            String msg = "Error while enrolling the droneanalyzer device : " + device.getDeviceIdentifier();
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
                log.debug("Modifying the droneanalyzer device enrollment data");
            }
            DroneAnalyzerDAO.beginTransaction();
            status = deviceTypeDAO.getDroneAnalyzerDAO().updateDevice(device);
            DroneAnalyzerDAO.commitTransaction();
        } catch (DeviceMgtPluginException e) {
            try {
                DroneAnalyzerDAO.rollbackTransaction();
            } catch (DeviceMgtPluginException iotDAOEx) {
                String msg = "Error occurred while roll back the update device transaction :" + device.toString();
                log.warn(msg, iotDAOEx);
            }
            String msg = "Error while updating the enrollment of the droneanalyzer device : " +
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
                log.debug("Dis-enrolling droneanalyzer device : " + deviceId);
            }
            DroneAnalyzerDAO.beginTransaction();
            status = deviceTypeDAO.getDroneAnalyzerDAO().deleteDevice(deviceId.getId());
            DroneAnalyzerDAO.commitTransaction();
        } catch (DeviceMgtPluginException e) {
            try {
                DroneAnalyzerDAO.rollbackTransaction();
            } catch (DeviceMgtPluginException iotDAOEx) {
                String msg = "Error occurred while roll back the device dis enrol transaction :" + deviceId.toString();
                log.warn(msg, iotDAOEx);
            }
            String msg = "Error while removing the droneanalyzer device : " + deviceId.getId();
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
                log.debug("Checking the enrollment of droneanalyzer device : " + deviceId.getId());
            }
            Device iotDevice =
                    deviceTypeDAO.getDroneAnalyzerDAO().getDevice(deviceId.getId());
            if (iotDevice != null) {
                isEnrolled = true;
            }
        } catch (DeviceMgtPluginException e) {
            String msg = "Error while checking the enrollment status of droneanalyzer device : " +
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
        try {
            if (log.isDebugEnabled()) {
                log.debug("Getting the details of droneanalyzer device : " + deviceId.getId());
            }
            device = deviceTypeDAO.getDroneAnalyzerDAO().getDevice(deviceId.getId());
        } catch (DeviceMgtPluginException e) {
            String msg = "Error while fetching the droneanalyzer device : " + deviceId.getId();
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
        return true;
    }

    @Override
    public boolean updateDeviceInfo(DeviceIdentifier deviceIdentifier, Device device) throws DeviceManagementException {
        boolean status;
        try {
            if (log.isDebugEnabled()) {
                log.debug("updating the details of droneanalyzer device : " + deviceIdentifier);
            }
            DroneAnalyzerDAO.beginTransaction();
            status = deviceTypeDAO.getDroneAnalyzerDAO().updateDevice(device);
            DroneAnalyzerDAO.commitTransaction();
        } catch (DeviceMgtPluginException e) {
            try {
                DroneAnalyzerDAO.rollbackTransaction();
            } catch (DeviceMgtPluginException iotDAOEx) {
                String msg = "Error occurred while roll back the update device info transaction :" + device.toString();
                log.warn(msg, iotDAOEx);
            }
            String msg =
                    "Error while updating the droneanalyzer device : " + deviceIdentifier;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return status;
    }

    @Override
    public List<Device> getAllDevices() throws DeviceManagementException {
        List<Device> devices;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Fetching the details of all droneanalyzer devices");
            }
            devices = deviceTypeDAO.getDroneAnalyzerDAO().getAllDevices();
        } catch (DeviceMgtPluginException e) {
            String msg = "Error while fetching all droneanalyzer devices.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return devices;
    }
}