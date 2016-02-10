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

/**
 * Created by menaka on 1/19/16.
 *
 */
public class IOTIntegrationUIBaseTestCase {

    private static final Log log = LogFactory.getLog(IOTIntegrationUIBaseTestCase.class);
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
            throws RemoteException, XPathExpressionException,
                   LoginAuthenticationExceptionException {
        AuthenticatorClient authenticationAdminClient = new AuthenticatorClient(context.getContextUrls().getBackEndUrl());
        String sessionCookie = authenticationAdminClient.login(automationContext.getSuperTenant().
                                                                       getTenantAdmin().getUserName(), automationContext.getSuperTenant().
                                                                       getTenantAdmin().getPassword(),
                                                               automationContext.getDefaultInstance().getHosts().get("default"));

        return sessionCookie;
    }

    protected String getServiceURL() throws XPathExpressionException {
        String serviceURL = automationContext.getContextUrls().getServiceUrl();
        return automationContext.getContextUrls().getServiceUrl();
    }

    protected String getLoginURL() throws XPathExpressionException {
        return HomePageGenerator.getProductHomeURL(automationContext);
    }
}
