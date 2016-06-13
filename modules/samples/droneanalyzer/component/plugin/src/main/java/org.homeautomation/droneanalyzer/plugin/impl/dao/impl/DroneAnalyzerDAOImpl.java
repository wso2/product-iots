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

package org.homeautomation.droneanalyzer.plugin.impl.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.homeautomation.droneanalyzer.plugin.constants.DroneAnalyzerConstants;
import org.homeautomation.droneanalyzer.plugin.exception.DeviceMgtPluginException;
import org.homeautomation.droneanalyzer.plugin.impl.dao.DroneAnalyzerDAO;
import org.homeautomation.droneanalyzer.plugin.impl.util.DroneAnalyzerUtils;
import org.wso2.carbon.device.mgt.common.Device;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements IotDeviceDAO for gg Devices.
 */
public class DroneAnalyzerDAOImpl {


    private static final Log log = LogFactory.getLog(DroneAnalyzerDAOImpl.class);

    public Device getDevice(String deviceId) throws DeviceMgtPluginException {
        Connection conn;
        PreparedStatement stmt = null;
        Device iotDevice = null;
        ResultSet resultSet = null;
        try {
            conn = DroneAnalyzerDAO.getConnection();
            String selectDBQuery =
                    "SELECT drone_DEVICE_ID, DEVICE_NAME" +
                            " FROM drone_DEVICE WHERE drone_DEVICE_ID = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setString(1, deviceId);
            resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                iotDevice = new Device();
                iotDevice.setName(resultSet.getString(
                        DroneAnalyzerConstants.DEVICE_PLUGIN_DEVICE_NAME));
                if (log.isDebugEnabled()) {
                    log.debug("drone device " + deviceId + " data has been fetched from " +
                            "drone database.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while fetching drone device : '" + deviceId + "'";
            log.error(msg, e);
            throw new DeviceMgtPluginException(msg, e);
        } finally {
            DroneAnalyzerUtils.cleanupResources(stmt, resultSet);
            DroneAnalyzerDAO.closeConnection();
        }
        return iotDevice;
    }

    public boolean addDevice(Device device) throws DeviceMgtPluginException {
        boolean status = false;
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = DroneAnalyzerDAO.getConnection();
            String createDBQuery =
                    "INSERT INTO drone_DEVICE(drone_DEVICE_ID, DEVICE_NAME) VALUES (?, ?)";
            stmt = conn.prepareStatement(createDBQuery);
            stmt.setString(1, device.getDeviceIdentifier());
            stmt.setString(2, device.getName());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                status = true;
                if (log.isDebugEnabled()) {
                    log.debug("drone device " + device.getDeviceIdentifier() + " data has been" +
                            " added to the drone database.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while adding the drone device '" +
                    device.getDeviceIdentifier() + "' to the drone db.";
            log.error(msg, e);
            throw new DeviceMgtPluginException(msg, e);
        } finally {
            DroneAnalyzerUtils.cleanupResources(stmt, null);
        }
        return status;
    }

    public boolean updateDevice(Device device) throws DeviceMgtPluginException {
        boolean status = false;
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = DroneAnalyzerDAO.getConnection();
            String updateDBQuery =
                    "UPDATE drone_DEVICE SET  DEVICE_NAME = ? WHERE drone_DEVICE_ID = ?";
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
                    log.debug("drone device " + device.getDeviceIdentifier() + " data has been" +
                            " modified.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while modifying the drone device '" +
                    device.getDeviceIdentifier() + "' data.";
            log.error(msg, e);
            throw new DeviceMgtPluginException(msg, e);
        } finally {
            DroneAnalyzerUtils.cleanupResources(stmt, null);
        }
        return status;
    }

    public boolean deleteDevice(String deviceId) throws DeviceMgtPluginException {
        boolean status = false;
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = DroneAnalyzerDAO.getConnection();
            String deleteDBQuery =
                    "DELETE FROM drone_DEVICE WHERE drone_DEVICE_ID = ?";
            stmt = conn.prepareStatement(deleteDBQuery);
            stmt.setString(1, deviceId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                status = true;
                if (log.isDebugEnabled()) {
                    log.debug("drone device " + deviceId + " data has deleted" +
                            " from the drone database.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting drone device " + deviceId;
            log.error(msg, e);
            throw new DeviceMgtPluginException(msg, e);
        } finally {
            DroneAnalyzerUtils.cleanupResources(stmt, null);
        }
        return status;
    }

    public List<Device> getAllDevices() throws DeviceMgtPluginException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Device device;
        List<Device> devices = new ArrayList<>();
        try {
            conn = DroneAnalyzerDAO.getConnection();
            String selectDBQuery =
                    "SELECT drone_DEVICE_ID, DEVICE_NAME " +
                            "FROM drone_DEVICE";
            stmt = conn.prepareStatement(selectDBQuery);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                device = new Device();
                device.setDeviceIdentifier(resultSet.getString(DroneAnalyzerConstants.DEVICE_PLUGIN_DEVICE_ID));
                device.setName(resultSet.getString(DroneAnalyzerConstants.DEVICE_PLUGIN_DEVICE_NAME));
                List<Device.Property> propertyList = new ArrayList<>();
                device.setProperties(propertyList);
                devices.add(device);
            }
            if (log.isDebugEnabled()) {
                log.debug("All drone device details have fetched from drone database.");
            }
            return devices;
        } catch (SQLException e) {
            String msg = "Error occurred while fetching all drone device data'";
            log.error(msg, e);
            throw new DeviceMgtPluginException(msg, e);
        } finally {
            DroneAnalyzerUtils.cleanupResources(stmt, resultSet);
            DroneAnalyzerDAO.closeConnection();
        }
    }
}
