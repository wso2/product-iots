/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import oauth.OAuthTokenValdiator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import util.ServiceHolder;
import util.AuthenticationInfo;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * Connect to web socket with a tenant
 */

@ServerEndpoint(value = "/t/{tdomain}/{streamname}/{version}")
public class TenantSubscriptionEndpoint extends SubscriptionEndpoint {

    private static final Log log = LogFactory.getLog(TenantSubscriptionEndpoint.class);

    /**
     * Web socket onOpen - When client sends a message
     *
     * @param session - Users registered session.
     * @param streamName - StreamName extracted from the ws url.
     * @param version -  Version extracted from the ws url.
     * @param tdomain - Tenant domain extracted from ws url.
     */
    @OnOpen
    public void onOpen (Session session, @PathParam("streamname") String streamName ,
            @PathParam("version") String version, @PathParam("tdomain") String tdomain) {
        if (log.isDebugEnabled()) {
            log.debug("WebSocket opened, for Session id: "+session.getId()+", for the Stream:"+streamName);
        }
        AuthenticationInfo authenticationInfo = OAuthTokenValdiator.getInstance().validateToken(session);
        //TODO Authorization
        if (authenticationInfo != null && authenticationInfo.isAuthenticated()) {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tdomain, true);
                ServiceHolder.getInstance().getUiOutputCallbackControllerService().subscribeWebsocket(streamName,
                                                                                                      version, session);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        } else {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Unauthorized Access"));
            } catch (IOException e) {
                log.error("Failed to disconnect the unauthorized client.");
            }
        }
    }

    /**
     * Web socket onMessage - When client sens a message
     *
     * @param session - Users registered session.
     * @param message  - Status code for web-socket close.
     * @param streamName - StreamName extracted from the ws url.
     */
    @OnMessage
    public void onMessage (Session session, String message, @PathParam("streamname") String streamName, @PathParam("tdomain") String tdomain) {
        if (log.isDebugEnabled()) {
            log.debug("Received and dropped message from client. Message: " + message+", for Session id: "+session.getId()+", for tenant domain"+tdomain+", for the Adaptor:"+streamName);
        }
    }

    /**
     * Web socket onClose - Remove the registered sessions
     *
     * @param session - Users registered session.
     * @param reason  - Status code for web-socket close.
     * @param streamName - StreamName extracted from the ws url.
     * @param version - Version extracted from the ws url.
     */
    @OnClose
    public void onClose (Session session, CloseReason reason, @PathParam("streamname") String streamName,
            @PathParam("version") String version, @PathParam("tdomain") String tdomain) {

        try {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tdomain,true);
            super.onClose(session, reason, streamName, version);
        } finally {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().endTenantFlow();
        }
    }

    /**
     * Web socket onError - Remove the registered sessions
     *
     * @param session - Users registered session.
     * @param throwable  - Status code for web-socket close.
     * @param streamName - StreamName extracted from the ws url.
     * @param version - Version extracted from the ws url.
     */
    @OnError
    public void onError (Session session, Throwable throwable, @PathParam("streamname") String streamName,
            @PathParam("version") String version, @PathParam("tdomain") String tdomain) {

        try {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tdomain, true);
            super.onError(session, throwable, streamName, version);
        } finally {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().endTenantFlow();
        }
    }
}
