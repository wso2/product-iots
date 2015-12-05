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
import org.wso2.mdm.agent.api.DeviceInfo;

import android.content.Context;
import android.util.Log;

public class BuildDeviceInfoPayload {
	private JSONObject result;
	private DeviceInfo deviceInfo;
	private static final String TAG = BuildDeviceInfoPayload.class.getName();
	private static final String DEVICE_IDENTIFIER = "deviceIdentifier";
	private static final String DEVICE_DESCRIPTION = "description";
	private static final String DEVICE_OWNERSHIP = "ownership";
	private static final String DEVICE_PROPERTY_NAME = "name";
	private static final String DEVICE_PROPERTY_VALUE = "value";
	private static final String DEVICE_PROPERTY_USERNAME = "username";
	private static final String DEVICE_PROPERTY_DESCRIPTION = "device";
	private static final String DEVICE_PROPERTY_IMEI = "imei";
	private static final String DEVICE_PROPERTY_IMSI = "imsi";
	private static final String DEVICE_PROPERTY_MODEL = "model";
	private static final String DEVICE_PROPERTY_VENDOR = "vendor";
	private static final String DEVICE_PROPERTY_OS = "osVersion";
	private static final String DEVICE_PROPERTY_TAG = "properties";
	
	public BuildDeviceInfoPayload(Context context){
		deviceInfo = new DeviceInfo(context.getApplicationContext());
	}
	
	/**
	 * Builds device information payload.
	 * @param type - Device ownership type.
	 * @param username - Current user name.
	 */
	public void build(String type, String username){
		result = new JSONObject();
		
		try{
    		result.put(DEVICE_IDENTIFIER, deviceInfo.getMACAddress());
    		result.put(DEVICE_DESCRIPTION, deviceInfo.getDeviceName());
    		result.put(DEVICE_OWNERSHIP, type);
    		
    		JSONArray properties = new JSONArray();
    		JSONObject property = new JSONObject();
    		property.put(DEVICE_PROPERTY_NAME, DEVICE_PROPERTY_USERNAME);
    		property.put(DEVICE_PROPERTY_VALUE, username);
    		properties.put(property);
    		
    		property = new JSONObject();
    		property.put(DEVICE_PROPERTY_NAME, DEVICE_PROPERTY_DESCRIPTION);
    		property.put(DEVICE_PROPERTY_VALUE, deviceInfo.getDeviceName());
    		properties.put(property);
    		
    		property = new JSONObject();
    		property.put(DEVICE_PROPERTY_NAME, DEVICE_PROPERTY_IMEI);
    		property.put(DEVICE_PROPERTY_VALUE, deviceInfo.getDeviceId());
    		properties.put(property);
    		
    		property = new JSONObject();
    		property.put(DEVICE_PROPERTY_NAME, DEVICE_PROPERTY_IMSI);
    		property.put(DEVICE_PROPERTY_VALUE, deviceInfo.getIMSINumber());
    		properties.put(property);
    		
    		property = new JSONObject();
    		property.put(DEVICE_PROPERTY_NAME, DEVICE_PROPERTY_MODEL);
    		property.put(DEVICE_PROPERTY_VALUE, deviceInfo.getDeviceModel());
    		properties.put(property);
    		
    		property = new JSONObject();
    		property.put(DEVICE_PROPERTY_NAME, DEVICE_PROPERTY_VENDOR);
    		property.put(DEVICE_PROPERTY_VALUE, deviceInfo.getOsVersion());
    		properties.put(property);
    		
    		property = new JSONObject();
    		property.put(DEVICE_PROPERTY_NAME, DEVICE_PROPERTY_OS);
    		property.put(DEVICE_PROPERTY_VALUE, deviceInfo.getOsVersion());
    		properties.put(property);
    		
    		result.put(DEVICE_PROPERTY_TAG, properties);
		}catch(JSONException e){
			Log.e(TAG, "Invalid object saved in JSON.");
		}
	}
	
	/**
	 * Returns the final payload.
	 * @return - Device info payload.
	 */
	public JSONObject getDeviceInfoPayload(){
		return this.result;
	}
}
