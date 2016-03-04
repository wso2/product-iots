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
package org.wso2.iot.integration.ui.pages;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.common.HomePageGenerator;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;

import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.rmi.RemoteException;


public class IOTIntegrationUIBaseTestCase {
    protected AutomationContext automationContext;

    protected void init() throws IOException, XMLStreamException, XPathExpressionException {
        automationContext = new AutomationContext("IOT", "iot001", TestUserMode.SUPER_TENANT_ADMIN);
    }

    protected String getBackendURL() throws XPathExpressionException {
        return automationContext.getContextUrls().getBackEndUrl();
    }

    protected String getWebAppURL() throws XPathExpressionException {
        return automationContext.getContextUrls().getWebAppURL();
    }

    protected String getSessionCookie(AutomationContext context)
            throws RemoteException, XPathExpressionException, LoginAuthenticationExceptionException {
        AuthenticatorClient authenticationAdminClient = new AuthenticatorClient(context.getContextUrls().getBackEndUrl());
        return authenticationAdminClient.login(automationContext.getSuperTenant().
                                                       getTenantAdmin().getUserName(), automationContext.getSuperTenant().
                                                       getTenantAdmin().getPassword(),
                                               automationContext.getDefaultInstance().getHosts().get("default"));
    }

    protected String getServiceURL() throws XPathExpressionException {
        String serviceURL = automationContext.getContextUrls().getServiceUrl();
        return automationContext.getContextUrls().getServiceUrl();
    }

    protected String getLoginURL() throws XPathExpressionException {
        return HomePageGenerator.getProductHomeURL(automationContext);
    }
}
