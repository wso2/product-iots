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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.mdm.agent.R;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

public class BuildResultPayload {
	private JSONArray operationListResult;
	private JSONObject result;
	private Resources resources;
	private String TAG = BuildResultPayload.class.getName();
	
	public BuildResultPayload(Context context){
		this.resources = context.getResources();
		this.operationListResult = new JSONArray();
		this.result = new JSONObject();
	}
	
	/**
	 * Add operation results to the array to be returned.
	 * @param code     - Operation code.
	 */
	public void build(String code) {
		try {
			result.put(resources.getString(R.string.intent_extra_status),
				           resources.getString(R.string.shared_pref_default_status));
			
			result.put(resources.getString(R.string.intent_extra_code), code);
			
			operationListResult.put(result);
		} catch (JSONException e) {
			Log.e(TAG, "Invalid JSON format." + e.toString());
		}
	}
	
	/**
	 * Add operation results to the array to be returned.
	 * @param code     - Operation code.
	 * @param data     - Data JSON object.
	 */
	public void build(String code, JSONObject data) {
		try {
			result.put(resources.getString(R.string.intent_extra_status),
				          resources.getString(R.string.shared_pref_default_status));

			result.put(resources.getString(R.string.intent_extra_code), code);
			
			if (data != null) {
				result.put(resources.getString(R.string.intent_extra_data), data);
			}
			
			operationListResult.put(result);
		} catch (JSONException e) {
			Log.e(TAG, "Invalid JSON format." + e.toString());
		}
	}
	
	/**
	 * Add operation results to the array to be returned.
	 * @param code     - Operation code.
	 * @param dataList - Data JSON Array.
	 */
	public void build(String code, JSONArray dataList) {
		try {
			result.put(resources.getString(R.string.intent_extra_status),
			           	resources.getString(R.string.shared_pref_default_status));
			
			result.put(resources.getString(R.string.intent_extra_code), code);

			if (dataList != null) {
				result.put(resources.getString(R.string.intent_extra_data), dataList);
			}
			
			operationListResult.put(result);
		} catch (JSONException e) {
			Log.e(TAG, "Invalid JSON format." + e.toString());
		}
	}
	
	/**
	 * Add operation results to the array to be returned.
	 * @param code     - Operation code.
	 * @param customStatus - Operation status.
	 */
	public void build(String code, String customStatus) {
		try {
			if (customStatus != null) {
				result.put(resources.getString(R.string.intent_extra_status), customStatus);
			} else {
				result.put(resources.getString(R.string.intent_extra_status),
				           resources.getString(R.string.shared_pref_default_status));
			}
			
			result.put(resources.getString(R.string.intent_extra_code), code);
			
			operationListResult.put(result);
		} catch (JSONException e) {
			Log.e(TAG, "Invalid JSON format." + e.toString());
		}
	}
	
	
	/**
	 * Return final results payload.
	 * @return - Final payload.
	 */
	public JSONArray getResultPayload(){
		return this.operationListResult;
	}
}
