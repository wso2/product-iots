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

var utility;
utility = function () {
    var JavaClass = Packages.java.lang.Class;
    var PrivilegedCarbonContext = Packages.org.wso2.carbon.context.PrivilegedCarbonContext;

    var getOsgiService = function (className) {
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getOSGiService(JavaClass.forName(className));
    };

    var publicMethods = {};

    publicMethods.getDeviceManagementService = function () {
        return getOsgiService('org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService');
    };

    publicMethods.getPolicyManagementService = function () {
        return getOsgiService('org.wso2.carbon.policy.mgt.core.PolicyManagerService');
    };

    publicMethods.insertAppPermissions = function (userModule, type) {
        userModule.addPermissions([{key: "device-mgt", name: "Device Management"}], "", type);
        userModule.addPermissions([{
            key: "admin",
            name: "Device Management Admin"
        }], "device-mgt", type);
        userModule.addPermissions([{
            key: "user",
            name: "Device Management User"
        }], "device-mgt", type);

        userModule.addPermissions([{key: "devices", name: "Devices"}], "device-mgt/admin", type);
        userModule.addPermissions([{
            key: "devices/list",
            name: "List Devices"
        }], "device-mgt/admin", type);
        userModule.addPermissions([{
            key: "devices/operation",
            name: "Perform Operation"
        }], "device-mgt/admin", type);

        userModule.addPermissions([{key: "groups", name: "Groups"}], "device-mgt/admin", type);
        userModule.addPermissions([{
            key: "groups/add_devices",
            name: "Add Devices to Group"
        }], "device-mgt/admin", type);
        userModule.addPermissions([{
            key: "groups/remove_devices",
            name: "Remove Devices from Group"
        }], "device-mgt/admin", type);
        userModule.addPermissions([{
            key: "groups/modify",
            name: "Modify Group"
        }], "device-mgt/admin", type);
        userModule.addPermissions([{
            key: "groups/share",
            name: "Change Group Sharing"
        }], "device-mgt/admin", type);
        userModule.addPermissions([{
            key: "groups/delete",
            name: "Delete Group"
        }], "device-mgt/admin", type);

        userModule.addPermissions([{key: "users", name: "Users"}], "device-mgt/admin", type);
        userModule.addPermissions([{
            key: "users/add",
            name: "Add New Users"
        }], "device-mgt/admin", type);
        userModule.addPermissions([{
            key: "users/invite",
            name: "Invite Users"
        }], "device-mgt/admin", type);
        userModule.addPermissions([{
            key: "users/list",
            name: "List Users"
        }], "device-mgt/admin", type);
        userModule.addPermissions([{
            key: "users/remove",
            name: "Remove Users"
        }], "device-mgt/admin", type);

        userModule.addPermissions([{key: "devices", name: "Devices"}], "device-mgt/user", type);
        userModule.addPermissions([{
            key: "devices/list",
            name: "List Devices"
        }], "device-mgt/user", type);
        userModule.addPermissions([{
            key: "devices/operation",
            name: "Perform Operation"
        }], "device-mgt/user", "init");

        userModule.addPermissions([{key: "groups", name: "Groups"}], "device-mgt/user", type);
        userModule.addPermissions([{
            key: "groups/monitor",
            name: "Monitor Devices in Group"
        }], "device-mgt/user", type);
        userModule.addPermissions([{
            key: "groups/operation",
            name: "Perform Device Operation"
        }], "device-mgt/user", type);
        userModule.addPermissions([{
            key: "groups/list",
            name: "List Groups of User"
        }], "device-mgt/user", type);
    };

    return publicMethods;
}();


