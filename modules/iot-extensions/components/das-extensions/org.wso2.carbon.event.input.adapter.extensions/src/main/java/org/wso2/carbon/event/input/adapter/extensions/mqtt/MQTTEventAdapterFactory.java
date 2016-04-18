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

import org.wso2.carbon.event.input.adapter.core.*;
import org.wso2.carbon.event.input.adapter.extensions.mqtt.util.MQTTEventAdapterConstants;

import java.util.*;

/**
 * The mqtt event adapter factory class to create a mqtt input adapter
 */
public class MQTTEventAdapterFactory extends InputEventAdapterFactory {

    private ResourceBundle resourceBundle = ResourceBundle.getBundle
            ("org.wso2.carbon.event.input.adapter.extensions.mqtt.i18n.Resources", Locale.getDefault());

    @Override
    public String getType() {
        return MQTTEventAdapterConstants.ADAPTER_TYPE_MQTT;
    }

    @Override
    public List<String> getSupportedMessageFormats() {
        List<String> supportInputMessageTypes = new ArrayList<String>();
        supportInputMessageTypes.add(MessageType.JSON);
        return supportInputMessageTypes;
    }

    @Override
    public List<Property> getPropertyList() {
        List<Property> propertyList = new ArrayList<Property>();

        // set topic
        Property topicProperty = new Property(MQTTEventAdapterConstants.ADAPTER_MESSAGE_TOPIC);
        topicProperty.setDisplayName(resourceBundle.getString(MQTTEventAdapterConstants.ADAPTER_MESSAGE_TOPIC));
        topicProperty.setRequired(true);
        topicProperty.setHint(resourceBundle.getString(MQTTEventAdapterConstants.ADAPTER_MESSAGE_TOPIC_HINT));
        propertyList.add(topicProperty);

        //Broker Url
        Property brokerUrl = new Property(MQTTEventAdapterConstants.ADAPTER_CONF_URL);
        brokerUrl.setDisplayName(resourceBundle.getString(MQTTEventAdapterConstants.ADAPTER_CONF_URL));
        brokerUrl.setRequired(true);
        brokerUrl.setHint(resourceBundle.getString(MQTTEventAdapterConstants.ADAPTER_CONF_URL_HINT));
        propertyList.add(brokerUrl);

        //DCR endpoint details
        Property dcrUrl = new Property(MQTTEventAdapterConstants.ADAPTER_CONF_DCR_URL);
        dcrUrl.setDisplayName(resourceBundle.getString(MQTTEventAdapterConstants.ADAPTER_CONF_DCR_URL));
        dcrUrl.setRequired(false);
        dcrUrl.setHint(resourceBundle.getString(MQTTEventAdapterConstants.ADAPTER_CONF_DCR_URL_HINT));
        propertyList.add(dcrUrl);

        //Content Validator details
        Property contentValidator = new Property(MQTTEventAdapterConstants.ADAPTER_CONF_CONTENT_VALIDATOR_CLASSNAME);
        contentValidator.setDisplayName(
                resourceBundle.getString(MQTTEventAdapterConstants.ADAPTER_CONF_CONTENT_VALIDATOR_CLASSNAME));
        contentValidator.setRequired(false);
        contentValidator.setHint(
                resourceBundle.getString(MQTTEventAdapterConstants.ADAPTER_CONF_CONTENT_VALIDATOR_CLASSNAME_HINT));
        contentValidator.setDefaultValue(Constants.DEFAULT);
        propertyList.add(contentValidator);

        //Content Validator Params details
        Property contentValidatorParams = new Property(MQTTEventAdapterConstants.ADAPTER_CONF_CONTENT_VALIDATOR_PARAMS);
        contentValidatorParams.setDisplayName(
                resourceBundle.getString(MQTTEventAdapterConstants.ADAPTER_CONF_CONTENT_VALIDATOR_PARAMS));
        contentValidatorParams.setRequired(false);
        contentValidatorParams.setHint(
                resourceBundle.getString(MQTTEventAdapterConstants.ADAPTER_CONF_CONTENT_VALIDATOR_PARAMS_HINT));
        contentValidatorParams.setDefaultValue(Constants.MQTT_CONTENT_VALIDATION_DEFAULT_PARAMETERS);
        propertyList.add(contentValidatorParams);

        //Broker Username
        Property userName = new Property(MQTTEventAdapterConstants.ADAPTER_CONF_USERNAME);
        userName.setDisplayName(
                resourceBundle.getString(MQTTEventAdapterConstants.ADAPTER_CONF_USERNAME));
        userName.setRequired(false);
        userName.setHint(resourceBundle.getString(MQTTEventAdapterConstants.ADAPTER_CONF_USERNAME_HINT));
        propertyList.add(userName);

        //Broker Required Scopes.
        Property scopes = new Property(MQTTEventAdapterConstants.ADAPTER_CONF_SCOPES);
        scopes.setDisplayName(
                resourceBundle.getString(MQTTEventAdapterConstants.ADAPTER_CONF_SCOPES));
        scopes.setRequired(false);
        scopes.setHint(resourceBundle.getString(MQTTEventAdapterConstants.ADAPTER_CONF_SCOPES_HINT));
        propertyList.add(scopes);

        //Broker clear session
        Property clearSession = new Property(MQTTEventAdapterConstants.ADAPTER_CONF_CLEAN_SESSION);
        clearSession.setDisplayName(resourceBundle.getString(MQTTEventAdapterConstants.ADAPTER_CONF_CLEAN_SESSION));
        clearSession.setRequired(false);
        clearSession.setOptions(new String[]{"true", "false"});
        clearSession.setDefaultValue("true");
        clearSession.setHint(resourceBundle.getString(MQTTEventAdapterConstants.ADAPTER_CONF_CLEAN_SESSION_HINT));
        propertyList.add(clearSession);

        // set clientId
        Property clientId = new Property(MQTTEventAdapterConstants.ADAPTER_CONF_CLIENTID);
        clientId.setDisplayName(resourceBundle.getString(MQTTEventAdapterConstants.ADAPTER_CONF_CLIENTID));
        clientId.setRequired(false);
        clientId.setHint(resourceBundle.getString(MQTTEventAdapterConstants.ADAPTER_CONF_CLIENTID_HINT));
        propertyList.add(clientId);

        return propertyList;
    }

    @Override
    public String getUsageTips() {
        return null;
    }

    @Override
    public InputEventAdapter createEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration,
                                                Map<String, String> globalProperties) {
        return new MQTTEventAdapter(eventAdapterConfiguration, globalProperties);
    }
}
