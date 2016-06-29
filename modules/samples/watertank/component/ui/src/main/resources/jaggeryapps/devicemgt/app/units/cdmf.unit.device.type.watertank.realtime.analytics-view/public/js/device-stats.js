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

var temperature;
var temperatureData = [];

var humidity;
var humidityData = [];

//var palette = new Rickshaw.Color.Palette({scheme: "classic9"});

$(window).load(function () {
    var websocketUrl = $("#div-chart").data("websocketurl");
    connect(websocketUrl);
    updateCoffee(20);
});

$(window).unload(function () {
    disconnect();
});

//function lineGraph(type, chartData) {
//    var tNow = new Date().getTime() / 1000;
//    for (var i = 0; i < 30; i++) {
//        chartData.push({
//                           x: tNow - (30 - i) * 15,
//                           y: parseFloat(0)
//                       });
//    }
//
//    var graph = new Rickshaw.Graph({
//        element: document.getElementById("chart-" + type),
//        width: $("#div-chart").width() - 50,
//        height: 300,
//        renderer: "line",
//        padding: {top: 0.2, left: 0.0, right: 0.0, bottom: 0.2},
//        xScale: d3.time.scale(),
//        series: [{
//            'color': palette.color(),
//            'data': chartData,
//            'name': type && type[0].toUpperCase() + type.slice(1)
//        }]
//    });
//
//    graph.render();
//
//    var xAxis = new Rickshaw.Graph.Axis.Time({
//        graph: graph
//    });
//
//    xAxis.render();
//
//    new Rickshaw.Graph.Axis.Y({
//        graph: graph,
//        orientation: 'left',
//        height: 300,
//        tickFormat: Rickshaw.Fixtures.Number.formatKMBT,
//        element: document.getElementById('y_axis-' + type)
//    });
//
//    new Rickshaw.Graph.HoverDetail({
//        graph: graph,
//        formatter: function (series, x, y) {
//            var date = '<span class="date">' + moment(x * 1000).format('Do MMM YYYY h:mm:ss a') + '</span>';
//            var swatch = '<span class="detail_swatch" style="background-color: ' + series.color + '"></span>';
//            return swatch + series.name + ": " + parseInt(y) + '<br>' + date;
//        }
//    });
//
//    return graph;
//}

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
                var time = parseInt(dataPoint[0]) / 1000;
                graphUpdate(temperatureData, time, dataPoint[3], temperature);
                graphUpdate(humidityData, time, dataPoint[4], humidity);
            }
        };
    }
}

function graphUpdate(chartData, xValue, yValue, graph) {
    updateCoffee(xValue);
}

function disconnect() {
    if (ws != null) {
        ws.close();
        ws = null;
    }
}
