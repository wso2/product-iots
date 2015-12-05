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

import java.util.List;

import org.wso2.mdm.agent.utils.Constants;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WiFiConfig {
	private static ConnectivityManager connectivityManager;
	private WifiManager wifiManager;
	private static final int WIFI_CONFIG_PRIORITY = 40;
	private static final int WIFI_CONFIG_DEFAULT_INDEX = 0;
	private static final String TAG = WiFiConfig.class.getName();

	public WiFiConfig(Context context) {
		wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		connectivityManager =
				(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (!isConnected(context, ConnectivityManager.TYPE_WIFI)) {
			wifiManager.setWifiEnabled(true);
		}
	}

	/**
	 * Checks whether the WIFI is switched on.
	 * @param context     - Application context.
	 * @param networkType - Network type (WIFI/Data).
	 */
	private static boolean isConnected(Context context, int networkType) {
		NetworkInfo networkInfo = null;
		if (connectivityManager != null) {
			networkInfo = connectivityManager.getNetworkInfo(networkType);
		}
		return networkInfo == null ? false : networkInfo.isConnected();
	}

	/**
	 * Saves a WEP WIFI Configuration Profile.
	 * @param ssid     - WIFI network SSID.
	 * @param password - WIFI network password.
	 */
	public boolean saveWEPConfig(String ssid, String password) {
		WifiConfiguration wifiConfig = new WifiConfiguration();
		wifiConfig.SSID = "\"" + ssid + "\"";
		wifiConfig.hiddenSSID = true;
		wifiConfig.status = WifiConfiguration.Status.DISABLED;
		wifiConfig.priority = WIFI_CONFIG_PRIORITY;
		wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
		wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
		wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
		wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
		wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
		wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
		wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
		wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

		wifiConfig.wepKeys[WIFI_CONFIG_DEFAULT_INDEX] = "\"" + password + "\"";
		wifiConfig.wepTxKeyIndex = WIFI_CONFIG_DEFAULT_INDEX;
		
		wifiManager.setWifiEnabled(true);
		int result = wifiManager.addNetwork(wifiConfig);

		boolean isSaveSuccessful = wifiManager.saveConfiguration();

		boolean isNetworkEnabled = wifiManager.enableNetwork(result, true);
		if (Constants.DEBUG_MODE_ENABLED) {
			Log.d(TAG, "add Network returned." + result);
			Log.d(TAG, "saveConfiguration returned." + isSaveSuccessful);
			Log.d(TAG, "enableNetwork returned." + isNetworkEnabled);
		}

		return isSaveSuccessful;
	}

	/**
	 * Remove WIFI Configuration By SSID.
	 * @param ssid - SSID of the WIFI profile which needs to be removed.
	 */
	public boolean removeWifiConfigurationBySsid(String ssid) {
		List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
		boolean isRemoved = false;
		for (WifiConfiguration configuration : configuredNetworks) {
			if (configuration.SSID.equals(ssid)) {
				wifiManager.removeNetwork(configuration.networkId);
				wifiManager.saveConfiguration();
				isRemoved = true;
				break;
			}
		}

		return isRemoved;
	}

	/**
	 * Find WIFI Configuration By SSID.
	 * @param ssid - SSID of the WIFI profile which needs to be found.
	 */
	public boolean findWifiConfigurationBySsid(String ssid) {
		List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
		boolean isAvailable = false;
		for (WifiConfiguration configuration : configuredNetworks) {
			if (configuration.SSID.equals(ssid)) {
				isAvailable = true;
				break;
			}
		}

		return isAvailable;
	}
}
