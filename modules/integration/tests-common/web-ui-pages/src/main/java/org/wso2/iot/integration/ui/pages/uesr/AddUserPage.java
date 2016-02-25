package org.wso2.iot.integration.ui.pages.uesr;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.iot.integration.ui.pages.UIConstants;
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

        WebDriverWait webDriverWait = new WebDriverWait(driver, UIConstants.webDriverTimeOut);
        if (!webDriverWait.until(ExpectedConditions.titleContains("User Management | IoT Server"))) {
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

        WebDriverWait wait = new WebDriverWait(driver, UIConstants.webDriverTimeOut);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                uiElementMapper.getElement("iot.admin.addUser.add.btn.xpath"))));

        driver.findElement(By.xpath(uiElementMapper.getElement("iot.admin.addUser.add.btn.xpath"))).click();

        return new UserAddedConfirmationPage(driver);
    }
}