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
package org.wso2.mdm.agent;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.mdm.agent.R;
import org.wso2.mdm.agent.proxy.interfaces.APIResultCallBack;
import org.wso2.mdm.agent.proxy.utils.Constants.HTTP_METHODS;
import org.wso2.mdm.agent.services.LocalNotification;
import org.wso2.mdm.agent.services.AgentDeviceAdminReceiver;
import org.wso2.mdm.agent.utils.CommonDialogUtils;
import org.wso2.mdm.agent.utils.Constants;
import org.wso2.mdm.agent.utils.Preference;
import org.wso2.mdm.agent.utils.CommonUtils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class AlreadyRegisteredActivity extends SherlockActivity implements APIResultCallBack {

	private static final String TAG = AlreadyRegisteredActivity.class.getSimpleName();
	private static final int ACTIVATION_REQUEST = 47;
	private String regId;
	private Context context;
	private Resources resources;
	private ProgressDialog progressDialog;
	private Button btnUnregister;
	private TextView txtRegText;
	private static final int TAG_BTN_UNREGISTER = 0;
	private static final int TAG_BTN_RE_REGISTER = 2;
	private boolean freshRegFlag = false;
	private boolean isUnregisterBtnClicked = false;
	private AlertDialog.Builder alertDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_already_registered);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		getSupportActionBar().setCustomView(R.layout.custom_sherlock_bar);
		getSupportActionBar().setTitle(R.string.empty_app_title);

		DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		ComponentName cdmDeviceAdmin = new ComponentName(this, AgentDeviceAdminReceiver.class);
		context = this;
		resources = context.getResources();
		Bundle extras = getIntent().getExtras();
		
		if (extras != null) {
			if (extras.
					containsKey(getResources().getString(R.string.intent_extra_fresh_reg_flag))) {
				freshRegFlag = extras.getBoolean(
                                 getResources().getString(R.string.intent_extra_fresh_reg_flag));
			}

		}

		String registrationId =
				Preference.getString(context, resources.
				                     			getString(R.string.shared_pref_regId));
		
		if (!registrationId.isEmpty()) {
			regId = registrationId;
		}

		if (freshRegFlag) {
			Preference.putString(context, resources.getString(R.string.shared_pref_registered),
			                     			resources.getString(R.string.shared_pref_reg_success));

			if (!devicePolicyManager.isAdminActive(cdmDeviceAdmin)) {
				startDeviceAdminPrompt(cdmDeviceAdmin);
			}

			freshRegFlag = false;
		}
		
		txtRegText = (TextView) findViewById(R.id.txtRegText);
		btnUnregister = (Button) findViewById(R.id.btnUnreg);
		btnUnregister.setTag(TAG_BTN_UNREGISTER);
		btnUnregister.setOnClickListener(onClickListenerButtonClicked);
		LocalNotification.startPolling(context);

	}
	
	private DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					dialog.dismiss();
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					startUnRegistration();
					break;
			}
		}
	};

	private OnClickListener onClickListenerButtonClicked = new OnClickListener() {

		@Override
		public void onClick(View view) {
			int iTag = (Integer) view.getTag();

			switch (iTag) {

				case TAG_BTN_UNREGISTER:
					showUnregisterDialog();
					break;

				case TAG_BTN_RE_REGISTER:
					loadServerDetailsActivity();
					break;

				default:
					break;
			}

		}
	};

	private DialogInterface.OnClickListener isRegisteredFailedOKBtnClickListerner =
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0,
				                    int arg1) {

					loadServerDetailsActivity();

				}
			};

	/**
	 * Send unregistration request.
	 */
	private void startUnRegistration() {
		final Context context = AlreadyRegisteredActivity.this;
		isUnregisterBtnClicked = true;

		progressDialog =
				ProgressDialog.show(AlreadyRegisteredActivity.this,
				                    getResources().getString(R.string.dialog_message_unregistering),
				                    getResources().getString(R.string.dialog_message_please_wait),
				                    true);

		regId =
				Preference.getString(context,
				                     context.getResources().getString(R.string.shared_pref_regId));

		JSONObject requestParams = new JSONObject();
		try {
			requestParams.put(resources.getString(R.string.shared_pref_regId), regId);
		} catch (JSONException e) {
			Log.e(TAG, "Invalid JSON." + e);
		}

		if (CommonUtils.isNetworkAvailable(context)) {
			CommonUtils.callSecuredAPI(AlreadyRegisteredActivity.this,
			                           Constants.UNREGISTER_ENDPOINT, HTTP_METHODS.POST,
			                           requestParams, AlreadyRegisteredActivity.this,
			                           Constants.UNREGISTER_REQUEST_CODE);
		} else {
			CommonDialogUtils.stopProgressDialog(progressDialog);
			CommonDialogUtils.showNetworkUnavailableMessage(AlreadyRegisteredActivity.this);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (Constants.DEBUG_MODE_ENABLED) {
			getSupportMenuInflater().inflate(R.menu.sherlock_menu_debug, menu);
		} else {
			getSupportMenuInflater().inflate(R.menu.sherlock_menu, menu);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.info_setting:
				loadDeviceInfoActivity();
				return true;
			case R.id.pin_setting:
				loadPinCodeActivity();
				return true;
			case R.id.ip_setting:
				loadServerDetailsActivity();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		loadHomeScreen();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			loadHomeScreen();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_HOME) {
			loadHomeScreen();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!freshRegFlag && !isUnregisterBtnClicked) {
			if (CommonUtils.isNetworkAvailable(context)) {
				JSONObject requestParams = new JSONObject();
				try {
					requestParams.put(resources.getString(R.string.shared_pref_regId), regId);
				} catch (JSONException e) {
						Log.e(TAG, "Invalid JSON." + e);
				}

				CommonUtils.callSecuredAPI(AlreadyRegisteredActivity.this,
				                           Constants.IS_REGISTERED_ENDPOINT, HTTP_METHODS.POST,
				                           requestParams, AlreadyRegisteredActivity.this,
				                           Constants.IS_REGISTERED_REQUEST_CODE);
			} else {
				CommonDialogUtils.showNetworkUnavailableMessage(AlreadyRegisteredActivity.this);
			}

		}

	}
	
	/**
	 * Displays an internal server error message to the user.
	 */
	private void displayInternalServerError() {
		alertDialog = CommonDialogUtils.getAlertDialogWithOneButtonAndTitle(context,
                                            getResources().getString(R.string.title_head_connection_error),
                                            getResources().getString(R.string.error_internal_server),
                                            getResources().getString(R.string.button_ok),
                                            null);
		alertDialog.show();
	}
	
	/**
	 * Clears application data and displays unregister success message.
	 */
	private void clearAppData() {
		CommonUtils.clearAppData(context);

		alertDialog = CommonDialogUtils.getAlertDialogWithOneButtonAndTitle(context,
                                            getResources().getString(R.string.title_head_registration_error),
                                            getResources().getString(R.string.error_for_all_unknown_registration_failures),
                                            getResources().getString(R.string.button_ok),
                                            isRegisteredFailedOKBtnClickListerner);
		alertDialog.show();
	}

	@Override
	public void onReceiveAPIResult(Map<String, String> result, int requestCode) {
		String responseStatus = null;

		if (requestCode == Constants.UNREGISTER_REQUEST_CODE) {
			stopProgressDialog();
			if (result != null) {
				responseStatus = result.get(Constants.STATUS_KEY);
				if (responseStatus != null && responseStatus.equals(Constants.REQUEST_SUCCESSFUL)) {
					clearAppData();
				} else if (responseStatus != null && responseStatus.equals(Constants.INTERNAL_SERVER_ERROR)) {
					displayInternalServerError();
				} else {
					loadAuthenticationErrorActivity();
				}
			} else {
				loadAuthenticationErrorActivity();
			}

		}

		if (requestCode == Constants.IS_REGISTERED_REQUEST_CODE) {
			stopProgressDialog();
			if (result != null) {
				responseStatus = result.get(Constants.STATUS_KEY);
				if (responseStatus.equals(Constants.INTERNAL_SERVER_ERROR)) {
					displayInternalServerError();
				} else if (!responseStatus.equals(Constants.REQUEST_SUCCESSFUL)) {
					initiateUnregistration();
				} 
			} else {
				clearAppData();
			}

		}

	}
	
	/**
	 * Load device home screen.
	 */
	
	private void loadHomeScreen(){
		Intent i = new Intent();
		i.setAction(Intent.ACTION_MAIN);
		i.addCategory(Intent.CATEGORY_HOME);
		this.startActivity(i);
		super.onBackPressed();
	}
	
	/**
	 * Initiate unregistration.
	 */
	private void initiateUnregistration(){
		txtRegText.setText(R.string.register_text_view_text_unregister);
		btnUnregister.setText(R.string.register_button_text);
		btnUnregister.setTag(TAG_BTN_RE_REGISTER);
		btnUnregister.setOnClickListener(onClickListenerButtonClicked);
		CommonUtils.clearAppData(context);
	}
	
	/**
	 * Start device admin activation request.
	 * @param cdmDeviceAdmin - Device admin component.
	 */
	private void startDeviceAdminPrompt(ComponentName cdmDeviceAdmin){
		Intent deviceAdminIntent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		deviceAdminIntent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, cdmDeviceAdmin);
		deviceAdminIntent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
		                           getResources()
				                           .getString(R.string.device_admin_enable_alert));
		startActivityForResult(deviceAdminIntent, ACTIVATION_REQUEST);
	}
	
	/**
	 * Display unregistration confirmation dialog.
	 */
	private void showUnregisterDialog(){
		AlertDialog.Builder builder =
				new AlertDialog.Builder(
						AlreadyRegisteredActivity.this);
		builder.setMessage(getResources().getString(R.string.dialog_unregister))
		       .setNegativeButton(getResources().getString(R.string.yes),
		                          dialogClickListener)
		       .setPositiveButton(getResources().getString(R.string.no),
		                          dialogClickListener).show();
	}
	
	/**
	 * Load device info activity.
	 */
	private void loadDeviceInfoActivity(){
		Intent intent =
				new Intent(AlreadyRegisteredActivity.this,
				           DisplayDeviceInfoActivity.class);
		intent.putExtra(getResources().getString(R.string.intent_extra_from_activity),
		                  AlreadyRegisteredActivity.class.getSimpleName());
		startActivity(intent);
	}
	
	/**
	 * Load server details activity.
	 */
	private void loadServerDetailsActivity(){
		Preference.putString(context, resources.getString(R.string.shared_pref_ip),
		                     resources.getString(R.string.shared_pref_default_string));
		Intent intent = new Intent(
		                           AlreadyRegisteredActivity.this,
		                           			ServerDetails.class);
		intent.putExtra(getResources().getString(R.string.intent_extra_regid),
		                regId);
		intent.putExtra(getResources().getString(R.string.intent_extra_from_activity),
		                AlreadyRegisteredActivity.class.getSimpleName());
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}
	
	/**
	 * Load PIN code activity.
	 */
	private void loadPinCodeActivity(){
		Intent intent =
				new Intent(AlreadyRegisteredActivity.this, PinCodeActivity.class);
		intent.putExtra(getResources().getString(R.string.intent_extra_from_activity),
		                   AlreadyRegisteredActivity.class.getSimpleName());
		startActivity(intent);
	}

	/**
	 * Loads authentication error activity.
	 */
	private void loadAuthenticationErrorActivity() {
		Intent intent =
				new Intent(AlreadyRegisteredActivity.this,
				           AuthenticationErrorActivity.class);
		intent.putExtra(getResources().getString(R.string.intent_extra_regid), regId);
		intent.putExtra(getResources().getString(R.string.intent_extra_from_activity),
		                AlreadyRegisteredActivity.class.getSimpleName());
		startActivity(intent);
	}

	private void stopProgressDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
	}

}