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

var flightDynamics = function () {
    var api = this;
    api.processingMessage = function (droneStats) {
        if (droneStats.basicParam.battery_level != undefined) {
            $("#batteryLevelPlaceholder").width(parseInt(droneStats.basicParam.battery_level) + "%");
            $("#batteryLevel").html(droneStats.basicParam.battery_level + "%");
        }
        if (droneStats.basicParam.battery_voltage != undefined) {
            $("#batteryVoltagePlaceholder").width(parseInt(droneStats.basicParam.battery_voltage) + "%");
            $("#batteryVoltage").html(droneStats.basicParam.battery_voltage + "%");
        }
        if (droneStats.quatanium_val != undefined) {
            currentStatus = droneRender.getHeadingAttitudeAndBank(droneStats.quatanium_val);
            droneRender.setHeadingAttitudeAndBank(currentStatus);
            $("#imageTop").animate({rotate: '' + (180 / Math.PI) * 2.890456 + 'deg'}, 2);
        }
        if (config.modulesStatus.angleOfRotationv1 || config.modulesStatus.angleOfRotationv2) {
            droneRender.setBank("#imageTop", currentStatus.bank);
            droneRender.setHeading("#imageBackSecond", currentStatus.heading);
        }
        if (config.modulesStatus.realtimePlotting) {
            if (currentStatus[$('#plottingAttribute').val()] != undefined) {
                plotting.pushData(currentStatus[$('#plottingAttribute').val()]);
            }
        }
        if (droneStats.basicParam != undefined) {
            if (droneStats.basicParam.velocity != undefined) {
                var velocity = droneStats.basicParam.velocity;
                if (velocity.length == 3) {
                    $("#velocityx").html(velocity[0]);
                    $("#velocityy").html(velocity[1]);
                    $("#velocityz").html(velocity[2]);
                }
            } else {
                $("#velocityx").html(NaN);
                $("#velocityy").html(NaN);
                $("#velocityz").html(NaN);
            }
            if (droneStats.basicParam.global_location != undefined) {
                var location = droneStats.basicParam.global_location;
                if (location.length == 3) {
                    $("#locationLog").html(location[0]);
                    $("#locationAlt").html(location[1]);
                    $("#locationLat").html(location[2]);
                }
            } else {
                $("#locationLog").html(NaN);
                $("#locationAlt").html(NaN);
                $("#locationLat").html(NaN);
            }
        }
    }
};


