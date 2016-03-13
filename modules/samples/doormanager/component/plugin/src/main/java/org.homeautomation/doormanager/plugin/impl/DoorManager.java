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

package org.homeautomation.doormanager.plugin.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.homeautomation.doormanager.plugin.exception.DoorManagerDeviceMgtPluginException;
import org.homeautomation.doormanager.plugin.impl.dao.DoorLockSafe;
import org.homeautomation.doormanager.plugin.impl.dao.DoorManagerDAO;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.configuration.mgt.TenantConfiguration;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManagementException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.List;

public class DoorManager implements DeviceManager {

    private static final Log log = LogFactory.getLog(DoorManager.class);
    private static final DoorManagerDAO DOOR_MANAGER_DAO = new DoorManagerDAO();
    private PrivilegedCarbonContext ctx;

    @Override
    public FeatureManager getFeatureManager() {
        return null;
    }

    @Override
    public boolean saveConfiguration(TenantConfiguration tenantConfiguration)
            throws DeviceManagementException {
        return false;
    }

    @Override
    public TenantConfiguration getConfiguration() throws DeviceManagementException {
        return null;
    }

    @Override
    public boolean enrollDevice(Device device) throws DeviceManagementException {
        boolean status;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Enrolling a new Automatic Door Locker device : " + device.getDeviceIdentifier());
            }
            DoorManagerDAO.beginTransaction();
            status = DOOR_MANAGER_DAO.getAutomaticDoorLockerDeviceDAO().addDevice(device);
            DoorManagerDAO.commitTransaction();
        } catch (DoorManagerDeviceMgtPluginException e) {
            try {
                DoorManagerDAO.rollbackTransaction();
            } catch (DoorManagerDeviceMgtPluginException e1) {
                e1.printStackTrace();
            }
            String msg = "Error while enrolling the Automatic Door Locker device : " + device.getDeviceIdentifier();
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
                log.debug("Modifying the Automatic Door Locker device enrollment data");
            }
            DoorManagerDAO.beginTransaction();
            status = DOOR_MANAGER_DAO.getAutomaticDoorLockerDeviceDAO().updateDevice(device);
            DoorManagerDAO.commitTransaction();
        } catch (DoorManagerDeviceMgtPluginException e) {
            try {
                DoorManagerDAO.rollbackTransaction();
            } catch (DoorManagerDeviceMgtPluginException e1) {
                e1.printStackTrace();
            }
            String msg = "Error while updating the enrollment of the Automatic Door Locker device : " +
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
                log.debug("Dis-enrolling Automatic Door Locker device : " + deviceId);
            }
            DoorManagerDAO.beginTransaction();
            status = DOOR_MANAGER_DAO.getAutomaticDoorLockerDeviceDAO().deleteDevice(deviceId.getId());
            DoorManagerDAO.commitTransaction();
        } catch (DoorManagerDeviceMgtPluginException e) {
            try {
                DoorManagerDAO.rollbackTransaction();
            } catch (DoorManagerDeviceMgtPluginException e1) {
                e1.printStackTrace();
            }
            String msg = "Error while removing the Automatic Door Locker device : " + deviceId.getId();
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
                log.debug("Checking the enrollment of Automatic Door Locker device : " + deviceId.getId());
            }
            Device iotDevice = DOOR_MANAGER_DAO.getAutomaticDoorLockerDeviceDAO().getDevice(deviceId.getId());
            if (iotDevice != null) {
                isEnrolled = true;
            }
        } catch (DoorManagerDeviceMgtPluginException e) {
            String msg = "Error while checking the enrollment status of Automatic Door Locker device : " +
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
                log.debug("Getting the details of Automatic Door Locker device : " + deviceId.getId());
            }
            device = DOOR_MANAGER_DAO.getAutomaticDoorLockerDeviceDAO().getDevice(deviceId.getId());

        } catch (DoorManagerDeviceMgtPluginException e) {
            String msg = "Error while fetching the Automatic Door Locker device : " + deviceId.getId();
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
                        "updating the details of Automatic Door Locker device : " + deviceIdentifier);
            }
            DoorManagerDAO.beginTransaction();
            status = DOOR_MANAGER_DAO.getAutomaticDoorLockerDeviceDAO().updateDevice(device);
            DoorManagerDAO.commitTransaction();
        } catch (DoorManagerDeviceMgtPluginException e) {
            try {
                DoorManagerDAO.rollbackTransaction();
            } catch (DoorManagerDeviceMgtPluginException iotDAOEx) {
                String msg = "Error occurred while roll back the update device info transaction :" + device.toString();
                log.warn(msg, iotDAOEx);
            }
            String msg =
                    "Error while updating the Automatic Door Locker device : " + deviceIdentifier;
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
                log.debug("Fetching the details of all Automatic Door Locker devices");
            }
            devices = DOOR_MANAGER_DAO.getAutomaticDoorLockerDeviceDAO().getAllDevices();
        } catch (DoorManagerDeviceMgtPluginException e) {
            String msg = "Error while fetching all Automatic Door Locker devices.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
        return devices;
    }

    /**
     * Get userStore manager
     *
     * @return
     * @throws UserStoreException
     */
    public UserStoreManager getUserStoreManager() throws UserStoreException {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        PrivilegedCarbonContext.startTenantFlow();
        ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        ctx.setTenantDomain(tenantDomain, true);
        if (log.isDebugEnabled()) {
            log.debug("Getting thread local carbon context for tenant domain: " + tenantDomain);
        }
        RealmService realmService = (RealmService) ctx.getOSGiService(RealmService.class, null);
        return realmService.getTenantUserRealm(ctx.getTenantId()).getUserStoreManager();
    }

    /**
     * Ends tenant flow.
     */
    private void endTenantFlow() {
        PrivilegedCarbonContext.endTenantFlow();
        ctx = null;
        if (log.isDebugEnabled()) {
            log.debug("Tenant flow ended");
        }
    }

    public boolean assignUserToLock(DoorLockSafe doorLockSafe) throws DoorManagerDeviceMgtPluginException {
        boolean status;
        try {
            DoorManagerDAO.beginTransaction();
            status = DOOR_MANAGER_DAO.getAutomaticDoorLockerDeviceDAO().registerDoorLockSafe(doorLockSafe);
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
        status = DOOR_MANAGER_DAO.getAutomaticDoorLockerDeviceDAO().checkCardDoorAssociation(cardNumber, deviceId);
        DoorManagerDAO.commitTransaction();
        return status;
    }

}