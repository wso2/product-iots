/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.device.mgt.iot.sample.arduino.plugin.impl.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.iot.common.util.iotdevice.dao.IotDeviceDAO;
import org.wso2.carbon.device.mgt.iot.common.util.iotdevice.dao.IotDeviceManagementDAOException;
import org.wso2.carbon.device.mgt.iot.common.util.iotdevice.dao.util.IotDeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.iot.common.util.iotdevice.dto.IotDevice;
import org.wso2.carbon.device.mgt.iot.sample.arduino.plugin.constants.ArduinoConstants;
import org.wso2.carbon.device.mgt.iot.sample.arduino.plugin.impl.dao.ArduinoDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements IotDeviceDAO for arduino Devices.
 */
public class ArduinoDeviceDAOImpl implements IotDeviceDAO{
	

	    private static final Log log = LogFactory.getLog(ArduinoDeviceDAOImpl.class);

	    @Override
	    public IotDevice getIotDevice(String iotDeviceId)
	            throws IotDeviceManagementDAOException {
	        Connection conn = null;
	        PreparedStatement stmt = null;
	        IotDevice iotDevice = null;
	        ResultSet resultSet = null;
	        try {
	            conn = ArduinoDAO.getConnection();
	            String selectDBQuery =
						"SELECT ARDUINO_DEVICE_ID, DEVICE_NAME" +
						" FROM ARDUINO_DEVICE WHERE ARDUINO_DEVICE_ID = ?";
	            stmt = conn.prepareStatement(selectDBQuery);
	            stmt.setString(1, iotDeviceId);
	            resultSet = stmt.executeQuery();

	            if (resultSet.next()) {
					iotDevice = new IotDevice();
					iotDevice.setIotDeviceName(resultSet.getString(
							ArduinoConstants.DEVICE_PLUGIN_DEVICE_NAME));
					Map<String, String> propertyMap = new HashMap<String, String>();
					


					iotDevice.setDeviceProperties(propertyMap);

					if (log.isDebugEnabled()) {
						log.debug("Arduino device " + iotDeviceId + " data has been fetched from " +
						          "Arduino database.");
					}
				}
	        } catch (SQLException e) {
	            String msg = "Error occurred while fetching Arduino device : '" + iotDeviceId + "'";
	            log.error(msg, e);
	            throw new IotDeviceManagementDAOException(msg, e);
	        } finally {
	            IotDeviceManagementDAOUtil.cleanupResources(stmt, resultSet);
	            ArduinoDAO.closeConnection();
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
	            conn = ArduinoDAO.getConnection();
	            String createDBQuery =
						"INSERT INTO ARDUINO_DEVICE(ARDUINO_DEVICE_ID, DEVICE_NAME) VALUES (?, ?)";

	            stmt = conn.prepareStatement(createDBQuery);
				stmt.setString(1, iotDevice.getIotDeviceId());
				stmt.setString(2,iotDevice.getIotDeviceName());
				if (iotDevice.getDeviceProperties() == null) {
					iotDevice.setDeviceProperties(new HashMap<String, String>());
				}
			
				
				int rows = stmt.executeUpdate();
				if (rows > 0) {
					status = true;
					if (log.isDebugEnabled()) {
						log.debug("Arduino device " + iotDevice.getIotDeviceId() + " data has been" +
						          " added to the Arduino database.");
					}
				}
	        } catch (SQLException e) {
	            String msg = "Error occurred while adding the Arduino device '" +
	                         iotDevice.getIotDeviceId() + "' to the Arduino db.";
	            log.error(msg, e);
	            throw new IotDeviceManagementDAOException(msg, e);
	        } finally {
	            IotDeviceManagementDAOUtil.cleanupResources(stmt, null);
	        }
	        return status;
	    }

	    @Override
	    public boolean updateIotDevice(IotDevice iotDevice)
	            throws IotDeviceManagementDAOException {
	        boolean status = false;
	        Connection conn = null;
	        PreparedStatement stmt = null;
	        try {
	            conn = ArduinoDAO.getConnection();
	            String updateDBQuery =
						"UPDATE ARDUINO_DEVICE SET  DEVICE_NAME = ? WHERE ARDUINO_DEVICE_ID = ?";

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
						log.debug("Arduino device " + iotDevice.getIotDeviceId() + " data has been" +
						          " modified.");
					}
				}
	        } catch (SQLException e) {
	            String msg = "Error occurred while modifying the Arduino device '" +
	                         iotDevice.getIotDeviceId() + "' data.";
	            log.error(msg, e);
	            throw new IotDeviceManagementDAOException(msg, e);
	        } finally {
	            IotDeviceManagementDAOUtil.cleanupResources(stmt, null);
	        }
	        return status;
	    }

	    @Override
	    public boolean deleteIotDevice(String iotDeviceId)
	            throws IotDeviceManagementDAOException {
	        boolean status = false;
	        Connection conn = null;
	        PreparedStatement stmt = null;
	        try {
	            conn = ArduinoDAO.getConnection();
	            String deleteDBQuery =
						"DELETE FROM ARDUINO_DEVICE WHERE ARDUINO_DEVICE_ID = ?";
				stmt = conn.prepareStatement(deleteDBQuery);
				stmt.setString(1, iotDeviceId);
				int rows = stmt.executeUpdate();
				if (rows > 0) {
					status = true;
					if (log.isDebugEnabled()) {
						log.debug("Arduino device " + iotDeviceId + " data has deleted" +
						          " from the Arduino database.");
					}
				}
	        } catch (SQLException e) {
	            String msg = "Error occurred while deleting Arduino device " + iotDeviceId;
	            log.error(msg, e);
	            throw new IotDeviceManagementDAOException(msg, e);
	        } finally {
	            IotDeviceManagementDAOUtil.cleanupResources(stmt, null);
	        }
	        return status;
	    }

	    @Override
	    public List<IotDevice> getAllIotDevices()
	            throws IotDeviceManagementDAOException {

	        Connection conn = null;
	        PreparedStatement stmt = null;
	        ResultSet resultSet = null;
	        IotDevice iotDevice;
	        List<IotDevice> iotDevices = new ArrayList<IotDevice>();

	        try {
	            conn = ArduinoDAO.getConnection();
	            String selectDBQuery =
						"SELECT ARDUINO_DEVICE_ID, DEVICE_NAME " +
						"FROM ARDUINO_DEVICE";
				stmt = conn.prepareStatement(selectDBQuery);
				resultSet = stmt.executeQuery();
				while (resultSet.next()) {
					iotDevice = new IotDevice();
					iotDevice.setIotDeviceId(resultSet.getString(ArduinoConstants.DEVICE_PLUGIN_DEVICE_ID));
					iotDevice.setIotDeviceName(resultSet.getString(ArduinoConstants.DEVICE_PLUGIN_DEVICE_NAME));

					Map<String, String> propertyMap = new HashMap<String, String>();

					iotDevice.setDeviceProperties(propertyMap);
					iotDevices.add(iotDevice);
				}
	            if (log.isDebugEnabled()) {
	                log.debug("All Arduino device details have fetched from Arduino database.");
	            }
	            return iotDevices;
	        } catch (SQLException e) {
	            String msg = "Error occurred while fetching all Arduino device data'";
	            log.error(msg, e);
	            throw new IotDeviceManagementDAOException(msg, e);
	        } finally {
	            IotDeviceManagementDAOUtil.cleanupResources(stmt, resultSet);
	            ArduinoDAO.closeConnection();
	        }
	        
	    }

	}