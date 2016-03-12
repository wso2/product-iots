/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.iot.integration.web.ui.test.samples;

import junit.framework.Assert;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.iot.integration.web.ui.test.common.Constants;
import org.wso2.carbon.iot.integration.web.ui.test.common.LoginUtils;
import org.wso2.carbon.iot.integration.web.ui.test.common.IOTIntegrationUIBaseTestCase;
import org.wso2.iot.integration.ui.pages.devices.EnrollDevicePage;
import org.wso2.iot.integration.ui.pages.home.IOTAdminDashboard;
import org.wso2.iot.integration.ui.pages.samples.ConnectedCupDeviceTypeViewPage;

import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

/**
 * Test cases to verify the enrolment process.
 */
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

    @Test(description = "Verify the pop up modal is displayed.",
          groups = {"iot.enroll"},
          dependsOnGroups = {"iot.install"})
    public void enrollDevicePopUpModalTest() throws InterruptedException, IOException {
        Assert.assertTrue(connectedCupDeviceTypeViewPage.isPopUpPresent());
    }

    @Test(description = "Test case for device enrolment",
          groups = {"iot.enroll"},
          dependsOnMethods = {"enrollDevicePopUpModalTest"})
    public void enrollDeviceTest() throws InterruptedException {
        Assert.assertTrue(connectedCupDeviceTypeViewPage.enrollDevice(Constants.IOT_CONNECTED_CUP_NAME));
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        driver.quit();
    }

}
