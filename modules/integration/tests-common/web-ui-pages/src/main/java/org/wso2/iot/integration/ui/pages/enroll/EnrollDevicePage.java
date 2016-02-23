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
package org.wso2.iot.integration.ui.pages.enroll;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.WebDriver;
import org.wso2.iot.integration.ui.pages.UIElementMapper;

import java.io.IOException;

/**
 * Device Enrollment page for new user
 */
public class EnrollDevicePage {
    private static final Log log = LogFactory.getLog(EnrollDevicePage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public EnrollDevicePage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
    }

}
