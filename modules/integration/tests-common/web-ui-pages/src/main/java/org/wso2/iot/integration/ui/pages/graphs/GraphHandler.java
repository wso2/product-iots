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
package org.wso2.iot.integration.ui.pages.graphs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.iot.integration.ui.pages.UIUtils;
import org.wso2.iot.integration.ui.pages.UIElementMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Graphs should also be tested in UI tests. So, this class contains methods to extract various properties of graphs..
 * Such as,
 * - Legend
 * - Tool Tips
 * - X, Y axis properties
 * - get the graph path values etc.
 * Works with IOT server device view graphs and analytics graphs.
 */
public class GraphHandler {
    private UIElementMapper uiElementMapper;
    private Log log = LogFactory.getLog(GraphHandler.class);
    private WebElement graphDiv;
    private WebDriver driver;
    private List<WebElement> graphs;

    public GraphHandler(WebDriver driver) throws IOException {
        this.driver = driver;
        uiElementMapper = UIElementMapper.getInstance();
        graphDiv = driver.findElement(By.xpath(uiElementMapper.getElement("iot.stats.graph.container.xpath")));
    }

    /**
     * This method is to get all the elements of graphs and store in a Hash map.
     * This simplifies iterating through the DOM every time finding for an element when having multiple graphs.
     */
    public HashMap<String, Graph> getGraphMap() {
        HashMap<String, Graph> graphMap = new HashMap<>();
        WebDriverWait wait = new WebDriverWait(driver, UIUtils.webDriverTimeOut);
        wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.stat.graph.wrapper.xpath")))));
        List<WebElement> graphs = driver.findElements(By.xpath(
                uiElementMapper.getElement("iot.stat.graph.wrapper.xpath")));
        for (WebElement e : graphs) {
            Graph g = new Graph();
            String key = e.getAttribute("id").split("-")[1];
            g.setGraphId(key.toLowerCase().replace(" ", ""));
            String xAxis = e.findElement(By.xpath(uiElementMapper.getElement("ot.stat.graph.xAxis.xpath"))).getText();
            g.setxAxis(xAxis);
            String yAxis = e.findElement(By.xpath("//*[contains(@id, \"y_axis-" + key + "\")]")).getText();
            g.setyAxis(yAxis);
            String legend = e.findElement(By.xpath("//*[contains(@id, \"legend-" + key + "\")]")).findElement(
                    By.tagName("span")).getText();
            g.setLegend(legend);
            graphMap.put(key, g);
            log.info(g.toString());
        }
        return graphMap;
    }

    /**
     * Get the number of graphs in the UI
     */
    public int getGraphCount() {
        try {
            graphs = this.graphDiv.findElements(By.xpath("//*[contains(@class, \"chartWrapper\")]"));
        } catch (NoSuchElementException e) {
            log.info("Graph element not found");
        }
        return graphs.size();
    }

    /**
     * Get the Web Element corresponds to the given graph id
     *
     * @param graphId : the id of the graph.
     * @return Web Element of the graph
     */
    public WebElement getGraphById(String graphId) {
        graphs = this.graphDiv.findElements(By.xpath(uiElementMapper.getElement("iot.stat.graph.wrapper.xpath")));
        for (int i = 0; i < graphs.size() && graphs.size() > 0; i++) {
            WebElement element = graphs.get(i);
            if (element.getAttribute("id").toLowerCase().replace(" ", "").contains(graphId.toLowerCase())) {
                log.info(">>>>>>>>>>>>>>>>>>> Graph for id: " + graphId + " is present ");
                return element;
            }
        }
        return null;
    }

    /**
     * Check the graph path is visible or not.
     *
     * @param graph : web element of the graph
     * @return : True if the path is visible. False otherwise
     */
    public boolean isPathAvailable(WebElement graph) {
        try {
            WebElement graphContainer = getGraph(graph, uiElementMapper.getElement("iot.stat.graph.class.name"));
            return graphContainer != null && graphContainer.findElement(By.tagName("path")).isDisplayed();
        } catch (NoSuchElementException e) {
            log.error("No element found. " + e.getMessage());
            return false;
        }
    }

    /**
     * Check the path of the graph draws the values pushed by the device. As it takes some time, explicit wait of 10
     * seconds is added.
     *
     * @param graph : Web element of the graph
     * @param val   : Value which pushed by the device
     * @return : True if the path is drawn to the values. False otherwise.
     */
    public boolean isPathGetValues(WebElement graph, String val) {
        WebElement graphContainer = getGraph(graph, uiElementMapper.getElement("iot.stat.graph.class.name"));
        driver.manage().timeouts().implicitlyWait(UIUtils.webDriverTimeOut, TimeUnit.SECONDS);
        String[] values;
        if (graphContainer != null) {
            values = graphContainer.findElement(By.tagName("path")).getAttribute("d").split(",");
            for (String value : values) {
                if (value.contains(val)) {
                    log.info("Graph values : " + value);
                    return true;
                }
            }
        }
        return false;
    }

    private WebElement getGraph(WebElement graph, String className) {
        List<WebElement> elements = graph.findElements(By.tagName("div"));
        for (WebElement e : elements) {
            if (e.getAttribute("class").contains(className)) {
                return e;
            }
        }
        return null;
    }
}
