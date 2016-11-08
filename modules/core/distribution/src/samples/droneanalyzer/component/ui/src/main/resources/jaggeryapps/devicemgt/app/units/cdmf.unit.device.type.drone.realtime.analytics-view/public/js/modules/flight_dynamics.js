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

var flight_dynamics = function () {
    var api = this;
    api.processingMessage = function (sender_message) {
        if(sender_message.basicParam.battery_level!= undefined){

            $("#battery_level_holder").width( parseInt(sender_message.basicParam.battery_level)+"%" );
            $("#battery_level").html(sender_message.basicParam.battery_level+"%");
        }
        if(sender_message.basicParam.battery_voltage!= undefined){

            $("#battery_voltage_holder").width( parseInt(sender_message.basicParam.battery_voltage)+"%" );
            console.log(sender_message.basicParam.battery_voltage);
            $("#battery_voltage").html(sender_message.basicParam.battery_voltage+"%");
        }
        if (sender_message.quatanium_val != undefined) {
            current_status = object_maker.get_heading_attitude_bank(sender_message.quatanium_val);
            object_maker.set_heading_attitude_bank(current_status);
            $("#imageTop").animate({rotate: '' + (180 / Math.PI) * 2.890456 + 'deg'}, 2);
        }
        if (config_api.modules_status.angleOfRotation_2 || config_api.modules_status.angleOfRotation_1) {
            object_maker.set_bank("#imageTop", current_status.bank);
            object_maker.set_heading("#imageBackSecond", current_status.heading);

        }
        if (config_api.modules_status.realtimePlotting) {
            if (current_status[$('#plotting_attribute').val()] != undefined) {
                plotting.pushData(current_status[$('#plotting_attribute').val()]);
            }
        }
        if (sender_message.basicParam != undefined) {
            if (sender_message.basicParam.velocity != undefined) {
                var velocity = sender_message.basicParam.velocity;
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
            if (sender_message.basicParam.global_location != undefined) {
                var location = sender_message.basicParam.global_location;
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
        if (sender_message.battery_voltage != undefined) {
            $("#battery_voltage").html(sender_message.battery_voltage);
        } else {
            $("#battery_voltage").html(NaN);
        }
    }

};


