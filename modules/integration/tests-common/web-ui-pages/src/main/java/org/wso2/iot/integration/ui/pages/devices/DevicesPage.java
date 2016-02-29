package org.wso2.iot.integration.ui.pages.devices;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.iot.integration.ui.pages.UIConstants;
import org.wso2.iot.integration.ui.pages.UIElementMapper;

import java.io.IOException;

public class DevicesPage {
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public DevicesPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();

        WebDriverWait webDriverWait = new WebDriverWait(driver, UIConstants.webDriverTimeOut);
        if (!webDriverWait.until(ExpectedConditions.titleContains("Device Management | IoT Server"))) {
            throw new IllegalStateException("This is not the Device Management page");
        }
    }

}
