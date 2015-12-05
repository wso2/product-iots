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

import java.util.ArrayList;
import java.util.Map;

import org.wso2.mdm.agent.R;
import org.wso2.mdm.agent.AlertActivity;
import org.wso2.mdm.agent.ServerDetails;
import org.wso2.mdm.agent.api.ApplicationManager;
import org.wso2.mdm.agent.api.DeviceInfo;
import org.wso2.mdm.agent.api.GPSTracker;
import org.wso2.mdm.agent.api.WiFiConfig;
import org.wso2.mdm.agent.beans.DeviceAppInfo;
import org.wso2.mdm.agent.factory.DeviceStateFactory;
import org.wso2.mdm.agent.interfaces.DeviceState;
import org.wso2.mdm.agent.utils.Constants;
import org.wso2.mdm.agent.utils.Preference;
import org.wso2.mdm.agent.utils.CommonUtils;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class Operation {

	private Context context;
	private DevicePolicyManager devicePolicyManager;
	private ApplicationManager appList;
	private Resources resources;
	private BuildResultPayload resultBuilder;
	private static final String TAG = "Operation Handler";
	private static final String MEMORY_INFO_TAG_TOTAL = "total";
	private static final String MEMORY_INFO_TAG_AVAILABLE = "available";
	private static final String MEMORY_INFO_TAG_INTERNAL = "internal_memory";
	private static final String MEMORY_INFO_TAG_EXTERNAL = "external_memory";
	private static final String BATTERY_INFO_TAG_LEVEL = "level";
	private static final String BATTERY_INFO_TAG = "battery";
	private static final String LOCATION_INFO_TAG_LONGITUDE = "longitude";
	private static final String LOCATION_INFO_TAG_LATITUDE = "latitude";
	private static final String LOCATION_INFO_TAG = "location_obj";
	private static final String NETWORK_OPERATOR_TAG = "operator";
	private static final String APP_INFO_TAG_NAME = "name";
	private static final String APP_INFO_TAG_PACKAGE = "package";
	private static final String APP_INFO_TAG_ICON = "icon";
	private static final int PRE_WIPE_WAIT_TIME = 4000;
	private static final int ACTIVATION_REQUEST = 47;
	private static final int DEFAULT_PASSWORD_LENGTH = 0;
	private static final int DEFAULT_VOLUME = 0;
	private static final int DEFAULT_FLAG = 0;
	private static final int DEFAULT_PASSWORD_MIN_LENGTH = 3;
	private static final long DAY_MILLISECONDS_MULTIPLIER = 24 * 60 * 60 * 1000;
	private Map<String, String> bundleParams;

	public Operation(Context context) {
		this.context = context;
		this.resources = context.getResources();
		this.devicePolicyManager =
				(DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		this.appList = new ApplicationManager(context.getApplicationContext());
		this.resultBuilder = new BuildResultPayload(context.getApplicationContext());
	}

	/**
	 * Executes device management operations on the device.
	 * @param operationCode - Device management operation code.
	 * @param operationData - Operation data if required(ex:WIFI configuration).
	 * @param requestMode   - Request mode (GCM/Local).
	 * @return Operation/Policy status list.
	 */
	public JSONArray doTask(String operationCode, String operationData) {

		switch (operationCode) {
			case Constants.OPERATION_DEVICE_INFO:
				getDeviceInfo(operationCode);
				break;
			case Constants.OPERATION_DEVICE_LOCATION:
				getLocationInfo(operationCode);
				break;
			case Constants.OPERATION_GET_APPLICATION_LIST:
				getApplicationList(operationCode);
				break;
			case Constants.OPERATION_LOCK_DEVICE:
				lockDevice(operationCode);
				break;
			case Constants.OPERATION_WIPE_DATA:
				wipeDevice(operationCode, operationData);
				break;
			case Constants.OPERATION_CLEAR_PASSWORD:
				clearPassword(operationCode);
				break;
			case Constants.OPERATION_NOTIFICATION:
				displayNotification(operationCode);
				break;
			case Constants.OPERATION_WIFI:
				configureWifi(operationCode, operationData);
				break;
			case Constants.OPERATION_DISABLE_CAMERA:
				disableCamera(operationCode, operationData);
				break;
			case Constants.OPERATION_INSTALL_APPLICATION:
				installAppBundle(operationCode, operationData);
				break;
			case Constants.OPERATION_INSTALL_APPLICATION_BUNDLE:
				installAppBundle(operationCode, operationData);
				break;
			case Constants.OPERATION_UNINSTALL_APPLICATION:
				uninstallApplication(operationCode, operationData);
				break;
			case Constants.OPERATION_ENCRYPT_STORAGE:
				encryptStorage(operationCode, operationData);
				break;
			case Constants.OPERATION_RING:
				ringDevice(operationCode);
				break;
			case Constants.OPERATION_MUTE:
				muteDevice(operationCode);
				break;
			case Constants.OPERATION_WEBCLIP:
				createWebClip(operationCode, operationData);
				break;
			case Constants.OPERATION_PASSWORD_POLICY:
				setPasswordPolicy(operationCode, operationData);
				break;
			case Constants.OPERATION_INSTALL_GOOGLE_APP:
				installGooglePlayApp(operationCode, operationData);
				break;
			case Constants.OPERATION_CHANGE_LOCK_CODE:
				changeLockCode(operationCode, operationData);
				break;
			case Constants.OPERATION_POLICY_BUNDLE:
				setPolicyBundle(operationCode);
				break;
			case Constants.OPERATION_ENTERPRISE_WIPE_DATA:
				enterpriseWipe(operationCode);
				break;
			case Constants.OPERATION_BLACKLIST_APPS:
				blacklistApps(operationCode, operationData);
				break;
			default:
				Log.e(TAG, "Invalid operation code receieved");
				break;
		}

		return resultBuilder.getResultPayload();
	}

	/**
	 * Retrieve device information.
	 * @param code - Operation code.
	 */
	public void getDeviceInfo(String code) {
		DeviceInfo deviceInfo = new DeviceInfo(context.getApplicationContext());
		DeviceState phoneState = DeviceStateFactory.getDeviceState(context.getApplicationContext(),
	                                                           				deviceInfo.getSdkVersion());
		GPSTracker gps = new GPSTracker(context.getApplicationContext());
		JSONObject result = new JSONObject();
		JSONObject batteryInfo = new JSONObject();
		JSONObject internalMemoryInfo = new JSONObject();
		JSONObject externalMemoryInfo = new JSONObject();
		JSONObject locationInfo = new JSONObject();
		double latitude;
		double longitude;
		
		try {
			latitude = gps.getLatitude();
			longitude = gps.getLongitude();
			int batteryLevel = (int) Math.floor(phoneState.getBatteryLevel());
			batteryInfo.put(BATTERY_INFO_TAG_LEVEL, batteryLevel);

			internalMemoryInfo.put(MEMORY_INFO_TAG_TOTAL, phoneState.getTotalInternalMemorySize());
			internalMemoryInfo.put(MEMORY_INFO_TAG_AVAILABLE,
			                       phoneState.getAvailableInternalMemorySize());
			externalMemoryInfo.put(MEMORY_INFO_TAG_TOTAL, phoneState.getTotalExternalMemorySize());
			externalMemoryInfo.put(MEMORY_INFO_TAG_AVAILABLE,
			                       phoneState.getAvailableExternalMemorySize());
			locationInfo.put(LOCATION_INFO_TAG_LATITUDE, latitude);
			locationInfo.put(LOCATION_INFO_TAG_LONGITUDE, longitude);

			result.put(BATTERY_INFO_TAG, batteryInfo);
			result.put(MEMORY_INFO_TAG_INTERNAL, internalMemoryInfo);
			result.put(MEMORY_INFO_TAG_EXTERNAL, externalMemoryInfo);
			if (latitude != 0 && longitude != 0) {
				result.put(LOCATION_INFO_TAG, locationInfo);
			}
			result.put(NETWORK_OPERATOR_TAG, deviceInfo.getNetworkOperatorName());

			resultBuilder.build(code, result);

		} catch (JSONException e) {
			Log.e(TAG, "Invalid JSON format." + e);
		}
	}

	/**
	 * Retrieve device information.
	 * @param code - Operation code.
	 */
	public void getLocationInfo(String code) {
		double latitude;
		double longitude;
		JSONObject result = new JSONObject();
		GPSTracker gps = new GPSTracker(context);
		
		try {
			latitude = gps.getLatitude();
			longitude = gps.getLongitude();
			result.put(LOCATION_INFO_TAG_LATITUDE, latitude);
			result.put(LOCATION_INFO_TAG_LONGITUDE, longitude);
			resultBuilder.build(code, result);
		} catch (JSONException e) {
			Log.e(TAG, "Invalid JSON format." + e);
		}
	}

	/**
	 * Retrieve device application information.
	 * @param code - Operation code.
	 */
	public void getApplicationList(String code) {
		ArrayList<DeviceAppInfo> apps = appList.getInstalledApps();

		JSONArray result = new JSONArray();
		int size = apps.size();
		
		for (int i = 0; i < size; i++) {
			JSONObject app = new JSONObject();
			try {
				app.put(APP_INFO_TAG_NAME, Uri.encode(apps.get(i).getAppname()));
				app.put(APP_INFO_TAG_PACKAGE, apps.get(i).getPackagename());
				app.put(APP_INFO_TAG_ICON, apps.get(i).getIcon());
				result.put(app);
			} catch (JSONException e) {
				Log.e(TAG, "Invalid JSON format." + e);
			}
		}
		
		resultBuilder.build(code, result);
	}

	/**
	 * Lock the device.
	 * @param code        - Operation code.
	 */
	public void lockDevice(String code) {
		resultBuilder.build(code);
		devicePolicyManager.lockNow();
	}
	
	/**
	 * Ring the device.
	 * @param code        - Operation code.
	 */
	public void ringDevice(String code) {
		resultBuilder.build(code);
		devicePolicyManager.lockNow();
	}

	/**
	 * Wipe the device.
	 * @param code - Operation code.
	 * @param data - Data required by the operation(PIN).
	 */
	public void wipeDevice(String code, String data) {
		String inputPin;
		String savedPin =
				Preference.getString(context,
				                     resources.getString(R.string.shared_pref_pin));

		try {
			JSONObject wipeKey = new JSONObject(data);
			inputPin = (String) wipeKey.get(resources.getString(R.string.shared_pref_pin));
			String status;
			if (inputPin.trim().equals(savedPin.trim())) {
				status = resources.getString(R.string.shared_pref_default_status);
			} else {
				status = resources.getString(R.string.shared_pref_false_status);
			}

			resultBuilder.build(code, status);

			if (inputPin.trim().equals(savedPin.trim())) {
				Toast.makeText(context, resources.getString(R.string.toast_message_wipe),
				               Toast.LENGTH_LONG).show();
				try {
					Thread.sleep(PRE_WIPE_WAIT_TIME);
				} catch (InterruptedException e) {
					Log.e(TAG, "Wipe pause interrupted :" + e.toString());
				}
				devicePolicyManager.wipeData(ACTIVATION_REQUEST);
			} else {
				Toast.makeText(context, resources.getString(R.string.toast_message_wipe_failed),
				               Toast.LENGTH_LONG).show();
			}
		} catch (JSONException e) {
			Log.e(TAG, "Invalid JSON format." + e);
		}
	}

	/**
	 * Clear device password.
	 * @param code        - Operation code.
	 * @param requestMode - Request mode(Normal mode or policy bundle mode).
	 */
	public void clearPassword(String code) {
		ComponentName demoDeviceAdmin = new ComponentName(context, AgentDeviceAdminReceiver.class);
		resultBuilder.build(code);

		devicePolicyManager.setPasswordQuality(demoDeviceAdmin,
		                                       DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
		devicePolicyManager.setPasswordMinimumLength(demoDeviceAdmin, DEFAULT_PASSWORD_LENGTH);
		devicePolicyManager.resetPassword(resources.getString(R.string.shared_pref_default_string),
		                                  DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
		devicePolicyManager.lockNow();
		devicePolicyManager.setPasswordQuality(demoDeviceAdmin,
		                                       DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
	}

	/**
	 * Display notification.
	 * @param code - Operation code.
	 */
	public void displayNotification(String code) {
		String notification = null;

		try {
			JSONObject inputData = new JSONObject(code);
			if (inputData.get(resources.getString(R.string.intent_extra_notification)).toString() !=
			    null &&
			    !inputData.get(resources.getString(R.string.intent_extra_notification)).toString()
			              .isEmpty()) {
				notification =
						inputData.get(resources.getString(R.string.intent_extra_notification))
						         .toString();
			}

			resultBuilder.build(code);

			if (notification != null) {
				Intent intent = new Intent(context, AlertActivity.class);
				intent.putExtra(resources.getString(R.string.intent_extra_message), notification);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP |
				                Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}
		} catch (JSONException e) {
			Log.e(TAG, "Invalid JSON format." + e);
		}
	}

	/**
	 * Configure device WIFI profile.
	 * @param code        - Operation code.
	 * @param data        - Data required(SSID, Password).
	 * @param requestMode - Request mode(Normal mode or policy bundle mode).
	 */
	public void configureWifi(String code, String data) {
		boolean wifistatus = false;
		String ssid = null;
		String password = null;
		
		try {
			JSONObject wifiData = new JSONObject(data);
			if (!wifiData.isNull(resources.getString(R.string.intent_extra_ssid))) {
				ssid = (String) wifiData.get(resources.getString(R.string.intent_extra_ssid));
			}
			if (!wifiData.isNull(resources.getString(R.string.intent_extra_password))) {
				password =
						(String) wifiData.get(resources.getString(R.string.intent_extra_password));
			}
		} catch (JSONException e) {
			Log.e(TAG, "Invalid JSON format " + e.toString());
		}

		WiFiConfig config = new WiFiConfig(context.getApplicationContext());

		wifistatus = config.saveWEPConfig(ssid, password);

		String status = null;
		if (wifistatus) {
			status = resources.getString(R.string.shared_pref_default_status);
		} else {
			status = resources.getString(R.string.shared_pref_false_status);
		}

		resultBuilder.build(code, status);

	}

	/**
	 * Disable/Enable device camera.
	 * @param code        - Operation code.
	 * @param data        - Data required(Camera enable/disable switch).
	 * @param requestMode - Request mode(Normal mode or policy bundle mode).
	 */
	public void disableCamera(String code, String data) {
		boolean camFunc = false;
		try {
			JSONObject inputData = new JSONObject(data);
			if (!inputData.isNull(resources.getString(R.string.intent_extra_function)) &&
			    inputData.get(resources.getString(R.string.intent_extra_function)).toString()
			             .equalsIgnoreCase(resources.getString(R.string.intent_extra_enable))) {
				camFunc = false;
			} else if (!inputData.isNull(resources.getString(R.string.intent_extra_function)) &&
			           inputData.get(resources.getString(R.string.intent_extra_function)).toString()
			                    .equalsIgnoreCase(
					                    resources.getString(R.string.intent_extra_disable))) {
				camFunc = true;
			} else if (!inputData.isNull(resources.getString(R.string.intent_extra_function))) {
				camFunc =
						Boolean.parseBoolean(
								inputData.get(resources.getString(R.string.intent_extra_function))
								         .toString());
			}

			ComponentName cameraAdmin = new ComponentName(context, AgentDeviceAdminReceiver.class);

			resultBuilder.build(code);

			devicePolicyManager.setCameraDisabled(cameraAdmin, camFunc);
		} catch (JSONException e) {
			Log.e(TAG, "Invalid JSON format." + e);
		}
	}

	/**
	 * Install application/bundle.
	 * @param code - Operation code.
	 * @param data - Data required(App data).
	 */
	public void installAppBundle(String code, String data) {
		try {
			resultBuilder.build(code);

			if (code.equals(Constants.OPERATION_INSTALL_APPLICATION)) {
				JSONObject appData = new JSONObject(data);
				installApplication(appData, code);
			} else if (code.equals(Constants.OPERATION_INSTALL_APPLICATION_BUNDLE)) {
				JSONArray jArray = null;
				jArray = new JSONArray(data);
				for (int i = 0; i < jArray.length(); i++) {
					JSONObject appObj = (JSONObject) jArray.getJSONObject(i);
					installApplication(appObj, code);
				}
			}

		} catch (JSONException e) {
			Log.e(TAG, "Invalid JSON format." + e);
		}
	}

	/**
	 * Uninstall application.
	 * @param code - Operation code.
	 * @param data - Data required(App package).
	 */
	public void uninstallApplication(String code, String data) {
		String packageName;
		try {
			JSONObject appData = new JSONObject(data);
			packageName = (String) appData.get(resources.getString(R.string.intent_extra_identity));

			resultBuilder.build(code);

			appList.uninstallApplication(packageName);
		} catch (JSONException e) {
			Log.e(TAG, "Invalid JSON format." + e);
		}
	}

	/**
	 * Encrypt/Decrypt device storage.
	 * @param code        - Operation code.
	 * @param data        - Data required(Encryption enable/disable switch).
	 * @param requestMode - Request mode(Normal mode or policy bundle mode).
	 */
	public void encryptStorage(String code, String data) {
		boolean doEncrypt = true;
		try {
			JSONObject encryptData = new JSONObject(data);
			if (!encryptData.isNull(resources.getString(R.string.intent_extra_function)) &&
			    encryptData.get(resources.getString(R.string.intent_extra_function)).toString()
			               .equalsIgnoreCase(resources.getString(R.string.intent_extra_encrypt))) {
				doEncrypt = true;
			} else if (!encryptData.isNull(resources.getString(R.string.intent_extra_function)) &&
			           encryptData.get(resources.getString(R.string.intent_extra_function))
			                      .toString()
			                      .equalsIgnoreCase(
					                      resources.getString(R.string.intent_extra_decrypt))) {
				doEncrypt = false;
			} else if (!encryptData.isNull(resources.getString(R.string.intent_extra_function))) {
				doEncrypt =
						Boolean.parseBoolean(
								encryptData.get(resources.getString(R.string.intent_extra_function))
								           .toString());
			}
		} catch (JSONException e) {
			Log.e(TAG, "Invalid JSON format." + e);
		}

		ComponentName admin = new ComponentName(context, AgentDeviceAdminReceiver.class);

		if (doEncrypt &&
		    devicePolicyManager.getStorageEncryptionStatus() != DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED &&
		    (devicePolicyManager.getStorageEncryptionStatus() == DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE)) {

			devicePolicyManager.setStorageEncryption(admin, doEncrypt);
			Intent intent = new Intent(DevicePolicyManager.ACTION_START_ENCRYPTION);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);

		} else if (!doEncrypt &&
		           devicePolicyManager.getStorageEncryptionStatus() != DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED &&
		           (devicePolicyManager.getStorageEncryptionStatus() == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE || 
		           devicePolicyManager.getStorageEncryptionStatus() == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVATING)) {
			
			devicePolicyManager.setStorageEncryption(admin, doEncrypt);
		}

		String status;
		if (devicePolicyManager.getStorageEncryptionStatus() !=
		    DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED) {
			status = resources.getString(R.string.shared_pref_default_status);
		} else {
			status = resources.getString(R.string.shared_pref_false_status);
		}

		resultBuilder.build(code, status);

	}

	/**
	 * Mute the device.
	 * @param code        - Operation code.
	 * @param requestMode - Request mode(Normal mode or policy bundle mode).
	 */
	private void muteDevice(String code) {
		resultBuilder.build(code);

		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		audioManager.setStreamVolume(AudioManager.STREAM_RING, DEFAULT_VOLUME, DEFAULT_FLAG);
	}

	/**
	 * Create web clip (Web app shortcut on device home screen).
	 * @param code - Operation code.
	 * @param data - Data required(Web app data).
	 */
	public void createWebClip(String code, String data) {
		String appUrl = null;
		String title = null;

		try {
			JSONObject webClipData = new JSONObject(code);
			appUrl = (String) webClipData.get(resources.getString(R.string.intent_extra_identity));
			title = (String) webClipData.get(resources.getString(R.string.intent_extra_title));
		} catch (JSONException e) {
			Log.e(TAG, "Invalid JSON format. " + e);
		}

		resultBuilder.build(data);

		if(appUrl!=null && title!=null){
			appList.createWebAppBookmark(appUrl, title);
		}
	}

	/**
	 * Set device password policy.
	 * @param code        - Operation code.
	 * @param data        - Data required (Password policy parameters).
	 * @param requestMode - Request mode(Normal mode or policy bundle mode).
	 */
	public void setPasswordPolicy(String code, String data) {
		ComponentName demoDeviceAdmin = new ComponentName(context, AgentDeviceAdminReceiver.class);

		int attempts, length, history, specialChars;
		String alphanumeric, complex;
		boolean isAlphanumeric, isComplex;
		long timout;

		resultBuilder.build(code);

		try {
			JSONObject policyData = new JSONObject(data);
			if (!policyData
					.isNull(resources.getString(R.string.policy_password_max_failed_attempts)) &&
			    policyData.get(resources.getString(R.string.policy_password_max_failed_attempts)) !=
			    null) {
				attempts =
						Integer.parseInt((String) policyData.get(resources.getString(
								R.string.policy_password_max_failed_attempts)));
				devicePolicyManager.setMaximumFailedPasswordsForWipe(demoDeviceAdmin, attempts);
			}

			if (!policyData.isNull(resources.getString(R.string.policy_password_min_length)) &&
			    policyData.get(resources.getString(R.string.policy_password_min_length)) != null) {
				length =
						Integer.parseInt((String) policyData
								.get(resources.getString(R.string.policy_password_min_length)));
				devicePolicyManager.setPasswordMinimumLength(demoDeviceAdmin, length);
			}

			if (!policyData.isNull(resources.getString(R.string.policy_password_pin_history)) &&
			    policyData.get(resources.getString(R.string.policy_password_pin_history)) != null) {
				history =
						Integer.parseInt((String) policyData
								.get(resources.getString(R.string.policy_password_pin_history)));
				devicePolicyManager.setPasswordHistoryLength(demoDeviceAdmin, history);
			}

			if (!policyData
					.isNull(resources.getString(R.string.policy_password_min_complex_chars)) &&
			    policyData.get(resources.getString(R.string.policy_password_min_complex_chars)) !=
			    null) {
				specialChars =
						Integer.parseInt((String) policyData.get(resources.getString(
								R.string.policy_password_min_complex_chars)));
				devicePolicyManager.setPasswordMinimumSymbols(demoDeviceAdmin, specialChars);
			}

			if (!policyData
					.isNull(resources.getString(R.string.policy_password_require_alphanumeric)) &&
			    policyData
					    .get(resources.getString(R.string.policy_password_require_alphanumeric)) !=
			    null) {
				if (policyData.get(resources.getString(
						R.string.policy_password_require_alphanumeric)) instanceof String) {
					alphanumeric =
							(String) policyData.get(resources.getString(
									R.string.policy_password_require_alphanumeric));
					if (alphanumeric
							.equals(resources.getString(R.string.shared_pref_default_status))) {
						devicePolicyManager.setPasswordQuality(demoDeviceAdmin,
						                                       DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC);
					}
				} else if (policyData.get(resources.getString(
						R.string.policy_password_require_alphanumeric)) instanceof Boolean) {
					isAlphanumeric =
							policyData.getBoolean(resources.getString(
									R.string.policy_password_require_alphanumeric));
					if (isAlphanumeric) {
						devicePolicyManager.setPasswordQuality(demoDeviceAdmin,
						                                       DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC);
					}
				}
			}

			if (!policyData.isNull(resources.getString(R.string.policy_password_allow_simple)) &&
			    policyData.get(resources.getString(R.string.policy_password_allow_simple)) !=
			    null) {
				if (policyData.get(resources.getString(
						R.string.policy_password_allow_simple)) instanceof String) {
					complex =
							(String) policyData.get(resources.getString(
									R.string.policy_password_allow_simple));
					if (!complex.equals(resources.getString(R.string.shared_pref_default_status))) {
						devicePolicyManager.setPasswordQuality(demoDeviceAdmin,
						                                       DevicePolicyManager.PASSWORD_QUALITY_COMPLEX);
					}
				} else if (policyData.get(resources.getString(
						R.string.policy_password_allow_simple)) instanceof Boolean) {
					isComplex =
							policyData.getBoolean(
									resources.getString(R.string.policy_password_allow_simple));
					if (!isComplex) {
						devicePolicyManager.setPasswordQuality(demoDeviceAdmin,
						                                       DevicePolicyManager.PASSWORD_QUALITY_COMPLEX);
					}
				}
			}

			if (!policyData.isNull(resources.getString(R.string.policy_password_pin_age_in_days)) &&
			    policyData.get(resources.getString(R.string.policy_password_pin_age_in_days)) !=
			    null) {
				int daysOfExp =
						Integer.parseInt((String) policyData.get(resources.getString(
								R.string.policy_password_pin_age_in_days)));
				timout = (long) (daysOfExp * DAY_MILLISECONDS_MULTIPLIER);
				devicePolicyManager.setPasswordExpirationTimeout(demoDeviceAdmin, timout);
			}

		} catch (JSONException e) {
			Log.e(TAG, "Invalid JSON format." + e);
		}

	}

	/**
	 * Install google play applications.
	 * @param code - Operation code.
	 * @param data - Data required(App data).
	 */
	public void installGooglePlayApp(String code, String data) {
		String packageName = null;
		try {
			JSONObject appData = new JSONObject(data);
			packageName = (String) appData.get(resources.getString(R.string.intent_extra_package));

		} catch (JSONException e) {
			Log.e(TAG, "Invalid JSON format." + e);
		}

		resultBuilder.build(code);
		triggerGooglePlayApp(packageName);
	}

	/**
	 * Open Google Play store application with an application given.
	 * @param packageName - Application package name.
	 */
	public void triggerGooglePlayApp(String packageName) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setData(Uri.parse(Constants.GOOGLE_PLAY_APP_URI + packageName));
		context.startActivity(intent);
	}

	/**
	 * Change device lock code.
	 * @param code - Operation code.
	 * @param data - Data required(Lock code).
	 */
	public void changeLockCode(String code, String data) {
		ComponentName demoDeviceAdmin = new ComponentName(context, AgentDeviceAdminReceiver.class);
		devicePolicyManager.setPasswordMinimumLength(demoDeviceAdmin, DEFAULT_PASSWORD_MIN_LENGTH);
		String password = null;

		try {
			JSONObject lockData = new JSONObject(data);
			if (!lockData.isNull(resources.getString(R.string.intent_extra_password))) {
				password =
						(String) lockData.get(resources.getString(R.string.intent_extra_password));
			}

			resultBuilder.build(code);

			if (password!=null && !password.isEmpty()) {
				devicePolicyManager.resetPassword(password,
				                                  DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
				devicePolicyManager.lockNow();
			}
		} catch (JSONException e) {
			Log.e(TAG, "Invalid JSON format." + e);
		}
	}

	/**
	 * Set policy bundle.
	 * @param code - Operation code.
	 */
	public void setPolicyBundle(String code) {
		try {
			resultBuilder.build(code, new JSONObject(bundleParams.toString()));
		} catch (JSONException e) { 
			Log.e(TAG, "Invalid JSON format." + e);
		}
	}

	/**
	 * Enterprise wipe the device.
	 * @param code - Operation code.
	 */
	public void enterpriseWipe(String code) {
		resultBuilder.build(code);

		CommonUtils.clearAppData(context);
		Intent intent = new Intent(context, ServerDetails.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	/**
	 * Blacklisting apps.
	 * @param code - Operation code.
	 * @param data - Data required(Application data).
	 */
	public void blacklistApps(String code, String data) {
		ArrayList<DeviceAppInfo> apps = appList.getInstalledApps();
		JSONArray appList = new JSONArray();
		String identity = null;
		try {
			JSONObject resultApp = new JSONObject(data);
			if (!resultApp.isNull(resources.getString(R.string.intent_extra_data))) {
				resultApp =
						(JSONObject) resultApp.get(resources.getString(R.string.intent_extra_data));
			}

			identity = (String) resultApp.get(resources.getString(R.string.intent_extra_identity));
		} catch (JSONException e) {
			Log.e(TAG, "Invalid JSON format." + e);
		}

		for (DeviceAppInfo app : apps) {
			JSONObject result = new JSONObject();
			try {
				result.put(resources.getString(R.string.intent_extra_name), app.getAppname());
				result.put(resources.getString(R.string.intent_extra_package),
				           app.getPackagename());
				if (identity.trim().equals(app.getPackagename())) {
					result.put(resources.getString(R.string.intent_extra_not_violated), false);
					result.put(resources.getString(R.string.intent_extra_package),
					           app.getPackagename());
				} else {
					result.put(resources.getString(R.string.intent_extra_not_violated), true);
				}

			} catch (JSONException e) {
				Log.e(TAG, "Invalid JSON format." + e);
			}
			appList.put(result);
		}

		resultBuilder.build(code, appList);
	}

	/**
	 * Install an Application
	 */
	private void installApplication(JSONObject data, String code) {
		String appUrl = null;
		String type = null;
		String os = null;

		try {
			JSONObject applicationData = data;
			appUrl = (String) applicationData
					.get(resources.getString(R.string.intent_extra_identity));
			if (!applicationData.isNull(resources.getString(R.string.intent_extra_type))) {
				type = (String) applicationData
						.get(resources.getString(R.string.intent_extra_type));
			}

			if (!applicationData.isNull(resources.getString(R.string.intent_extra_platform_id))) {
				os = (String) applicationData
						.get(resources.getString(R.string.intent_extra_platform_id));
			} else if (!applicationData.isNull(resources.getString(R.string.intent_extra_os))) {
				os = (String) applicationData.get(resources.getString(R.string.intent_extra_os));
			}

			if (type != null && type.equalsIgnoreCase(resources.getString(R.string.intent_extra_enterprise))) {
				if (os != null) {
					if (os.equalsIgnoreCase(resources.getString(R.string.intent_extra_android))) {
						appList.installApp(appUrl);
					}
				} else {
					appList.installApp(appUrl);
				}
			} else if (type!= null && type.equalsIgnoreCase(resources.getString(R.string.intent_extra_market))) {
				if (os != null) {
					if (os.equalsIgnoreCase(resources.getString(R.string.intent_extra_android))) {
						triggerGooglePlayApp(appUrl);
					}
				} else {
					triggerGooglePlayApp(appUrl);
				}

			} else {
				if (os != null) {
					if (os.equalsIgnoreCase(resources.getString(R.string.intent_extra_android))) {
						appList.installApp(appUrl);
					}
				} else {
					appList.installApp(appUrl);
				}
			}

		} catch (JSONException e) {
			Log.e(TAG, "Invalid JSON format." + e);
		}

	}

}
