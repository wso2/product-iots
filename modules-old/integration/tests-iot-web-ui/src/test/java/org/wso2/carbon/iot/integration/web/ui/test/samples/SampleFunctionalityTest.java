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
 * Test cases for test the functionality of the connected cup sample.
 * In these test cases following features are tested.
 * 1. Setting temperature and Coffee level
 * 2. Order coffee
 * 3. Test the stat graphs
 */
public class SampleFunctionalityTest extends IOTIntegrationUIBaseTestCase {

    private WebDriver driverDevice;
    private WebDriver driverServer;
    private ConnectedCupDeviceInterface sampleViewPage;
    private ConnectedCupDeviceViewPage deviceViewPage;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws XPathExpressionException, XMLStreamException, IOException {
        super.init();
        driverDevice = BrowserManager.getWebDriver();
        driverServer = BrowserManager.getWebDriver();
        LoginUtils.login(driverServer, automationContext, getWebAppURL());
        driverServer.get(getWebAppURL() + Constants.IOT_DEVICES_URL);
        DevicesPage devicesPage = new DevicesPage(driverServer);
        deviceViewPage = devicesPage.viewDevice(Constants.IOT_CONNECTED_CUP_NAME);

        //Opens the connected cup device interface in the browser.
        driverDevice.get(deviceViewPage.getDeviceLink());
        sampleViewPage = new ConnectedCupDeviceInterface(driverDevice);
    }

    @Test(description = "Set the temperature level.",
          groups = Constants.TestSample.VERIFY,
          dependsOnGroups = Constants.TestSample.ENROLL_VERIFY)
    public void setTemperatureTest() {
        Assert.assertTrue(sampleViewPage.changeTemperature(Constants.ConnectedCup.TEMPERATURE));
    }

    @Test(description = "Set the coffee level.",
          groups = Constants.TestSample.VERIFY,
          dependsOnGroups = Constants.TestSample.ENROLL_VERIFY)
    public void setCoffeeLevelTest() throws IOException {
        Assert.assertTrue(sampleViewPage.changeCoffeeLevel(Constants.ConnectedCup.COFFEE_LEVEl));
    }

    @Test(description = "Verify order coffee function.",
          groups = Constants.TestSample.VERIFY,
          dependsOnGroups = Constants.TestSample.ENROLL_VERIFY)
    public void orderCoffeeTest() throws IOException, InterruptedException {
        Assert.assertTrue(sampleViewPage.orderCoffee());
    }

    @Test(description = "Test the graphs are present in device view.",
          groups = Constants.TestSample.VERIFY,
          dependsOnMethods = {"setTemperatureTest", "setCoffeeLevelTest", "orderCoffeeTest"})
    public void verifyGraphs() throws IOException {
        Assert.assertTrue(deviceViewPage.isGraphsAvailable(2));
    }

    @Test(description = "Test the Y axis name of Temperature graph.",
          groups = {Constants.TestSample.VERIFY,
                    Constants.TestSample.TEMPERATURE},
          dependsOnGroups = Constants.TestSample.ENROLL_VERIFY,
          dependsOnMethods = {"verifyGraphs"})
    public void temperatureGraphYAxisNameTest() throws IOException {
        Assert.assertTrue(deviceViewPage.graphAxisName(Constants.IOT_GRAPH_Y_AXIS,
                                                       Constants.ConnectedCup.TEMPERATURE_ID,
                                                       Constants.ConnectedCup.TEMPERATURE_Y_AXIS));
    }

    @Test(description = "Test the X axis name of Temperature graph.",
          groups = {Constants.TestSample.VERIFY,
                    Constants.TestSample.TEMPERATURE},
          dependsOnGroups = Constants.TestSample.ENROLL_VERIFY,
          dependsOnMethods = {"verifyGraphs"})
    public void temperatureGraphXAxisNameTest() throws IOException {
        Assert.assertTrue(deviceViewPage.graphAxisName(Constants.IOT_GRAPH_X_AXIS,
                                                       Constants.ConnectedCup.TEMPERATURE_ID,
                                                       Constants.ConnectedCup.TEMPERATURE_X_AXIS));
    }

    @Test(description = "Test the whether the Coffee Level graph legend is present.",
          groups = {Constants.TestSample.VERIFY,
                    Constants.TestSample.TEMPERATURE},
          dependsOnGroups = Constants.TestSample.ENROLL_VERIFY,
          dependsOnMethods = {"verifyGraphs"})
    public void temperatureGraphLegendTest() {
        Assert.assertTrue(deviceViewPage.graphLegendName(Constants.ConnectedCup.TEMPERATURE_ID,
                                                         Constants.ConnectedCup.TEMPERATURE_LEGEND));
    }

    @Test(description = "Test the whether the Temperature graph path is visible.",
          groups = {Constants.TestSample.VERIFY,
                    Constants.TestSample.TEMPERATURE},
          dependsOnGroups = Constants.TestSample.ENROLL_VERIFY,
          dependsOnMethods = {"verifyGraphs"})
    public void temperatureGraphPathTest() {
        Assert.assertTrue(deviceViewPage.checkGraphPath(Constants.ConnectedCup.TEMPERATURE_GRAPH_ID));
    }

    @Test(description = "Test the whether the Temperature graph gets values.",
          groups = {Constants.TestSample.VERIFY,
                    Constants.TestSample.TEMPERATURE},
          dependsOnGroups = Constants.TestSample.ENROLL_VERIFY,
          dependsOnMethods = {"verifyGraphs"})
    public void temperatureGraphDataPublisherTest() {
        Assert.assertTrue(deviceViewPage.checkGraphValues(Constants.ConnectedCup.TEMPERATURE_GRAPH_ID,
                                                          Constants.ConnectedCup.TEMPERATURE));
    }

    @Test(description = "Test the Y axis name of Coffee Level graph.",
          groups = Constants.TestSample.COFFEE_LEVEL,
          dependsOnGroups = Constants.TestSample.TEMPERATURE)
    public void coffeeLevelGraphYAxisNameTest() {
        Assert.assertTrue(deviceViewPage.graphAxisName(Constants.IOT_GRAPH_Y_AXIS,
                                                       Constants.ConnectedCup .COFFEE_LEVEL_ID,
                                                       Constants.ConnectedCup.COFFEE_LEVEL_Y_AXIS));
    }

    @Test(description = "Test the X axis name of Coffee Level graph.",
          groups =  Constants.TestSample.COFFEE_LEVEL,
          dependsOnGroups = {Constants.TestSample.ENROLL_VERIFY,
                             Constants.TestSample.TEMPERATURE})
    public void coffeeLevelGraphXAxisNameTest() {
        Assert.assertTrue(deviceViewPage.graphAxisName(Constants.IOT_GRAPH_X_AXIS,
                                                       Constants.ConnectedCup.COFFEE_LEVEL_ID,
                                                       Constants.ConnectedCup.COFFEE_LEVEL_X_AXIS));
    }

    @Test(description = "Test the whether the Coffee Level graph legend is present.",
          groups =  Constants.TestSample.COFFEE_LEVEL,
          dependsOnGroups = {Constants.TestSample.TEMPERATURE})
    public void coffeeLevelGraphLegendTest() throws IOException {
        Assert.assertTrue(deviceViewPage.graphLegendName(Constants.ConnectedCup.COFFEE_LEVEL_ID,
                                                         Constants.ConnectedCup.COFFEE_LEVEL_LEGEND));
    }

    @Test(description = "Test the whether the Coffee Level graph path is visible.",
          groups =  Constants.TestSample.COFFEE_LEVEL,
          dependsOnGroups = {Constants.TestSample.TEMPERATURE})
    public void coffeeLevelGraphPathTest() {
        Assert.assertTrue(deviceViewPage.checkGraphPath(Constants.ConnectedCup.COFFEE_LEVEL_GRAPH_ID));
    }

    @Test(description = "Test the whether the Coffee Level graph gets values.",
          groups =  Constants.TestSample.COFFEE_LEVEL,
          dependsOnGroups = Constants.TestSample.TEMPERATURE)
    public void coffeeLevelGraphDataPublisherTest() {
        Assert.assertTrue(deviceViewPage.checkGraphValues(Constants.ConnectedCup.COFFEE_LEVEL_GRAPH_ID,
                                                          Constants.ConnectedCup.COFFEE_LEVEl));
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        driverServer.quit();
        driverDevice.quit();
    }

}
