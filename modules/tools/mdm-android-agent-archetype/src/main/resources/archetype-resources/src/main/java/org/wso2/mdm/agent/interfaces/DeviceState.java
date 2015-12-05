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
package org.wso2.mdm.agent.interfaces;

import org.wso2.mdm.agent.utils.Response;

public interface DeviceState {
	/**
	 * Returns whether the external memory is available or not.
	 * @return - External memory status.
	 */
	public boolean externalMemoryAvailable();

	/**
	 * Returns the available internal memory size.
	 * @return - Available internal memory size.
	 */
	public double getAvailableInternalMemorySize();

	/**
	 * Returns the total internal memory size.
	 * @return - Total internal memory size.
	 */
	public double getTotalInternalMemorySize();

	/**
	 * Returns the available external memory size.
	 * @return - Available external memory size.
	 */
	public double getAvailableExternalMemorySize();

	/**
	 * Returns the total external memory size.
	 * @return - Total external memory size.
	 */
	public double getTotalExternalMemorySize();

	/**
	 * Returns the string formatted value for the size.
	 * @param byteValue - Memory in bytes.
	 * @return - Memory formatted into GB.
	 */
	public double formatSizeInGb(double byteValue);

	/**
	 * Returns the string formatted value for the size.
	 * @param byteValue
	 *            - Memory in bytes.
	 * @return - Memory formatted into MB.
	 */
	public double formatSizeInMb(double byteValue);

	/**
	 * Returns true if the device is compatible to run the agent.
	 * @return - Device compatibility status.
	 */
	public Response evaluateCompatibility();

	/**
	 * Returns the device IP address.
	 * @return - Device IP address.
	 */
	public String getIpAddress();

	/**
	 * Format the integer IP address and return it as a String.
	 * @param ip - IP address should be passed in as an Integer.
	 * @return - Formatted IP address.
	 */
	public String intToIp(int ip);

	/**
	 * Returns the device battery information.
	 * @return - Battery level.
	 */
	public float getBatteryLevel();
	
}
