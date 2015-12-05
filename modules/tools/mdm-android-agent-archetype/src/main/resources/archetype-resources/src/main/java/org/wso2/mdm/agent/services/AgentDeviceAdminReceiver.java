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

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.mdm.agent.R;
import org.wso2.mdm.agent.proxy.interfaces.APIResultCallBack;
import org.wso2.mdm.agent.proxy.utils.Constants.HTTP_METHODS;
import org.wso2.mdm.agent.utils.Constants;
import org.wso2.mdm.agent.utils.Preference;
import org.wso2.mdm.agent.utils.CommonUtils;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.widget.Toast;

/**
 * This is the component that is responsible for actual device administration.
 * It becomes the receiver when a policy is applied. It is important that we
 * subclass DeviceAdminReceiver class here and to implement its only required
 * method onEnabled().
 */
public class AgentDeviceAdminReceiver extends DeviceAdminReceiver implements APIResultCallBack {
	private static final String TAG = AgentDeviceAdminReceiver.class.getName();
	private String regId;

	/**
	 * Called when this application is approved to be a device administrator.
	 */
	@Override
	public void onEnabled(Context context, Intent intent) {
		super.onEnabled(context, intent);
		Resources resources = context.getResources();
		Preference.putString(context,
		                     context.getResources().getString(R.string.shared_pref_device_active),
		                     resources.getString(R.string.shared_pref_reg_success));

		MessageProcessor processor = new MessageProcessor(context);
		processor.getMessages();
		
		Toast.makeText(context, R.string.device_admin_enabled,
		               Toast.LENGTH_LONG).show();
		LocalNotification.startPolling(context);
	}

	/**
	 * Called when this application is no longer the device administrator.
	 */
	@Override
	public void onDisabled(Context context, Intent intent) {
		super.onDisabled(context, intent);
		Toast.makeText(context, R.string.device_admin_disabled,
		               Toast.LENGTH_LONG).show();
		regId = Preference
				.getString(context, context.getResources().getString(R.string.shared_pref_regId));

		if (regId != null && !regId.isEmpty()) {
			startUnRegistration(context);
		}

	}

	/**
	 * Start un-registration process.
	 * @param context - Application context.
	 */
	public void startUnRegistration(Context context) {
		String regId = Preference.getString(context, context
				.getResources().getString(R.string.shared_pref_regId));

		JSONObject requestParams = new JSONObject();
		try {
			requestParams.put(context.getResources().getString(R.string.shared_pref_regId), regId);
		} catch (JSONException e) {
			Log.e(TAG, "Registration ID not retrieved." + e.toString());
		}
		
		CommonUtils.clearAppData(context);
		CommonUtils.callSecuredAPI(context,
		                           Constants.UNREGISTER_ENDPOINT,
		                           HTTP_METHODS.POST, requestParams,
		                           AgentDeviceAdminReceiver.this,
		                           Constants.UNREGISTER_REQUEST_CODE);
	}

	@Override
	public void onPasswordChanged(Context context, Intent intent) {
		super.onPasswordChanged(context, intent);
		if (Constants.DEBUG_MODE_ENABLED) { 
			Log.d(TAG, "onPasswordChanged.");
		}
	}

	@Override
	public void onPasswordFailed(Context context, Intent intent) {
		super.onPasswordFailed(context, intent);
		if (Constants.DEBUG_MODE_ENABLED) {
			Log.d(TAG, "onPasswordFailed.");
		}
	}

	@Override
	public void onPasswordSucceeded(Context context, Intent intent) {
		super.onPasswordSucceeded(context, intent);
		if (Constants.DEBUG_MODE_ENABLED) {
			Log.d(TAG, "onPasswordSucceeded.");
		}
	}

	@Override
	public void onReceiveAPIResult(Map<String, String> arg0, int arg1) {
		if (Constants.DEBUG_MODE_ENABLED) {
			Log.d(TAG, "Unregistered." + arg0.toString());
		}
	}

}
