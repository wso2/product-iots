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

var ws;
var coffee_amount = 0;

$(window).load(function () {
    var websocketUrl = $("#div-chart").data("websocketurl");
    connect(websocketUrl);
});

$(window).unload(function () {
    disconnect();
});

//websocket connection
function connect(target) {
    if ('WebSocket' in window) {
        ws = new WebSocket(target);
    } else if ('MozWebSocket' in window) {
        ws = new MozWebSocket(target);
    } else {
        console.log('WebSocket is not supported by this browser.');
    }
    if (ws) {
        ws.onmessage = function (event) {
            var dataPoint = JSON.parse(event.data);
            if (dataPoint) {
                updateWaterLevel(dataPoint[4]);
            }
        };
    }
}

function disconnect() {
    if (ws != null) {
        ws.close();
        ws = null;
    }
}

function updateWaterLevel(newValue) {
    var waterLevel = document.getElementById("water");
    waterLevel.innerHTML = (newValue | 0) + "%";
    if (newValue == 0) {
        waterLevel.style.height = (newValue * 3) + 'px';
        waterLevel.style.paddingTop = 0;
    } else {
        waterLevel.style.height = (newValue * 3) - 3 + 'px';
        waterLevel.style.paddingTop = (newValue * 3 / 2.4) - 10 + 'px';
    }
}