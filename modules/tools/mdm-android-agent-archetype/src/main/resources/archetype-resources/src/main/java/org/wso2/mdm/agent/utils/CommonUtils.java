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
package org.wso2.mdm.agent.utils;

import org.json.JSONObject;
import org.wso2.mdm.agent.R;
import org.wso2.mdm.agent.proxy.APIController;
import org.wso2.mdm.agent.proxy.interfaces.APIResultCallBack;
import org.wso2.mdm.agent.proxy.utils.Constants.HTTP_METHODS;
import org.wso2.mdm.agent.proxy.beans.EndPointInfo;
import org.wso2.mdm.agent.services.AgentDeviceAdminReceiver;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class CommonUtils {

	public static String TAG = CommonUtils.class.getSimpleName();

	/**
	 * Calls the secured API.
	 * @param context           -The Activity which calls an API.
	 * @param serverUrl         -The server url.
	 * @param endpoint          -The API endpoint.
	 * @param apiVersion        -The API version.
	 * @param methodType        -The method type.
	 * @param apiResultCallBack -The API result call back object.
	 * @param requestCode       -The request code.
	 */
	public static void callSecuredAPI(Context context, String endpoint, HTTP_METHODS methodType,
	                                  JSONObject requestParams,
	                                  APIResultCallBack apiResultCallBack, int requestCode) {

		EndPointInfo apiUtilities = new EndPointInfo();
		apiUtilities.setEndPoint(endpoint);
		apiUtilities.setHttpMethod(methodType);
		if (requestParams != null) {
			apiUtilities.setRequestParams(requestParams);
		}
		APIController apiController = new APIController();
		String clientKey =
				Preference.getString(context,
				                     context.getResources()
				                            .getString(R.string.shared_pref_client_id)
				);
		String clientSecret =
				Preference.getString(context,
				                     context.getResources()
				                            .getString(R.string.shared_pref_client_secret)
				);
		if (clientKey!=null && !clientKey.isEmpty() && !clientSecret.isEmpty()) {
			apiController.setClientDetails(clientKey, clientSecret);
		}
		apiController.invokeAPI(apiUtilities, apiResultCallBack, requestCode,
		                        context.getApplicationContext());
	}

	/**
	 * Clear application data.
	 * @param context - Application context.
	 */
	public static void clearAppData(Context context) {
		DevicePolicyManager devicePolicyManager;
		ComponentName demoDeviceAdmin;
		Resources resources = context.getResources();

		devicePolicyManager =
				(DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		demoDeviceAdmin = new ComponentName(context, AgentDeviceAdminReceiver.class);
		SharedPreferences mainPref =
				context.getSharedPreferences(context.getResources()
				                                    .getString(R.string.shared_pref_package),
				                             Context.MODE_PRIVATE
				);
		
		Editor editor = mainPref.edit();
		editor.putString(context.getResources().getString(R.string.shared_pref_policy),
		                 resources.getString(R.string.shared_pref_default_string));
		editor.putString(context.getResources().getString(R.string.shared_pref_isagreed),
		                 resources.getString(R.string.shared_pref_device_active));
		editor.putString(context.getResources().getString(R.string.shared_pref_regId),
		                 resources.getString(R.string.shared_pref_default_string));
		editor.putString(context.getResources().getString(R.string.shared_pref_registered),
		                 resources.getString(R.string.shared_pref_device_active));
		editor.putString(context.getResources().getString(R.string.shared_pref_ip),
		                 resources.getString(R.string.shared_pref_default_string));
		editor.putString(context.getResources().getString(R.string.shared_pref_sender_id),
		                 resources.getString(R.string.shared_pref_default_string));
		editor.putString(context.getResources().getString(R.string.shared_pref_eula),
		                 resources.getString(R.string.shared_pref_default_string));
		editor.commit();
		
		devicePolicyManager.removeActiveAdmin(demoDeviceAdmin);
	}
	
	/**
	 * Returns network availability status.
	 * @param context - Application context.
	 * @return - Network availability status.
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager =
				(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		if (info == null) {
			return false;
		}
		return info.isConnected();
	}

}
