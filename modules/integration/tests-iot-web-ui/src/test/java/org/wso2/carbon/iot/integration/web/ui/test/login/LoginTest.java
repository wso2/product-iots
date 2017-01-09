/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.iot.integration.web.ui.test.login;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.iot.integration.web.ui.test.common.Constants;
import org.wso2.carbon.iot.integration.web.ui.test.common.IOTIntegrationUIBaseTestCase;
import org.wso2.iot.integration.ui.pages.home.IOTAdminDashboard;
import org.wso2.iot.integration.ui.pages.login.LoginPage;

import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

/**
 * Test Login as Admin
 */
public class LoginTest extends IOTIntegrationUIBaseTestCase {

    private WebDriver driver;

    @BeforeClass(alwaysRun = true)
    public void setup() throws XPathExpressionException, XMLStreamException, IOException {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getWebAppURL() + Constants.IOT_LOGIN_PATH);
    }

    @Test(description = "Verify logins to IOT server dashboard")
    public void testAdminLogin() throws IOException, XPathExpressionException {
        LoginPage loginPage = new LoginPage(driver);
        IOTAdminDashboard dashboard = loginPage.loginAsAdmin(
                automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                automationContext.getSuperTenant().getTenantAdmin().getPassword());
        dashboard.logout();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown(){
        driver.quit();
    }

}
