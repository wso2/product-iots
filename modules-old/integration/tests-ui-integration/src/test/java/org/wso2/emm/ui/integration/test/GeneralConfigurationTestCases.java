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
package org.wso2.emm.ui.integration.test;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.iot.integration.ui.pages.EMMIntegrationUiBaseTestCase;
import org.wso2.iot.integration.ui.pages.platformConfiguration.PlatformConfiguration;


public class GeneralConfigurationTestCases extends EMMIntegrationUiBaseTestCase {
    private static String configValue = "3600";
    private WebDriver driver;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        LoginUtils.login(driver, automationContext, getWebAppURL());
    }

    @Test(description = "verify updating general platform configuration in emm")
    public void testChangeGeneralConfig() throws Exception {
        driver.get(getWebAppURL() + Constants.MDM_PLATFORM_CONFIG_URL);
        PlatformConfiguration platformConfiguration = new PlatformConfiguration(driver);
        platformConfiguration.changeGeneralConfig(configValue);
    }

    @Test(description = "verify updating android platform configuration in emm")
    public void testChangeAndroidConfig() throws Exception {
        driver.get(getWebAppURL() + Constants.MDM_PLATFORM_CONFIG_URL);
        PlatformConfiguration platformConfiguration = new PlatformConfiguration(driver);
        platformConfiguration.changeAndroidConfig(configValue);
    }

    @Test(description = "verify updating windows platform configuration in emm")
    public void testChangeWindowsConfig() throws Exception {
        driver.get(getWebAppURL() + Constants.MDM_PLATFORM_CONFIG_URL);
        PlatformConfiguration platformConfiguration = new PlatformConfiguration(driver);
        platformConfiguration.changeWindowsConfig(configValue);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }
}
