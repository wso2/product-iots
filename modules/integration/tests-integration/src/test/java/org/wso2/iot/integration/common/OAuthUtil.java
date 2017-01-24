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

import org.apache.commons.net.util.Base64;
import org.json.JSONObject;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;

/**
 * This Util class holds method necessary to get an OAuth token.
 */
public class OAuthUtil {

    public static String getOAuthToken(String backendHTTPURL, String backendHTTPSURL)
            throws Exception {
        RestClient client = new RestClient(backendHTTPURL, Constants.APPLICATION_JSON);
        HttpResponse oAuthData = client.post(Constants.DynamicClientAuthentication.REGISTRATION_ENDPOINT,
                                             Constants.DynamicClientAuthentication.DYNAMIC_CLIENT_REGISTRATION_PAYLOAD);
        JSONObject jsonObj = new JSONObject(oAuthData.getData());
        String clientId = jsonObj.get(Constants.OAUTH_CLIENT_ID).toString();
        String clientSecret = jsonObj.get(Constants.OAUTH_CLIENT_SECRET).toString();
        byte[] bytesEncoded = Base64.encodeBase64((clientId + ":" + clientSecret).getBytes());
        String basicAuthString = "Basic " + new String(bytesEncoded);
        //Initiate a RestClient to get OAuth token
        client = new RestClient(backendHTTPSURL, Constants.APPLICATION_URL_ENCODED, basicAuthString);
        oAuthData = client.post(Constants.DynamicClientAuthentication.TOKEN_ENDPOINT,
                                Constants.DynamicClientAuthentication.OAUTH_TOKEN_PAYLOAD);
        jsonObj = new JSONObject(oAuthData.getData());
        return jsonObj.get(Constants.OAUTH_ACCESS_TOKEN).toString();
    }
}
