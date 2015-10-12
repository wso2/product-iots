/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.wso2.carbon.device.mgt.iot.sample.firealarm.plugin.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.iot.common.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.sample.firealarm.plugin.constants.FireAlarmConstants;
import org.wso2.carbon.policy.mgt.common.Policy;
import org.wso2.carbon.policy.mgt.common.monitor.ComplianceData;
import org.wso2.carbon.policy.mgt.common.monitor.ComplianceFeature;
import org.wso2.carbon.policy.mgt.common.monitor.PolicyComplianceException;
import org.wso2.carbon.policy.mgt.common.spi.PolicyMonitoringService;
import org.wso2.carbon.device.mgt.iot.common.transport.mqtt.MqttPublisher;

import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class FireAlarmPolicyMonitoringService implements PolicyMonitoringService {

    private static Log log = LogFactory.getLog(FireAlarmPolicyMonitoringService.class);

    @Override
    public void notifyDevices(List<Device> list) throws PolicyComplianceException {
        String userName = CarbonContext.getThreadLocalCarbonContext().getUsername();
        MqttPublisher mqttPublisher = new MqttPublisher();
        try {
            mqttPublisher.initControlQueue();
            mqttPublisher.publish("Raspberry-Policy-sender",
                                  "/iot/policymgt/govern/" + FireAlarmConstants.DEVICE_TYPE + "/"
                                          + userName, userName.getBytes(UTF_8));
        } catch (DeviceControllerException e) {
            log.error("Error on notifying "+FireAlarmConstants.DEVICE_TYPE+" devices, message not sent.");
        }
    }

    @Override
    public ComplianceData checkPolicyCompliance(DeviceIdentifier deviceIdentifier, Policy policy,
                                                Object compliancePayload) throws PolicyComplianceException {
        if (log.isDebugEnabled()) {
            log.debug("Checking policy compliance status of device '" + deviceIdentifier.getId() + "'");
        }
        ComplianceData complianceData = new ComplianceData();
        if (compliancePayload == null || policy == null) {
            return complianceData;
        }
        List<ComplianceFeature> complianceFeatures = new ArrayList<ComplianceFeature>();

        // Parsing json string to get compliance features.
        JsonElement jsonElement;
        if (compliancePayload instanceof String) {
            jsonElement = new JsonParser().parse((String) compliancePayload);
        } else {
            throw new PolicyComplianceException("Invalid policy compliance payload");
        }
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        Gson gson = new Gson();
        ComplianceFeature complianceFeature;

        for (JsonElement element : jsonArray) {
            complianceFeature = gson.fromJson(element, ComplianceFeature.class);
            complianceFeatures.add(complianceFeature);
        }

        complianceData.setComplianceFeatures(complianceFeatures);

        for (ComplianceFeature cf : complianceFeatures) {
            if (!cf.isCompliant()) {
                complianceData.setStatus(false);
                break;
            }
        }
        return complianceData;
    }

    @Override
    public String getType() {
        return FireAlarmConstants.DEVICE_TYPE;
    }
}