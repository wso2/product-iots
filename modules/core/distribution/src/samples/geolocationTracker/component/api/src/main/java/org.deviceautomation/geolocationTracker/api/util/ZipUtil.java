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

package org.deviceautomation.geolocationTracker.api.util;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.application.extension.constants.ApiApplicationConstants;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationEntry;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationManagementException;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.iot.util.Utils;
import org.wso2.carbon.device.mgt.iot.util.ZipArchive;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is used to create a zip file that includes the necessary configuration required for the agent.
 */
public class ZipUtil {

    private static final String HTTPS_PORT_PROPERTY = "httpsPort";
    private static final String HTTP_PORT_PROPERTY = "httpPort";

    private static final String LOCALHOST = "localhost";
    private static final String HTTPS_PROTOCOL_APPENDER = "https://";
    private static final String HTTP_PROTOCOL_APPENDER = "http://";
    private static final String CONFIG_TYPE = "general";
    private static final String DEFAULT_MQTT_ENDPOINT = "tcp://localhost:1883";

    public ZipArchive createZipFile(String owner, String tenantDomain, String deviceType,
                                    String deviceId, String deviceName, String token,
                                    String refreshToken, String apiApplicationKey) throws DeviceManagementException {

        String sketchFolder = "repository" + File.separator + "resources" + File.separator + "sketches";
        String archivesPath = CarbonUtils.getCarbonHome() + File.separator + sketchFolder + File.separator + "archives" +
                File.separator + deviceId;
        String templateSketchPath = sketchFolder + File.separator + deviceType;
        String iotServerIP;

        try {
            iotServerIP = Utils.getServerUrl();
            String httpsServerPort = System.getProperty(HTTPS_PORT_PROPERTY);
            String httpServerPort = System.getProperty(HTTP_PORT_PROPERTY);
            String httpsServerEP = HTTPS_PROTOCOL_APPENDER + iotServerIP + ":" + httpsServerPort;
            String httpServerEP = HTTP_PROTOCOL_APPENDER + iotServerIP + ":" + httpServerPort;
            String mqttEndpoint = DEFAULT_MQTT_ENDPOINT;
            if (mqttEndpoint.contains(LOCALHOST)) {
                mqttEndpoint = mqttEndpoint.replace(LOCALHOST, iotServerIP);
            }

            PlatformConfiguration configuration = APIUtil.getTenantConfigurationManagementService().getConfiguration(
                    CONFIG_TYPE);
            if (configuration != null && configuration.getConfiguration() != null && configuration
                    .getConfiguration().size() > 0) {
                List<ConfigurationEntry> configurations = configuration.getConfiguration();
                for (ConfigurationEntry configurationEntry : configurations) {
                    switch (configurationEntry.getName()) {
                        case Constants.GEO_LOCATION_TRACKER_HTTPS_EP:
                            httpsServerEP = (String) configurationEntry.getValue();
                            break;
                        case Constants.GEO_LOCATION_TRACKER_HTTP_EP:
                            httpServerEP = (String) configurationEntry.getValue();
                            break;
                        case Constants.GEO_LOCATION_TRACKER_MQTT_EP:
                            mqttEndpoint = (String) configurationEntry.getValue();
                            break;
                        default:
                            break;
                    }
                }
            }
            String base64EncodedApplicationKey = getBase64EncodedAPIAppKey(apiApplicationKey).trim();
            Map<String, String> contextParams = new HashMap<>();
            contextParams.put(Constants.TENANT_DOMAIN, APIUtil.getTenantDomainOftheUser());
            contextParams.put(Constants.DEVICE_OWNER, owner);
            contextParams.put(Constants.DEVICE_ID, deviceId);
            contextParams.put(Constants.DEVICE_NAME, deviceName);
            contextParams.put(Constants.HTTPS_EP, httpsServerEP);
            contextParams.put(Constants.HTTP_EP, httpServerEP);
            contextParams.put(Constants.APIM_EP, httpServerEP);
            contextParams.put(Constants.MQTT_EP, mqttEndpoint);
            contextParams.put(Constants.API_APPLICATION_KEY, base64EncodedApplicationKey);
            contextParams.put(Constants.DEVICE_TOKEN, token);
            contextParams.put(Constants.DEVICE_REFRESH_TOKEN, refreshToken);
            ZipArchive zipFile;
            zipFile = Utils.getSketchArchive(archivesPath, templateSketchPath, contextParams, deviceName);
            return zipFile;
        } catch (IOException e) {
            throw new DeviceManagementException("Zip File Creation Failed", e);
        } catch (ConfigurationManagementException e) {
            throw new DeviceManagementException("Failed to retrieve configuration", e);
        }
    }

    private String getBase64EncodedAPIAppKey(String apiAppCredentialsAsJSONString) {
        JSONObject jsonObject = new JSONObject(apiAppCredentialsAsJSONString);
        String consumerKey = jsonObject.get(ApiApplicationConstants.OAUTH_CLIENT_ID).toString();
        String consumerSecret = jsonObject.get(ApiApplicationConstants.OAUTH_CLIENT_SECRET).toString();
        String stringToEncode = consumerKey + ":" + consumerSecret;
        return Base64.encodeBase64String(stringToEncode.getBytes());
    }
}
