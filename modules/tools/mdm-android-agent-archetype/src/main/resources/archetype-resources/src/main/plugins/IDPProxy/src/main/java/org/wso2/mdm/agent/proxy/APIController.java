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

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import org.wso2.mdm.agent.proxy.beans.EndPointInfo;
import org.wso2.mdm.agent.proxy.beans.Token;
import org.wso2.mdm.agent.proxy.interfaces.APIResultCallBack;
import org.wso2.mdm.agent.proxy.interfaces.TokenCallBack;
import org.wso2.mdm.agent.proxy.utils.ServerUtilities;

import java.util.HashMap;
import java.util.Map;

public class APIController implements TokenCallBack {
	private static final String TAG = "APIController";
	private static Token token;
	private static String clientKey, clientSecret;
	private APIResultCallBack apiResultCall;
	private EndPointInfo apiUtilitiesCurrent;

	public void setClientDetails(String clientKey, String clientSecret) {
		APIController.clientKey = clientKey;
		APIController.clientSecret = clientSecret;
	}

	/**
	 * Invoking an API using retrieved token.
	 *
	 * @param apiUtilities      - Server and API end point information.
	 * @param apiResultCallBack - API result callback data.
	 * @param requestCode       - Request code to avoid response complications.
	 * @param context           - Application context.
	 */
	public void invokeAPI(EndPointInfo apiUtilities, APIResultCallBack apiResultCallBack,
	                      int requestCode, Context context) {

		apiResultCall = apiResultCallBack;
		apiUtilitiesCurrent = apiUtilities;

		if (IdentityProxy.getInstance().getContext() == null) {
			IdentityProxy.getInstance().setContext(context);
		}

		IdentityProxy.getInstance().setRequestCode(requestCode);

		IdentityProxy.getInstance().requestToken(IdentityProxy.getInstance().getContext(), this,
		                                         APIController.clientKey,
		                                         APIController.clientSecret);
	}

	@Override
	public void onReceiveTokenResult(Token token, String status) {
		APIController.token = token;
		new NetworkCallTask(apiResultCall).execute(apiUtilitiesCurrent);
	}

	/**
	 * AsyncTask to contact server and access the API with retrieved token.
	 */
	private class NetworkCallTask extends AsyncTask<EndPointInfo, Void, Map<String, String>> {
		APIResultCallBack apiResultCallBack;

		public NetworkCallTask(APIResultCallBack apiResultCallBack) {
			this.apiResultCallBack = apiResultCallBack;
		}

		@Override
		protected Map<String, String> doInBackground(EndPointInfo... params) {
			EndPointInfo apiUtilities = params[0];

			Map<String, String> responseParams = null;
			String accessToken = token.getAccessToken();
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Content-Type", "application/json");
			headers.put("Accept", "*/*");
			headers.put("User-Agent", "Mozilla/5.0 ( compatible ), Android");
			headers.put("Authorization", "Bearer " + accessToken);

			try {
				responseParams = ServerUtilities.postData(apiUtilities, headers);
			} catch (IDPTokenManagerException e) {
				Log.e(TAG, "Failed to contact server." + e);
			}

			return responseParams;
		}

		@Override
		protected void onPostExecute(Map<String, String> result) {
			apiResultCallBack.onReceiveAPIResult(result, IdentityProxy.getInstance()
			                                                          .getRequestCode());
		}
	}

}
