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

package org.homeautomation.currentsensor.plugin.impl.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.homeautomation.currentsensor.plugin.constants.CurrentSensorConstants;
import org.homeautomation.currentsensor.plugin.exception.CurrentSensorDeviceMgtPluginException;
import org.homeautomation.currentsensor.plugin.impl.dao.impl.CurrentSensorDAO;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class CurrentSensorDAOUtil{

    private static final Log log = LogFactory.getLog(CurrentSensorDAOUtil.class);
    static DataSource dataSource;
    private static ThreadLocal<Connection> currentConnection = new ThreadLocal<Connection>();

    public CurrentSensorDAOUtil() {
        initCurrentSensorDAO();
    }

    public static void initCurrentSensorDAO() {
        try {
            Context ctx = new InitialContext();
            dataSource = (DataSource) ctx.lookup(CurrentSensorConstants.DATA_SOURCE_NAME);
        } catch (NamingException e) {
            log.error("Error while looking up the data source: " +
                      CurrentSensorConstants.DATA_SOURCE_NAME);
        }

    }


    public CurrentSensorDAO getCurrentSensorDeviceDAO() {
        return new CurrentSensorDAO();
    }

    public static void beginTransaction() throws CurrentSensorDeviceMgtPluginException {
        try {
            Connection conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            currentConnection.set(conn);
        } catch (SQLException e) {
            throw new CurrentSensorDeviceMgtPluginException(
                    "Error occurred while retrieving datasource connection", e);
        }
    }

    public static Connection getConnection() throws CurrentSensorDeviceMgtPluginException {
        if (currentConnection.get() == null) {
            try {
                currentConnection.set(dataSource.getConnection());
            } catch (SQLException e) {
                throw new CurrentSensorDeviceMgtPluginException(
                        "Error occurred while retrieving data source connection", e);
            }
        }
        return currentConnection.get();
    }

    public static void commitTransaction() throws CurrentSensorDeviceMgtPluginException {
        try {
            Connection conn = currentConnection.get();
            if (conn != null) {
                conn.commit();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Datasource connection associated with the current thread is null, " +
                              "hence commit has not been attempted");
                }
            }
        } catch (SQLException e) {
            throw new CurrentSensorDeviceMgtPluginException(
                    "Error occurred while committing the transaction", e);
        } finally {
            closeConnection();
        }
    }

    public static void closeConnection() throws CurrentSensorDeviceMgtPluginException {

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

    public static void rollbackTransaction() throws CurrentSensorDeviceMgtPluginException {
        try {
            Connection conn = currentConnection.get();
            if (conn != null) {
                conn.rollback();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Datasource connection associated with the current thread is null, " +
                            "hence rollback has not been attempted");
                }
            }
        } catch (SQLException e) {
            throw new CurrentSensorDeviceMgtPluginException("Error occurred while rollback the transaction", e);
        } finally {
            closeConnection();
        }
    }
}