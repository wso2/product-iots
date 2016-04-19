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

package org.coffeeking.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.coffeeking.connectedcup.plugin.constants.ConnectedCupConstants;
import org.coffeeking.api.transport.ConnectedCupMQTTConnector;
import org.wso2.carbon.device.mgt.iot.controlqueue.mqtt.MqttConfig;
import org.wso2.carbon.device.mgt.iot.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.service.IoTServerStartupListener;
import org.wso2.carbon.device.mgt.iot.transport.TransportHandlerException;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ConnectedCupControllerServiceImpl implements ConnectedCupControllerService {

    private static Log log = LogFactory.getLog(ConnectedCupControllerServiceImpl.class);
    private static ConnectedCupMQTTConnector connectedCupMQTTConnector;

    @Path("device/ordercoffee")
    @POST
    public Response orderCoffee(@QueryParam("deviceId") String deviceId, @QueryParam("deviceOwner") String deviceOwner) {
        log.info("Coffee ordered....!");

        if (log.isDebugEnabled()) {
            log.debug("Sending request to read liquid level value of device [" + deviceId + "] via MQTT");
        }
        return Response.ok().entity("Coffee ordered.").build();
    }

    public ConnectedCupMQTTConnector getConnectedCupMQTTConnector() {
        return ConnectedCupControllerServiceImpl.connectedCupMQTTConnector;
    }

    public void setConnectedCupMQTTConnector(
            final ConnectedCupMQTTConnector connectedCupMQTTConnector) {

        Runnable connector = new Runnable() {
            public void run() {
                if (waitForServerStartup()) {
                    return;
                }
                ConnectedCupControllerServiceImpl.connectedCupMQTTConnector = connectedCupMQTTConnector;
                if (MqttConfig.getInstance().isEnabled()) {
                    connectedCupMQTTConnector.connect();
                } else {
                    log.warn("MQTT disabled in 'devicemgt-config.xml'. " +
                             "Hence, ConnectedCupMQTTConnector not started.");
                }
            }
        };
        Thread connectorThread = new Thread(connector);
        connectorThread.setDaemon(true);
        connectorThread.start();
    }

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

}
