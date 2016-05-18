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

var palette = new Rickshaw.Color.Palette({scheme: "classic9"});

function drawGraph_connectedcup(from, to) {
    $("#y_axis-temperature").html("");
    $("#smoother-temperature").html("");
    $("#legend-temperature").html("");
    $("#chart-temperature").html("");
    $("#x_axis-temperature").html("");
    $("#slider-temperature").html("");

    $("#y_axis-coffeelevel").html("");
    $("#smoother-coffeelevel").html("");
    $("#legend-coffeelevel").html("");
    $("#chart-coffeelevel").html("");
    $("#x_axis-coffeelevel").html("");
    $("#slider-coffeelevel").html("");

    var devices = $("#connectedcup-details").data("devices");
    var tzOffset = new Date().getTimezoneOffset() * 60;

    var chartWrapperElmId = "#connectedcup-div-chart";
    var graphWidth = $(chartWrapperElmId).width() - 50;
    var temperatureGraphConfig = {
        element: document.getElementById("chart-temperature"),
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
    };

    var coffeelevelGraphConfig = {
        element: document.getElementById("chart-coffeelevel"),
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
    };

    if (devices) {
        for (var i = 0; i < devices.length; i++) {
            temperatureGraphConfig['series'].push(
                    {
                        'color': palette.color(),
                        'data': [{
                            x: parseInt(new Date().getTime() / 1000),
                            y: 0
                        }],
                        'name': devices[i].name
                    });

            coffeelevelGraphConfig['series'].push(
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
        temperatureGraphConfig['series'].push(
                {
                    'color': palette.color(),
                    'data': [{
                        x: parseInt(new Date().getTime() / 1000),
                        y: 0
                    }],
                    'name': $("#connectedcup-details").data("devicename")
                });
        coffeelevelGraphConfig['series'].push(
            {
                'color': palette.color(),
                'data': [{
                    x: parseInt(new Date().getTime() / 1000),
                    y: 0
                }],
                'name': $("#connectedcup-details").data("devicename")
            });
    }

    var temperatureGraph = new Rickshaw.Graph(temperatureGraphConfig);
    var coffeelevelGraph = new Rickshaw.Graph(coffeelevelGraphConfig);

    temperatureGraph.render();
    coffeelevelGraph.render

    var xAxisTemepature = new Rickshaw.Graph.Axis.Time({
        graph: temperatureGraph
    });

    xAxisTemepature.render();

    var xAxisCoffeelevel = new Rickshaw.Graph.Axis.Time({
        graph: coffeelevelGraph
    });

    xAxisCoffeelevel.render();

    var yAxisTemperature = new Rickshaw.Graph.Axis.Y({
        graph: temperatureGraph,
        orientation: 'left',
        element: document.getElementById("y_axis-temperature"),
        width: 40,
        height: 410
    });

    yAxisTemperature.render();

    var yAxisCoffeelevel = new Rickshaw.Graph.Axis.Y({
        graph: coffeelevelGraph,
        orientation: 'left',
        element: document.getElementById("y_axis-coffeelevel"),
        width: 40,
        height: 410
    });

    yAxisCoffeelevel.render();

    var slider = new Rickshaw.Graph.RangeSlider.Preview({
        graph: temperatureGraph,
        element: document.getElementById("slider-temperature")
    });

    var legend = new Rickshaw.Graph.Legend({
        graph: temperatureGraph,
        element: document.getElementById('legend-temperature')
    });

    var sliderCoffee = new Rickshaw.Graph.RangeSlider.Preview({
        graph: coffeelevelGraph,
        element: document.getElementById("slider-coffeelevel")
    });

    var legendCoffee = new Rickshaw.Graph.Legend({
        graph: coffeelevelGraph,
        element: document.getElementById('legend-coffeelevel')
    });

    var hoverDetail = new Rickshaw.Graph.HoverDetail({
        graph: temperatureGraph,
        formatter: function (series, x, y) {
            var date = '<span class="date">' +
                       moment((x + tzOffset) * 1000).format('Do MMM YYYY h:mm:ss a') + '</span>';
            var swatch = '<span class="detail_swatch" style="background-color: ' +
                         series.color + '"></span>';
            return swatch + series.name + ": " + parseInt(y) + '<br>' + date;
        }
    });

    var hoverDetailCoffeelevel = new Rickshaw.Graph.HoverDetail({
        graph: coffeelevelGraph,
        formatter: function (series, x, y) {
            var date = '<span class="date">' +
                moment((x + tzOffset) * 1000).format('Do MMM YYYY h:mm:ss a') + '</span>';
            var swatch = '<span class="detail_swatch" style="background-color: ' +
                series.color + '"></span>';
            return swatch + series.name + ": " + parseInt(y) + '<br>' + date;
        }
    });

    var shelving = new Rickshaw.Graph.Behavior.Series.Toggle({
        graph: temperatureGraph,
        legend: legend
    });

    var order = new Rickshaw.Graph.Behavior.Series.Order({
        graph: temperatureGraph,
        legend: legend
    });

    var highlighter = new Rickshaw.Graph.Behavior.Series.Highlight({
        graph: temperatureGraph,
        legend: legend
    });

    var shelvingCoffee = new Rickshaw.Graph.Behavior.Series.Toggle({
        graph: coffeelevelGraph,
        legend: legendCoffee
    });

    var orderCoffee = new Rickshaw.Graph.Behavior.Series.Order({
        graph: coffeelevelGraph,
        legend: legendCoffee
    });

    var highlighterCoffee = new Rickshaw.Graph.Behavior.Series.Highlight({
        graph: coffeelevelGraph,
        legend: legendCoffee
    });

    var deviceIndex = 0;

    if (devices) {
        getData();
    } else {
        var backendApiUrl = $("#connectedcup-div-chart").data("backend-api-url") + "/sensors/temperature" + "?from=" + from + "&to=" + to;
        var successCallback = function (data) {
            if (data) {
                drawTemperatureLineGraph(JSON.parse(data));
            }
        };
        invokerUtil.get(backendApiUrl, successCallback, function (message) {
            console.log(message);
        });

        var coffeeLevelApiUrl = $("#connectedcup-div-chart").data("backend-api-url") + "/sensors/coffeelevel"
            + "?from=" + from + "&to=" + to;
        var successCallbackCoffeeLevel = function (data) {
            if (data) {
                drawCoffeeLevelLineGraph(JSON.parse(data));
            }
        };
        invokerUtil.get(coffeeLevelApiUrl, successCallbackCoffeeLevel, function (message) {
            console.log(message);
        });
    }

    function getData() {
        if (deviceIndex >= devices.length) {
            return;
        }
        var backendApiUrl = $("#connectedcup-div-chart").data("backend-api-url") + devices[deviceIndex].deviceIdentifier
            + "/sensors/temperature"
                            + "?from=" + from + "&to=" + to;
        var successCallback = function (data) {
            if (data) {
                drawTemperatureLineGraph(JSON.parse(data));
            }
            deviceIndex++;
            getData();
        };
        invokerUtil.get(backendApiUrl, successCallback, function (message) {
            console.log(message);
            deviceIndex++;
            getData();
        });
        var coffeeLevelApiUrl = $("#connectedcup-div-chart").data("backend-api-url") + devices[deviceIndex].deviceIdentifier
            +  "/sensors/coffeelevel" + "?from=" + from + "&to=" + to;

        var successCallbackCoffeeLevel = function (data) {
            if (data) {
                drawCoffeeLevelLineGraph(JSON.parse(data));
            }
        };
        invokerUtil.get(coffeeLevelApiUrl, successCallbackCoffeeLevel, function (message) {
            console.log(message);
        });
    }

    function drawTemperatureLineGraph(data) {
        if (data.length === 0 || data.length === undefined) {
            return;
        }

        var chartData = [];
        for (var i = 0; i < data.length; i++) {
            chartData.push(
                    {
                        x: parseInt(data[i].values.time) - tzOffset,
                        y: parseInt(data[i].values.temperature)
                    }
            );
        }

        temperatureGraphConfig.series[deviceIndex].data = chartData;
        temperatureGraph.update();
    }

    function drawCoffeeLevelLineGraph(data) {
        if (data.length === 0 || data.length === undefined) {
            return;
        }

        var chartData = [];
        for (var i = 0; i < data.length; i++) {
            chartData.push(
                {
                    x: parseInt(data[i].values.time) - tzOffset,
                    y: parseInt(data[i].values.coffeelevel)
                }
            );
        }

        coffeelevelGraphConfig.series[deviceIndex].data = chartData;
        coffeelevelGraph.update();
    }
}
