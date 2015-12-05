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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.mdm.agent.beans;

import org.wso2.mdm.agent.utils.Constants;

public class ServerConfig {
	private String serverIP;
	private String serverURL;
	private String APIServerURL;
	private static final String COLON = ":";

	public String getServerIP() {
		return serverIP;
	}

	public void setServerIP(String serverIP) {
		this.serverIP = serverIP;
	}

	public String getServerURL() {
		serverURL = Constants.SERVER_PROTOCOL + serverIP + COLON + Constants.SERVER_PORT;
		return serverURL;
	}

	public void setServerURL(String serverURL) {
		this.serverURL = serverURL;
	}

	public String getAPIServerURL() {
		APIServerURL = Constants.SERVER_PROTOCOL + serverIP + COLON + Constants.API_SERVER_PORT;
		return APIServerURL;
	}

	public void setAPIServerURL(String aPIServerURL) {
		APIServerURL = aPIServerURL;
	}
}
