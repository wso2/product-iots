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

package org.wso2.emm.ui.integration.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.iot.integration.ui.pages.EMMIntegrationUiBaseTestCase;
import org.wso2.iot.integration.ui.pages.role.EditRolePage;
import org.wso2.iot.integration.ui.pages.role.RoleListPage;

public class RoleTestCases extends EMMIntegrationUiBaseTestCase {

    private static final Log log = LogFactory.getLog(RoleTestCases.class);
    private static String roleName = "role-manager";
    private static String roleNameEdit = "role-wso2-manager";
    private WebDriver driver;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        LoginUtils.login(driver, automationContext, getWebAppURL());
    }

    @Test(description = "verify add role to emm")
    public void testAddRole() throws Exception {
        driver.get(getWebAppURL() + Constants.MDM_ROLES_URL);
        RoleListPage roleListPage = new RoleListPage(driver, Constants.MDM_ROLES_URL);
        roleListPage.addRole(roleName);
    }

    @Test(description = "verify edit role to emm", dependsOnMethods = {"testAddRole"})
    public void testEditRole() throws Exception {
        driver.get(getWebAppURL() + Constants.MDM_ROLES_EDIT_URL + roleName);
        EditRolePage rolePage = new EditRolePage(driver);
        rolePage.editRole(roleNameEdit);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }
}
