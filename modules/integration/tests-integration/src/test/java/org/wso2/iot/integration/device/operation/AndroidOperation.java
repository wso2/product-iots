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
package org.wso2.iot.integration.device.operation;

import com.google.gson.JsonObject;
import junit.framework.Assert;
import org.apache.commons.httpclient.HttpStatus;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.iot.integration.common.*;

/**
 * This contain tests to check operations supported by Android. Test are executed against a previously enrolled device
 */
public class AndroidOperation extends TestBase {
    private RestClient client;

    @BeforeTest(alwaysRun = true, groups = { Constants.AndroidOperations.OPERATIONS_GROUP})
    public void initTest() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String accessTokenString = "Bearer " + OAuthUtil.getOAuthToken(backendHTTPURL, backendHTTPSURL);
        this.client = new RestClient(backendHTTPSURL, Constants.APPLICATION_JSON, accessTokenString);
        //Enroll a device
        JsonObject enrollmentData = PayloadGenerator.getJsonPayload(
                Constants.AndroidEnrollment.ENROLLMENT_PAYLOAD_FILE_NAME,
                Constants.HTTP_METHOD_POST);
        client.post(Constants.AndroidEnrollment.ENROLLMENT_ENDPOINT, enrollmentData.toString());
    }

    @Test(groups = {Constants.AndroidOperations.OPERATIONS_GROUP}, description = "Test Android device lock operation.")
    public void testLock() throws Exception {
        HttpResponse response = client.post(Constants.AndroidOperations.OPERATION_ENDPOINT +
                                            Constants.AndroidOperations.LOCK_ENDPOINT,
                                            Constants.AndroidOperations.LOCK_OPERATION_PAYLOAD);
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
    }

/*
    @Test(groups = Constants.AndroidOperations.OPERATIONS_GROUP, description = "Test Android getPendingOperations.")
    public void testGetPendingOperations() throws Exception {
        HttpResponse response = client.put(Constants.AndroidOperations.OPERATION_ENDPOINT + Constants.DEVICE_ID,
                                           "[]");
        Assert.assertEquals(response.getResponseCode(), HttpStatus.SC_CREATED);
    }*/

    @Test(groups = {Constants.AndroidOperations.OPERATIONS_GROUP}, description = "Test Android device location "
            + "operation.")
    public void testLocation() throws Exception {
        HttpResponse response = client.post(Constants.AndroidOperations.OPERATION_ENDPOINT +
                                            Constants.AndroidOperations.LOCATION_ENDPOINT,
                                            Constants.AndroidOperations.LOCATION_PAYLOAD);
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
    }

    @Test(groups = {Constants.AndroidOperations.OPERATIONS_GROUP}, description = "Test Android device clear password " +
                                                                               "operation.")
    public void testClearPassword() throws Exception {
        HttpResponse response = client.post(Constants.AndroidOperations.OPERATION_ENDPOINT +
                                            Constants.AndroidOperations.CLEAR_PASSWORD_ENDPOINT,
                                            Constants.AndroidOperations.CLEAR_PASSWORD_PAYLOAD);
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
    }

    @Test(groups = {Constants.AndroidOperations.OPERATIONS_GROUP}, description = "Test Android device camera "
            + "operation.")
    public void testCamera() throws Exception {
        HttpResponse response = client.post(Constants.AndroidOperations.OPERATION_ENDPOINT +
                                            Constants.AndroidOperations.CAMERA_OPERATION,
                                            Constants.AndroidOperations.CAMERA_OPERATION_PAYLOAD);
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
    }

//    //404
//    @Test(groups = {Constants.AndroidOperations.OPERATIONS_GROUP}, description = "Test Android device information "
//            + "operation.")
//    public void testDeviceInfo() throws Exception {
//
//        HttpResponse response = client.post(Constants.AndroidOperations.OPERATION_ENDPOINT +
//                                            Constants.AndroidOperations.DEVICE_INFO_ENDPOINT,
//                                            Constants.AndroidOperations.DEVICE_INFO_PAYLOAD);
//        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
//    }

    @Test(groups = {Constants.AndroidOperations.OPERATIONS_GROUP}, description = "Test Android enterprise-wipe "
            + "operation.")
    public void testEnterpriseWipe() throws Exception {
        HttpResponse response = client.post(Constants.AndroidOperations.OPERATION_ENDPOINT +
                                            Constants.AndroidOperations.ENTERPRISE_WIPE_ENDPOINT,
                                            Constants.AndroidOperations.ENTERPRISE_WIPE_PAYLOAD);
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
    }

    @Test(groups = {Constants.AndroidOperations.OPERATIONS_GROUP}, description = "Test Android wipe data operation.")
    public void testWipeData() throws Exception {
        HttpResponse response = client.post(Constants.AndroidOperations.OPERATION_ENDPOINT +
                                            Constants.AndroidOperations.WIPE_DATA_ENDPOINT,
                                            Constants.AndroidOperations.WIPE_DATA_PAYLOAD);
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
    }

//    //400
//    @Test(groups = {Constants.AndroidOperations.OPERATIONS_GROUP}, description = "Test Android application list "
//            + "operation.")
//    public void testApplicationList() throws Exception {
//        HttpResponse response = client.post(Constants.AndroidOperations.OPERATION_ENDPOINT +
//                                            Constants.AndroidOperations.APPLICATION_LIST_ENDPOINT,
//                                            Constants.AndroidOperations.APPLICATION_LIST_PAYLOAD);
//        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
//    }

    @Test(groups = {Constants.AndroidOperations.OPERATIONS_GROUP}, description = "Test Android ring operation.")
    public void testRing() throws Exception {
        HttpResponse response = client.post(Constants.AndroidOperations.OPERATION_ENDPOINT +
                                            Constants.AndroidOperations.RING_ENDPOINT,
                                            Constants.AndroidOperations.RING_PAYLOAD);
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
    }

    @Test(groups = {Constants.AndroidOperations.OPERATIONS_GROUP}, description = "Test Android mute operation.")
    public void testMute() throws Exception {
        HttpResponse response = client.post(Constants.AndroidOperations.OPERATION_ENDPOINT +
                                            Constants.AndroidOperations.MUTE_ENDPOINT,
                                            Constants.AndroidOperations.MUTE_PAYLOAD);
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
    }

//    //400
//    @Test(groups = {Constants.AndroidOperations.OPERATIONS_GROUP}, description = "Test Android install apps operation.")
//    public void testInstallApps() throws Exception {
//        HttpResponse response = client.post(Constants.AndroidOperations.OPERATION_ENDPOINT +
//                                            Constants.AndroidOperations.INSTALL_APPS_ENDPOINT,
//                                            Constants.AndroidOperations.INSTALL_APPS_PAYLOAD);
//        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
//    }

//    //400
//    @Test(groups = {Constants.AndroidOperations.OPERATIONS_GROUP}, description = "Test Android uninstall apps "
//            + "operation.")
//    public void testUninstallApps() throws Exception {
//        HttpResponse response = client.post(Constants.AndroidOperations.OPERATION_ENDPOINT +
//                                            Constants.AndroidOperations.UNINSTALL_APPS_ENDPOINT,
//                Constants.AndroidOperations.UNINSTALL_APPS_PAYLOAD);
//        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
//    }

    @Test(groups = {Constants.AndroidOperations.OPERATIONS_GROUP}, description = "Test Android notification operation.")
    public void testNotification() throws Exception {
        HttpResponse response = client.post(Constants.AndroidOperations.OPERATION_ENDPOINT +
                                            Constants.AndroidOperations.NOTIFICATION_ENDPOINT,
                                            Constants.AndroidOperations.NOTIFICATION_PAYLOAD);
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
    }

    @Test(groups = {Constants.AndroidOperations.OPERATIONS_GROUP}, description = "Test Android WiFi operation.")
    public void testWiFi() throws Exception {
        HttpResponse response = client.post(Constants.AndroidOperations.OPERATION_ENDPOINT +
                                            Constants.AndroidOperations.WIFI_ENDPOINT,
                                            Constants.AndroidOperations.WIFI_PAYLOAD);
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
    }

    @Test(groups = {Constants.AndroidOperations.OPERATIONS_GROUP}, description = "Test Android encrypt operation.")
    public void testEncrypt() throws Exception {
        HttpResponse response = client.post(Constants.AndroidOperations.OPERATION_ENDPOINT +
                                            Constants.AndroidOperations.ENCRYPT_ENDPOINT,
                                            Constants.AndroidOperations.ENCRYPT_PAYLOAD);
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
    }

    @Test(groups = {Constants.AndroidOperations.OPERATIONS_GROUP}, description = "Test Android change lock operation.")
    public void testChangeLock() throws Exception {
        HttpResponse response = client.post(Constants.AndroidOperations.OPERATION_ENDPOINT +
                                            Constants.AndroidOperations.CHANGE_LOCK_ENDPOINT,
                                            Constants.AndroidOperations.CHANGE_LOCK_PAYLOAD);
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
    }

    @Test(groups = {Constants.AndroidOperations.OPERATIONS_GROUP}, description = "Test Android password policy operation.")
    public void testPasswordPolicy() throws Exception {
        HttpResponse response = client.post(Constants.AndroidOperations.OPERATION_ENDPOINT +
                                            Constants.AndroidOperations.PASSWORD_POLICY_ENDPOINT,
                                            Constants.AndroidOperations.PASSWORD_POLICY_PAYLOAD);
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
    }

    @Test(groups = {Constants.AndroidOperations.OPERATIONS_GROUP}, description = "Test Android web clip operation.")
    public void testWebClip() throws Exception {
        HttpResponse response = client.post(Constants.AndroidOperations.OPERATION_ENDPOINT +
                                            Constants.AndroidOperations.WEB_CLIP_ENDPOINT,
                                            Constants.AndroidOperations.WEB_CLIP_PAYLOAD);
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
    }
}