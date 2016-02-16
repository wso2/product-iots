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

package org.homeautomation.doormanager.plugin.impl.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.homeautomation.doormanager.plugin.constants.DoorManagerConstants;
import org.homeautomation.doormanager.plugin.exception.DoorManagerDeviceMgtPluginException;
import org.homeautomation.doormanager.plugin.impl.dao.DoorManagerDAO;
import org.homeautomation.doormanager.plugin.impl.dao.DoorLockSafe;
import org.homeautomation.doormanager.plugin.impl.dao.util.DoorManagerUtils;
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
 * Device Dao for automatic door locker Devices.
 */
public class DoorManagerDAOImpl {


	private static final Log log = LogFactory.getLog(DoorManagerDAOImpl.class);

	public Device getDevice(String deviceId) throws DoorManagerDeviceMgtPluginException {
		Connection conn = null;
		PreparedStatement stmt = null;
		Device automaticDoorLockerDevice = null;
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
				automaticDoorLockerDevice = new Device();
				automaticDoorLockerDevice.setName(resultSet.getString(
						DoorManagerConstants.DEVICE_PLUGIN_DEVICE_NAME));
				List<Device.Property> properties = new ArrayList<>();
				automaticDoorLockerDevice.setProperties(properties);
				if (log.isDebugEnabled()) {
					log.debug("Locker Manager service " + deviceId + " data has been fetched from" +
							"Locker Manager database.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while fetching Locker Manager device : '" + deviceId + "'";
			log.error(msg, e);
			throw new DoorManagerDeviceMgtPluginException(msg, e);
		} finally {
			DoorManagerUtils.cleanupResources(stmt, resultSet);
			DoorManagerDAO.closeConnection();
		}
		return automaticDoorLockerDevice;
	}


	public boolean addDevice(Device automaticDoorLOcker) throws DoorManagerDeviceMgtPluginException {
		boolean status = false;
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = DoorManagerDAO.getConnection();
			String createDBQuery =
					"INSERT INTO doormanager_DEVICE(doormanager_DEVICE_ID, DEVICE_NAME, ACCESS_TOKEN, REFRESH_TOKEN) VALUES (?, ?, ?, ?)";

			stmt = conn.prepareStatement(createDBQuery);
			stmt.setString(1, automaticDoorLOcker.getDeviceIdentifier());
			stmt.setString(2, automaticDoorLOcker.getName());
			stmt.setString(3, DoorManagerUtils.getDeviceProperty(
					automaticDoorLOcker.getProperties(),
					DoorManagerConstants.DEVICE_PLUGIN_PROPERTY_ACCESS_TOKEN));
			stmt.setString(4, DoorManagerUtils.getDeviceProperty(
					automaticDoorLOcker.getProperties(),
					DoorManagerConstants.DEVICE_PLUGIN_PROPERTY_REFRESH_TOKEN));
			int rows = stmt.executeUpdate();
			if (rows > 0) {
				status = true;
				if (log.isDebugEnabled()) {
					log.debug("Locker Manager device " + automaticDoorLOcker.getDeviceIdentifier() +
							" data has been added to the Locker Manager database.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while adding the Locker Manager device '" +
					automaticDoorLOcker.getDeviceIdentifier() + "' to the Locker Manager db.";
			log.error(msg, e);
			throw new DoorManagerDeviceMgtPluginException(msg, e);
		} finally {
			DoorManagerUtils.cleanupResources(stmt, null);
		}
		return status;
	}

	public boolean registerDoorLockSafe(DoorLockSafe automaticDoorLOcker) throws DoorManagerDeviceMgtPluginException {
		boolean status = false;
		Connection conn = null;
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

		Connection conn = null;
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
            if(resultSet.next()){
                String result = resultSet.getString(DoorManagerConstants.DEVICE_PLUGIN_DEVICE_SERIAL_NUMBER);
                log.warn(result);
                return true;
            }else{
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
		Connection conn = null;
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
            if(resultSet.next()){
                result = resultSet.getString(DoorManagerConstants.DEVICE_PLUGIN_DEVICE_UID_OF_USER);
                log.warn(result);
                return true;
            }else {
				selectDBQuery =
						"SELECT UID_OF_USER FROM SHARED_DOORLOCK_SAFE WHERE SERIAL_NUMBER = ? AND DOORMANAGER_DEVICE_ID = ?  AND UID_OF_USER = ?";
				stmt = conn.prepareStatement(selectDBQuery);
				stmt.setString(1, serialNumber);
				stmt.setString(2, deviceId);
				stmt.setString(3, UIDofUser);
				resultSet = stmt.executeQuery();
                if(resultSet.next()){
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
		Connection conn = null;
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
		boolean status = false;
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = DoorManagerDAO.getConnection();
			String createDBQuery = "SELECT * FROM SHARED_DOORLOCK_SAFE WHERE" +
					" DOORMANAGER_DEVICE_ID = ? AND SERIAL_NUMBER = ?";
			stmt = conn.prepareStatement(createDBQuery);
			stmt.setString(1, cardNum);
			stmt.setString(2, deviceID);
			int rows = stmt.executeUpdate();
			if (rows > 0) {
				status = true;
				if (log.isDebugEnabled()) {
					log.debug("Lock : "+ deviceID + " is associated with card no : "+cardNum);
				}
			}
		} catch (SQLException e) {
			String msg = "No associations were found between lock : "+ deviceID +" and card : "+ cardNum;
			log.error(msg, e);
			throw new DoorManagerDeviceMgtPluginException(msg, e);
		} finally {
			DoorManagerUtils.cleanupResources(stmt, null);
		}
		return status;
	}

	public List<String> getUserCredentials(String deviceId, String UIDofUser) throws DoorManagerDeviceMgtPluginException {

		Connection conn = null;
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
            if(resultSet.next()){
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

	public List<String> getRegisteredDoorLocks(String deviceId) throws DoorManagerDeviceMgtPluginException {

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		List<String> doorLockSafes = new ArrayList<>();
		try {
			conn = DoorManagerDAO.getConnection();
			String selectDBQuery =
					"SELECT SERIAL_NUMBER FROM REGISTERED_DOORLOCK_SAFE WHERE doormanager_DEVICE_ID = ?";
			stmt = conn.prepareStatement(selectDBQuery);
			stmt.setString(1, deviceId);
			resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				doorLockSafes.add(resultSet.getString(DoorManagerConstants.DEVICE_PLUGIN_DEVICE_SERIAL_NUMBER));
			}
			if (log.isDebugEnabled()) {
				log.debug("All Locker Manager device details have fetched from Automatic Door Locker database.");
			}
			return doorLockSafes;
		} catch (SQLException e) {
			String msg = "Error occurred while fetching all Automatic Door Locker device data'";
			log.error(msg, e);
			throw new DoorManagerDeviceMgtPluginException(msg, e);
		} finally {
			DoorManagerUtils.cleanupResources(stmt, resultSet);
			DoorManagerDAO.closeConnection();
		}
	}

	public boolean updateDevice(Device automaticDoorLocker) throws DoorManagerDeviceMgtPluginException {
		boolean status = false;
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = DoorManagerDAO.getConnection();
			String updateDBQuery =
					"UPDATE doormanager_DEVICE SET  DEVICE_NAME = ? WHERE doormanager_DEVICE_ID = ?";
			stmt = conn.prepareStatement(updateDBQuery);
			if (automaticDoorLocker.getProperties() == null) {
				automaticDoorLocker.setProperties(new ArrayList<Device.Property>());
			}
			stmt.setString(1, automaticDoorLocker.getName());
			stmt.setString(2, automaticDoorLocker.getDeviceIdentifier());
			int rows = stmt.executeUpdate();
			if (rows > 0) {
				status = true;
				if (log.isDebugEnabled()) {
					log.debug("Locker Manager device " + automaticDoorLocker.getDeviceIdentifier() +
							" data has been modified.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while modifying the Locker Manager device '" +
					automaticDoorLocker.getDeviceIdentifier() + "' data.";
			log.error(msg, e);
			throw new DoorManagerDeviceMgtPluginException(msg, e);
		} finally {
			DoorManagerUtils.cleanupResources(stmt, null);
		}
		return status;
	}

	public boolean deleteDevice(String deviceId) throws DoorManagerDeviceMgtPluginException {
		boolean status = false;
		Connection conn = null;
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
					log.debug("Automatic Door Locker device " + deviceId + " data has deleted" +
							" from the Automatic Door Locker database.");
				}
			}
		} catch (SQLException e) {
			String msg = "Error occurred while deleting Automatic Door Locker device " + deviceId;
			log.error(msg, e);
			throw new DoorManagerDeviceMgtPluginException(msg, e);
		} finally {
			DoorManagerUtils.cleanupResources(stmt, null);
		}
		return status;
	}

	public List<Device> getAllDevices() throws DoorManagerDeviceMgtPluginException {

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		Device connectedCupDevice;
		List<Device> iotDevices = new ArrayList<>();
		try {
			conn = DoorManagerDAO.getConnection();
			String selectDBQuery =
					"SELECT doormanager_DEVICE_ID, DEVICE_NAME FROM doormanager_DEVICE";
			stmt = conn.prepareStatement(selectDBQuery);
			resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				connectedCupDevice = new Device();
				connectedCupDevice.setDeviceIdentifier(resultSet.getString(
						DoorManagerConstants.DEVICE_PLUGIN_DEVICE_ID));
				connectedCupDevice.setName(resultSet.getString(
						DoorManagerConstants.DEVICE_PLUGIN_DEVICE_NAME));
			}
			if (log.isDebugEnabled()) {
				log.debug("All Locker Manager device details have fetched from Automatic Door Locker database.");
			}
			return iotDevices;
		} catch (SQLException e) {
			String msg = "Error occurred while fetching all Automatic Door Locker device data'";
			log.error(msg, e);
			throw new DoorManagerDeviceMgtPluginException(msg, e);
		} finally {
			DoorManagerUtils.cleanupResources(stmt, resultSet);
			DoorManagerDAO.closeConnection();
		}
	}
}