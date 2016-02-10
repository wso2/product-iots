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
 * Login page of the iot server
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

    public NewUserRegisterPage registerNewUser() throws IOException {
        WebElement registerLink = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.user.register.link.xpath")));
        registerLink.click();
        return new NewUserRegisterPage(driver);
    }

}
