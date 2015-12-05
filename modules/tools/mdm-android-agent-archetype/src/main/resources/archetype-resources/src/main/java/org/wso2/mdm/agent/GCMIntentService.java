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
package org.wso2.mdm.agent;

import java.util.Locale;

import org.wso2.mdm.agent.R;
import org.wso2.mdm.agent.services.MessageProcessor;
import org.wso2.mdm.agent.utils.Constants;
import org.wso2.mdm.agent.utils.Preference;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {
	private static final String MESSAGE_MODE = "GCM";

	private static final String TAG = "GCMIntentService";

	public GCMIntentService() {
		super(Constants.SENDER_ID);
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		if (Constants.DEBUG_MODE_ENABLED) {
			Log.i(TAG, "Device registered." + registrationId);
		}

		Preference.putString(context, getResources().getString(R.string.shared_pref_regId),
		                     registrationId);
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
		if (Constants.DEBUG_MODE_ENABLED) {
			Log.i(TAG, "Device unregistered.");
		}
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		String mode =
				Preference.getString(context,
				                     context.getResources()
				                            .getString(R.string.shared_pref_message_mode)
				);
		
		if (mode.trim().toUpperCase(Locale.getDefault()).equals(MESSAGE_MODE)) {
			MessageProcessor msg = new MessageProcessor(context);
			msg.getMessages();
		}
	}

	@Override
	protected void onDeletedMessages(Context context, int total) {
		if (Constants.DEBUG_MODE_ENABLED) {
			Log.i(TAG, "Received deleted messages notification.");
		}
	}

	@Override
	public void onError(Context context, String errorId) {
		if (Constants.DEBUG_MODE_ENABLED) {
			Log.i(TAG, "Received error." + errorId);
		}
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		if (Constants.DEBUG_MODE_ENABLED) {
			Log.i(TAG, "Received recoverable error." + errorId);
		}

		return super.onRecoverableError(context, errorId);
	}
}
