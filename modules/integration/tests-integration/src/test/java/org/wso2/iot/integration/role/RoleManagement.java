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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import junit.framework.Assert;
import org.apache.commons.httpclient.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.iot.integration.common.AssertUtil;
import org.wso2.iot.integration.common.Constants;
import org.wso2.iot.integration.common.IOTHttpClient;
import org.wso2.iot.integration.common.IOTResponse;
import org.wso2.iot.integration.common.PayloadGenerator;
import org.wso2.iot.integration.common.TestBase;

import javax.xml.xpath.XPathExpressionException;
import java.io.FileNotFoundException;

/**
 * This class contains integration tests for role management backend services.
 */
public class RoleManagement extends TestBase {
    private IOTHttpClient client;
    private static final String ROLE_NAME = "administration";

    @Factory(dataProvider = "userModeProvider")
    public RoleManagement(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true, groups = { Constants.RoleManagement.ROLE_MANAGEMENT_GROUP})
    public void initTest() throws Exception {
        super.init(userMode);
        this.client = new IOTHttpClient(backendHTTPSURL, Constants.APPLICATION_JSON, accessTokenString);
    }

    @Test(description = "Test add role.")
    public void testAddRole() throws FileNotFoundException {
        IOTResponse response = client.post(Constants.RoleManagement.ROLE_MANAGEMENT_END_POINT, PayloadGenerator
                .getJsonPayload(Constants.RoleManagement.ROLE_PAYLOAD_FILE_NAME, Constants.HTTP_METHOD_POST)
                .toString());
        Assert.assertEquals(HttpStatus.SC_CREATED, response.getStatus());
    }

    @Test(description = "Test update permission role.", dependsOnMethods = {"testAddRole"})
    public void testUpdateRolePermission() throws FileNotFoundException {
        IOTResponse response = client.put(Constants.RoleManagement.ROLE_MANAGEMENT_END_POINT + "/" + ROLE_NAME,
                PayloadGenerator
                        .getJsonPayload(Constants.RoleManagement.ROLE_PAYLOAD_FILE_NAME, Constants.HTTP_METHOD_PUT)
                        .toString());
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
        IOTResponse response = client
                .get(Constants.RoleManagement.ROLE_MANAGEMENT_END_POINT + "/filter/administ?offset=0&limit=2");
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
        AssertUtil.jsonPayloadCompare(PayloadGenerator
                .getJsonPayload(Constants.RoleManagement.ROLE_RESPONSE_PAYLOAD_FILE_NAME,
                        Constants.RoleManagement.GET_FILTERED_ROLED_METHOD).toString(), response.getBody(), true);
    }

    @Test(description = "Test getting permissions of a role.", dependsOnMethods = {"testGetFilteredRoles"})
    public void testGetRolePermissions() throws FileNotFoundException {
        IOTResponse response = client
                .get(Constants.RoleManagement.ROLE_MANAGEMENT_END_POINT + "/" + ROLE_NAME + "/permissions");
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
    }

    @Test(description = "Test getting role details.", dependsOnMethods = {"testGetRolePermissions"})
    public void testGetRole() throws FileNotFoundException {
        IOTResponse response = client.get(Constants.RoleManagement.ROLE_MANAGEMENT_END_POINT + "/" + ROLE_NAME);
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
    }

    @Test(description = "Test updating users with a given role.", dependsOnMethods = {"testGetRole"})
    public void testUpdateRolesOfUser() throws FileNotFoundException, XPathExpressionException {
        boolean isRoleNameExist = false;
        IOTResponse response = client.put(Constants.RoleManagement.ROLE_MANAGEMENT_END_POINT + "/administration/users",
                PayloadGenerator.getJsonArray(Constants.RoleManagement.ROLE_PAYLOAD_FILE_NAME,
                        Constants.RoleManagement.UPDATE_ROLES_METHOD).toString());

        Assert.assertEquals("Error while updating the user list for the role administration", HttpStatus.SC_OK,
                response.getStatus());
        String url =
                Constants.UserManagement.USER_ENDPOINT + "/" + automationContext.getContextTenant().getContextUser()
                        .getUserNameWithoutDomain() + "/roles";
        response = client.get(url);

        JsonArray jsonArray = new JsonParser().parse(response.getBody()).getAsJsonObject().get("roles")
                .getAsJsonArray();
        Assert.assertEquals("Error while retrieving the role details", HttpStatus.SC_OK, response.getStatus());

        for (JsonElement jsonElement:jsonArray) {
            if (jsonElement.getAsString().equals(ROLE_NAME)) {
                isRoleNameExist = true;
                break;
            }
        }
        if (!isRoleNameExist) {
            Assert.fail("The user is not assigned with the new role " + ROLE_NAME);
        }
    }

    @Test(description = "Test remove user.", dependsOnMethods = {"testUpdateRolesOfUser"})
    public void testRemoveRole() throws Exception {
        IOTResponse response = client.delete(Constants.RoleManagement.ROLE_MANAGEMENT_END_POINT + "/" + ROLE_NAME);
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatus());
    }
}
