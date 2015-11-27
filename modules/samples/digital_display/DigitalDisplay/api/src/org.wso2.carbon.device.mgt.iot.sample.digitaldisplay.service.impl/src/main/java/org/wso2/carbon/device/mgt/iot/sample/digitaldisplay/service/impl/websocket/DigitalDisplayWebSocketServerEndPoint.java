package org.wso2.carbon.device.mgt.iot.sample.digitaldisplay.service.impl.websocket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.inject.Singleton;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.Map;


@ServerEndpoint(value = "/digital-display-server-end-point/{owner}")
@Singleton
public class DigitalDisplayWebSocketServerEndPoint {

    private static Log log = LogFactory.getLog(DigitalDisplayWebSocketServerEndPoint.class);

    private static Map<String,Session> clientSessions = new HashMap<>();

    /**
     * This method will be invoked when a client requests for a
     * WebSocket connection.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(@PathParam("owner") String owner , Session userSession ){
        log.info(owner + " Connected with Session Id : " + userSession.getId());

        clientSessions.put(owner,userSession);
    }

    /**
     * This method will be invoked when a client closes a WebSocket
     * connection.
     *
     * @param userSession the userSession which is opened.
     */
    @OnClose
    public void onClose(Session userSession){
        log.info("Client disconnected - Session Id : " + userSession.getId());
        clientSessions.values().remove(userSession);

    }

    @OnError
    public void onError(Throwable t){
        log.error("Error occurred " + t );
    }

    /**
     * This method will be invoked when a message received from device
     * to send client.
     *
     * @param deviceOwner the client of message to be sent.
     * @param message the message sent by device to client
     */
    public static void sendMessage(String deviceOwner , String message){
        if(clientSessions.keySet().contains(deviceOwner)){
            clientSessions.get(deviceOwner).getAsyncRemote().sendText(message);
            log.info("Message : " + message + " send to Session Id : " + deviceOwner);
        }else {
            log.error("Client already disconnected.");
        }
    }

}
