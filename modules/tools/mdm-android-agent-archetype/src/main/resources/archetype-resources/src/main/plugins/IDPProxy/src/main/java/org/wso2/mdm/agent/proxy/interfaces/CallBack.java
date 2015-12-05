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
package org.wso2.mdm.agent.proxy.interfaces;

import org.wso2.mdm.agent.proxy.beans.Token;

/**
 * This interface handles token callback when the application
 * is able to receive the access token from the app local storage or from the server.
 * Applications can implement this when it requires access token.
 */
public interface CallBack {

	/**
	 * Get access token stored in the app preferences.
	 *
	 * @param status  - Status code.
	 * @param message - Success/Error message.
	 * @param token   - Token retrieved.
	 */
	void receiveAccessToken(String status, String message, Token token);

	/**
	 * Get a new access token from the server.
	 *
	 * @param status  - Status code.
	 * @param message - Success/Error message.
	 * @param token   - Token retrieved.
	 */
	void receiveNewAccessToken(String status, String message, Token token);

}
