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
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.util.Log;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.mdm.agent.proxy.beans.EndPointInfo;
import org.wso2.mdm.agent.proxy.beans.CredentialInfo;
import org.wso2.mdm.agent.proxy.beans.Token;
import org.wso2.mdm.agent.proxy.interfaces.CallBack;
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
 * After receiving authorization code client application can use this class to
 * obtain access token
 */
public class AccessTokenHandler extends Activity {
	private static final String TAG = "AccessTokenHandler";
	private static final String USERNAME_LABEL = "username";
	private static final String PASSWORD_LABEL = "password";
	private static final String COLON = ":";
	private static final DateFormat dateFormat =
			new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault());
	private CredentialInfo info;

	public AccessTokenHandler(CredentialInfo info, CallBack callBack) {
		this.info = info;
	}

	public void obtainAccessToken() {
		new NetworkCallTask().execute();
	}

	/**
	 * AsyncTask to contact authorization server and get access token, refresh
	 * token as a result
	 */
	private class NetworkCallTask extends AsyncTask<Void, Void, String> {
		private String response = null;
		private String responseCode = null;

		@Override
		protected String doInBackground(Void... arg0) {
			Map<String, String> request_params = new HashMap<String, String>();
			request_params.put(Constants.GRANT_TYPE, Constants.GRANT_TYPE_PASSWORD);
			request_params.put(USERNAME_LABEL, info.getUsername());
			request_params.put(PASSWORD_LABEL, info.getPassword());
			EndPointInfo apiUtilities = new EndPointInfo();
			apiUtilities.setEndPoint(info.getTokenEndPoint());
			apiUtilities.setHttpMethod(HTTP_METHODS.POST);
			apiUtilities.setRequestParamsMap(request_params);
			byte[] credentials = Base64.encodeBase64((info.getClientID() + COLON +
			                                          info.getClientSecret()).getBytes());
			String encodedCredentials = new String(credentials);

			Map<String, String> headers = new HashMap<String, String>();
			String authorizationString = Constants.AUTHORIZATION_MODE + encodedCredentials;
			headers.put(Constants.AUTHORIZATION_HEADER, authorizationString);
			headers.put(Constants.CONTENT_TYPE_HEADER, Constants.DEFAULT_CONTENT_TYPE);

			Map<String, String> responseParams = null;

			try {
				responseParams = ServerUtilities.postDataAPI(apiUtilities, headers);
				response = responseParams.get(Constants.SERVER_RESPONSE_BODY);
				responseCode = responseParams.get(Constants.SERVER_RESPONSE_STATUS);
			} catch (IDPTokenManagerException e) {
				Log.e(TAG, "Failed to contact server." + e);
			}

			return response;
		}

		@SuppressLint("SimpleDateFormat")
		@Override
		protected void onPostExecute(String result) {
			String refreshToken = null;
			String accessToken = null;
			int timeToExpireSecond = 3000;
			try {
				IdentityProxy identityProxy = IdentityProxy.getInstance();

				if (responseCode != null && responseCode.equals(Constants.REQUEST_SUCCESSFUL)) {
					JSONObject response = new JSONObject(result);
					try {
						accessToken = response.getString(Constants.ACCESS_TOKEN);
						refreshToken = response.getString(Constants.REFRESH_TOKEN);
						timeToExpireSecond =
								Integer.parseInt(response.getString(Constants.EXPIRE_LABEL));
						Token token = new Token();
						Date date = new Date();
						String currentDate = dateFormat.format(date);
						token.setDate(currentDate);
						token.setRefreshToken(refreshToken);
						token.setAccessToken(accessToken);
						token.setExpired(false);

						SharedPreferences mainPref =
								IdentityProxy.getInstance()
								             .getContext()
								             .getSharedPreferences(Constants.APPLICATION_PACKAGE,
								                                   Context.MODE_PRIVATE);
						Editor editor = mainPref.edit();
						editor.putString(Constants.ACCESS_TOKEN, accessToken);
						editor.putString(Constants.REFRESH_TOKEN, refreshToken);
						editor.putString(USERNAME_LABEL, info.getUsername());
						long expiresIN = date.getTime() + (timeToExpireSecond * 1000);
						Date expireDate = new Date(expiresIN);
						String strDate = dateFormat.format(expireDate);
						token.setDate(strDate);
						editor.putString(Constants.DATE_LABEL, strDate);
						editor.commit();

						identityProxy.receiveAccessToken(responseCode, Constants.SUCCESS_RESPONSE,
						                                 token);
					} catch (JSONException e) {
						Log.e(TAG, "Invalid JSON format." + e);
					}

				} else if (responseCode != null) {
					if (Constants.INTERNAL_SERVER_ERROR.equals(responseCode)) {
						identityProxy.receiveAccessToken(responseCode, result, null);
					} else {
						JSONObject mainObject = new JSONObject(result);
						String errorDescription =
								mainObject.getString(Constants.ERROR_DESCRIPTION_LABEL);
						identityProxy.receiveAccessToken(responseCode, errorDescription, null);
					}
				}
			} catch (JSONException e) {
				Log.e(TAG, "Invalid JSON." + e);
			}
		}
	}
}
