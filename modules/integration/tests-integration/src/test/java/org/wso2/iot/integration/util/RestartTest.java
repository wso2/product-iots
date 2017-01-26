package org.wso2.iot.integration.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.BeforeClass;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.common.admin.client.LogViewerClient;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.logging.view.stub.LogViewerLogViewerException;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;
import org.wso2.iot.integration.common.TestBase;

import javax.xml.xpath.XPathExpressionException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.concurrent.*;

public class RestartTest extends TestBase {

    private Log log = LogFactory.getLog(RestartTest.class);
    private LogViewerClient logViewerClient;

    @BeforeClass
    public void initTest() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        logViewerClient =  new LogViewerClient(getBackendHTTPSURL(), getSessionCookie());
    }

    @Test(description = "Test restarting the server")
    public void serverRestartTest() {
        ServerConfigurationManager serverManager;
        try {
            serverManager = new ServerConfigurationManager(automationContext);
            log.info("Restart Triggered -------------------------------------------------------------------");
            serverManager.restartGracefully();
            logViewerClient.getAllRemoteSystemLogs();
            waitForRestart();
        } catch (AutomationUtilException | XPathExpressionException | MalformedURLException e) {
            log.error("Restart failed due to : " + e.getLocalizedMessage());
        } catch (RemoteException | LogViewerLogViewerException e) {
            log.error("Cannot get server log due to : " + e.getLocalizedMessage());
        }
    }

    /**
     * Wait until the server restarts.
     * This method looks for "Mgt console URL:" to be appeared in the terminal.
     * If it does not appear within the given timeout an Exception will be thrown.
     */
    private void waitForRestart() {
        ExecutorService service = Executors.newSingleThreadExecutor();
        try {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    try {
                        LogEvent[] logEvents = logViewerClient.getAllRemoteSystemLogs();
                        for (LogEvent event : logEvents) {
                            log.info(event.getMessage() + " @ " + event.getLogTime());
                            if (event.getMessage().contains("Mgt Console URL  : " )){
                                log.info("Server restarted successfully");
                                Assert.assertTrue(true);
                            }
                        }
                    } catch (RemoteException | LogViewerLogViewerException e) {
                        log.error("Error reading logs. \n" + e.getMessage());
                        Assert.assertTrue(false);
                    }
                }
            };

            Future<?> f = service.submit(r);

            f.get(30, TimeUnit.MINUTES);
        } catch (final InterruptedException e) {
            log.error("Interrupted "+e.getMessage());
            Assert.assertTrue(false);
        }  catch (final TimeoutException e) {
            log.error("Timeout " + e.getMessage());
            Assert.assertTrue(false);
        }  catch (final ExecutionException e) {
            log.error("Execution failed " + e.getMessage());
            Assert.assertTrue(false);
        }  finally {
            service.shutdown();
        }
    }

}
