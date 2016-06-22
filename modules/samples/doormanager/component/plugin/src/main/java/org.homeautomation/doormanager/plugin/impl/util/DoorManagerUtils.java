/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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

package org.homeautomation.doormanager.plugin.impl.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.homeautomation.doormanager.plugin.constants.DoorManagerConstants;
import org.homeautomation.doormanager.plugin.exception.DoorManagerDeviceMgtPluginException;
import org.homeautomation.doormanager.plugin.internal.DoorManagerManagementDataHolder;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.Utils;
import org.wso2.carbon.device.mgt.iot.devicetype.config.DeviceManagementConfiguration;
import org.wso2.carbon.event.output.adapter.core.MessageType;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Contains utility methods used by doormanager plugin.
 */
public class DoorManagerUtils {

    private static Log log = LogFactory.getLog(org.homeautomation.doormanager.plugin.impl.util.DoorManagerUtils.class);

    public static void cleanupResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.warn("Error occurred while closing result set", e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.warn("Error occurred while closing prepared statement", e);
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.warn("Error occurred while closing database connection", e);
            }
        }
    }

    public static void cleanupResources(PreparedStatement stmt, ResultSet rs) {
        cleanupResources(null, stmt, rs);
    }

    public static void setupDeviceManagementSchema() throws DoorManagerDeviceMgtPluginException {
        DeviceManagementConfiguration deviceManagementConfiguration = DoorManagerManagementDataHolder.getInstance()
                .getDeviceTypeConfigService().getConfiguration(DoorManagerConstants.DEVICE_TYPE,
                        DoorManagerConstants.DEVICE_TYPE_PROVIDER_DOMAIN);
        String datasource = deviceManagementConfiguration.getDeviceManagementConfigRepository().getDataSourceConfig()
                .getJndiLookupDefinition().getJndiName();
        try {
            Context ctx = new InitialContext();
            DataSource dataSource = (DataSource) ctx.lookup(datasource);
            DeviceSchemaInitializer initializer = new DeviceSchemaInitializer(dataSource);
            log.info("Initializing device management repository database schema");
            initializer.createRegistryDatabase();
        } catch (NamingException e) {
            log.error("Error while looking up the data source: " + datasource, e);
        } catch (Exception e) {
            throw new DoorManagerDeviceMgtPluginException("Error occurred while initializing Iot Device " +
                    "Management database schema", e);
        }
    }
}
