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
package org.wso2.iot.integration.ui.pages.home;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.iot.integration.ui.pages.UIConstants;
import org.wso2.iot.integration.ui.pages.UIElementMapper;
import org.wso2.iot.integration.ui.pages.enroll.EnrollDevicePage;
import org.wso2.iot.integration.ui.pages.groups.DeviceAddGroupPage;
import org.wso2.iot.integration.ui.pages.login.LoginPage;

import java.io.IOException;

/**
 * In IOT server, this page is the Devices page for users.
 * For admin this is the dashboard.
 */
public class IOTHomePage {
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public IOTHomePage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.
        WebDriverWait wait = new WebDriverWait(driver, UIConstants.webDriverTimeOut);
        if (!wait.until(ExpectedConditions.titleIs("Device Management | IoT Server"))) {
            throw new IllegalStateException("This is not the home page");
        }
    }

    public boolean checkUserName() {
        String name = driver.findElement(By.xpath(uiElementMapper.getElement("iot.user.registered.name"))).getText();
        return name.contains(uiElementMapper.getElement("iot.user.login.username"));
    }

    //To logout
    public LoginPage logout() throws IOException {
        driver.findElement(By.xpath(uiElementMapper.getElement("iot.user.registered.name"))).click();
        WebElement logout = driver.findElement(By.xpath(uiElementMapper.getElement("iot.user.logout.link.xpath")));
        logout.click();
        return new LoginPage(driver);
    }

    //To enroll devices as user
    public EnrollDevicePage enrollNewDevice() throws IOException {
        driver.findElement(By.xpath("iot.home.page.uuf-menu.xpath")).click();
        driver.findElement(By.xpath("iot.home.page.uuf-menu.devicemgt.xpath")).click();
        driver.findElement(By.xpath("iot.home.enrollDevice.xpath")).click();
        return new EnrollDevicePage(driver);
    }

    //To add new Device groups
    public void goToGroupManagementPage() {
        driver.findElement(By.xpath("iot.home.page.uuf-menu.xpath")).click();
        driver.findElement(By.xpath("iot.home.page.uuf-menu.groupmgt.xpath")).click();
    }

    public DeviceAddGroupPage addNewGroup() throws IOException {
        driver.findElement(By.xpath("iot.device.viewGroup.empty.addGroup.xpath")).click();
        return new DeviceAddGroupPage(driver);
    }

    //ToDo : To add policies

}
