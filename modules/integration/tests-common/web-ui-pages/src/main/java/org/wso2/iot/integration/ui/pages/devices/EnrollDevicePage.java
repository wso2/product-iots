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
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.iot.integration.ui.pages.UIConstants;
import org.wso2.iot.integration.ui.pages.UIElementMapper;
import org.wso2.iot.integration.ui.pages.samples.ConnectedCupDeviceTypeViewPage;

import java.io.IOException;

/**
 * Device Enrollment page for new user
 */
public class EnrollDevicePage {
    private WebDriver driver;
    private UIElementMapper uiElementMapper;
    private Log log = LogFactory.getLog(EnrollDevicePage.class);

    public EnrollDevicePage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();

        WebDriverWait webDriverWait = new WebDriverWait(driver, UIConstants.webDriverTimeOut);
        if (!webDriverWait.until(ExpectedConditions.titleContains("Device Types | IoT Server"))) {
            throw new IllegalStateException("This is not the Device Enrollment page");
        }
    }

    public boolean isInstalled() {
        boolean check = UIConstants.isElementPresent(log, driver, By.xpath(
                uiElementMapper.getElement("iot.sample.connectedcup.xpath")));
        if (check) {
            WebElement deviceDiv = driver.findElement(By.xpath(
                    uiElementMapper.getElement("iot.sample.connectedcup.xpath")));
            WebElement tryBtn = deviceDiv.findElement(By.tagName("button"));
            return tryBtn.isDisplayed();
        }
        return false;
    }

    public ConnectedCupDeviceTypeViewPage gotoConnectedCupDeviceTypeViewPage() throws IOException {
        boolean check = UIConstants.isElementPresent(log, driver,By.xpath(
                uiElementMapper.getElement("iot.sample.connectedcup.xpath")));
        if (check){
            WebElement deviceDiv = driver.findElement(By.xpath(
                    uiElementMapper.getElement("iot.sample.connectedcup.xpath")));
            WebElement tryBtn = deviceDiv.findElement(By.tagName("button"));
            tryBtn.click();
            return new ConnectedCupDeviceTypeViewPage(driver);
        } else {
            log.error("Element not found...........................");
            return null;
        }
    }

}
