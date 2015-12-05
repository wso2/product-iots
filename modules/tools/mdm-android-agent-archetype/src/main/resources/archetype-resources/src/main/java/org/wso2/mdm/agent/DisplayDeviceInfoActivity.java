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

import org.wso2.mdm.agent.R;
import org.wso2.mdm.agent.api.DeviceInfo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class DisplayDeviceInfoActivity extends Activity {
	private String fromActivity;
	private String registrationId;
	private TextView deviceId;
	private TextView deviceName;
	private TextView model;
	private TextView operator;
	private TextView sdk;
	private TextView os;
	private TextView root;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_device_info);
		DeviceInfo deviceInfo = new DeviceInfo(this.getApplicationContext());
		deviceId = (TextView) findViewById(R.id.txtId);
		deviceName = (TextView) findViewById(R.id.txtDevice);
		model = (TextView) findViewById(R.id.txtModel);
		operator = (TextView) findViewById(R.id.txtOperator);
		sdk = (TextView) findViewById(R.id.txtSDK);
		os = (TextView) findViewById(R.id.txtOS);
		root = (TextView) findViewById(R.id.txtRoot);

		deviceId.setText(getResources().getString(R.string.info_label_imei) +
		                 getResources().getString(R.string.intent_extra_space) +
		                 deviceInfo.getDeviceId());
		
		deviceName.setText(getResources().getString(R.string.info_label_device) +
		                   getResources().getString(R.string.intent_extra_space) +
		                   deviceInfo.getDeviceName());
		
		model.setText(getResources().getString(R.string.info_label_model) +
		              getResources().getString(R.string.intent_extra_space) +
		              deviceInfo.getDeviceModel());

		String operators = null;
		
		if (deviceInfo.getNetworkOperatorName() != null) {
			operators = deviceInfo.getNetworkOperatorName();
		}else{
			operators = getResources().getString(R.string.info_label_no_sim);
		}
		
		operator.setText(getResources().getString(R.string.info_label_operator) +
		                 getResources().getString(R.string.intent_extra_space) + operators);
		
		if (deviceInfo.getIMSINumber() != null) {
			sdk.setText(getResources().getString(R.string.info_label_imsi) +
			            getResources().getString(R.string.intent_extra_space) +
			            deviceInfo.getIMSINumber());
		} else {
			sdk.setText(getResources().getString(R.string.info_label_imsi) +
			            getResources().getString(R.string.intent_extra_space) + operators);
		}
		
		os.setText(getResources().getString(R.string.info_label_os) +
		           getResources().getString(R.string.intent_extra_space) +
		           deviceInfo.getOsVersion());
		
		root.setText(getResources().getString(R.string.info_label_rooted) +
		             getResources().getString(R.string.intent_extra_space) +
		             (deviceInfo.isRooted() ? getResources().getString(R.string.yes)
		                                    : getResources().getString(R.string.no)));

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if (extras.containsKey(getResources().getString(R.string.intent_extra_from_activity))) {
				fromActivity =
						extras.getString(
								getResources().getString(R.string.intent_extra_from_activity));
			}

			if (extras.containsKey(getResources().getString(R.string.intent_extra_regid))) {
				registrationId =
						extras.getString(getResources().getString(R.string.intent_extra_regid));
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem menu) {
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && fromActivity != null &&
		    fromActivity.equals(AlreadyRegisteredActivity.class.getSimpleName())) {
			Intent intent =
					new Intent(DisplayDeviceInfoActivity.this,
					           AlreadyRegisteredActivity.class);
			intent.putExtra(getResources().getString(R.string.intent_extra_from_activity),
			                DisplayDeviceInfoActivity.class.getSimpleName());
			intent.putExtra(getResources().getString(R.string.intent_extra_regid), registrationId);
			startActivity(intent);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent i = new Intent();
			i.setAction(Intent.ACTION_MAIN);
			i.addCategory(Intent.CATEGORY_HOME);
			this.startActivity(i);
			this.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
