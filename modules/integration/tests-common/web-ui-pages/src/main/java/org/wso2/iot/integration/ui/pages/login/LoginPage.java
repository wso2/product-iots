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
package org.wso2.iot.integration.ui.pages.login;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.iot.integration.ui.pages.UIElementMapper;
import org.wso2.iot.integration.ui.pages.home.IOTAdminDashboard;
import org.wso2.iot.integration.ui.pages.home.IOTHomePage;
import org.wso2.iot.integration.ui.pages.uesr.NewUserRegisterPage;

import java.io.IOException;

/**
 * Represents the Login page of the iot server.
 *
 */
public class LoginPage {
    private static final Log log = LogFactory.getLog(LoginPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public LoginPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        WebDriverWait webDriverWait = new WebDriverWait(driver, 10);

        if (!webDriverWait.until(ExpectedConditions.titleContains("Login | IoT Server"))) {
            throw new IllegalStateException("This is not the Login page");
        }
    }

    /**
     * Method to login as a registered user.
     * @param username Username of the user.
     * @param password Password of the user.
     * @return After a user is logged in, it will redirect to the IOT Server Home page.
     * @throws IOException
     */
    public IOTHomePage loginAsUser(String username, String password) throws IOException {
        log.info("Login as " + username);
        WebElement userNameField = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.user.login.input.username.xpath")));
        WebElement passwordField = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.user.login.input.password.xpath")));
        userNameField.sendKeys(username);
        passwordField.sendKeys(password);
        driver.findElement(By.xpath(uiElementMapper.getElement("iot.user.login.button.xpath"))).click();
        return new IOTHomePage(driver);
    }

    /**
     * Method to login as the server admin.
     * @param username The admin user name (admin).
     * @param password The admin password (admin).
     * @return After admin logged in, it will redirect to the IOT Server dashboard.
     * @throws IOException
     */
    public IOTAdminDashboard loginAsAdmin(String username, String password) throws IOException {
        log.info("Login as " + username);
        WebElement userNameField = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.user.login.input.username.xpath")));
        WebElement passwordField = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.user.login.input.password.xpath")));
        userNameField.sendKeys(username);
        passwordField.sendKeys(password);
        driver.findElement(By.xpath(uiElementMapper.getElement("iot.user.login.button.xpath"))).click();
        return new IOTAdminDashboard(driver);
    }

    /**
     * Method for new user registration.
     * @return After clicking the Register link, it will navigate to the User Registration page.
     * @throws IOException
     */
    public NewUserRegisterPage registerNewUser() throws IOException {
        WebElement registerLink = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.user.register.link.xpath")));
        registerLink.click();
        return new NewUserRegisterPage(driver);
    }

}
