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
import org.apache.commons.net.util.Base64;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.iot.integration.common.*;

import javax.xml.xpath.XPathExpressionException;
import java.io.FileNotFoundException;

/**
 * This class contains integration tests for role management backend services.
 */
public class RoleManagement extends TestBase {
    private IOTHttpClient client;
    private TestUserMode userMode;

    @Factory(dataProvider = "userModeProvider")
    public RoleManagement(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true, groups = { Constants.RoleManagement.ROLE_MANAGEMENT_GROUP})
    public void initTest() throws Exception {
        super.init(userMode);
        User currentUser = getAutomationContext().getContextTenant().getContextUser();
        byte[] bytesEncoded = Base64
                .encodeBase64((currentUser.getUserName() + ":" + currentUser.getPassword()).getBytes());
        String encoded = new String(bytesEncoded);
        String accessTokenString = "Bearer " + OAuthUtil
                .getOAuthTokenPair(encoded, backendHTTPSURL, backendHTTPSURL, currentUser.getUserName(),
                        currentUser.getPassword());
        this.client = new IOTHttpClient(backendHTTPSURL, Constants.APPLICATION_JSON, accessTokenString);
    }

    @Test(description = "Test add role.")
    public void testAddRole() throws FileNotFoundException {
        IOTResponse response = client.post(Constants.RoleManagement.ROLE_MANAGEMENT_END_POINT,
                                           PayloadGenerator.getJsonPayload(Constants.RoleManagement.ROLE_PAYLOAD_FILE_NAME,
                                                                           Constants.HTTP_METHOD_POST).toString());
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getStatus());
    }

    @Test(description = "Test update permission role.", dependsOnMethods = {"testAddRole"})
    public void testUpdateRolePermission() throws FileNotFoundException {
        IOTResponse response = client.put(Constants.RoleManagement.ROLE_MANAGEMENT_END_POINT + "/administration",
                                          PayloadGenerator.getJsonPayload(Constants.RoleManagement.ROLE_PAYLOAD_FILE_NAME,
                                                                          Constants.HTTP_METHOD_PUT).toString());
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
    }

    @Test(description = "Test get roles.", dependsOnMethods = {"testUpdateRolePermission"})
    public void testGetRoles() throws FileNotFoundException {
        IOTResponse response = client.get(Constants.RoleManagement.ROLE_MANAGEMENT_END_POINT + "?offset=0&limit=2");
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
        AssertUtil.jsonPayloadCompare(PayloadGenerator
                .getJsonPayload(Constants.RoleManagement.ROLE_RESPONSE_PAYLOAD_FILE_NAME,
                        Constants.UserManagement.GET_ROLES_METHOD).toString(), response.getBody(), true);
    }

    @Test(description = "Test getting roles that has particular prefix.", dependsOnMethods = {"testGetRoles"})
    public void testGetFilteredRoles() throws FileNotFoundException {
        IOTResponse response = client.get(Constants.RoleManagement.ROLE_MANAGEMENT_END_POINT +
                "/filter/administ?offset=0&limit=2");
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
        AssertUtil.jsonPayloadCompare(PayloadGenerator
                .getJsonPayload(Constants.RoleManagement.ROLE_RESPONSE_PAYLOAD_FILE_NAME,
                        Constants.RoleManagement.GET_FILTERED_ROLED_METHOD).toString(), response.getBody(), true);
    }

    @Test(description = "Test getting permissions of a role.", dependsOnMethods = {"testGetFilteredRoles"})
    public void testGetRolePermissions() throws FileNotFoundException {
        IOTResponse response = client
                .get(Constants.RoleManagement.ROLE_MANAGEMENT_END_POINT + "/administration/permissions");
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
    }

    @Test(description = "Test getting role details.", dependsOnMethods = {"testGetRolePermissions"})
    public void testGetRole() throws FileNotFoundException {
        IOTResponse response = client.get(Constants.RoleManagement.ROLE_MANAGEMENT_END_POINT +
                "/administration");
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
    }

    @Test(description = "Test remove user.", dependsOnMethods = {"testGetRole"})
    public void testRemoveRole() throws Exception {
        IOTResponse response = client.delete(Constants.RoleManagement.ROLE_MANAGEMENT_END_POINT +"/administration");
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
    }

    @DataProvider
    private static Object[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN},
                new TestUserMode[]{TestUserMode.TENANT_ADMIN}
        };
    }
}