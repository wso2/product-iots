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
package org.wso2.iot.integration.ui.pages.Notification;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.wso2.iot.integration.ui.pages.UIElementMapper;

import java.io.IOException;

public class NotificationView {
    private static final Log log = LogFactory.getLog(NotificationView.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public NotificationView(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.
        if (!(driver.getCurrentUrl().contains("notification-listing"))) {
            throw new IllegalStateException("This is not the notification-listing page");
        }
    }

    public void viewNotification() {
        WebElement unReadNotificationTab = driver.findElement(By.id(uiElementMapper.
                getElement("emm.notification.unread.identifier")));
        WebElement notificationTab = driver.findElement(By.id(uiElementMapper.
                getElement("emm.notification.all.identifier")));

        if (!unReadNotificationTab.isDisplayed()) {
            throw new IllegalStateException("Notification View must be having two tabs");
        }
        if (!notificationTab.isDisplayed()) {
            throw new IllegalStateException("Notification View must be having two tabs");
        }
    }
}
