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
package org.wso2.mdm.agent.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.wso2.mdm.agent.utils.Constants;
import org.wso2.mdm.agent.utils.StreamHandler;

import android.util.Log;

public class ShellExecutor {

	private static final String LOG_TAG = ShellExecutor.class.getName();

	/**
	 * Executes shell commands on the device.
	 * @param shellCommand - Shell command that needs to be executed.
	 * @return - Shell command response.
	 */
	public String executeCommand(String[] shellCommand) {
		Process shellProcess = null;
		String response = null;
		BufferedReader shellInput = null;

		try {
			if (shellCommand != null) {
				shellProcess = Runtime.getRuntime().exec(shellCommand);
			}
		} catch (IOException e) {
			Log.e(LOG_TAG, "Shell command processing failed." + e);
		}

		if(shellProcess!=null){
    		shellInput =
    				new BufferedReader(
    						new InputStreamReader(
    								shellProcess.getInputStream())
    				);
		}

		try {
			while (shellInput!=null && (response = shellInput.readLine()) != null) {
				if (Constants.DEBUG_MODE_ENABLED) {
					Log.d(LOG_TAG, "Shell Executor Result." + response);
				}
			}
		} catch (IOException e) {
			Log.e(LOG_TAG, "Shell input processing failed." + e);
		}finally{
			StreamHandler.closeBufferedReader(shellInput, LOG_TAG);
		}

		return response;
	}

}