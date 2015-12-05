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

import org.wso2.mdm.agent.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * The CommonDialogUtils class contains the all dialog templates.
 */
public abstract class CommonDialogUtils {

	/**
	 * Return an Alert Dialog with one button.
	 * @param context              - The Activity which needs this alert dialog.
	 * @param message              - The message in the alert.
	 * @param positiveBtnLabel     - The label of the positive button.
	 * @param positiveClickListener- The onClickListener of the positive button.
	 * @return - The generated Alert Dialog.
	 */
	public static AlertDialog.Builder getAlertDialogWithOneButton(Context context,
                                      String message,
                                      String positiveBtnLabel,
                                      DialogInterface.OnClickListener positiveClickListener) {

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message).setPositiveButton(positiveBtnLabel, positiveClickListener);

		return builder;
	}

	/**
	 * Return an Alert Dialog with two buttons.
	 * @param context              - The Activity which needs this alert dialog.
	 * @param message              - The message in the alert.
	 * @param positiveBtnLabel     - The label of the positive button.
	 * @param negetiveBtnLabel     - The label of the negative button.
	 * @param positiveClickListener- The onClickListener of the positive button.
	 * @param negativeClickListener- The onClickListener of the negative button.
	 * @return - The generated Alert Dialog.
	 */
	public static AlertDialog.Builder getAlertDialogWithTwoButton(Context context,
                                      String message,
                                      String positiveBtnLabel,
                                      String negetiveBtnLabel,
                                      DialogInterface.OnClickListener positiveClickListener,
                                      DialogInterface.OnClickListener negativeClickListener) {

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message).setPositiveButton(positiveBtnLabel, positiveClickListener)
		       .setNegativeButton(negetiveBtnLabel, negativeClickListener);

		return builder;
	}

	/**
	 * Shows the Network unavailable message.
	 * @param context - The Activity where checking the network availability.
	 */
	public static void showNetworkUnavailableMessage(Context context) {
		AlertDialog.Builder builder =
				CommonDialogUtils.getAlertDialogWithOneButton(context,
                                  context.getResources()
                                         .getString(
                                                 R.string.error_network_unavailable),
                                  context.getResources()
                                         .getString(R.string.button_ok),
                                  null
				);
		builder.show();
	}

	/**
	 * Return an Alert Dialog with two buttons and a title.
	 * @param context              - The Activity which needs this alert dialog.
	 * @param title                - The title of the message.
	 * @param message              - The message in the alert.
	 * @param positiveBtnLabel     - The label of the positive button.
	 * @param negetiveBtnLabel     - The label of the negative button.
	 * @param positiveClickListener- The onClickListener of the positive button.
	 * @param negativeClickListener- The onClickListener of the negative button.
	 * @return - The generated Alert Dialog.
	 */
	public static AlertDialog.Builder getAlertDialogWithTwoButtonAndTitle(Context context,
                                      String title,
                                      String message,
                                      String positiveBtnLabel,
                                      String negetiveBtnLabel,
                                      DialogInterface.OnClickListener positiveClickListener,
                                      DialogInterface.OnClickListener negativeClickListener) {

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setMessage(message).setPositiveButton(positiveBtnLabel, positiveClickListener)
		       .setNegativeButton(negetiveBtnLabel, negativeClickListener);

		return builder;
	}

	/**
	 * Returns an Alert Dialog with one button and title.
	 * @param context               -The activity which need this alert.
	 * @param title                 -The alert title.
	 * @param message               -The alert message.
	 * @param positiveBtnLabel      -The positive button label.
	 * @param positiveClickListener -The positive button listener.
	 * @return - An alert dialog.
	 */
	public static AlertDialog.Builder getAlertDialogWithOneButtonAndTitle(Context context,
                                      String title,
                                      String message,
                                      String positiveBtnLabel,
                                      DialogInterface.OnClickListener positiveClickListener) {

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setMessage(message).setPositiveButton(positiveBtnLabel, positiveClickListener);
		builder.show();
		return builder;
	}

	/**
	 * Shows the ProgressDialog.
	 *
	 * @param context        -The Activity which needs the ProgressDialog.
	 * @param title          -The title.
	 * @param message        -The message.
	 * @param cancelListener -The OnCancelListener.
	 * @return - A progress dialog.
	 */
	public static ProgressDialog showPrgressDialog(Context context, String title, String message,
	                                               OnCancelListener cancelListener) {
		ProgressDialog progressDialog = ProgressDialog.show(context, title, message, true);
		progressDialog.setCancelable(true);
		progressDialog.setOnCancelListener(cancelListener);

		return progressDialog;
	}

	/**
	 * Stops progressDialog.
	 * @param progressDialog -Progress dialog which needs to be stopped.
	 */
	public static void stopProgressDialog(ProgressDialog progressDialog) {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
	}

	/**
	 * 
	 * Return an Alert Dialog with two buttons and a title.
	 * @param context              - The Activity which needs this alert dialog.
	 * @param message              - The message in the alert.
	 * @param positiveBtnLabel     - The label of the positive button.
	 * @param negetiveBtnLabel     - The label of the negative button.
	 * @param positiveClickListener- The onClickListener of the positive button.
	 * @param negativeClickListener- The onClickListener of the negative button.
	 * @param input                - Edit text input.
	 * @return - The generated Alert Dialog.
	 */
	public static AlertDialog.Builder getAlertDialogWithTwoButtonAndEditView(Context context,
                                     String message,
                                     String positiveBtnLabel,
                                     String negetiveBtnLabel,
                                     DialogInterface.OnClickListener positiveClickListener,
                                     DialogInterface.OnClickListener negativeClickListener,
                                     EditText input) {

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message).setPositiveButton(positiveBtnLabel, positiveClickListener)
		       .setNegativeButton(negetiveBtnLabel, negativeClickListener);

		LinearLayout.LayoutParams params =
				new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						LinearLayout.LayoutParams.MATCH_PARENT);
		input.setLayoutParams(params);
		builder.setView(input);

		return builder;
	}

}
