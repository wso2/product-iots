package org.wso2.iot.integration.ui.pages.uesr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.iot.integration.ui.pages.UIElementMapper;
import org.wso2.iot.integration.ui.pages.login.LoginPage;

import java.io.IOException;

/**
 * Created by menaka on 1/19/16.
 */
public class NewUserRegisterPage {
    private static final Log log = LogFactory.getLog(NewUserRegisterPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public NewUserRegisterPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.
        WebDriverWait webDriverWait = new WebDriverWait(driver, 10);
        if (!webDriverWait.until(ExpectedConditions.titleContains("Register | IoT Server"))){
            throw new IllegalStateException("This is not the Register page");
        }
    }

    public LoginPage addUser(String firstName, String lastName, String email, String userName, String password,
                             String
            confirmPassword) throws IOException {
        WebElement firstNameField = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.user.add.input.firstname.xpath")));
        WebElement lastNameField = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.user.add.input.lastname.xpath")));
        WebElement emailField = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.user.add.input.email.xpath")));
        WebElement userNameField = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.user.add.input.username.xpath")));
        WebElement passwordField = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.user.add.input.password.xpath")));
        WebElement passwordConfirmationField = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.user.add.input.confirmpassword.xpath")));
        firstNameField.sendKeys(firstName);
        lastNameField.sendKeys(lastName);
        emailField.sendKeys(email);
        userNameField.sendKeys(userName);
        passwordField.sendKeys(password);
        passwordConfirmationField.sendKeys(confirmPassword);
        driver.findElement(By.xpath(uiElementMapper.getElement("iot.user.add.register.button.xpath"))).click();
        return new LoginPage(driver);
    }

}
