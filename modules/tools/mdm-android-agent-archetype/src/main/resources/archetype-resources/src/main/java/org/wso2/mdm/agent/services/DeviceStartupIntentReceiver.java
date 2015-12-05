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

import java.util.Locale;

import org.wso2.mdm.agent.R;
import org.wso2.mdm.agent.utils.Preference;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.SystemClock;

public class DeviceStartupIntentReceiver extends BroadcastReceiver {
	private static final String NOTIFIER_MODE = "LOCAL";
	private static final int DEFAULT_TIME_MILLISECONDS = 1000;
	private static final int DEFAULT_REQUEST_CODE = 0;
	private Resources resources;

	@Override
	public void onReceive(final Context context, Intent intent) {
		setRecurringAlarm(context.getApplicationContext());
	}

	/**
	 * Initiates device notifier on device startup.
	 * @param context - Application context.
	 */
	private void setRecurringAlarm(Context context) {
		this.resources = context.getApplicationContext().getResources();
		String mode =
				Preference
						.getString(context, resources.getString(R.string.shared_pref_message_mode));
		Float interval =
				(Float) Preference
						.getFloat(context, resources.getString(R.string.shared_pref_interval));

		if (mode.trim().toUpperCase(Locale.ENGLISH).equals(NOTIFIER_MODE)) {
			long startTime = SystemClock.elapsedRealtime() + DEFAULT_TIME_MILLISECONDS;

			Intent alarmIntent = new Intent(context, AlarmReceiver.class);
			PendingIntent recurringAlarmIntent =
					PendingIntent.getBroadcast(context,
					                           DEFAULT_REQUEST_CODE,
					                           alarmIntent,
					                           PendingIntent.FLAG_CANCEL_CURRENT);
			AlarmManager alarmManager =
					(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, startTime,
			                          interval.intValue(), recurringAlarmIntent);
		}
	}
}