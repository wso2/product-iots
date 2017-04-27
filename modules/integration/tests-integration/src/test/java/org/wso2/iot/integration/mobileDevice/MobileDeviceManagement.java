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
package org.wso2.iot.integration.mobileDevice;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import junit.framework.Assert;
import org.apache.commons.httpclient.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.iot.integration.common.*;

/**
 * This class contains integration tests for API Device management backend services.
 */
public class MobileDeviceManagement extends TestBase {
    private RestClient client;

    @Factory(dataProvider = "userModeProvider")
    public MobileDeviceManagement(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true, groups = { Constants.MobileDeviceManagement.MOBILE_DEVICE_MANAGEMENT_GROUP})
    public void initTest() throws Exception {
        super.init(userMode);
        this.client = new RestClient(backendHTTPSURL, Constants.APPLICATION_JSON, accessTokenString);
        JsonObject enrollmentData = PayloadGenerator
                .getJsonPayload(Constants.AndroidEnrollment.ENROLLMENT_PAYLOAD_FILE_NAME, Constants.HTTP_METHOD_POST);
        enrollmentData.addProperty(Constants.DEVICE_IDENTIFIER_KEY, Constants.DEVICE_ID);
        client.post(Constants.AndroidEnrollment.ENROLLMENT_ENDPOINT, enrollmentData.toString());
    }

    @Test(description = "Test getting devices")
    public void testViewDevices() throws Exception {
        int expectedCount = this.userMode == TestUserMode.TENANT_ADMIN ? 1 : 23;
        HttpResponse response = client.get(Constants.MobileDeviceManagement.GET_ALL_DEVICES_ENDPOINT);
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        JsonObject devices = new JsonParser().parse(response.getData()).getAsJsonObject();
        Assert.assertEquals("Expected device count is not received", expectedCount, devices.get("count").getAsInt());
    }

    @Test(description = "Test getting devices")
    public void testGetUserDevices() throws Exception {
        int expectedCount = this.userMode == TestUserMode.TENANT_ADMIN ? 1 : 13;
        HttpResponse response = client.get(Constants.MobileDeviceManagement.GET_ALL_DEVICES_ENDPOINT
                + Constants.MobileDeviceManagement.USER_DEVICE_ENDPOINT);
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        JsonObject devices = new JsonParser().parse(response.getData()).getAsJsonObject();
        Assert.assertEquals("Expected device count is not received", expectedCount, devices.get("count").getAsInt());
    }

    @Test(description = "Test Advance search")
    public void testAdvancedSearch() throws Exception {
        JsonArray pendingOperationsData = PayloadGenerator
                .getJsonArray(Constants.AndroidEnrollment.ENROLLMENT_PAYLOAD_FILE_NAME,
                        Constants.AndroidEnrollment.GET_PENDING_OPERATIONS_METHOD);
        JsonArray newPayload = new JsonArray();
        HttpResponse response = client.put(Constants.AndroidEnrollment.ENROLLMENT_ENDPOINT + "/" + Constants.DEVICE_ID
                + "/pending-operations", pendingOperationsData.toString());
        JsonArray pendingOperations = new JsonParser().parse(response.getData()).getAsJsonArray();

        for (JsonElement pendingOperation : pendingOperations) {
            JsonObject jsonObject = pendingOperation.getAsJsonObject();

            if (jsonObject.get("code").getAsString().equals("DEVICE_INFO")) {
                jsonObject.addProperty("operationResponse", PayloadGenerator
                        .getJsonPayload(Constants.MobileDeviceManagement.REQUEST_PAYLOAD_FILE_NAME,
                                Constants.MobileDeviceManagement.UPDATE_PAYLOAD_OPERATION).toString());
                jsonObject.addProperty("status", "COMPLETED");
                newPayload.add(jsonObject);
                break;
            }
        }
        client.put(Constants.AndroidEnrollment.ENROLLMENT_ENDPOINT + "/" + Constants.DEVICE_ID + "/pending-operations",
                newPayload.toString());
        response = client.post(Constants.MobileDeviceManagement.GET_ALL_DEVICES_ENDPOINT
                + Constants.MobileDeviceManagement.ADVANCE_SEARCH_ENDPOINT, PayloadGenerator
                .getJsonPayload(Constants.MobileDeviceManagement.REQUEST_PAYLOAD_FILE_NAME,
                        Constants.MobileDeviceManagement.ADVANCE_SEARCH_OPERATION).toString());
        JsonObject devices = new JsonParser().parse(response.getData()).getAsJsonObject();
        Assert.assertEquals("Expected device count is not received", 1, devices.get("devices").getAsJsonArray().size());
    }

}
