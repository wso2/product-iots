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

package org.wso2.agent.datasense;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.agent.transport.TransportHandlerException;
import org.wso2.agent.transport.mqtt.ConnectedLapMQttTransportHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class PushData extends HttpServlet {
    private static final Log log = LogFactory.getLog(PushData.class);
    private ConnectedLapMQttTransportHandler connectedLapMQttTransportHandler;

    public PushData() {
        connectedLapMQttTransportHandler = ConnectedLapMQttTransportHandler.getInstance();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String deviceId = req.getParameter("deviceId");
        String tenantDomain = req.getParameter("tenantDomain");
        String deviceOwner = req.getParameter("deviceOwner");
        String batteryusage = req.getParameter("batteryusage");
        String chargerpluggedin = req.getParameter("chargerpluggedin");
        String cpuusage = req.getParameter("cpuusage");
        String networktraffic = req.getParameter("networktraffic");
        String memoryusage = req.getParameter("memoryusage");
        String harddiskusage = req.getParameter("harddiskusage");
        String token = req.getParameter("token");
        String payload = " {\"event\": {\"metaData\": {\"owner\": \"" + deviceOwner +
                "\", \"type\": \"connectedlap\",\"deviceId\": " +
                "\"" + deviceId + "\",\"timestamp\": " + System.currentTimeMillis() +
                "},\"payloadData\": { \"batteryusage\": " + Float.parseFloat(batteryusage) + ", \"chargerpluggedin\": " + Float.parseFloat(chargerpluggedin) + ", \"cpuusage\": " + Float.parseFloat(cpuusage) + ", \"networktraffic\": "+ Float.parseFloat(networktraffic) +",\"memoryusage\": " + Float.parseFloat(memoryusage) + ", \"harddiskusage\": "+ Float.parseFloat(harddiskusage) +"} }}";

        PrintWriter out = resp.getWriter();
        resp.setContentType("application/json; charset=UTF-8;");
        if (!connectedLapMQttTransportHandler.isConnected()) {
            connectedLapMQttTransportHandler.setToken(token);
            connectedLapMQttTransportHandler.connect();
        }
        try {
            if (connectedLapMQttTransportHandler.isConnected()) {
                connectedLapMQttTransportHandler.publishToConnectedLap(deviceOwner, deviceId, payload, tenantDomain, 0,
                        true);
                out.println("{\"success\": true }");
            }else {
                out.println("{\"success\": false }");
            }
        } catch (TransportHandlerException e) {
            log.error(e);
            resp.sendError(500);
            out.println("{\"success\": false }");
        }
    }
}
