package org.wso2.iot.integration.ui.pages.uesr;

import org.apache.commons.jexl2.UnifiedJEXL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.PortableServer.THREAD_POLICY_ID;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.wso2.iot.integration.ui.pages.UIElementMapper;

public class UserListingPage {
    private static final Log log = LogFactory.getLog(UserListingPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public UserListingPage(WebDriver driver) throws Exception {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();

        if (!driver.findElement(By.xpath("//span[@class='page-sub-title']"))
                .getText().contains("USERS")){
            throw new IllegalStateException("This is not the User Listing page");
        }
    }

    public UserListingPage deleteUser() throws Exception {
        WebElement deleteBtn = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.admin.deleteUser.btn.xpath")));
        if (deleteBtn!=null){
            deleteBtn.click();
        } else {
            return new UserListingPage(driver);
        }
        driver.findElement(By.xpath(uiElementMapper.getElement("iot.admin.deleteUser.yes.link.xpath"))).click();
        Thread.sleep(1000);
        driver.findElement(By.xpath(uiElementMapper.getElement("iot.admin.deleteUser.success.link.xpath"))).click();

        return new UserListingPage(driver);
    }


}
