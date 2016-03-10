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

public class ConnectedCupDeviceViewPage {
    private HashMap<String, Graph> graphMap = new HashMap<>();
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

    public VirtualSampleViewPage gotoDevice() throws IOException {
        WebDriverWait wait = new WebDriverWait(driverServer, UIUtils.webDriverTime);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                uiElementMapper.getElement("iot.sample.connectedcup.gotodevice.xpath"))));
        String link = driverServer.findElement(By.xpath(
                uiElementMapper.getElement("iot.sample.connectedcup.gotodevice.xpath"))).getAttribute("href");
        driverDevice.get(link);
        return new VirtualSampleViewPage(driverDevice);
    }

    public String getDeviceLink() {
        WebDriverWait wait = new WebDriverWait(driverServer, UIUtils.webDriverTime);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                uiElementMapper.getElement("iot.sample.connectedcup.gotodevice.xpath"))));
        return driverServer.findElement(By.xpath(
                uiElementMapper.getElement("iot.sample.connectedcup.gotodevice.xpath"))).getAttribute("href");
    }

    public boolean isGraphsAvailable(int count) throws IOException {
        return handler.getGraphCount() == count;
    }

    public boolean graphAxisName(String axis, String graphId, String axisName) throws IOException {
        log.info(graphMap.toString());

        if (graphMap.size() != 0) {
            if (axis.toLowerCase().contains("x")) {
                return graphMap.get(graphId).getxAxis().contains(axisName);
            } else {
                return graphMap.get(graphId).getyAxis().contains(axisName);
            }
        }
        return false;
    }

    public boolean graphLegendName(String graphId, String legend) throws IOException {
        log.info(graphMap.toString());
        if (graphMap.size() != 0) {
            try {
                return graphMap.get(graphId).getLegend().contains(legend);
            } catch (NullPointerException e) {
                log.info("Null Pointer" + e.getMessage());
            }
        }
        return false;
    }

    public boolean checkGraphPath(String graphId) {
        WebElement graph = handler.getGraphById(graphId);
        if (graph != null) {
            return handler.isPathAvailable(graph);
        } else {
            log.error(String.format("Graph for Id %s is not present......", graphId));
            return false;
        }
    }

    public boolean checkGraphValues(String graphId, String value) {
        WebElement graph = handler.getGraphById(graphId);
        if (graph != null) {
            return handler.isPathGetValues(graph, value);
        } else {
            log.error(String.format("Graph for Id %s is not present......", graphId));
            return false;
        }
    }
}
