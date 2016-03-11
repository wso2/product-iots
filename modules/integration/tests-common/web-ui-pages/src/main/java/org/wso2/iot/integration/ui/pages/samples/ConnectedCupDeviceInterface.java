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
package org.wso2.iot.integration.ui.pages.samples;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.iot.integration.ui.pages.UIUtils;
import org.wso2.iot.integration.ui.pages.UIElementMapper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Class to represent the Connected cup device interface.
 * This device is a virtual device, which allows users to change Temperature and Level values and put an order.
 */
public class ConnectedCupDeviceInterface {
    private Log log = LogFactory.getLog(ConnectedCupDeviceInterface.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public ConnectedCupDeviceInterface(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        WebDriverWait webDriverWait = new WebDriverWait(driver, UIUtils.webDriverTimeOut);
        if (!webDriverWait.until(ExpectedConditions.titleContains("Connected Coffee Cup"))) {
            throw new IllegalStateException("This is not the Connected cup device page");
        }
    }

    /**
     * Method to perform the order coffee functionality.
     * @return : True if the element is present and action is performed. False otherwise.
     */
    public boolean orderCoffee() {
        if (UIUtils.isElementPresent(log, driver, By.xpath(
                uiElementMapper.getElement("iot.sample.orderCoffee.xpath")))) {
            WebElement orderBtn = driver.findElement(By.xpath(
                    uiElementMapper.getElement("iot.sample.orderCoffee.xpath")));
            orderBtn.click();
            return true;
        }
        return false;
    }

    /**
     * Method to change the temperature level.
     * @param temp : The value to be set.
     * @return : True if the element is present and value is set. False otherwise.
     */
    public boolean changeTemperature(String temp) {
        if (UIUtils.isElementPresent(log, driver, By.xpath(
                uiElementMapper.getElement("iot.sample.temperature.xpath")))) {
            WebElement tempSlider = driver.findElement(By.xpath(
                    uiElementMapper.getElement("iot.sample.temperature.xpath")));
            moveSlider(tempSlider, Integer.parseInt(temp));
            return true;
        }
        return false;
    }

    /**
     * Method to change the Coffee level.
     * @param level : The value to be set.
     * @return : True if the element is present and value is set. False otherwise.
     */
    public boolean changeCoffeeLevel(String level) {
        if (UIUtils.isElementPresent(log, driver, By.xpath(
                uiElementMapper.getElement("iot.sample.coffee.level.xpath")))) {
            WebElement lvlSlider = driver.findElement(By.xpath(
                    uiElementMapper.getElement("iot.sample.coffee.level.xpath")));
            moveSlider(lvlSlider, Integer.parseInt(level));
            driver.manage().timeouts().implicitlyWait(UIUtils.webDriverTime, TimeUnit.SECONDS);
            return true;
        }
        return false;
    }

    /**
     * This method performs the slider change action of the web interface.
     * @param slider : The element of the slider to be changed.
     * @param val : Value to be set.
     */
    private void moveSlider(WebElement slider, int val) {
        Actions move = new Actions(driver);
        Action action = move.dragAndDropBy(slider, 0, val).build();
        action.perform();

    }
}