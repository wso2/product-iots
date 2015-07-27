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

    publicMethods.addPolicy = function (name, deviceType, policyDefinition) {
        var carbonModule = require("carbon");
        var carbonServer = application.get("carbonServer");
        var options = {system: true};
        var carbonUser = session.get(constants.USER_SESSION_KEY);

        resource = {
            name : name,
            mediaType: 'text/plain',
            content : policyDefinition
        };

        if (carbonUser) {
            options.tenantId = carbonUser.tenantId;
            var registry = new carbonModule.registry.Registry(carbonServer, options);
            log.info("########### Policy name : "+name);
            log.info("########### Policy type : "+deviceType);
            log.info("########### Policy Declarationsss : "+policyDefinition);
            registry.put("/_system/governance/policy_declarations/" + deviceType + "/" + name, resource);
        }

        var mqttsenderClass = Packages.org.wso2.device.mgt.mqtt.policy.push.MqttPush;
        var mqttsender = new mqttsenderClass();

        var result = mqttsender.pushToMQTT("/iot/policymgt/govern",policyDefinition,"tcp://10.100.0.104:1883","Raspberry-Policy-sender");

        mqttsender = null;
    };

    publicMethods.getPolicies = function () {
        var carbonModule = require("carbon");
        var carbonServer = application.get("carbonServer");
        var options = {system: true};
        var carbonUser = session.get(constants.USER_SESSION_KEY);

        if (carbonUser) {
            options.tenantId = carbonUser.tenantId;
            var registry = new carbonModule.registry.Registry(carbonServer, options);
            log.info(registry.get("/_system/governance/policy_declarations/firealarm/"));
        }

        //TODO-This method returns includes dummy policy data

        var policies = [];
        var policyObj = {
            "id":1,                         // Identifier of the policy.
            "priorityId":1,                 // Priority of the policies. This will be used only for simple evaluation.
            "profile":{},                   // Profile
            "policyName":"Turn off light",  // Name of the policy.
            "generic":true,                 // If true, this should be applied to all related device.
            "roles":{},                     // Roles which this policy should be applied.
            "ownershipType":{},             // Ownership type (COPE, BYOD, CPE)
            "devices":{},                   // Individual devices this policy should be applied
            "users":{},                     // Individual users this policy should be applied
            "Compliance":{},
            "policyCriterias":{},
            "startTime":283468236,          // Start time to apply the policy.
            "endTime":283468236,            // After this time policy will not be applied
            "startDate":"",                 // Start date to apply the policy
            "endDate":"",                   // After this date policy will not be applied.
            "tenantId":-1234,
            "profileId":1
        };

        policies.push(policyObj);

        policyObj = {
            "id":2,                         // Identifier of the policy.
            "priorityId":1,                 // Priority of the policies. This will be used only for simple evaluation.
            "profile":{},                   // Profile
            "policyName":"Turn on Buzzer",  // Name of the policy.
            "generic":false,                 // If true, this should be applied to all related device.
            "roles":{},                     // Roles which this policy should be applied.
            "ownershipType":{},             // Ownership type (COPE, BYOD, CPE)
            "devices":{},                   // Individual devices this policy should be applied
            "users":{},                     // Individual users this policy should be applied
            "Compliance":{},
            "policyCriterias":{},
            "startTime":283468236,          // Start time to apply the policy.
            "endTime":283468236,            // After this time policy will not be applied
            "startDate":"",                 // Start date to apply the policy
            "endDate":"",                   // After this date policy will not be applied.
            "tenantId":-1234,
            "profileId":2
        };

        policies.push(policyObj);
        return policies;
    };

    return publicMethods;
}();


