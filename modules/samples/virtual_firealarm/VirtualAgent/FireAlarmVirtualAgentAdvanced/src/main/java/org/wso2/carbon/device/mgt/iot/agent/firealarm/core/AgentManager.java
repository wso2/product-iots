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
import org.wso2.carbon.device.mgt.iot.agent.firealarm.communication.CommunicationHandler;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.communication.CommunicationHandlerException;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.communication.CommunicationUtils;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.sidhdhi.SidhdhiQuery;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.utils.http.HTTPCommunicationHandlerImpl;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.utils.mqtt.MQTTCommunicationHandlerImpl;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.utils.xmpp.XMPPCommunicationHandlerImpl;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.virtual.VirtualHardwareManager;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgentManager {

    private static final Log log = LogFactory.getLog(AgentManager.class);
    private static final Object lock = new Object();
    private static AgentManager agentManager;
    private static Boolean policyUpdated = false;
    private String rootPath = "";
    private boolean deviceReady = false;
    private boolean isAlarmOn = false;
    private String initialPolicy;

    private String deviceMgtControlUrl, deviceMgtAnalyticUrl;
    private String deviceName, agentStatus;

    private int pushInterval;               // seconds
    private String prevProtocol, protocol;

    private String networkInterface;
    private List<String> interfaceList, protocolList;
    private Map<String, CommunicationHandler> agentCommunicator;

    private AgentConfiguration agentConfigs;

    private String deviceIP;
    private String ipRegistrationEP;
    private String pushDataAPIEP;

    private AgentManager() {
    }

    public static synchronized AgentManager getInstance() {
        if (agentManager == null) {
            agentManager = new AgentManager();
        }
        return agentManager;
    }

    public static void setUpdated(Boolean isUpdated) {
        synchronized (lock) {
            policyUpdated = isUpdated;
        }
    }

    public static Boolean isUpdated() {
        synchronized (lock) {
            Boolean temp = policyUpdated;
            policyUpdated = false;
            return temp;
        }
    }

    public void init() {

        agentCommunicator = new HashMap<>();

        // Read IoT-Server specific configurations from the 'deviceConfig.properties' file
        this.agentConfigs = AgentUtilOperations.readIoTServerConfigs();

        // Initialise IoT-Server URL endpoints from the configuration read from file
        AgentUtilOperations.initializeHTTPEndPoints();

        String analyticsPageContext = String.format(AgentConstants.DEVICE_ANALYTICS_PAGE_URL,
                                                    agentConfigs.getDeviceId(),
                                                    AgentConstants.DEVICE_TYPE);

        String controlPageContext = String.format(AgentConstants.DEVICE_DETAILS_PAGE_EP,
                                                  AgentConstants.DEVICE_TYPE,
                                                  agentConfigs.getDeviceId());

        this.deviceMgtAnalyticUrl = agentConfigs.getHTTPS_ServerEndpoint() + analyticsPageContext;
        this.deviceMgtControlUrl = agentConfigs.getHTTPS_ServerEndpoint() + controlPageContext;

        this.agentStatus = AgentConstants.NOT_REGISTERED;
        this.deviceName = this.agentConfigs.getDeviceName();

        this.pushInterval = this.agentConfigs.getDataPushInterval();
        this.networkInterface = AgentConstants.DEFAULT_NETWORK_INTERFACE;

        this.protocol = AgentConstants.DEFAULT_PROTOCOL;
        this.prevProtocol = protocol;

        Map<String, String> xmppIPPortMap;
        try {
            xmppIPPortMap = CommunicationUtils.getHostAndPort(agentConfigs.getXmppServerEndpoint
                    ());

            String xmppServer = xmppIPPortMap.get("Host");
            int xmppPort = Integer.parseInt(xmppIPPortMap.get("Port"));
            CommunicationHandler xmppCommunicator = new XMPPCommunicationHandlerImpl(xmppServer,
                                                                                     xmppPort);
            agentCommunicator.put(AgentConstants.XMPP_PROTOCOL, xmppCommunicator);

        } catch (CommunicationHandlerException e) {
            log.error("XMPP Endpoint String - " + agentConfigs.getXmppServerEndpoint() +
                      ", provided in the configuration file is invalid.");
        }
        String mqttTopic = String.format(AgentConstants.MQTT_SUBSCRIBE_TOPIC,
                                         agentConfigs.getDeviceOwner(),
                                         agentConfigs.getDeviceId());

        CommunicationHandler httpCommunicator = new HTTPCommunicationHandlerImpl();
        CommunicationHandler mqttCommunicator = new MQTTCommunicationHandlerImpl(
                agentConfigs.getDeviceOwner(), agentConfigs.getDeviceId(),
                agentConfigs.getMqttBrokerEndpoint(), mqttTopic);

        agentCommunicator.put(AgentConstants.HTTP_PROTOCOL, httpCommunicator);
        agentCommunicator.put(AgentConstants.MQTT_PROTOCOL, mqttCommunicator);

        try {
            interfaceList = new ArrayList<>(CommunicationUtils.getInterfaceIPMap().keySet());
            protocolList = new ArrayList<>(agentCommunicator.keySet());
        } catch (CommunicationHandlerException e) {
            log.error("An error occurred whilst retrieving all NetworkInterface-IP mappings");
        }

        String siddhiQueryFilePath = rootPath + AgentConstants.CEP_FILE_NAME;
        initialPolicy = SidhdhiQuery.readFile(siddhiQueryFilePath, StandardCharsets.UTF_8);
        (new Thread(new SidhdhiQuery())).start();

        //Initializing hardware at that point
        //AgentManger.setDeviceReady() method should invoked from hardware after initialization
        VirtualHardwareManager.getInstance().init();

        //Wait till hardware get ready
        while (!deviceReady) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.info(AgentConstants.LOG_APPENDER + "Sleep error in 'device ready-flag' checking thread");
            }
        }

        //Start agent communication
        agentCommunicator.get(protocol).connect();
    }

    private void switchCommunicator(String stopProtocol, String startProtocol) {
        agentCommunicator.get(stopProtocol).disconnect();

        while (agentCommunicator.get(stopProtocol).isConnected()) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                log.info(AgentConstants.LOG_APPENDER +
                         "Sleep error in 'Switch-Communicator' Thread's shutdown wait.");
            }
        }

        agentCommunicator.get(startProtocol).connect();
    }

    public void setInterface(int interfaceId) {
        if (interfaceId != -1) {
            String newInterface = interfaceList.get(interfaceId);

            if (!newInterface.equals(networkInterface)) {
                networkInterface = newInterface;

                if (protocol.equals(AgentConstants.HTTP_PROTOCOL) && !protocol.equals(
                        prevProtocol)) {
                    switchCommunicator(protocol, protocol);
                }
            }
        }
    }

    public void setProtocol(int protocolId) {
        if (protocolId != -1) {
            String newProtocol = protocolList.get(protocolId);

            if (!protocol.equals(newProtocol)) {
                prevProtocol = protocol;
                protocol = newProtocol;
                switchCommunicator(prevProtocol, protocol);
            }
        }
    }

    public void changeAlarmStatus(boolean isOn) {
        VirtualHardwareManager.getInstance().changeAlarmStatus(isOn);
        isAlarmOn = isOn;
    }

    public void updateAgentStatus(String status) {
        this.agentStatus = status;
    }

    public void addToPolicyLog(String policy) {
        VirtualHardwareManager.getInstance().addToPolicyLog(policy);
    }

    public String getRootPath() {
        return rootPath;
    }

	/*------------------------------------------------------------------------------------------*/
    /* 		            Getter and Setter Methods for the private variables                 	*/
	/*------------------------------------------------------------------------------------------*/

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public void setDeviceReady(boolean deviceReady) {
        this.deviceReady = deviceReady;
    }

    public String getInitialPolicy() {
        return initialPolicy;
    }

    public String getDeviceMgtControlUrl() {
        return deviceMgtControlUrl;
    }

    public String getDeviceMgtAnalyticUrl() {
        return deviceMgtAnalyticUrl;
    }

    public AgentConfiguration getAgentConfigs() {
        return agentConfigs;
    }

    public String getDeviceIP() {
        return deviceIP;
    }

    public void setDeviceIP(String deviceIP) {
        this.deviceIP = deviceIP;
    }

    public String getIpRegistrationEP() {
        return ipRegistrationEP;
    }

    public void setIpRegistrationEP(String ipRegistrationEP) {
        this.ipRegistrationEP = ipRegistrationEP;
    }

    public String getPushDataAPIEP() {
        return pushDataAPIEP;
    }

    public void setPushDataAPIEP(String pushDataAPIEP) {
        this.pushDataAPIEP = pushDataAPIEP;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getNetworkInterface() {
        return networkInterface;
    }

    public String getAgentStatus() {
        return agentStatus;
    }

    public int getPushInterval() {
        return pushInterval;
    }

    public void setPushInterval(int pushInterval) {
        this.pushInterval = pushInterval;
        CommunicationHandler communicationHandler = agentCommunicator.get(protocol);

        switch (protocol) {
            case AgentConstants.HTTP_PROTOCOL:
                ((HTTPCommunicationHandlerImpl) communicationHandler).getDataPushServiceHandler()
                        .cancel(true);
                break;
            case AgentConstants.MQTT_PROTOCOL:
                ((MQTTCommunicationHandlerImpl) communicationHandler).getDataPushServiceHandler()
                        .cancel(true);
                break;
            case AgentConstants.XMPP_PROTOCOL:
                ((XMPPCommunicationHandlerImpl) communicationHandler).getDataPushServiceHandler()
                        .cancel(true);
                break;
            default:
                log.warn("Unknown protocol " + protocol);
        }
        communicationHandler.publishDeviceData(pushInterval);

        if (log.isDebugEnabled()) {
            log.debug("The Data Publish Interval was changed to: " + pushInterval);
        }
    }

    public List<String> getInterfaceList() {
        return interfaceList;
    }

    public List<String> getProtocolList() {
        return protocolList;
    }

    /**
     * Get temperature reading from device
     * @return Temperature
     */
    public int getTemperature() {
        return VirtualHardwareManager.getInstance().getTemperature();
    }

    /**
     * Get humidity reading from device
     * @return Humidity
     */
    public int getHumidity(){
        return VirtualHardwareManager.getInstance().getHumidity();
    }

    public boolean isAlarmOn() {
        return isAlarmOn;
    }
}
