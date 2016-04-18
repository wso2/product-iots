package org.wso2.carbon.event.output.adapter.extensions.ui.internal.util;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.websocket.Session;
import java.util.HashMap;
import java.util.Map;

/**
 * This is wrapper class over the javax.websocket.Session implementation. This class contains additional attributes
 * of the Session object derived from processing some of the (default) existing attributes.
 * Ex: Query-String's [Key:Value] Map derived from the queryString attribute of the original class.
 */
public class CEPWebSocketSession {
    private static final Log log = LogFactory.getLog(CEPWebSocketSession.class);

    private static final String QUERY_STRING_SEPERATOR = "&";
    private static final String QUERY_KEY_VALUE_SEPERATOR = "=";
    private Map<String, String> queryParamValuePairs = null;
    private Session session;

    public CEPWebSocketSession(Session session) {
        this.session = session;
        setQueryParamValuePairs();
    }

    public Map<String, String> getQueryParamValuePairs() {
        return queryParamValuePairs;
    }

    public Session getSession() {
        return session;
    }

    /**
     * Processes the queryString from the current instance's Session attribute and constructs a map of Query
     * Key:Value pair.
     */
    private void setQueryParamValuePairs() {
        if (session.getQueryString() != null) {
            String queryString = session.getQueryString();
            String[] allQueryParamPairs = queryString.split(QUERY_STRING_SEPERATOR);

            for (String keyValuePair : allQueryParamPairs) {
                String[] thisQueryParamPair = keyValuePair.split(QUERY_KEY_VALUE_SEPERATOR);

                if (thisQueryParamPair.length != 2) {
                    log.warn("Invalid query string [" + queryString + "] passed in.");
                    break;
                }

                if (queryParamValuePairs == null) {
                    queryParamValuePairs = new HashMap<>();
                }

                queryParamValuePairs.put(thisQueryParamPair[0], thisQueryParamPair[1]);
            }
        }
    }
}
