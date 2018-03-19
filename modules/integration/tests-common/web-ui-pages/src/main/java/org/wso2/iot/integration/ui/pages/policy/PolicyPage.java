/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.iot.integration.ui.pages.policy;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.iot.integration.ui.pages.UIElementMapper;
import org.wso2.iot.integration.ui.pages.UIUtils;

import java.io.IOException;

public class PolicyPage {

    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public PolicyPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        WebDriverWait webDriverWait = new WebDriverWait(driver, UIUtils.webDriverTimeOut);

        if (!webDriverWait.until(ExpectedConditions.titleContains(uiElementMapper.getElement("cdmf.policy.page")))) {
            throw new IllegalStateException("This is not the Policy page");
        }
    }

    /**
     * Method to go to the add policy page, by clicking the Add policy button.
     * @return {@link PolicyAddPage} add policy page
     */
    public PolicyAddPage addNewPolicy() throws IOException {
        WebElement addPolicyButton = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.policy.add.button.xpath")));
        addPolicyButton.click();
        return new PolicyAddPage(driver);
    }

    /**
     * Method to go to the view policy page, by clicking a policy
     * @return {@link PolicyViewPage} view policy page
     * @throws IOException If unable to navigate to view policy page
     */
    public PolicyViewPage viewPolicy() throws IOException {
        WebElement policyViewIcon = driver.findElement(By.xpath(uiElementMapper.getElement("iot.policy.view.icon")));
        policyViewIcon.click();
        return new PolicyViewPage(driver);
    }
}
