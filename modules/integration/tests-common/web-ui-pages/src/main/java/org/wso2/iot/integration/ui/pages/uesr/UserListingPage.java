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
package org.wso2.iot.integration.ui.pages.uesr;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.wso2.iot.integration.ui.pages.UIElementMapper;

import java.io.IOException;

/**
 * Class to represent the user listing page.
 */
public class UserListingPage {
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public UserListingPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
    }

    /**
     * @return After deleting a user, returns back to the user listing page.
     */
    //Don't use generic exceptions.
    public UserListingPage deleteUser() throws IOException, InterruptedException {
        WebElement deleteBtn = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.admin.deleteUser.btn.xpath")));
        if (deleteBtn != null) {
            deleteBtn.click();
        } else {
            return new UserListingPage(driver);
        }
        driver.findElement(By.xpath(uiElementMapper.getElement("iot.admin.deleteUser.yes.link.xpath"))).click();
        Thread.sleep(1000);
        driver.findElement(By.xpath(uiElementMapper.getElement("iot.admin.deleteUser.success.link.xpath"))).click();

        return new UserListingPage(driver);
    }
}
