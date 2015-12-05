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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import org.wso2.mdm.agent.R;

/**
 * Data retrieval and saving to shared preferences is done here.
 */
public class Preference {
	private static final int DEFAULT_INDEX = 0;

	/**
	 * Put data to shared preferences in private mode.
	 * @param context - The context of activity which is requesting to put data.
	 * @param key     - Used to identify the value.
	 * @param value   - The actual value to be saved.
	 */
	public static void putString(Context context, String key, String value) {
		SharedPreferences mainPref =
				context.getSharedPreferences(context.getResources()
				                                    .getString(R.string.shared_pref_package),
				                             Context.MODE_PRIVATE
				);
		Editor editor = mainPref.edit();
		editor.putString(key, value);
		editor.commit();
	}

	/**
	 * Retrieve data from shared preferences in private mode.
	 * @param context - The context of activity which is requesting to put data.
	 * @param key     - Used to identify the value to to be retrieved.
	 */
	public static String getString(Context context, String key) {
		SharedPreferences mainPref =
				context.getSharedPreferences(context.getResources()
				                                    .getString(R.string.shared_pref_package),
				                             Context.MODE_PRIVATE
				);
		return mainPref.getString(key, null);
	}

	/**
	 * Put data to shared preferences in private mode.
	 * @param context - The context of activity which is requesting to put data.
	 * @param key     - Used to identify the value.
	 * @param value   - The actual value to be saved.
	 */
	public static void putFloat(Context context, String key, float value) {
		SharedPreferences mainPref =
				context.getSharedPreferences(context.getResources()
				                                    .getString(R.string.shared_pref_package),
				                             Context.MODE_PRIVATE
				);
		Editor editor = mainPref.edit();
		editor.putFloat(key, value);
		editor.commit();
	}

	/**
	 * Retrieve data from shared preferences in private mode.
	 * @param context - The context of activity which is requesting to put data.
	 * @param key     - Used to identify the value to to be retrieved.
	 */
	public static float getFloat(Context context, String key) {
		SharedPreferences mainPref =
				context.getSharedPreferences(context.getResources()
				                                    .getString(R.string.shared_pref_package),
				                             Context.MODE_PRIVATE
				);
		return mainPref.getFloat(key, DEFAULT_INDEX);
	}

}
