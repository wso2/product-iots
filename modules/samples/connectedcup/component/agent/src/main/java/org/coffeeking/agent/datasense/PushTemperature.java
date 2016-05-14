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

package org.coffeeking.agent.datasense;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.coffeeking.agent.transport.TransportHandlerException;
import org.coffeeking.agent.transport.mqtt.ConnectedCupMQttTransportHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class PushTemperature extends HttpServlet {
    private static final Log log = LogFactory.getLog(PushTemperature.class);
    private ConnectedCupMQttTransportHandler connectedCupMQttTransportHandler;

    public PushTemperature() {
        connectedCupMQttTransportHandler = ConnectedCupMQttTransportHandler.getInstance();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String deviceId = req.getParameter("deviceId");
        String tenantDomain = req.getParameter("tenantDomain");
        String payload = req.getParameter("payload");
        String deviceOwner = req.getParameter("deviceOwner");
        payload = " {\"event\": {\"metaData\": {\"owner\": \"" + deviceOwner +
                "\", \"type\": \"temperature\",\"deviceId\": " +
                "\"" + deviceId + "\",\"timestamp\": " + System.currentTimeMillis() +
                "},\"payloadData\": { \"temperature\": " + Float.parseFloat(payload) + ", \"coffeelevel\": 0} }}";
        String token = (String) req.getSession().getAttribute("token");
        if (!connectedCupMQttTransportHandler.isConnected()) {
            connectedCupMQttTransportHandler.setToken(token);
            connectedCupMQttTransportHandler.connect();
        }
        try {
            if (connectedCupMQttTransportHandler.isConnected()) {
                connectedCupMQttTransportHandler.publishToConnectedCup(deviceOwner, deviceId, payload, tenantDomain, 0,
                                                                       true);
            }
        } catch (TransportHandlerException e) {
            log.error(e);
            resp.sendError(500);
        }
    }

}
