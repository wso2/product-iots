package org.wso2.iot.integration.ui.pages.uesr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.wso2.iot.integration.ui.pages.UIElementMapper;

/**
 * Class to represent the user listing page.
 */
public class UserListingPage {
    private static final Log log = LogFactory.getLog(UserListingPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public UserListingPage(WebDriver driver) throws Exception {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();

        if (!driver.findElement(By.xpath(uiElementMapper.getElement("iot.admin.user.added.page.subtitle.xpath")))
                .getText().contains("USERS")){
            throw new IllegalStateException("This is not the User Listing page");
        }
    }

    /**
     * @return After deleting a user, returns back to the user listing page.
     * @throws Exception
     */
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
