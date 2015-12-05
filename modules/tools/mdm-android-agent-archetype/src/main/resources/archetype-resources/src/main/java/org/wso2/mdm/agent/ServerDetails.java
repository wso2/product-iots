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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.wso2.mdm.agent.R;
import org.wso2.mdm.agent.api.DeviceInfo;
import org.wso2.mdm.agent.factory.DeviceStateFactory;
import org.wso2.mdm.agent.interfaces.DeviceState;
import org.wso2.mdm.agent.utils.Preference;
import org.wso2.mdm.agent.utils.Response;

/**
 * This the the activity that is used to capture the server's host name.
 */
public class ServerDetails extends Activity {

	private TextView evServerIP;
	private Button btnStartRegistration;
	private Context context;
	private DeviceInfo deviceInfo;
	private DeviceState state;
	private TextView txtSeverAddress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		context = this.getApplicationContext();
		deviceInfo = new DeviceInfo(context);
		state = DeviceStateFactory.getDeviceState(context, deviceInfo.getSdkVersion());
		evServerIP = (TextView) findViewById(R.id.evServerIP);
		txtSeverAddress = (TextView) findViewById(R.id.tvSeverAddress);
		btnStartRegistration = (Button) findViewById(R.id.btnStartRegistration);

		Response compatibility = state.evaluateCompatibility();
		if (!compatibility.getCode()) {
			btnStartRegistration.setVisibility(View.GONE);
			txtSeverAddress.setVisibility(View.GONE);
			evServerIP.setVisibility(View.GONE);
		} else {
			btnStartRegistration.setVisibility(View.VISIBLE);
			evServerIP.setVisibility(View.VISIBLE);
			String ipSaved =
					Preference.getString(context.getApplicationContext(),
					                     getResources().getString(R.string.shared_pref_ip));

			// check if we have the IP saved previously.
			if (ipSaved != null && !ipSaved.isEmpty()) {
				evServerIP.setText(ipSaved);
				startAuthenticationActivity();
			} else {
				evServerIP.setText(ipSaved);
			}

			String deviceActive =
					Preference.getString(context,
					                     context.getResources()
					                            .getString(R.string.shared_pref_device_active)
					);
			
			if (deviceActive != null &&
			    deviceActive.equals(context.getResources()
			                               .getString(R.string.shared_pref_reg_success))) {
				Intent intent = new Intent(ServerDetails.this, AlreadyRegisteredActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
			// on click handler for start registration.
			btnStartRegistration.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					loadStartRegistrationDialog();
				}
			});
		}
	}
	
	private void loadStartRegistrationDialog(){
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ServerDetails.this);
		StringBuilder messageBuilder = new StringBuilder();
		messageBuilder
				.append(getResources().getString(R.string.dialog_init_confirmation));
		messageBuilder.append(context.getResources()
		                             .getString(R.string.intent_extra_space));
		messageBuilder.append(evServerIP.getText().toString());
		messageBuilder.append(context.getResources()
		                             .getString(R.string.intent_extra_space));
		messageBuilder
				.append(getResources().getString(R.string.dialog_init_end_general));
		alertBuilder.setMessage(messageBuilder.toString())
		            .setPositiveButton(getResources().getString(R.string.yes),
		                               dialogClickListener)
		            .setNegativeButton(getResources().getString(R.string.no),
		                               dialogClickListener).show();
	}
	
	private DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					if (!evServerIP.getText().toString().trim().isEmpty()) {
						Preference.putString(context.getApplicationContext(),
						                     getResources()
								                     .getString(R.string.shared_pref_ip),
						                     evServerIP.getText().toString().trim());
						startAuthenticationActivity();

					} else {
						Toast.makeText(context.getApplicationContext(),
						               getResources().getString(
								               R.string.toast_message_enter_server_address),
						               Toast.LENGTH_LONG).show();
					}
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					dialog.dismiss();
					break;
			}
		}
	};

	/**
	 * This method is called to open AuthenticationActivity.
	 */
	private void startAuthenticationActivity() {
		Intent intent = new Intent(ServerDetails.this, AuthenticationActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		context = null;
		super.onDestroy();
	}
}
