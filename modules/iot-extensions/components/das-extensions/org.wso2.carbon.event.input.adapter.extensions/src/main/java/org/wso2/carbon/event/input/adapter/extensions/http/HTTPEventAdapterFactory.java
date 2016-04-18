/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.carbon.event.input.adapter.extensions.http;


import org.wso2.carbon.event.input.adapter.core.InputEventAdapter;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterFactory;
import org.wso2.carbon.event.input.adapter.core.MessageType;
import org.wso2.carbon.event.input.adapter.core.Property;
import org.wso2.carbon.event.input.adapter.extensions.http.util.HTTPEventAdapterConstants;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * The http event adapter factory class to create a http input adapter
 */
public class HTTPEventAdapterFactory extends InputEventAdapterFactory {

	private ResourceBundle resourceBundle =
			ResourceBundle.getBundle("org.wso2.carbon.event.input.adapter.extensions.http.i18n.Resources", Locale.getDefault());
	private int httpPort;
	private int httpsPort;
	private int portOffset;

	public HTTPEventAdapterFactory() {
		portOffset = getPortOffset();
		httpPort = HTTPEventAdapterConstants.DEFAULT_HTTP_PORT + portOffset;
		httpsPort = HTTPEventAdapterConstants.DEFAULT_HTTPS_PORT + portOffset;
	}

	@Override
	public String getType() {
		return HTTPEventAdapterConstants.ADAPTER_TYPE_HTTP;
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

		// Transport Exposed
		Property exposedTransportsProperty = new Property(HTTPEventAdapterConstants.EXPOSED_TRANSPORTS);
		exposedTransportsProperty.setRequired(true);
		exposedTransportsProperty.setDisplayName(
				resourceBundle.getString(HTTPEventAdapterConstants.EXPOSED_TRANSPORTS));
		exposedTransportsProperty.setOptions(
				new String[]{HTTPEventAdapterConstants.HTTPS, HTTPEventAdapterConstants.HTTP,
						HTTPEventAdapterConstants.LOCAL, HTTPEventAdapterConstants.ALL});
		exposedTransportsProperty.setDefaultValue(HTTPEventAdapterConstants.ALL);
		propertyList.add(exposedTransportsProperty);

		// OAUTH validation endpoint admin service username
		Property username = new Property(HTTPEventAdapterConstants.USERNAME);
		username.setRequired(true);
		username.setDisplayName(resourceBundle.getString(HTTPEventAdapterConstants.USERNAME));
		username.setHint(resourceBundle.getString(HTTPEventAdapterConstants.USERNAME_HINT));
		propertyList.add(username);

		// OAUTH validation endpoint admin service password
		Property password = new Property(HTTPEventAdapterConstants.PASSWORD);
		password.setRequired(true);
		password.setDisplayName(resourceBundle.getString(HTTPEventAdapterConstants.PASSWORD));
		password.setHint(resourceBundle.getString(HTTPEventAdapterConstants.PASSWORD_HINT));
		propertyList.add(password);

		// OAUTH validation endpoint
		Property tokenValidationEndpoint = new Property(HTTPEventAdapterConstants.TOKEN_VALIDATION_ENDPOINT_URL);
		tokenValidationEndpoint.setRequired(true);
		tokenValidationEndpoint.setDisplayName(resourceBundle.getString(HTTPEventAdapterConstants.TOKEN_VALIDATION_ENDPOINT_URL));
		tokenValidationEndpoint.setHint(resourceBundle.getString(HTTPEventAdapterConstants.TOKEN_VALIDATION_ENDPOINT_URL_HINT));
		propertyList.add(tokenValidationEndpoint);

		Property maximumHttpConnectionPerHost = new Property(HTTPEventAdapterConstants.MAXIMUM_HTTP_CONNECTION_PER_HOST);
		maximumHttpConnectionPerHost.setRequired(true);
		maximumHttpConnectionPerHost.setDisplayName(resourceBundle.getString(
				HTTPEventAdapterConstants.MAXIMUM_HTTP_CONNECTION_PER_HOST));
		maximumHttpConnectionPerHost.setHint(resourceBundle.getString(
				HTTPEventAdapterConstants.MAXIMUM_HTTP_CONNECTION_PER_HOST_HINT));
		maximumHttpConnectionPerHost.setDefaultValue(HTTPEventAdapterConstants.MAX_HTTP_CONNECTION);
		propertyList.add(maximumHttpConnectionPerHost);

		Property maxTotalHttpConnection = new Property(HTTPEventAdapterConstants.MAXIMUM_TOTAL_HTTP_CONNECTION);
		maxTotalHttpConnection.setRequired(true);
		maxTotalHttpConnection.setDisplayName(resourceBundle.getString(
				HTTPEventAdapterConstants.MAXIMUM_TOTAL_HTTP_CONNECTION));
		maxTotalHttpConnection.setHint(resourceBundle.getString(
				HTTPEventAdapterConstants.MAXIMUM_TOTAL_HTTP_CONNECTION_HINT));
		maxTotalHttpConnection.setDefaultValue(HTTPEventAdapterConstants.MAX_TOTAL_HTTP_CONNECTION);
		propertyList.add(maxTotalHttpConnection);

		//Content Validator details
		Property contentValidator = new Property(HTTPEventAdapterConstants.ADAPTER_CONF_CONTENT_VALIDATOR_CLASSNAME);
		contentValidator.setDisplayName(
				resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_CONF_CONTENT_VALIDATOR_CLASSNAME));
		contentValidator.setRequired(false);
		contentValidator.setHint(
				resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_CONF_CONTENT_VALIDATOR_CLASSNAME_HINT));
		propertyList.add(contentValidator);
		contentValidator.setDefaultValue(HTTPEventAdapterConstants.DEFAULT);
		propertyList.add(contentValidator);

		//Content Validator Params details
		Property contentValidatorParams = new Property(HTTPEventAdapterConstants.ADAPTER_CONF_CONTENT_VALIDATOR_PARAMS);
		contentValidatorParams.setDisplayName(
				resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_CONF_CONTENT_VALIDATOR_PARAMS));
		contentValidatorParams.setRequired(false);
		contentValidatorParams.setHint(
				resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_CONF_CONTENT_VALIDATOR_PARAMS_HINT));
		propertyList.add(contentValidatorParams);
		contentValidatorParams.setDefaultValue(HTTPEventAdapterConstants.MQTT_CONTENT_VALIDATION_DEFAULT_PARAMETERS);
		propertyList.add(contentValidatorParams);
		return propertyList;
	}

	@Override
	public String getUsageTips() {
		return resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_USAGE_TIPS_PREFIX) + httpPort +
				resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_USAGE_TIPS_MID1) + httpsPort +
				resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_USAGE_TIPS_MID2) + httpPort +
				resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_USAGE_TIPS_MID3) + httpsPort +
				resourceBundle.getString(HTTPEventAdapterConstants.ADAPTER_USAGE_TIPS_POSTFIX);
	}

	@Override
	public InputEventAdapter createEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration,
												Map<String, String> globalProperties) {
		return new HTTPEventAdapter(eventAdapterConfiguration, globalProperties);
	}

	private int getPortOffset() {
		return CarbonUtils.getPortFromServerConfig(HTTPEventAdapterConstants.CARBON_CONFIG_PORT_OFFSET_NODE) + 1;
	}
}
