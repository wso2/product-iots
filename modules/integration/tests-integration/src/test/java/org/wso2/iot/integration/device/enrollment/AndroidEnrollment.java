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
package org.wso2.iot.integration.device.enrollment;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import junit.framework.Assert;
import org.apache.commons.httpclient.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.iot.integration.common.*;

/**
 * This contains testing of Android device enrollment which is necessary to run prior to all other Android related
 * tests.
 */
public class AndroidEnrollment extends TestBase {
    private RestClient client;
    private String deviceId;

    @BeforeClass(alwaysRun = true, groups = { Constants.AndroidEnrollment.ENROLLMENT_GROUP}, dependsOnGroups =
            Constants.MobileDeviceManagement.MOBILE_DEVICE_MANAGEMENT_GROUP)
    public void initTest() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String accessTokenString = "Bearer " + OAuthUtil.getOAuthToken(backendHTTPURL, backendHTTPSURL);
        this.client = new RestClient(backendHTTPSURL, Constants.APPLICATION_JSON, accessTokenString);
    }

    @Test(description = "Test an Android device enrollment.")
    public void testEnrollment() throws Exception {
        String enrollmentData = PayloadGenerator.getJsonPayload(Constants.AndroidEnrollment
                .ENROLLMENT_PAYLOAD_FILE_NAME, Constants.HTTP_METHOD_POST).toString();
        HttpResponse response = client.post(Constants.AndroidEnrollment.ENROLLMENT_ENDPOINT, enrollmentData);
        JsonParser jsonParser = new JsonParser();
        JsonElement element = jsonParser.parse(response.getData());
        JsonObject jsonObject = element.getAsJsonObject();
        JsonElement msg = jsonObject.get("responseMessage");
        deviceId = msg.getAsString().split("\'")[1].split("\'")[0];
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        AssertUtil.jsonPayloadCompare(PayloadGenerator.getJsonPayload(
                Constants.AndroidEnrollment.ENROLLMENT_RESPONSE_PAYLOAD_FILE_NAME,
                Constants.HTTP_METHOD_POST).toString(), response.getData(), true);
    }

    @Test(description = "Test an Android device is enrolled.", dependsOnMethods = {"testEnrollment"})
    public void testIsEnrolled() throws Exception {
        HttpResponse response = client.get(Constants.AndroidEnrollment.ENROLLMENT_ENDPOINT + "/" + deviceId + "/status");
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        AssertUtil.jsonPayloadCompare(PayloadGenerator.getJsonPayload(
                Constants.AndroidEnrollment.ENROLLMENT_RESPONSE_PAYLOAD_FILE_NAME,
                Constants.HTTP_METHOD_GET).toString(), response.getData(), true);
    }

    @Test(description = "Test modify enrollment.", dependsOnMethods = {"testIsEnrolled"})
    public void testModifyEnrollment() throws Exception {
        JsonObject enrollmentData = PayloadGenerator.getJsonPayload(
                Constants.AndroidEnrollment.ENROLLMENT_PAYLOAD_FILE_NAME,
                Constants.HTTP_METHOD_PUT);
        HttpResponse response = client.put(Constants.AndroidEnrollment.ENROLLMENT_ENDPOINT + "/" + deviceId,
                                           enrollmentData.toString());
        AssertUtil.jsonPayloadCompare(PayloadGenerator.getJsonPayload(
                Constants.AndroidEnrollment.ENROLLMENT_RESPONSE_PAYLOAD_FILE_NAME,
                Constants.HTTP_METHOD_PUT).toString(), response.getData(), true);
    }

    @Test(description = "Test disEnrollment.", dependsOnMethods = {"testModifyEnrollment"})
    public void testDisEnrollDevice() throws Exception {
        HttpResponse response = client.delete(Constants.AndroidEnrollment.ENROLLMENT_ENDPOINT + "/" + deviceId);
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        AssertUtil.jsonPayloadCompare(PayloadGenerator.getJsonPayload(
                Constants.AndroidEnrollment.ENROLLMENT_RESPONSE_PAYLOAD_FILE_NAME,
                Constants.HTTP_METHOD_DELETE).toString(), response.getData(), true);
    }
}
