/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.iot.integration.common.extensions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.ContextXpathConstants;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.engine.extensions.ExecutionListenerExtension;
import org.wso2.carbon.automation.extensions.ExtensionConstants;
import org.wso2.carbon.automation.extensions.servers.carbonserver.CarbonServerExtension;

import javax.xml.xpath.XPathExpressionException;

/**
 * Test Automation server extension to start the IOT core.
 * This will set the carbon_home to {carbonHome}/core and port offset : 0
 */
public class IOTServerExtension extends ExecutionListenerExtension {

    private CustomTestServerManager serverManager;
    private static final Log log = LogFactory.getLog(CarbonServerExtension.class);
    private String executionEnvironment;
    private AutomationContext automationContext;
    private final String  IOT_CORE_PORT_OFFSET = "0";

    @Override
    public void initiate() throws AutomationFrameworkException {
        try {
            automationContext = new AutomationContext("IOT", TestUserMode.SUPER_TENANT_USER);
            if(getParameters().get(ExtensionConstants.SERVER_STARTUP_PORT_OFFSET_COMMAND) == null) {
                getParameters().put(ExtensionConstants.SERVER_STARTUP_PORT_OFFSET_COMMAND, IOT_CORE_PORT_OFFSET);
            }
            serverManager = new CustomTestServerManager(getAutomationContext(), null, getParameters());
            executionEnvironment =
                    automationContext.getConfigurationValue(ContextXpathConstants.EXECUTION_ENVIRONMENT);

        } catch (XPathExpressionException e) {
            handleException("Error while initiating test environment", e);
        }
    }

    @Override
    public void onExecutionStart() throws AutomationFrameworkException {
        try {
            if (executionEnvironment.equalsIgnoreCase(ExecutionEnvironment.STANDALONE.name())) {
                String carbonHome = serverManager.startServer("core");
                log.info(carbonHome);
                System.setProperty(ExtensionConstants.CARBON_HOME, carbonHome);

                // Need to give time for the apis to be added to the synapse configurations.
                Thread.sleep(30000);
            }
        } catch (Exception e) {
            handleException("Fail to start carbon server ", e);
        }
    }

    @Override
    public void onExecutionFinish() throws AutomationFrameworkException {
        try {
            if (executionEnvironment.equalsIgnoreCase(ExecutionEnvironment.STANDALONE.name())) {
                serverManager.stopServer();
            }
        } catch (Exception e) {
            handleException("Fail to stop carbon server ", e);
        }
    }

    private static void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new RuntimeException(msg, e);
    }
}
