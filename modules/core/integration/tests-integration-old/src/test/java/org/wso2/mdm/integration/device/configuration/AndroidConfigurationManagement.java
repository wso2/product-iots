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

package org.wso2.mdm.integration.device.configuration;

import junit.framework.Assert;
import org.apache.commons.httpclient.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.mdm.integration.common.*;

/**
 * This class contains integration tests for Android configuration management backend services.
 */
public class AndroidConfigurationManagement extends TestBase {

    private RestClient client;

    @BeforeClass(alwaysRun = true, groups = {Constants.AndroidConfigurationManagement.DEVICE_CONFIGURATION_GROUP})
    public void initTest() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String accessTokenString = "Bearer " + OAuthUtil.getOAuthToken(backendHTTPURL, backendHTTPSURL);
        this.client = new RestClient(backendHTTPURL, Constants.APPLICATION_JSON, accessTokenString);
    }

    @Test(description = "Test add android platform configuration.")
    public void testAddConfiguration() throws Exception {
        HttpResponse response = client.post(Constants.AndroidConfigurationManagement.CONFIG_MGT_ENDPOINT,
                                            PayloadGenerator.getJsonPayload(
                                                    Constants.AndroidConfigurationManagement.PAYLOAD_FILE_NAME,
                                                    Constants.HTTP_METHOD_POST).toString()
        );
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        AssertUtil.jsonPayloadCompare(PayloadGenerator.getJsonPayload(
                                              Constants.AndroidConfigurationManagement.RESPONSE_PAYLOAD_FILE_NAME,
                                              Constants.HTTP_METHOD_POST).toString(),
                                      response.getData().toString(), true
        );
    }

    @Test(description = "Test update android configuration.", dependsOnMethods = {"testAddConfiguration"})
    public void testModifyConfiguration() throws Exception {
        HttpResponse response = client.put(Constants.AndroidConfigurationManagement.CONFIG_MGT_ENDPOINT,
                                           PayloadGenerator.getJsonPayload(
                                                   Constants.AndroidConfigurationManagement.PAYLOAD_FILE_NAME,
                                                   Constants.HTTP_METHOD_PUT).toString()
        );
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        AssertUtil.jsonPayloadCompare(PayloadGenerator.getJsonPayload(
                                              Constants.AndroidConfigurationManagement.RESPONSE_PAYLOAD_FILE_NAME,
                                              Constants.HTTP_METHOD_PUT).toString(),
                                      response.getData().toString(), true
        );
    }

//    @Test(description = "Test get android configuration.",
//          dependsOnMethods = { "testAddConfiguration", "testModifyConfiguration" })
//    public void testGetConfiguration() throws Exception {
//        HttpResponse response = client.get(Constants.AndroidConfigurationManagement.CONFIG_MGT_ENDPOINT);
//        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
//        AssertUtil.jsonPayloadCompare(
//                PayloadGenerator.getJsonPayload(Constants.AndroidConfigurationManagement.PAYLOAD_FILE_NAME,
//                                                Constants.HTTP_METHOD_PUT).toString(),
//                response.getData().toString(), true
//        );
//    }
}
