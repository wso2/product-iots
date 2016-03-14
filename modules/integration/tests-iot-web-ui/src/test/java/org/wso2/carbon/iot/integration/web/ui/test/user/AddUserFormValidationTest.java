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
package org.wso2.carbon.iot.integration.web.ui.test.user;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.iot.integration.web.ui.test.common.Constants;
import org.wso2.carbon.iot.integration.web.ui.test.common.LoginUtils;
import org.wso2.carbon.iot.integration.web.ui.test.common.IOTIntegrationUIBaseTestCase;
import org.wso2.iot.integration.ui.pages.UIElementMapper;

import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

/**
 * Class contains test cases for Add user form validation.
 * 1. Empty  form submission
 * 2. Short user name
 * 3. Empty First Name
 * 4. Empty Last Name
 * 5. Empty email
 * 6. Incorrect email
 */
public class AddUserFormValidationTest extends IOTIntegrationUIBaseTestCase {

    private WebDriver driver;
    private UIElementMapper uiElementMapper;
    private WebElement firstNameField;
    private WebElement lastNameField;
    private WebElement emailField;
    private WebElement userNameField;
    private WebElement addUserButton;

    @BeforeClass(alwaysRun = true)
    public void setup() throws XPathExpressionException, XMLStreamException, IOException {
        super.init();
        driver = BrowserManager.getWebDriver();
        LoginUtils.login(driver, automationContext, getWebAppURL());
        driver.get(getWebAppURL() + Constants.IOT_USER_ADD_URL);
        uiElementMapper = UIElementMapper.getInstance();

        userNameField = driver.findElement(By.id(uiElementMapper.getElement("iot.admin.addUser.username.id")));
        firstNameField = driver.findElement(By.id(uiElementMapper.getElement("iot.admin.addUser.firstName.id")));
        lastNameField = driver.findElement(By.id(uiElementMapper.getElement("iot.admin.addUser.lastName.id")));
        emailField = driver.findElement(By.id(uiElementMapper.getElement("iot.admin.addUser.email.id")));

        addUserButton = driver.findElement(By.xpath(uiElementMapper.getElement("iot.admin.addUser.add.btn.xpath")));
    }

    @Test(description = "Test for empty form submission")
    public void emptyFormTest() {
        clearForm();

        firstNameField.sendKeys("");
        lastNameField.sendKeys("");
        emailField.sendKeys("");
        userNameField.sendKeys("");

        addUserButton.click();

        WebElement alert = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.admin.addUser.formError.xpath")));

        if (!alert.isDisplayed()) {
            Assert.assertTrue(false, "Alert for empty form not is displayed.");
        }

        Assert.assertEquals(alert.getText(), "Username is a required field. It cannot be empty.");
    }

    @Test(description = "Test for short user name")
    public void shortUserNameTest() {
        clearForm();

        firstNameField.sendKeys(Constants.ADD_USER_FIRST_NAME);
        lastNameField.sendKeys(Constants.ADD_USER_LAST_NAME);
        emailField.sendKeys(Constants.ADD_USER_EMAIL);
        userNameField.sendKeys(Constants.ADD_USER_SHORT_USER_NAME);

        addUserButton.click();

        WebElement alert = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.admin.addUser.formError.xpath")));

        if (!alert.isDisplayed()) {
            Assert.assertTrue(false, "Alert for short user name is not displayed.");
        }

        Assert.assertEquals(alert.getText(), Constants.ADD_USER_SHORT_USER_NAME_ERROR_MSG);
    }

    @Test(description = "Test for empty first name")
    public void emptyFirstNameTest() {
        clearForm();

        firstNameField.sendKeys("");
        lastNameField.sendKeys(Constants.ADD_USER_LAST_NAME);
        emailField.sendKeys(Constants.ADD_USER_EMAIL);
        userNameField.sendKeys(Constants.ADD_USER_USER_NAME);

        addUserButton.click();

        WebElement alert = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.admin.addUser.formError.xpath")));

        if (!alert.isDisplayed()) {
            Assert.assertTrue(false, "Alert for First name is not displayed.");
        }

        Assert.assertEquals(alert.getText(), Constants.ADD_USER_FIRST_NAME_ERROR_MSG);
    }

    @Test(description = "Test for empty last name")
    public void emptyLastNameTest() {
        clearForm();

        firstNameField.sendKeys(Constants.ADD_USER_FIRST_NAME);
        lastNameField.sendKeys("");
        emailField.sendKeys(Constants.ADD_USER_EMAIL);
        userNameField.sendKeys(Constants.ADD_USER_USER_NAME);

        addUserButton.click();

        WebElement alert = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.admin.addUser.formError.xpath")));

        if (!alert.isDisplayed()) {
            Assert.assertTrue(false, "Alert for Last name is not displayed.");
        }

        Assert.assertEquals(alert.getText(), Constants.ADD_USER_LAST_NAME_ERROR_MSG);
    }

    @Test(description = "Test for empty email name")
    public void emptyEmailTest() {
        clearForm();

        firstNameField.sendKeys(Constants.ADD_USER_FIRST_NAME);
        lastNameField.sendKeys(Constants.ADD_USER_LAST_NAME);
        emailField.sendKeys("");
        userNameField.sendKeys(Constants.ADD_USER_USER_NAME);

        addUserButton.click();

        WebElement alert = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.admin.addUser.formError.xpath")));

        if (!alert.isDisplayed()) {
            Assert.assertTrue(false, "Alert for E-mail is not displayed.");
        }

        Assert.assertEquals(alert.getText(), Constants.ADD_USER_NO_EMAIL_ERROR_MSG);
    }

    @Test(description = "Test for incorrect email")
    public void incorrectEmailTest() {
        clearForm();

        firstNameField.sendKeys(Constants.ADD_USER_FIRST_NAME);
        lastNameField.sendKeys(Constants.ADD_USER_LAST_NAME);
        emailField.sendKeys(Constants.ADD_USER_EMAIL_ERROR);
        userNameField.sendKeys(Constants.ADD_USER_USER_NAME);

        addUserButton.click();

        WebElement alert = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.admin.addUser.formError.xpath")));

        if (!alert.isDisplayed()) {
            Assert.assertTrue(false, "Alert for incorrect E-mail is not displayed.");
        }

        Assert.assertEquals(alert.getText(), Constants.ADD_USER_WRONG_EMAIL_ERROR_MSG);
    }

    private void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        userNameField.clear();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        driver.quit();
    }
}
