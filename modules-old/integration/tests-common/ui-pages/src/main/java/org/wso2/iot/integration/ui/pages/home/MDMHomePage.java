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

package org.wso2.iot.integration.ui.pages.home;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.wso2.iot.integration.ui.pages.UIElementMapper;

import java.io.IOException;

public class MDMHomePage {

    private static final Log log = LogFactory.getLog(MDMHomePage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public MDMHomePage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.
        if (!driver.findElement(By.xpath(uiElementMapper.getElement("emm.dashboard.device.div.xpath"))).getText()
                .contains("DEVICES")) {
            throw new IllegalStateException("This is not the home page");
        }
    }

    //        public MDMLoginPage logout() throws IOException {
    //            driver.findElement(By.xpath("/html/body/div[3]/header/div/div[2]/div/div/div[3]/a")).click();
    //            return new MDMLoginPage(driver);
    //        }
}
