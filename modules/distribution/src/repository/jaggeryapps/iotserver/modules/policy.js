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

    var userManagementService = utility.getUserManagementService();

    var publicMethods = {};
    var privateMethods = {};

    publicMethods.getPolicies = function () {

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


