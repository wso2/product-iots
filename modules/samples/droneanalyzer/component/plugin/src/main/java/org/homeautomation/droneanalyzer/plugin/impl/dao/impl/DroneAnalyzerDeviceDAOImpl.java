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

package org.homeautomation.droneanalyzer.plugin.impl.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.homeautomation.droneanalyzer.plugin.constants.DroneConstants;
import org.homeautomation.droneanalyzer.plugin.exception.DroneAnalyzerDeviceMgtPluginException;
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
 * Implements CRUD for drone analyzer.
 */
public class DroneAnalyzerDeviceDAOImpl {

    private static final Log log = LogFactory.getLog(DroneAnalyzerDeviceDAOImpl.class);

    public Device getDevice(String deviceId) throws DroneAnalyzerDeviceMgtPluginException {
        Connection conn = null;
        PreparedStatement stmt = null;
        Device device = null;
        ResultSet resultSet = null;
        try {
            conn = DroneAnalyzerDAO.getConnection();
            if(conn == null){
                log.error("Database connection hasn't been created");
            }
            String selectDBQuery =
                    "SELECT DRONE_DEVICE_ID, DEVICE_NAME FROM DRONE_DEVICE WHERE DRONE_DEVICE_ID = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setString(1, deviceId);
            resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                device = new Device();
                if (log.isDebugEnabled()) {
                    log.debug("Drone device " + deviceId + " data has been fetched from " +
                            "Drone database.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while fetching drone device : '" + deviceId + "'";
            log.error(msg, e);
            throw new DroneAnalyzerDeviceMgtPluginException(msg, e);
        } finally {
            DroneAnalyzerUtils.cleanupResources(stmt, resultSet);
            DroneAnalyzerDAO.closeConnection();
        }
        return device;
    }

    public boolean addDevice(Device device) throws DroneAnalyzerDeviceMgtPluginException {
        boolean status = false;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DroneAnalyzerDAO.getConnection();
            String createDBQuery =
                    "INSERT INTO DRONE_DEVICE(DRONE_DEVICE_ID, DEVICE_NAME) VALUES (?, ?)";
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
            throw new DroneAnalyzerDeviceMgtPluginException(msg, e);
        } finally {
            DroneAnalyzerUtils.cleanupResources(stmt, null);
        }
        return status;
    }

    public boolean updateDevice(Device device) throws DroneAnalyzerDeviceMgtPluginException {
        boolean status = false;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DroneAnalyzerDAO.getConnection();
            String updateDBQuery =
                    "UPDATE DRONE_DEVICE SET  DEVICE_NAME = ? WHERE DRONE_DEVICE_ID = ?";
            stmt = conn.prepareStatement(updateDBQuery);
            stmt.setString(1, device.getName());
            stmt.setString(2, device.getDeviceIdentifier());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                status = true;
                if (log.isDebugEnabled()) {
                    log.debug("Drone device " + device.getDeviceIdentifier() + " data has been" +
                            " modified.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while modifying the Drone device '" + device.getDeviceIdentifier() + "' data.";
            log.error(msg, e);
            throw new DroneAnalyzerDeviceMgtPluginException(msg, e);
        } finally {
            DroneAnalyzerUtils.cleanupResources(stmt, null);
        }
        return status;
    }

    public boolean deleteIotDevice(String iotDeviceId) throws DroneAnalyzerDeviceMgtPluginException {
        boolean status = false;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DroneAnalyzerDAO.getConnection();
            System.out.println("delete device ");
            String deleteDBQuery =
                    "DELETE FROM DRONE_DEVICE WHERE DRONE_DEVICE_ID = ?";
            stmt = conn.prepareStatement(deleteDBQuery);
            stmt.setString(1, iotDeviceId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                status = true;
                if (log.isDebugEnabled()) {
                    log.debug("Drone device " + iotDeviceId + " data has deleted" +
                            " from the drone database.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting drone device " + iotDeviceId;
            log.error(msg, e);
            throw new DroneAnalyzerDeviceMgtPluginException(msg, e);
        } finally {
            DroneAnalyzerUtils.cleanupResources(stmt, null);
        }
        return status;
    }

    public List<Device> getAllDevices() throws DroneAnalyzerDeviceMgtPluginException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Device iotDevice;
        List<Device> iotDevices = new ArrayList<Device>();
        try {
            conn = DroneAnalyzerDAO.getConnection();
            String selectDBQuery =
                    "SELECT DRONE_DEVICE_ID, DEVICE_NAME " +
                            "FROM DRONE_DEVICE";
            stmt = conn.prepareStatement(selectDBQuery);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                iotDevice = new Device();
                iotDevice.setDeviceIdentifier(resultSet.getString(DroneConstants.DEVICE_PLUGIN_DEVICE_ID));
                iotDevice.setName(resultSet.getString(DroneConstants.DEVICE_PLUGIN_DEVICE_NAME));
            }
            if (log.isDebugEnabled()) {
                log.debug("All drone device details have fetched from drone database.");
            }
            return iotDevices;
        } catch (SQLException e) {
            String msg = "Error occurred while fetching all drone device data'";
            log.error(msg, e);
            throw new DroneAnalyzerDeviceMgtPluginException(msg, e);
        } finally {
            DroneAnalyzerUtils.cleanupResources(stmt, resultSet);
            DroneAnalyzerDAO.closeConnection();
        }
    }
}
