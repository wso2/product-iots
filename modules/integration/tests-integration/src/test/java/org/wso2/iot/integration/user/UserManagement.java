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
import org.wso2.iot.integration.common.AssertUtil;
import org.wso2.iot.integration.common.Constants;
import org.wso2.iot.integration.common.OAuthUtil;
import org.wso2.iot.integration.common.PayloadGenerator;
import org.wso2.iot.integration.common.RestClient;
import org.wso2.iot.integration.common.TestBase;

/**
 * This class contains integration tests for user management backend services.
 */
public class UserManagement extends TestBase {
    private String NON_EXISTING_USERNAME = "non_exiting";
    private RestClient client;

    @BeforeClass(alwaysRun = true, groups = { Constants.UserManagement.USER_MANAGEMENT_GROUP})
    public void initTest() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String accessTokenString = "Bearer " + OAuthUtil.getOAuthToken(backendHTTPSURL, backendHTTPSURL);
        this.client = new RestClient(backendHTTPSURL, Constants.APPLICATION_JSON, accessTokenString);
    }

    @Test(description = "Test add user.")
    public void testAddUser() throws Exception {
        HttpResponse response = client.post(Constants.UserManagement.USER_ENDPOINT,
                                            PayloadGenerator.getJsonPayload(Constants.UserManagement.USER_PAYLOAD_FILE_NAME,
                                                                            Constants.HTTP_METHOD_POST).toString());
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
        AssertUtil.jsonPayloadCompare(PayloadGenerator.getJsonPayload(Constants.UserManagement.USER_RESPONSE_PAYLOAD_FILE_NAME,
                                                                      Constants.HTTP_METHOD_POST).toString(), response.getData().toString(), true);
    }

    @Test(description = "Test update user.", dependsOnMethods = {"testAddUser"})
    public void testUpdateUser() throws Exception {
        String url = Constants.UserManagement.USER_ENDPOINT + "/" + Constants.UserManagement.USER_NAME;
        HttpResponse response = client.put(url,
                                           PayloadGenerator.getJsonPayload(Constants.UserManagement.USER_PAYLOAD_FILE_NAME,
                                                                           Constants.HTTP_METHOD_PUT).toString());
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        AssertUtil.jsonPayloadCompare(PayloadGenerator.getJsonPayload(Constants.UserManagement.USER_RESPONSE_PAYLOAD_FILE_NAME,
                                                                      Constants.HTTP_METHOD_PUT).toString(), response.getData().toString(), true);

    }

    @Test(description = "Test view user.", dependsOnMethods = {"testUpdateUser"})
    public void testViewUser() throws Exception {
        String url = Constants.UserManagement.USER_ENDPOINT + "/" + Constants.UserManagement.USER_NAME;
        HttpResponse response = client.get(url);
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        AssertUtil.jsonPayloadCompare(PayloadGenerator.getJsonPayload(Constants.UserManagement.USER_RESPONSE_PAYLOAD_FILE_NAME,
                                                                      Constants.HTTP_METHOD_GET).toString(), response.getData().toString(), true);
    }

    @Test(description = "Test getting user roles.", dependsOnMethods = {"testViewUser"})
    public void testGetUserRoles() throws Exception {
        String url = Constants.UserManagement.USER_ENDPOINT + "/" + Constants.UserManagement.USER_NAME + "/roles";
        HttpResponse response = client.get(url);
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        AssertUtil.jsonPayloadCompare(PayloadGenerator.getJsonPayload(Constants.UserManagement.USER_RESPONSE_PAYLOAD_FILE_NAME,
                Constants.UserManagement.GET_ROLES_METHOD).toString(), response.getData().toString(), true);
    }

    @Test(description = "Test remove user.", dependsOnMethods = {"testGetUserRoles"})
    public void testRemoveUser() throws Exception {
        String url = Constants.UserManagement.USER_ENDPOINT + "/" + Constants.UserManagement.USER_NAME;
        HttpResponse response = client.delete(url);
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
    }
}
