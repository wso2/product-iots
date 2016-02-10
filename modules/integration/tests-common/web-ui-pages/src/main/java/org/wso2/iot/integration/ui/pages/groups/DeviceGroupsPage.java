package org.wso2.iot.integration.ui.pages.groups;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.iot.integration.ui.pages.UIElementMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by menaka on 1/24/16.
 *
 */
public class DeviceGroupsPage {
    private static final Log log = LogFactory.getLog(DeviceGroupsPage.class);
    private WebDriver driver;
    private UIElementMapper uiElementMapper;

    public DeviceGroupsPage(WebDriver driver) throws IOException {
        this.driver = driver;
        this.uiElementMapper = UIElementMapper.getInstance();
        WebDriverWait webDriverWait = new WebDriverWait(driver, 10);

        if (!webDriverWait.until(ExpectedConditions.textToBePresentInElement(driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.device.groups.view.header.xpath"))), "GROUPS"))) {
            throw new IllegalStateException("This is not the Groups page");
        }
    }

    public DeviceAddGroupPage addNewGroup() throws Exception {
        WebElement addNewGroupBtn = driver.findElement(By.xpath(
                uiElementMapper.getElement("iot.device.viewGroup.empty.addGroup.xpath")));
        addNewGroupBtn.click();
        return new DeviceAddGroupPage(driver);
    }

    public boolean isGroupCreated (String groupName) {
        WebElement table = driver.findElement(By.id(uiElementMapper.getElement("iot.device.table.id")));
        List<WebElement> allGroupNames = table.findElements(By.tagName("h4"));
        List<String> groupsList = new ArrayList<>();

        for (WebElement name : allGroupNames) {
           groupsList.add(name.getText());

        }

        return groupsList.contains(groupName);
    }



}
