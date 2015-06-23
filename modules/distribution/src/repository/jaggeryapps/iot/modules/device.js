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

var deviceModule;
var carbon = require('carbon');
var carbonHttpServletTransport = carbon.server.address('http');
var carbonHttpsServletTransport = carbon.server.address('https');

deviceModule = function () {
    var log = new Log("modules/device.js");

    var constants = require("/modules/constants.js");
    var utility = require("/modules/utility.js").utility;

    var publicMethods = {};
    var privateMethods = {};

    publicMethods.getAllDevicesTypes = function(){
        //URL: https://localhost:9443/devicecloud/manager/devices/username/{username}
        deviceCloudService = carbonHttpsServletTransport + "/devicecloud/manager";
        listAllDeviceTypesEndPoint = deviceCloudService + "/devices/types";

        var data = {};
        //XMLHTTPRequest's GET
        return get(listAllDeviceTypesEndPoint, data, "json");
    };

}();