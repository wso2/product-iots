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

import junit.framework.Assert;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.experimental.theories.Theories;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.iot.integration.common.*;

import java.util.concurrent.TimeUnit;

/**
 * This class contains integration tests for API  Mobile Device Management with No Devices Enrolled.
 */
public class MobileDeviceManagementWithNoDevices extends TestBase {
    private IOTHttpClient client;

    /**
     * @BeforeSuite annotation is added to run this verification before the test suite starts.
     * As in IoT server, apis are published after the server startup. Due to that the generated token doesn't get
     * required scope.
     * This method delays test suit startup until the tokens get required scopes.
     * @throws Exception
     */
    @BeforeSuite
    public void verifyApiPublishing() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);

        while (!checkScopes(Constants.APIApplicationRegistration.PERMISSION_LIST)) {
            TimeUnit.SECONDS.sleep(5);
        }
    }

    @BeforeClass(alwaysRun = true, groups = { Constants.MobileDeviceManagement.MOBILE_DEVICE_MANAGEMENT_GROUP})
    public void initTest() throws Exception {

        String accessTokenString = "Bearer " + OAuthUtil.getOAuthToken(backendHTTPSURL, backendHTTPSURL);
        this.client = new IOTHttpClient(backendHTTPSURL, Constants.APPLICATION_JSON, accessTokenString);
    }

    @Test(description = "Test count devices with no added devices")
    public void testCountDevicesWithNoDevices() throws Exception {
        IOTResponse response = client.get(Constants.MobileDeviceManagement.GET_DEVICE_COUNT_ENDPOINT);
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
        Assert.assertEquals(Constants.MobileDeviceManagement.NO_DEVICE, response.getBody());
    }

    private boolean checkScopes(String permissionsList) throws Exception {
        String tokenString = OAuthUtil.getScopes(backendHTTPSURL, backendHTTPSURL);
        return tokenString.contains(permissionsList);
    }

}