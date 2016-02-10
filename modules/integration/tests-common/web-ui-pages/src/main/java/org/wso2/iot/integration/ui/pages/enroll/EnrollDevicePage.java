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

    public EnrollDevicePage(WebDriver driver) throws IOException{
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
    }

}
