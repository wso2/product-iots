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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.iot.integration.web.ui.test.Constants;
import org.wso2.iot.integration.ui.pages.IOTIntegrationUIBaseTestCase;
import org.wso2.iot.integration.ui.pages.UIElementMapper;
import org.wso2.iot.integration.ui.pages.uesr.NewUserRegisterPage;

import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

/**
 * These test cases check for following:
 * - Submitting an empty form
 * - Existing username
 * - Non matching passwords
 * - Password, username length
 */
public class RegistrationFormValidationTests extends IOTIntegrationUIBaseTestCase {
    private WebDriver driver;
    private UIElementMapper uiElementMapper;
    private NewUserRegisterPage registerPage;

    @BeforeClass(alwaysRun = true)
    public void setup() throws XPathExpressionException, XMLStreamException, IOException {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getWebAppURL() + Constants.IOT_USER_REGISTER_URL);
        registerPage = new NewUserRegisterPage(driver);
        uiElementMapper = UIElementMapper.getInstance();
}

    @Test(description = "Test for submitting an empty registration form")
    public void emptyFormTest() throws IOException {
        registerPage.clearForm();
        registerPage.validateForm("", "", "", "", "", "");

        Assert.assertEquals(driver.findElement(By.id(
                uiElementMapper.getElement("iot.user.register.firstname.error"))).getText(),
                            "Firstname is a required field. This cannot be empty.");
        Assert.assertEquals(driver.findElement(By.id(
                uiElementMapper.getElement("iot.user.register.lastname.error"))).getText(),
                            "Lastname is a required field. This cannot be empty.");
        Assert.assertEquals(driver.findElement(By.id(
                uiElementMapper.getElement("iot.user.register.email.error"))).getText(),
                            "Email is a required field. This cannot be empty.");
        Assert.assertEquals(driver.findElement(By.id(
                uiElementMapper.getElement("iot.user.register.username.error"))).getText(),
                            "Username is a required field. This cannot be empty.");
        Assert.assertEquals(driver.findElement(By.id(
                uiElementMapper.getElement("iot.user.register.password.error"))).getText(),
                            "Please enter a user login password");
        Assert.assertEquals(driver.findElement(By.id(
                uiElementMapper.getElement("iot.user.register.confirmPassword.error"))).getText(),
                            "Please enter a user login password");
    }

    @Test(description = "Test for non matching passwords")
    public void nonMatchingPasswordTest() {
        registerPage.validateForm("user", "user", "user@wso2.com", "user1", "password", "Password");

        Assert.assertEquals(driver.findElement(By.id(
                uiElementMapper.getElement("iot.user.register.confirmPassword.error"))).getText(),
                            "Please enter the same password as above");
    }

    @Test(description = "Test for email")
    public void incorrectEmailTest() {
        registerPage.validateForm("user", "user", "user123", "user1", "password", "password");

        Assert.assertEquals(driver.findElement(By.id(
                uiElementMapper.getElement("iot.user.register.email.error"))).getText(),
                            "Email is not valid. Please enter a correct email address.");
    }

    @Test(description = "Test for password length")
    public void passwordLengthTest() {
        registerPage.validateForm("user", "user", "user@wso2.com", "user1", "passw", "passw");
        Assert.assertEquals(driver.findElement(By.id(
                uiElementMapper.getElement("iot.user.register.password.error"))).getText(),
                            "Password should be between 5 and 30 characters.");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        driver.quit();
    }


}
