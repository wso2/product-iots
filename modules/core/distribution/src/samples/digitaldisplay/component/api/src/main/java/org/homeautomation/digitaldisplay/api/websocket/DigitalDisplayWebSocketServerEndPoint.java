package org.homeautomation.digitaldisplay.api.websocket;

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

@ServerEndpoint(value = "/{sessionId}")
@Singleton
public class DigitalDisplayWebSocketServerEndPoint {

    private static Log log = LogFactory.getLog(DigitalDisplayWebSocketServerEndPoint.class);
    private static Map<String, Session> clientSessions = new HashMap<>();

    /**
     * This method will be invoked when a client requests for a
     * WebSocket connection.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession, @PathParam("sessionId") String sessionId) {
        clientSessions.put(sessionId, userSession);
    }

    /**
     * This method will be invoked when a client closes a WebSocket
     * connection.
     *
     * @param userSession the userSession which is opened.
     */
    @OnClose
    public void onClose(Session userSession) {
        clientSessions.values().remove(userSession);

    }

    @OnError
    public void onError(Throwable t) {
        log.error("Error occurred " + t);
    }

    /**
     * This method will be invoked when a message received from device
     * to send client.
     *
     * @param sessionId   the client of message to be sent.
     * @param message the message sent by device to client
     */
    public static void sendMessage(String sessionId, StringBuilder message) {
        Session session = clientSessions.get(sessionId);
        if (session != null) {
            session.getAsyncRemote().sendText(message.toString());
        } else {
            log.error("Client already disconnected.");
        }
    }

}
