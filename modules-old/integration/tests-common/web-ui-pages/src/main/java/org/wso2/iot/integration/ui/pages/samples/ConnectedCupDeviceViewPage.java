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
package org.wso2.iot.integration.ui.pages.samples;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.iot.integration.ui.pages.UIUtils;
import org.wso2.iot.integration.ui.pages.UIElementMapper;
import org.wso2.iot.integration.ui.pages.graphs.Graph;
import org.wso2.iot.integration.ui.pages.graphs.GraphHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This class represents the Connected cup device view page.
 * In this page, there are following elements.
 *      1. Device overview. (Whether the device is active or not)
 *      2. Device operations. (Operations which are performed on the device)
 *      3. Device Stats
 *
 * In this class, device operations and device stats are validated.
 */
public class ConnectedCupDeviceViewPage {

    private Map<String, Graph> graphMap = new HashMap<>();
    private Log log = LogFactory.getLog(ConnectedCupDeviceViewPage.class);
    private WebDriver driverServer;
    private WebDriver driverDevice;
    private UIElementMapper uiElementMapper;
    private GraphHandler handler;

    public ConnectedCupDeviceViewPage(WebDriver driver, String name) throws IOException {
        this.driverServer = driver;
        this.driverDevice = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        this.handler = new GraphHandler(driverServer);

        if (!driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.sample.connectedcup.view.page.title"))).getText().
                contains(name)) {
            throw new IllegalStateException("This is not the Connected cup device type view page");
        }
        handler = new GraphHandler(driverServer);
        graphMap = handler.getGraphMap();
    }

    /**
     * This method executes Connected cup sample web app.
     * @return : The Connected cup web page.
     */
    public ConnectedCupDeviceInterface gotoDevice() throws IOException {
        WebDriverWait wait = new WebDriverWait(driverServer, UIUtils.webDriverTime);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                uiElementMapper.getElement("iot.sample.connectedcup.gotodevice.xpath"))));
        String link = driverServer.findElement(By.xpath(
                uiElementMapper.getElement("iot.sample.connectedcup.gotodevice.xpath"))).getAttribute("href");
        driverDevice.get(link);
        return new ConnectedCupDeviceInterface(driverDevice);
    }

    /**
     * Gets the connected cup device web app URL.
     * @return : Link of the connected cup device web app.
     */
    public String getDeviceLink() {
        WebDriverWait wait = new WebDriverWait(driverServer, UIUtils.webDriverTime);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                uiElementMapper.getElement("iot.sample.connectedcup.gotodevice.xpath"))));
        return driverServer.findElement(By.xpath(
                uiElementMapper.getElement("iot.sample.connectedcup.gotodevice.xpath"))).getAttribute("href");
    }

    /**
     * This method checks whether there are expected number of graphs are available in the UI.
     * @param count : Number of graphs expected.
     * @return : True if there are count number of graphs. False otherwise.
     */
    public boolean isGraphsAvailable(int count) {
        return handler.getGraphCount() == count;
    }

    /**
     * Checks whether the selected graph axes represent the given values.
     * @param axis : Graph axis. X or Y
     * @param graphId : Id of the graph
     * @param axisName : Name which is expected to be displayed.
     * @return : True if given axis contains the expected title. False otherwise or, there are no graphs present.
     */
    public boolean graphAxisName(String axis, String graphId, String axisName) {
        if (graphMap.size() != 0) {
            if (axis.toLowerCase().contains("x")) {
                return graphMap.get(graphId).getxAxis().contains(axisName);
            } else {
                return graphMap.get(graphId).getyAxis().contains(axisName);
            }
        }
        log.error("There are no graphs found.");
        return false;
    }

    /**
     * Check the legend of the selected graph have the expected title.
     * @param graphId : Id of the graph.
     * @param legend : Expected value to be displayed in the legend.
     * @return : True if legend contains the expected value. False otherwise or there are no graphs present.
     */
    public boolean graphLegendName(String graphId, String legend) {
        if (graphMap.size() != 0) {
            if (graphMap.get(graphId) != null){
                return graphMap.get(graphId).getLegend().contains(legend);
            }
            log.error(String.format("Graph for %s is not found.", graphId));
            return false;
        }
        log.error("There are no graphs found.");
        return false;
    }

    /**
     * Method to check the graph path is displayed in the UI for given graph.
     * @param graphId : Id of the graph.
     * @return : True of path is displayed. False otherwise or no graphs are present.
     */
    public boolean checkGraphPath(String graphId) {
        WebElement graph = handler.getGraphById(graphId);
        if (graph != null) {
            return handler.isPathAvailable(graph);
        } else {
            log.error(String.format("Graph for Id %s is not present.", graphId));
            return false;
        }
    }

    /**
     * Method to verify that the graphs get readings from the device.
     * @param graphId : Id of the graph.
     * @param value : Value which is expected to be displayed in the graph.
     * @return : True if the value is displayed in the graph. False otherwise or graph is null.
     */
    public boolean checkGraphValues(String graphId, String value) {
        WebElement graph = handler.getGraphById(graphId);
        driverServer.manage().timeouts().implicitlyWait(UIUtils.webDriverTime, TimeUnit.SECONDS);
        if (graph != null) {
            return handler.isPathGetValues(graph, value);
        } else {
            log.error(String.format("Graph for Id %s is not present.", graphId));
            return false;
        }
    }

}
