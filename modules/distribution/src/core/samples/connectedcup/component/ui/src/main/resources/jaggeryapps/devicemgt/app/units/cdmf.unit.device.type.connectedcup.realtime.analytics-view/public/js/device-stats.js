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

var wsConnectionTemperature;
var wsConnectionCoffeeLevel;
var graphForSensorTypeTemperature;
var graphForSensorTypeCoffeeLevel;
var chartDataSensorTypeTemperature = [];
var chartDataSensorTypeCoffeeLevel = [];

var palette = new Rickshaw.Color.Palette({scheme: "classic9"});

$(window).load(function () {
    drawGraph(wsConnectionTemperature, "#div-chartTemperature", "y_axisTemperature", "chartTemperature", chartDataSensorTypeTemperature
        , graphForSensorTypeTemperature);
    var millisecondsToWait = 1000;
    setTimeout(function() {
        drawGraph(wsConnectionCoffeeLevel, "#div-chartCoffeeLevel", "y_axisCoffeeLevel", "chartCoffeeLevel", chartDataSensorTypeCoffeeLevel
            , graphForSensorTypeCoffeeLevel);
    }, millisecondsToWait);
});

window.onbeforeunload = function() {
    disconnect(wsConnectionTemperature);
    disconnect(wsConnectionCoffeeLevel);
};

function drawGraph(wsConnection, placeHolder, yAxis, chat, chartData, graph) {
    var tNow = new Date().getTime() / 1000;
    for (var i = 0; i < 30; i++) {
        chartData.push({
            x: tNow - (30 - i) * 15,
            y: parseFloat(0)
        });
    }

    graph = new Rickshaw.Graph({
        element: document.getElementById(chat),
        width: $(placeHolder).width() - 50,
        height: 300,
        renderer: "line",
        interpolation: "linear",
        padding: {top: 0.2, left: 0.0, right: 0.0, bottom: 0.2},
        xScale: d3.time.scale(),
        series: [{
            'color': palette.color(),
            'data': chartData,
            'name': "SensorValue"
        }]
    });

    graph.render();

    var xAxis = new Rickshaw.Graph.Axis.Time({
        graph: graph
    });

    xAxis.render();

    new Rickshaw.Graph.Axis.Y({
        graph: graph,
        orientation: 'left',
        height: 300,
        tickFormat: Rickshaw.Fixtures.Number.formatKMBT,
        element: document.getElementById(yAxis)
    });

    new Rickshaw.Graph.HoverDetail({
        graph: graph,
        formatter: function (series, x, y) {
            var date = '<span class="date">' + moment.unix(x * 1000).format('Do MMM YYYY h:mm:ss a') + '</span>';
            var swatch = '<span class="detail_swatch" style="background-color: ' + series.color + '"></span>';
            return swatch + series.name + ": " + parseInt(y) + '<br>' + date;
        }
    });

    var sensorType = $(placeHolder).attr("data-sensorType");
    var websocketUrl = $(placeHolder).attr("data-websocketurl");
    connect(wsConnection, websocketUrl, chartData, graph, sensorType);

}

//websocket connection
function connect(wsConnection, target, chartData, graph, sensorType) {
    if ('WebSocket' in window) {
        wsConnection = new WebSocket(target);
    } else if ('MozWebSocket' in window) {
        wsConnection = new MozWebSocket(target);
    } else {
        console.log('WebSocket is not supported by this browser.');
    }
    if (wsConnection) {
        wsConnection.onmessage = function (event) {
            var dataPoint = JSON.parse(event.data);
            chartData.push({
               x: parseInt(dataPoint[4]) / 1000,
               y: parseFloat(dataPoint[5])
            });
            chartData.shift();
            graph.update();
        };
        wsConnection.onerror = function (event) {
            var websocketURL = event.currentTarget.url;
            websocketURL = websocketURL.replace("wss://","https://");
            var uriParts = websocketURL.split("/");
            websocketURL = uriParts[0] + "//" + uriParts[2];
            var errorMsg = $("#websocker-onerror").html();
            errorMsg = errorMsg.replace(new RegExp('\\$sensorType', 'g'), sensorType);
            errorMsg = errorMsg.replace(new RegExp('\\$webSocketURL', 'g'), websocketURL);
            $(graph.element).parent().html("<div class='alert alert-danger'>" + errorMsg + "</div>");
            $(graph.element).hide();
        };
    }
    if (sensorType == "temperature") {
        wsConnectionTemperature = wsConnection;
    } else {
        wsConnectionCoffeeLevel = wsConnection;
    }
}

function disconnect(wsConnection) {
    if (wsConnection != null) {
        wsConnection.close();
        wsConnection = null;
    }
}
