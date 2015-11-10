/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.wso2.carbon.iot.android.sense.util;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.wso2.carbon.iot.android.sense.constants.SenseConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class SenseClient {


    private final static String TAG = "SenseService Client";

    private Context context;

    public SenseClient(Context context) {

        this.context = context;
    }

    public boolean isAuthenticate(String username, String password) {
        Map<String, String> _response = new HashMap<String, String>();
        Map<String, String> params = new HashMap<String, String>();
        params.put("username", username);
        params.put("password", password);
        String response = "";
        try {

            String endpoint = LocalRegister.getServerURL(context) + SenseConstants.LOGIN_CONTEXT;
            response = sendWithTimeWait(endpoint, params, "POST", null).get("status");

            if (response.trim().contains(SenseConstants.Request.REQUEST_SUCCESSFUL)) {

                return true;
            } else {
                //Toast.makeText(context, "Authentication failed, please check your credentials and try again.", Toast.LENGTH_LONG).show();

                return false;
            }
        } catch (Exception ex) {
            Log.e("Authentication", "Authentication failed due to a connection failure");
            return false;
        }
    }

    public boolean register(String username, String deviceId) {
        Map<String, String> _response = new HashMap<String, String>();
        Map<String, String> params = new HashMap<String, String>();
        params.put("deviceId", deviceId);
        params.put("owner", username);

        try {
            String endpoint = LocalRegister.getServerURL(context) + SenseConstants.REGISTER_CONTEXT;
            Map<String, String> response = sendWithTimeWait(endpoint, params, "PUT", null);

            String responseStatus = response.get("status");

            if (responseStatus.trim().contains(SenseConstants.Request.REQUEST_SUCCESSFUL)) {

                Toast.makeText(context, "Device Registered", Toast.LENGTH_LONG).show();

                return true;
            } else if (responseStatus.trim().contains(SenseConstants.Request.REQUEST_CONFLICT)) {
                Toast.makeText(context, "Login Successful", Toast.LENGTH_LONG).show();
                return true;
            } else {
                Toast.makeText(context, "Authentication failed, please check your credentials and try again.", Toast
                        .LENGTH_LONG).show();

                return false;
            }
        } catch (Exception ex) {
            Log.e("Authentication", "Authentication failed due to a connection failure");
            Toast.makeText(context, "Authentication failed due to a connection failure", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public Map<String, String> sendWithTimeWait(String endpoint, Map<String, String> params, String option, String
            jsonBody) {
        Map<String, String> response = null;
        Map<String, String> responseFinal = null;
        for (int i = 1; i <= SenseConstants.Request.MAX_ATTEMPTS; i++) {
            Log.d(TAG, "Attempt #" + i + " to register");
            try {

                response = sendToServer(endpoint, params, option, jsonBody);

                if (response != null && !response.equals(null)) {
                    responseFinal = response;
                }

                return responseFinal;
            } catch (Exception e) {
                Log.e(TAG, "Failed to register on attempt " + i, e);
                if (i == SenseConstants.Request.MAX_ATTEMPTS) {
                    break;
                }

                return responseFinal;
            }
        }

        return responseFinal;
    }

    public void sendSensorDataToServer(String data) {
        String urlString = null;
        try {
            urlString = LocalRegister.getServerURL(context) + SenseConstants.DATA_ENDPOINT;
            Log.i("SENDING DATAs", "SENDING JSON to " + urlString + " : " + data);
            Map<String, String> response = sendWithTimeWait(urlString, null, "POST", data);
            String responseStatus = response.get("status");

        } catch (Exception ex) {
            Log.e("Send Sensor Data", "Failure to send data to "+ urlString);

        }

    }

    public Map<String, String> sendToServer(String endpoint, Map<String, String> params,
                                            String option, String jsonBody) throws IOException {
        String body = null;
        if (params != null && !params.isEmpty()) {
            StringBuilder bodyBuilder = new StringBuilder();
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, String> param = iterator.next();
                bodyBuilder.append(param.getKey()).append('=')
                        .append(param.getValue());
                if (iterator.hasNext()) {
                    bodyBuilder.append('&');
                }
            }
            body = bodyBuilder.toString();
        }


        try {
            SenseClientAsyncExecutor senseClientAsyncExecutor = new SenseClientAsyncExecutor(context);
            return senseClientAsyncExecutor.execute(endpoint, body, option, jsonBody).get();
        } catch (InterruptedException e) {
            Log.e("Send Sensor Data", "Thread Inturption for endpoint " + endpoint);
        } catch (ExecutionException e) {
            Log.e("Send Sensor Data", "Failed to push data to the endpoint " + endpoint);
        }
        return null;
    }

    private String inputStreamAsString(InputStream in) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder builder = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append("\n"); // append a new line
            }
        } catch (IOException e) {
            Log.e(SenseClient.class.getName(), e.getMessage());
        } finally {
            try {
                in.close();
            } catch (IOException e) {
               Log.e(SenseClient.class.getName(), e.getMessage());
            }
        }
        // System.out.println(builder.toString());
        return builder.toString();
    }


}
