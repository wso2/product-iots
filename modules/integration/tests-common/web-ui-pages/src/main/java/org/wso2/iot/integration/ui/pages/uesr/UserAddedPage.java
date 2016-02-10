package org.wso2.iot.integration.ui.pages.uesr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.iot.integration.ui.pages.UIElementMapper;

/**
 * Created by menaka on 1/27/16.
 *
 */
public class UserAddedPage {
    private static final Log log = LogFactory.getLog(UserAddedPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public UserAddedPage(WebDriver driver) throws Exception {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();

        driver.findElement(By.xpath(uiElementMapper.getElement("iot.admin.addUser.view.btn.xpath"))).click();
//        WebDriverWait webDriverWait = new WebDriverWait(driver, 10);
//        if (!driver.findElement(By.xpath(uiElementMapper.getElement("iot.admin.user.added.page.subtitle.xpath")))
//                .getText().contains("User was added successfully.")){
//            throw new IllegalStateException("This is not the User Added page");
//        }



    }



}
