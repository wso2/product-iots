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
package org.wso2.carbon.event.input.adapter.extensions.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterFactory;
import org.wso2.carbon.event.input.adapter.extensions.http.HTTPEventAdapterFactory;
import org.wso2.carbon.event.input.adapter.extensions.mqtt.MQTTEventAdapterFactory;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component component.name="input.iot.Mqtt.AdapterService.component" immediate="true"
 */

/**
 * @scr.component name="org.wso2.carbon.event.input.adapter.extension.EventAdapterServiceComponent" immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService" cardinality="1..1"
 * policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="http.service" interface="org.osgi.service.http.HttpService"
 * cardinality="1..1" policy="dynamic" bind="setHttpService" unbind="unsetHttpService"
 */
public class EventAdapterServiceComponent {

	private static final Log log = LogFactory.getLog(EventAdapterServiceComponent.class);

	protected void activate(ComponentContext context) {
		try {
			InputEventAdapterFactory mqttEventAdapterFactory = new MQTTEventAdapterFactory();
			context.getBundleContext().registerService(InputEventAdapterFactory.class.getName(),
													   mqttEventAdapterFactory, null);
			InputEventAdapterFactory httpEventEventAdapterFactory = new HTTPEventAdapterFactory();
			context.getBundleContext().registerService(InputEventAdapterFactory.class.getName(),
													   httpEventEventAdapterFactory, null);
			if (log.isDebugEnabled()) {
				log.debug("Successfully deployed the input IoT-MQTT adapter service");
			}
		} catch (RuntimeException e) {
			log.error("Can not create the input IoT-MQTT adapter service ", e);
		}
	}

	protected void setRealmService(RealmService realmService) {
		EventAdapterServiceDataHolder.registerRealmService(realmService);
	}

	protected void unsetRealmService(RealmService realmService) {
		EventAdapterServiceDataHolder.registerRealmService(null);
	}

	protected void setHttpService(HttpService httpService) {
		EventAdapterServiceDataHolder.registerHTTPService(httpService);
	}

	protected void unsetHttpService(HttpService httpService) {
		EventAdapterServiceDataHolder.registerHTTPService(null);
	}

}
