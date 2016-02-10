package org.wso2.carbon.iot.integration.web.ui.test.group;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.iot.integration.web.ui.test.Constants;
import org.wso2.carbon.iot.integration.web.ui.test.LoginUtils;
import org.wso2.iot.integration.ui.pages.IOTIntegrationUIBaseTestCase;
import org.wso2.iot.integration.ui.pages.groups.DeviceAddGroupPage;
import org.wso2.iot.integration.ui.pages.groups.DeviceGroupsPage;
import org.wso2.iot.integration.ui.pages.home.IOTAdminDashboard;

/**
 * Test cases for grouping feature of IOT server.
 */
public class DeviceGroupTest extends IOTIntegrationUIBaseTestCase {
    private WebDriver driver;
    private IOTAdminDashboard adminDashboard;

    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        LoginUtils.login(driver, automationContext, getWebAppURL());
        adminDashboard = new IOTAdminDashboard(driver);
    }

    @Test(description = "Test for adding a new device group.")
    public void addNewGroupTest () throws Exception {
        DeviceAddGroupPage addGroupPage = adminDashboard.addGroup();
        addGroupPage.addNewGroup("group1", "This is test group");
    }

    @Test (description = "Check whether the created group exists", dependsOnMethods = {"addNewGroupTest"})
    public void isGroupCreatedTest () throws Exception {
        driver.get(getWebAppURL() + Constants.IOT_HOME_URL);
        DeviceGroupsPage groupsPage = adminDashboard.viewGroups();
        Assert.assertTrue(groupsPage.isGroupCreated("group1"));
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }

}
