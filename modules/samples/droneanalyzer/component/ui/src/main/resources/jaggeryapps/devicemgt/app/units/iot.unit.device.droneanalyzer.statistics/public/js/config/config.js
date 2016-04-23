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

var config_api = function () {
    var config_api = this;
    var context_controller = "/drone_analyzer/controller/send_command";
    config_api.config_3dobject_holder = "#virtualDrone";
    config_api.realtime_plotting_update_interval = 30;
    config_api.realtime_plotting_totalPoints = 30;
    config_api.realtime_plotting_data_window = {};
    config_api.effectController = {uy: 70.0, uz: 15.0, ux: 10.0, fx: 2.0, fz: 15.0, Tmax: 1};
    config_api.drone_control = context_controller;
    config_api.drone_controlType = "POST";
    config_api.drone_controlDataType = "json";
    config_api.web_socket_endpoint = "/drone_analyzer/datastream/drone_status";
    config_api.modules_status = {
        "realtimePlotting": false,
        "sensorReadings": false,
        "angleOfRotation_2": false,
        "angleOfRotation_1": false
    };
};