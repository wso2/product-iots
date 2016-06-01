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

package org.homeautomation.firealarm.plugin.mqtt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.homeautomation.firealarm.plugin.constants.DeviceTypeConstants;
import org.homeautomation.firealarm.plugin.impl.util.DeviceTypeUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MqttConfig {

    private static String brokerEndpoint;

    private static MqttConfig mqttConfig = new MqttConfig();
    private static final Log log = LogFactory.getLog(MqttConfig.class);

    private MqttConfig() {
        File configFile = new File(DeviceTypeConstants.MQTT_CONFIG_LOCATION);
        if (configFile.exists()) {
            try {
                InputStream propertyStream = configFile.toURI().toURL().openStream();
                Properties properties = new Properties();
                properties.load(propertyStream);
                brokerEndpoint = DeviceTypeUtils.replaceMqttProperty(
                        properties.getProperty(DeviceTypeConstants.BROKER_URL_PROPERTY_KEY));
            } catch (IOException e) {
                log.error("Failed to read the mqtt.properties file" + e);
            }
        }
    }

    public static MqttConfig getInstance() {
        return mqttConfig;
    }

    public String getBrokerEndpoint() {
        return brokerEndpoint;
    }
}
