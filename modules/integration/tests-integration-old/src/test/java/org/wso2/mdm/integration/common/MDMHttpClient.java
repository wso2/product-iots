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
package org.wso2.mdm.integration.common;


import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class creates a customised Http Client Class
 */
public class MDMHttpClient {

    private static final String AUTHORIZATION = "Authorization";
    private static Log log = LogFactory.getLog(MDMHttpClient.class);
    private String backEndUrl;
    private String authrizationString;
    private Map<String, String> requestHeaders = new HashMap<String, String>();

    public MDMHttpClient(String backEndUrl, String contentType, String authorization) {

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

    public MDMResponse post(String endpoint, String body) {
        HttpClient client = new HttpClient();
        try {
            ProtocolSocketFactory socketFactory = new EasySSLProtocolSocketFactory();
            Protocol https = new Protocol("https", socketFactory, 9443);
            Protocol.registerProtocol("https", https);
            String url = backEndUrl + endpoint;
            PostMethod method = new PostMethod(url);
            method.setRequestHeader(AUTHORIZATION, authrizationString);
            StringRequestEntity requestEntity = new StringRequestEntity(body,
                                                                        requestHeaders.get(Constants.CONTENT_TYPE), Constants.UTF8);
            method.setRequestEntity(requestEntity);
            MDMResponse mdmResponse = new MDMResponse();
            mdmResponse.setStatus(client.executeMethod(method));
            mdmResponse.setBody(method.getResponseBodyAsString());
            return mdmResponse;

        } catch (GeneralSecurityException e) {
            log.error("Failure occurred at MDMResponse post for GeneralSecurityException", e);
        } catch (IOException e) {
            log.error("Failure occured at MDMResponse post for IOException", e);
        }
        return null;
    }

    public MDMResponse put(String endpoint, String body) {
        HttpClient client = new HttpClient();
        try {
            ProtocolSocketFactory socketFactory = new EasySSLProtocolSocketFactory();
            Protocol https = new Protocol("https", socketFactory, 9443);
            Protocol.registerProtocol("https", https);
            String url = backEndUrl + endpoint;
            PutMethod method = new PutMethod(url);
            method.setRequestHeader(AUTHORIZATION, authrizationString);
            StringRequestEntity requestEntity = new StringRequestEntity(
                    body, requestHeaders.get(Constants.CONTENT_TYPE), Constants.UTF8);
            method.setRequestEntity(requestEntity);
            MDMResponse mdmResponse = new MDMResponse();
            mdmResponse.setStatus(client.executeMethod(method));
            mdmResponse.setBody(method.getResponseBodyAsString());
            return mdmResponse;

        } catch (GeneralSecurityException e) {
            log.error("Failure occurred at MDMResponse put for GeneralSecurityException", e);
        } catch (IOException e) {
            log.error("Failure occurred at MDMResponse put for IO Exception", e);
        }
        return null;
    }

    public MDMResponse get(String endpoint) {
        HttpClient client = new HttpClient();
        try {
            ProtocolSocketFactory socketFactory = new EasySSLProtocolSocketFactory();

            Protocol https = new Protocol("https", socketFactory, 9443);
            Protocol.registerProtocol("https", https);
            String url = backEndUrl + endpoint;
            GetMethod method = new GetMethod(url);
            method.setRequestHeader(AUTHORIZATION, authrizationString);
            method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                                            new DefaultHttpMethodRetryHandler(3, false));
            MDMResponse mdmResponse = new MDMResponse();
            mdmResponse.setStatus(client.executeMethod(method));
            mdmResponse.setBody(new String(method.getResponseBody()));
            return mdmResponse;

        } catch (GeneralSecurityException e) {
            log.error("Failure occurred at MDMResponse get for GeneralSecurityException", e);
        } catch (IOException e) {
            log.error("Failure occurred at MDMResponse get for IOException", e);
        }

        return null;
    }

    public MDMResponse delete(String endpoint) {

        HttpClient client = new HttpClient();

        try {
            ProtocolSocketFactory socketFactory = new EasySSLProtocolSocketFactory();

            Protocol https = new Protocol("https", socketFactory, 9443);
            Protocol.registerProtocol("https", https);

            String url = backEndUrl + endpoint;

            DeleteMethod method = new DeleteMethod(url);
            method.setRequestHeader(AUTHORIZATION, authrizationString);
            method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                                            new DefaultHttpMethodRetryHandler(3, false));

            MDMResponse mdmResponse = new MDMResponse();
            mdmResponse.setStatus(client.executeMethod(method));
            mdmResponse.setBody(method.getResponseBodyAsString());
            return mdmResponse;

        } catch (GeneralSecurityException e) {
            log.error("Failure occurred at MDMResponse delete for GeneralSecurityException", e);
        } catch (IOException e) {
            log.error("Failure occurred at MDMResponse delete for IOException", e);
        }
        return null;
    }
}