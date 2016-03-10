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
import org.wso2.iot.integration.ui.pages.UIConstants;
import org.wso2.iot.integration.ui.pages.UIElementMapper;
import org.wso2.iot.integration.ui.pages.samples.ConnectedCupDeviceViewPage;

import java.io.IOException;
import java.util.List;

public class DevicesPage {
    Log log = LogFactory.getLog(DevicesPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public DevicesPage(WebDriver driver) throws IOException {

        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        WebDriverWait webDriverWait = new WebDriverWait(driver, UIConstants.webDriverTimeOut);
        if (!webDriverWait.until(ExpectedConditions.titleContains("Device Management | IoT Server"))) {
            throw new IllegalStateException("This is not the Device Management page");
        }
    }

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

    private String getLink(WebElement element, String... lookupText) {
        String link = element.getAttribute("href");
        log.info("Link -----------------------> " + link);
        boolean check = true;
        for (String s : lookupText) {
            if (!link.contains(s)) {
                check = false;
            }
        }
        return check ? link : null;
    }
}
