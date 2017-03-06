/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

$(document).ready(function () {
    invokerUtil.get(
        "/devicemgt_admin/configuration",
        function (data) {
            data = JSON.parse(data);
            if (data && data.configuration) {
                for (var i = 0; i < data.configuration.length; i++) {
                    var config = data.configuration[i];
                    if (config.name == "GEO_LOCATION_TRACKER_HTTP_EP") {
                        $("input #http-endpoint").val(config.value);
                    } else if (config.name == "GEO_LOCATION_TRACKER_HTTPS_EP") {
                        $("input #https-endpoint").val(config.value);
                    } else if (config.name == "GEO_LOCATION_TRACKER_MQTT_EP") {
                        $("input #mqtt-endpoint").val(config.value);
                    }
                }
            }
        }, function (data) {
            console.log(data);
        });
});


// Start of HTML embedded invoke methods
var showAdvanceOperation = function (operation, button) {
    $(button).addClass('selected');
    $(button).siblings().removeClass('selected');
    var hiddenOperation = ".wr-hidden-operations-content > div";
    $(hiddenOperation + '[data-operation="' + operation + '"]').show();
    $(hiddenOperation + '[data-operation="' + operation + '"]').siblings().hide();
};

// Start of HTML embedded invoke methods
var addConfiguration = function () {
    var errorMsgWrapper = "#geolocationTracker-config-error-msg";
    var errorMsg = "#geolocationTracker-config-error-msg span";
    var addConfigFormData = {};
    var configList = new Array();
    var httpEp = $("input#http-endpoint").val();
    var httpConfig = {
        "name": "GEO_LOCATION_TRACKER_HTTP_EP",
        "value": String(httpEp),
        "contentType": "text"
    };

    var httpsEp = $("input#https-endpoint").val();
    var httpsConfig = {
        "name": "GEO_LOCATION_TRACKER_HTTPS_EP",
        "value": String(httpsEp),
        "contentType": "text"
    };

    var mqttEp = $("input#mqtt-endpoint").val();
    var mqttConfig = {
        "name": "GEO_LOCATION_TRACKER_MQTT_EP",
        "value": String(mqttEp),
        "contentType": "text"
    };

    configList.push(httpConfig);
    configList.push(httpsConfig);
    configList.push(mqttConfig);
    addConfigFormData.type = "geolocationTracker"
    addConfigFormData.configuration = configList;

    var addConfigAPI = "/devicemgt_admin/configuration";
    invokerUtil.post(
        addConfigAPI,
        addConfigFormData,
        function (data) {
            data = JSON.parse(data);
            if (data.statusCode == 201) {
                $("#config-save-form").addClass("hidden");
                $("#record-created-msg").removeClass("hidden");
            } else if (data == 500) {
                $(errorMsg).text("Exception occurred at backend.");
            } else if (data == 403) {
                $(errorMsg).text("Action was not permitted.");
            } else {
                $(errorMsg).text("An unexpected error occurred.");
            }

            $(errorMsgWrapper).removeClass("hidden");
        }, function (data) {
            data = data.status;
            if (data == 500) {
                $(errorMsg).text("Exception occurred at backend.");
            } else if (data == 403) {
                $(errorMsg).text("Action was not permitted.");
            } else {
                $(errorMsg).text("An unexpected error occurred.");
            }
            $(errorMsgWrapper).removeClass("hidden");
        }
    );
};
