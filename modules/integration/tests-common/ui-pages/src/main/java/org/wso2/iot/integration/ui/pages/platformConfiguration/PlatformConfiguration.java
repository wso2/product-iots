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

package org.wso2.iot.integration.ui.pages.platformConfiguration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.wso2.iot.integration.ui.pages.UIElementMapper;

import java.io.IOException;

public class PlatformConfiguration {

    private static final Log log = LogFactory.getLog(PlatformConfiguration.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public PlatformConfiguration(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.
        if (!(driver.getCurrentUrl().contains("platform-configuration"))) {
            throw new IllegalStateException("This is not the platform-configuration page");
        }
    }

    public void changeGeneralConfig(String value) {
        WebElement generalConfigButton = driver.findElement(By.xpath((uiElementMapper.getElement("emm.configuration.general.tab.identifier"))));
        WebElement inputGeneralConfig = driver.findElement(By.xpath((uiElementMapper.getElement("emm.configuration.general.input.monitoringFr.identifier"))));
        WebElement buttonSaveConfig = driver.findElement(By.xpath((uiElementMapper.getElement("emm.configuration.general.button.save.identifier"))));

        generalConfigButton.click();
        inputGeneralConfig.sendKeys(value);
        buttonSaveConfig.click();
    }

    public void changeAndroidConfig(String value) {
        WebElement androidConfigButton = driver.findElement(By.xpath((uiElementMapper.getElement("emm.configuration.android.tab.identifier"))));
        WebElement inputAndroidConfig = driver.findElement(By.xpath((uiElementMapper.getElement("emm.configuration.android.input.identifier"))));
        WebElement buttonSaveConfig = driver.findElement(By.xpath((uiElementMapper.getElement("emm.configuration.android.button.save.identifier"))));

        androidConfigButton.click();
        inputAndroidConfig.sendKeys(value);
        buttonSaveConfig.click();

    }

    public void changeWindowsConfig(String value) {
        WebElement windowsConfigButton = driver.findElement(By.xpath((uiElementMapper.getElement("emm.configuration.widows.tab.identifier"))));
        WebElement inputWindowsConfig = driver.findElement(By.xpath((uiElementMapper.getElement("emm.configuration.windows.input.identifier"))));
        WebElement buttonSaveConfig = driver.findElement(By.xpath((uiElementMapper.getElement("emm.configuration.windows.button.save.identifier"))));

        windowsConfigButton.click();
        inputWindowsConfig.sendKeys(value);
        buttonSaveConfig.click();

    }
}
