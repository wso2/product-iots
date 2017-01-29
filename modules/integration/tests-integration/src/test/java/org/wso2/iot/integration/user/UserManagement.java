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

package org.wso2.iot.integration.user;

import junit.framework.Assert;
import org.apache.commons.httpclient.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.iot.integration.common.*;

/**
 * This class contains integration tests for user management backend services.
 */
public class UserManagement extends TestBase {

    private RestClient client;

    @BeforeClass(alwaysRun = true, groups = { Constants.UserManagement.USER_MANAGEMENT_GROUP})
    public void initTest() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        Thread.sleep(80000);
        String accessTokenString = "Bearer " + OAuthUtil.getOAuthToken(backendHTTPSURL, backendHTTPSURL);
        this.client = new RestClient(backendHTTPSURL, Constants.APPLICATION_JSON, accessTokenString);
    }

    @Test(description = "Test add user.")
    public void testAddUser() throws Exception {
        HttpResponse response = client.post(Constants.UserManagement.USER_ADD_ENDPOINT,
                                            PayloadGenerator.getJsonPayload(
                                                    Constants.UserManagement.USER_PAYLOAD_FILE_NAME,
                                                    Constants.HTTP_METHOD_POST).toString());
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
        AssertUtil.jsonPayloadCompare(PayloadGenerator.getJsonPayload(
                Constants.UserManagement.USER_RESPONSE_PAYLOAD_FILE_NAME, Constants.HTTP_METHOD_POST).toString(),
                response.getData().toString(), true);
    }

    @Test(description = "Test update user.", dependsOnMethods = {"testAddUser"})
    public void testUpdateUser() throws Exception {
        String url = GetURL(Constants.UserManagement.USER_ENDPOINT);
        HttpResponse response = client.put(url, PayloadGenerator.getJsonPayload(
                Constants.UserManagement.USER_PAYLOAD_FILE_NAME, Constants.HTTP_METHOD_PUT).toString());
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
        AssertUtil.jsonPayloadCompare(PayloadGenerator.getJsonPayload(
                Constants.UserManagement.USER_RESPONSE_PAYLOAD_FILE_NAME, Constants.HTTP_METHOD_PUT).toString(),
                response.getData().toString(), true);

    }
    @Test(description = "Test view user.", dependsOnMethods = {"testUpdateUser"})
    public void testViewUser() throws Exception {
        String url = GetURL(Constants.UserManagement.VIEW_USER_ENDPOINT);
        HttpResponse response = client.get(url);
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        AssertUtil.jsonPayloadCompare(PayloadGenerator.getJsonPayload(
                Constants.UserManagement.USER_RESPONSE_PAYLOAD_FILE_NAME, Constants.HTTP_METHOD_GET).toString(),
                response.getData().toString(), true);
    }

    @Test(description = "Test remove user.", dependsOnMethods = {"testViewUser"})
    public void testRemoveUser() throws Exception {
        String url = GetURL(Constants.UserManagement.USER_ENDPOINT);
        HttpResponse response = client.delete(url);
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        AssertUtil.jsonPayloadCompare(PayloadGenerator.getJsonPayload(
                Constants.UserManagement.USER_RESPONSE_PAYLOAD_FILE_NAME,
                                                                      Constants.HTTP_METHOD_DELETE).toString(),
                response.getData().toString(), true);

    }

    private String GetURL(String endPoint) {
        return endPoint + "?username=" + Constants.UserManagement.USER_NAME;
    }

}
