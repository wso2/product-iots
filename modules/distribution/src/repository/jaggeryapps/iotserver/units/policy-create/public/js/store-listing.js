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

/*
 * Setting-up global variables.
 */
var assetContainer = "#ast-container";

function loadDeviceTypes() {
    var deviceListing = $("#store-listing");
    var deviceListingSrc = deviceListing.attr("src");

    $.template("store-listing", deviceListingSrc, function (template) {
        var serviceURL = "/iotserver/api/devices/types";

        var successCallback = function (data) {
            var viewModel = {};
            var deviceTypes = JSON.parse(data);
            if (!deviceTypes) return;

            for (var i = 0; i < deviceTypes.length; i++) {
                var deviceType = deviceTypes[i];

                //setting defaults
                var storeTitle = deviceType.name;
                var storeDescription = "Connect your " + deviceType.name + " into the WSO2 Device Cloud Platform.";

                if (deviceType.storeTitle != null) {
                    storeTitle = deviceType.storeTitle;
                }

                if (deviceType.storeDescription != null) {
                    storeDescription = deviceType.storeDescription;
                }

                deviceTypes[i].storeTitle = storeTitle;
                deviceTypes[i].storeDescription = storeDescription;
            }

            viewModel.deviceTypes = deviceTypes;

            if (data.length == 0) {
                $("#ast-container").html("No Devices found");
            } else {
                var content = template(viewModel);
                $("#ast-container").html(content);
                initStepper(".wizard-stepper");
            }
        };
        invokerUtil.get(serviceURL,
            successCallback, function (message) {
                console.log(message);
            });
    });
}

$(document).ready(function () {
    loadDeviceTypes();
});