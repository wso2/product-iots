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

package org.deviceautomation.geolocationTracker.plugin.impl.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deviceautomation.geolocationTracker.plugin.constants.GeoLocationTrackerConstants;
import org.deviceautomation.geolocationTracker.plugin.exception.DeviceMgtPluginException;
import org.deviceautomation.geolocationTracker.plugin.impl.dao.GeoLocationTrackerDAO;
import org.deviceautomation.geolocationTracker.plugin.impl.util.GeoLocationTrackerUtils;
import org.wso2.carbon.device.mgt.common.Device;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements IotDeviceDAO for geolocationTracker Devices.
 */
public class GeoLocationTrackerDAOImpl {

    private static final Log log = LogFactory.getLog(GeoLocationTrackerDAOImpl.class);

    public Device getDevice(String deviceId) throws DeviceMgtPluginException {
        Connection conn;
        PreparedStatement stmt = null;
        Device iotDevice = null;
        ResultSet resultSet = null;
        try {
            conn = GeoLocationTrackerDAO.getConnection();
            String selectDBQuery =
                    "SELECT geolocationTracker_DEVICE_ID, DEVICE_NAME" +
                            " FROM geolocationTracker_DEVICE WHERE geolocationTracker_DEVICE_ID = ?";
            stmt = conn.prepareStatement(selectDBQuery);
            stmt.setString(1, deviceId);
            resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                iotDevice = new Device();
                iotDevice.setName(resultSet.getString(
                        GeoLocationTrackerConstants.DEVICE_PLUGIN_DEVICE_NAME));
                if (log.isDebugEnabled()) {
                    log.debug("geolocationTracker device " + deviceId + " data has been fetched from " +
                            "geolocationTracker database.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while fetching geolocationTracker device : '" + deviceId + "'";
            log.error(msg, e);
            throw new DeviceMgtPluginException(msg, e);
        } finally {
            GeoLocationTrackerUtils.cleanupResources(stmt, resultSet);
            GeoLocationTrackerDAO.closeConnection();
        }
        return iotDevice;
    }

    public boolean addDevice(Device device) throws DeviceMgtPluginException {
        boolean status = false;
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = GeoLocationTrackerDAO.getConnection();
            String createDBQuery =
                    "INSERT INTO geolocationTracker_DEVICE(geolocationTracker_DEVICE_ID, DEVICE_NAME) VALUES (?, ?)";
            stmt = conn.prepareStatement(createDBQuery);
            stmt.setString(1, device.getDeviceIdentifier());
            stmt.setString(2, device.getName());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                status = true;
                if (log.isDebugEnabled()) {
                    log.debug("geolocationTracker device " + device.getDeviceIdentifier() + " data has been" +
                            " added to the geolocationTracker database.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while adding the geolocationTracker device '" +
                    device.getDeviceIdentifier() + "' to the geolocationTracker db.";
            log.error(msg, e);
            throw new DeviceMgtPluginException(msg, e);
        } finally {
            GeoLocationTrackerUtils.cleanupResources(stmt, null);
        }
        return status;
    }

    public boolean updateDevice(Device device) throws DeviceMgtPluginException {
        boolean status = false;
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = GeoLocationTrackerDAO.getConnection();
            String updateDBQuery =
                    "UPDATE geolocationTracker_DEVICE SET  DEVICE_NAME = ? WHERE geolocationTracker_DEVICE_ID = ?";
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
                    log.debug("geolocationTracker device " + device.getDeviceIdentifier() + " data has been" +
                            " modified.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while modifying the geolocationTracker device '" +
                    device.getDeviceIdentifier() + "' data.";
            log.error(msg, e);
            throw new DeviceMgtPluginException(msg, e);
        } finally {
            GeoLocationTrackerUtils.cleanupResources(stmt, null);
        }
        return status;
    }

    public boolean deleteDevice(String deviceId) throws DeviceMgtPluginException {
        boolean status = false;
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = GeoLocationTrackerDAO.getConnection();
            String deleteDBQuery =
                    "DELETE FROM geolocationTracker_DEVICE WHERE geolocationTracker_DEVICE_ID = ?";
            stmt = conn.prepareStatement(deleteDBQuery);
            stmt.setString(1, deviceId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                status = true;
                if (log.isDebugEnabled()) {
                    log.debug("geolocationTracker device " + deviceId + " data has deleted" +
                            " from the geolocationTracker database.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting geolocationTracker device " + deviceId;
            log.error(msg, e);
            throw new DeviceMgtPluginException(msg, e);
        } finally {
            GeoLocationTrackerUtils.cleanupResources(stmt, null);
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
            conn = GeoLocationTrackerDAO.getConnection();
            String selectDBQuery =
                    "SELECT geolocationTracker_DEVICE_ID, DEVICE_NAME " +
                            "FROM geolocationTracker_DEVICE";
            stmt = conn.prepareStatement(selectDBQuery);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                device = new Device();
                device.setDeviceIdentifier(resultSet.getString(GeoLocationTrackerConstants.DEVICE_PLUGIN_DEVICE_ID));
                device.setName(resultSet.getString(GeoLocationTrackerConstants.DEVICE_PLUGIN_DEVICE_NAME));
                List<Device.Property> propertyList = new ArrayList<>();
                device.setProperties(propertyList);
                devices.add(device);
            }
            if (log.isDebugEnabled()) {
                log.debug("All geolocationTracker device details have fetched from geolocationTracker database.");
            }
            return devices;
        } catch (SQLException e) {
            String msg = "Error occurred while fetching all geolocationTracker device data'";
            log.error(msg, e);
            throw new DeviceMgtPluginException(msg, e);
        } finally {
            GeoLocationTrackerUtils.cleanupResources(stmt, resultSet);
            GeoLocationTrackerDAO.closeConnection();
        }
    }
}
