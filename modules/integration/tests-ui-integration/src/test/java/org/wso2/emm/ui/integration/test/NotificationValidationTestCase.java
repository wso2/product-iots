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
import org.wso2.iot.integration.ui.pages.Notification.NotificationView;

public class NotificationValidationTestCase extends EMMIntegrationUiBaseTestCase {
    private static String notificationValue = "0";
    private WebDriver driver;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        LoginUtils.login(driver, automationContext, getWebAppURL());
    }

//    @Test(description = "verify notification count in emm")
//    public void viewNotificationCount() throws Exception {
//        driver.get(getWebAppURL() + Constants.MDM_HOME_URL);
//        HomePage homePage = new HomePage(driver);
//        homePage.checkNotificationCount(notificationValue);
//    }

    @Test(description = "verify notification visibility in emm")
    public void verifyNotificationView() throws Exception {
        driver.get(getWebAppURL() + Constants.MDM_NOTIFICATION_URL);
        NotificationView notificationView = new NotificationView(driver);
        notificationView.viewNotification();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }
}
