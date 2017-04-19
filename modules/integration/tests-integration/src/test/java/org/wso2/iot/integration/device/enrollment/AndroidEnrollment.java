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
import org.wso2.iot.integration.common.AssertUtil;
import org.wso2.iot.integration.common.Constants;
import org.wso2.iot.integration.common.PayloadGenerator;
import org.wso2.iot.integration.common.RestClient;
import org.wso2.iot.integration.common.TestBase;

/**
 * This contains testing of Android device enrollment which is necessary to run prior to all other Android related
 * tests.
 */
public class AndroidEnrollment extends TestBase {
    private RestClient client;
    private String deviceId;

    @Factory(dataProvider = "userModeProvider")
    public AndroidEnrollment(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true, groups = { Constants.UserManagement.USER_MANAGEMENT_GROUP})
    public void initTest() throws Exception {
        super.init(userMode);
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

    @Test(description = "Test update applications", dependsOnMethods = {"testModifyEnrollment"})
    public void testUpdateApplications() throws Exception {
        JsonArray updateApplicationData = PayloadGenerator.getJsonArray(
                Constants.AndroidEnrollment.ENROLLMENT_PAYLOAD_FILE_NAME,
                Constants.AndroidEnrollment.UPDATE_APPLICATION_METHOD);
        HttpResponse response = client.put(Constants.AndroidEnrollment.ENROLLMENT_ENDPOINT + "/" + deviceId +
                        "/applications", updateApplicationData.toString());
        Assert.assertEquals("Update of applications for the device id " + deviceId + " failed", HttpStatus
                .SC_ACCEPTED, response.getResponseCode());
        response = client.get(Constants.MobileDeviceManagement.CHANGE_DEVICE_STATUS_ENDPOINT + Constants
                .AndroidEnrollment.ANDROID_DEVICE_TYPE + "/" + deviceId + "/applications");
        Assert.assertEquals("Error while getting application list for the device with the id " + deviceId + " failed",
                HttpStatus.SC_OK, response.getResponseCode());
        JsonArray jsonArray = new JsonParser().parse(response.getData()).getAsJsonArray();
        Assert.assertEquals("Installed applications for the device with the device id " + deviceId + " has not been "
                + "updated yet", 3, jsonArray.size());

    }

    @Test(description = "Test get pending operations", dependsOnMethods = {"testModifyEnrollment"})
    public void testGetPendingOperations() throws Exception {
        JsonArray pendingOperationsData = PayloadGenerator.getJsonArray(
                Constants.AndroidEnrollment.ENROLLMENT_PAYLOAD_FILE_NAME,
                Constants.AndroidEnrollment.GET_PENDING_OPERATIONS_METHOD);
        HttpResponse response = client.put(Constants.AndroidEnrollment.ENROLLMENT_ENDPOINT + "/" + deviceId +
                "/pending-operations", pendingOperationsData.toString());
        JsonArray pendingOperations = new JsonParser().parse(response.getData()).getAsJsonArray();
        Assert.assertEquals("Error while getting pending operations for android device with the id " + deviceId,
                HttpStatus.SC_CREATED, response.getResponseCode());
        Assert.assertTrue("Pending operation count is 0. Periodic monitoring tasks are not running.", 0 <
                pendingOperations.size());

    }
    @Test(description = "Test disEnrollment.", dependsOnMethods = {"testGetPendingOperations"})
    public void testDisEnrollDevice() throws Exception {
        HttpResponse response = client.delete(Constants.AndroidEnrollment.ENROLLMENT_ENDPOINT + "/" + deviceId);
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        AssertUtil.jsonPayloadCompare(PayloadGenerator.getJsonPayload(
                Constants.AndroidEnrollment.ENROLLMENT_RESPONSE_PAYLOAD_FILE_NAME,
                Constants.HTTP_METHOD_DELETE).toString(), response.getData(), true);
    }

}
