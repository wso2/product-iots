package org.wso2.iot.integration.ui.pages.uesr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.wso2.iot.integration.ui.pages.UIElementMapper;

import java.io.IOException;

/**
 * This class represents the confirmation page for adding a new user.
 */
public class UserAddedConfirmationPage {
    private static final Log log = LogFactory.getLog(UserAddedConfirmationPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public UserAddedConfirmationPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();

        driver.findElement(By.xpath(uiElementMapper.getElement("iot.admin.addUser.view.btn.xpath"))).click();
    }
}
