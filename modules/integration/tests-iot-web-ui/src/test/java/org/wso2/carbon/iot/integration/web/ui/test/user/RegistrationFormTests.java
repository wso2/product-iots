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
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.iot.integration.web.ui.test.Constants;
import org.wso2.iot.integration.ui.pages.IOTIntegrationUIBaseTestCase;
import org.wso2.iot.integration.ui.pages.UIElementMapper;

/**
 * These test cases check for following:
 *   - Submitting an empty form
 *   - Existing username
 *   - Non matching passwords
 *   - Password, username length
 */
public class RegistrationFormTests extends IOTIntegrationUIBaseTestCase{
    private WebDriver driver;
    UIElementMapper uiElementMapper;

    WebElement firstNameField;
    WebElement lastNameField;
    WebElement emailField;
    WebElement userNameField;
    WebElement passwordField;
    WebElement passwordConfirmationField;
    WebElement registerButton;

    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getWebAppURL() + Constants.IOT_USER_REGISTER_URL);

        uiElementMapper = UIElementMapper.getInstance();

        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.titleContains("Register | IoT Server"));

        firstNameField = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.user.add.input.firstname.xpath")));
        lastNameField = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.user.add.input.lastname.xpath")));
        emailField = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.user.add.input.email.xpath")));
        userNameField = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.user.add.input.username.xpath")));
        passwordField = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.user.add.input.password.xpath")));
        passwordConfirmationField = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.user.add.input.confirmpassword.xpath")));
        registerButton = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.user.add.register.button.xpath")));
    }

    @Test(description = "Test for submitting an empty registration form")
    public void emptyFormTest() throws Exception {
        clearForm();
        firstNameField.sendKeys("");
        lastNameField.sendKeys("");
        emailField.sendKeys("");
        userNameField.sendKeys("");
        passwordField.sendKeys("");
        passwordConfirmationField.sendKeys("");

        registerButton.click();

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

    @Test (description = "Test for non matching passwords")
    public void nonMatchingPasswordTest() throws Exception {
        clearForm();

        firstNameField.sendKeys("User");
        lastNameField.sendKeys("User");
        emailField.sendKeys("user@user.com");
        userNameField.sendKeys("user");
        passwordField.sendKeys("user123");
        passwordConfirmationField.sendKeys("user234");
        registerButton.click();

        Assert.assertEquals(driver.findElement(By.id(
                uiElementMapper.getElement("iot.user.register.confirmPassword.error"))).getText(),
                            "Please enter the same password as above");
    }

    @Test (description = "Test for email")
    public void incorrectEmail() throws Exception {
        clearForm();

        firstNameField.sendKeys("User");
        lastNameField.sendKeys("User");
        emailField.sendKeys("user.com");
        registerButton.click();

        Assert.assertEquals(driver.findElement(By.id(
                uiElementMapper.getElement("iot.user.register.email.error"))).getText(),
                            "Email is not valid. Please enter a correct email address.");

    }

    @Test (description = "Test for password length")
    public void passwordLengthTest() throws Exception {
        clearForm();

        firstNameField.sendKeys("User");
        lastNameField.sendKeys("User");
        emailField.sendKeys("user@user.com");
        userNameField.sendKeys("user");
        passwordField.sendKeys("user");

        registerButton.click();

        Assert.assertEquals(driver.findElement(By.id(
                uiElementMapper.getElement("iot.user.register.password.error"))).getText(),
                            "Password should be between 5 and 30 characters.");

    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }

    public void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        userNameField.clear();
        passwordField.clear();
        passwordConfirmationField.clear();
    }
}
