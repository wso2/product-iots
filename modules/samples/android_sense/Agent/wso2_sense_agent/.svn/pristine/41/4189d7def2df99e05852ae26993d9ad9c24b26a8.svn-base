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
import android.os.AsyncTask;
import android.util.Log;

import org.wso2.carbon.iot.android.sense.constants.SenseConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import agent.sense.android.iot.carbon.wso2.org.wso2_senseagent.R;

public class SenseClientAsyncExecutor extends AsyncTask<String, Void, Map<String, String>> {

    private static List<String> cookies;
    private Context context;
    private final static String TAG = "SenseService Client";

    public SenseClientAsyncExecutor(Context context) {
        this.context = context;

    }

    private HttpsURLConnection getTrustedConnection(HttpsURLConnection conn) {
        HttpsURLConnection urlConnection = conn;
        try {
            KeyStore localTrustStore;

            localTrustStore = KeyStore.getInstance("BKS");

            InputStream in = context.getResources().openRawResource(
                    R.raw.client_truststore);

            localTrustStore.load(in, SenseConstants.TRUSTSTORE_PASSWORD.toCharArray());

            TrustManagerFactory tmf;
            tmf = TrustManagerFactory.getInstance(TrustManagerFactory
                    .getDefaultAlgorithm());

            tmf.init(localTrustStore);

            SSLContext sslCtx;

            sslCtx = SSLContext.getInstance("TLS");

            sslCtx.init(null, tmf.getTrustManagers(), null);

            urlConnection.setSSLSocketFactory(sslCtx.getSocketFactory());
            return urlConnection;
        } catch (KeyManagementException | NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException e) {

            Log.e(SenseClientAsyncExecutor.class.getName(), "Invalid Certifcate");
            return null;
        }

    }

    @Override
    protected Map<String, String> doInBackground(String... parameters) {
        if (android.os.Debug.isDebuggerConnected())
            android.os.Debug.waitForDebugger();
        String response = null;
        Map<String, String> response_params = new HashMap<String, String>();


        String endpoint = parameters[0];
        String body = parameters[1];
        String option = parameters[2];
        String jsonBody = parameters[3];

        if(jsonBody!=null && !jsonBody.isEmpty()){
            body = jsonBody;
        }

        URL url;
        try {
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }

        Log.v(TAG, "post'" + body + "'to" + url);


        HttpURLConnection conn = null;
        HttpsURLConnection sConn = null;
        try {

            if (url.getProtocol().toLowerCase().equals("https")) {

                sConn = (HttpsURLConnection) url.openConnection();
                sConn = getTrustedConnection(sConn);
                sConn.setHostnameVerifier(SERVER_HOST);
                conn = sConn;

            } else {
                conn = (HttpURLConnection) url.openConnection();
            }

            if (cookies != null) {
                for (String cookie : cookies) {
                    conn.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
                }

            }
            if (conn == null) {
                return null;

            }

            byte[] bytes = body.getBytes();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod(option);
            if(jsonBody!=null && !jsonBody.isEmpty()){
                conn.setRequestProperty("Content-Type", "application/json");
            }else {
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            }
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Connection", "close");

            // post the request
            int status = 0;

            if (!option.equals("DELETE")) {
                OutputStream out = conn.getOutputStream();
                out.write(bytes);
                out.close();
                // handle the response
                status = conn.getResponseCode();
                response_params.put("status", String.valueOf(status));
                Log.v("Response Status", status + "");

                List<String> receivedCookie = conn.getHeaderFields().get("Set-Cookie");
                if(receivedCookie!=null){
                    cookies=receivedCookie;

                }

                try {
                    InputStream inStream = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
                    StringBuilder builder = new StringBuilder();
                    String line = null;
                    try {
                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                            builder.append("\n"); // append a new line
                        }
                    } catch (IOException e) {
                    } finally {
                        try {
                            inStream.close();
                        } catch (IOException e) {
                        }
                    }
                    // System.out.println(builder.toString());
                    response = builder.toString();
                    response_params.put("response", response);
                    Log.v("Response Message", response);
                } catch (IOException ex) {


                }

            } else {
                status = Integer.valueOf(SenseConstants.Request.REQUEST_SUCCESSFUL);
            }


        } catch (Exception e) {
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return response_params;
    }

    public HostnameVerifier SERVER_HOST = new HostnameVerifier() {
        //String allowHost = LocalRegister.getServerHost(context);
        @Override
        public boolean verify(String hostname, SSLSession session) {
            HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
            return true;
            //return hv.verify(allowHost, session);
        }
    };
}
