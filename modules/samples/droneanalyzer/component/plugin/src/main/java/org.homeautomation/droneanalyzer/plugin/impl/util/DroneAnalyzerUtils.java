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

package org.homeautomation.droneanalyzer.plugin.impl.util;

import org.homeautomation.droneanalyzer.plugin.constants.DroneAnalyzerConstants;
import org.homeautomation.droneanalyzer.plugin.exception.DeviceMgtPluginException;
import org.homeautomation.droneanalyzer.plugin.internal.DroneAnalyzerManagementDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.Utils;
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
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Contains utility methods used by gg plugin.
 */
public class DroneAnalyzerUtils {

    private static Log log = LogFactory.getLog(org.homeautomation.droneanalyzer.plugin.impl.util.DroneAnalyzerUtils.class);

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
            DataSource dataSource = (DataSource) ctx.lookup(DroneAnalyzerConstants.DATA_SOURCE_NAME);
            DeviceSchemaInitializer initializer =
                    new DeviceSchemaInitializer(dataSource);
            log.info("Initializing device management repository database schema");
            initializer.createRegistryDatabase();
        } catch (NamingException e) {
            log.error("Error while looking up the data source: " + DroneAnalyzerConstants.DATA_SOURCE_NAME);
        } catch (Exception e) {
            throw new DeviceMgtPluginException("Error occurred while initializing Iot Device " +
                    "Management database schema", e);
        }
    }

    public static String replaceMqttProperty(String urlWithPlaceholders) {
        String MQTT_BROKER_HOST = null;
        String MQTT_PORT = null;
        if(!DroneAnalyzerConstants.MQTT_BROKER_HOST.startsWith("$")){
            MQTT_BROKER_HOST = "\\$".concat(DroneAnalyzerConstants.MQTT_BROKER_HOST);
        }
        if(!DroneAnalyzerConstants.MQTT_PORT.startsWith("$")){
            MQTT_PORT = "\\$".concat(DroneAnalyzerConstants.MQTT_PORT);
        }
        urlWithPlaceholders = Utils.replaceSystemProperty(urlWithPlaceholders);
        urlWithPlaceholders = urlWithPlaceholders.replaceAll(MQTT_PORT, "" +
                (DroneAnalyzerConstants.DEFAULT_MQTT_PORT + getPortOffset()));
        urlWithPlaceholders = urlWithPlaceholders.replaceAll(MQTT_BROKER_HOST,
                System.getProperty(DroneAnalyzerConstants.DEFAULT_CARBON_LOCAL_IP_PROPERTY, "localhost"));
        return urlWithPlaceholders;
    }

    private static int getPortOffset() {
        ServerConfiguration carbonConfig = ServerConfiguration.getInstance();
        String portOffset = System.getProperty("portOffset", carbonConfig.getFirstProperty(
                DroneAnalyzerConstants.CARBON_CONFIG_PORT_OFFSET));
        try {
            if ((portOffset != null)) {
                return Integer.parseInt(portOffset.trim());
            } else {
                return DroneAnalyzerConstants.CARBON_DEFAULT_PORT_OFFSET;
            }
        } catch (NumberFormatException e) {
            return DroneAnalyzerConstants.CARBON_DEFAULT_PORT_OFFSET;
        }
    }

    public static void setupMqttOutputAdapter() throws IOException {
        OutputEventAdapterConfiguration outputEventAdapterConfiguration =
                createMqttOutputEventAdapterConfiguration(DroneAnalyzerConstants.MQTT_ADAPTER_NAME,
                        DroneAnalyzerConstants.MQTT_ADAPTER_TYPE, MessageType.TEXT);
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    DroneAnalyzerConstants.DEVICE_TYPE_PROVIDER_DOMAIN, true);
            DroneAnalyzerManagementDataHolder.getInstance().getOutputEventAdapterService()
                    .create(outputEventAdapterConfiguration);
        } catch (OutputEventAdapterException e) {
            log.error("Unable to create Output Event Adapter : " + DroneAnalyzerConstants.MQTT_ADAPTER_NAME, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Create Output Event Adapter Configuration for given configuration.
     *
     * @param name      Output Event Adapter name
     * @param type      Output Event Adapter type
     * @param msgFormat Output Event Adapter message format
     * @return OutputEventAdapterConfiguration instance for given configuration
     */
    private static OutputEventAdapterConfiguration createMqttOutputEventAdapterConfiguration(String name, String type,
                                                                                             String msgFormat) throws IOException {
        OutputEventAdapterConfiguration outputEventAdapterConfiguration = new OutputEventAdapterConfiguration();
        outputEventAdapterConfiguration.setName(name);
        outputEventAdapterConfiguration.setType(type);
        outputEventAdapterConfiguration.setMessageFormat(msgFormat);
        File configFile = new File(DroneAnalyzerConstants.MQTT_CONFIG_LOCATION);
        if (configFile.exists()) {
            Map<String, String> mqttAdapterProperties = new HashMap<>();
            InputStream propertyStream = configFile.toURI().toURL().openStream();
            Properties properties = new Properties();
            properties.load(propertyStream);
            mqttAdapterProperties.put(DroneAnalyzerConstants.USERNAME_PROPERTY_KEY, properties.getProperty(
                    DroneAnalyzerConstants.USERNAME_PROPERTY_KEY));
            mqttAdapterProperties.put(DroneAnalyzerConstants.DCR_PROPERTY_KEY, Utils.replaceSystemProperty(
                    properties.getProperty(DroneAnalyzerConstants.DCR_PROPERTY_KEY)));
            mqttAdapterProperties.put(DroneAnalyzerConstants.BROKER_URL_PROPERTY_KEY, replaceMqttProperty(
                    properties.getProperty(DroneAnalyzerConstants.BROKER_URL_PROPERTY_KEY)));
            mqttAdapterProperties.put(DroneAnalyzerConstants.SCOPES_PROPERTY_KEY, properties.getProperty(
                    DroneAnalyzerConstants.SCOPES_PROPERTY_KEY));
            mqttAdapterProperties.put(DroneAnalyzerConstants.CLEAR_SESSION_PROPERTY_KEY, properties.getProperty(
                    DroneAnalyzerConstants.CLEAR_SESSION_PROPERTY_KEY));
            mqttAdapterProperties.put(DroneAnalyzerConstants.QOS_PROPERTY_KEY, properties.getProperty(
                    DroneAnalyzerConstants.QOS_PROPERTY_KEY));
            mqttAdapterProperties.put(DroneAnalyzerConstants.CLIENT_ID_PROPERTY_KEY, "");
            mqttAdapterProperties.put(DroneAnalyzerConstants.RESOURCE, "output-event");
            outputEventAdapterConfiguration.setStaticProperties(mqttAdapterProperties);
        }
        return outputEventAdapterConfiguration;
    }
}
