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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import junit.framework.Assert;
import org.apache.commons.httpclient.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.iot.integration.common.AssertUtil;
import org.wso2.iot.integration.common.Constants;
import org.wso2.iot.integration.common.PayloadGenerator;
import org.wso2.iot.integration.common.RestClient;
import org.wso2.iot.integration.common.TestBase;
import org.wso2.iot.integration.samples.VirtualFireAlarmTestCase;
import static org.wso2.iot.integration.samples.VirtualFireAlarmTestCase.*;

/**
 * This class contains integration tests for user management backend services.
 */
public class UserManagement extends TestBase {
    private String NON_EXISTING_USERNAME = "non_exiting";
    private RestClient client;

    @Factory(dataProvider = "userModeProvider")
    public UserManagement(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true, groups = { Constants.UserManagement.USER_MANAGEMENT_GROUP})
    public void initTest() throws Exception {
        super.init(userMode);
        this.client = new RestClient(backendHTTPSURL, Constants.APPLICATION_JSON, accessTokenString);
    }

    @Test(description = "Test add user.")
    public void testAddUser() throws Exception {
        // Add a user with the details and check whether that user is added correctly.
        HttpResponse response = client.post(Constants.UserManagement.USER_ENDPOINT, PayloadGenerator
                .getJsonPayload(Constants.UserManagement.USER_PAYLOAD_FILE_NAME, Constants.HTTP_METHOD_POST)
                .toString());
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
        AssertUtil.jsonPayloadCompare(PayloadGenerator
                .getJsonPayload(Constants.UserManagement.USER_RESPONSE_PAYLOAD_FILE_NAME, Constants.HTTP_METHOD_POST)
                .toString(), response.getData(), true);
    }

    @Test(description = "Test update user.", dependsOnMethods = {"testAddUser"})
    public void testUpdateUser() throws Exception {
        // Update a existing user
        String url = Constants.UserManagement.USER_ENDPOINT + "/" + Constants.UserManagement.USER_NAME;
        HttpResponse response = client.put(url, PayloadGenerator
                .getJsonPayload(Constants.UserManagement.USER_PAYLOAD_FILE_NAME, Constants.HTTP_METHOD_PUT).toString());
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        AssertUtil.jsonPayloadCompare(PayloadGenerator
                .getJsonPayload(Constants.UserManagement.USER_RESPONSE_PAYLOAD_FILE_NAME, Constants.HTTP_METHOD_PUT)
                .toString(), response.getData(), true);
    }

    @Test(description = "Test view user.", dependsOnMethods = {"testUpdateUser"})
    public void testViewUser() throws Exception {
        String url = Constants.UserManagement.USER_ENDPOINT + "/" + Constants.UserManagement.USER_NAME;
        HttpResponse response = client.get(url);
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        AssertUtil.jsonPayloadCompare(PayloadGenerator
                .getJsonPayload(Constants.UserManagement.USER_RESPONSE_PAYLOAD_FILE_NAME, Constants.HTTP_METHOD_GET)
                .toString(), response.getData(), true);
    }

    @Test(description = "Test getting user roles.", dependsOnMethods = {"testViewUser"})
    public void testGetUserRoles() throws Exception {
        String url = Constants.UserManagement.USER_ENDPOINT + "/" + Constants.UserManagement.USER_NAME + "/roles";
        HttpResponse response = client.get(url);
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        AssertUtil.jsonPayloadCompare(PayloadGenerator
                .getJsonPayload(Constants.UserManagement.USER_RESPONSE_PAYLOAD_FILE_NAME,
                        Constants.UserManagement.GET_ROLES_METHOD).toString(), response.getData(), true);
        url = Constants.UserManagement.USER_ENDPOINT + "/" + NON_EXISTING_USERNAME + "/roles";
        response = client.get(url);
        Assert.assertEquals(HttpStatus.SC_NOT_FOUND, response.getResponseCode());
    }

    @Test(description = "Test the API that checks whether user exist.", dependsOnMethods = {"testGetUserRoles"})
    public void testIsUserExist() throws Exception {
        String url =
                Constants.UserManagement.USER_ENDPOINT + "/checkUser?username=" + Constants.UserManagement.USER_NAME;
        HttpResponse response = client.get(url);
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        Assert.assertEquals(
                "CheckUser API return false even the user with the username " + Constants.UserManagement.USER_NAME
                        + "exists", true, Boolean.parseBoolean(response.getData()));
        url = Constants.UserManagement.USER_ENDPOINT + "/checkUser?username=" + NON_EXISTING_USERNAME;
        response = client.get(url);
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        Assert.assertEquals("CheckUser API return true but user with " + NON_EXISTING_USERNAME + "exists", false,
                Boolean.parseBoolean(response.getData()));
    }


    @Test(description = "Test whether correct user count is returned.", dependsOnMethods = {"testIsUserExist"})
    public void testUserCount() throws Exception {
        int expectedCount = this.userMode == TestUserMode.TENANT_ADMIN ? 4 : 15;
        String url = Constants.UserManagement.USER_ENDPOINT + "/count";
        HttpResponse response = client.get(url);
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        JsonObject jsonElement = new JsonParser().parse(response.getData()).getAsJsonObject();
        Assert.assertEquals("Actual user count does not match with the returned user count", expectedCount,
                jsonElement.get("count").getAsInt());
    }

    @Test(description = "Test whether the API that is used to get the users returns all the user details.",
            dependsOnMethods = {"testUserCount"})
    public void testGetUsers() throws Exception {
        int expectedCount = this.userMode == TestUserMode.TENANT_ADMIN ? 4 : 15;
        String url = Constants.UserManagement.USER_ENDPOINT + "/?offset=0&limit=100";
        HttpResponse response = client.get(url);
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        JsonObject jsonElement = new JsonParser().parse(response.getData()).getAsJsonObject();
        Assert.assertEquals("All the users list is not returned", expectedCount,
                jsonElement.get("users").getAsJsonArray().size());
    }

    @Test(description = "Test whether the API that is used to get the users with particular filter returns all the "
            + "user details that satisfy particular filter.", dependsOnMethods = {"testGetUsers"})
    public void testSearchUserNames() throws Exception {
        String url = Constants.UserManagement.USER_ENDPOINT + "/search/usernames?filter="
                + Constants.UserManagement.USER_NAME;
        HttpResponse response = client.get(url);
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        JsonArray jsonArray = new JsonParser().parse(response.getData()).getAsJsonArray();
        Assert.assertEquals("Relevant filtered user list in not returned correctly.", 1, jsonArray.size());

        url = Constants.UserManagement.USER_ENDPOINT + "/search/usernames?filter=" + NON_EXISTING_USERNAME;
        response = client.get(url);
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        jsonArray = new JsonParser().parse(response.getData()).getAsJsonArray();
        Assert.assertEquals("Relevant filtered user list in not returned correctly. Return a list of users for "
                + "non-existing username", 0, jsonArray.size());
    }

    @Test(description = "Test remove user.", dependsOnMethods = {"testSearchUserNames"})
    public void testRemoveUser() throws Exception {
        String url = Constants.UserManagement.USER_ENDPOINT + "/" + Constants.UserManagement.USER_NAME;
        HttpResponse response = client.delete(url);
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
    }

    // Test case related to virtual fire alarm added here as the batch cron runs for every 5 minutes and rather than
    // waiting for that we can check them in a latter test cases
    @Test(description = "Test whether data that is published is stored in analytics event table", dependsOnMethods =
            {"testRemoveUser"} )
    public void testBatchDataPersistence() throws Exception {
        String deviceId1 =
                this.userMode == TestUserMode.TENANT_ADMIN ? tenantDeviceId1 : VirtualFireAlarmTestCase.deviceId1;
        String deviceId2 =
                this.userMode == TestUserMode.TENANT_ADMIN ? tenantDeviceId2 : VirtualFireAlarmTestCase.deviceId2;

        long MilliSecondDifference = System.currentTimeMillis() - VirtualFireAlarmTestCase.currentTime;
        if (MilliSecondDifference < 300000) {
            Thread.sleep(300000 - MilliSecondDifference);
        }
        String url = Constants.VirtualFireAlarmConstants.STATS_ENDPOINT + "/" + deviceId1;
        url += "?from=" + (VirtualFireAlarmTestCase.currentTime - 300000)/1000 + "&to=" + System.currentTimeMillis()
                /1000;
        HttpResponse response = client.get(url);
        JsonArray jsonArray = new JsonParser().parse(response.getData()).getAsJsonArray();
        Assert.assertEquals(
                "Published event for the device with the id " + deviceId1 + " is not inserted to "
                        + "analytics table", HttpStatus.SC_OK, response.getResponseCode());
        Assert.assertEquals(
                "Published event for the device with the id " + deviceId1 + " is not inserted to analytics table", 1,
                jsonArray.size());

        url = Constants.VirtualFireAlarmConstants.STATS_ENDPOINT + "/" + deviceId2;
        url += "?from=" + (VirtualFireAlarmTestCase.currentTime - 300000)/1000 + "&to=" + System.currentTimeMillis()
                /1000;
        response = client.get(url);
        jsonArray = new JsonParser().parse(response.getData()).getAsJsonArray();
        Assert.assertEquals(
                "Published event for the device with the id " + deviceId2 + " is not inserted to "
                        + "analytics table", HttpStatus.SC_OK, response.getResponseCode());
        Assert.assertEquals(
                "Published event for the device with the id " + deviceId2 + " is not inserted to analytics table", 1,
                jsonArray.size());
    }

    @Test(description = "Test whether the API that is used to change the password works as expected.",
            dependsOnMethods = {"testBatchDataPersistence"})
    public void testChangePassword() throws Exception {
        String url = Constants.UserManagement.USER_ENDPOINT + "/credentials";
        HttpResponse response = client.put(url, PayloadGenerator
                .getJsonPayload(Constants.UserManagement.USER_PAYLOAD_FILE_NAME,
                        Constants.UserManagement.RESET_PASSWORD_PAYLOAD).toString());
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        Assert.assertEquals("Password of the user cannot be changed",
                "\"UserImpl password by " + "username: admin was successfully changed.\"", response.getData());
    }
}
