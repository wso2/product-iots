/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

var palette = new Rickshaw.Color.Palette({scheme: "classic9"});
var sensorType1 = "sensor_temp";

var sensorType1Graph;


function drawGraph_raspberrypicodebased(from, to)
{
    //clear the graph before reloading
    $("#sensorType1yAxis").html("");
    $("#smoother").html("");
    $("#sensorType1Legend").html("");
    $("#chartSensorType1").html("");
    $("#sensorType1xAxis").html("");
    $("#sensorType1Slider").html("");



    var devices = $("#details").data("devices");
    var tzOffset = new Date().getTimezoneOffset() * 60;
    var chartWrapperElmId = "#chartDivSensorType1";
    var graphWidth = $(chartWrapperElmId).width() - 50;
    var graphConfigSensorType1 = getGraphConfig("chartSensorType1");
    var graphConfigSensorType2 = getGraphConfig("chartSensorType2");

    function getGraphConfig(placeHolder) {
        return {
            element: document.getElementById(placeHolder),
            width: graphWidth,
            height: 400,
            strokeWidth: 2,
            renderer: 'line',
            interpolation: "linear",
            unstack: true,
            stack: false,
            xScale: d3.time.scale(),
            padding: {top: 0.2, left: 0.02, right: 0.02, bottom: 0.2},
            series: []
        }
    };

    if (devices) {
        for (var i = 0; i < devices.length; i++) {
            graphConfigSensorType1['series'].push(
                {
                    'color': palette.color(),
                    'data': [{
                        x: parseInt(new Date().getTime() / 1000),
                        y: 0
                    }],
                    'name': devices[i].name
                });

            graphConfigSensorType2['series'].push(
                {
                    'color': palette.color(),
                    'data': [{
                        x: parseInt(new Date().getTime() / 1000),
                        y: 0
                    }],
                    'name': devices[i].name
                });
        }
    } else {
        graphConfigSensorType1['series'].push(
            {
                'color': palette.color(),
                'data': [{
                    x: parseInt(new Date().getTime() / 1000),
                    y: 0
                }],
                'name': $("#details").data("devicename")
            });
        graphConfigSensorType2['series'].push(
            {
                'color': palette.color(),
                'data': [{
                    x: parseInt(new Date().getTime() / 1000),
                    y: 0
                }],
                'name': $("#details").data("devicename")
            });
    }

    sensorType1Graph = new Rickshaw.Graph(graphConfigSensorType1);

    drawGraph(sensorType1Graph, "sensorType1yAxis", "sensorType1Slider", "sensorType1Legend", sensorType1
        , graphConfigSensorType1, "chartSensorType1");

    function drawGraph(graph, yAxis, slider, legend, sensorType, graphConfig, chart) {
        console.log("1");
        graph.render();
        var xAxis = new Rickshaw.Graph.Axis.Time({
            graph: graph
        });
        xAxis.render();
        var yAxis = new Rickshaw.Graph.Axis.Y({
            graph: graph,
            orientation: 'left',
            element: document.getElementById(yAxis),
            width: 40,
            height: 410
        });
        yAxis.render();
        var slider = new Rickshaw.Graph.RangeSlider.Preview({
            graph: graph,
            element: document.getElementById(slider)
        });
        var legend = new Rickshaw.Graph.Legend({
            graph: graph,
            element: document.getElementById(legend)
        });
        var hoverDetail = new Rickshaw.Graph.HoverDetail({
            graph: graph,
            formatter: function (series, x, y) {
                var date = '<span class="date">' +
                    moment.unix((x + tzOffset) * 1000).format('Do MMM YYYY h:mm:ss a') + '</span>';
                var swatch = '<span class="detail_swatch" style="background-color: ' +
                    series.color + '"></span>';
                return swatch + series.name + ": " + parseInt(y) + '<br>' + date;
            }
        });
        var shelving = new Rickshaw.Graph.Behavior.Series.Toggle({
            graph: graph,
            legend: legend
        });
        var order = new Rickshaw.Graph.Behavior.Series.Order({
            graph: graph,
            legend: legend
        });
        var highlighter = new Rickshaw.Graph.Behavior.Series.Highlight({
            graph: graph,
            legend: legend
        });
        var deviceIndex = 0;
        console.log("1");
        if (devices) {
            getData(chat, deviceIndex, sensorType);
        } else {
            var backendApiUrl = $("#" + chart + "").data("backend-api-url") + "?from=" + from + "&to=" + to
                + "&sensorType=" + sensorType;
            var successCallback = function (data) {
                if (data) {
                    drawLineGraph(JSON.parse(data), sensorType, deviceIndex, graphConfig, graph);
                }
            };
            invokerUtil.get(backendApiUrl, successCallback, function (message) {
                console.log(message);
            });
        }
    }

    function getData(placeHolder, deviceIndex, sensorType, graphConfig, graph) {
        if (deviceIndex >= devices.length) {
            return;
        }
        var backendApiUrl = $("#" + placeHolder + "").data("backend-api-url") + devices[deviceIndex].deviceIdentifier
            + "?from=" + from + "&to=" + to + "&sensorType=" + sensorType;
        var successCallback = function (data) {
            if (data) {
                console.log("----" + data);
                drawLineGraph(JSON.parse(data), sensorType, deviceIndex, graphConfig, graph);
            }
            deviceIndex++;
            getData(placeHolder, deviceIndex, sensorType);
        };
        invokerUtil.get(backendApiUrl, successCallback, function (message) {
            console.log(message);
            deviceIndex++;
            getData(placeHolder, deviceIndex, sensorType);
        });
    }

    function drawLineGraph(data, sensorType, deviceIndex, graphConfig, graph) {
        if (data.length === 0 || data.length === undefined) {
            return;
        }
        var chartData = [];
        for (var i = 0; i < data.length; i++) {
            chartData.push(
                {
                    x: parseInt(data[i].values.meta_time) - tzOffset,
                    y: parseInt(data[i].values[sensorType])
                }
            );
        }
        graphConfig.series[deviceIndex].data = chartData;
        graph.update();
    }
}
