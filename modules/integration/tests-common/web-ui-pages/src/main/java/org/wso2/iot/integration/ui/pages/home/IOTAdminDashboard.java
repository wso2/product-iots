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
package org.wso2.iot.integration.ui.pages.home;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.wso2.iot.integration.ui.pages.UIElementMapper;
import org.wso2.iot.integration.ui.pages.devices.DevicesPage;
import org.wso2.iot.integration.ui.pages.devices.EnrollDevicePage;
import org.wso2.iot.integration.ui.pages.groups.DeviceAddGroupPage;
import org.wso2.iot.integration.ui.pages.groups.DeviceGroupsPage;
import org.wso2.iot.integration.ui.pages.login.LoginPage;
import org.wso2.iot.integration.ui.pages.uesr.AddUserPage;
import org.wso2.iot.integration.ui.pages.uesr.UserListingPage;

import java.io.IOException;

/**
 * This class represents the Admin Dashboard of the IOT server.
 * Server dashboard has following sections and functions.
 * 1. User Management
 * - View users.
 * - Add a new user
 * 2. Group Management.
 * - View Groups.
 * - Add a new group.
 * 3. Device Management.
 * - View enrolled devices.
 * - Enroll a device.
 * 3. Policy Management.
 * - View Policies.
 * - Create a policy.
 */
public class IOTAdminDashboard {

    private static final Log log = LogFactory.getLog(IOTHomePage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public IOTAdminDashboard(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        log.info(driver.findElement(By.xpath(uiElementMapper.getElement("iot.admin.dashboard.title.xpath"))).
                getText());
        // Check that we're on the right page.
        if (!driver.findElement(By.xpath(uiElementMapper.getElement("iot.admin.dashboard.title.xpath"))).
                getText().contains("DASHBOARD")) {
            throw new IllegalStateException("This is not the home page");
        }
    }

    /**
     * Performs the logout action.
     * @return : The IOT login page
     */
    public LoginPage logout() throws IOException {
        driver.findElement(By.xpath(uiElementMapper.getElement("iot.user.registered.name"))).click();
        WebElement logout = driver.findElement(By.xpath(uiElementMapper.getElement("iot.user.logout.link.xpath")));
        logout.click();
        return new LoginPage(driver);
    }

    /**
     * Performs the navigation to Add device group page.
     * @return : Add Device Group page.
     */
    public DeviceAddGroupPage addGroup() throws IOException {
        driver.findElement(By.xpath(uiElementMapper.getElement("iot.device.group.addButton.xpath"))).click();
        return new DeviceAddGroupPage(driver);
    }

    /**
     * Performs the navigation to Group listing page.
     * @return : Groups page.
     */
    public DeviceGroupsPage viewGroups() throws IOException {
        driver.findElement(By.xpath(uiElementMapper.getElement("iot.device.group.viewButton.xpath"))).click();
        return new DeviceGroupsPage(driver);
    }

    /**
     * Navigates to the Add User page.
     * @return : Add user page.
     */
    public AddUserPage addUser() throws IOException {
        driver.findElement(By.xpath(uiElementMapper.getElement("iot.admin.user.addButton.xpath"))).click();
        return new AddUserPage(driver);
    }

    /**
     * Navigates to the User Listing page.
     * @return : User Listing page.
     */
    public UserListingPage viewUser() throws IOException {
        driver.findElement(By.xpath(uiElementMapper.getElement("iot.admin.user.viewButton.xpath"))).click();
        return new UserListingPage(driver);
    }

    /**
     * Navigates to the New device enrollment page.
     * @return : Enroll Device page.
     */
    public EnrollDevicePage enrollNewDevice() throws IOException {
        driver.findElement(By.xpath(uiElementMapper.getElement("iot.admin.device.addBtn.xpath"))).click();
        return new EnrollDevicePage(driver);
    }

    /**
     * Navigates to the Devices listing page.
     * @return : devices listing page.
     */
    public DevicesPage viewDevices() throws IOException {
        driver.findElement(By.xpath(uiElementMapper.getElement("iot.admin.device.viewBtn.xpath"))).click();
        return new DevicesPage(driver);
    }

    //ToDo : Need to add device and policy methods
}
