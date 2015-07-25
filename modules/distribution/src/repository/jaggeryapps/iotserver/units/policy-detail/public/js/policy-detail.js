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

function formatDates(){
    $(".formatDate").each(function(){
        var timeStamp = $(this).html();
        $(this).html(new Date(parseInt(timeStamp)).toUTCString());
    });
}

(function () {
    var deviceId = $(".device-id");
    var deviceIdentifier = deviceId.data("deviceid");
    var deviceType = deviceId.data("type");
    var payload = [deviceIdentifier];
    if (deviceType == "ios") {
        var serviceUrl = "/ios/operation/deviceinfo";
    } else if (deviceType == "android") {
        var serviceUrl = "/mdm-android-agent/operation/device-info";
    }
    invokerUtil.post(serviceUrl, payload,
        function(message){
            console.log(message);
        }, function (message) {
            console.log(message);
        });
    $(document).ready(function(){
        loadOperationBar(deviceType);
        loadMap();
        formatDates();
    });
    function loadMap(){
        var map;
        function initialize() {
            var mapOptions = {
                zoom: 18
            };
            var lat = 6.9098591;
            var long = 79.8523753;
            map = new google.maps.Map(document.getElementById('device-location'),
                mapOptions);

            var pos = new google.maps.LatLng(lat,
                long);
            var marker = new google.maps.Marker({
                position: pos,
                map: map
            });

            map.setCenter(pos);
        }
        google.maps.event.addDomListener(window, 'load', initialize);
    }
}());
