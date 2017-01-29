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
package org.wso2.iot.integration.role;

import junit.framework.Assert;
import org.apache.commons.httpclient.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.iot.integration.common.*;

/**
 * This class contains integration tests for role management backend services.
 */
public class RoleManagement extends TestBase {

    private IOTHttpClient client;

    @BeforeClass(alwaysRun = true, groups = { Constants.RoleManagement.ROLE_MANAGEMENT_GROUP})
    public void initTest() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String accessTokenString = "Bearer " + OAuthUtil.getOAuthToken(backendHTTPSURL, backendHTTPSURL);
        this.client = new IOTHttpClient(backendHTTPSURL, Constants.APPLICATION_JSON, accessTokenString);
    }

    @Test(description = "Test add role.")
    public void testAddRole() throws Exception {
        IOTResponse response = client.post(Constants.RoleManagement.ADD_ROLE_ENDPOINT,
                                           PayloadGenerator.getJsonPayload(Constants.RoleManagement.ROLE_PAYLOAD_FILE_NAME,
                                                                           Constants.HTTP_METHOD_POST).toString());
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getStatus());
    }

    @Test(description = "Test update permission role.", dependsOnMethods = {"testAddRole"})
    public void testUpdateRolePermission() throws Exception {
        IOTResponse response = client.put(Constants.RoleManagement.UPDATE_ROLE_PERMISSION_ENDPOINT,
                                          PayloadGenerator.getJsonPayload(Constants.RoleManagement.UPDATE_ROLE_PERMISSION_PAYLOAD_FILE_NAME,
                                                                          Constants.HTTP_METHOD_PUT).toString());
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
    }

    @Test(description = "Test remove user.", dependsOnMethods = {"testUpdateRolePermission"})
    public void testRemoveRole() throws Exception {
        IOTResponse response = client.delete(Constants.RoleManagement.REMOVE_ROLE_ENDPOINT);
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
    }
}