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

package org.coffeeking.connectedcup.plugin.impl.dao.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Contains utility methods used by FireAlarm plugin.
 */
public class ConnectedCupUtils {

    private static Log log = LogFactory.getLog(ConnectedCupUtils.class);

    public static String getDeviceProperty(List<Device.Property> deviceProperties, String propertyKey) {
        String deviceProperty = "";
        for(Device.Property property :deviceProperties){
            if(propertyKey.equals(property.getName())){
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


}
