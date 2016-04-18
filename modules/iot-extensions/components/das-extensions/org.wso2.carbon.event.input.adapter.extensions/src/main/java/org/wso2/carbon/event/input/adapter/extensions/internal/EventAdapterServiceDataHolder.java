/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.input.adapter.extensions.internal;

import org.osgi.service.http.HttpService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * common place to hold some OSGI service references.
 */
public final class EventAdapterServiceDataHolder {

	private static RealmService realmService;
	private static HttpService httpService;

	private EventAdapterServiceDataHolder() {
	}

	public static void registerRealmService(
			RealmService realmService) {
		EventAdapterServiceDataHolder.realmService = realmService;
	}

	public static RealmService getRealmService() {
		return realmService;
	}

	public static void registerHTTPService(
			HttpService httpService) {
		EventAdapterServiceDataHolder.httpService = httpService;
	}

	public static HttpService getHTTPService() {
		return httpService;
	}


}
