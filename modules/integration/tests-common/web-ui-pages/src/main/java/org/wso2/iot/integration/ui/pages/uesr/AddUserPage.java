package org.wso2.iot.integration.ui.pages.uesr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.iot.integration.ui.pages.UIElementMapper;

public class AddUserPage {
    private static final Log log = LogFactory.getLog(AddUserPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public AddUserPage(WebDriver driver) throws Exception {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();

        WebDriverWait webDriverWait = new WebDriverWait(driver, 10);
        if (!webDriverWait.until(ExpectedConditions.titleContains("User Management | IoT Server"))){
            throw new IllegalStateException("This is not the Add User page");
        }
    }

    public UserAddedPage createNewUser(String userName, String firstName, String lastName, String email) throws Exception {

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

        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                uiElementMapper.getElement("iot.admin.addUser.add.btn.xpath"))));

        driver.findElement(By.xpath("//button[@id='add-user-btn']")).click();

        return new UserAddedPage(driver);
    }


}