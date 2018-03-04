/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.iot.integration.web.ui.test.policy;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.iot.integration.web.ui.test.common.IOTIntegrationUIBaseTestCase;
import org.wso2.carbon.iot.integration.web.ui.test.common.LoginUtils;
import org.wso2.iot.integration.ui.pages.home.IOTAdminDashboard;
import org.wso2.iot.integration.ui.pages.policy.PolicyAddPage;
import org.wso2.iot.integration.ui.pages.policy.PolicyPage;
import org.wso2.iot.integration.ui.pages.policy.PolicyViewPage;

import java.io.IOException;

public class PolicyTest extends IOTIntegrationUIBaseTestCase {

    private WebDriver driver;
    private IOTAdminDashboard adminDashboard;

    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        LoginUtils.login(driver, automationContext, getWebAppURL());
        adminDashboard = new IOTAdminDashboard(driver);
    }

    @Test(description = "Test for adding a new policy.")
    public void addNewPolicyTest() throws IOException {
        PolicyPage policyPage = adminDashboard.getPolicyManagementPage();
        PolicyAddPage policyAddPage = policyPage.addNewPolicy();
        policyAddPage.addAndroidSamplePolicy("TestPolicy", "This is a sample policy");
    }

    @Test(description = "Test for adding a new policy.", dependsOnMethods = "addNewPolicyTest")
    public void policyViewTest() throws IOException {
        PolicyPage policyPage = adminDashboard.getPolicyManagementPage();
        PolicyViewPage policyViewPage = policyPage.viewPolicy();
        Assert.assertTrue(policyViewPage.isPolicyViewable());
    }

}
