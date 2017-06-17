/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.iot.integration.jmeter;

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
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.extensions.jmeter.JMeterTest;
import org.wso2.carbon.automation.extensions.jmeter.JMeterTestManager;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.iot.integration.common.Constants;
import org.wso2.iot.integration.common.MqttSubscriberClient;
import org.wso2.iot.integration.common.PayloadGenerator;
import org.wso2.iot.integration.common.RestClient;
import org.wso2.iot.integration.common.TestBase;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

/**
 * This test case is used to run the jmeter test cases related with DeviceType Management APIs.
 */
public class DeviceTypeManagementJMeterTestCase extends TestBase {
    private static Log log = LogFactory.getLog(DeviceTypeManagementJMeterTestCase.class);
    private String broker = "tcp://localhost:1886";
    private RestClient restClient;

    @BeforeClass(alwaysRun = true)
    public void initTest() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        restClient = new RestClient(backendHTTPSURL, Constants.APPLICATION_JSON, accessTokenString);

    }
    @Test(description = "This test case tests the Device Type/Device Management/Device Agent APIs")
    public void DeviceTypeManagementTest() throws AutomationFrameworkException {
        URL url = Thread.currentThread().getContextClassLoader()
                .getResource("jmeter-scripts" + File.separator + "DeviceTypeManagementAPI.jmx");
        JMeterTest script = new JMeterTest(new File(url.getPath()));
        JMeterTestManager manager = new JMeterTestManager();
        log.info("Running Device Type/Device Management/Device Agent management api test cases using jmeter scripts");
        manager.runTest(script);
        log.info("Completed DeviceType API service test using jmeter scripts");
    }

    @Test(description = "Test whether the policy publishing from the server to device works", dependsOnMethods =
            {"DeviceTypeManagementTest"} )
    public void testMqttFlow() throws Exception {
        String deviceId = "123422578912";
        String deviceType = "firealarmmqtt";
        String payload = "{\"deviceIdentifiers\":[123422578912],\"operation\":{\"code\":\"ring\",\"type\":\"CONFIG\"," +
                "\"payLoad\":\"volume:30%\"}}";
        String topic = automationContext.getContextTenant().getDomain() + "/"+deviceType+"/" + deviceId + "/operation/#";
        String clientId = deviceId + ":firealarmmqtt";
        MqttSubscriberClient mqttDeviceSubscriberClient = new MqttSubscriberClient(broker, clientId, topic, accessToken);
        restClient.post("/api/device-mgt/v1.0/devices/" + deviceType + "/operations", payload);

        // Allow some time for message delivery
        Thread.sleep(10000);
        ArrayList<MqttMessage> mqttMessages = mqttDeviceSubscriberClient.getMqttMessages();
        Assert.assertEquals("listener did not recieve mqtt messages ", 1, mqttMessages.size());

        String topicPub = automationContext.getContextTenant().getDomain() + "/"+deviceType+"/"+deviceId+"/events";
        int qos = 2;
        String clientIdPub = deviceId + ":firealarmmqttpub";
        MemoryPersistence persistence = new MemoryPersistence();
        MqttClient sampleClient = new MqttClient(broker, clientIdPub, persistence);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setUserName(accessToken);
        connOpts.setPassword("".toCharArray());
        connOpts.setKeepAliveInterval(120);
        connOpts.setCleanSession(false);
        log.info("Connecting to broker: " + broker);
        sampleClient.connect(connOpts);
        log.info("Connected");
        payload = "{\"temperature\":10,\"status\":\"workingh\",\"humidity\":20}";
        String payload2 = "{\"temperature\":100,\"status\":\"workingh\",\"humidity\":20}";
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(qos);
        MqttMessage message2 = new MqttMessage(payload2.getBytes());
        message.setQos(qos);
        sampleClient.publish(topicPub, message2);
        sampleClient.publish(topicPub, message2);
        log.info("Message is published to Mqtt Client");
        sampleClient.disconnect();
        log.info("Mqtt Client is Disconnected");
        // Allow some time for message delivery
        Thread.sleep(20000);
        HttpResponse response = restClient.get("/api/device-mgt/v1.0/events/last-known/"+deviceType+"/"+deviceId);
        Assert.assertEquals("No published event found (mqtt)", HttpStatus.SC_OK,
                            response.getResponseCode());
        Assert.assertTrue("Event count does not match published event count, " + response.getData() ,
                          response.getData().contains("\"count\":2"));

    }
}
