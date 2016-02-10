package org.wso2.carbon.iot.integration.web.ui.test.user;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.iot.integration.web.ui.test.Constants;
import org.wso2.iot.integration.ui.pages.IOTIntegrationUIBaseTestCase;
import org.wso2.iot.integration.ui.pages.UIElementMapper;
import org.wso2.iot.integration.ui.pages.home.IOTHomePage;
import org.wso2.iot.integration.ui.pages.login.LoginPage;
import org.wso2.iot.integration.ui.pages.uesr.NewUserRegisterPage;

/**
 * Test for registering a new user and login
 *
 */
public class RegisterTest extends IOTIntegrationUIBaseTestCase {
    private WebDriver driver;
    UIElementMapper uiElementMapper;

    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getWebAppURL() + Constants.IOT_LOGIN_PATH);
    }

    @Test(description = "Verify new User registration")
    public void testUserRegister() throws Exception {
        LoginPage test = new LoginPage(driver);
        uiElementMapper = UIElementMapper.getInstance();
        NewUserRegisterPage registerTest = test.registerNewUser();
        LoginPage loginPage = registerTest.addUser(
                uiElementMapper.getElement("iot.user.add.firstname"),
                uiElementMapper.getElement("iot.user.add.lastname"),
                uiElementMapper.getElement("iot.user.add.email"),
                uiElementMapper.getElement("iot.user.add.username"),
                uiElementMapper.getElement("iot.user.add.password"),
                uiElementMapper.getElement("iot.user.add.password"));

        IOTHomePage homePage = loginPage.loginAsUser(uiElementMapper.getElement("iot.user.add.username"),
                                                     uiElementMapper.getElement("iot.user.add.password"));

        if (!homePage.checkUserName()){
            throw new Exception("Incorrect user logged in");
        }

        homePage.logout();

        driver.close();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }
}
