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

package org.wso2.iot.integration.ui.pages.role;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.wso2.iot.integration.ui.pages.UIElementMapper;

import java.io.IOException;

public class AddRolePage {
    private static final Log log = LogFactory.getLog(RoleListPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public AddRolePage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        // Check that we're on the right page.
        if (!(driver.getCurrentUrl().contains("roles/add-role"))) {
            throw new IllegalStateException("This is not the add role page");
        }
    }

    public void addRole(String role) {
        WebElement roleName = driver.findElement(By.id(uiElementMapper.getElement("emm.roles.add.rolename.input")));
        roleName.sendKeys(role);
        WebElement addRoleButton = driver.findElement(By.id(uiElementMapper.getElement("emm.roles.add.role.button")));
        addRoleButton.click();
        String resultText = driver.findElement(By.id(uiElementMapper.getElement("emm.roles.add.role.created.msg.div")
        )).getText();
        if (!resultText.contains("ROLE WAS ADDED SUCCESSFULLY")) {
            throw new IllegalStateException("Role was not added");
        }
    }
}
