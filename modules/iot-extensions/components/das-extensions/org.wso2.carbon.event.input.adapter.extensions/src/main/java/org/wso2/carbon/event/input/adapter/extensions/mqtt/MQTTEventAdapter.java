/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.input.adapter.extensions.mqtt;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapter;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;
import org.wso2.carbon.event.input.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.input.adapter.extensions.mqtt.util.MQTTAdapterListener;
import org.wso2.carbon.event.input.adapter.extensions.mqtt.util.MQTTBrokerConnectionConfiguration;
import org.wso2.carbon.event.input.adapter.extensions.mqtt.util.MQTTEventAdapterConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Input MQTTEventAdapter will be used to receive events with MQTT protocol using specified broker and topic.
 */
public class MQTTEventAdapter implements InputEventAdapter {

    private final InputEventAdapterConfiguration eventAdapterConfiguration;
    private final Map<String, String> globalProperties;
    private InputEventAdapterListener eventAdapterListener;
    private final String id = UUID.randomUUID().toString();
    private MQTTAdapterListener mqttAdapterListener;
    private MQTTBrokerConnectionConfiguration mqttBrokerConnectionConfiguration;


    public MQTTEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration,
                            Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }

    @Override
    public void init(InputEventAdapterListener eventAdapterListener) throws InputEventAdapterException {
        this.eventAdapterListener = eventAdapterListener;
        try {
            int keepAlive;

            //If global properties are available those will be assigned else constant values will be assigned
            if (globalProperties.get(MQTTEventAdapterConstants.ADAPTER_CONF_KEEP_ALIVE) != null) {
                keepAlive = Integer.parseInt((globalProperties.get(MQTTEventAdapterConstants.ADAPTER_CONF_KEEP_ALIVE)));
            } else {
                keepAlive = MQTTEventAdapterConstants.ADAPTER_CONF_DEFAULT_KEEP_ALIVE;
            }
            String contentValidationParams = eventAdapterConfiguration.getProperties().get(MQTTEventAdapterConstants.ADAPTER_CONF_CONTENT_VALIDATOR_PARAMS);
            String params[] = contentValidationParams.split(",");
            Map<String, String> paramsMap = new HashMap<>();
            for (String param: params) {
                String paramsKeyAndValue[] = splitOnFirst(param, ':');
                if (paramsKeyAndValue.length != 2) {
                    throw  new InputEventAdapterException("Invalid parameters for content validation - " + param);
                }
                paramsMap.put(paramsKeyAndValue[0], paramsKeyAndValue[1]);
            }

            mqttBrokerConnectionConfiguration = new MQTTBrokerConnectionConfiguration(
                    eventAdapterConfiguration.getProperties().get(MQTTEventAdapterConstants.ADAPTER_CONF_URL),
                    eventAdapterConfiguration.getProperties().get(MQTTEventAdapterConstants.ADAPTER_CONF_USERNAME),
                    eventAdapterConfiguration.getProperties().get(MQTTEventAdapterConstants.ADAPTER_CONF_SCOPES),
                    eventAdapterConfiguration.getProperties().get(MQTTEventAdapterConstants.ADAPTER_CONF_DCR_URL),
                    eventAdapterConfiguration.getProperties().get(MQTTEventAdapterConstants.ADAPTER_CONF_CLEAN_SESSION),
                    keepAlive,
                    eventAdapterConfiguration.getProperties().get(MQTTEventAdapterConstants.ADAPTER_CONF_CONTENT_VALIDATOR_CLASSNAME),
                    paramsMap
                    );


            mqttAdapterListener = new MQTTAdapterListener(mqttBrokerConnectionConfiguration,
                    eventAdapterConfiguration.getProperties().get(MQTTEventAdapterConstants.ADAPTER_MESSAGE_TOPIC),
                    eventAdapterConfiguration.getProperties().get(MQTTEventAdapterConstants.ADAPTER_CONF_CLIENTID),
                    eventAdapterListener, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());

        } catch (Throwable t) {
            throw new InputEventAdapterException(t.getMessage(), t);
        }
    }

    private String[] splitOnFirst(String str, char c) {
        int idx = str.indexOf(c);
        String head = str.substring(0, idx);
        String tail = str.substring(idx + 1);
        return new String[] { head, tail} ;
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("not-supported");
    }

    @Override
    public void connect() {
        mqttAdapterListener.createConnection();
    }

    @Override
    public void disconnect() {
        //when mqtt and this feature  both together then this method becomes a blocking method, Therefore
        // have used a thread to skip it.
        try {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    if (mqttAdapterListener != null) {
                        mqttAdapterListener.stopListener(eventAdapterConfiguration.getName());
                    }
                }
            });
            thread.start();
            thread.join(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MQTTEventAdapter)) return false;

        MQTTEventAdapter that = (MQTTEventAdapter) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }


    @Override
    public boolean isEventDuplicatedInCluster() {
        return true;
    }

    @Override
    public boolean isPolling() {
        return true;
    }

}
