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

import com.google.gson.JsonObject;
import junit.framework.Assert;
import org.apache.commons.httpclient.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.iot.integration.common.*;

/**
 * This class contains integration tests for API Device management backend services.
 */
public class MobileDeviceManagement extends TestBase {
    private IOTHttpClient client;

    @BeforeClass(alwaysRun = true, groups = { Constants.MobileDeviceManagement.MOBILE_DEVICE_MANAGEMENT_GROUP})
    public void initTest() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String accessTokenString = "Bearer " + OAuthUtil.getOAuthToken(backendHTTPSURL, backendHTTPSURL);
        this.client = new IOTHttpClient(backendHTTPSURL, Constants.APPLICATION_JSON, accessTokenString);
    }

    @Test(description = "Add an Android device.")
    public void addEnrollment() throws Exception {
        JsonObject enrollmentData = PayloadGenerator.getJsonPayload(
                Constants.AndroidEnrollment.ENROLLMENT_PAYLOAD_FILE_NAME, Constants.HTTP_METHOD_POST);
        enrollmentData.addProperty(Constants.DEVICE_IDENTIFIER_KEY, Constants.DEVICE_ID);
        IOTResponse response = client.post(Constants.AndroidEnrollment.ENROLLMENT_ENDPOINT, enrollmentData.toString());
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
        AssertUtil.jsonPayloadCompare(PayloadGenerator.getJsonPayload(
                Constants.AndroidEnrollment.ENROLLMENT_RESPONSE_PAYLOAD_FILE_NAME,
                Constants.HTTP_METHOD_POST).toString(), response.getBody(), true);
    }

    @Test(dependsOnMethods = {"addEnrollment"}, description = "Test count devices")
    public void testCountDevices() throws Exception {
        IOTResponse response = client.get(Constants.MobileDeviceManagement.GET_DEVICE_COUNT_ENDPOINT);
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
        Assert.assertTrue(response.getBody().equals(Constants.MobileDeviceManagement.NO_OF_DEVICES));

    }

    @Test(dependsOnMethods = {"addEnrollment"}, description = "Test view devices")
    public void testViewDevices() throws Exception {
        IOTResponse response = client.get(Constants.MobileDeviceManagement.GET_ALL_DEVICES_ENDPOINT);
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
    }

    @Test(dependsOnMethods = {"addEnrollment"}, description = "Test view device types")
    public void testViewDeviceTypes() throws Exception {
        IOTResponse response = client.get(Constants.MobileDeviceManagement.VIEW_DEVICE_TYPES_ENDPOINT);
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
        Assert.assertEquals(PayloadGenerator.getJsonPayloadToString
                (Constants.MobileDeviceManagement.VIEW_DEVICE_RESPONSE_PAYLOAD_FILE_NAME), response.getBody());
        //Response has two device types, because in windows enrollment a windows device is previously enrolled.
    }

}
