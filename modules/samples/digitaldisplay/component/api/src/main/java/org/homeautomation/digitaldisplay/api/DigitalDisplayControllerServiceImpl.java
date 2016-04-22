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

package org.homeautomation.digitaldisplay.api;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.homeautomation.digitaldisplay.api.exception.DigitalDisplayException;
import org.homeautomation.digitaldisplay.api.util.DigitalDisplayMQTTConnector;
import org.homeautomation.digitaldisplay.plugin.constants.DigitalDisplayConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.service.IoTServerStartupListener;
import org.wso2.carbon.device.mgt.iot.transport.TransportHandlerException;

import javax.ws.rs.core.Response;

public class DigitalDisplayControllerServiceImpl implements DigitalDisplayControllerService {

    private static Log log = LogFactory.getLog(DigitalDisplayControllerServiceImpl.class);
    private static DigitalDisplayMQTTConnector digitalDisplayMQTTConnector;

    private boolean waitForServerStartup() {
        while (!IoTServerStartupListener.isServerReady()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return true;
            }
        }
        return false;
    }

    public DigitalDisplayMQTTConnector getDigitalDisplayMQTTConnector() {
        return DigitalDisplayControllerServiceImpl.digitalDisplayMQTTConnector;
    }

    public void setDigitalDisplayMQTTConnector(final
                                               DigitalDisplayMQTTConnector digitalDisplayMQTTConnector) {

        Runnable connector = new Runnable() {
            public void run() {
                if (waitForServerStartup()) {
                    return;
                }
                DigitalDisplayControllerServiceImpl.digitalDisplayMQTTConnector = digitalDisplayMQTTConnector;
                if (MqttConfig.getInstance().isEnabled()) {
                    digitalDisplayMQTTConnector.connect();
                } else {
                    log.warn("MQTT disabled in 'devicemgt-config.xml'. " +
                             "Hence, DigitalDisplayMQTTConnector not started.");
                }
            }
        };
        Thread connectorThread = new Thread(connector);
        connectorThread.setDaemon(true);
        connectorThread.start();
    }

    public Response restartBrowser(String deviceId, String sessionId) {
        try {
            sendCommandViaMQTT(deviceId, sessionId + "::" + DigitalDisplayConstants.RESTART_BROWSER_CONSTANT + "::", "");
            return Response.ok().build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
        } catch (DigitalDisplayException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    public Response terminateDisplay(String deviceId, String sessionId) {
        try {
            sendCommandViaMQTT(deviceId, sessionId + "::" + DigitalDisplayConstants.TERMINATE_DISPLAY_CONSTANT + "::", "");
            return Response.ok().build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
        } catch (DigitalDisplayException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }

    }

    public Response restartDisplay(String deviceId, String sessionId) {
        try {
            sendCommandViaMQTT(deviceId, sessionId + "::" + DigitalDisplayConstants.RESTART_DISPLAY_CONSTANT + "::", "");
            return Response.ok().build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
        } catch (DigitalDisplayException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    public Response editSequence(String deviceId, String name, String attribute, String newValue, String sessionId) {
        try {
            String params = name + "|" + attribute + "|" + newValue;
            sendCommandViaMQTT(deviceId, sessionId + "::" + DigitalDisplayConstants.EDIT_SEQUENCE_CONSTANT + "::", params);
            return Response.ok().build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
        } catch (DigitalDisplayException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    public Response uploadContent(String deviceId, String remotePath, String screenName, String sessionId) {
        try {
            String params = remotePath + "|" + screenName;
            sendCommandViaMQTT(deviceId, sessionId + "::" + DigitalDisplayConstants.UPLOAD_CONTENT_CONSTANT + "::",
                               params);
            return Response.ok().build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
        } catch (DigitalDisplayException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    public Response addNewResource(String deviceId, String type, String time, String path, String name, String position,
                                   String sessionId) {
        String params;
        try {
            if (position.isEmpty()) {
                params = type + "|" + time + "|" + path + "|" + name;
            } else {
                params = type + "|" + time + "|" + path + "|" + name +
                         "|" + "after=" + position;
            }
            sendCommandViaMQTT(deviceId, sessionId + "::" + DigitalDisplayConstants.ADD_NEW_RESOURCE_CONSTANT + "::", params);
            return Response.ok().build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
        } catch (DigitalDisplayException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    public Response removeResource(String deviceId, String name, String sessionId) {
        try {
            sendCommandViaMQTT(deviceId, sessionId + "::" + DigitalDisplayConstants.REMOVE_RESOURCE_CONSTANT + "::", name);
            return Response.ok().build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
        } catch (DigitalDisplayException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    public Response restartServer(String deviceId, String sessionId) {
        try {
            sendCommandViaMQTT(deviceId, sessionId + "::" + DigitalDisplayConstants.RESTART_SERVER_CONSTANT + "::", "");
            return Response.ok().build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
        } catch (DigitalDisplayException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    public Response showScreenshot(String deviceId, String sessionId) {
        try {
            sendCommandViaMQTT(deviceId, sessionId + "::" + DigitalDisplayConstants.SCREENSHOT_CONSTANT + "::", "");
            return Response.ok().build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
        } catch (DigitalDisplayException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    public Response getDevicestatus(String deviceId, String sessionId) {
        try {
            sendCommandViaMQTT(deviceId, sessionId + "::" + DigitalDisplayConstants.GET_DEVICE_STATUS_CONSTANT + "::", "");
            return Response.ok().build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
        } catch (DigitalDisplayException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    public Response getResources(String deviceId, String sessionId) {
        try {
            sendCommandViaMQTT(deviceId, sessionId + "::" + DigitalDisplayConstants.GET_CONTENTLIST_CONSTANT + "::", "");
            return Response.ok().build();
        } catch (DeviceManagementException e) {
            return Response.status(Response.Status.UNAUTHORIZED.getStatusCode()).build();
        } catch (DigitalDisplayException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()).build();
        }
    }

    /**
     * Send message via MQTT protocol
     *
     * @param deviceId  id of the target digital display
     * @param operation operation need to execute
     * @param param     parameters need to given operation
     * @throws DeviceManagementException
     * @throws DigitalDisplayException
     */
    private void sendCommandViaMQTT(String deviceId, String operation, String param)
            throws DeviceManagementException, DigitalDisplayException {
        String topic = String.format(DigitalDisplayConstants.PUBLISH_TOPIC, deviceId);
        String payload = operation + param;
        try {
            digitalDisplayMQTTConnector.publishToDigitalDisplay(topic, payload, 2, false);
        } catch (TransportHandlerException e) {
            String errorMessage = "Error publishing data to device with ID " + deviceId;
            throw new DigitalDisplayException(errorMessage, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

}
