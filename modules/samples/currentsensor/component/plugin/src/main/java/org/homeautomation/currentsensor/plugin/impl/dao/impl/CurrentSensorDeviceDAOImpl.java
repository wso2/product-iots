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
import org.homeautomation.currentsensor.plugin.impl.dao.CurrentSensorDAO;
import org.wso2.carbon.device.mgt.iot.util.iotdevice.dao.IotDeviceDAO;
import org.wso2.carbon.device.mgt.iot.util.iotdevice.dao.IotDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.iot.util.iotdevice.dao.util.IotDeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.iot.util.iotdevice.dto.IotDevice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements IotDeviceDAO for current sensor Devices.
 */
public class CurrentSensorDeviceDAOImpl implements IotDeviceDAO {

    private static final Log log = LogFactory.getLog(CurrentSensorDeviceDAOImpl.class);

    @Override
    public IotDevice getIotDevice(String iotDeviceId) throws IotDeviceManagementDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        IotDevice iotDevice = null;
        ResultSet resultSet = null;
        try {
            conn = CurrentSensorDAO.getConnection();
            String selectDBQuery =
                    "SELECT CURRENT_SENSOR_DEVICE_ID, DEVICE_NAME FROM CURRENT_SENSOR_DEVICE WHERE " +
                    "CURRENT_SENSOR_DEVICE_ID = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setString(1, iotDeviceId);
            resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                iotDevice = new IotDevice();
                iotDevice.setIotDeviceName(resultSet.getString(CurrentSensorConstants.DEVICE_PLUGIN_DEVICE_NAME));
                Map<String, String> propertyMap = new HashMap<String, String>();
                iotDevice.setDeviceProperties(propertyMap);
                if (log.isDebugEnabled()) {
                    log.debug("Current Sensor device " + iotDeviceId + " data has been fetched from " +
                              "Current Sensor database.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while fetching Current Sensor device : '" + iotDeviceId + "'";
            log.error(msg, e);
            throw new IotDeviceManagementDAOException(msg, e);
        } finally {
            IotDeviceManagementDAOUtil.cleanupResources(stmt, resultSet);
            CurrentSensorDAO.closeConnection();
        }
        return iotDevice;
    }

    @Override
    public boolean addIotDevice(IotDevice iotDevice)
            throws IotDeviceManagementDAOException {
        boolean status = false;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = CurrentSensorDAO.getConnection();
            String createDBQuery =
                    "INSERT INTO CURRENT_SENSOR_DEVICE(CURRENT_SENSOR_DEVICE_ID, DEVICE_NAME) VALUES (?, ?)";
            stmt = conn.prepareStatement(createDBQuery);
            stmt.setString(1, iotDevice.getIotDeviceId());
            stmt.setString(2, iotDevice.getIotDeviceName());
            if (iotDevice.getDeviceProperties() == null) {
                iotDevice.setDeviceProperties(new HashMap<String, String>());
            }
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                status = true;
                if (log.isDebugEnabled()) {
                    log.debug("Current Sensor device " + iotDevice.getIotDeviceId() + " data has been" +
                              " added to the Current Sensor database.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while adding the Current Sensor device '" +
                         iotDevice.getIotDeviceId() + "' to the Current Sensor db.";
            log.error(msg, e);
            throw new IotDeviceManagementDAOException(msg, e);
        } finally {
            IotDeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return status;
    }

    @Override
    public boolean updateIotDevice(IotDevice iotDevice) throws IotDeviceManagementDAOException {
        boolean status = false;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = CurrentSensorDAO.getConnection();
            String updateDBQuery =
                    "UPDATE CURRENT_SENSOR_DEVICE SET  DEVICE_NAME = ? WHERE CURRENT_SENSOR_DEVICE_ID = ?";
            stmt = conn.prepareStatement(updateDBQuery);
            if (iotDevice.getDeviceProperties() == null) {
                iotDevice.setDeviceProperties(new HashMap<String, String>());
            }
            stmt.setString(1, iotDevice.getIotDeviceName());
            stmt.setString(2, iotDevice.getIotDeviceId());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                status = true;
                if (log.isDebugEnabled()) {
                    log.debug("Current Sensor device " + iotDevice.getIotDeviceId() + " data has been" +
                              " modified.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while modifying the Current Sensor device '" +
                         iotDevice.getIotDeviceId() + "' data.";
            log.error(msg, e);
            throw new IotDeviceManagementDAOException(msg, e);
        } finally {
            IotDeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return status;
    }

    @Override
    public boolean deleteIotDevice(String iotDeviceId) throws IotDeviceManagementDAOException {
        boolean status = false;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = CurrentSensorDAO.getConnection();
            String deleteDBQuery =
                    "DELETE FROM CURRENT_SENSOR_DEVICE WHERE CURRENT_SENSOR_DEVICE_ID = ?";
            stmt = conn.prepareStatement(deleteDBQuery);
            stmt.setString(1, iotDeviceId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                status = true;
                if (log.isDebugEnabled()) {
                    log.debug("Current Sensor device " + iotDeviceId + " data has deleted" +
                              " from the Current Sensor database.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting Current Sensor device " + iotDeviceId;
            log.error(msg, e);
            throw new IotDeviceManagementDAOException(msg, e);
        } finally {
            IotDeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return status;
    }

    @Override
    public List<IotDevice> getAllIotDevices() throws IotDeviceManagementDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        IotDevice iotDevice;
        List<IotDevice> iotDevices = new ArrayList<IotDevice>();

        try {
            conn = CurrentSensorDAO.getConnection();
            String selectDBQuery =
                    "SELECT CURRENT_SENSOR_DEVICE_ID, DEVICE_NAME FROM CURRENT_SENSOR_DEVICE";
            stmt = conn.prepareStatement(selectDBQuery);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                iotDevice = new IotDevice();
                iotDevice.setIotDeviceId(resultSet.getString(CurrentSensorConstants.DEVICE_PLUGIN_DEVICE_ID));
                iotDevice.setIotDeviceName(resultSet.getString(CurrentSensorConstants.DEVICE_PLUGIN_DEVICE_NAME));
                Map<String, String> propertyMap = new HashMap<String, String>();
                iotDevice.setDeviceProperties(propertyMap);
                iotDevices.add(iotDevice);
            }
            if (log.isDebugEnabled()) {
                log.debug("All Current Sensor device details have fetched from Current Sensor database.");
            }
            return iotDevices;
        } catch (SQLException e) {
            String msg = "Error occurred while fetching all Current Sensor device data'";
            log.error(msg, e);
            throw new IotDeviceManagementDAOException(msg, e);
        } finally {
            IotDeviceManagementDAOUtil.cleanupResources(stmt, resultSet);
            CurrentSensorDAO.closeConnection();
        }

    }

}
