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
import org.wso2.carbon.iot.integration.web.ui.test.Constants;
import org.wso2.carbon.iot.integration.web.ui.test.LoginUtils;
import org.wso2.iot.integration.ui.pages.IOTIntegrationUIBaseTestCase;
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
    private ConnectedCupDeviceViewPage connectedCupDeviceViewPage;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws XPathExpressionException, XMLStreamException, IOException {
        super.init();
        driverDevice = BrowserManager.getWebDriver();
        driverServer = BrowserManager.getWebDriver();
        LoginUtils.login(driverServer, automationContext, getWebAppURL());
        driverServer.get(getWebAppURL() + Constants.IOT_DEVICES_URL);
        DevicesPage devicesPage = new DevicesPage(driverServer);
        connectedCupDeviceViewPage = devicesPage.viewDevice(Constants.IOT_CONNECTED_CUP_NAME);
        driverDevice.get(connectedCupDeviceViewPage.getDeviceLink());
        sampleViewPage = new ConnectedCupDeviceInterface(driverDevice);
    }

    @Test(description = "Set the temperature level.",
          groups = {"iot.sample.verify"},
          dependsOnGroups = "iot.enroll.verify")
    public void setTemperatureTest() {
        Assert.assertTrue(sampleViewPage.changeTemperature(Constants.IOT_CONNECTED_CUP_TEMPERATURE));
    }

    @Test(description = "Set the coffee level.",
          groups = {"iot.sample.verify"},
          dependsOnGroups = "iot.enroll.verify")
    public void setCoffeeLevelTest() throws IOException {
        Assert.assertTrue(sampleViewPage.changeCoffeeLevel(Constants.IOT_CONNECTED_CUP_LEVEl));
    }

    @Test(description = "Verify order coffee function.",
          groups = {"iot.sample.verify"},
          dependsOnGroups = "iot.enroll.verify")
    public void orderCoffeeTest() throws IOException, InterruptedException {
        Assert.assertTrue(sampleViewPage.orderCoffee());
    }

    @Test(description = "Test the graphs are present in device view.",
          groups = {"iot.sample.verify"},
          dependsOnMethods = {"setTemperatureTest", "setCoffeeLevelTest", "orderCoffeeTest"})
    public void verifyGraphs() throws IOException {
        Assert.assertTrue(connectedCupDeviceViewPage.isGraphsAvailable(2));
    }

    @Test(description = "Test the Y axis name of Temperature graph.",
          groups = {"iot.sample.verify", "sample.temp"},
          dependsOnGroups = {"iot.enroll.verify"},
          dependsOnMethods = {"verifyGraphs"})
    public void temperatureGraphYAxisNameTest() throws IOException {
        Assert.assertTrue(connectedCupDeviceViewPage.graphAxisName(Constants.IOT_GRAPH_Y_AXIS,
                                                                   Constants.IOT_CONNECTED_CUP_TEMPERATURE_ID,
                                                                   Constants.IOT_CONNECTED_CUP_TEMPERATURE_Y_AXIS));
    }

    @Test(description = "Test the X axis name of Temperature graph.",
          groups = {"iot.sample.verify", "sample.temp"},
          dependsOnGroups = {"iot.enroll.verify"},
          dependsOnMethods = {"verifyGraphs"})
    public void temperatureGraphXAxisNameTest() throws IOException {
        Assert.assertTrue(connectedCupDeviceViewPage.graphAxisName(Constants.IOT_GRAPH_X_AXIS,
                                                                   Constants.IOT_CONNECTED_CUP_TEMPERATURE_ID,
                                                                   Constants.IOT_CONNECTED_CUP_TEMPERATURE_X_AXIS));
    }

    @Test(description = "Test the whether the Coffee Level graph legend is present.",
          groups = {"iot.sample.verify", "sample.temp"},
          dependsOnGroups = {"iot.enroll.verify"},
          dependsOnMethods = {"verifyGraphs"})
    public void temperatureGraphLegendTest() {
        Assert.assertTrue(connectedCupDeviceViewPage.graphLegendName(Constants.IOT_CONNECTED_CUP_TEMPERATURE_ID,
                                                                     Constants.IOT_CONNECTED_CUP_TEMPERATURE_LEGEND));
    }

    @Test(description = "Test the whether the Temperature graph path is visible.",
          groups = {"iot.sample.verify", "sample.temp"},
          dependsOnGroups = {"iot.enroll.verify"},
          dependsOnMethods = {"verifyGraphs"})
    public void temperatureGraphPathTest() {
        Assert.assertTrue(connectedCupDeviceViewPage.checkGraphPath(Constants.IOT_CONNECTED_CUP_TEMPERATURE_GRAPH_ID));
    }

    @Test(description = "Test the whether the Temperature graph gets values.",
          groups = {"iot.sample.verify", "sample.temp"},
          dependsOnGroups = {"iot.enroll.verify"},
          dependsOnMethods = {"verifyGraphs"})
    public void temperatureGraphDataPublisherTest() {
        Assert.assertTrue(connectedCupDeviceViewPage.checkGraphValues(Constants.IOT_CONNECTED_CUP_TEMPERATURE_GRAPH_ID,
                                                                      Constants.IOT_CONNECTED_CUP_TEMPERATURE));
    }

    @Test(description = "Test the Y axis name of Coffee Level graph.",
          groups = {"iot.sample.coffee"},
          dependsOnGroups = {"sample.temp"})
    public void coffeeLevelGraphYAxisNameTest() {
        Assert.assertTrue(connectedCupDeviceViewPage.graphAxisName(Constants.IOT_GRAPH_Y_AXIS,
                                                                   Constants.IOT_CONNECTED_CUP_COFFEE_LEVEL_ID,
                                                                   Constants.IOT_CONNECTED_CUP_COFFEE_LEVEL_Y_AXIS));
    }

    @Test(description = "Test the X axis name of Coffee Level graph.",
          groups = {"iot.sample.coffee"},
          dependsOnGroups = {"iot.enroll.verify", "sample.temp"})
    public void coffeeLevelGraphXAxisNameTest() {
        Assert.assertTrue(connectedCupDeviceViewPage.graphAxisName(Constants.IOT_GRAPH_X_AXIS,
                                                                   Constants.IOT_CONNECTED_CUP_COFFEE_LEVEL_ID,
                                                                   Constants.IOT_CONNECTED_CUP_COFFEE_LEVEL_X_AXIS));
    }

    @Test(description = "Test the whether the Coffee Level graph legend is present.",
          groups = {"iot.sample.coffee"},
          dependsOnGroups = {"sample.temp"})
    public void coffeeLevelGraphLegendTest() throws IOException {
        Assert.assertTrue(connectedCupDeviceViewPage.graphLegendName(Constants.IOT_CONNECTED_CUP_COFFEE_LEVEL_ID,
                                                                     Constants.IOT_CONNECTED_CUP_COFFEE_LEVEL_LEGEND));
    }

    @Test(description = "Test the whether the Coffee Level graph path is visible.",
          groups = {"iot.sample.coffee"},
          dependsOnGroups = {"sample.temp"})
    public void coffeeLevelGraphPathTest() {
        Assert.assertTrue(connectedCupDeviceViewPage.checkGraphPath(Constants.IOT_CONNECTED_CUP_COFFEE_LEVEL_GRAPH_ID));
    }

    @Test(description = "Test the whether the Coffee Level graph gets values.",
          groups = {"iot.sample.coffee"},
          dependsOnGroups = {"sample.temp"})
    public void coffeeLevelGraphDataPublisherTest() {
        Assert.assertTrue(connectedCupDeviceViewPage.checkGraphValues(Constants.IOT_CONNECTED_CUP_COFFEE_LEVEL_GRAPH_ID,
                                                                      Constants.IOT_CONNECTED_CUP_LEVEl));
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        driverServer.quit();
        driverDevice.quit();
    }
}
