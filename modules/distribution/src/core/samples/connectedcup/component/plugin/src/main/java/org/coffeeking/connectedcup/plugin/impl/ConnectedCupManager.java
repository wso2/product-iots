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

package org.coffeeking.connectedcup.plugin.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.coffeeking.connectedcup.plugin.exception.ConnectedCupDeviceMgtPluginException;
import org.coffeeking.connectedcup.plugin.impl.dao.ConnectedCupDAOUtil;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.DeviceManager;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.FeatureManager;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManagementException;
import java.util.List;

/**
 * Device Manager interface impl for connected cup.
 */
public class ConnectedCupManager implements DeviceManager {

    private static final Log log = LogFactory.getLog(ConnectedCupManager.class);

    private static final ConnectedCupDAOUtil CONNECTED_CUP_DAO_UTIL = new ConnectedCupDAOUtil();


    @Override
    public FeatureManager getFeatureManager() {
        return null;
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
                log.debug("Enrolling a new Connected Cup device : " + device.getDeviceIdentifier());
            }
            ConnectedCupDAOUtil.beginTransaction();
            status = CONNECTED_CUP_DAO_UTIL.getConnectedCupDeviceDAO().addDevice(device);
            ConnectedCupDAOUtil.commitTransaction();
        } catch (ConnectedCupDeviceMgtPluginException e) {
            try {
                ConnectedCupDAOUtil.rollbackTransaction();
            } catch (ConnectedCupDeviceMgtPluginException iotDAOEx) {
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
            ConnectedCupDAOUtil.beginTransaction();
            status = CONNECTED_CUP_DAO_UTIL.getConnectedCupDeviceDAO().updateDevice(device);
            ConnectedCupDAOUtil.commitTransaction();
        } catch (ConnectedCupDeviceMgtPluginException e) {
            try {
                ConnectedCupDAOUtil.rollbackTransaction();
            } catch (ConnectedCupDeviceMgtPluginException iotDAOEx) {
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
            ConnectedCupDAOUtil.beginTransaction();
            status = CONNECTED_CUP_DAO_UTIL.getConnectedCupDeviceDAO().deleteDevice(deviceId.getId());
            ConnectedCupDAOUtil.commitTransaction();
        } catch (ConnectedCupDeviceMgtPluginException e) {
            try {
                ConnectedCupDAOUtil.rollbackTransaction();
            } catch (ConnectedCupDeviceMgtPluginException iotDAOEx) {
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
            Device iotDevice = CONNECTED_CUP_DAO_UTIL.getConnectedCupDeviceDAO().getDevice(deviceId.getId());
            if (iotDevice != null) {
                isEnrolled = true;
            }
        } catch (ConnectedCupDeviceMgtPluginException e) {
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
            device = CONNECTED_CUP_DAO_UTIL.getConnectedCupDeviceDAO().getDevice(deviceId.getId());

        } catch (ConnectedCupDeviceMgtPluginException e) {
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
            ConnectedCupDAOUtil.beginTransaction();
            status = CONNECTED_CUP_DAO_UTIL.getConnectedCupDeviceDAO().updateDevice(device);
            ConnectedCupDAOUtil.commitTransaction();
        } catch (ConnectedCupDeviceMgtPluginException e) {
            try {
                ConnectedCupDAOUtil.rollbackTransaction();
            } catch (ConnectedCupDeviceMgtPluginException iotDAOEx) {
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
            devices = CONNECTED_CUP_DAO_UTIL.getConnectedCupDeviceDAO().getAllDevices();
        } catch (ConnectedCupDeviceMgtPluginException e) {
            String msg = "Error while fetching all Connected Cup devices.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return devices;
    }
}