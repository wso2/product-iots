/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.iot.integration.samples;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.iot.integration.common.Constants;
import org.wso2.iot.integration.common.RestClient;
import org.wso2.iot.integration.common.TestBase;

import java.io.File;
import java.io.IOException;

/**
 * This class tests the functionality of the mobile qsg.
 */
public class MobileQSGTestCase extends TestBase {
    private RestClient client;
    private String username1;
    private String username2;

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        username1 = "alex";
        username2 = "chris";
        this.client = new RestClient(backendHTTPSURL, Constants.APPLICATION_JSON, accessTokenString);
    }

    @Test(description = "This test case tests the execution of QSG script, whether it executes without any exceptions")
    public void executeQSGScript() throws IOException, InterruptedException {
        String scriptPath =
                FrameworkPathUtil.getCarbonHome() + File.separator + "samples" + File.separator + "mobile-qsg";
        File scriptFile = new File(scriptPath);
        String[] cmdArray = new String[] { "sh", "mobile-qsg.sh" };
        Runtime.getRuntime().exec(cmdArray, null, scriptFile);
        // Allow some time to finish its execution
        Thread.sleep(10000);
    }
//    TODO:Need to verify all QSG Testcase
//    @Test(description = "This test case tests whether user and roles are created as expected", dependsOnMethods =
//            {"executeQSGScript"})
//    public void testUserRoleCreation() throws Exception {
//        // Two users will be created with the quick start script, check whether those two users are created
//        // successfully,
//        String url = Constants.UserManagement.USER_ENDPOINT + "/" + username1;
//        HttpResponse response = client.get(url);
//        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
//        url = Constants.UserManagement.USER_ENDPOINT + "/" + username2;
//        response = client.get(url);
//        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
//
//        // A single role will be created with the quick start script, checking whether that role creation happens
//        // without problem
//        String rolename = "iotMobileUser";
//        response = client.get(Constants.RoleManagement.ROLE_MANAGEMENT_END_POINT + "/" + rolename);
//        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
//    }
//
//    @Test(description = "This test case tests whether app-catalogue is created from qsg script", dependsOnMethods =
//            {"executeQSGScript"})
//    public void testMobileApp() throws Exception {
//        RestClient appManagerRestClient = new RestClient(automationContext.getContextUrls().getWebAppURLHttps(),
//                Constants.APPLICATION_JSON, accessTokenString);
//        HttpResponse response = appManagerRestClient.get(Constants.QSGManagement.GET_MOBILE_APPS_ENDPONT);
//        Assert.assertEquals("Catalog mobile app is not uploaded successfully", HttpStatus.SC_OK,
//                response.getResponseCode());
//        Assert.assertTrue("Catalog app addition through script is not successful",
//                response.getData().contains("Catalog"));
//    }
//
//    @Test(description = "This test case tests the policy creation through qsg script", dependsOnMethods = {"executeQSGScript"})
//    public void testPolicyCreation() throws Exception {
//        HttpResponse response = client.get(Constants.PolicyManagement.VIEW_POLICY_LIST_ENDPOINT + "?offset=0&limit=10");
//        Assert.assertEquals("Policy upload view mobile-qsg script failed", HttpStatus.SC_OK,
//                response.getResponseCode());
//        Assert.assertTrue("Android pass-code policy is not added from qsg script",
//                response.getData().contains("android-passcode-policy1"));
//        Assert.assertTrue("Windows pass-code policy is not added from qsg script",
//                response.getData().contains("windows-passcode-policy1"));
//    }
//
//    @AfterClass(alwaysRun = true)
//    public void tearDown() throws Exception {
//        if (client != null) {
//            String url = Constants.UserManagement.USER_ENDPOINT + "/" + username1;
//            client.delete(url);
//            url = Constants.UserManagement.USER_ENDPOINT + "/" + username2;
//            client.delete(url);
//        }
//    }
}
