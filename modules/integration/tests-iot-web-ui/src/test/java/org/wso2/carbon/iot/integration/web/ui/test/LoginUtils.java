package org.wso2.carbon.iot.integration.web.ui.test;

import org.openqa.selenium.WebDriver;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.iot.integration.ui.pages.UIElementMapper;
import org.wso2.iot.integration.ui.pages.login.LoginPage;

/**
 * Login as registered user
 */
public class LoginUtils {
    private static UIElementMapper uiElementMapper;

    public static void login(WebDriver driver, AutomationContext automationContext,
                             String webAppURL) throws Exception {
        uiElementMapper = UIElementMapper.getInstance();
        driver.get(webAppURL + Constants.IOT_LOGIN_PATH);
        LoginPage test = new LoginPage(driver);
        test.loginAsAdmin(automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                          automationContext.getSuperTenant().getTenantAdmin().getPassword());
        }


}
