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
package org.wso2.mdm.agent.proxy.beans;

import org.wso2.mdm.agent.proxy.utils.ServerUtilities;

import java.util.Date;

/**
 * Persists refresh token to obtain new access token and id token to retrieve
 * login user claims
 */
public final class Token {
	private String refreshToken = null;
	private String idToken = null;
	private String accessToken = null;
	private Date receivedDate = null;

	@SuppressWarnings("unused")
	private boolean expired = false;

	public Date getDate() {
		return receivedDate;
	}

	public void setDate(String date) {
		receivedDate = ServerUtilities.convertDate(date);
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getIdToken() {
		return idToken;
	}

	public void setIdToken(String id_Token) {
		idToken = id_Token;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public void setExpired(boolean expired) {
		this.expired = expired;
	}

}
