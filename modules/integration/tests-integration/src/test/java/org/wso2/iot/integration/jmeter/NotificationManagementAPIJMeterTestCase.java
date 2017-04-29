package org.wso2.iot.integration.jmeter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.extensions.jmeter.JMeterTest;
import org.wso2.carbon.automation.extensions.jmeter.JMeterTestManager;

import java.io.File;
import java.net.URL;

/**
 * This class tests the Notification Management APIs
 */
public class NotificationManagementAPIJMeterTestCase {
    private static Log log = LogFactory.getLog(NotificationManagementAPIJMeterTestCase.class);

    @Test(description = "This test case tests the Notification Management APIs")
    public void NotificationManagementTest() throws AutomationFrameworkException {
        URL url = Thread.currentThread().getContextClassLoader()
                .getResource("jmeter-scripts" + File.separator + "NotificationManagementAPI.jmx");
        JMeterTest script = new JMeterTest(new File(url.getPath()));
        JMeterTestManager manager = new JMeterTestManager();
        log.info("Running notification management api test cases using jmeter scripts");
        manager.runTest(script);
    }
}
