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
package org.wso2.iot.integration.ui.pages.uesr;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.iot.integration.ui.pages.UIUtils;
import org.wso2.iot.integration.ui.pages.UIElementMapper;

import java.io.IOException;

/**
 * This class represents the add user page of the IOT server.
 */
public class AddUserPage {

    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public AddUserPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();

        WebDriverWait webDriverWait = new WebDriverWait(driver, UIUtils.webDriverTimeOut);
        if (!webDriverWait.until(ExpectedConditions.titleContains(uiElementMapper.getElement("cdmf.user.add.page")))) {
            throw new IllegalStateException("This is not the Add User page");
        }
    }

    /**
     * Method to create a new user.
     *
     * @param userName  The username for the user.
     * @param firstName The user's first name.
     * @param lastName  The user's last name.
     * @param email     Email address of the user.
     * @return The user added confirmation page.
     */
    public UserAddedConfirmationPage createNewUser(String userName, String firstName, String lastName, String email)
            throws IOException {

        WebElement userNameField = driver.findElement(By.id(
                uiElementMapper.getElement("iot.admin.addUser.username.id")));
        WebElement firstNameField = driver.findElement(By.id(
                uiElementMapper.getElement("iot.admin.addUser.firstName.id")));
        WebElement lastNameField = driver.findElement(By.id(
                uiElementMapper.getElement("iot.admin.addUser.lastName.id")));
        WebElement emailField = driver.findElement(By.id(
                uiElementMapper.getElement("iot.admin.addUser.email.id")));

        userNameField.sendKeys(userName);
        firstNameField.sendKeys(firstName);
        lastNameField.sendKeys(lastName);
        emailField.sendKeys(email);

        driver.findElement(By.xpath(uiElementMapper.getElement("iot.admin.addUser.add.btn.xpath"))).click();

        return new UserAddedConfirmationPage(driver);
    }
}