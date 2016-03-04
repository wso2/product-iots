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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.iot.integration.ui.pages.UIConstants;
import org.wso2.iot.integration.ui.pages.UIElementMapper;

import java.io.IOException;

public class ConnectedCupDeviceViewPage {
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public ConnectedCupDeviceViewPage(WebDriver driver, String name) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();

        if (!driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.sample.connectedcup.view.page.title"))).getText().
                contains(name)) {
            throw new IllegalStateException("This is not the Connected cup device type view page");
        }
    }

    public VirtualSampleViewPage gotoDevice() throws IOException {
        WebDriverWait wait = new WebDriverWait(driver, UIConstants.webDriverTime);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                uiElementMapper.getElement("iot.sample.connectedcup.gotodevice.xpath"))));
        driver.findElement(By.xpath(uiElementMapper.getElement("iot.sample.connectedcup.gotodevice.xpath"))).click();
        return new VirtualSampleViewPage(driver);
    }

}
