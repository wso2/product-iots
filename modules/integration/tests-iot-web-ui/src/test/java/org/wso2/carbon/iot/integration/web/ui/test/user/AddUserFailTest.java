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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.iot.integration.web.ui.test.Constants;
import org.wso2.carbon.iot.integration.web.ui.test.LoginUtils;
import org.wso2.iot.integration.ui.pages.IOTIntegrationUIBaseTestCase;
import org.wso2.iot.integration.ui.pages.UIElementMapper;

import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

/**
 * Test cases for
 * 1. Empty  form submission
 * 2. Short user name
 * 3. Empty First Name
 * 4. Empty Last Name
 * 5. Empty email
 * 6. Incorrect email
 */
public class AddUserFailTest extends IOTIntegrationUIBaseTestCase {

    private WebDriver driver;
    UIElementMapper uiElementMapper;

    WebElement firstNameField;
    WebElement lastNameField;
    WebElement emailField;
    WebElement userNameField;
    WebElement addUserButton;

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
    public void emptyFormTest() throws Exception {
        clearForm();

        firstNameField.sendKeys("");
        lastNameField.sendKeys("");
        emailField.sendKeys("");
        userNameField.sendKeys("");

        addUserButton.click();

        Assert.assertEquals(driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.admin.addUser.formError.xpath"))).getText(),
                            "Username is a required field. It cannot be empty.");
    }

    @Test(description = "Test for short user name")
    public void shortUserNameTest() throws Exception {
        clearForm();

        firstNameField.sendKeys("User");
        lastNameField.sendKeys("User");
        emailField.sendKeys("user@wso2.com");
        userNameField.sendKeys("us");

        addUserButton.click();

        Assert.assertEquals(driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.admin.addUser.formError.xpath"))).getText(),
                            "Username must be between 3 and 30 characters long.");
    }

    @Test(description = "Test for empty first name")
    public void emptyFirstNameTest() throws Exception {
        clearForm();

        firstNameField.sendKeys("");
        lastNameField.sendKeys("User");
        emailField.sendKeys("user@wso2.com");
        userNameField.sendKeys("user1");

        addUserButton.click();

        Assert.assertEquals(driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.admin.addUser.formError.xpath"))).getText(),
                            "Firstname is a required field. It cannot be empty.");
    }

    @Test(description = "Test for empty last name")
    public void emptyLastNameTest() throws Exception {
        clearForm();

        firstNameField.sendKeys("User");
        lastNameField.sendKeys("");
        emailField.sendKeys("user@wso2.com");
        userNameField.sendKeys("user1");

        addUserButton.click();
        Assert.assertEquals(driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.admin.addUser.formError.xpath"))).getText(),
                            "Lastname is a required field. It cannot be empty.");
    }

    @Test(description = "Test for empty email name")
    public void emptyEmailTest() throws Exception {
        clearForm();

        firstNameField.sendKeys("User");
        lastNameField.sendKeys("User");
        emailField.sendKeys("");
        userNameField.sendKeys("user1");

        addUserButton.click();

        Assert.assertEquals(driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.admin.addUser.formError.xpath"))).getText(),
                            "Email is a required field. It cannot be empty.");
    }

    @Test(description = "Test for incorrect email")
    public void incorrectEmailTest() throws Exception {
        clearForm();

        firstNameField.sendKeys("User");
        lastNameField.sendKeys("User");
        emailField.sendKeys("user.com");
        userNameField.sendKeys("user1");

        addUserButton.click();

        Assert.assertEquals(driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.admin.addUser.formError.xpath"))).getText(),
                            "Provided email is invalid. Please check.");

    }

    private void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        userNameField.clear();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }

}
