/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.mdm.qsg.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.mdm.qsg.dto.ClientCredentials;
import org.wso2.mdm.qsg.dto.EMMQSGConfig;
import org.wso2.mdm.qsg.dto.HTTPResponse;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class holds the utility methods used by the EMM-QSG package.
 */
public class QSGUtils {

    public static EMMQSGConfig initConfig() {
        Properties props = new Properties();
        InputStream input = null;
        EMMQSGConfig emmConfig = null;
        try {
            input = new FileInputStream("config.properties");
            // load a properties file and set the properties
            props.load(input);
            emmConfig = EMMQSGConfig.getInstance();
            emmConfig.setEmmHost(props.getProperty("emm-host"));
            emmConfig.setDcrEndPoint(props.getProperty("dcr-endpoint"));
            emmConfig.setOauthEndPoint(props.getProperty("oauth-endpoint"));
            emmConfig.setUsername(props.getProperty("username"));
            emmConfig.setPassword(props.getProperty("password"));
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return emmConfig;
    }

    private static ClientCredentials getClientCredentials() {
        ClientCredentials clientCredentials = null;
        HashMap<String, String> headers = new HashMap<String, String>();
        String dcrEndPoint = EMMQSGConfig.getInstance().getDcrEndPoint();
        //Set the DCR payload
        JSONObject obj = new JSONObject();
        obj.put("owner", "admin");
        obj.put("clientName", "qsg");
        obj.put("grantType", "refresh_token password client_credentials");
        obj.put("tokenScope", "user:view,user:manage,user:admin:reset-password,role:view,role:manage,policy:view," +
                              "policy:manage,application:manage,appm:create,appm:publish,appm:update,appm:read");
        //Set the headers
        headers.put(Constants.Header.CONTENT_TYPE, Constants.ContentType.APPLICATION_JSON);
        HTTPResponse httpResponse = HTTPInvoker.sendHTTPPost(dcrEndPoint, obj.toJSONString(), headers);
        if (httpResponse.getResponseCode() == Constants.HTTPStatus.CREATED) {
            try {
                JSONObject jsonObject = (JSONObject) new JSONParser().parse(httpResponse.getResponse());
                clientCredentials = new ClientCredentials();
                clientCredentials.setClientKey((String) jsonObject.get("client_id"));
                clientCredentials.setClientSecret((String) jsonObject.get("client_secret"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return clientCredentials;
    }

    public static String getOAuthToken() {
        QSGUtils.initConfig();
        ClientCredentials clientCredentials = getClientCredentials();
        String authorizationStr = clientCredentials.getClientKey() + ":" + clientCredentials.getClientSecret();
        String authHeader = "Basic " + new String(Base64.encodeBase64(authorizationStr.getBytes()));
        HashMap<String, String> headers = new HashMap<String, String>();
        //Set the form params
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("username", EMMQSGConfig.getInstance().getUsername()));
        urlParameters.add(new BasicNameValuePair("password", EMMQSGConfig.getInstance().getPassword()));
        urlParameters.add(new BasicNameValuePair("grant_type", "password"));
        urlParameters.add(new BasicNameValuePair("scope",
                                                 "user:view user:manage user:admin:reset-password role:view role:manage policy:view policy:manage " +
                                                 "application:manage appm:administration appm:create appm:publish appm:update appm:read"));
        //Set the headers
        headers.put(Constants.Header.CONTENT_TYPE, Constants.ContentType.APPLICATION_URL_ENCODED);
        headers.put(Constants.Header.AUTH, authHeader);
        HTTPResponse httpResponse = HTTPInvoker
                .sendHTTPPostWithURLParams(EMMQSGConfig.getInstance().getOauthEndPoint(), urlParameters, headers);
        if (httpResponse.getResponseCode() == Constants.HTTPStatus.OK) {
            try {
                JSONObject jsonObject = (JSONObject) new JSONParser().parse(httpResponse.getResponse());
                return (String) jsonObject.get("access_token");
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static boolean isValidEmailAddress(String email) {
        String emailPattern =
                "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        Pattern p = Pattern.compile(emailPattern);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    public static String getResourceId(String resourcePath) {
        return resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
    }
}
