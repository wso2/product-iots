package org.wso2.iot.integration.ui.pages.error;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.iot.integration.ui.pages.UIElementMapper;
import org.wso2.iot.integration.ui.pages.UIUtils;

import java.io.IOException;

public class IOTErrorPage {

    private WebDriver driver;
    private UIElementMapper uiElementMapper;
    private boolean isErrorPage = true;

    public IOTErrorPage (WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        WebDriverWait webDriverWait = new WebDriverWait(driver, UIUtils.webDriverTimeOut);

        if (!webDriverWait.until(ExpectedConditions.titleContains(uiElementMapper.getElement("cdmf.error.page")))) {
            isErrorPage = false;
            throw new IllegalStateException("This is not the Error page");
        }
    }

    public boolean isErrorPresent() {
        return this.isErrorPage;
    }
}
