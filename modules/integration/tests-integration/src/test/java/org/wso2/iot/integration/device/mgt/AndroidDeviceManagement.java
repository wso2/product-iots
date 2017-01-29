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

package org.wso2.iot.integration.device.mgt;

import com.google.gson.JsonArray;
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
 * This class contains integration tests for Android device management backend services.
 */
public class AndroidDeviceManagement extends TestBase {
    private RestClient client;
    private JsonObject device;

    @BeforeClass(alwaysRun = true, groups = {Constants.AndroidDeviceManagement.DEVICE_MANAGEMENT_GROUP})
    public void initTest() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String accessTokenString = "Bearer " + OAuthUtil.getOAuthToken(backendHTTPURL, backendHTTPSURL);
        this.client = new RestClient(backendHTTPURL, Constants.APPLICATION_JSON, accessTokenString);
    }

    @Test(description = "Test get all android devices.", groups = Constants.AndroidDeviceManagement.DEVICE_MANAGEMENT_GROUP)
    public void testGetAllDevices() throws Exception {
        HttpResponse response = client.get(Constants.AndroidDeviceManagement.DEVICE_MGT_ENDPOINT);
        JsonArray jsonArray = new JsonParser().parse(response.getData()).getAsJsonArray();
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        Assert.assertEquals(jsonArray.size(), 1);
    }

    @Test(description = "Test get android device.", groups = Constants.AndroidDeviceManagement.DEVICE_MANAGEMENT_GROUP)
    public void testGetDevice() throws Exception {
        HttpResponse response = client.get(Constants.AndroidDeviceManagement.DEVICE_MGT_ENDPOINT + Constants.DEVICE_ID);
        device = new JsonParser().parse(response.getData()).getAsJsonObject();
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        Assert.assertEquals(device.get(Constants.AndroidDeviceManagement.KEY_DEVICE_ID).getAsString(), Constants.DEVICE_ID);
    }

    @Test(description = "Test update android device.", groups = Constants.AndroidDeviceManagement.DEVICE_MANAGEMENT_GROUP,
            dependsOnMethods = {"testGetDevice"})
    public void testUpdateDevice() throws Exception {
        device.addProperty(Constants.AndroidDeviceManagement.KEY_DEVICE_NAME, "UpdatedName");
        HttpResponse response = client.put(Constants.AndroidDeviceManagement.DEVICE_MGT_ENDPOINT + Constants.DEVICE_ID,
                                           device.toString());
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        AssertUtil.jsonPayloadCompare(PayloadGenerator.getJsonPayload(
                                              Constants.AndroidDeviceManagement.RESPONSE_PAYLOAD_FILE_NAME,
                                              Constants.HTTP_METHOD_PUT).toString(),
                                      response.getData().toString(), true);
    }


    @Test(description = "Test get android license.", groups = Constants.AndroidDeviceManagement.DEVICE_MANAGEMENT_GROUP)
    public void testGetLicense() throws Exception {
        HttpResponse response = client.get(Constants.AndroidDeviceManagement.LICENSE_ENDPOINT);
        CharSequence sequence = Constants.AndroidDeviceManagement.LICENSE_SECTION;
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        Assert.assertTrue(response.getData().contains(sequence));
    }

    /*
    @Test(description = "Test update android device applist.", groups = Constants.DeviceManagement.DEVICE_MANAGEMENT_GROUP,
            dependsOnMethods = {"testGetDevice"} )
    public void testUpdateAppList() throws Exception {
        HttpResponse response = client.post(Constants.DeviceManagement.APP_LIST_ENDPOINT,
                                           Constants.DeviceManagement.APPLIST_PAYLOAD);
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        AssertUtil.jsonPayloadCompare(Constants.DeviceManagement.REQUEST_MODIFY_DEVICE_EXPECTED,
                                      response.getData().toString(), true);
    }*/

}
