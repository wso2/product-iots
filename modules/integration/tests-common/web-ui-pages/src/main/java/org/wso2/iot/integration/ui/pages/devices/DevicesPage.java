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
package org.wso2.iot.integration.ui.pages.devices;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.iot.integration.ui.pages.UIUtils;
import org.wso2.iot.integration.ui.pages.UIElementMapper;
import org.wso2.iot.integration.ui.pages.samples.ConnectedCupDeviceViewPage;

import java.io.IOException;
import java.util.List;

/**
 * Class to represent the IOT devices page. In this page, all the enrolled devices are listed.
 * User can perform following functions on the enrolled devices.
 * 1. View the device.
 * 2. View device analytics.
 * 3. Edit the device.
 * 4. Delete the device.
 */
public class DevicesPage {

    Log log = LogFactory.getLog(DevicesPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public DevicesPage(WebDriver driver) throws IOException {

        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        WebDriverWait webDriverWait = new WebDriverWait(driver, UIUtils.webDriverTimeOut);
        if (!webDriverWait.until(ExpectedConditions.titleContains("Device Management | IoT Server"))) {
            throw new IllegalStateException("This is not the Device Management page");
        }
    }

    /**
     * This method checks whether the given device is enrolled and visible in the UI of the IOT server.
     * @param name : The name of the device to be checked.
     * @return : True if the device is enrolled and visible. False otherwise.
     */
    public boolean isDeviceEnrolled(String name) {
        List<WebElement> deviceNames = driver.findElements(By.tagName("h4"));
        if (!deviceNames.isEmpty()) {
            for (WebElement deviceName : deviceNames) {
                if (deviceName.getText().contains(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method performs the navigation to the Device view of the given device.
     * Here the navigation happens to the Connected cup device.
     * @param deviceName : Name of the device.
     * @return : The corresponding device view page. Null if not visible.
     */
    public ConnectedCupDeviceViewPage viewDevice(String deviceName) throws IOException {
        WebElement deviceTable = driver.findElement(By.xpath(uiElementMapper.getElement("iot.devices.table.xpath")));
        List<WebElement> data = deviceTable.findElements(By.cssSelector("a"));
        for (WebElement e : data) {
            String s = getLink(e, "/device/connectedcup?id=");
            if (s != null) {
                driver.get(s);
                return new ConnectedCupDeviceViewPage(driver, deviceName);
            }
        }
        return null;
    }

    /**
     * Method to extract the URL, which matches with the given parameters from an HTML element.
     * @param element : WebElement, from which the URL should be extracted.
     * @param lookupText : The parameters to be contained in the URL.
     * @return : The URL String found. NULL if the URL is not found.
     */
    private String getLink(WebElement element, String... lookupText) {
        String link = element.getAttribute("href");
        boolean check = true;
        for (String s : lookupText) {
            if (!link.contains(s)) {
                check = false;
            }
        }
        return check ? link : null;
    }

}
