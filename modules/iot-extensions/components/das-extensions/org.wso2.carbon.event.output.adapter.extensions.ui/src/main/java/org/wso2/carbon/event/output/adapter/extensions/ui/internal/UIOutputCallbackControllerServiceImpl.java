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
package org.wso2.carbon.event.output.adapter.extensions.ui.internal;

import com.google.gson.JsonObject;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adapter.extensions.ui.UIOutputCallbackControllerService;
import org.wso2.carbon.event.output.adapter.extensions.ui.internal.util.CEPWebSocketSession;
import org.wso2.carbon.event.output.adapter.extensions.ui.internal.util.UIEventAdapterConstants;
import org.wso2.carbon.event.output.adapter.extensions.ui.internal.ds.UIEventAdaptorServiceInternalValueHolder;

import javax.websocket.Session;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Service implementation class which exposes to front end
 */
public class UIOutputCallbackControllerServiceImpl implements UIOutputCallbackControllerService {

    private ConcurrentHashMap<Integer, ConcurrentHashMap<String, CopyOnWriteArrayList<CEPWebSocketSession>>>
            outputEventAdaptorSessionMap;

    public UIOutputCallbackControllerServiceImpl() {
        outputEventAdaptorSessionMap =
                new ConcurrentHashMap<Integer, ConcurrentHashMap<String, CopyOnWriteArrayList<CEPWebSocketSession>>>();
    }


    /**
     * Used to subscribe the session id and stream id for later web socket connectivity
     *
     * @param streamName - Stream name which user register to.
     * @param version    - Stream version which user uses.
     * @param session    - Session which user registered.
     * @return
     */
    public void subscribeWebsocket(String streamName, String version, Session session) {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (version == null || " ".equals(version)) {
            version = UIEventAdapterConstants.ADAPTER_UI_DEFAULT_OUTPUT_STREAM_VERSION;
        }
        String streamId = streamName + UIEventAdapterConstants.ADAPTER_UI_COLON + version;
        ConcurrentHashMap<String, CopyOnWriteArrayList<CEPWebSocketSession>> tenantSpecificAdaptorMap =
                outputEventAdaptorSessionMap.get(tenantId);
        if (tenantSpecificAdaptorMap == null) {
            tenantSpecificAdaptorMap = new ConcurrentHashMap<String, CopyOnWriteArrayList<CEPWebSocketSession>>();
            if (null != outputEventAdaptorSessionMap.putIfAbsent(tenantId, tenantSpecificAdaptorMap)) {
                tenantSpecificAdaptorMap = outputEventAdaptorSessionMap.get(tenantId);
            }
        }
        CopyOnWriteArrayList<CEPWebSocketSession> adapterSpecificSessions = tenantSpecificAdaptorMap.get(streamId);
        if (adapterSpecificSessions == null) {
            adapterSpecificSessions = new CopyOnWriteArrayList<CEPWebSocketSession>();
            if (null != tenantSpecificAdaptorMap.putIfAbsent(streamId, adapterSpecificSessions)) {
                adapterSpecificSessions = tenantSpecificAdaptorMap.get(streamId);
            }
        }

        CEPWebSocketSession cepWebSocketSession = new CEPWebSocketSession(session);
        adapterSpecificSessions.add(cepWebSocketSession);
    }

    /**
     * Used to return registered sessions per streamId
     *
     * @param tenantId - Tenant id of the user.
     * @param streamId - Stream name and version which user register to.
     * @return the sessions list.
     */
    public CopyOnWriteArrayList<CEPWebSocketSession> getSessions(int tenantId, String streamId) {
        ConcurrentHashMap<String, CopyOnWriteArrayList<CEPWebSocketSession>> tenantSpecificAdaptorMap = outputEventAdaptorSessionMap.get(tenantId);
        if (tenantSpecificAdaptorMap != null) {
            return tenantSpecificAdaptorMap.get(streamId);
        }
        return null;
    }

    /**
     * Used to return events per streamId
     *
     * @param tenanId    - Tenant id of the user.
     * @param streamName - Stream name which user register to.
     * @param version    - Stream version which user uses.
     * @return the events list.
     */
    public LinkedBlockingDeque<Object> getEvents(int tenanId, String streamName, String version) {

        ConcurrentHashMap<String, LinkedBlockingDeque<Object>> tenantSpecificStreamMap =
                UIEventAdaptorServiceInternalValueHolder.getTenantSpecificStreamEventMap().get(tenanId);

        if (tenantSpecificStreamMap != null) {
            String streamId = streamName + UIEventAdapterConstants.ADAPTER_UI_COLON + version;
            return tenantSpecificStreamMap.get(streamId);
        }
        return null;
    }

    /**
     * Used to return events per streamId
     *
     * @param streamName - Stream name which user register to.
     * @param version    - Stream version which user uses.
     * @param session    - Session which user subscribed to.
     * @return the events list.
     */
    public void unsubscribeWebsocket(String streamName, String version, Session session) {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (version == null || " ".equals(version)) {
            version = UIEventAdapterConstants.ADAPTER_UI_DEFAULT_OUTPUT_STREAM_VERSION;
        }
        String id = streamName + UIEventAdapterConstants.ADAPTER_UI_COLON + version;
        ConcurrentHashMap<String, CopyOnWriteArrayList<CEPWebSocketSession>> tenantSpecificAdaptorMap = outputEventAdaptorSessionMap.get(tenantId);
        if (tenantSpecificAdaptorMap != null) {
            CopyOnWriteArrayList<CEPWebSocketSession> adapterSpecificSessions = tenantSpecificAdaptorMap.get(id);
            if (adapterSpecificSessions != null) {
                CEPWebSocketSession sessionToRemove = null;
                for (Iterator<CEPWebSocketSession> iterator = adapterSpecificSessions.iterator(); iterator.hasNext(); ) {
                    CEPWebSocketSession cepWebSocketSession = iterator.next();
                    if (session.getId().equals(cepWebSocketSession.getSession().getId())) {
                        sessionToRemove = cepWebSocketSession;
                        break;
                    }
                }
                if (sessionToRemove != null) {
                    adapterSpecificSessions.remove(sessionToRemove);
                }
            }
        }
    }

    /**
     * Used to return events per http GET request.
     *
     * @param streamName      - Stream name which user register to.
     * @param version         - Stream version which user uses.
     * @param lastUpdatedTime - Last dispatched events time.
     * @return the events list.
     */
    @Override
    public JsonObject retrieveEvents(String streamName, String version, String lastUpdatedTime) {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        LinkedBlockingDeque<Object> allEvents = getEvents(tenantId, streamName, version);
        //List<Object> eventsListToBeSent;
        Object lastEventTime = null;
        JsonObject eventsData;

        if (allEvents != null) {
            eventsData = new JsonObject();

            Boolean firstFilteredValue = true;
            long sentTimeStamp = Long.parseLong(lastUpdatedTime);
            //eventsListToBeSent = new ArrayList<Object>();

            StringBuilder allEventsAsString = new StringBuilder("[");
            // set Iterator as descending
            Iterator iterator = allEvents.descendingIterator();

            while (iterator.hasNext()) {

                Object[] eventValues = (Object[]) iterator.next();
                long eventTimeStamp = (Long) eventValues[UIEventAdapterConstants.INDEX_ONE];
                if (sentTimeStamp < eventTimeStamp) {

                    if (!firstFilteredValue) {
                        allEventsAsString.append(",");
                    }
                    firstFilteredValue = false;
                    String eventString = (String) eventValues[UIEventAdapterConstants.INDEX_ZERO];
                    allEventsAsString.append(eventString);
                }
            }
            allEventsAsString.append("]");

            if (allEvents.size() != 0) {
                Object[] lastObj = (Object[]) allEvents.getLast();
                lastEventTime = lastObj[UIEventAdapterConstants.INDEX_ONE];
                eventsData.addProperty("lastEventTime", String.valueOf(lastEventTime));
            }
            eventsData.addProperty("events", allEventsAsString.toString());

            return eventsData;
        }
        return null;
    }
}
