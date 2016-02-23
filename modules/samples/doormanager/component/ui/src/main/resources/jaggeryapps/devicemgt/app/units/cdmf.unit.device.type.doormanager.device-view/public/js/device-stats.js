/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


var deviceType = $("#details").data("devicetype");
var deviceId = $(".device-id").data("deviceid");
var monitor_operations = $("#details").data("monitor");
var appContext = $("#details").data("appcontext");
var publicURL =$("#details").data("unitpublicuri");

$(window).on('resize', function () {
        location.reload(false);
});

$(document).ready(function () {
    updateGraphs();
});

function updateGraphs() {
    var tv = 2000;
    var iv = setInterval(function () {
        var getStatsRequest = $.ajax({
            url: appContext + "/api/operations/" + deviceType + "/stats?deviceId=" + deviceId,
            method: "get"
        });
        getStatsRequest.done(function (data) {
            var data = data.data;
            if(data[0].door_locker_state){
               var currentStateOfTheLock=data[0].door_locker_state;
                console.log(currentStateOfTheLock);
                if( currentStateOfTheLock == "UNLOCKED"){
                    $("#lockerCurrentState").attr("src", publicURL+"/images/unlock.png");
                }else{
                    $("#lockerCurrentState").attr("src", publicURL+"/images/lock.png");
                }
            }else{
                console.log("Backend server not available");
            }
        });
    }, tv);
}