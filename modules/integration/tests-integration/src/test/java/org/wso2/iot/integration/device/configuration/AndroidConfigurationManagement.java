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

package org.wso2.iot.integration.device.configuration;

import junit.framework.Assert;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.net.util.Base64;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.iot.integration.common.*;

/**
 * This class contains integration tests for Android configuration management backend services.
 */
public class AndroidConfigurationManagement extends TestBase {
    private RestClient client;

    @Factory(dataProvider = "userModeProvider")
    public AndroidConfigurationManagement(TestUserMode testUserMode) {
        this.userMode = testUserMode;
    }

    @BeforeClass(alwaysRun = true, groups = { Constants.UserManagement.USER_MANAGEMENT_GROUP})
    public void initTest() throws Exception {
        super.init(userMode);
        this.client = new RestClient(backendHTTPSURL, Constants.APPLICATION_JSON, accessTokenString);
    }

    @Test(description = "Test update android configuration.")
    public void testModifyConfiguration() throws Exception {
        HttpResponse response = client.put(Constants.AndroidConfigurationManagement.CONFIG_MGT_ENDPOINT,
                PayloadGenerator.getJsonPayload(Constants.AndroidConfigurationManagement.PAYLOAD_FILE_NAME,
                        Constants.HTTP_METHOD_PUT).toString());
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        Assert.assertEquals("Android configuration update message is not received properly",
                "Android platform " + "configuration has been updated successfully.",
                response.getData().replaceAll("\"", ""));
    }

    @Test(description = "Test get android configuration.", dependsOnMethods = { "testModifyConfiguration" })
    public void testGetConfiguration() throws Exception {
        HttpResponse response = client.get(Constants.AndroidConfigurationManagement.CONFIG_MGT_ENDPOINT);
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        AssertUtil.jsonPayloadCompare(PayloadGenerator
                .getJsonPayload(Constants.AndroidConfigurationManagement.PAYLOAD_FILE_NAME, Constants.HTTP_METHOD_PUT)
                .toString(), response.getData(), true);
    }

    @Test(description = "Test get android license.", dependsOnMethods = { "testModifyConfiguration" })
    public void testGetLicense() throws Exception {
        HttpResponse response = client.get(Constants.AndroidConfigurationManagement.CONFIG_MGT_ENDPOINT
                + Constants.AndroidConfigurationManagement.LICENSE_ENDPOINT);
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        Assert.assertEquals("Expected android license agreement is not received",
                "This End User License Agreement is " + "Eula.", response.getData());
    }
}
