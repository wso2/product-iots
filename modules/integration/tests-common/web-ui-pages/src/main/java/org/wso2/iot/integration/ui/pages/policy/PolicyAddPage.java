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

public class PolicyAddPage {

    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public PolicyAddPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        WebDriverWait webDriverWait = new WebDriverWait(driver, UIUtils.webDriverTimeOut);

        if (!webDriverWait
                .until(ExpectedConditions.titleContains(uiElementMapper.getElement("cdmf.policy.add.page")))) {
            throw new IllegalStateException("This is not the Add Group page");
        }
    }

    /**
     * Adds an android policy.
     *
     * @param name name of the policy
     * @param description description of the policy
     * @return {@link PolicyPage}
     * @throws IOException If error occurred when adding a policy
     */
    public PolicyPage addAndroidSamplePolicy(String name, String description) throws IOException {
        WebElement androidPolicyCreateButton = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.policy.add.android.policy.xpath")));
        androidPolicyCreateButton.click();
        WebElement androidPasscodePolicy = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.policy.android.passcode.policy.button")));
        androidPasscodePolicy.click();
        WebElement androidPasscodePolicyEnableButton = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.policy.android.passcode.policy.enable.button")));
        androidPasscodePolicyEnableButton.click();
        WebElement androidPolicyConfigContinueButton = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.policy.android.policy.configuration.continue.button")));
        androidPolicyConfigContinueButton.click();
        WebElement androidPolicyAssignGroupsContinueButton = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.policy.android.assignGroups.continue.button")));
        androidPolicyAssignGroupsContinueButton.click();
        WebElement androidPolicyNameInput = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.policy.android.name.input")));
        WebElement androidPolicyDescriptionInput = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.policy.android.description.input")));
        WebElement androidPolicyPublishButton = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.policy.android.publish.button")));

        androidPolicyNameInput.sendKeys(name);
        androidPolicyDescriptionInput.sendKeys(description);
        androidPolicyPublishButton.click();

        return new PolicyPage(driver);
    }
}
