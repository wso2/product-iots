/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var policyModule;
policyModule = function () {
    var log = new Log("modules/policy.js");

    var constants = require("/modules/constants.js");
    var utility = require("/modules/utility.js").utility;

    var publicMethods = {};
    var privateMethods = {};

    publicMethods.addPolicy = function (policyName, deviceType, policyDefinition, policyDescription) {
        if (policyName && deviceType) {

            var carbonModule = require("carbon");
            var carbonServer = application.get("carbonServer");
            var options = {system: true};
            var carbonUser = session.get(constants.USER_SESSION_KEY);

            var resource = {
                name: policyName,
                mediaType: 'text/plain',
                content: policyDefinition,
                description: policyDescription
            };

            if (carbonUser) {
                options.tenantId = carbonUser.tenantId;
                var registry = new carbonModule.registry.Registry(carbonServer, options);
                log.info("########### Policy name : " + policyName);
                log.info("########### Policy type : " + deviceType);
                log.info("########### Policy Declaration : " + policyDefinition);
                log.info("########### Policy policyDescription: " + policyDescription);
                registry.put(constants.POLICY_REGISTRY_PATH + deviceType + "/" + policyName, resource);
            }

            var mqttsenderClass = Packages.org.wso2.device.mgt.mqtt.policy.push.MqttPush;
            var mqttsender = new mqttsenderClass();

            var result = mqttsender.pushToMQTT("/iot/policymgt/govern", policyDefinition, "tcp://10.100.0.104:1883", "Raspberry-Policy-sender");

            mqttsender = null;

            return true;

        } else {
            return false;
        }
    };

    publicMethods.getPolicies = function () {
        var carbonModule = require("carbon");
        var carbonServer = application.get("carbonServer");
        var options = {system: true};
        var carbonUser = session.get(constants.USER_SESSION_KEY);

        var policies = [];

        if (carbonUser) {
            options.tenantId = carbonUser.tenantId;
            var registry = new carbonModule.registry.Registry(carbonServer, options);
            var allPolicies = registry.get(constants.POLICY_REGISTRY_PATH);

            if (allPolicies) {

                //loop through all device types
                for (var i = 0; i < allPolicies.content.length; i++) {
                    log.info("Policies for device types: " + allPolicies.content);
                    log.info("");
                    var deviceType = allPolicies.content[i].replace(constants.POLICY_REGISTRY_PATH, "");
                    log.info("##### deviceType:" + deviceType);
                    var deviceTypePolicies = registry.get(allPolicies.content[i]);

                    //loop through policies
                    for (var j = 0; j < deviceTypePolicies.content.length; j++) {
                        log.info("Policies:" + deviceTypePolicies.content);
                        var deviceTypePolicy = registry.get(deviceTypePolicies.content[j]);
                        log.info(deviceTypePolicy);
                        var policyObj = {
                            "id": deviceTypePolicy.uuid,                         // Identifier of the policy.
                            //"priorityId": 1,                 // Priority of the policies. This will be used only for simple evaluation.
                            //"profile": {},                   // Profile
                            "policyName": deviceTypePolicy.name,  // Name of the policy.
                            "updated": deviceTypePolicy.updated.time,
                            "deviceType": deviceType
                            //"generic": true,                 // If true, this should be applied to all related device.
                            //"roles": {},                     // Roles which this policy should be applied.
                            //"ownershipType": {},             // Ownership type (COPE, BYOD, CPE)
                            //"devices": {},                   // Individual devices this policy should be applied
                            //"users": {},                     // Individual users this policy should be applied
                            //"Compliance": {},
                            //"policyCriterias": {},
                            //"startTime": 283468236,          // Start time to apply the policy.
                            //"endTime": 283468236,            // After this time policy will not be applied
                            //"startDate": "",                 // Start date to apply the policy
                            //"endDate": "",                   // After this date policy will not be applied.
                            //"tenantId": -1234,
                            //"profileId": 1
                        };

                        policies.push(policyObj);
                        log.info("");
                    }//end of policy loop
                    log.info("");
                }//end of device type policy loop
            }
        }

        return policies;

    };

    publicMethods.removePolicy = function (name, deviceType) {
        var carbonModule = require("carbon");
        var carbonServer = application.get("carbonServer");
        var options = {system: true};
        var carbonUser = session.get(constants.USER_SESSION_KEY);
        var bool = false;

        if (carbonUser) {
            options.tenantId = carbonUser.tenantId;
            var registry = new carbonModule.registry.Registry(carbonServer, options);
            log.info("########### Policy name : " + name);
            log.info("########### Policy type : " + deviceType);
            try {
                registry.remove(constants.POLICY_REGISTRY_PATH + deviceType + "/" + name);
                bool = true;
            } catch (err) {
                log.error("Error while trying to remove policy :" + name);
            }
        }

        return bool;
    };

    return publicMethods;
}();


