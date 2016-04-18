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

package org.coffeeking.connectedcup.plugin.impl.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.coffeeking.connectedcup.plugin.constants.ConnectedCupConstants;
import org.coffeeking.connectedcup.plugin.exception.ConnectedCupDeviceMgtPluginException;
import org.coffeeking.connectedcup.plugin.impl.dao.ConnectedCupDAOUtil;
import org.coffeeking.connectedcup.plugin.impl.dao.util.ConnectedCupUtils;
import org.wso2.carbon.device.mgt.common.Device;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *  Device Dao for connected cup Devices.
 */
public class ConnectedCupDAO {


	private static final Log log = LogFactory.getLog(ConnectedCupDAO.class);

	public Device getDevice(String deviceId) throws ConnectedCupDeviceMgtPluginException {
		Connection conn = null;
		PreparedStatement stmt = null;
		Device connectedCupDevice = null;
		ResultSet resultSet = null;
		try {
			conn = ConnectedCupDAOUtil.getConnection();
			String selectDBQuery =
					"SELECT CONNECTED_CUP_DEVICE_ID, DEVICE_NAME, ACCESS_TOKEN, REFRESH_TOKEN" +
							" FROM CONNECTED_CUP_DEVICE WHERE CONNECTED_CUP_DEVICE_ID = ?";
			stmt = conn.prepareStatement(selectDBQuery);
			stmt.setString(1, deviceId);
			resultSet = stmt.executeQuery();

			if (resultSet.next()) {
				connectedCupDevice = new Device();
				connectedCupDevice.setName(resultSet.getString(
						ConnectedCupConstants.DEVICE_PLUGIN_DEVICE_NAME));
				List<Device.Property> propertyList = new ArrayList<Device.Property>();
				propertyList.add(ConnectedCupUtils.getProperty(
						ConnectedCupConstants.DEVICE_PLUGIN_PROPERTY_ACCESS_TOKEN,
						resultSet.getString("ACCESS_TOKEN")));
				propertyList.add(ConnectedCupUtils.getProperty(
						ConnectedCupConstants.DEVICE_PLUGIN_PROPERTY_REFRESH_TOKEN,
						resultSet.getString("REFRESH_TOKEN")));
				connectedCupDevice.setProperties(propertyList);

				if (log.isDebugEnabled()) {
					log.debug("Connected Cup service " + deviceId + " data has been fetched from" +
									  "Connected Cup database.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while fetching Connected Cup device : '" + deviceId + "'";
			log.error(msg, e);
			throw new ConnectedCupDeviceMgtPluginException(msg, e);
		} finally {
			ConnectedCupUtils.cleanupResources(stmt, resultSet);
			ConnectedCupDAOUtil.closeConnection();
		}
		return connectedCupDevice;
	}


	public boolean addDevice(Device connectedCupDevice) throws ConnectedCupDeviceMgtPluginException {
		boolean status = false;
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = ConnectedCupDAOUtil.getConnection();
			String createDBQuery =
					"INSERT INTO CONNECTED_CUP_DEVICE(CONNECTED_CUP_DEVICE_ID, DEVICE_NAME, " +
							"ACCESS_TOKEN, REFRESH_TOKEN) VALUES (?, ?, ?, ?)";

			stmt = conn.prepareStatement(createDBQuery);
			stmt.setString(1, connectedCupDevice.getDeviceIdentifier());
			stmt.setString(2, connectedCupDevice.getName());
			stmt.setString(3, ConnectedCupUtils.getDeviceProperty(
					connectedCupDevice.getProperties(),
					ConnectedCupConstants.DEVICE_PLUGIN_PROPERTY_ACCESS_TOKEN));
			stmt.setString(4, ConnectedCupUtils.getDeviceProperty(
					connectedCupDevice.getProperties(),
					ConnectedCupConstants.DEVICE_PLUGIN_PROPERTY_REFRESH_TOKEN));

			int rows = stmt.executeUpdate();
			if (rows > 0) {
				status = true;
				if (log.isDebugEnabled()) {
					log.debug("Connected Cup device " + connectedCupDevice.getDeviceIdentifier() +
									  " data has been added to the Connected Cup database.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while adding the Connected Cup device '" +
					connectedCupDevice.getDeviceIdentifier() + "' to the Connected Cup db.";
			log.error(msg, e);
			throw new ConnectedCupDeviceMgtPluginException(msg, e);
		} finally {
			ConnectedCupUtils.cleanupResources(stmt, null);
		}
		return status;
	}

	public boolean updateDevice(Device connectedCupDevice) throws ConnectedCupDeviceMgtPluginException {
		boolean status = false;
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = ConnectedCupDAOUtil.getConnection();
			String updateDBQuery =
					"UPDATE CONNECTED_CUP_DEVICE SET  DEVICE_NAME = ?, ACCESS_TOKEN=?, " +
							"REFRESH_TOKEN=? WHERE CONNECTED_CUP_DEVICE_ID = ?";

			stmt = conn.prepareStatement(updateDBQuery);

			if (connectedCupDevice.getProperties() == null) {
				connectedCupDevice.setProperties(new ArrayList<Device.Property>());
			}
			stmt.setString(1, connectedCupDevice.getName());
			stmt.setString(2, ConnectedCupUtils.getDeviceProperty(
					connectedCupDevice.getProperties(),
					ConnectedCupConstants.DEVICE_PLUGIN_PROPERTY_ACCESS_TOKEN));
			stmt.setString(3, ConnectedCupUtils.getDeviceProperty(
					connectedCupDevice.getProperties(),
					ConnectedCupConstants.DEVICE_PLUGIN_PROPERTY_REFRESH_TOKEN));
			stmt.setString(4, connectedCupDevice.getDeviceIdentifier());
			int rows = stmt.executeUpdate();
			if (rows > 0) {
				status = true;
				if (log.isDebugEnabled()) {
					log.debug("Connected Cup device " + connectedCupDevice.getDeviceIdentifier() +
									  " data has been modified.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while modifying the Connected Cup device '" +
					connectedCupDevice.getDeviceIdentifier() + "' data.";
			log.error(msg, e);
			throw new ConnectedCupDeviceMgtPluginException(msg, e);
		} finally {
			ConnectedCupUtils.cleanupResources(stmt, null);
		}
		return status;
	}

	public boolean deleteDevice(String deviceId) throws ConnectedCupDeviceMgtPluginException {
		boolean status = false;
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = ConnectedCupDAOUtil.getConnection();
			String deleteDBQuery =
					"DELETE FROM CONNECTED_CUP_DEVICE WHERE CONNECTED_CUP_DEVICE_ID = ?";
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
			throw new ConnectedCupDeviceMgtPluginException(msg, e);
		} finally {
			ConnectedCupUtils.cleanupResources(stmt, null);
		}
		return status;
	}

	public List<Device> getAllDevices() throws ConnectedCupDeviceMgtPluginException {

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		Device connectedCupDevice;
		List<Device> iotDevices = new ArrayList<>();

		try {
			conn = ConnectedCupDAOUtil.getConnection();
			String selectDBQuery =
					"SELECT CONNECTED_CUP_DEVICE_ID, DEVICE_NAME, ACCESS_TOKEN, REFRESH_TOKEN" +
							"FROM CONNECTED_CUP_DEVICE";
			stmt = conn.prepareStatement(selectDBQuery);
			resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				connectedCupDevice = new Device();
				connectedCupDevice.setDeviceIdentifier(resultSet.getString(
						ConnectedCupConstants.DEVICE_PLUGIN_DEVICE_ID));
				connectedCupDevice.setName(resultSet.getString(
						ConnectedCupConstants.DEVICE_PLUGIN_DEVICE_NAME));

				List<Device.Property> propertyList = new ArrayList<Device.Property>();
				propertyList.add(ConnectedCupUtils.getProperty(
						ConnectedCupConstants.DEVICE_PLUGIN_PROPERTY_ACCESS_TOKEN,
						resultSet.getString("ACCESS_TOKEN")));
				propertyList.add(ConnectedCupUtils.getProperty(
						ConnectedCupConstants.DEVICE_PLUGIN_PROPERTY_REFRESH_TOKEN,
						resultSet.getString("REFRESH_TOKEN")));
				connectedCupDevice.setProperties(propertyList);
			}
			if (log.isDebugEnabled()) {
				log.debug("All Connected Cup device details have fetched from Connected Cup database" +
								".");
			}
			return iotDevices;
		} catch (SQLException e) {
			String msg = "Error occurred while fetching all Connected Cup device data'";
			log.error(msg, e);
			throw new ConnectedCupDeviceMgtPluginException(msg, e);
		} finally {
			ConnectedCupUtils.cleanupResources(stmt, resultSet);
			ConnectedCupDAOUtil.closeConnection();
		}

	}

}