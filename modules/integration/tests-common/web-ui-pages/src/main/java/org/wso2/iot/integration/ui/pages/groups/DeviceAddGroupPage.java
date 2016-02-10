/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.iot.integration.ui.pages.groups;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.iot.integration.ui.pages.UIElementMapper;

import java.io.IOException;

/**
 * Add group page of iot server
 */
public class DeviceAddGroupPage {

    private static final Log log = LogFactory.getLog(DeviceAddGroupPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public DeviceAddGroupPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        WebDriverWait webDriverWait = new WebDriverWait(driver, 10);

        if (!driver.findElement(By.xpath
                    (uiElementMapper.getElement("iot.device.group.addNewGroup.xpath"))).getText().contains("ADD NEW GROUP")){
            throw new IllegalStateException("This is not the Add Group page");
        }
    }


    public DeviceGroupsPage addNewGroup (String name, String description) throws Exception{

        WebElement nameField = driver.findElement(By.id(
                uiElementMapper.getElement("iot.device.group.addGroupForm.name.id")));
        WebElement descriptionField = driver.findElement(By.id(
                uiElementMapper.getElement("iot.device.group.addGroupForm.description.id")));
        WebElement addGroupButton = driver.findElement(By.id(
                uiElementMapper.getElement("iot.device.group.addGroupForm.addButton.id")));

        nameField.sendKeys(name);
        descriptionField.sendKeys(description);

        addGroupButton.click();

        return new DeviceGroupsPage(driver);

    }

    public String submitEmptyForm() {
        WebElement nameField = driver.findElement(By.id(
                uiElementMapper.getElement("iot.device.group.addGroupForm.name.id")));
        WebElement descriptionField = driver.findElement(By.id(
                uiElementMapper.getElement("iot.device.group.addGroupForm.description.id")));
        WebElement addGroupButton = driver.findElement(By.id(
                uiElementMapper.getElement("iot.device.group.addGroupForm.addButton.id")));

        nameField.sendKeys("");
        descriptionField.sendKeys("");

        addGroupButton.click();

        return driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.device.groups.add.emptyfrom.error"))).getText();
    }

}
