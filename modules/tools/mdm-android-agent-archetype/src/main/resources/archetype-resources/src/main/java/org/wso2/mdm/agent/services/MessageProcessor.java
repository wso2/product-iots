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
package org.wso2.mdm.agent.services;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.mdm.agent.R;
import org.wso2.mdm.agent.api.DeviceInfo;
import org.wso2.mdm.agent.beans.ServerConfig;
import org.wso2.mdm.agent.proxy.interfaces.APIResultCallBack;
import org.wso2.mdm.agent.proxy.utils.Constants.HTTP_METHODS;
import org.wso2.mdm.agent.utils.Constants;
import org.wso2.mdm.agent.utils.Preference;
import org.wso2.mdm.agent.utils.CommonUtils;

import android.content.Context;
import android.util.Log;

/**
 * Used to coordinate the retrieval and processing of messages from the server.
 */
public class MessageProcessor implements APIResultCallBack {

	private String TAG = MessageProcessor.class.getSimpleName();
	private Context context;
	private String deviceId;
	private static final String DEVICE_ID_PREFERENCE_KEY = "deviceId";
	private static final String ENCODE_METHOD_UTF_8 = "utf-8";

	/**
	 * Local notification message handler.
	 * @param context Context of the application.
	 */
	public MessageProcessor(Context context) {
		this.context = context;

		deviceId = Preference.getString(context, DEVICE_ID_PREFERENCE_KEY);
		if (deviceId == null) {
			DeviceInfo deviceInfo = new DeviceInfo(context.getApplicationContext());
			deviceId = deviceInfo.getMACAddress();
			Preference.putString(context, DEVICE_ID_PREFERENCE_KEY, deviceId);
		}
	}

	/**
	 * @param response Response received from the server that needs to be processed
	 *                 and applied to the device.
	 */
	public void performOperation(String response) {
		try {
			JSONArray operations = new JSONArray(response);
			for (int i = 0; i < operations.length(); i++) {
				String featureCode = operations.getJSONObject(i).getString(Constants.CODE);
				String properties = operations.getJSONObject(i).getString(Constants.PROPERTIES);
				Operation operation = new Operation(context.getApplicationContext());
				operation.doTask(featureCode, properties);
			}
		} catch (JSONException e) {
			Log.e(TAG, "JSON Exception in response String." + e.toString());
		}

	}

	/**
	 * Call the message retrieval end point of the server to get messages
	 * pending.
	 */
	public void getMessages() {
		String ipSaved =
				Preference.getString(context.getApplicationContext(),
				                     context.getResources().getString(R.string.shared_pref_ip));
		ServerConfig utils = new ServerConfig();
		utils.setServerIP(ipSaved);
		String deviceIdentifier = null;
		
		try {
			deviceIdentifier = URLEncoder.encode(deviceId, ENCODE_METHOD_UTF_8);
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Unsupport encoding." + e.toString());
		}
		
		CommonUtils.callSecuredAPI(context, utils.getAPIServerURL() +
		                                    Constants.NOTIFICATION_ENDPOINT + File.separator +
		                                    deviceIdentifier,
		                           HTTP_METHODS.GET, new JSONObject(), MessageProcessor.this,
		                           Constants.NOTIFICATION_REQUEST_CODE
		);
	}

	@Override
	public void onReceiveAPIResult(Map<String, String> result, int requestCode) {
		String responseStatus;
		String response;
		if (requestCode == Constants.NOTIFICATION_REQUEST_CODE) {
			if (result != null) {
				responseStatus = result.get(Constants.STATUS_KEY);
				if (responseStatus != null &&
				    responseStatus.equals(Constants.REQUEST_SUCCESSFUL)) {
					response = result.get(Constants.RESPONSE);
					if (response != null && !response.isEmpty()) {
						if (Constants.DEBUG_MODE_ENABLED) { 
							Log.d(TAG, "onReceiveAPIResult." + response);
						}
						performOperation(response);
					}
				}

			}
		}

	}

}
