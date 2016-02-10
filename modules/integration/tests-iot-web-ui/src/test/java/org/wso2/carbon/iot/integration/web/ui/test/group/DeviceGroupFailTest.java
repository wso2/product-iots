package org.wso2.carbon.iot.integration.web.ui.test.group;

import org.junit.Assert;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.iot.integration.web.ui.test.Constants;
import org.wso2.carbon.iot.integration.web.ui.test.LoginUtils;
import org.wso2.iot.integration.ui.pages.IOTIntegrationUIBaseTestCase;
import org.wso2.iot.integration.ui.pages.groups.DeviceAddGroupPage;

/**
 * Created by menaka on 1/25/16.
 *
 */
public class DeviceGroupFailTest extends IOTIntegrationUIBaseTestCase {
    private WebDriver driver;

    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        LoginUtils.login(driver, automationContext, getWebAppURL());
    }

    @Test(description = "Test for submitting an empty form.")
    public void addNewGroupFailTest () throws Exception {
        driver.get(getWebAppURL() + Constants.IOT_GROUP_ADD_URL);
        DeviceAddGroupPage addGroupPage = new DeviceAddGroupPage(driver);

        Assert.assertEquals(addGroupPage.submitEmptyForm(), "Group Name is a required field. It cannot be empty.");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }
}
