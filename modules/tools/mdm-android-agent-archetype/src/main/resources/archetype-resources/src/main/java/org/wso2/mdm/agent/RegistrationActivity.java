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
import org.wso2.mdm.agent.R;
import org.wso2.mdm.agent.beans.ServerConfig;
import org.wso2.mdm.agent.proxy.interfaces.APIResultCallBack;
import org.wso2.mdm.agent.proxy.utils.Constants.HTTP_METHODS;
import org.wso2.mdm.agent.services.BuildDeviceInfoPayload;
import org.wso2.mdm.agent.utils.CommonDialogUtils;
import org.wso2.mdm.agent.utils.Constants;
import org.wso2.mdm.agent.utils.Preference;
import org.wso2.mdm.agent.utils.CommonUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

public class RegistrationActivity extends Activity implements APIResultCallBack {
	private Context context;
	private ProgressDialog progressDialog;
	private AlertDialog.Builder alertDialog;
	private BuildDeviceInfoPayload deviceInfoBuilder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = this;
		deviceInfoBuilder = new BuildDeviceInfoPayload(context);
		registerDevice();
	}

	private void registerDevice() {
		progressDialog =
				CommonDialogUtils.showPrgressDialog(RegistrationActivity.this,
                        getResources()
                                .getString(R.string.dialog_enrolling),
                        getResources()
                                .getString(R.string.dialog_please_wait),
                        null);
		progressDialog.show();

		String type =
				Preference.getString(context,
				                     context.getResources()
				                            .getString(R.string.shared_pref_reg_type)
				);
		
		String username =
				Preference.getString(context,
				                     context.getResources().getString(R.string.username));
		deviceInfoBuilder.build(type, username);
		
		// Check network connection availability before calling the API.
		if (CommonUtils.isNetworkAvailable(context)) {
			// Call device registration API.
			String ipSaved =
					Preference.getString(context.getApplicationContext(),
					                     context.getResources()
					                            .getString(R.string.shared_pref_ip)
					);
			
			ServerConfig utils = new ServerConfig();
			utils.setServerIP(ipSaved);
			
			CommonUtils.callSecuredAPI(RegistrationActivity.this, utils.getAPIServerURL() +
			                                                      Constants.REGISTER_ENDPOINT,
			                           HTTP_METHODS.POST, deviceInfoBuilder.getDeviceInfoPayload(),
			                           RegistrationActivity.this,
			                           Constants.REGISTER_REQUEST_CODE
			);
			
		} else {
			CommonDialogUtils.stopProgressDialog(progressDialog);
			CommonDialogUtils.showNetworkUnavailableMessage(RegistrationActivity.this);
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);
			i.addCategory(Intent.CATEGORY_HOME);
			this.startActivity(i);
			finish();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_HOME) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	private DialogInterface.OnClickListener registrationFailedOKBtnClickListerner =
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0,
				                    int arg1) {
					loadAuthenticationErrorActivity();
				}
	};
	
	/**
	 * Loads Already registered activity.
	 */
	private void loadAlreadyRegisteredActivity(){
		Intent intent =
				new Intent(RegistrationActivity.this,
				           AlreadyRegisteredActivity.class);
		intent.putExtra(getResources().getString(R.string.intent_extra_fresh_reg_flag),
		                true);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
	
	/**
	 * Display connectivity error.
	 */
	private void displayConnectionError(){
		alertDialog = CommonDialogUtils.getAlertDialogWithOneButtonAndTitle(context,
                            getResources().getString(R.string.title_head_connection_error),
                            getResources().getString(R.string.error_internal_server),
                            getResources().getString(R.string.button_ok),
                            registrationFailedOKBtnClickListerner);
		alertDialog.show();
	}
	
	/**
	 * Display internal server error.
	 */
	private void displayInternalServerError(){
		alertDialog = CommonDialogUtils.getAlertDialogWithOneButtonAndTitle(context,
                      getResources().getString(R.string.title_head_registration_error),
                      getResources().getString(R.string.error_for_all_unknown_registration_failures),
                      getResources().getString(R.string.button_ok),
                      registrationFailedOKBtnClickListerner);
	}

	@Override
	public void onReceiveAPIResult(Map<String, String> result, int requestCode) {
		CommonDialogUtils.stopProgressDialog(progressDialog);
		String responseStatus = null;
		if (result != null) {
			responseStatus = result.get(Constants.STATUS_KEY);

			if (responseStatus.equals(Constants.REQUEST_SUCCESSFUL)) {
				loadAlreadyRegisteredActivity();
			} else {
				displayInternalServerError();
			}
		} else {
			displayConnectionError();
		}
	}

	/**
	 * Loads Authentication error activity.
	 */
	private void loadAuthenticationErrorActivity() {
		Intent intent = new Intent(RegistrationActivity.this, AuthenticationErrorActivity.class);
		intent.putExtra(getResources().getString(R.string.intent_extra_from_activity),
		                RegistrationActivity.class.getSimpleName());
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

}
