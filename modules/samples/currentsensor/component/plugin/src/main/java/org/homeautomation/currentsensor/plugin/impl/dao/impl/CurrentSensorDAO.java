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

package org.homeautomation.currentsensor.plugin.impl.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.homeautomation.currentsensor.plugin.constants.CurrentSensorConstants;
import org.homeautomation.currentsensor.plugin.exception.CurrentSensorDeviceMgtPluginException;
import org.homeautomation.currentsensor.plugin.impl.dao.CurrentSensorDAOUtil;
import org.homeautomation.currentsensor.plugin.impl.util.CurrentSensorUtils;
import org.wso2.carbon.device.mgt.common.Device;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements IotDeviceDAO for current sensor Devices.
 */
public class CurrentSensorDAO {

    private static final Log log = LogFactory.getLog(CurrentSensorDAO.class);

    public Device getDevice(String deviceId) throws CurrentSensorDeviceMgtPluginException {
        Connection conn = null;
        PreparedStatement stmt = null;
        Device CurrentSensorDevice = null;
        ResultSet resultSet = null;
        try {
            conn = CurrentSensorDAOUtil.getConnection();
            String selectDBQuery =
                    "SELECT CURRENT_SENSOR_DEVICE_ID, DEVICE_NAME, ACCESS_TOKEN, REFRESH_TOKEN" +
                    " FROM CURRENT_SENSOR_DEVICE WHERE CURRENT_SENSOR_DEVICE_ID = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setString(1, deviceId);
            resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                CurrentSensorDevice = new Device();
                CurrentSensorDevice.setName(resultSet.getString(
                        CurrentSensorConstants.DEVICE_PLUGIN_DEVICE_NAME));
                List<Device.Property> propertyList = new ArrayList<Device.Property>();
                propertyList.add(CurrentSensorUtils.getProperty(
                        CurrentSensorConstants.DEVICE_PLUGIN_PROPERTY_ACCESS_TOKEN,
                        resultSet.getString("ACCESS_TOKEN")));
                propertyList.add(CurrentSensorUtils.getProperty(
                        CurrentSensorConstants.DEVICE_PLUGIN_PROPERTY_REFRESH_TOKEN,
                        resultSet.getString("REFRESH_TOKEN")));
                CurrentSensorDevice.setProperties(propertyList);

                if (log.isDebugEnabled()) {
                    log.debug("Connected Cup service " + deviceId + " data has been fetched from" +
                              "Connected Cup database.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while fetching Connected Cup device : '" + deviceId + "'";
            log.error(msg, e);
            throw new CurrentSensorDeviceMgtPluginException(msg, e);
        } finally {
            CurrentSensorUtils.cleanupResources(stmt, resultSet);
            CurrentSensorDAOUtil.closeConnection();
        }
        return CurrentSensorDevice;
    }


    public boolean addDevice(Device CurrentSensorDevice) throws CurrentSensorDeviceMgtPluginException {
        boolean status = false;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = CurrentSensorDAOUtil.getConnection();
            String createDBQuery =
                    "INSERT INTO CURRENT_SENSOR_DEVICE(CURRENT_SENSOR_DEVICE_ID, DEVICE_NAME, " +
                    "ACCESS_TOKEN, REFRESH_TOKEN) VALUES (?, ?, ?, ?)";

            stmt = conn.prepareStatement(createDBQuery);
            stmt.setString(1, CurrentSensorDevice.getDeviceIdentifier());
            stmt.setString(2, CurrentSensorDevice.getName());
            stmt.setString(3, CurrentSensorUtils.getDeviceProperty(
                    CurrentSensorDevice.getProperties(),
                    CurrentSensorConstants.DEVICE_PLUGIN_PROPERTY_ACCESS_TOKEN));
            stmt.setString(4, CurrentSensorUtils.getDeviceProperty(
                    CurrentSensorDevice.getProperties(),
                    CurrentSensorConstants.DEVICE_PLUGIN_PROPERTY_REFRESH_TOKEN));

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                status = true;
                if (log.isDebugEnabled()) {
                    log.debug("Connected Cup device " + CurrentSensorDevice.getDeviceIdentifier() +
                              " data has been added to the Connected Cup database.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while adding the Connected Cup device '" +
                         CurrentSensorDevice.getDeviceIdentifier() + "' to the Connected Cup db.";
            log.error(msg, e);
            throw new CurrentSensorDeviceMgtPluginException(msg, e);
        } finally {
            CurrentSensorUtils.cleanupResources(stmt, null);
        }
        return status;
    }

    public boolean updateDevice(Device CurrentSensorDevice) throws CurrentSensorDeviceMgtPluginException {
        boolean status = false;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = CurrentSensorDAOUtil.getConnection();
            String updateDBQuery =
                    "UPDATE CURRENT_SENSOR_DEVICE SET  DEVICE_NAME = ?, ACCESS_TOKEN=?, " +
                    "REFRESH_TOKEN=? WHERE CURRENT_SENSOR_DEVICE_ID = ?";

            stmt = conn.prepareStatement(updateDBQuery);

            if (CurrentSensorDevice.getProperties() == null) {
                CurrentSensorDevice.setProperties(new ArrayList<Device.Property>());
            }
            stmt.setString(1, CurrentSensorDevice.getName());
            stmt.setString(2, CurrentSensorUtils.getDeviceProperty(
                    CurrentSensorDevice.getProperties(),
                    CurrentSensorConstants.DEVICE_PLUGIN_PROPERTY_ACCESS_TOKEN));
            stmt.setString(3, CurrentSensorUtils.getDeviceProperty(
                    CurrentSensorDevice.getProperties(),
                    CurrentSensorConstants.DEVICE_PLUGIN_PROPERTY_REFRESH_TOKEN));
            stmt.setString(4, CurrentSensorDevice.getDeviceIdentifier());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                status = true;
                if (log.isDebugEnabled()) {
                    log.debug("Connected Cup device " + CurrentSensorDevice.getDeviceIdentifier() +
                              " data has been modified.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while modifying the Connected Cup device '" +
                         CurrentSensorDevice.getDeviceIdentifier() + "' data.";
            log.error(msg, e);
            throw new CurrentSensorDeviceMgtPluginException(msg, e);
        } finally {
            CurrentSensorUtils.cleanupResources(stmt, null);
        }
        return status;
    }

    public boolean deleteDevice(String deviceId) throws CurrentSensorDeviceMgtPluginException {
        boolean status = false;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = CurrentSensorDAOUtil.getConnection();
            String deleteDBQuery =
                    "DELETE FROM CURRENT_SENSOR_DEVICE WHERE CURRENT_SENSOR_DEVICE_ID = ?";
            stmt = conn.prepareStatement(deleteDBQuery);
            stmt.setString(1, deviceId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                status = true;
                if (log.isDebugEnabled()) {
                    log.debug("Connected Cup device " + deviceId + " data has deleted" +
                              " from the Connected Cup database.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting Connected Cup device " + deviceId;
            log.error(msg, e);
            throw new CurrentSensorDeviceMgtPluginException(msg, e);
        } finally {
            CurrentSensorUtils.cleanupResources(stmt, null);
        }
        return status;
    }

    public List<Device> getAllDevices() throws CurrentSensorDeviceMgtPluginException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Device CurrentSensorDevice;
        List<Device> iotDevices = new ArrayList<>();

        try {
            conn = CurrentSensorDAOUtil.getConnection();
            String selectDBQuery =
                    "SELECT CURRENT_SENSOR_DEVICE_ID, DEVICE_NAME, ACCESS_TOKEN, REFRESH_TOKEN" +
                    "FROM CURRENT_SENSOR_DEVICE";
            stmt = conn.prepareStatement(selectDBQuery);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                CurrentSensorDevice = new Device();
                CurrentSensorDevice.setDeviceIdentifier(resultSet.getString(
                        CurrentSensorConstants.DEVICE_PLUGIN_DEVICE_ID));
                CurrentSensorDevice.setName(resultSet.getString(
                        CurrentSensorConstants.DEVICE_PLUGIN_DEVICE_NAME));

                List<Device.Property> propertyList = new ArrayList<Device.Property>();
                propertyList.add(CurrentSensorUtils.getProperty(
                        CurrentSensorConstants.DEVICE_PLUGIN_PROPERTY_ACCESS_TOKEN,
                        resultSet.getString("ACCESS_TOKEN")));
                propertyList.add(CurrentSensorUtils.getProperty(
                        CurrentSensorConstants.DEVICE_PLUGIN_PROPERTY_REFRESH_TOKEN,
                        resultSet.getString("REFRESH_TOKEN")));
                CurrentSensorDevice.setProperties(propertyList);
            }
            if (log.isDebugEnabled()) {
                log.debug("All Connected Cup device details have fetched from Connected Cup database" +
                          ".");
            }
            return iotDevices;
        } catch (SQLException e) {
            String msg = "Error occurred while fetching all Connected Cup device data'";
            log.error(msg, e);
            throw new CurrentSensorDeviceMgtPluginException(msg, e);
        } finally {
            CurrentSensorUtils.cleanupResources(stmt, resultSet);
            CurrentSensorDAOUtil.closeConnection();
        }

    }

}
