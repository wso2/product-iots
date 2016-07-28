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

package org.homeautomation.doormanager.plugin.impl.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.homeautomation.doormanager.plugin.constants.DoorManagerConstants;
import org.homeautomation.doormanager.plugin.exception.DoorManagerDeviceMgtPluginException;
import org.homeautomation.doormanager.plugin.impl.dao.DoorLockSafe;
import org.homeautomation.doormanager.plugin.impl.dao.DoorManagerDAO;
import org.homeautomation.doormanager.plugin.impl.util.DoorManagerUtils;
import org.wso2.carbon.device.mgt.common.Device;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements IotDeviceDAO for door manager Devices.
 */
public class DoorManagerDAOImpl {

    private static final Log log = LogFactory.getLog(DoorManagerDAOImpl.class);

    public Device getDevice(String deviceId) throws DoorManagerDeviceMgtPluginException {
        Connection conn;
        PreparedStatement stmt = null;
        Device iotDevice = null;
        ResultSet resultSet = null;
        try {
            conn = DoorManagerDAO.getConnection();
            String selectDBQuery =
                    "SELECT doormanager_DEVICE_ID, DEVICE_NAME" +
                            " FROM doormanager_DEVICE WHERE doormanager_DEVICE_ID = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setString(1, deviceId);
            resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                iotDevice = new Device();
                iotDevice.setName(resultSet.getString(
                        DoorManagerConstants.DEVICE_PLUGIN_DEVICE_NAME));
                if (log.isDebugEnabled()) {
                    log.debug("doormanager device " + deviceId + " data has been fetched from " +
                            "doormanager database.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while fetching doormanager device : '" + deviceId + "'";
            log.error(msg, e);
            throw new DoorManagerDeviceMgtPluginException(msg, e);
        } finally {
            DoorManagerUtils.cleanupResources(stmt, resultSet);
            DoorManagerDAO.closeConnection();
        }
        return iotDevice;
    }

    public boolean addDevice(Device device) throws DoorManagerDeviceMgtPluginException {
        boolean status = false;
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = DoorManagerDAO.getConnection();
            String createDBQuery =
                    "INSERT INTO doormanager_DEVICE(doormanager_DEVICE_ID, DEVICE_NAME) VALUES (?, ?)";
            stmt = conn.prepareStatement(createDBQuery);
            stmt.setString(1, device.getDeviceIdentifier());
            stmt.setString(2, device.getName());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                status = true;
                if (log.isDebugEnabled()) {
                    log.debug("doormanager device " + device.getDeviceIdentifier() + " data has been" +
                            " added to the doormanager database.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while adding the doormanager device '" +
                    device.getDeviceIdentifier() + "' to the doormanager db.";
            log.error(msg, e);
            throw new DoorManagerDeviceMgtPluginException(msg, e);
        } finally {
            DoorManagerUtils.cleanupResources(stmt, null);
        }
        return status;
    }

    public boolean updateDevice(Device device) throws DoorManagerDeviceMgtPluginException {
        boolean status = false;
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = DoorManagerDAO.getConnection();
            String updateDBQuery =
                    "UPDATE doormanager_DEVICE SET  DEVICE_NAME = ? WHERE doormanager_DEVICE_ID = ?";
            stmt = conn.prepareStatement(updateDBQuery);
            if (device.getProperties() == null) {
                device.setProperties(new ArrayList<Device.Property>());
            }
            stmt.setString(1, device.getName());
            stmt.setString(2, device.getDeviceIdentifier());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                status = true;
                if (log.isDebugEnabled()) {
                    log.debug("doormanager device " + device.getDeviceIdentifier() + " data has been" +
                            " modified.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while modifying the doormanager device '" +
                    device.getDeviceIdentifier() + "' data.";
            log.error(msg, e);
            throw new DoorManagerDeviceMgtPluginException(msg, e);
        } finally {
            DoorManagerUtils.cleanupResources(stmt, null);
        }
        return status;
    }

    public boolean deleteDevice(String deviceId) throws DoorManagerDeviceMgtPluginException {
        boolean status = false;
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = DoorManagerDAO.getConnection();
            String deleteDBQuery =
                    "DELETE FROM doormanager_DEVICE WHERE doormanager_DEVICE_ID = ?";
            stmt = conn.prepareStatement(deleteDBQuery);
            stmt.setString(1, deviceId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                status = true;
                if (log.isDebugEnabled()) {
                    log.debug("doormanager device " + deviceId + " data has deleted" +
                            " from the doormanager database.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting doormanager device " + deviceId;
            log.error(msg, e);
            throw new DoorManagerDeviceMgtPluginException(msg, e);
        } finally {
            DoorManagerUtils.cleanupResources(stmt, null);
        }
        return status;
    }

    public List<Device> getAllDevices() throws DoorManagerDeviceMgtPluginException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Device device;
        List<Device> iotDevices = new ArrayList<>();
        try {
            conn = DoorManagerDAO.getConnection();
            String selectDBQuery =
                    "SELECT doormanager_DEVICE_ID, DEVICE_NAME " +
                            "FROM doormanager_DEVICE";
            stmt = conn.prepareStatement(selectDBQuery);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                device = new Device();
                device.setDeviceIdentifier(resultSet.getString(DoorManagerConstants.DEVICE_PLUGIN_DEVICE_ID));
                device.setName(resultSet.getString(DoorManagerConstants.DEVICE_PLUGIN_DEVICE_NAME));
                List<Device.Property> propertyList = new ArrayList<>();
                device.setProperties(propertyList);
            }
            if (log.isDebugEnabled()) {
                log.debug("All doormanager device details have fetched from doormanager database.");
            }
            return iotDevices;
        } catch (SQLException e) {
            String msg = "Error occurred while fetching all doormanager device data'";
            log.error(msg, e);
            throw new DoorManagerDeviceMgtPluginException(msg, e);
        } finally {
            DoorManagerUtils.cleanupResources(stmt, resultSet);
            DoorManagerDAO.closeConnection();
        }
    }

    public boolean registerDoorLockSafe(DoorLockSafe automaticDoorLOcker) throws DoorManagerDeviceMgtPluginException {
        boolean status = false;
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = DoorManagerDAO.getConnection();
            String createDBQuery =
                    "INSERT INTO REGISTERED_DOORLOCK_SAFE(doormanager_DEVICE_ID, SERIAL_NUMBER, UID_of_USER, POLICY, " +
                            "EMAIL_ADDRESS, ACCESS_TOKEN, REFRESH_TOKEN) VALUES (?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(createDBQuery);
            stmt.setString(1, automaticDoorLOcker.getDeviceId());
            stmt.setString(2, automaticDoorLOcker.getSerialNumber());
            stmt.setString(3, automaticDoorLOcker.getUIDofUser());
            stmt.setString(4, automaticDoorLOcker.getPolicy());
            stmt.setString(5, automaticDoorLOcker.getEmailAddress());
            stmt.setString(6, automaticDoorLOcker.getAccessToken());
            stmt.setString(7, automaticDoorLOcker.getRefreshToken());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                status = true;
                if (log.isDebugEnabled()) {
                    log.debug("Locker Manager device " + automaticDoorLOcker.getOwner() +
                            " data has been added to the Locker Manager database.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while adding the Locker Manager device '" +
                    automaticDoorLOcker.getOwner() + "' to the Locker Manager db.";
            log.error(msg, e);
            throw new DoorManagerDeviceMgtPluginException(msg, e);
        } finally {
            DoorManagerUtils.cleanupResources(stmt, null);
        }
        return status;
    }

    public boolean isDoorLockSafeRegistered(String serialNumber, String deviceId) throws DoorManagerDeviceMgtPluginException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            conn = DoorManagerDAO.getConnection();
            String selectDBQuery =
                    "SELECT  SERIAL_NUMBER  FROM REGISTERED_DOORLOCK_SAFE WHERE SERIAL_NUMBER = ? AND doormanager_DEVICE_ID = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setString(1, serialNumber);
            stmt.setString(2, deviceId);
            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                String result = resultSet.getString(DoorManagerConstants.DEVICE_PLUGIN_DEVICE_SERIAL_NUMBER);
                log.warn(result);
                return true;
            } else {
                return false;
            }

        } catch (SQLException e) {
            String msg = "Error occurred while fetching all Automatic Door Locker device data'";
            log.error(msg, e);
            throw new DoorManagerDeviceMgtPluginException(msg, e);
        } finally {
            DoorManagerUtils.cleanupResources(stmt, resultSet);
            DoorManagerDAO.closeConnection();
        }

    }

    public boolean isUserAllowed(String serialNumber, String UIDofUser, String deviceId) throws DoorManagerDeviceMgtPluginException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            conn = DoorManagerDAO.getConnection();
            String selectDBQuery =
                    "SELECT UID_OF_USER FROM REGISTERED_DOORLOCK_SAFE  WHERE SERIAL_NUMBER = ? AND DOORMANAGER_DEVICE_ID = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setString(1, serialNumber);
            stmt.setString(2, deviceId);
            resultSet = stmt.executeQuery();
            String result;
            if (resultSet.next()) {
                result = resultSet.getString(DoorManagerConstants.DEVICE_PLUGIN_DEVICE_UID_OF_USER);
                log.warn(result);
                return true;
            } else {
                selectDBQuery =
                        "SELECT UID_OF_USER FROM SHARED_DOORLOCK_SAFE WHERE SERIAL_NUMBER = ? AND DOORMANAGER_DEVICE_ID = ?  AND UID_OF_USER = ?";
                stmt = conn.prepareStatement(selectDBQuery);
                stmt.setString(1, serialNumber);
                stmt.setString(2, deviceId);
                stmt.setString(3, UIDofUser);
                resultSet = stmt.executeQuery();
                if (resultSet.next()) {
                    result = resultSet.getString(DoorManagerConstants.DEVICE_PLUGIN_DEVICE_UID_OF_USER);
                    log.warn(result);
                    return true;
                }
                return false;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while validating: whether user is registered or not";
            log.error(msg, e);
            throw new DoorManagerDeviceMgtPluginException(msg, e);
        } finally {
            DoorManagerUtils.cleanupResources(stmt, resultSet);
            DoorManagerDAO.closeConnection();
        }

    }


    public boolean shareDoorLockSafe(DoorLockSafe automaticDoorLOcker) throws DoorManagerDeviceMgtPluginException {
        boolean status = false;
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = DoorManagerDAO.getConnection();
            String createDBQuery =
                    "INSERT INTO SHARED_DOORLOCK_SAFE(doormanager_DEVICE_ID, SERIAL_NUMBER, UID_of_USER, POLICY) VALUES (?, ?, ?, ?)";
            stmt = conn.prepareStatement(createDBQuery);
            stmt.setString(1, automaticDoorLOcker.getDeviceId());
            stmt.setString(2, automaticDoorLOcker.getSerialNumber());
            stmt.setString(3, automaticDoorLOcker.getUIDofUser());
            stmt.setString(4, automaticDoorLOcker.getPolicy());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                status = true;
                if (log.isDebugEnabled()) {
                    log.debug("Locker Manager device " + automaticDoorLOcker.getOwner() +
                            " data has been added to the Locker Manager database.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while adding the Locker Manager device '" +
                    automaticDoorLOcker.getOwner() + "' to the Locker Manager db.";
            log.error(msg, e);
            throw new DoorManagerDeviceMgtPluginException(msg, e);
        } finally {
            DoorManagerUtils.cleanupResources(stmt, null);
        }
        return status;
    }

    public boolean checkCardDoorAssociation(String cardNum, String deviceID) throws DoorManagerDeviceMgtPluginException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet;
        try {
            conn = DoorManagerDAO.getConnection();
            String selectDBQuery = "SELECT * FROM REGISTERED_DOORLOCK_SAFE WHERE UID_of_USER = ? AND doormanager_DEVICE_ID = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setString(1, cardNum);
            stmt.setString(2, deviceID);
            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            String msg = "No associations were found between lock : " + deviceID + " and card : " + cardNum;
            throw new DoorManagerDeviceMgtPluginException(msg, e);
        } finally {
            DoorManagerUtils.cleanupResources(stmt, null);
        }
    }


    public String getUserEmailAddress(String cardNum) throws DoorManagerDeviceMgtPluginException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet;
        String email;
        try {
            conn = DoorManagerDAO.getConnection();
            String selectDBQuery = "SELECT EMAIL_ADDRESS FROM REGISTERED_DOORLOCK_SAFE WHERE UID_of_USER = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setString(1, cardNum);
            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                email = resultSet.getString("EMAIL_ADDRESS");
                log.warn(email);
                return email;
            }
            return null;
        } catch (SQLException e) {
            String msg = "No email found for the and card : " + cardNum;
            throw new DoorManagerDeviceMgtPluginException(msg, e);
        } finally {
            DoorManagerUtils.cleanupResources(stmt, null);
        }
    }

    public List<String> getUserCredentials(String deviceId, String UIDofUser) throws DoorManagerDeviceMgtPluginException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<String> userCredentials = new ArrayList<>();
        try {
            conn = DoorManagerDAO.getConnection();
            String selectDBQuery =
                    "SELECT ACCESS_TOKEN, REFRESH_TOKEN FROM REGISTERED_DOORLOCK_SAFE WHERE DOORMANAGER_DEVICE_ID = ?  AND UID_OF_USER = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setString(1, deviceId);
            stmt.setString(2, UIDofUser);
            resultSet = stmt.executeQuery();
            if (log.isDebugEnabled()) {
                log.debug("Get user credentials from Automatic Door Locker database.");
            }
            log.warn("getting user credentials");
            if (resultSet.next()) {
                String accessToken = resultSet.getString(DoorManagerConstants.DEVICE_PLUGIN_PROPERTY_ACCESS_TOKEN);
                String refreshToken = resultSet.getString(DoorManagerConstants.DEVICE_PLUGIN_PROPERTY_REFRESH_TOKEN);
                log.warn(accessToken);
                userCredentials.add(accessToken);
                userCredentials.add(refreshToken);
            }
            return userCredentials;
        } catch (SQLException e) {
            String msg = "Error occurred while getting user credentials";
            log.error(msg, e);
            throw new DoorManagerDeviceMgtPluginException(msg, e);
        } finally {
            DoorManagerUtils.cleanupResources(stmt, resultSet);
            DoorManagerDAO.closeConnection();
        }
    }
}
