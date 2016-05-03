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

package ${groupId}.${rootArtifactId}.api.util;

import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppConfig;
import org.wso2.carbon.device.mgt.iot.exception.IoTException;
import org.wso2.carbon.device.mgt.iot.util.IoTUtil;
import org.wso2.carbon.device.mgt.iot.util.IotDeviceManagementUtil;
import org.wso2.carbon.device.mgt.iot.util.ZipArchive;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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

    public ZipArchive createZipFile(String owner, String tenantDomain, String deviceType,
                                    String deviceId, String deviceName, String token,
                                    String refreshToken) throws DeviceManagementException {

        String sketchFolder = "repository" + File.separator + "resources" + File.separator + "sketches";
        String archivesPath = CarbonUtils.getCarbonHome() + File.separator + sketchFolder + File.separator + "archives" +
                        File.separator + deviceId;
        String templateSketchPath = sketchFolder + File.separator + deviceType;
        String iotServerIP;

        try {
            iotServerIP = IoTUtil.getHostName();
            String httpsServerPort = System.getProperty(HTTPS_PORT_PROPERTY);
            String httpServerPort = System.getProperty(HTTP_PORT_PROPERTY);
            String httpsServerEP = HTTPS_PROTOCOL_APPENDER + iotServerIP + ":" + httpsServerPort;
            String httpServerEP = HTTP_PROTOCOL_APPENDER + iotServerIP + ":" + httpServerPort;
            String apimEndpoint = httpsServerEP;
            String mqttEndpoint = MqttConfig.getInstance().getMqttQueueEndpoint();
            if (mqttEndpoint.contains(LOCALHOST)) {
                mqttEndpoint = mqttEndpoint.replace(LOCALHOST, iotServerIP);
            }
            String xmppEndpoint = XmppConfig.getInstance().getXmppEndpoint();
            int indexOfChar = xmppEndpoint.lastIndexOf(":");
            if (indexOfChar != -1) {
                xmppEndpoint = xmppEndpoint.substring(0, indexOfChar);
            }
            xmppEndpoint = xmppEndpoint + ":" + XmppConfig.getInstance().getSERVER_CONNECTION_PORT();

            Map<String, String> contextParams = new HashMap<>();
            contextParams.put("SERVER_NAME", "wso2/" + APIUtil.getTenantDomainOftheUser());
            contextParams.put("DEVICE_OWNER", owner);
            contextParams.put("DEVICE_ID", deviceId);
            contextParams.put("DEVICE_NAME", deviceName);
            contextParams.put("HTTPS_EP", httpsServerEP);
            contextParams.put("HTTP_EP", httpServerEP);
            contextParams.put("APIM_EP", apimEndpoint);
            contextParams.put("MQTT_EP", mqttEndpoint);
            contextParams.put("XMPP_EP", xmppEndpoint);
            contextParams.put("DEVICE_TOKEN", token);
            contextParams.put("DEVICE_REFRESH_TOKEN", refreshToken);

            ZipArchive zipFile;
            zipFile = IotDeviceManagementUtil.getSketchArchive(archivesPath, templateSketchPath, contextParams);
            return zipFile;
        } catch (IoTException e) {
            throw new DeviceManagementException(e.getMessage());
        } catch (IOException e) {
            throw new DeviceManagementException("Zip File Creation Failed", e);
        }
    }
}
