/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.iot.integration.common;

import org.apache.commons.net.util.Base64;
import org.testng.annotations.DataProvider;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;

import javax.xml.xpath.XPathExpressionException;

/**
 * This is the base test class that provides common details necessary for other test cases.
 */
public class TestBase {
    protected AutomationContext automationContext;
    protected String backendHTTPSURL;
    protected String backendHTTPURL;
    protected String accessTokenString;
    protected String accessToken;
    protected TestUserMode userMode;

    protected void init(TestUserMode userMode) throws Exception {
        automationContext = new AutomationContext(Constants.AUTOMATION_CONTEXT, userMode);
        String tenantDomain = automationContext.getContextTenant().getDomain();
        backendHTTPSURL = automationContext.getContextUrls().getWebAppURLHttps().replace("9443", String.valueOf(Constants
                .HTTPS_GATEWAY_PORT)).replace("/t/" + tenantDomain , "");
        backendHTTPURL = automationContext.getContextUrls().getWebAppURL().replace("9763", String.valueOf(Constants
                .HTTP_GATEWAY_PORT)).replace("/t/" + tenantDomain , "");
        User currentUser = getAutomationContext().getContextTenant().getContextUser();
        byte[] bytesEncoded = Base64
                .encodeBase64((currentUser.getUserName() + ":" + currentUser.getPassword()).getBytes());
        String encoded = new String(bytesEncoded);
        accessToken = OAuthUtil.getOAuthTokenPair(encoded, backendHTTPSURL, backendHTTPSURL, currentUser.getUserName(),
                currentUser.getPassword());
        accessTokenString = "Bearer " + accessToken;
    }

    protected void initPublisher(String productGroupName, String instanceName,
                                 TestUserMode userMode)
            throws XPathExpressionException {
        automationContext = new AutomationContext(productGroupName, instanceName, userMode);
        backendHTTPSURL = automationContext.getContextUrls().getBackEndUrl();
    }

    public String getBackendHTTPURL() {
        return backendHTTPURL;
    }

    public void setBackendHTTPURL(String backendHTTPURL) {
        this.backendHTTPURL = backendHTTPURL;
    }

    protected String getBackendHTTPSURL() throws XPathExpressionException {
        return backendHTTPSURL;
    }

    protected String getSessionCookie() throws Exception {
        LoginLogoutClient loginLogoutClient = new LoginLogoutClient(automationContext);
        return loginLogoutClient.login();
    }

    protected String getServiceURL() throws XPathExpressionException {
        return automationContext.getContextUrls().getServiceUrl();
    }

    protected AutomationContext getAutomationContext() {
        return automationContext;
    }

    protected String getTestArtifactLocation() {
        return FrameworkPathUtil.getSystemResourceLocation();
    }

    @DataProvider
    public static Object[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN},
                new TestUserMode[]{TestUserMode.TENANT_ADMIN}
        };
    }
}
