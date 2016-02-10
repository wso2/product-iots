package org.wso2.iot.integration.ui.pages.uesr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.iot.integration.ui.pages.UIElementMapper;

/**
 * Created by menaka on 2/8/16.
 */
public class ViewUserPage {
    private static final Log log = LogFactory.getLog(ViewUserPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public ViewUserPage (WebDriver driver) throws Exception {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();

        WebDriverWait webDriverWait = new WebDriverWait(driver, 10);
        if (!webDriverWait.until(ExpectedConditions.titleContains("User Management | IoT Server"))){
            throw new IllegalStateException("This is not the User view page");
        }
    }



}
