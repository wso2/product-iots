/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.iot.integration.web.ui.test.samples;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.extensions.servers.carbonserver.CarbonServerManager;
import org.wso2.iot.integration.ui.pages.IOTIntegrationUIBaseTestCase;

import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;


/**
 * Test cases for building and installation of sample device types.
 * When installing a custom device type to the IOT server, developer has to create the device type as a feature and
 * put it in to the samples folder.
 * Then build the device type created with maven.
 * After that install the device type by running the device-deployer.xml.
 * Then the server has to be restarted, in order to activate newly installed device type.
 * <p>
 * In this test case, the build process of a new device type and installation to the server is tested.
 */
public class SampleInstallationTest extends IOTIntegrationUIBaseTestCase {
    Log log = LogFactory.getLog(SampleInstallationTest.class);
    private Process tempProcess = null;
    private Properties properties = System.getProperties();
    private String carbonHome = properties.getProperty("carbon.home");
    private String[] cmdArray;

    @BeforeClass(alwaysRun = true)
    public void setup() throws XPathExpressionException, XMLStreamException, IOException, AutomationFrameworkException {
        super.init();
    }

    @Test(description = "Verify the sample build process")
    public void sampleBuildTest() throws IOException {
        String connectedCupDir = carbonHome + File.pathSeparator + "samples" + File.pathSeparator + "connectedcup";
        log.info("Connected cup Sample: " + connectedCupDir);
        File dir = new File(connectedCupDir);
        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                log.info("Executing maven clean install --------------------------------");
                cmdArray = new String[]{"cmd.exe", "/c", "mvn clean install"};
                tempProcess = Runtime.getRuntime().exec(cmdArray, null, dir);

            } else {
                log.info("Executing maven clean install --------------------------------");
                cmdArray = new String[]{"mvn", "clean", "install"};
                tempProcess = Runtime.getRuntime().exec(cmdArray, null, dir);
            }

            boolean buildStatus = waitForMessage(tempProcess.getInputStream(), "BUILD SUCCESS");
            Assert.assertTrue(buildStatus, "Building the sample was not successful");
        } finally {
            if (tempProcess != null) {
                tempProcess.destroy();
            }
        }
    }

    @Test(description = "Verify the sample installation process", dependsOnMethods = {"sampleBuildTest"})
    public void sampleInstallationTest() throws IOException {

        log.info("CARBON_HOME: " + System.getProperty("carbon.home"));
        File dir = new File(carbonHome);
        log.info("Sample installation started : mvn clean install -f device-deployer.xml");
        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                cmdArray = new String[]{"cmd.exe", "/c", "mvn clean install -f device-deployer.xml"};
                tempProcess = Runtime.getRuntime().exec(cmdArray, null, dir);
            } else {
                cmdArray = new String[]{"mvn", "clean", "install", "-f", "device-deployer.xml"};
                tempProcess = Runtime.getRuntime().exec(cmdArray, null, dir);
            }
            boolean buildStatus = waitForMessage(tempProcess.getInputStream(), "BUILD SUCCESS");
            Assert.assertTrue(buildStatus, "Sample installation was not successful");
        } finally {
            if (tempProcess != null) {
                tempProcess.destroy();
            }
        }
    }

    @Test(description = "Test restarting the server", dependsOnMethods = {"sampleInstallationTest"})
    public void serverRestartTest() {
        CarbonServerManager serverManager = new CarbonServerManager(automationContext);
        try {
            serverManager.restartGracefully();
        } catch (AutomationFrameworkException e) {
            log.error("Restart failed....");
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
    }

    public boolean waitForMessage(InputStream inputStream, String message) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        boolean status = false;
        while ((line = br.readLine()) != null) {
            log.info(line);
            if (!status && line.contains(message)) {
                status = true;
            }
        }
        return status;
    }
}
