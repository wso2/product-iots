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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.iot.integration.common.Constants;
import org.wso2.iot.integration.common.PayloadGenerator;
import org.wso2.iot.integration.common.RestClient;
import org.wso2.iot.integration.common.TestBase;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;

/**
 * This contain tests to check operations supported by Android. Test are executed against a previously enrolled device
 */
public class AndroidOperation extends TestBase {
    private RestClient client;

    @Factory(dataProvider = "userModeProvider")
    public AndroidOperation(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true, groups = { Constants.UserManagement.USER_MANAGEMENT_GROUP})
    public void initTest() throws Exception {
        super.init(userMode);
        this.client = new RestClient(backendHTTPSURL, Constants.APPLICATION_JSON, accessTokenString);
        //Enroll a device
        JsonObject enrollmentData = PayloadGenerator.getJsonPayload(
                Constants.AndroidEnrollment.ENROLLMENT_PAYLOAD_FILE_NAME,
                Constants.HTTP_METHOD_POST);
        client.post(Constants.AndroidEnrollment.ENROLLMENT_ENDPOINT, enrollmentData.toString());
    }

    @Test(groups = {Constants.AndroidOperations.OPERATIONS_GROUP}, description = "Test Android device lock operation.")
    public void testLock() throws MalformedURLException, AutomationFrameworkException {
        HttpResponse response = client
                .post(Constants.AndroidOperations.OPERATION_ENDPOINT + Constants.AndroidOperations.LOCK_ENDPOINT,
                        Constants.AndroidOperations.LOCK_OPERATION_PAYLOAD);
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
    }

    @Test(groups = {Constants.AndroidOperations.OPERATIONS_GROUP}, description = "Test Android device un-lock "
            + "operation.")
    public void testUnLock() throws MalformedURLException, AutomationFrameworkException {
        HttpResponse response = client.post(Constants.AndroidOperations.OPERATION_ENDPOINT +
                        Constants.AndroidOperations.UNLOCK_ENDPOINT, Constants.AndroidOperations.UNLOCK_OPERATION_PAYLOAD);
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
    }


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

    @Test(groups = {Constants.AndroidOperations.OPERATIONS_GROUP}, description = "Test Android get info operation")
    public void testGetInfo() throws Exception {
        HttpResponse response = client.post(Constants.AndroidOperations.OPERATION_ENDPOINT +
                        Constants.AndroidOperations.DEVICE_INFO_ENDPOINT, Constants.AndroidOperations.DEVICE_INFO_PAYLOAD);
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
    }

    @Test(groups = {Constants.AndroidOperations.OPERATIONS_GROUP}, description = "Test Android logcat operation")
    public void testLogcat() throws Exception {
        HttpResponse response = client.post(Constants.AndroidOperations.OPERATION_ENDPOINT +
                Constants.AndroidOperations.DEVICE_LOGCAT_ENDPOINT, Constants.AndroidOperations.PAYLOAD_COMMON);
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
    }


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

    @Test(groups = {Constants.AndroidOperations.OPERATIONS_GROUP}, description = "Test Android get applications "
            + "operation.")
    public void testGetApplications() throws Exception {
        HttpResponse response = client.post(Constants.AndroidOperations.OPERATION_ENDPOINT +
                        Constants.AndroidOperations.APPLICATION_LIST_ENDPOINT, Constants.AndroidOperations.PAYLOAD_COMMON);
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
    }

    @Test(groups = {Constants.AndroidOperations.OPERATIONS_GROUP}, description = "Test Android app install operation")
    public void testInstallApplication() throws Exception {
        JsonObject installApplicationPayload = PayloadGenerator.getJsonPayload(Constants.AndroidOperations
                .OPERATION_PAYLOAD_FILE_NAME, Constants.AndroidOperations.INSTALL_APPS_OPERATION);
        HttpResponse response = client.post(Constants.AndroidOperations.OPERATION_ENDPOINT +
                Constants.AndroidOperations.INSTALL_APPS_ENDPOINT, installApplicationPayload.toString());
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
    }

    @Test(groups = {Constants.AndroidOperations.OPERATIONS_GROUP}, description = "Test Android app update operation")
    public void testUpdateApplication()
            throws FileNotFoundException, MalformedURLException, AutomationFrameworkException {
        JsonObject installApplicationPayload = PayloadGenerator.getJsonPayload(Constants.AndroidOperations
                .OPERATION_PAYLOAD_FILE_NAME, Constants.AndroidOperations.INSTALL_APPS_OPERATION);
        HttpResponse response = client.post(Constants.AndroidOperations.OPERATION_ENDPOINT +
                Constants.AndroidOperations.UPDATE_APPS_ENDPOINT, installApplicationPayload.toString());
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
    }

    @Test(groups = {Constants.AndroidOperations.OPERATIONS_GROUP}, description = "Test Android app un-install "
            + "operation")
    public void testUninstallApplication() throws MalformedURLException, AutomationFrameworkException {
        HttpResponse response = client.post(Constants.AndroidOperations.OPERATION_ENDPOINT
                        + Constants.AndroidOperations.UNINSTALL_APPS_ENDPOINT,
                Constants.AndroidOperations.UNINSTALL_APPS_PAYLOAD);
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
    }

    @Test(groups = {Constants.AndroidOperations.OPERATIONS_GROUP}, description = "Test black list application "
            + "operation")
    public void testBlackListApplication()
            throws FileNotFoundException, MalformedURLException, AutomationFrameworkException {
        JsonObject blackListApplicationPayload = PayloadGenerator.getJsonPayload(Constants.AndroidOperations
                .OPERATION_PAYLOAD_FILE_NAME, Constants.AndroidOperations.BLACKLIST_OPERATION);
        HttpResponse response = client.post(Constants.AndroidOperations.OPERATION_ENDPOINT +
                Constants.AndroidOperations.BLACKLIST_APPS_ENDPOINT, blackListApplicationPayload.toString());
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
    }

    @Test(groups = {Constants.AndroidOperations.OPERATIONS_GROUP}, description = "Test upgrade firmware operation")
    public void testUpgradeFirmware()
            throws FileNotFoundException, MalformedURLException, AutomationFrameworkException {
        JsonObject upgradeFirmWarePayload = PayloadGenerator
                .getJsonPayload(Constants.AndroidOperations.OPERATION_PAYLOAD_FILE_NAME,
                        Constants.AndroidOperations.UPGRADE_FIRMWARE_OPERATION);
        HttpResponse response = client.post(Constants.AndroidOperations.OPERATION_ENDPOINT
                + Constants.AndroidOperations.UPGRADE_FIRMWARE_ENDPOINT, upgradeFirmWarePayload.toString());
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
    }

    @Test(groups = {Constants.AndroidOperations.OPERATIONS_GROUP}, description = "Test configure VPN operation")
    public void testConfigureVPN()
            throws FileNotFoundException, MalformedURLException, AutomationFrameworkException {
        JsonObject configureVPNPayload = PayloadGenerator
                .getJsonPayload(Constants.AndroidOperations.OPERATION_PAYLOAD_FILE_NAME,
                        Constants.AndroidOperations.VPN_OPERATION);
        HttpResponse response = client.post(Constants.AndroidOperations.OPERATION_ENDPOINT
                + Constants.AndroidOperations.VPN_ENDPOINT, configureVPNPayload.toString());
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
    }

    @Test(groups = {Constants.AndroidOperations.OPERATIONS_GROUP}, description = "Test Android reboot operation")
    public void testReboot() throws Exception {
        HttpResponse response = client.post(Constants.AndroidOperations.OPERATION_ENDPOINT +
                Constants.AndroidOperations.REBOOT_ENDPOINT, Constants.AndroidOperations.PAYLOAD_COMMON);
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
    }

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
