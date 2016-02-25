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
package org.wso2.carbon.iot.integration.web.ui.test.user;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.iot.integration.web.ui.test.Constants;
import org.wso2.iot.integration.ui.pages.IOTIntegrationUIBaseTestCase;
import org.wso2.iot.integration.ui.pages.UIElementMapper;
import org.wso2.iot.integration.ui.pages.home.IOTHomePage;
import org.wso2.iot.integration.ui.pages.login.LoginPage;
import org.wso2.iot.integration.ui.pages.uesr.NewUserRegisterPage;

import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

/**
 * Test for registering a new user and login
 */
public class NewUserRegistrationTest extends IOTIntegrationUIBaseTestCase {
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    @BeforeClass(alwaysRun = true)
    public void setup() throws XPathExpressionException, XMLStreamException, IOException {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getWebAppURL() + Constants.IOT_LOGIN_PATH);
    }

    @Test(description = "Verify new User registration")
    public void userRegisterTest() throws IOException {
        LoginPage loginPage = new LoginPage(driver);
        uiElementMapper = UIElementMapper.getInstance();
        NewUserRegisterPage registerTest = loginPage.registerNewUser();
        loginPage = registerTest.registerUser(
                uiElementMapper.getElement("iot.user.add.firstname"),
                uiElementMapper.getElement("iot.user.add.lastname"),
                uiElementMapper.getElement("iot.user.add.email"),
                uiElementMapper.getElement("iot.user.add.username"),
                uiElementMapper.getElement("iot.user.add.password"),
                uiElementMapper.getElement("iot.user.add.password"));

        IOTHomePage homePage = loginPage.loginAsUser(uiElementMapper.getElement("iot.user.add.username"),
                                                     uiElementMapper.getElement("iot.user.add.password"));

        Assert.assertTrue(homePage.checkUserName());
    }

    @Test(description = "Test user logout function", dependsOnMethods = {"userRegisterTest"})
    public void logoutTest() throws IOException {
        IOTHomePage homePage = new IOTHomePage(driver);
        homePage.logout();
        Assert.assertEquals(driver.getTitle(), "Login | IoT Server");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }
}
