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
import org.wso2.iot.integration.ui.pages.devices.DevicesPage;
import org.wso2.iot.integration.ui.pages.samples.ConnectedCupDeviceViewPage;
import org.wso2.iot.integration.ui.pages.samples.ConnectedCupDeviceInterface;

import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

/**
 * Test cases to check whether the sample is enrolled correctly.
 */
public class SampleEnrolmentVerificationTest extends IOTIntegrationUIBaseTestCase {
    private WebDriver webDriver;
    private DevicesPage devicesPage;
    private ConnectedCupDeviceViewPage connectedCupDeviceViewPage;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws XPathExpressionException, XMLStreamException, IOException {
        super.init();
        webDriver = BrowserManager.getWebDriver();
        LoginUtils.login(webDriver, automationContext, getWebAppURL());
        webDriver.get(getWebAppURL() + Constants.IOT_DEVICES_URL);
        devicesPage = new DevicesPage(webDriver);
    }

    @Test(description = "Verify enrolment of the sample device",
          groups = {"iot.enroll.verify"},
          dependsOnGroups = "iot.enroll")
    public void verifyEnrollmentTest() {
        Assert.assertTrue(devicesPage.isDeviceEnrolled(Constants.IOT_CONNECTED_CUP_NAME));
    }

    @Test(description = "Verify navigation to device view",
          dependsOnMethods = "verifyEnrollmentTest",
          groups = {"iot.enroll.verify"})
    public void verifyNavigationTest() throws IOException {
        connectedCupDeviceViewPage = devicesPage.viewDevice(Constants.IOT_CONNECTED_CUP_NAME);
        Assert.assertNotNull(connectedCupDeviceViewPage);
    }

    @Test(description = "Verify sample functions",
          dependsOnMethods = {"verifyNavigationTest"})
    public void sampleStartUpTest() throws IOException {
        ConnectedCupDeviceInterface sampleViewPage = connectedCupDeviceViewPage.gotoDevice();
        Assert.assertNotNull(sampleViewPage);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        webDriver.quit();
    }
}
