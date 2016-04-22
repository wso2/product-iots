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
$("#module_control button").click(function (index) {
    console.log("Asking Server to send the " + $(this).attr('id') + " command to Ar Drone");
    var url = config_api.drone_control;
    ajax_handler.ajaxRequest(url, config_api.drone_controlType, {action: $(this).attr('id'), speed: 7, duration: 7},
        config_api.drone_controlDataType, function (data, status) {
            console.log(JSON.stringify(data));
        }
    );
});



