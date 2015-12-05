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
package org.wso2.mdm.agent.parsers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class PayloadParser {
	private static final String LABEL_CODE = "code";
	private static final String LABEL_REG_ID = "regId";
	private static final String LABEL_MESSAGE_ID = "messageId";
	private static final String LABEL_DATA = "data";
	private static final String TAG = PayloadParser.class.getName();

	/**
	 * Parses the input operation payload.
	 * @param inputPayload   - Operation payload to be parsed.
	 * @param registrationId - Device registration ID.
	 * @return - Parsed payload.
	 */
	public String generateReply(JSONArray inputPayload, String registrationId) {
		JSONObject outerJson = new JSONObject();
		JSONArray outerArr = new JSONArray();
		try {
			outerJson.put(LABEL_REG_ID, registrationId);
			outerJson.put(LABEL_DATA, outerArr);
		} catch (JSONException e) {
			Log.e(TAG, "Invalid JSON Exception - RegId." + e.toString());
		}

		for (int i = 0; i < inputPayload.length(); i++) {
			try {
				String code = inputPayload.getJSONObject(i).getString(LABEL_CODE);
				String messageId = inputPayload.getJSONObject(i).getString(LABEL_MESSAGE_ID);
				JSONArray data = inputPayload.getJSONObject(i).getJSONArray(LABEL_DATA);

				JSONObject dataArrContents = new JSONObject();

				dataArrContents.put(LABEL_CODE, code);
				JSONArray innerDataArr = new JSONArray();

				JSONObject innerDataOb = new JSONObject();
				innerDataOb.put(LABEL_MESSAGE_ID, messageId);
				innerDataOb.put(LABEL_DATA, data);
				innerDataArr.put(innerDataOb);

				dataArrContents.put(LABEL_DATA, innerDataArr);

				outerArr.put(dataArrContents);

			} catch (JSONException e) {
				Log.d(TAG, "Invalid JSON Exception - Input payload " + e.toString());
			}

		}

		return outerJson.toString();

	}

}
