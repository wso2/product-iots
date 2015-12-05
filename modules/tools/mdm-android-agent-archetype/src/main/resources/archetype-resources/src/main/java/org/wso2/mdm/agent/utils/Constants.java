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

public class Constants {

	public static final boolean DEBUG_MODE_ENABLED = true;
	public static final boolean LOCAL_NOTIFICATIONS_ENABLED = true;
	public static final boolean GCM_ENABLED = false;

	public static final String SERVER_PORT = "9763";
	public static final String SERVER_PROTOCOL = "http://";
	public static final String API_VERSION = "1.0.0";
	public static final String API_SERVER_PORT = "9763";

	public static final String SERVER_APP_ENDPOINT = "/mdm-android-agent/";
	public static final String SERVER_AUTHENTICATION_ENDPOINT = "register/authenticate/device";
	public static final String LICENSE_ENDPOINT = "/license/" + API_VERSION;
	public static final String REGISTER_ENDPOINT = "/enroll/" + API_VERSION;

	public static final String OAUTH_ENDPOINT = "/oauth2/token";
	public static final String SENDER_ID_ENDPOINT = "devices/sender_id/";
	public static final String IS_REGISTERED_ENDPOINT = "devices/isregistered/";
	public static final String UNREGISTER_ENDPOINT = "devices/unregister/";
	public static final String NOTIFICATION_ENDPOINT = "/operation/" + API_VERSION;
	public static final String GOOGLE_PLAY_APP_URI = "market://details?id=";

	public static final String TRUSTSTORE_PASSWORD = "wso2carbon";
	public static final String EULA_TITLE = "POLICY AGREEMENT";
	public static final String EULA_TEXT = "Test policy agreement.";

	public static final String EMPTY_STRING = "";
	public static final String NULL_STRING = "null";
	public static final String STATUS_KEY = "status";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String STATUS = "status";
	public static final String RESPONSE = "response";
	public static final String PROPERTIES = "properties";
	public static final String CODE = "code";
	public static final String LOCAL = "LOCAL";
	public static final String LOG_FILE = "wso2log.txt";

	/**
	 * Request codes.
	 */
	public static final int REGISTER_REQUEST_CODE = 300;
	public static final int IS_REGISTERED_REQUEST_CODE = 301;
	public static final int SENDER_ID_REQUEST_CODE = 303;
	public static final int LICENSE_REQUEST_CODE = 304;
	public static final int UNREGISTER_REQUEST_CODE = 305;
	public static final int NOTIFICATION_REQUEST_CODE = 306;

	/**
	 * Google API project id registered to use GCM.
	 */

	public static final String SENDER_ID = "";

	/**
	 * Tag used on log messages.
	 */
	public static final String TAG = "WSO2EMM";

	/**
	 * Intent used to display a message in the screen.
	 */
	public static final String DISPLAY_MESSAGE_ACTION =
			"com.google.android.gcm.demo.app.DISPLAY_MESSAGE";

	/**
	 * Intent's extra that contains the message to be displayed.
	 */
	public static final String EXTRA_MESSAGE = "message";
	public static final int MESSAGE_MODE_GCM = 1;
	public static final int MESSAGE_MODE_SMS = 2;
	public static final int MESSAGE_MODE_LOCAL = 3;

	/**
	 * Status codes
	 */
	public static final String REQUEST_SUCCESSFUL = "200";
	public static final String REGISTERATION_SUCCESSFUL = "201";
	public static final String REQUEST_FAILED = "500";
	public static final String AUTHENTICATION_FAILED = "400";
	public static final String UNAUTHORIZED_ACCESS = "401";
	public static final String NOT_FOUND = "404";
	public static final String INTERNAL_SERVER_ERROR = "500";

	/**
	 * Operation IDs
	 */
	public static final String OPERATION_DEVICE_INFO = "500A";
	public static final String OPERATION_DEVICE_LOCATION = "501A";
	public static final String OPERATION_GET_APPLICATION_LIST = "502A";
	public static final String OPERATION_LOCK_DEVICE = "DEVICE_LOCK";
	public static final String OPERATION_WIPE_DATA = "504A";
	public static final String OPERATION_CLEAR_PASSWORD = "505A";
	public static final String OPERATION_NOTIFICATION = "506A";
	public static final String OPERATION_WIFI = "507A";
	public static final String OPERATION_DISABLE_CAMERA = "508A";
	public static final String OPERATION_INSTALL_APPLICATION = "509A";
	public static final String OPERATION_INSTALL_APPLICATION_BUNDLE = "509B";
	public static final String OPERATION_UNINSTALL_APPLICATION = "510A";
	public static final String OPERATION_ENCRYPT_STORAGE = "511A";
	public static final String OPERATION_RING = "512A";
	public static final String OPERATION_MUTE = "513A";
	public static final String OPERATION_WEBCLIP = "518A";
	public static final String OPERATION_PASSWORD_POLICY = "519A";
	public static final String OPERATION_EMAIL_CONFIGURATION = "520A";
	public static final String OPERATION_INSTALL_GOOGLE_APP = "522A";
	public static final String OPERATION_CHANGE_LOCK_CODE = "526A";
	public static final String OPERATION_ENTERPRISE_WIPE_DATA = "527A";
	public static final String OPERATION_POLICY_BUNDLE = "500P";
	public static final String OPERATION_POLICY_MONITOR = "501P";
	public static final String OPERATION_BLACKLIST_APPS = "528B";
	public static final String OPERATION_POLICY_REVOKE = "502P";

}
