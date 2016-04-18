/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.event.output.adapter.extensions.ui;

import com.google.gson.JsonObject;

import javax.websocket.Session;

/**
 * This interface is exposed as an OSGI service, which will be invoked by the local websocket endpoint to inform new subscriptions; and do un-subscriptions..
 */
public interface UIOutputCallbackControllerService {

    /**
     * Used to subscribe the session id and stream id for later web socket connectivity
     *
     * @param streamName - Stream name which user register to.
     * @param version - Stream version which user uses.
     * @param session - Session which user registered.
     * @return
     */
    public void subscribeWebsocket(String streamName, String version, Session session);

    /**
     * Used to return events per streamId
     *
     * @param streamName - Stream name which user register to.
     * @param version - Stream version which user uses.
     * @param session - Session which user subscribed to.
     * @return the events list.
     */
    public void unsubscribeWebsocket(String streamName, String version, Session session);

    /**
     * Used to return events per http GET request.
     *
     * @param streamName - Stream name which user register to.
     * @param version - Stream version which user uses.
     * @param lastUpdatedTime - Last dispatched events time.
     * @return the events list.
     */
    public JsonObject retrieveEvents(String streamName, String version, String lastUpdatedTime);
}
