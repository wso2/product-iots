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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.output.adapter.core.EventAdapterUtil;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterRuntimeException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.output.adapter.extensions.ui.internal.ds.OutputAdaptorEventStreamServiceValueHolder;
import org.wso2.carbon.event.output.adapter.extensions.ui.internal.util.UIEventAdapterConstants;
import org.wso2.carbon.event.output.adapter.extensions.ui.internal.UIOutputCallbackControllerServiceImpl;
import org.wso2.carbon.event.output.adapter.extensions.ui.internal.ds.UIEventAdaptorServiceInternalValueHolder;
import org.wso2.carbon.event.output.adapter.extensions.ui.internal.util.CEPWebSocketSession;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Contains the life cycle of executions regarding the UI Adapter
 */

public class UIEventAdapter implements OutputEventAdapter {

    private static final Log log = LogFactory.getLog(UIEventAdapter.class);
    private OutputEventAdapterConfiguration eventAdapterConfiguration;
    private Map<String, String> globalProperties;
    private int queueSize;
    private LinkedBlockingDeque<Object> streamSpecificEvents;
    private static ThreadPoolExecutor executorService;
    private int tenantId;
    private boolean doLogDroppedMessage;

    private String streamId;
    private StreamDefinition streamDefinition;
    private List<Attribute> streamMetaAttributes;
    private List<Attribute> streamCorrelationAttributes;
    private List<Attribute> streamPayloadAttributes;

    public UIEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration, Map<String,
            String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
        this.doLogDroppedMessage = true;
    }

    @Override
    public void init() throws OutputEventAdapterException {

        tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        //ExecutorService will be assigned  if it is null
        if (executorService == null) {
            int minThread;
            int maxThread;
            long defaultKeepAliveTime;
            int jobQueSize;

            //If global properties are available those will be assigned else constant values will be assigned
            if (globalProperties.get(UIEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE_NAME) != null) {
                minThread = Integer.parseInt(globalProperties.get(
                        UIEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE_NAME));
            } else {
                minThread = UIEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE;
            }

            if (globalProperties.get(UIEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE_NAME) != null) {
                maxThread = Integer.parseInt(globalProperties.get(
                        UIEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE_NAME));
            } else {
                maxThread = UIEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE;
            }

            if (globalProperties.get(UIEventAdapterConstants.ADAPTER_KEEP_ALIVE_TIME_NAME) != null) {
                defaultKeepAliveTime = Integer.parseInt(globalProperties.get(
                        UIEventAdapterConstants.ADAPTER_KEEP_ALIVE_TIME_NAME));
            } else {
                defaultKeepAliveTime = UIEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME_IN_MILLIS;
            }

            if (globalProperties.get(UIEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME) != null) {
                jobQueSize = Integer.parseInt(globalProperties.get(
                        UIEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME));
            } else {
                jobQueSize = UIEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE;
            }

            executorService = new ThreadPoolExecutor(minThread, maxThread, defaultKeepAliveTime, TimeUnit.MILLISECONDS,
                                                     new LinkedBlockingQueue<Runnable>(jobQueSize));
        }

        streamId = eventAdapterConfiguration.getOutputStreamIdOfWso2eventMessageFormat();
        if (streamId == null || streamId.isEmpty()) {
            throw new OutputEventAdapterRuntimeException("UI event adapter needs a output stream id");
        }

        // fetch the "streamDefinition" corresponding to the "streamId" and then fetch the different attribute types
        // of the streamDefinition corresponding to the event's streamId. They are required when validating values in
        // the events against the streamDef attributes.
        streamDefinition = getStreamDefinition(streamId);
        streamMetaAttributes = streamDefinition.getMetaData();
        streamCorrelationAttributes = streamDefinition.getCorrelationData();
        streamPayloadAttributes = streamDefinition.getPayloadData();

        ConcurrentHashMap<Integer, ConcurrentHashMap<String, String>> tenantSpecifcEventOutputAdapterMap =
                UIEventAdaptorServiceInternalValueHolder.getTenantSpecificOutputEventStreamAdapterMap();

        ConcurrentHashMap<String, String> streamSpecifAdapterMap = tenantSpecifcEventOutputAdapterMap.get(tenantId);

        if (streamSpecifAdapterMap == null) {
            streamSpecifAdapterMap = new ConcurrentHashMap<String, String>();
            if (null != tenantSpecifcEventOutputAdapterMap.putIfAbsent(tenantId, streamSpecifAdapterMap)) {
                streamSpecifAdapterMap = tenantSpecifcEventOutputAdapterMap.get(tenantId);
            }
        }

        String adapterName = streamSpecifAdapterMap.get(streamId);

        if (adapterName != null) {
            throw new OutputEventAdapterException(("An Output ui event adapter \"" + adapterName + "\" is already" +
                    " exist for stream id \"" + streamId + "\""));
        } else {
            streamSpecifAdapterMap.put(streamId, eventAdapterConfiguration.getName());

            ConcurrentHashMap<Integer, ConcurrentHashMap<String, LinkedBlockingDeque<Object>>> tenantSpecificStreamMap =
                    UIEventAdaptorServiceInternalValueHolder.getTenantSpecificStreamEventMap();
            ConcurrentHashMap<String, LinkedBlockingDeque<Object>> streamSpecificEventsMap =
                    tenantSpecificStreamMap.get(tenantId);

            if (streamSpecificEventsMap == null) {
                streamSpecificEventsMap = new ConcurrentHashMap<String, LinkedBlockingDeque<Object>>();
                if (null != tenantSpecificStreamMap.putIfAbsent(tenantId, streamSpecificEventsMap)) {
                    streamSpecificEventsMap = tenantSpecificStreamMap.get(tenantId);
                }
            }
            streamSpecificEvents = streamSpecificEventsMap.get(streamId);

            if (streamSpecificEvents == null) {
                streamSpecificEvents = new LinkedBlockingDeque<Object>();
                if (null != streamSpecificEventsMap.putIfAbsent(streamId, streamSpecificEvents)) {
                    streamSpecificEvents = streamSpecificEventsMap.get(streamId);
                }
            }
        }

        if (globalProperties.get(UIEventAdapterConstants.ADAPTER_EVENT_QUEUE_SIZE_NAME) != null) {
            try {
                queueSize = Integer.parseInt(
                        globalProperties.get(UIEventAdapterConstants.ADAPTER_EVENT_QUEUE_SIZE_NAME));
            } catch (NumberFormatException e) {
                log.error("String does not have the appropriate format for conversion." + e.getMessage());
                queueSize = UIEventAdapterConstants.EVENTS_QUEUE_SIZE;
            }
        } else {
            queueSize = UIEventAdapterConstants.EVENTS_QUEUE_SIZE;
        }
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("Test connection is not available");
    }

    @Override
    public void connect() {
        //Not needed
    }

    @Override
    public void publish(Object message, Map<String, String> dynamicProperties) {

        Event event = (Event) message;
        StringBuilder eventBuilder = new StringBuilder("[");

        if (streamSpecificEvents.size() == queueSize) {
            streamSpecificEvents.removeFirst();
        }

        eventBuilder.append(event.getTimeStamp());

        if (event.getMetaData() != null) {
            eventBuilder.append(",");
            Object[] metaData = event.getMetaData();
            for (int i = 0; i < metaData.length; i++) {
                eventBuilder.append("\"");
                eventBuilder.append(metaData[i]);
                eventBuilder.append("\"");
                if (i != (metaData.length - 1)) {
                    eventBuilder.append(",");
                }
            }
        }

        if (event.getCorrelationData() != null) {
            Object[] correlationData = event.getCorrelationData();

            eventBuilder.append(",");

            for (int i = 0; i < correlationData.length; i++) {
                eventBuilder.append("\"");
                eventBuilder.append(correlationData[i]);
                eventBuilder.append("\"");
                if (i != (correlationData.length - 1)) {
                    eventBuilder.append(",");
                }
            }
        }

        if (event.getPayloadData() != null) {
            Object[] payloadData = event.getPayloadData();
            eventBuilder.append(",");
            for (int i = 0; i < payloadData.length; i++) {
                eventBuilder.append("\"");
                eventBuilder.append(payloadData[i]);
                eventBuilder.append("\"");
                if (i != (payloadData.length - 1)) {
                    eventBuilder.append(",");
                }
            }
        }

        eventBuilder.append("]");
        String eventString = eventBuilder.toString();
        Object[] eventValues = new Object[UIEventAdapterConstants.INDEX_TWO];
        eventValues[UIEventAdapterConstants.INDEX_ZERO] = eventString;
        eventValues[UIEventAdapterConstants.INDEX_ONE] = System.currentTimeMillis();
        streamSpecificEvents.add(eventValues);

        // fetch all valid sessions checked against any queryParameters provided when subscribing.
        CopyOnWriteArrayList<CEPWebSocketSession> validSessions = getValidSessions(event);

        try {
            executorService.execute(new WebSocketSender(validSessions, eventString));
        } catch (RejectedExecutionException e) {
            EventAdapterUtil.logAndDrop(eventAdapterConfiguration.getName(), message, "Job queue is full", e, log,
                                        tenantId);
        }

    }

    @Override
    public void disconnect() {
        //Not needed
    }

    @Override
    public void destroy() {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        ConcurrentHashMap<String, String> tenantSpecificAdapterMap = UIEventAdaptorServiceInternalValueHolder
                .getTenantSpecificOutputEventStreamAdapterMap().get(tenantId);
        if (tenantSpecificAdapterMap != null && streamId != null) {
            tenantSpecificAdapterMap.remove(streamId);      //Removing outputadapter and streamId
        }

        ConcurrentHashMap<String, LinkedBlockingDeque<Object>> tenantSpecificStreamEventMap =
                UIEventAdaptorServiceInternalValueHolder.getTenantSpecificStreamEventMap().get(tenantId);
        if (tenantSpecificStreamEventMap != null && streamId != null) {
            //Removing the streamId and events registered for the output adapter
            tenantSpecificStreamEventMap.remove(streamId);
        }
    }

    @Override
    public boolean isPolled() {
        return true;
    }

    /**
     * Fetch the StreamDefinition corresponding to the given StreamId from the EventStreamService.
     *
     * @param streamId the streamId of this UIEventAdaptor.
     * @return the "StreamDefinition" object corresponding to the streamId of this EventAdaptor.
     * @throws OutputEventAdapterException if the "EventStreamService" OSGI service is unavailable/unregistered or if
     *                                     the matching Steam-Definition for the given StreamId cannot be retrieved.
     */
    private StreamDefinition getStreamDefinition(String streamId) throws OutputEventAdapterException {
        EventStreamService eventStreamService = OutputAdaptorEventStreamServiceValueHolder.getEventStreamService();
        if (eventStreamService != null) {
            try {
                return eventStreamService.getStreamDefinition(streamId);
            } catch (EventStreamConfigurationException e) {
                String adaptorType = eventAdapterConfiguration.getType();
                String adaptorName = eventAdapterConfiguration.getName();
                String errorMsg = "Error while retrieving Stream-Definition for Stream with id [" + streamId + "] " +
                        "for Adaptor [" + adaptorName + "] of type [" + adaptorType + "].";
                log.error(errorMsg);
                throw new OutputEventAdapterException(errorMsg, e);
            }
        }
        throw new OutputEventAdapterException(
                "Could not retrieve the EventStreamService whilst trying to fetch the Stream-Definition of Stream " +
                        "with Id [" + streamId + "].");
    }

    /**
     * Fetches all valid web-socket sessions from the entire pool of subscribed sessions. The validity is checked
     * against any queryString provided when subscribing to the web-socket endpoint.
     *
     * @param event the current event received and that which needs to be published to subscribed sessions.
     * @return a list of all validated web-socket sessions against the queryString values.
     */
    private CopyOnWriteArrayList<CEPWebSocketSession> getValidSessions(Event event) {
        CopyOnWriteArrayList<CEPWebSocketSession> validSessions = new CopyOnWriteArrayList<>();
        UIOutputCallbackControllerServiceImpl uiOutputCallbackControllerServiceImpl =
                UIEventAdaptorServiceInternalValueHolder.getUIOutputCallbackRegisterServiceImpl();
        // get all subscribed web-socket sessions.
        CopyOnWriteArrayList<CEPWebSocketSession> cepWebSocketSessions =
                uiOutputCallbackControllerServiceImpl.getSessions(tenantId, streamId);
        if (cepWebSocketSessions != null) {
            for (CEPWebSocketSession cepWebSocketSession : cepWebSocketSessions) {
                boolean isValidSession = validateEventAgainstSessionFilters(event, cepWebSocketSession);
                if (isValidSession) {
                    validSessions.add(cepWebSocketSession);
                }
            }
        }
        return validSessions;
    }


    /**
     * Processes the given session's validity to receive the current "event" against any queryParams that was used at
     * the time when the web-socket-session is subscribed. This method can be extended to validate the event against
     * any additional attribute of the given session too.
     *
     * @param event               the current event received and that which needs to be published to subscribed
     *                            sessions.
     * @param cepWebSocketSession the session which needs validated for its authenticity to receive this event.
     * @return "true" if the session is valid to receive the event else "false".
     */
    private boolean validateEventAgainstSessionFilters(Event event, CEPWebSocketSession cepWebSocketSession) {

        // fetch the queryString Key:Value pair map of the given session.
        Map<String, String> queryParamValuePairs = cepWebSocketSession.getQueryParamValuePairs();
        if (queryParamValuePairs != null) {
            // fetch the different attribute values received as part of the current event.
            Object[] eventMetaData = event.getMetaData();
            Object[] eventCorrelationData = event.getCorrelationData();
            Object[] eventPayloadData = event.getPayloadData();

            if (streamMetaAttributes != null) {
                for (int i = 0; i < streamMetaAttributes.size(); i++) {
                    String attributeName = streamMetaAttributes.get(i).getName();
                    String queryValue = queryParamValuePairs.get(attributeName);

                    if (queryValue != null &&
                            (eventMetaData == null || !eventMetaData[i].toString().equals(queryValue))) {
                        return false;
                    }
                }
            }

            if (streamCorrelationAttributes != null) {
                for (int i = 0; i < streamCorrelationAttributes.size(); i++) {
                    String attributeName = streamCorrelationAttributes.get(i).getName();
                    String queryValue = queryParamValuePairs.get(attributeName);

                    if (queryValue != null &&
                            (eventCorrelationData == null || !eventCorrelationData[i].toString().equals(queryValue))) {
                        return false;
                    }
                }
            }

            if (streamPayloadAttributes != null) {
                for (int i = 0; i < streamPayloadAttributes.size(); i++) {
                    String attributeName = streamPayloadAttributes.get(i).getName();
                    String queryValue = queryParamValuePairs.get(attributeName);

                    if (queryValue != null && (eventPayloadData == null || !eventPayloadData[i].toString().equals(
                            queryValue))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private class WebSocketSender implements Runnable {

        private String message;
        private CopyOnWriteArrayList<CEPWebSocketSession> cepWebSocketSessions;

        public WebSocketSender(CopyOnWriteArrayList<CEPWebSocketSession> cepWebSocketSessions, String message) {
            this.cepWebSocketSessions = cepWebSocketSessions;
            this.message = message;
        }

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p/>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            if (cepWebSocketSessions != null) {
                doLogDroppedMessage = true;
                for (CEPWebSocketSession cepWebSocketSession : cepWebSocketSessions) {
                    synchronized (cepWebSocketSession) {
                        try {
                            cepWebSocketSession.getSession().getBasicRemote().sendText(message);
                        } catch (IOException e) {
                            EventAdapterUtil.logAndDrop(eventAdapterConfiguration.getName(), message,
                                                        "Cannot send to endpoint", e, log, tenantId);
                        }
                    }
                }
            } else if (doLogDroppedMessage) {
                EventAdapterUtil.logAndDrop(eventAdapterConfiguration.getName(), message, "No clients registered", log,
                                            tenantId);
                doLogDroppedMessage = false;
            }
        }
    }
}

