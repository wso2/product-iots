/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.homeautomation.digitaldisplay.plugin.impl.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.homeautomation.digitaldisplay.plugin.constants.DigitalDisplayConstants;
import org.homeautomation.digitaldisplay.plugin.exception.DigitalDisplayDeviceMgtPluginException;
import org.homeautomation.digitaldisplay.plugin.impl.dao.DigitalDisplayDAO;
import org.homeautomation.digitaldisplay.plugin.impl.util.DigitalDisplayUtils;
import org.wso2.carbon.device.mgt.common.Device;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements CRUD for digital display Devices.
 */
public class DigitalDisplayDeviceDAOImpl  {
	private static final Log log = LogFactory.getLog(DigitalDisplayDeviceDAOImpl.class);

	public Device getDevice(String iotDeviceId) throws DigitalDisplayDeviceMgtPluginException {
		Connection conn = null;
		PreparedStatement stmt = null;
		Device device = null;
		ResultSet resultSet = null;
		try {
			conn = DigitalDisplayDAO.getConnection();
			String selectDBQuery =
					"SELECT DIGITAL_DISPLAY_DEVICE_ID, DEVICE_NAME" +
					" FROM DIGITAL_DISPLAY_DEVICE WHERE DIGITAL_DISPLAY_DEVICE_ID = ?";
			stmt = conn.prepareStatement(selectDBQuery);
			stmt.setString(1, iotDeviceId);
			resultSet = stmt.executeQuery();
			if (resultSet.next()) {
				device = new Device();
				device.setName(resultSet.getString(
                        DigitalDisplayConstants.DEVICE_PLUGIN_DEVICE_NAME));
				if (log.isDebugEnabled()) {
					log.debug("Digital Display device " + iotDeviceId + " data has been fetched from " +
							  "Digital Display database.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while fetching Digital Display device : '" + iotDeviceId + "'";
			log.error(msg, e);
			throw new DigitalDisplayDeviceMgtPluginException(msg, e);
		} finally {
			DigitalDisplayUtils.cleanupResources(stmt, resultSet);
			DigitalDisplayDAO.closeConnection();
		}
		return device;
	}

	public boolean addDevice(Device device) throws DigitalDisplayDeviceMgtPluginException {
		boolean status = false;
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = DigitalDisplayDAO.getConnection();
			String createDBQuery =
					"INSERT INTO DIGITAL_DISPLAY_DEVICE(DIGITAL_DISPLAY_DEVICE_ID, DEVICE_NAME) VALUES (?, ?)";
			stmt = conn.prepareStatement(createDBQuery);
			stmt.setString(1, device.getDeviceIdentifier());
			stmt.setString(2, device.getName());
			int rows = stmt.executeUpdate();
			if (rows > 0) {
				status = true;
				if (log.isDebugEnabled()) {
					log.debug("Digital Display device " + device.getDeviceIdentifier() + " data has been" +
							  " added to the Digital Display database.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while adding the Digital Display device '" +
						 device.getDeviceIdentifier() + "' to the Digital Display db.";
			log.error(msg, e);
			throw new DigitalDisplayDeviceMgtPluginException(msg, e);
		} finally {
			DigitalDisplayUtils.cleanupResources(stmt, null);
		}
		return status;
	}

	public boolean updateDevice(Device device) throws DigitalDisplayDeviceMgtPluginException {
		boolean status = false;
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = DigitalDisplayDAO.getConnection();
			String updateDBQuery =
					"UPDATE DIGITAL_DISPLAY_DEVICE SET  DEVICE_NAME = ? WHERE DIGITAL_DISPLAY_DEVICE_ID = ?";
			stmt = conn.prepareStatement(updateDBQuery);
			stmt.setString(1, device.getName());
			stmt.setString(2, device.getDeviceIdentifier());
			int rows = stmt.executeUpdate();
			if (rows > 0) {
				status = true;
				if (log.isDebugEnabled()) {
					log.debug("Digital Display device " + device.getDeviceIdentifier() + " data has been" +
							  " modified.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while modifying the Digital Display device '" +
						 device.getDeviceIdentifier() + "' data.";
			log.error(msg, e);
			throw new DigitalDisplayDeviceMgtPluginException(msg, e);
		} finally {
			DigitalDisplayUtils.cleanupResources(stmt, null);
		}
		return status;
	}

	public boolean deleteDevice(String iotDeviceId) throws DigitalDisplayDeviceMgtPluginException {
		boolean status = false;
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = DigitalDisplayDAO.getConnection();
			String deleteDBQuery =
					"DELETE FROM DIGITAL_DISPLAY_DEVICE WHERE DIGITAL_DISPLAY_DEVICE_ID = ?";
			stmt = conn.prepareStatement(deleteDBQuery);
			stmt.setString(1, iotDeviceId);
			int rows = stmt.executeUpdate();
			if (rows > 0) {
				status = true;
				if (log.isDebugEnabled()) {
					log.debug("Digital Display device " + iotDeviceId + " data has deleted" +
							  " from the Digital Display database.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while deleting Digital Display device " + iotDeviceId;
			log.error(msg, e);
			throw new DigitalDisplayDeviceMgtPluginException(msg, e);
		} finally {
			DigitalDisplayUtils.cleanupResources(stmt, null);
		}
		return status;
	}

	public List<Device> getAllDevices() throws DigitalDisplayDeviceMgtPluginException {
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		Device iotDevice;
		List<Device> iotDevices = new ArrayList<Device>();

		try {
			conn = DigitalDisplayDAO.getConnection();
			String selectDBQuery =
					"SELECT DIGITAL_DISPLAY_DEVICE_ID, DEVICE_NAME FROM DIGITAL_DISPLAY_DEVICE";
			stmt = conn.prepareStatement(selectDBQuery);
			resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				iotDevice = new Device();
				iotDevice.setDeviceIdentifier(resultSet.getString(DigitalDisplayConstants.DEVICE_PLUGIN_DEVICE_ID));
				iotDevice.setName(resultSet.getString(DigitalDisplayConstants.DEVICE_PLUGIN_DEVICE_NAME));
				iotDevices.add(iotDevice);
			}
			if (log.isDebugEnabled()) {
				log.debug("All Digital Display device details have fetched from Digital Display database.");
			}
			return iotDevices;
		} catch (SQLException e) {
			String msg = "Error occurred while fetching all Digital Display device data'";
			log.error(msg, e);
			throw new DigitalDisplayDeviceMgtPluginException(msg, e);
		} finally {
			DigitalDisplayUtils.cleanupResources(stmt, resultSet);
			DigitalDisplayDAO.closeConnection();
		}
	}
}