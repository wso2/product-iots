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

package org.wso2.iot.integration.device.enrollment;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import junit.framework.Assert;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.util.Base64;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.iot.integration.common.Constants;
import org.wso2.iot.integration.common.PayloadGenerator;
import org.wso2.iot.integration.common.RestClient;
import org.wso2.iot.integration.common.TestBase;

import java.sql.Timestamp;

/**
 * This class tests the android sense enrollment.
 */
public class AndroidSenseEnrollment extends TestBase {
    private static Log log = LogFactory.getLog(AndroidSenseEnrollment.class);
    private RestClient client;
    private String DEVICE_ID = "AS101";
    private RestClient analyticsClient;

    @Factory(dataProvider = "userModeProvider")
    public AndroidSenseEnrollment(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true, groups = { Constants.UserManagement.USER_MANAGEMENT_GROUP})
    public void initTest() throws Exception {
        super.init(userMode);
        User currentUser = getAutomationContext().getContextTenant().getContextUser();
        byte[] bytesEncoded = Base64
                .encodeBase64((currentUser.getUserName() + ":" + currentUser.getPassword()).getBytes());
        String encoded = new String(bytesEncoded);
        String auth_string = "Basic " + encoded;
        String anaytics_https_url = automationContext.getContextUrls().getWebAppURLHttps()
                .replace("9443", String.valueOf(Constants.HTTPS_ANALYTICS_PORT))
                .replace("/t/" + automationContext.getContextTenant().getDomain(), "") + "/";
        this.client = new RestClient(backendHTTPSURL, Constants.APPLICATION_JSON, accessTokenString);
        this.analyticsClient = new RestClient(anaytics_https_url, Constants.APPLICATION_JSON, auth_string);
        if (this.userMode == TestUserMode.TENANT_ADMIN) {
            HttpResponse response = client
                    .post(Constants.AndroidSenseEnrollment.ANALYTICS_ARTIFACTS_DEPLOYMENT_ENDPOINT, "");
            Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
        }
    }

    @Test(description = "Test an Android sense device enrollment.")
    public void testEnrollment() throws Exception {
        // Time for deploying the carbon apps
        Thread.sleep(30000);
        HttpResponse response = client.post(Constants.AndroidSenseEnrollment.ENROLLMENT_ENDPOINT + DEVICE_ID
                + "/register?deviceName=android_sense_test", "");
        Assert.assertEquals(HttpStatus.SC_OK, response.getResponseCode());
        JsonElement jsonElement = new JsonParser().parse(response.getData());
        JsonObject expectedPayloadObject = jsonElement.getAsJsonObject();
        Assert.assertNotNull("Mqtt end-point is returned with the android sense enrollment " + "payload",
                expectedPayloadObject.get("mqttEndpoint"));
        Assert.assertNotNull("Tenant domain is returned with the android sense enrollment " + "payload",
                expectedPayloadObject.get("tenantDomain"));

    }

    @Test(description = "Test an Android sense device data publishing.", dependsOnMethods = {"testEnrollment"})
    public void testEventPublishing() throws Exception {
        String DEVICE_TYPE = "android_sense";
        String topic = automationContext.getContextTenant().getDomain() + "/" + DEVICE_TYPE + "/" + DEVICE_ID + "/data";
        int qos = 2;
        String broker = "tcp://localhost:1886";
        String clientId = DEVICE_ID + ":" + DEVICE_TYPE;
        MemoryPersistence persistence = new MemoryPersistence();
        MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setUserName(accessToken);
        connOpts.setPassword("".toCharArray());
        connOpts.setKeepAliveInterval(120);
        connOpts.setCleanSession(true);
        log.info("Connecting to broker: " + broker);
        sampleClient.connect(connOpts);
        log.info("Connected");
        MqttMessage message = new MqttMessage(PayloadGenerator
                .getJsonArray(Constants.AndroidSenseEnrollment.ENROLLMENT_PAYLOAD_FILE_NAME,
                        Constants.AndroidSenseEnrollment.PUBLISH_DATA_OPERATION).toString().getBytes());
        message.setQos(qos);
        for (int i = 0; i< 100 ; i++) {
            sampleClient.publish(topic, message);
            log.info("Message is published to Mqtt Client");
            Thread.sleep(1000);
        }
        sampleClient.disconnect();

        HttpResponse response = analyticsClient
                .get(Constants.AndroidSenseEnrollment.IS_TABLE_EXIST_CHECK_URL + "?table="
                        + Constants.AndroidSenseEnrollment.BATTERY_STATS_TABLE_NAME);
        Assert.assertEquals("ORG_WSO2_IOT_ANDROID_BATTERY_STATS table does not exist. Problem with the android sense "
                                    + "analytics", HttpStatus.SC_OK, response.getResponseCode());
        // Allow some time to perform the analytics tasks.

        log.info("Mqtt Client is Disconnected");

        String url = Constants.AndroidSenseEnrollment.RETRIEVER_ENDPOINT
                + Constants.AndroidSenseEnrollment.BATTERY_STATS_TABLE_NAME + "/";
        Timestamp timestamp = new Timestamp(System.currentTimeMillis() - 3600000);
        url += timestamp.getTime() + "/" + new Timestamp(System.currentTimeMillis()).getTime() + "/0/100";
        response = analyticsClient.get(url);
        JsonArray jsonArray = new JsonParser().parse(response.getData()).getAsJsonArray();
        //TODO: temporarily commenting out untill new changes are merged
//        Assert.assertEquals(
//                "Published event for the device with the id " + DEVICE_ID + " is not inserted to analytics table",
//                HttpStatus.SC_OK, response.getResponseCode());
//        Assert.assertTrue(
//                "Published event for the device with the id " + DEVICE_ID + " is not inserted to analytics table",
//                jsonArray.size() > 0);
    }
}
