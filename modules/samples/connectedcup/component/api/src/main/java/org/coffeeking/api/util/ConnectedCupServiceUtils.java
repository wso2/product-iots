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

package org.coffeeking.api.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.coffeeking.connectedcup.plugin.constants.ConnectedCupConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.data.publisher.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.analytics.data.publisher.service.EventsPublisherService;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;

import javax.ws.rs.HttpMethod;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

public class ConnectedCupServiceUtils {
    private static final Log log = LogFactory.getLog(ConnectedCupServiceUtils.class);

    private static final String TEMPERATURE_STREAM_DEFINITION = "org.wso2.iot.devices.temperature";
    private static final String COFFEE_LEVEL_STREAM_DEFINITION = "org.wso2.iot.devices.coffeelevel";

    public static String sendCommandViaHTTP(final String deviceHTTPEndpoint, String urlContext,
                                            boolean fireAndForgot) throws DeviceManagementException {

        String responseMsg = "";
        String urlString = ConnectedCupConstants.URL_PREFIX + deviceHTTPEndpoint + urlContext;

        if (log.isDebugEnabled()) {
            log.debug(urlString);
        }

        if (!fireAndForgot) {
            HttpURLConnection httpConnection = getHttpConnection(urlString);

            try {
                httpConnection.setRequestMethod(HttpMethod.GET);
            } catch (ProtocolException e) {
                String errorMsg =
                        "Protocol specific error occurred when trying to set method to GET" +
                        " for:" + urlString;
                log.error(errorMsg);
                throw new DeviceManagementException(errorMsg, e);
            }

            responseMsg = readResponseFromGetRequest(httpConnection);

        } else {
            CloseableHttpAsyncClient httpclient = null;
            try {

                httpclient = HttpAsyncClients.createDefault();
                httpclient.start();
                HttpGet request = new HttpGet(urlString);
                final CountDownLatch latch = new CountDownLatch(1);
                Future<HttpResponse> future = httpclient.execute(
                        request, new FutureCallback<HttpResponse>() {
                            @Override
                            public void completed(HttpResponse httpResponse) {
                                latch.countDown();
                            }

                            @Override
                            public void failed(Exception e) {
                                latch.countDown();
                            }

                            @Override
                            public void cancelled() {
                                latch.countDown();
                            }
                        });

                latch.await();

            } catch (InterruptedException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Sync Interrupted");
                }
            } finally {
                try {
                    if (httpclient != null) {
                        httpclient.close();

                    }
                } catch (IOException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Failed on close");
                    }
                }
            }
        }

        return responseMsg;
    }

	/*	---------------------------------------------------------------------------------------
                    Utility methods relevant to creating and sending http requests
 		---------------------------------------------------------------------------------------	*/

	/* This methods creates and returns a http connection object */

    public static HttpURLConnection getHttpConnection(String urlString) throws
                                                                        DeviceManagementException {

        URL connectionUrl = null;
        HttpURLConnection httpConnection;

        try {
            connectionUrl = new URL(urlString);
            httpConnection = (HttpURLConnection) connectionUrl.openConnection();
        } catch (MalformedURLException e) {
            String errorMsg =
                    "Error occured whilst trying to form HTTP-URL from string: " + urlString;
            log.error(errorMsg);
            throw new DeviceManagementException(errorMsg, e);
        } catch (IOException e) {
            String errorMsg = "Error occured whilst trying to open a connection to: " +
                              connectionUrl.toString();
            log.error(errorMsg);
            throw new DeviceManagementException(errorMsg, e);
        }

        return httpConnection;
    }

	/* This methods reads and returns the response from the connection */

    public static String readResponseFromGetRequest(HttpURLConnection httpConnection)
            throws DeviceManagementException {
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(
                    httpConnection.getInputStream()));
        } catch (IOException e) {
            String errorMsg =
                    "There is an issue with connecting the reader to the input stream at: " +
                    httpConnection.getURL();
            log.error(errorMsg);
            throw new DeviceManagementException(errorMsg, e);
        }

        String responseLine;
        StringBuilder completeResponse = new StringBuilder();

        try {
            while ((responseLine = bufferedReader.readLine()) != null) {
                completeResponse.append(responseLine);
            }
        } catch (IOException e) {
            String errorMsg =
                    "Error occured whilst trying read from the connection stream at: " +
                    httpConnection.getURL();
            log.error(errorMsg);
            throw new DeviceManagementException(errorMsg, e);
        }
        try {
            bufferedReader.close();
        } catch (IOException e) {
            log.error(
                    "Could not succesfully close the bufferedReader to the connection at: " +
                    httpConnection.getURL());
        }

        return completeResponse.toString();
    }

    public static boolean publishToDAS(String deviceId, String sensor, float values) {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        EventsPublisherService deviceAnalyticsService = (EventsPublisherService) ctx.getOSGiService(
                EventsPublisherService.class, null);
        String owner = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        Object metdaData[] = {owner, ConnectedCupConstants.DEVICE_TYPE, deviceId, System.currentTimeMillis()};
        Object payloadData[] = {values};
        try {
            switch (sensor) {
                case "temperature":
                    deviceAnalyticsService.publishEvent(TEMPERATURE_STREAM_DEFINITION, "1.0.0", metdaData,
                                                        new Object[0], payloadData);
                    break;
                case "coffeelevel":
                    deviceAnalyticsService.publishEvent(COFFEE_LEVEL_STREAM_DEFINITION, "1.0.0", metdaData,
                                                        new Object[0], payloadData);
            }
        } catch (DataPublisherConfigurationException e) {
            return false;
        }
        return true;
    }
}
