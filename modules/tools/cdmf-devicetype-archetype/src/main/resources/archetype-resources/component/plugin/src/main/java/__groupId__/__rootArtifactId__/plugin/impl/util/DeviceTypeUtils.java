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

package ${groupId}.${rootArtifactId}.plugin.impl.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;



import ${groupId}.${rootArtifactId}.plugin.constants.DeviceTypeConstants;
import ${groupId}.${rootArtifactId}.plugin.exception.DeviceMgtPluginException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Contains utility methods used by ${rootArtifactId} plugin.
 */
public class DeviceTypeUtils {

    private static Log log = LogFactory.getLog(DeviceTypeUtils.class);

    public static String getDeviceProperty(List<Device.Property> deviceProperties, String propertyKey) {
        String deviceProperty = "";
        for (Device.Property property : deviceProperties) {
            if (propertyKey.equals(property.getName())) {
                deviceProperty = property.getValue();
            }
        }
        return deviceProperty;
    }

    public static Device.Property getProperty(String property, String value) {
        if (property != null) {
            Device.Property prop = new Device.Property();
            prop.setName(property);
            prop.setValue(value);
            return prop;
        }
        return null;
    }

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

    /**
     * Creates the device management schema.
     */
    public static void setupDeviceManagementSchema() throws DeviceMgtPluginException {
        try {
            Context ctx = new InitialContext();
            DataSource dataSource = (DataSource) ctx.lookup(DeviceTypeConstants.DATA_SOURCE_NAME);
            DeviceSchemaInitializer initializer =
                    new DeviceSchemaInitializer(dataSource);
            log.info("Initializing device management repository database schema");
            initializer.createRegistryDatabase();
        } catch (NamingException e) {
            log.error("Error while looking up the data source: " + DeviceTypeConstants.DATA_SOURCE_NAME);
        } catch (Exception e) {
            throw new DeviceMgtPluginException("Error occurred while initializing Iot Device " +
                    "Management database schema", e);
        }
    }

}
