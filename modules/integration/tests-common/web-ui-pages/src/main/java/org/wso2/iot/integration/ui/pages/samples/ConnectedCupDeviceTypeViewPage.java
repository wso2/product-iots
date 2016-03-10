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


import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.wso2.iot.integration.ui.pages.UIUtils;
import org.wso2.iot.integration.ui.pages.UIElementMapper;

import java.io.IOException;

/**
 * This class represents the Device Type View page of the Connected cup device, which has the functionality for
 * download the device agent.
 */
public class ConnectedCupDeviceTypeViewPage {
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public ConnectedCupDeviceTypeViewPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        if (!driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.sample.connectedcup.page.title"))).getText().
                contains("Connected Cup")) {
            throw new IllegalStateException("This is not the Connected cup device type view page");
        }
    }

    /**
     * This method verifies that the pop up modal for inserting a name for device is present.
     */
    public boolean isPopUpPresent() throws InterruptedException {
        WebElement createInstanceBtn = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.sample.connectedcup.createInstanceBtn.xpath")));
        createInstanceBtn.click();

        Thread.sleep(UIUtils.threadTimeout);

        return driver.findElement(By.xpath(uiElementMapper.getElement("iot.sample.modal.popup.xpath"))).isDisplayed();
    }

    /**
     * This method performs the enrolment process of the connected cup device. After clicking the Download button in
     * the popup, user should be navigated to the device type view page again.
     * This method checks the navigation and return true if navigation is correct.
     * @param name : Name for the device.
     */
    public boolean enrollDevice(String name) {

        WebElement nameField = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.sample.connectedcup.createInstance.nameField.xpath")));
        WebElement createButton = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.sample.connectedcup.createInstance.downloadBtn.xpath")));
        nameField.sendKeys(name);
        createButton.click();
        return driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.sample.connectedcup.page.title"))).getText().contains("Connected Cup");
    }
}
