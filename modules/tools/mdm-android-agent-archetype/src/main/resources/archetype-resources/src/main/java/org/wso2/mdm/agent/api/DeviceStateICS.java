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

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.wso2.mdm.agent.interfaces.DeviceState;
import org.wso2.mdm.agent.utils.Response;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.StatFs;

public class DeviceStateICS implements DeviceState{
	private Context context;
	private DeviceInfo info;
	private File dataDirectory;
	private StatFs directoryStatus;
	private static final int DEFAULT_LEVEL = -1;
	private static final float PRECENTAGE_MULTIPLIER = 100.0f;
	private static final int SCALE = 2;
	private static final int MEMORY_NOT_AVAILABLE = 0;
	private static final double GB_DIVIDER = 1073741824;
	private static final double MB_DIVIDER = 1048576;

	public DeviceStateICS(Context context) {
		this.context = context;
		this.info = new DeviceInfo(context);
		this.dataDirectory = Environment.getDataDirectory();
		this.directoryStatus = new StatFs(dataDirectory.getPath());
	}
	
	@Override
	public boolean externalMemoryAvailable() {
		return android.os.Environment.getExternalStorageState()
		                             .equals(android.os.Environment.MEDIA_MOUNTED);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public double getAvailableInternalMemorySize() {
		double blockSize, availableBlocks;
		blockSize = directoryStatus.getBlockSize();
		availableBlocks = directoryStatus.getAvailableBlocks();

		return formatSizeInGb(availableBlocks * blockSize);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public double getTotalInternalMemorySize() {
		double blockSize, totalBlocks;	
		blockSize = directoryStatus.getBlockSize();
		totalBlocks = directoryStatus.getBlockCount();
		
		return formatSizeInGb(totalBlocks * blockSize);
	}

	@SuppressWarnings("deprecation")
	@Override
	public double getAvailableExternalMemorySize() {
		double blockSize, availableBlocks;
		if (externalMemoryAvailable()) {
			blockSize = directoryStatus.getBlockSize();
			availableBlocks = directoryStatus.getAvailableBlocks();
			
			return formatSizeInGb(availableBlocks * blockSize);
		} else {
			return MEMORY_NOT_AVAILABLE;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public double getTotalExternalMemorySize() {
		double blockSize, totalBlocks;
		if (externalMemoryAvailable()) {		
			blockSize = directoryStatus.getBlockSize();
			totalBlocks = directoryStatus.getBlockCount();
			
			return formatSizeInGb(totalBlocks * blockSize);
		} else {
			return MEMORY_NOT_AVAILABLE;
		}
	}

	@Override
	public double formatSizeInGb(double byteValue) {
		double gbValue = (byteValue / GB_DIVIDER);
		BigDecimal roundedValue = new BigDecimal(gbValue).setScale(SCALE, RoundingMode.HALF_EVEN);
		gbValue = roundedValue.doubleValue();
		
		return gbValue;
	}

	@Override
	public double formatSizeInMb(double byteValue) {
		double mbValue = (byteValue / MB_DIVIDER);
		BigDecimal roundedValue = new BigDecimal(mbValue).setScale(SCALE, RoundingMode.HALF_EVEN);
		mbValue = roundedValue.doubleValue();
		
		return mbValue;
	}

	@Override
	public Response evaluateCompatibility() {
		if (!(info.getSdkVersion() >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) &&
				info.isRooted()) {
			return Response.INCOMPATIBLE;

		} else if (info.getSdkVersion() < android.os.Build.VERSION_CODES.FROYO) {
			return Response.INCOMPATIBLE;
		} else if (info.isRooted()) {
			return Response.INCOMPATIBLE;
		}
		
		return Response.COMPATIBLE;
	}

	@Override
	public String getIpAddress() {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		return intToIp(wifiInfo.getIpAddress());
	}

	@Override
	public String intToIp(int ip) {
		return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." +
		       ((ip >> 24) & 0xFF);
	}

	@Override
	public float getBatteryLevel() {
		Intent batteryIntent = context.registerReceiver(null,
		                                                new IntentFilter(
                                                             Intent.ACTION_BATTERY_CHANGED));
		int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, DEFAULT_LEVEL);
		int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, DEFAULT_LEVEL);
		return ((float) level / (float) scale) * PRECENTAGE_MULTIPLIER;
	}
}
