package org.wso2.carbon.iot.integration.web.ui.test.error;

import junit.framework.Assert;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.carbon.iot.integration.web.ui.test.common.Constants;
import org.wso2.carbon.iot.integration.web.ui.test.common.IOTIntegrationUIBaseTestCase;
import org.wso2.iot.integration.ui.pages.error.IOTErrorPage;

import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

public class InitialErrorTest extends IOTIntegrationUIBaseTestCase{

    private WebDriver driver;

    @BeforeClass(alwaysRun = true)
    public void setup() throws XPathExpressionException, XMLStreamException, IOException {
        super.init();
        driver = BrowserManager.getWebDriver();
        driver.get(getWebAppURL() + Constants.IOT_LOGIN_PATH);
    }

    @Test(description = "Test for initial login error.")
    public void ErrorPageTest() throws IOException {
        IOTErrorPage errorPage = new IOTErrorPage(driver);
        Assert.assertTrue(errorPage.isErrorPresent());
    }

    @AfterClass(alwaysRun = true)
    public void tearDown(){
        driver.quit();
    }

}
