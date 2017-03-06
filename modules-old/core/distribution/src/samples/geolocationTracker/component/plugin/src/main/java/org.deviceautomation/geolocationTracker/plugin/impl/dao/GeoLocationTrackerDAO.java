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

package org.deviceautomation.geolocationTracker.plugin.impl.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deviceautomation.geolocationTracker.plugin.constants.GeoLocationTrackerConstants;
import org.deviceautomation.geolocationTracker.plugin.exception.DeviceMgtPluginException;
import org.deviceautomation.geolocationTracker.plugin.impl.dao.impl.GeoLocationTrackerDAOImpl;
import org.deviceautomation.geolocationTracker.plugin.internal.GeoLocationTrackerManagementDataHolder;
import org.wso2.carbon.device.mgt.iot.devicetype.config.DeviceManagementConfiguration;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database handler which is specified for geolocationTracker type
 */
public class GeoLocationTrackerDAO {

    private static final Log log = LogFactory.getLog(GeoLocationTrackerDAO.class);
    static DataSource dataSource;           // package local variable
    private static ThreadLocal<Connection> currentConnection = new ThreadLocal<>();

    public GeoLocationTrackerDAO() {
        initDeviceTypeDAO();
    }

    public static void initDeviceTypeDAO() {
        DeviceManagementConfiguration deviceManagementConfiguration = GeoLocationTrackerManagementDataHolder.getInstance()
                .getDeviceTypeConfigService().getConfiguration(GeoLocationTrackerConstants.DEVICE_TYPE,
                        GeoLocationTrackerConstants.DEVICE_TYPE_PROVIDER_DOMAIN);
        String datasource = deviceManagementConfiguration.getDeviceManagementConfigRepository().getDataSourceConfig()
                .getJndiLookupDefinition().getJndiName();
        try {
            Context ctx = new InitialContext();
            dataSource = (DataSource) ctx.lookup(datasource);
        } catch (NamingException e) {
            log.error("Error while looking up the data source: " + datasource, e);
        }
    }

    public static void beginTransaction() throws DeviceMgtPluginException {
        try {
            Connection conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            currentConnection.set(conn);
        } catch (SQLException e) {
            throw new DeviceMgtPluginException("Error occurred while retrieving datasource connection", e);
        }
    }

    public static Connection getConnection() throws DeviceMgtPluginException {
        if (currentConnection.get() == null) {
            try {
                currentConnection.set(dataSource.getConnection());
            } catch (SQLException e) {
                throw new DeviceMgtPluginException("Error occurred while retrieving data source connection",
                        e);
            }
        }
        return currentConnection.get();
    }

    public static void commitTransaction() throws DeviceMgtPluginException {
        try {
            Connection conn = currentConnection.get();
            if (conn != null) {
                conn.commit();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Datasource connection associated with the current thread is null, hence commit " +
                            "has not been attempted");
                }
            }
        } catch (SQLException e) {
            throw new DeviceMgtPluginException("Error occurred while committing the transaction", e);
        } finally {
            closeConnection();
        }
    }

    public static void closeConnection() throws DeviceMgtPluginException {

        Connection con = currentConnection.get();
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.error("Error occurred while close the connection");
            }
        }
        currentConnection.remove();
    }

    public static void rollbackTransaction() throws DeviceMgtPluginException {
        try {
            Connection conn = currentConnection.get();
            if (conn != null) {
                conn.rollback();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Datasource connection associated with the current thread is null, hence rollback " +
                            "has not been attempted");
                }
            }
        } catch (SQLException e) {
            throw new DeviceMgtPluginException("Error occurred while rollback the transaction", e);
        } finally {
            closeConnection();
        }
    }

    public GeoLocationTrackerDAOImpl getDeviceTypeDAO() {
        return new GeoLocationTrackerDAOImpl();
    }
}