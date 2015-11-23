package org.wso2.carbon.device.mgt.iot.sample.digitaldisplay.service.impl.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.inject.Singleton;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nuwan on 11/23/15.
 */

@ServerEndpoint(value = "/digital-display-server-end-point")
@Singleton
public class DigitalDisplayWebSocketServerEndPoint {

    private static Log log = LogFactory.getLog(DigitalDisplayWebSocketServerEndPoint.class);

    private static Map<String,Session> clientSessions = new HashMap<>();

    @OnOpen
    public void onOpen(Session userSession){
        log.info("Client Connected - Session Id : " + userSession.getId());
        clientSessions.put(userSession.getId(),userSession);
    }

    @OnClose
    public void onClose(Session userSession){
        log.info("Client disconnected - Session Id : " + userSession.getId());
        clientSessions.remove(userSession.getId());
    }

    public static void sendMessage(String userSessionId , String message){
        if(clientSessions.keySet().contains(userSessionId)){
            clientSessions.get(userSessionId).getAsyncRemote().sendText(message);
            log.info("Message : " + message + " send to Session Id : " + userSessionId);
        }else {
            log.error("Client already disconnected.");
        }
    }

}
