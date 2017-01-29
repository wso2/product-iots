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
package org.wso2.iot.integration.device.enrollment;

import junit.framework.Assert;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.iot.integration.common.Constants;
import org.wso2.iot.integration.common.OAuthUtil;
import org.wso2.iot.integration.common.RestClient;
import org.wso2.iot.integration.common.TestBase;

import java.io.File;
import java.net.URL;

/**
 * This contains testing of Windows device enrollment which is necessary to run prior to all other Windows related
 * tests.
 */
public class WindowsEnrollment extends TestBase {
    private static final String BSD_PLACEHOLDER = "{BinarySecurityToken}";
    private static String bsd;
    private RestClient client;

    @BeforeClass(alwaysRun = true, groups = { Constants.WindowsEnrollment.WINDOWS_ENROLLMENT_GROUP})
    public void initTest() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        String accessTokenString = "Bearer " + OAuthUtil.getOAuthToken(backendHTTPURL, backendHTTPSURL);
        client = new RestClient(backendHTTPSURL, Constants.APPLICATION_JSON, accessTokenString);
    }

    /**
     * Test the Windows Discovery Get endpoint to see if the server is available.
     *
     * @throws Exception
     */
    @Test(groups = Constants.WindowsEnrollment.WINDOWS_ENROLLMENT_GROUP, description = "Test Windows Discovery get.")
    public void testServerAvailability() throws Exception {
        client.setHttpHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_SOAP_XML);
        HttpResponse response = client.get(Constants.WindowsEnrollment.DISCOVERY_GET_URL);
        Assert.assertEquals(response.getResponseCode(), HttpStatus.SC_OK);
    }

    @Test(groups = Constants.WindowsEnrollment.WINDOWS_ENROLLMENT_GROUP, description = "Test Windows Discovery post.")
    public void testDiscoveryPost() throws Exception {
        String xml = readXML(Constants.WindowsEnrollment.DISCOVERY_POST_FILE, Constants.UTF8);
        client.setHttpHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_SOAP_XML);
        HttpResponse response = client.post(Constants.WindowsEnrollment.DISCOVERY_POST_URL, xml);
        Assert.assertEquals(response.getResponseCode(), HttpStatus.SC_OK);
    }

    @Test(groups = Constants.WindowsEnrollment.WINDOWS_ENROLLMENT_GROUP, description = "Test Windows BST.")
    public void testBST() throws Exception {
        JSONObject bsdObject = new JSONObject(Constants.WindowsEnrollment.BSD_PAYLOAD);
        JSONObject childObject = bsdObject.getJSONObject("credentials");

        JSONObject modifiedObject = new JSONObject();
        modifiedObject.put("token", OAuthUtil.getOAuthToken(backendHTTPURL, backendHTTPSURL));

        childObject.put("token", modifiedObject);

        HttpResponse response = client.post(Constants.WindowsEnrollment.BSD_URL, Constants.WindowsEnrollment.BSD_PAYLOAD);
        bsd = response.getData();

        Assert.assertEquals(response.getResponseCode(), HttpStatus.SC_OK);
    }

    @Test(groups = Constants.WindowsEnrollment.WINDOWS_ENROLLMENT_GROUP, description = "Test Windows MS XCEP post.",
            dependsOnMethods = "testBST")
    public void testMSXCEP() throws Exception {
        String xml = readXML(Constants.WindowsEnrollment.MS_XCEP_FILE, Constants.UTF8);
        String payload = xml.replace(BSD_PLACEHOLDER, bsd);
        client.setHttpHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_SOAP_XML);
        HttpResponse response = client.post(Constants.WindowsEnrollment.MS_EXCEP, payload);
        Assert.assertEquals(response.getResponseCode(), HttpStatus.SC_ACCEPTED);
    }

    @Test(groups = Constants.WindowsEnrollment.WINDOWS_ENROLLMENT_GROUP, description = "Test Windows WSETP post.",
            dependsOnMethods = "testMSXCEP")
    public void testWSETP() throws Exception {
        String xml = readXML(Constants.WindowsEnrollment.WS_STEP_FILE, Constants.UTF8);
        String payload = xml.replace(BSD_PLACEHOLDER, bsd);
        client.setHttpHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_SOAP_XML);
        HttpResponse response = client.post(Constants.WindowsEnrollment.WSTEP_URL, payload);
        Assert.assertEquals(response.getResponseCode(), HttpStatus.SC_ACCEPTED);
    }


    private String readXML(String fileName, String characterEncoding) throws Exception {
        URL url = ClassLoader.getSystemResource(fileName);
        File folder = new File(url.toURI());
        return FileUtils.readFileToString(folder, characterEncoding);
    }
/*
    @Test(groups = Constants.WindowsEnrollment.WINDOWS_ENROLLMENT_GROUP, description = "Test Windows WSETP post.",
          dependsOnMethods = "testWSETP")
    public void testInitialDeviceInfo() throws Exception {
        URL url = ClassLoader.getSystemResource("windows/enrollment/inital_device_info.xml");
        File folder = new File(url.toURI());
        String xml = FileUtils.readFileToString(folder, "UTF-16");
        client.setHttpHeader("Content-Type", "application/vnd.syncml.dm+xml;charset=utf-8");
        HttpResponse response = client.post(Constants.WindowsEnrollment.SYNC_ML_URL, xml);

        Assert.assertEquals(response.getResponseCode(), HttpStatus.SC_OK);
    }
    */

}
