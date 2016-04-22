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
package org.homeautomation.droneanalyzer.api;


import org.apache.commons.logging.LogFactory;
import org.homeautomation.droneanalyzer.api.transport.DroneAnalyzerXMPPConnector;
import org.homeautomation.droneanalyzer.api.trasformer.MessageTransformer;
import org.homeautomation.droneanalyzer.plugin.constants.DroneConstants;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppConfig;
import org.wso2.carbon.device.mgt.iot.service.IoTServerStartupListener;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ServerEndpoint("/datastream/drone_status")
public class DroneRealTimeService {

    private static org.apache.commons.logging.Log log = LogFactory.getLog(DroneRealTimeService.class);
    private MessageTransformer messageController;
    private DroneAnalyzerXMPPConnector xmppConnector;

    public DroneRealTimeService() {
        Runnable connector = new Runnable() {
            public void run() {
                if (waitForServerStartup()) {
                    return;
                }
                messageController = new MessageTransformer();
                xmppConnector = new DroneAnalyzerXMPPConnector(messageController);
                if (XmppConfig.getInstance().isEnabled()){
                    xmppConnector.connect();
                } else {
                    log.warn("XMPP disabled in 'devicemgt-config.xml'. Hence, DroneAnalyzerXMPPConnector not started.");
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

    @OnOpen
    public void onOpen(Session session){
        log.info(session.getId() + " has opened a connection");
        try {
            session.getBasicRemote().sendText("Connection Established");
        } catch (IOException e) {
            log.error( e.getMessage()+"\n"+ e);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session){
        while (true) {
            try {
                if (messageController != null && !messageController.isEmptyQueue()) {
                    String messageControllerMessage = messageController.getMessage();
                    session.getBasicRemote().sendText(messageControllerMessage);
                    break;
                }
                Thread.sleep(DroneConstants.MINIMUM_TIME_DURATION);
            } catch (IOException | InterruptedException ex) {
                log.error(ex.getMessage(), ex);
                break;
            }
        }
    }

    @OnClose
    public void onClose(Session session){
        try {
            xmppConnector.disconnect();
            log.info("XMPP connection is disconnected");
        }
        catch (Exception e) {
            log.error(e.getMessage() + "\n" + e);
        }
        log.info("Session " + session.getId() + " has ended");
    }

    @OnError
    public void onError(Session session, Throwable t) {
        try {
            session.getBasicRemote().sendText("Connection closed");
            xmppConnector.disconnect();
            log.info("XMPP connection is disconnected");
        } catch (Exception e) {
            log.error(e.getMessage()+"\n"+ e);
        }
        log.info("Session " + session.getId() + " has ended");
    }

}