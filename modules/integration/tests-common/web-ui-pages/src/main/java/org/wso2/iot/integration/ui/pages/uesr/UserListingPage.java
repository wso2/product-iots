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
package org.wso2.iot.integration.ui.pages.uesr;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.iot.integration.ui.pages.UIUtils;
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
     * Performs the delete user action.
     * @return After deleting a user, returns back to the user listing page.
     */
    public UserListingPage deleteUser() throws IOException, InterruptedException {
        WebElement deleteBtn = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.admin.deleteUser.btn.xpath")));

        WebDriverWait wait = new WebDriverWait(driver, UIUtils.webDriverTimeOut);
        wait.until(ExpectedConditions.visibilityOf(deleteBtn));
        deleteBtn.click();

        WebElement deleteConfirmationBtn = driver.findElement(
                By.xpath(uiElementMapper.getElement("iot.admin.deleteUser.yes.link.xpath")));
        wait.until(ExpectedConditions.visibilityOf(deleteConfirmationBtn));
        deleteConfirmationBtn.click();

        Thread.sleep(UIUtils.threadTimeout);
        WebElement deleteSuccessBtn = driver.findElement(
                By.xpath(uiElementMapper.getElement("iot.admin.deleteUser.success.link.xpath")));
        deleteSuccessBtn.click();

        return new UserListingPage(driver);
    }
}
