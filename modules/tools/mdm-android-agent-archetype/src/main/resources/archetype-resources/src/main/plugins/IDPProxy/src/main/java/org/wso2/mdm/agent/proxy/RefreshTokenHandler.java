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
package org.wso2.mdm.agent.proxy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.util.Log;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.mdm.agent.proxy.beans.EndPointInfo;
import org.wso2.mdm.agent.proxy.beans.Token;
import org.wso2.mdm.agent.proxy.utils.Constants;
import org.wso2.mdm.agent.proxy.utils.ServerUtilities;
import org.wso2.mdm.agent.proxy.utils.Constants.HTTP_METHODS;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Getting new access token and refresh token after access token expiration
 */
public class RefreshTokenHandler {
	private static final String TAG = "RefreshTokenHandler";
	private static final String SCOPE_LABEL = "scope";
	private static final String PRODUCTION_LABEL = "PRODUCTION";
	private static final DateFormat dateFormat =
			new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
	private static final String COLON = ":";
	private Token token;

	public RefreshTokenHandler(Token token) {
		this.token = token;
	}

	public void obtainNewAccessToken() {
		new NetworkCallTask().execute();
	}

	private class NetworkCallTask extends AsyncTask<String, Void, String> {

		private String responseCode = null;

		@Override
		protected String doInBackground(String... params) {
			String response = null;

			Map<String, String> requestParams = new HashMap<String, String>();
			requestParams.put(Constants.GRANT_TYPE, Constants.REFRESH_TOKEN);
			requestParams.put(Constants.REFRESH_TOKEN, token.getRefreshToken());
			requestParams.put(SCOPE_LABEL, PRODUCTION_LABEL);
			EndPointInfo apiUtilities = new EndPointInfo();
			apiUtilities.setEndPoint(IdentityProxy.getInstance().getAccessTokenURL());
			apiUtilities.setHttpMethod(HTTP_METHODS.POST);
			apiUtilities.setRequestParamsMap(requestParams);

			byte[] credentials = Base64.encodeBase64((IdentityProxy.clientID + COLON +
			                                          IdentityProxy.clientSecret).getBytes());
			String encodedCredentials = new String(credentials);

			Map<String, String> headers = new HashMap<String, String>();

			String authorizationString = Constants.AUTHORIZATION_MODE + encodedCredentials;
			headers.put(Constants.AUTHORIZATION_HEADER, authorizationString);
			headers.put(Constants.CONTENT_TYPE_HEADER, Constants.DEFAULT_CONTENT_TYPE);

			Map<String, String> responseParams = null;
			try {
				responseParams = ServerUtilities.postDataAPI(apiUtilities, headers);
			} catch (IDPTokenManagerException e) {
				Log.e(TAG, "Failed to contact server." + e);
			}

			response = responseParams.get(Constants.SERVER_RESPONSE_BODY);
			responseCode = responseParams.get(Constants.SERVER_RESPONSE_STATUS);
			return response;
		}

		@SuppressLint("SimpleDateFormat")
		@Override
		protected void onPostExecute(String result) {

			String refreshToken = null;
			String accessToken = null;
			int timeToExpireSecond = 3000;
			IdentityProxy identityProxy = IdentityProxy.getInstance();
			try {
				JSONObject response = new JSONObject(result);

				if (responseCode != null && responseCode.equals(Constants.REQUEST_SUCCESSFUL)) {
					refreshToken = response.getString(Constants.REFRESH_TOKEN);
					accessToken = response.getString(Constants.ACCESS_TOKEN);
					timeToExpireSecond =
							Integer.parseInt(response.getString(Constants.EXPIRE_LABEL));

					token.setRefreshToken(refreshToken);
					token.setAccessToken(accessToken);

					SharedPreferences mainPref =
							IdentityProxy.getInstance()
							             .getContext()
							             .getSharedPreferences(Constants.APPLICATION_PACKAGE,
							                                   Context.MODE_PRIVATE);
					Editor editor = mainPref.edit();
					editor.putString(Constants.REFRESH_TOKEN, refreshToken);
					editor.putString(Constants.ACCESS_TOKEN, accessToken);

					Date date = new Date();
					long expiresIN = date.getTime() + (timeToExpireSecond * 1000);
					Date expireDate = new Date(expiresIN);
					String strDate = dateFormat.format(expireDate);
					token.setDate(strDate);
					editor.putString(Constants.DATE_LABEL, strDate);
					editor.commit();

					identityProxy
							.receiveNewAccessToken(responseCode, Constants.SUCCESS_RESPONSE, token);

				} else if (responseCode != null) {
					JSONObject responseBody = new JSONObject(result);
					String errorDescription =
							responseBody.getString(Constants.ERROR_DESCRIPTION_LABEL);
					identityProxy.receiveNewAccessToken(responseCode, errorDescription, token);
				}
			} catch (JSONException e) {
				identityProxy.receiveNewAccessToken(responseCode, null, token);
				Log.e(TAG, "Invalid JSON." + e);
			}
		}
	}
}
