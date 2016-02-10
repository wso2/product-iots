package org.wso2.carbon.iot.integration.web.ui.test.login;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.iot.integration.web.ui.test.Constants;
import org.wso2.iot.integration.ui.pages.IOTIntegrationUIBaseTestCase;
import org.wso2.iot.integration.ui.pages.home.IOTAdminDashboard;
import org.wso2.iot.integration.ui.pages.login.LoginPage;

/**
 * Test Login as Admin
 *
 */
public class LoginTest extends IOTIntegrationUIBaseTestCase {
    private WebDriver driver;

    @BeforeClass (alwaysRun = true)
    public void setup() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getWebAppURL() + Constants.IOT_LOGIN_PATH);
    }

    @Test (description = "Verify login to IOT server dashboard")
    public void testAdminLogin() throws Exception {
        LoginPage test = new LoginPage(driver);
        IOTAdminDashboard dashboard = test.loginAsAdmin(
                                              automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                                              automationContext.getSuperTenant().getTenantAdmin().getPassword());
        dashboard.logout();
        driver.close();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }
}
