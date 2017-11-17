/*
*  Copyright (c) 2005-2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.iot.integration.common.extensions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.extensions.ExtensionConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CustomTestServerManager {
    protected CarbonServerManagerExtension carbonServer;
    protected String carbonZip;
    protected int portOffset;
    protected Map<String, String> commandMap = new HashMap<String, String>();
    private static final Log log = LogFactory.getLog(CustomTestServerManager.class);
    protected String carbonHome;

    public CustomTestServerManager(AutomationContext context) {
        carbonServer = new CarbonServerManagerExtension(context);
    }

    public CustomTestServerManager(AutomationContext context, String carbonZip) {
        carbonServer = new CarbonServerManagerExtension(context);
        this.carbonZip = carbonZip;
    }

    public CustomTestServerManager(AutomationContext context, int portOffset) {
        carbonServer = new CarbonServerManagerExtension(context);
        this.portOffset = portOffset;
        commandMap.put(ExtensionConstants.SERVER_STARTUP_PORT_OFFSET_COMMAND, String.valueOf(portOffset));
    }

    public CustomTestServerManager(AutomationContext context, String carbonZip,
                                   Map<String, String> commandMap) {
        carbonServer = new CarbonServerManagerExtension(context);
        this.carbonZip = carbonZip;
        if (commandMap.get(ExtensionConstants.SERVER_STARTUP_PORT_OFFSET_COMMAND) != null) {
            this.portOffset = Integer.parseInt(commandMap.get(ExtensionConstants.SERVER_STARTUP_PORT_OFFSET_COMMAND));
        } else {
            throw new IllegalArgumentException("portOffset value must be set in command list");
        }
        this.commandMap = commandMap;
    }

    public String getCarbonZip() {
        return carbonZip;
    }

    public String getCarbonHome() {
        return carbonHome;
    }

    public int getPortOffset() {
        return portOffset;
    }

    public void configureServer() throws AutomationFrameworkException {

    }

    public Map<String, String> getCommands() {
        return commandMap;
    }

    /**
     * This method is called for starting a Carbon server in preparation for execution of a
     * TestSuite
     * <p/>
     * Add the @BeforeSuite TestNG annotation in the method overriding this method
     *
     * @param server : The server which needs to be start.
     * @return The CARBON_HOME
     * @throws IOException If an error occurs while copying the deployment artifacts into the
     *                     Carbon server
     */
    public synchronized String startServer(String server)
            throws AutomationFrameworkException, IOException, XPathExpressionException, InterruptedException {
        if (carbonHome == null) {
            if (carbonZip == null) {
                carbonZip = System.getProperty(FrameworkConstants.SYSTEM_PROPERTY_CARBON_ZIP_LOCATION);
            }
            if (carbonZip == null) {
                throw new IllegalArgumentException("carbon zip file cannot find in the given location");
            }
            String extractedDir = getExistingExtractedDir();
            if (server.equalsIgnoreCase("core")) {
                if (extractedDir == null) {
                    carbonHome = carbonServer.setUpCarbonHome(carbonZip);
                } else {
                    carbonHome = extractedDir;
                }
                // Deploy the plugins.
                String[] cmdArray = new String[] { "mvn", "clean", "install", "-f", "device-plugins-deployer.xml"};
                Runtime.getRuntime().exec(cmdArray, null, new File(carbonHome + File.separator + "samples"));
                Thread.sleep(15000);
            } else if (server.equalsIgnoreCase("analytics") || server.equalsIgnoreCase("broker")) {
                if (extractedDir == null) {
                    carbonHome = carbonServer.setUpCarbonHome(carbonZip) + File.separator + "wso2" + File.separator + server;
                } else {
                    carbonHome = extractedDir + File.separator + "wso2" + File.separator + server;
                }
            } else {
                throw new IllegalArgumentException("Unsupported server type provided - " + server);
            }
            configureServer();
        }
        log.info("Carbon Home - " + carbonHome);
        if (commandMap.get(ExtensionConstants.SERVER_STARTUP_PORT_OFFSET_COMMAND) != null) {
            this.portOffset = Integer.parseInt(commandMap.get(ExtensionConstants.SERVER_STARTUP_PORT_OFFSET_COMMAND));
        } else {
            this.portOffset = 0;
        }

        carbonServer.startServerUsingCarbonHome(carbonHome, commandMap);
        return carbonHome;
    }

    private String getExistingExtractedDir() {
        File zipDir = new File(System.getProperty("basedir", ".") + File.separator + "target");
        File[] subFiles = zipDir.listFiles();
        if (subFiles != null) {
            for (File subFile : subFiles) {
                if (subFile.getName().startsWith("carbontmp")) {
                    File[] carbonServerFiles = subFile.listFiles();
                    if (carbonServerFiles != null) {
                        for (File file : carbonServerFiles) {
                            if (file.getName().startsWith("wso2iot")) {
                                return file.getAbsolutePath();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Restarting server already started by the method startServer
     *
     * @throws AutomationFrameworkException
     */
    public void restartGracefully() throws AutomationFrameworkException {
        if (carbonHome == null) {
            throw new AutomationFrameworkException("No Running Server found to restart. " +
                    "Please make sure whether server is started");
        }
        carbonServer.restartGracefully();
    }

    /**
     * This method is called for stopping a Carbon server
     * <p/>
     * Add the @AfterSuite annotation in the method overriding this method
     *
     * @throws AutomationFrameworkException If an error occurs while shutting down the server
     */
    public void stopServer() throws AutomationFrameworkException {
        carbonServer.serverShutdown(portOffset);
    }
}
