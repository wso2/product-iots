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
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gcm.GCMRegistrar;
import org.wso2.mdm.agent.R;

public class AuthenticationErrorActivity extends Activity {
	private String registrationId;
	private Button btnTryAgain;
	private static final int TAG_BTN_TRY_AGAIN = 0;
	private static final int TAG_BTN_UNREGISTER = 1;
	private String fromActivity;
	private TextView txtMessage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_authentication_error);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			if (extras.containsKey(getResources().getString(R.string.intent_extra_regid))) {
				registrationId =
						extras.getString(getResources().getString(R.string.intent_extra_regid));
			}

			if (extras.containsKey(getResources().getString(R.string.intent_extra_from_activity))) {
				fromActivity =
						extras.getString(
								getResources().getString(R.string.intent_extra_from_activity));
			}
		}
		
		if (registrationId == null || registrationId.isEmpty()) {
			registrationId = GCMRegistrar.getRegistrationId(this);
		}
		
		txtMessage = (TextView) findViewById(R.id.error);

		btnTryAgain = (Button) findViewById(R.id.btnTryAgain);
		btnTryAgain.setTag(TAG_BTN_TRY_AGAIN);
		btnTryAgain.setOnClickListener(onClickListener_BUTTON_CLICKED);

		if (fromActivity.equals(RegistrationActivity.class.getSimpleName())) {
			txtMessage.setText(getResources().getString(R.string.error_registration_failed));
		} else if (fromActivity.equals(AlreadyRegisteredActivity.class.getSimpleName())) {
			txtMessage.setText(getResources().getString(R.string.error_unregistration_failed));
			btnTryAgain.setTag(TAG_BTN_UNREGISTER);
		}

	}

	OnClickListener onClickListener_BUTTON_CLICKED = new OnClickListener() {

		@Override
		public void onClick(View view) {
			int tag = (Integer) view.getTag();

			switch (tag) {

				case TAG_BTN_TRY_AGAIN:
					tryAgain();
					break;
				case TAG_BTN_UNREGISTER:
					finish();
					break;
				default:
					tryAgain();
					break;
			}

		}
	};

	/**
	 * Retry authentication.
	 */
	private void tryAgain() {
		Intent intent = new Intent(AuthenticationErrorActivity.this, AuthenticationActivity.class);
		intent.putExtra(getResources().getString(R.string.intent_extra_from_activity),
		                AuthenticationActivity.class.getSimpleName());
		intent.putExtra(getResources().getString(R.string.intent_extra_regid), registrationId);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.authentication_error, menu);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			tryAgain();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_HOME) {
			finish();
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}

}
