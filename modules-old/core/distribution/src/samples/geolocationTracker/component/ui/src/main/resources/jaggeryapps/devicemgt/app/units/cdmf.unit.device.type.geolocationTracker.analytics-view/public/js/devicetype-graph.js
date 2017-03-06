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

function drawGraph_geolocationTracker(from, to) {
    $("#y_axis").html("");
    $("#smoother").html("");
    $("#legend").html("");
    $("#chart").html("");
    $("#x_axis").html("");
    $("#slider").html("");

    var devices = $("#details").data("devices");

    var tzOffset = new Date().getTimezoneOffset() * 60;

    var chartWrapperElmId = "#div-chart";
    var graphWidth = $(chartWrapperElmId).width() - 50;
    var graphConfig = {
        element: document.getElementById("chart"),
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
            graphConfig['series'].push(
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
        graphConfig['series'].push(
            {
                'color': palette.color(),
                'data': [{
                    x: parseInt(new Date().getTime() / 1000),
                    y: 0
                }],
                'name': $("#details").data("devicename")
            });
    }

    var graph = new Rickshaw.Graph(graphConfig);

    graph.render();

    var xAxis = new Rickshaw.Graph.Axis.Time({
        graph: graph
    });

    xAxis.render();

    var yAxis = new Rickshaw.Graph.Axis.Y({
        graph: graph,
        orientation: 'left',
        element: document.getElementById("y_axis"),
        width: 40,
        height: 410
    });

    yAxis.render();

    var slider = new Rickshaw.Graph.RangeSlider.Preview({
        graph: graph,
        element: document.getElementById("slider")
    });

    var legend = new Rickshaw.Graph.Legend({
        graph: graph,
        element: document.getElementById('legend')
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

    if (devices) {
        getData();
    } else {
        var backendApiUrl = $("#chart").data("backend-api-url") + "?from=" + from + "&to=" + to;
        var successCallback = function (data) {
            if (data) {
                drawLineGraph(JSON.parse(data));
            }
        };
        invokerUtil.get(backendApiUrl, successCallback, function (message) {
            console.log(message);
        });
    }

    function getData() {
        if (deviceIndex >= devices.length) {
            return;
        }
        var backendApiUrl = $("#chart").data("backend-api-url") + devices[deviceIndex].deviceIdentifier
            + "?from=" + from + "&to=" + to;
        var successCallback = function (data) {
            if (data) {
                drawLineGraph(JSON.parse(data));
            }
            deviceIndex++;
            getData();
        };
        invokerUtil.get(backendApiUrl, successCallback, function (message) {
            console.log(message);
            deviceIndex++;
            getData();
        });
    }

    function drawLineGraph(data) {
        if (data.length === 0 || data.length === undefined) {
            return;
        }

        var chartData = [];
        for (var i = 0; i < data.length; i++) {
            chartData.push(
                {
                    x: parseInt(data[i].values.meta_time) - tzOffset,
                    y: parseInt(data[i].values.speed)
                }
            );
        }

        graphConfig.series[deviceIndex].data = chartData;
        graph.update();
    }
}
