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

package org.homeautomation.firealarm.plugin.impl.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.homeautomation.firealarm.plugin.constants.DeviceTypeConstants;
import org.homeautomation.firealarm.plugin.exception.DeviceMgtPluginException;
import org.homeautomation.firealarm.plugin.impl.dao.DeviceTypeDAO;
import org.homeautomation.firealarm.plugin.impl.util.DeviceTypeUtils;

import org.wso2.carbon.device.mgt.common.Device;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements IotDeviceDAO for firealarm Devices.
 */
public class DeviceTypeDAOImpl {


    private static final Log log = LogFactory.getLog(DeviceTypeDAOImpl.class);

    public Device getDevice(String deviceId) throws DeviceMgtPluginException {
        Connection conn = null;
        PreparedStatement stmt = null;
        Device iotDevice = null;
        ResultSet resultSet = null;
        try {
            conn = DeviceTypeDAO.getConnection();
            String selectDBQuery =
                    "SELECT firealarm_DEVICE_ID, DEVICE_NAME" +
                            " FROM firealarm_DEVICE WHERE firealarm_DEVICE_ID = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setString(1, deviceId);
            resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                iotDevice = new Device();
                iotDevice.setName(resultSet.getString(
                        DeviceTypeConstants.DEVICE_PLUGIN_DEVICE_NAME));
                if (log.isDebugEnabled()) {
                    log.debug("firealarm device " + deviceId + " data has been fetched from " +
                            "firealarm database.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while fetching firealarm device : '" + deviceId + "'";
            log.error(msg, e);
            throw new DeviceMgtPluginException(msg, e);
        } finally {
            DeviceTypeUtils.cleanupResources(stmt, resultSet);
            DeviceTypeDAO.closeConnection();
        }
        return iotDevice;
    }

    public boolean addDevice(Device device) throws DeviceMgtPluginException {
        boolean status = false;
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = DeviceTypeDAO.getConnection();
            String createDBQuery =
                    "INSERT INTO firealarm_DEVICE(firealarm_DEVICE_ID, DEVICE_NAME) VALUES (?, ?)";
            stmt = conn.prepareStatement(createDBQuery);
            stmt.setString(1, device.getDeviceIdentifier());
            stmt.setString(2, device.getName());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                status = true;
                if (log.isDebugEnabled()) {
                    log.debug("firealarm device " + device.getDeviceIdentifier() + " data has been" +
                            " added to the firealarm database.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while adding the firealarm device '" +
                    device.getDeviceIdentifier() + "' to the firealarm db.";
            log.error(msg, e);
            throw new DeviceMgtPluginException(msg, e);
        } finally {
            DeviceTypeUtils.cleanupResources(stmt, null);
        }
        return status;
    }

    public boolean updateDevice(Device device) throws DeviceMgtPluginException {
        boolean status = false;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DeviceTypeDAO.getConnection();
            String updateDBQuery =
                    "UPDATE firealarm_DEVICE SET  DEVICE_NAME = ? WHERE firealarm_DEVICE_ID = ?";
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
                    log.debug("firealarm device " + device.getDeviceIdentifier() + " data has been" +
                            " modified.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while modifying the firealarm device '" +
                    device.getDeviceIdentifier() + "' data.";
            log.error(msg, e);
            throw new DeviceMgtPluginException(msg, e);
        } finally {
            DeviceTypeUtils.cleanupResources(stmt, null);
        }
        return status;
    }

    public boolean deleteDevice(String deviceId) throws DeviceMgtPluginException {
        boolean status = false;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DeviceTypeDAO.getConnection();
            String deleteDBQuery =
                    "DELETE FROM firealarm_DEVICE WHERE firealarm_DEVICE_ID = ?";
            stmt = conn.prepareStatement(deleteDBQuery);
            stmt.setString(1, deviceId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                status = true;
                if (log.isDebugEnabled()) {
                    log.debug("firealarm device " + deviceId + " data has deleted" +
                            " from the firealarm database.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting firealarm device " + deviceId;
            log.error(msg, e);
            throw new DeviceMgtPluginException(msg, e);
        } finally {
            DeviceTypeUtils.cleanupResources(stmt, null);
        }
        return status;
    }

    public List<Device> getAllDevices() throws DeviceMgtPluginException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Device device;
        List<Device> iotDevices = new ArrayList<>();
        try {
            conn = DeviceTypeDAO.getConnection();
            String selectDBQuery =
                    "SELECT firealarm_DEVICE_ID, DEVICE_NAME " +
                            "FROM firealarm_DEVICE";
            stmt = conn.prepareStatement(selectDBQuery);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                device = new Device();
                device.setDeviceIdentifier(resultSet.getString(DeviceTypeConstants.DEVICE_PLUGIN_DEVICE_ID));
                device.setName(resultSet.getString(DeviceTypeConstants.DEVICE_PLUGIN_DEVICE_NAME));
                List<Device.Property> propertyList = new ArrayList<>();
                device.setProperties(propertyList);
            }
            if (log.isDebugEnabled()) {
                log.debug("All firealarm device details have fetched from firealarm database.");
            }
            return iotDevices;
        } catch (SQLException e) {
            String msg = "Error occurred while fetching all firealarm device data'";
            log.error(msg, e);
            throw new DeviceMgtPluginException(msg, e);
        } finally {
            DeviceTypeUtils.cleanupResources(stmt, resultSet);
            DeviceTypeDAO.closeConnection();
        }
    }
}
