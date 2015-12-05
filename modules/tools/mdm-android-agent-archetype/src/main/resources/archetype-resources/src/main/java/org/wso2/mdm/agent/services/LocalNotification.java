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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

/**
 * Local notification is a communication mechanism that essentially,
 * polls to server based on a predefined to retrieve pending data.
 */
public class LocalNotification {
	public static boolean localNoticicationInvoked = false;
	public static int DEFAULT_INTERVAL = 10000;
	public static int DEFAULT_BUFFER = 1000;
	public static int REQUEST_CODE = 0;

	public static void startPolling(Context context) {
		int interval = DEFAULT_INTERVAL;

		long currentTime = SystemClock.elapsedRealtime();
		currentTime += DEFAULT_BUFFER;
		if (localNoticicationInvoked == false) {
			localNoticicationInvoked = true;
			Intent alarm = new Intent(context, AlarmReceiver.class);
			PendingIntent recurringAlarm =
					PendingIntent.getBroadcast(context,
					                           REQUEST_CODE,
					                           alarm,
					                           PendingIntent.FLAG_CANCEL_CURRENT);
			AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			alarms.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, currentTime, interval,
			                    recurringAlarm);
		}
	}
}
