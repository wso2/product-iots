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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.homeautomation.doormanager.plugin.exception.DoorManagerDeviceMgtPluginException;
import org.homeautomation.doormanager.plugin.impl.dao.DoorLockSafe;
import org.homeautomation.doormanager.plugin.impl.dao.DoorManagerDAO;
import org.homeautomation.doormanager.plugin.impl.feature.DoorManagerFeatureManager;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManagementException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.List;


/**
 * This represents the door manager implementation of DeviceManagerService.
 */
public class DoorManagerManager implements DeviceManager {

    private static final Log log = LogFactory.getLog(DoorManagerManager.class);
    private static final DoorManagerDAO deviceTypeDAO = new DoorManagerDAO();
    private FeatureManager featureManager = new DoorManagerFeatureManager();

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
                log.debug("Enrolling a new doormanager device : " + device.getDeviceIdentifier());
            }
            DoorManagerDAO.beginTransaction();
            status = deviceTypeDAO.getDoorManagerDAO().addDevice(device);
            DoorManagerDAO.commitTransaction();
        } catch (DoorManagerDeviceMgtPluginException e) {
            try {
                DoorManagerDAO.rollbackTransaction();
            } catch (DoorManagerDeviceMgtPluginException iotDAOEx) {
                log.warn("Error occurred while roll back the device enrol transaction :" + device.toString(), iotDAOEx);
            }
            String msg = "Error while enrolling the door manager device : " + device.getDeviceIdentifier();
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
                log.debug("Modifying the door manager device enrollment data");
            }
            DoorManagerDAO.beginTransaction();
            status = deviceTypeDAO.getDoorManagerDAO().updateDevice(device);
            DoorManagerDAO.commitTransaction();
        } catch (DoorManagerDeviceMgtPluginException e) {
            try {
                DoorManagerDAO.rollbackTransaction();
            } catch (DoorManagerDeviceMgtPluginException iotDAOEx) {
                String msg = "Error occurred while roll back the update device transaction :" + device.toString();
                log.warn(msg, iotDAOEx);
            }
            String msg = "Error while updating the enrollment of the door manager device : " +
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
                log.debug("Dis-enrolling door manager device : " + deviceId);
            }
            DoorManagerDAO.beginTransaction();
            status = deviceTypeDAO.getDoorManagerDAO().deleteDevice(deviceId.getId());
            DoorManagerDAO.commitTransaction();
        } catch (DoorManagerDeviceMgtPluginException e) {
            try {
                DoorManagerDAO.rollbackTransaction();
            } catch (DoorManagerDeviceMgtPluginException iotDAOEx) {
                String msg = "Error occurred while roll back the device dis enrol transaction :" + deviceId.toString();
                log.warn(msg, iotDAOEx);
            }
            String msg = "Error while removing the door manager device : " + deviceId.getId();
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
                log.debug("Checking the enrollment of door manager device : " + deviceId.getId());
            }
            Device iotDevice =
                    deviceTypeDAO.getDoorManagerDAO().getDevice(deviceId.getId());
            if (iotDevice != null) {
                isEnrolled = true;
            }
        } catch (DoorManagerDeviceMgtPluginException e) {
            String msg = "Error while checking the enrollment status of door manager device : " +
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
                log.debug("Getting the details of door manager device : " + deviceId.getId());
            }
            device = deviceTypeDAO.getDoorManagerDAO().getDevice(deviceId.getId());
        } catch (DoorManagerDeviceMgtPluginException e) {
            String msg = "Error while fetching the door manager device : " + deviceId.getId();
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
                log.debug("updating the details of door manager device : " + deviceIdentifier);
            }
            DoorManagerDAO.beginTransaction();
            status = deviceTypeDAO.getDoorManagerDAO().updateDevice(device);
            DoorManagerDAO.commitTransaction();
        } catch (DoorManagerDeviceMgtPluginException e) {
            try {
                DoorManagerDAO.rollbackTransaction();
            } catch (DoorManagerDeviceMgtPluginException iotDAOEx) {
                String msg = "Error occurred while roll back the update device info transaction :" + device.toString();
                log.warn(msg, iotDAOEx);
            }
            String msg =
                    "Error while updating the door manager device : " + deviceIdentifier;
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
                log.debug("Fetching the details of all door manager devices");
            }
            devices = deviceTypeDAO.getDoorManagerDAO().getAllDevices();
        } catch (DoorManagerDeviceMgtPluginException e) {
            String msg = "Error while fetching all door manager devices.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return devices;
    }

    public boolean assignUserToLock(DoorLockSafe doorLockSafe) throws DoorManagerDeviceMgtPluginException {
        boolean status;
        try {
            DoorManagerDAO.beginTransaction();
            status = deviceTypeDAO.getDoorManagerDAO().registerDoorLockSafe(doorLockSafe);
            DoorManagerDAO.commitTransaction();
            return status;
        } catch (DoorManagerDeviceMgtPluginException e) {
            try {
                DoorManagerDAO.rollbackTransaction();
                throw new DoorManagerDeviceMgtPluginException(e);
            } catch (DoorManagerDeviceMgtPluginException e1) {
                String msg = "Error while adding new access card to user to control the lock "
                        + doorLockSafe.getOwner();
                log.error(msg, e);
                throw new DoorManagerDeviceMgtPluginException(msg, e);
            }
        }
    }

    public boolean checkCardDoorAssociation(String cardNumber, String deviceId)
            throws DoorManagerDeviceMgtPluginException {
        boolean status;
        DoorManagerDAO.beginTransaction();
        status = deviceTypeDAO.getDoorManagerDAO().checkCardDoorAssociation(cardNumber, deviceId);
        DoorManagerDAO.commitTransaction();
        return status;
    }
}
