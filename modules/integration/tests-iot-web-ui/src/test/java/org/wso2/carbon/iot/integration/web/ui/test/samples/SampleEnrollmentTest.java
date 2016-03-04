package org.wso2.carbon.iot.integration.web.ui.test.samples;

import junit.framework.Assert;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.iot.integration.web.ui.test.Constants;
import org.wso2.carbon.iot.integration.web.ui.test.LoginUtils;
import org.wso2.iot.integration.ui.pages.IOTIntegrationUIBaseTestCase;
import org.wso2.iot.integration.ui.pages.devices.EnrollDevicePage;
import org.wso2.iot.integration.ui.pages.home.IOTAdminDashboard;
import org.wso2.iot.integration.ui.pages.samples.ConnectedCupDeviceTypeViewPage;

import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

public class SampleEnrollmentTest extends IOTIntegrationUIBaseTestCase {
    private WebDriver driver;
    private ConnectedCupDeviceTypeViewPage connectedCupDeviceTypeViewPage;

    @BeforeClass(alwaysRun = true)
    public void setup() throws XPathExpressionException, XMLStreamException, IOException {
        super.init();
        driver = BrowserManager.getWebDriver();
        LoginUtils.login(driver, automationContext, getWebAppURL());
        IOTAdminDashboard adminDashboard = new IOTAdminDashboard(driver);
        EnrollDevicePage enrollDevicePage = adminDashboard.enrollNewDevice();
        connectedCupDeviceTypeViewPage = enrollDevicePage.gotoConnectedCupDeviceTypeViewPage();
    }

    @Test(description = "Verify the pop up modal is displayed.", groups = {"iot.enroll"}, dependsOnGroups = {"iot.install"})
    public void enrollDevicePopUpModalTest() throws InterruptedException, IOException {
        Assert.assertTrue(connectedCupDeviceTypeViewPage.isPopUpPresent());
    }

    @Test(description = "Test case for device enrolment", groups = {"iot.enroll"}, dependsOnMethods =
            {"enrollDevicePopUpModalTest"})
    public void enrollDeviceTest() throws InterruptedException {
        Assert.assertTrue(connectedCupDeviceTypeViewPage.enrollDevice(Constants.IOT_CONNECTED_CUP_NAME));
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        driver.quit();
    }

}
