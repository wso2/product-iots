/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.device.mgt.iot.agent.firealarm.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.exception.AgentCoreOperationException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.Clip;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

/**
 * This class contains all the core operations of the FireAlarm agent that are common to both
 * Virtual and Real Scenarios. These operations include, connecting to and subscribing to an MQTT
 * queue and to a XMPP Server. Pushing temperature data to the IoT-Server at timely intervals.
 * Reading device specific configuration from a configs file etc....
 */
public class AgentUtilOperations {

    private static final Log log = LogFactory.getLog(AgentUtilOperations.class);

    /**
     * This method reads the agent specific configurations for the device from the
     * "deviceConfigs.properties" file found at /repository/conf folder.
     * If the properties file is not found in the specified path, then the configuration values
     * are set to the default ones in the 'AgentConstants' class.
     *
     * @return an object of type 'AgentConfiguration' which contains all the necessary
     * configuration attributes
     */
    public static AgentConfiguration readIoTServerConfigs() {
        AgentManager agentManager = AgentManager.getInstance();
        AgentConfiguration iotServerConfigs = new AgentConfiguration();
        Properties properties = new Properties();
        InputStream propertiesInputStream = null;
        String propertiesFileName = AgentConstants.AGENT_PROPERTIES_FILE_NAME;

        try {
            ClassLoader loader = AgentUtilOperations.class.getClassLoader();
            URL path = loader.getResource(propertiesFileName);
            System.out.println(path);
            String root = path.getPath().replace(
                    "wso2-firealarm-virtual-agent-advanced.jar!/deviceConfig.properties",
                    "").replace("jar:", "").replace("file:", "");

            agentManager.setRootPath(root);

            propertiesInputStream = new FileInputStream(
                    root + AgentConstants.AGENT_PROPERTIES_FILE_NAME);

            //load a properties file from class path, inside static method
            properties.load(propertiesInputStream);

            iotServerConfigs.setDeviceOwner(properties.getProperty(
                    AgentConstants.DEVICE_OWNER_PROPERTY));
            iotServerConfigs.setDeviceId(properties.getProperty(
                    AgentConstants.DEVICE_ID_PROPERTY));
            iotServerConfigs.setDeviceName(properties.getProperty(
                    AgentConstants.DEVICE_NAME_PROPERTY));
            iotServerConfigs.setControllerContext(properties.getProperty(
                    AgentConstants.DEVICE_CONTROLLER_CONTEXT_PROPERTY));
            iotServerConfigs.setHTTPS_ServerEndpoint(properties.getProperty(
                    AgentConstants.SERVER_HTTPS_EP_PROPERTY));
            iotServerConfigs.setHTTP_ServerEndpoint(properties.getProperty(
                    AgentConstants.SERVER_HTTP_EP_PROPERTY));
            iotServerConfigs.setApimGatewayEndpoint(properties.getProperty(
                    AgentConstants.APIM_GATEWAY_EP_PROPERTY));
            iotServerConfigs.setMqttBrokerEndpoint(properties.getProperty(
                    AgentConstants.MQTT_BROKER_EP_PROPERTY));
            iotServerConfigs.setXmppServerEndpoint(properties.getProperty(
                    AgentConstants.XMPP_SERVER_EP_PROPERTY));
            iotServerConfigs.setAuthMethod(properties.getProperty(
                    AgentConstants.AUTH_METHOD_PROPERTY));
            iotServerConfigs.setAuthToken(properties.getProperty(
                    AgentConstants.AUTH_TOKEN_PROPERTY));
            iotServerConfigs.setRefreshToken(properties.getProperty(
                    AgentConstants.REFRESH_TOKEN_PROPERTY));
            iotServerConfigs.setDataPushInterval(Integer.parseInt(properties.getProperty(
                    AgentConstants.PUSH_INTERVAL_PROPERTY)));

            log.info(AgentConstants.LOG_APPENDER + "Device Owner: " +
                             iotServerConfigs.getDeviceOwner());
            log.info(AgentConstants.LOG_APPENDER + "Device ID: " + iotServerConfigs.getDeviceId());
            log.info(AgentConstants.LOG_APPENDER + "Device Name: " +
                             iotServerConfigs.getDeviceName());
            log.info(AgentConstants.LOG_APPENDER + "Device Controller Context: " +
                             iotServerConfigs.getControllerContext());
            log.info(AgentConstants.LOG_APPENDER + "IoT Server HTTPS EndPoint: " +
                             iotServerConfigs.getHTTPS_ServerEndpoint());
            log.info(AgentConstants.LOG_APPENDER + "IoT Server HTTP EndPoint: " +
                             iotServerConfigs.getHTTP_ServerEndpoint());
            log.info(AgentConstants.LOG_APPENDER + "API-Manager Gateway EndPoint: " +
                             iotServerConfigs.getApimGatewayEndpoint());
            log.info(AgentConstants.LOG_APPENDER + "MQTT Broker EndPoint: " +
                             iotServerConfigs.getMqttBrokerEndpoint());
            log.info(AgentConstants.LOG_APPENDER + "XMPP Server EndPoint: " +
                             iotServerConfigs.getXmppServerEndpoint());
            log.info(AgentConstants.LOG_APPENDER + "Authentication Method: " +
                             iotServerConfigs.getAuthMethod());
            log.info(AgentConstants.LOG_APPENDER + "Authentication Token: " +
                             iotServerConfigs.getAuthToken());
            log.info(AgentConstants.LOG_APPENDER + "Refresh Token: " +
                             iotServerConfigs.getRefreshToken());
            log.info(AgentConstants.LOG_APPENDER + "Data Push Interval: " +
                             iotServerConfigs.getDataPushInterval());

        } catch (FileNotFoundException ex) {
            log.error(AgentConstants.LOG_APPENDER + "Unable to find " + propertiesFileName +
                              " file at: " + AgentConstants.PROPERTIES_FILE_PATH);
            iotServerConfigs = setDefaultDeviceConfigs();

        } catch (IOException ex) {
            log.error(AgentConstants.LOG_APPENDER + "Error occurred whilst trying to fetch '" +
                              propertiesFileName + "' from: " +
                              AgentConstants.PROPERTIES_FILE_PATH);
            iotServerConfigs = setDefaultDeviceConfigs();

        } finally {
            if (propertiesInputStream != null) {
                try {
                    propertiesInputStream.close();
                } catch (IOException e) {
                    log.error(AgentConstants.LOG_APPENDER +
                                      "Error occurred whilst trying to close InputStream " +
                                      "resource used to read the '" + propertiesFileName +
                                      "' file");
                }
            }
        }
        return iotServerConfigs;
    }

    /**
     * Sets the default Device specific configurations listed in the 'AgentConstants' class.
     *
     * @return an object of AgentConfiguration class including all default device specific configs.
     */
    private static AgentConfiguration setDefaultDeviceConfigs() {
        log.warn(AgentConstants.LOG_APPENDER +
                         "Default Values are being set to all Agent specific configurations");

        AgentConfiguration iotServerConfigs = new AgentConfiguration();

        iotServerConfigs.setDeviceOwner(AgentConstants.DEFAULT_DEVICE_OWNER);
        iotServerConfigs.setDeviceId(AgentConstants.DEFAULT_DEVICE_ID);
        iotServerConfigs.setDeviceName(AgentConstants.DEFAULT_DEVICE_NAME);
        iotServerConfigs.setControllerContext(AgentConstants.DEVICE_CONTROLLER_API_EP);
        iotServerConfigs.setHTTPS_ServerEndpoint(AgentConstants.DEFAULT_HTTPS_SERVER_EP);
        iotServerConfigs.setHTTP_ServerEndpoint(AgentConstants.DEFAULT_HTTP_SERVER_EP);
        iotServerConfigs.setApimGatewayEndpoint(AgentConstants.DEFAULT_APIM_GATEWAY_EP);
        iotServerConfigs.setMqttBrokerEndpoint(AgentConstants.DEFAULT_MQTT_BROKER_EP);
        iotServerConfigs.setXmppServerEndpoint(AgentConstants.DEFAULT_XMPP_SERVER_EP);
        iotServerConfigs.setAuthMethod(AgentConstants.DEFAULT_AUTH_METHOD);
        iotServerConfigs.setAuthToken(AgentConstants.DEFAULT_AUTH_TOKEN);
        iotServerConfigs.setRefreshToken(AgentConstants.DEFAULT_REFRESH_TOKEN);
        iotServerConfigs.setDataPushInterval(AgentConstants.DEFAULT_DATA_PUBLISH_INTERVAL);

        return iotServerConfigs;
    }


    /**
     * This method constructs the URLs for each of the API Endpoints called by the device agent
     * Ex: Register API, Push-Data API
     *
     * @throws AgentCoreOperationException if any error occurs at socket level whilst trying to
     *                                     retrieve the deviceIP of the network-interface read
     *                                     from the configs file
     */
    public static void initializeHTTPEndPoints() {
        AgentManager agentManager = AgentManager.getInstance();
        String apimEndpoint = agentManager.getAgentConfigs().getHTTP_ServerEndpoint();
        String backEndContext = agentManager.getAgentConfigs().getControllerContext();

        String deviceControllerAPIEndpoint = apimEndpoint + backEndContext;

        String registerEndpointURL =
                deviceControllerAPIEndpoint + AgentConstants.DEVICE_REGISTER_API_EP;
        agentManager.setIpRegistrationEP(registerEndpointURL);

        String pushDataEndPointURL =
                deviceControllerAPIEndpoint + AgentConstants.DEVICE_PUSH_TEMPERATURE_API_EP;
        agentManager.setPushDataAPIEP(pushDataEndPointURL);

        log.info(AgentConstants.LOG_APPENDER + "IoT Server's Device Controller API Endpoint: " +
                         deviceControllerAPIEndpoint);
        log.info(AgentConstants.LOG_APPENDER + "DeviceIP Registration EndPoint: " +
                         registerEndpointURL);
        log.info(AgentConstants.LOG_APPENDER + "Push-Data API EndPoint: " + pushDataEndPointURL);
    }


    public static String formatMessage(String message) {
        StringBuilder formattedMsg = new StringBuilder(message);

        ArrayList<String> keyWordList = new ArrayList<String>();
        keyWordList.add("define");
        keyWordList.add("from");
        keyWordList.add("select");
        keyWordList.add("group");
        keyWordList.add("insert");
        keyWordList.add(";");


        for (String keyWord : keyWordList) {
            int startIndex = 0;

            while (true) {
                int keyWordIndex = formattedMsg.indexOf(keyWord, startIndex);

                if (keyWordIndex == -1) {
                    break;
                }

                if (keyWord.equals(";")) {
                    if (keyWordIndex != 0 && (keyWordIndex + 1) != formattedMsg.length() &&
                            formattedMsg.charAt(keyWordIndex + 1) == ' ') {
                        formattedMsg.setCharAt((keyWordIndex + 1), '\n');
                    }
                } else {
                    if (keyWordIndex != 0 && formattedMsg.charAt(keyWordIndex - 1) == ' ') {
                        formattedMsg.setCharAt((keyWordIndex - 1), '\n');
                    }
                }
                startIndex = keyWordIndex + 1;
            }
        }
        return formattedMsg.toString();
    }

}

