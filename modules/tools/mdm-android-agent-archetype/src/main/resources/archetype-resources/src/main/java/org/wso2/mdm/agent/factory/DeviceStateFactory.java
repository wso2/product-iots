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
package org.wso2.mdm.agent.factory;

import org.wso2.mdm.agent.api.DeviceStateICS;
import org.wso2.mdm.agent.api.DeviceStateJB;
import org.wso2.mdm.agent.interfaces.DeviceState;

import android.content.Context;

public class DeviceStateFactory {
	
	/**
	 * Get device state for given SDK version. Creation happens according to the 
	 * device OS version.
	 * @param context - Application context.
	 * @param sdkVersion - Device SDK version.
	 * @return DeviceState API object.
	 */
	public static DeviceState getDeviceState(Context context, int sdkVersion){
		if(sdkVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2){
			return new DeviceStateJB(context);
		}else{
			return new DeviceStateICS(context);
		}
	}
	
}
