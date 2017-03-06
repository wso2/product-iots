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
package org.wso2.iot.integration.common;

import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This is the rest client that is used to calls to APIs.
 */
public class RestClient {

    private static final String AUTHORIZATION = "Authorization";
    private String backEndUrl;
    private String authrizationString;
    private Map<String, String> requestHeaders = new HashMap<String, String>();

    public RestClient(String backEndUrl, String contentType) {
        this.backEndUrl = backEndUrl;
        this.requestHeaders.put(Constants.CONTENT_TYPE, contentType);
    }

    public RestClient(String backEndUrl, String contentType, String authorization) {
        this.backEndUrl = backEndUrl;
        this.requestHeaders.put(Constants.CONTENT_TYPE, contentType);
        if (authorization != null || !authorization.isEmpty()) {
            this.authrizationString = authorization;
            this.requestHeaders.put(AUTHORIZATION, authorization);
        }
    }

    public String getAuthrizationString() {
        return authrizationString;
    }

    public void setAuthrizationString(String authrizationString) {
        this.authrizationString = authrizationString;
    }

    public void setHttpHeader(String headerName, String value) {
        this.requestHeaders.put(headerName, value);
    }

    public String getHttpHeader(String headerName) {
        return this.requestHeaders.get(headerName);
    }

    public void removeHttpHeader(String headerName) {
        this.requestHeaders.remove(headerName);
    }

    public HttpResponse post(String endpoint, String body) throws MalformedURLException, AutomationFrameworkException {
        return HttpRequestUtil.doPost(new URL(backEndUrl + endpoint), body, requestHeaders);
    }

    public HttpResponse put(String endpoint, String body) throws Exception {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new URL(backEndUrl + endpoint).openConnection();
            try {
                urlConnection.setRequestMethod("PUT");
            } catch (ProtocolException e) {
                throw new Exception("Shouldn\'t happen: HttpURLConnection doesn\'t support POST?? " + e.getMessage(),
                                    e);
            }

            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setAllowUserInteraction(false);
            Iterator entryIterator = this.requestHeaders.entrySet().iterator();
            while (entryIterator.hasNext()) {
                Map.Entry sb = (Map.Entry) entryIterator.next();
                urlConnection.setRequestProperty((String) sb.getKey(), (String) sb.getValue());
            }
            OutputStream outputStream = urlConnection.getOutputStream();
            try {
                OutputStreamWriter sb1 = new OutputStreamWriter(outputStream, "UTF-8");
                sb1.write(body);
                sb1.close();
            } catch (IOException e) {
                throw new Exception("IOException while sending data " + e.getMessage(), e);
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
            }
            StringBuilder sb2 = new StringBuilder();
            BufferedReader rd = null;
            try {
                rd = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream(), Charset.defaultCharset()));
                String itr;
                while ((itr = rd.readLine()) != null) {
                    sb2.append(itr);
                }
            } catch (FileNotFoundException e) {
                throw new Exception("IOException while reading put request data " + e.getMessage(), e);
            } finally {
                if (rd != null) {
                    rd.close();
                }
            }
            Iterator iterator = urlConnection.getHeaderFields().keySet().iterator();
            HashMap responseHeaders = new HashMap();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                if (key != null) {
                    responseHeaders.put(key, urlConnection.getHeaderField(key));
                }
            }
            HttpResponse httpResponse =
                    new HttpResponse(sb2.toString(), urlConnection.getResponseCode(), responseHeaders);
            return httpResponse;
        } catch (IOException e) {
            throw new Exception("Connection error (Is server running at " + endpoint + " ?): " + e.getMessage(), e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }

        }
    }

    public HttpResponse get(String endpoint) throws Exception {
        return HttpRequestUtil.doGet(backEndUrl + endpoint, requestHeaders);
    }

    public HttpResponse delete(String endpoint) throws Exception {
        HttpURLConnection conn = null;

        HttpResponse httpResponse1;
        try {
            URL url = new URL(backEndUrl + endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setDoOutput(true);
            conn.setReadTimeout(30000);
            Iterator entryIterator = this.requestHeaders.entrySet().iterator();

            while (entryIterator.hasNext()) {
                Map.Entry rd = (Map.Entry) entryIterator.next();
                conn.setRequestProperty((String) rd.getKey(), (String) rd.getValue());
            }

            conn.connect();
            StringBuilder sb1 = new StringBuilder();
            BufferedReader rd1 = null;

            HttpResponse httpResponse;
            try {
                rd1 = new BufferedReader(new InputStreamReader(conn.getInputStream(), Charset.defaultCharset()));

                String ignored;
                while ((ignored = rd1.readLine()) != null) {
                    sb1.append(ignored);
                }

                httpResponse = new HttpResponse(sb1.toString(), conn.getResponseCode());
                httpResponse.setResponseMessage(conn.getResponseMessage());
            } catch (IOException e) {
                rd1 = new BufferedReader(new InputStreamReader(conn.getErrorStream(), Charset.defaultCharset()));

                String line;
                while ((line = rd1.readLine()) != null) {
                    sb1.append(line);
                }

                httpResponse = new HttpResponse(sb1.toString(), conn.getResponseCode());
                httpResponse.setResponseMessage(conn.getResponseMessage());
            } finally {
                if (rd1 != null) {
                    rd1.close();
                }

            }

            httpResponse1 = httpResponse;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }

        }

        return httpResponse1;
    }
}