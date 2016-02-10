package org.wso2.carbon.iot.integration.web.ui.test.user;

import org.apache.bcel.classfile.Constant;
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
import org.wso2.carbon.iot.integration.web.ui.test.LoginUtils;
import org.wso2.iot.integration.ui.pages.IOTIntegrationUIBaseTestCase;
import org.wso2.iot.integration.ui.pages.UIElementMapper;
import org.wso2.iot.integration.ui.pages.home.IOTAdminDashboard;
import org.wso2.iot.integration.ui.pages.uesr.AddUserPage;
import org.wso2.iot.integration.ui.pages.uesr.UserListingPage;

/**
 * Test for checking admin capabilities.
 *    - Create a new User
 *    - Delete a user
 *    -- and more
 */
public class TestAdminFunctions extends IOTIntegrationUIBaseTestCase {

    private WebDriver driver;

    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        LoginUtils.login(driver, automationContext, getWebAppURL());
    }

    @Test(description = "Test for creating a new user")
    public void createUserTest() throws Exception {
        IOTAdminDashboard adminDashboard = new IOTAdminDashboard(driver);
        AddUserPage addUserPage = adminDashboard.addUser();
        addUserPage.createNewUser("user1", "User", "User", "user@wso2.com");
    }

    @Test(description = "Test for deleting a created user", dependsOnMethods = {"createUserTest"})
    public void deleteUserTest() throws Exception {
        driver.get(getWebAppURL() + Constants.IOT_HOME_URL);
        IOTAdminDashboard adminDashboard = new IOTAdminDashboard(driver);
        UserListingPage userListingPage = adminDashboard.viewUser();
        userListingPage.deleteUser();
    }


    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }
}
