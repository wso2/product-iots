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
import org.wso2.iot.integration.ui.pages.login.LoginPage;

import java.io.IOException;

/**
 * This class represents the new user registration page.
 * User registration page has the registration form for new users to enter the required data and the submit button.
 */
public class NewUserRegisterPage {

    private WebDriver driver;
    private WebElement firstNameField;
    private WebElement lastNameField;
    private WebElement emailField;
    private WebElement userNameField;
    private WebElement passwordField;
    private WebElement passwordConfirmationField;
    private WebElement registerButton;

    public NewUserRegisterPage(WebDriver driver) throws IOException {
        this.driver = driver;
        UIElementMapper uiElementMapper = UIElementMapper.getInstance();

        // Check that we're on the right page.
        WebDriverWait webDriverWait = new WebDriverWait(driver, UIUtils.webDriverTimeOut);
        if (!webDriverWait.until(ExpectedConditions.titleContains(uiElementMapper.getElement("cdmf.register.page")))) {
            throw new IllegalStateException("This is not the Register page");
        }

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

    /**
     * The method to register a new user.
     *
     * @param firstName       First name of the user.
     * @param lastName        Last name of the user.
     * @param email           Email address of the user.
     * @param userName        User name for the user. This will be used to login to the server.
     * @param password        The password for the user.
     * @param confirmPassword Password confirmation.
     * @return After the user is created it navigates back to the login page.
     * @throws IOException
     */
    public LoginPage registerUser(String firstName, String lastName, String email, String userName, String password,
                                  String confirmPassword) throws IOException {
        handleAction(firstName, lastName, email, userName, password, confirmPassword);
        return new LoginPage(driver);
    }

    /**
     * Following method is to validate the user registration form.
     * */
    public void validateForm(String firstName, String lastName, String email, String userName,
                             String password, String confirmPassword) {
        handleAction(firstName, lastName, email, userName, password, confirmPassword);
    }

    /**
     * Support method to populate the User registration form.
     * @param firstName : First name of the user.
     * @param lastName : Last name of the user.
     * @param email : E mail of the user.
     * @param userName : User name of the user.
     * @param password : Password for the user.
     * @param confirmPassword : Confirmation password.
     */
    private void handleAction(String firstName, String lastName, String email, String userName, String password,
                             String confirmPassword) {
        clearForm();
        firstNameField.sendKeys(firstName);
        lastNameField.sendKeys(lastName);
        emailField.sendKeys(email);
        userNameField.sendKeys(userName);
        passwordField.sendKeys(password);
        passwordConfirmationField.sendKeys(confirmPassword);
        registerButton.click();
    }

    /**
     * Support method to clear the Registration form.
     */
    private void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        userNameField.clear();
        passwordField.clear();
        passwordConfirmationField.clear();
    }

}
