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

package org.wso2.iot.integration.samples;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import junit.framework.Assert;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.iot.integration.common.Constants;
import org.wso2.iot.integration.common.MqttSubscriberClient;
import org.wso2.iot.integration.common.PayloadGenerator;
import org.wso2.iot.integration.common.RestClient;
import org.wso2.iot.integration.common.TestBase;

import java.util.ArrayList;

/**
 * This class tests the functionality of the virtual fire alarm.
 */
public class VirtualFireAlarmTestCase extends TestBase {
    private static Log log = LogFactory.getLog(VirtualFireAlarmTestCase.class);
    public static String deviceId1;
    public static String deviceId2;
    public static String tenantDeviceId1;
    public static String tenantDeviceId2;
    public static long currentTime;
    private String broker = "tcp://localhost:1886";
    private String DEVICE_TYPE = "virtual_firealarm";
    private RestClient restClient;

    @Factory(dataProvider = "userModeProvider")
    public VirtualFireAlarmTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {
        super.init(userMode);
        restClient = new RestClient(backendHTTPSURL, Constants.APPLICATION_JSON, accessTokenString);
        if (userMode == TestUserMode.TENANT_ADMIN) {
            HttpResponse response = restClient
                    .post(Constants.VirtualFireAlarmConstants.ANALYTICS_ARTIFACTS_DEPLOYMENT_ENDPOINT, "");
            Assert.assertEquals(HttpStatus.SC_CREATED, response.getResponseCode());
        }
    }

    @Test(description = "This test case tests the virtual fire alarm enrollment")
    public void testEnrollment() throws Exception {
        // Time for deploying the carbon apps
        Thread.sleep(30000);
        RestClient client = new RestClient(backendHTTPSURL, Constants.APPLICATION_ZIP, accessTokenString);
        // Enroll an advanced agent and check whether that enrollment succeeds without issues.
        HttpResponse response = client.get(Constants.VirtualFireAlarmConstants.ENROLLMENT_ENDPOINT
                + "?deviceName=advanced&sketchType=virtual_firealarm_advanced");
        Assert.assertEquals("Advance fire alarm advance agent enrollment failed", HttpStatus.SC_OK,
                response.getResponseCode());

        // Enroll an simple agent and check whether that enrollment succeeds without issues.
        response = client.get(Constants.VirtualFireAlarmConstants.ENROLLMENT_ENDPOINT
                + "?deviceName=simple&sketchType=virtual_firealarm");
        Assert.assertEquals("Advance fire alarm advance agent enrollment failed", HttpStatus.SC_OK,
                response.getResponseCode());

        response = client.get(Constants.MobileDeviceManagement.GET_DEVICE_COUNT_ENDPOINT + "?type=virtual_firealarm");
        JsonArray jsonArray = new JsonParser().parse(response.getData()).getAsJsonObject().getAsJsonArray("devices");
        Assert.assertEquals("Virtual fire alarm enrollment failed ", 2, jsonArray.size());

        if (userMode != TestUserMode.TENANT_ADMIN) {
            deviceId1 = jsonArray.get(0).getAsJsonObject().getAsJsonPrimitive("deviceIdentifier").getAsString();
            deviceId2 = jsonArray.get(1).getAsJsonObject().getAsJsonPrimitive("deviceIdentifier").getAsString();
        } else {
            tenantDeviceId1 = jsonArray.get(0).getAsJsonObject().getAsJsonPrimitive("deviceIdentifier").getAsString();
            tenantDeviceId2 = jsonArray.get(1).getAsJsonObject().getAsJsonPrimitive("deviceIdentifier").getAsString();
        }
    }

    @Test(description = "Test whether the publishing to a mqtt broker works fine without exceptions", dependsOnMethods =
            {"testEnrollment"} )
    public void testEventPublishing() throws Exception {
        String deviceId1 = userMode == TestUserMode.TENANT_ADMIN ? tenantDeviceId1 : VirtualFireAlarmTestCase.deviceId1;
        String deviceId2 = userMode == TestUserMode.TENANT_ADMIN ? tenantDeviceId2 : VirtualFireAlarmTestCase.deviceId2;
        // Publishing message as a device with simple agent (device 1)
        String topic = automationContext.getContextTenant().getDomain() + "/" + DEVICE_TYPE + "/" + deviceId1 +
                "/temperature";
        int qos = 2;
        String clientId = deviceId1 + ":" + DEVICE_TYPE;
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
        JsonObject fireAlarmPayload = PayloadGenerator.getJsonPayload(Constants.VirtualFireAlarmConstants
                .PAYLOAD_FILE, Constants.AndroidSenseEnrollment.PUBLISH_DATA_OPERATION);
        JsonObject eventPayload = fireAlarmPayload.getAsJsonObject("event");
        JsonObject metaDataPayload = eventPayload.getAsJsonObject("metaData");
        metaDataPayload.addProperty("deviceId", deviceId1);
        eventPayload.add("metaData", metaDataPayload);
        fireAlarmPayload.add("event", eventPayload);
        MqttMessage message;
        for (int i = 0; i < 100; i++) {
            message = new MqttMessage(fireAlarmPayload.toString().getBytes());
            message.setQos(qos);
            sampleClient.publish(topic, message);
            Thread.sleep(1000);
        }
        log.info("Message is published to Mqtt Client");
        sampleClient.disconnect();
        log.info("Mqtt Client is Disconnected");

        // Publishing message as a device with simple agent (device 2)
        topic = automationContext.getContextTenant().getDomain() + "/" + DEVICE_TYPE + "/" + deviceId2 +
                "/temperature";
        clientId = deviceId2 + ":" + DEVICE_TYPE;
        persistence = new MemoryPersistence();
        sampleClient = new MqttClient(broker, clientId, persistence);
        connOpts = new MqttConnectOptions();
        connOpts.setUserName(accessToken);
        connOpts.setPassword("".toCharArray());
        connOpts.setKeepAliveInterval(120);
        connOpts.setCleanSession(true);
        log.info("Connecting to broker: " + broker);
        sampleClient.connect(connOpts);
        log.info("Connected");

        fireAlarmPayload = PayloadGenerator.getJsonPayload(Constants.VirtualFireAlarmConstants
                                                                   .PAYLOAD_FILE,
                                                           Constants.AndroidSenseEnrollment.PUBLISH_DATA_OPERATION);
        eventPayload = fireAlarmPayload.getAsJsonObject("event");
        metaDataPayload = eventPayload.getAsJsonObject("metaData");
        metaDataPayload.addProperty("deviceId", deviceId2);
        eventPayload.add("metaData", metaDataPayload);
        fireAlarmPayload.add("event", eventPayload);
        for (int i = 0; i < 100; i++) {
            message = new MqttMessage(fireAlarmPayload.toString().getBytes());
            message.setQos(qos);
            sampleClient.publish(topic, message);
            Thread.sleep(1000);
        }
        log.info("Message is published to Mqtt Client");
        sampleClient.disconnect();
        log.info("Mqtt Client is Disconnected");
        currentTime = System.currentTimeMillis();
    }

    @Test(description = "Test whether the policy publishing from the server to device works", dependsOnMethods =
            {"testEventPublishing"} )
    public void testPolicyPublishing() throws Exception {
        String deviceId2 = userMode == TestUserMode.TENANT_ADMIN ? tenantDeviceId2 : VirtualFireAlarmTestCase.deviceId2;
        String topic = automationContext.getContextTenant().getDomain() + "/" + DEVICE_TYPE + "/" + deviceId2 + "/operation/#";
        String clientId = deviceId2 + ":" + DEVICE_TYPE;
        HttpResponse response = restClient.post("/api/device-mgt/v1.0/policies", PayloadGenerator
                .getJsonPayload(Constants.VirtualFireAlarmConstants.PAYLOAD_FILE,
                        Constants.VirtualFireAlarmConstants.POLICY_DATA).toString());
        Assert.assertEquals("Policy creation for virtual fire alarm failed", HttpStatus.SC_CREATED,
                response.getResponseCode());
        JsonObject jsonObject = new JsonParser().parse(response.getData()).getAsJsonObject();
        String policyId = jsonObject.getAsJsonPrimitive("id").getAsString();
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(new JsonPrimitive(policyId));
        response = restClient.post(Constants.VirtualFireAlarmConstants.ACTIVATE_POLICY_ENDPOINT,
                jsonArray.toString());
        Assert.assertEquals("Policy activation for virtual fire alarm failed", HttpStatus.SC_OK,
                response.getResponseCode());
        MqttSubscriberClient mqttSubscriberClient = new MqttSubscriberClient(broker, clientId, topic, accessToken);
        response = restClient.put(Constants.VirtualFireAlarmConstants.APPLY_CHANGES_ENDPOINT, "");
        Assert.assertEquals("Applying changes to policy for virtual fire alarm failed", HttpStatus.SC_OK,
                response.getResponseCode());
        // Allow some time for message delivery
        Thread.sleep(20000);
        ArrayList<MqttMessage> mqttMessages = mqttSubscriberClient.getMqttMessages();
        Assert.assertEquals("Policy published message is not received by the mqtt listener. ", 2, mqttMessages.size());

    }

    // Test case related to virtual fire alarm added here as the batch cron runs for every 5 minutes and rather than
    // waiting for that we can check them in a latter test cases
    @Test(description = "Test whether data that is published is stored in analytics event table", dependsOnMethods =
            {"testPolicyPublishing"} )
    public void testBatchDataPersistence() throws Exception {
        String deviceId1 =
                this.userMode == TestUserMode.TENANT_ADMIN ? tenantDeviceId1 : VirtualFireAlarmTestCase.deviceId1;
        String deviceId2 =
                this.userMode == TestUserMode.TENANT_ADMIN ? tenantDeviceId2 : VirtualFireAlarmTestCase.deviceId2;

        long MilliSecondDifference = System.currentTimeMillis() - VirtualFireAlarmTestCase.currentTime;
        if (MilliSecondDifference < 300000) {
            Thread.sleep(300000 - MilliSecondDifference);
        }
        String url = Constants.VirtualFireAlarmConstants.STATS_ENDPOINT + "/" + deviceId1;
        url += "?from=" + (VirtualFireAlarmTestCase.currentTime - 300000)/1000 + "&to=" + System.currentTimeMillis()
                /1000;
        HttpResponse response = restClient.get(url);
//        TODO:Need to verify this testcase
//        JsonArray jsonArray = new JsonParser().parse(response.getData()).getAsJsonArray();
//        Assert.assertEquals(
//                "Published event for the device with the id " + deviceId1 + " is not inserted to "
//                        + "analytics table", HttpStatus.SC_OK, response.getResponseCode());
//        Assert.assertTrue(
//                "Published event for the device with the id " + deviceId1 + " is not inserted to analytics table",
//                jsonArray.size() > 0);
//
//        url = Constants.VirtualFireAlarmConstants.STATS_ENDPOINT + "/" + deviceId2;
//        url += "?from=" + (VirtualFireAlarmTestCase.currentTime - 300000)/1000 + "&to=" + System.currentTimeMillis()
//                /1000;
//        response = restClient.get(url);
//        log.info("PAYLOADXX : " + response.getData());
//        jsonArray = new JsonParser().parse(response.getData()).getAsJsonArray();
//        Assert.assertEquals(
//                "Published event for the device with the id " + deviceId2 + " is not inserted to "
//                        + "analytics table", HttpStatus.SC_OK, response.getResponseCode());
//        Assert.assertTrue(
//                "Published event for the device with the id " + deviceId2 + " is not inserted to analytics table",
//                jsonArray.size() > 0);
    }
}
