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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.iot.integration.common.*;

/**
 * This class contains integration tests for API  Mobile Device Management with No Devices Enrolled.
 */
public class MobileDeviceManagementWithNoDevices extends TestBase {
    private IOTHttpClient client;

    @BeforeClass(alwaysRun = true, groups = { Constants.MobileDeviceManagement.MOBILE_DEVICE_MANAGEMENT_GROUP})
    public void initTest() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String accessTokenString = "Bearer " + OAuthUtil.getOAuthToken(backendHTTPSURL, backendHTTPSURL);
        this.client = new IOTHttpClient(backendHTTPSURL, Constants.APPLICATION_JSON, accessTokenString);
    }

    @Test(description = "Test count devices with no added devices")
    public void testCountDevicesWithNoDevices() throws Exception {
        IOTResponse response = client.get(Constants.MobileDeviceManagement.GET_DEVICE_COUNT_ENDPOINT);
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
        Assert.assertEquals(Constants.ZERO, response.getBody());
    }

//    @Test(description = "Test view devices with no added devices")
//    public void testViewDevicesWithNoDevices() throws Exception {
//        IOTResponse response = client.get(Constants.MobileDeviceManagement.GET_ALL_DEVICES_ENDPOINT);
//        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
//        Assert.assertEquals(response.getBody(), Constants.NULL);
//    }

}