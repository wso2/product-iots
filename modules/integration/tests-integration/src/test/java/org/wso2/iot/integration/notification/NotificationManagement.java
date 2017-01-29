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
package org.wso2.iot.integration.notification;

import com.google.gson.JsonObject;
import junit.framework.Assert;
import org.apache.commons.httpclient.HttpStatus;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.iot.integration.common.*;

/**
 * This class contains integration tests for notification management backend services.
 */
public class NotificationManagement extends TestBase {

    private RestClient client;

    @BeforeTest(alwaysRun = true, groups = {Constants.NotificationManagement.NOTIFICATION_MANAGEMENT_GROUP})
    public void initTest() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String accessTokenString = "Bearer " + OAuthUtil.getOAuthToken(backendHTTPSURL, backendHTTPSURL);
        this.client = new RestClient(backendHTTPSURL, Constants.APPLICATION_JSON, accessTokenString);
        //Enroll a device
        JsonObject enrollmentData = PayloadGenerator.getJsonPayload(
                Constants.AndroidEnrollment.ENROLLMENT_PAYLOAD_FILE_NAME,
                Constants.HTTP_METHOD_POST);
        enrollmentData.addProperty(Constants.DEVICE_IDENTIFIER_KEY, Constants.DEVICE_ID);
        client.post(Constants.AndroidEnrollment.ENROLLMENT_ENDPOINT, enrollmentData.toString());
    }

    @Test(description = "Test add notification.")
    public void testAddNotification() throws Exception {
        HttpResponse response = client.post(Constants.NotificationManagement.NOTIFICATION_ENDPOINT,
                                            PayloadGenerator.getJsonPayload(
                                                    Constants.NotificationManagement.NOTIFICATION_PAYLOAD_FILE_NAME,
                                                    Constants.HTTP_METHOD_POST).toString());
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        AssertUtil.jsonPayloadCompare(PayloadGenerator.getJsonPayload(
                                              Constants.NotificationManagement.NOTIFICATION_RESPONSE_PAYLOAD_FILE_NAME,
                                              Constants.HTTP_METHOD_POST).toString(),
                                      response.getData().toString(), true);
    }

    @Test(description = "Test update notification.", dependsOnMethods = {"testAddNotification"})
    public void testUpdateNotification() throws Exception {
        HttpResponse response = client.put(Constants.NotificationManagement.NOTIFICATION_UPDATE_ENDPOINT,
                                           PayloadGenerator.getJsonPayload(
                                                   Constants.NotificationManagement.NOTIFICATION_PAYLOAD_FILE_NAME,
                                                   Constants.HTTP_METHOD_PUT).toString());
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        AssertUtil.jsonPayloadCompare(PayloadGenerator.getJsonPayload(Constants.NotificationManagement.NOTIFICATION_RESPONSE_PAYLOAD_FILE_NAME,
                                                                      Constants.HTTP_METHOD_PUT).toString(),
                                      response.getData().toString(), true);

    }

    @Test(description = "Test get notification.", dependsOnMethods = {"testUpdateNotification"})
    public void testGetNotification() throws Exception {
        HttpResponse response = client.get(Constants.NotificationManagement.NOTIFICATION_ENDPOINT);
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
    }
}
