var fromDate;
var toDate;

var currentDay = new Date();
var startDate = new Date(currentDay.getTime() - (60 * 60 * 24 * 100));
var endDate = new Date(currentDay.getTime());

var groupId;

var color = ['#c05020', '#30c020', '#6060c0', '#170B3B', '#5E610B', '#2F0B3A', '#FF4000', '#2F0B3A', 'steelblue'];

// create a custom bar renderer that shift bars
Rickshaw.Graph.Renderer.BinaryBar = Rickshaw.Class.create(Rickshaw.Graph.Renderer.Bar, {
    name: 'binary_bar',
    render: function (args) {

        args = args || {};

        var graph = this.graph;
        var series = args.series || graph.series;

        var vis = args.vis || graph.vis;
        vis.selectAll('*').remove();

        var barWidth = this.barWidth(series.active()[0]);
        var barXOffset = 0;

        var activeSeriesCount = series.filter(function (s) {
            return !s.disabled;
        }).length;
        var seriesBarWidth = this.unstack ? barWidth / activeSeriesCount : barWidth;

        var transform = function (d) {
            // add a matrix transform for negative values
            var matrix = [1, 0, 0, (d.y < 0 ? -1 : 1), 0, (d.y < 0 ? graph.y.magnitude(Math.abs(d.y)) * 2 : 0)];
            return "matrix(" + matrix.join(',') + ")";
        };

        var index = 0;
        series.forEach(function (series) {
            if (series.disabled) return;

            var nodes = vis.selectAll("path")
                .data(series.stack.filter(function (d) {
                    return d.y !== null
                }))
                .enter().append("svg:rect")
                .attr("x", function (d) {
                    return graph.x(d.x) + barXOffset
                })
                .attr("y", function (d) {
                    return ((graph.y(index + Math.abs(d.y))) * (d.y < 0 ? -1 : 1 ))
                })
                .attr("width", seriesBarWidth)
                .attr("height", function (d) {
                    return graph.y.magnitude(Math.abs(d.y))
                })
                .attr("transform", transform);

            index++;
            Array.prototype.forEach.call(nodes[0], function (n) {
                n.setAttribute('fill', series.color);
            });

            if (this.unstack) barXOffset += seriesBarWidth;

        }, this);
    }
});

function initDate() {
    currentDay = new Date();
    startDate = new Date(currentDay.getTime() - (60 * 60 * 24 * 100));
    endDate = new Date(currentDay.getTime());
}

var configObject = {
    startOfWeek: 'monday',
    separator: ' to ',
    format: 'YYYY-MM-DD HH:mm',
    autoClose: false,
    time: {
        enabled: true
    },
    shortcuts: 'hide',
    endDate: currentDay,
    maxDays: 2,
    getValue: function () {
        return this.value;
    },
    setValue: function (s) {
        this.value = s;
    }
};

var DateRange = convertDate(startDate) + " " + configObject.separator + " " + convertDate(endDate);

$(document).ready(function () {
    initDate();
    groupId = $("#request-group-id").data("groupid");

    $('#date-range').html(DateRange);
    $('#date-range').dateRangePicker(configObject)
        .bind('datepicker-apply', function (event, dateRange) {
            $(this).addClass('active');
            $(this).siblings().removeClass('active');
            fromDate = dateRange.date1 != "Invalid Date" ? dateRange.date1.getTime() / 1000 : null;
            toDate = dateRange.date2 != "Invalid Date" ? dateRange.date2.getTime() / 1000 : null;
            getStats(fromDate, toDate);
        }
    );
    getDateTime(startDate.getTime(), endDate.getTime());
});

//hour
$('#hour-btn').on('click', function () {
    initDate();
    getDateTime(currentDay.getTime() - 3600000, currentDay.getTime());
});

//12 hours
$('#h12-btn').on('click', function () {
    initDate();
    getDateTime(currentDay.getTime() - (3600000 * 12), currentDay.getTime());
});

//24 hours
$('#h24-btn').on('click', function () {
    initDate();
    getDateTime(currentDay.getTime() - (3600000 * 24), currentDay.getTime());
});

//48 hours
$('#h48-btn').on('click', function () {
    initDate();
    getDateTime(currentDay.getTime() - (3600000 * 48), currentDay.getTime());
});

$('body').on('click', '.btn-group button', function (e) {
    $(this).addClass('active');
    $(this).siblings().removeClass('active');
});

function getDateTime(from, to) {
    fromDate = from;
    toDate = to;
    startDate = new Date(from);
    endDate = new Date(to);
    DateRange = convertDate(startDate) + " " + configObject.separator + " " + convertDate(endDate);
    console.log(DateRange);
    $('#date-range').html(DateRange);
    getStats(from / 1000, to / 1000);
}

function getStats(from, to) {
    var requestData = new Object();
    var getStatsRequest;
    if (from) {
        requestData['from'] = from;
    }
    if (to) {
        requestData['to'] = to;
    }
    if (groupId && groupId != "0") {
        requestData['groupId'] = groupId;
        getStatsRequest = $.ajax({
            url: "api/stats/group",
            method: "GET",
            data: requestData
        });
    } else {
        var deviceId = getUrlParameter('deviceId');
        var deviceType = getUrlParameter('deviceType');

        requestData['deviceId'] = deviceId;
        requestData['deviceType'] = deviceType;

        getStatsRequest = $.ajax({
            url: "api/stats",
            method: "GET",
            data: requestData
        });
    }
    getStatsRequest.done(function (stats) {
        updateGraphs(JSON.parse(stats));
    });

    getStatsRequest.fail(function (jqXHR, textStatus) {
        alert("Request failed: " + textStatus);
    });
}

function getUrlParameter(paramName) {
    var pageURL = window.location.search.substring(1);
    var urlVariables = pageURL.split('&');
    for (var i = 0; i < urlVariables.length; i++) {
        var parameterName = urlVariables[i].split('=');
        if (parameterName[0] == paramName) {
            return parameterName[1];
        }
    }
}

function updateGraphs(stats) {
    console.log(stats);

    var temperatureData = stats['temperatureData'];
    if (typeof temperatureData != 'undefined') {
        $('#div-temperatureData').html("").html("<div class='row margin-double'><div><h2 class='grey'>Temperature</h2><hr><div id='canvas-wrapper1'></div></div><hr></div>");
        drawLineGraph(1, temperatureData);
    } else {
        $('#div-temperatureData').html("");
    }

    var lightData = stats['lightData'];
    if (typeof lightData != 'undefined') {
        $('#div-lightData').html("").html("<div class='row margin-double'><div><h2 class='grey'>Light</h2><hr><div id='canvas-wrapper2'></div></div><hr></div>");
        drawBarGraph(2, lightData);
    } else {
        $('#div-lightData').html("");
    }

    var motionData = stats['motionData'];
    if (typeof motionData != 'undefined') {
        $('#div-motionData').html("").html("<div class='row margin-double'><div><h2 class='grey'>Motion</h2><hr><div id='canvas-wrapper3'></div></div><hr></div>");
        drawBarGraph(3, motionData);
    } else {
        $('#div-motionData').html("");
    }

    var sonarData = stats['sonarData'];
    if (typeof sonarData != 'undefined') {
        $('#div-sonarData').html("").html("<div class='row margin-double'><div><h2 class='grey'>Sonar</h2><hr><div id='canvas-wrapper4'></div></div><hr></div>");
        drawLineGraph(4, sonarData);
    } else {
        $('#div-sonarData').html("");
    }

    var fanData = stats['fanData'];
    if (typeof fanData != 'undefined') {
        $('#div-fanData').html("").html("<div class='row margin-double'><div><h2 class='grey'>Fan Status</h2><hr><div id='canvas-wrapper5'></div></div><hr></div>");
        drawBarGraph(5, fanData);
    } else {
        $('#div-fanData').html("");
    }

    var bulbData = stats['bulbData'];
    if (typeof bulbData != 'undefined') {
        $('#div-bulbData').html("").html("<div class='row margin-double'><div><h2 class='grey'>Bulb Status</h2><hr><div id='canvas-wrapper6'></div></div><hr></div>");
        drawBarGraph(6, bulbData);
    } else {
        $('#div-bulbData').html("");
    }

    var cpuData = stats['cpuData'];
    if (typeof cpuData != 'undefined') {
        $('#div-CPUData').html("").html("<div class='row margin-double'><div><h2 class='grey'>CPU Load</h2><hr><div id='canvas-wrapper7'></div></div><hr></div>");
        drawLineGraph(7, cpuData);
    } else {
        $('#div-CPUData').html("");
    }

    var ramData = stats['ramData'];
    if (typeof ramData != 'undefined') {
        $('#div-RAMData').html("").html("<div class='row margin-double'><div><h2 class='grey'>RAM Usage</h2><hr><div id='canvas-wrapper8'></div></div><hr></div>");
        drawLineGraph(8, ramData);
    } else {
        $('#div-RAMData').html("");
    }

    var cpuTemperatureData = stats['cpuTemperatureData'];
    if (typeof cpuTemperatureData != 'undefined') {
        $('#div-cpuTemperatureData').html("").html("<div class='row margin-double'><div><h2 class='grey'>CPU Temperature</h2><hr><div id='canvas-wrapper9'></div></div><hr></div>");
        drawLineGraph(9, cpuTemperatureData);
    } else {
        $('#div-cpuTemperatureData').html("");
    }

    scaleGraphs();
}

function drawLineGraph(graphId, chartDataRaw) {
    var chartWrapperElmId = "#canvas-wrapper" + graphId;
    var graphWidth = $(chartWrapperElmId).width() - 50;
    if (chartDataRaw.length == 0) {
        $(chartWrapperElmId).html("No data available...");
        return;
    }

    var chartDiv = "chart" + graphId;
    var sliderDiv = "slider" + graphId;
    var y_axis = "y_axis" + graphId;
    $(chartWrapperElmId).html("").html('<div id = "' + y_axis
        + '" class="custom_y_axis"></div><div class="legend_container" id="legend_container'
        + graphId + '"><div id="smoother' + graphId + '" title="Smoothing"></div><div class="legend" id="legend'
        + graphId + '"></div></div><div id="' + chartDiv
        + '" class="custom_rickshaw_graph"></div><div class="custom_x_axis"></div><div id="' + sliderDiv
        + '" class="custom_slider"></div>');

    var graphConfig = {
        element: document.getElementById(chartDiv),
        width: graphWidth,
        height: 400,
        strokeWidth: 2,
        renderer: 'line',
        unstack: true,
        stack: false,
        xScale: d3.time.scale(),
        padding: {top: 0.2, left: 0.02, right: 0.02, bottom: 0},
        series: []
    };

    var k = 0;
    for (var i = 0; i < chartDataRaw.length; i++) {
        var chartData = [];
        if (chartDataRaw[i].stats.length > 0) {
            for (var j = 0; j < chartDataRaw[i].stats.length; j++) {
                chartData.push({
                    x: parseInt(chartDataRaw[i].stats[j].time),
                    y: parseInt(chartDataRaw[i].stats[j].value)
                });
            }
            graphConfig['series'].push({
                'color': color[k],
                'data': summerizeLine(chartData),
                'name': chartDataRaw[i].device
            });
        }
        if (++k == color.length) {
            k = 0;
        }
    }

    if (graphConfig['series'].length == 0) {
        $(chartWrapperElmId).html("No data available...");
        return;
    }

    var graph = new Rickshaw.Graph(graphConfig);

    graph.render();

    var hoverDetail = new Rickshaw.Graph.HoverDetail({
        graph: graph
    });

    var xAxis = new Rickshaw.Graph.Axis.Time({
        graph: graph
    });

    xAxis.render();

    var yAxis = new Rickshaw.Graph.Axis.Y({
        graph: graph,
        orientation: 'left',
        element: document.getElementById(y_axis),
        width: 40,
        height: 410
    });

    yAxis.render();

    var slider = new Rickshaw.Graph.RangeSlider.Preview({
        graph: graph,
        element: document.getElementById(sliderDiv)
    });

    var legend = new Rickshaw.Graph.Legend({
        graph: graph,
        element: document.getElementById('legend' + graphId)

    });
}


function drawBarGraph(graphId, chartDataRaw) {
    var chartWrapperElmId = "#canvas-wrapper" + graphId;
    var graphWidth = $(chartWrapperElmId).width() - 50;
    if (chartDataRaw.length == 0) {
        $(chartWrapperElmId).html("No data available...");
        return;
    }

    var chartDiv = "chart" + graphId;
    var sliderDiv = "slider" + graphId;
    var y_axis = "y_axis" + graphId;
    $(chartWrapperElmId).html("").html('<div id = "' + y_axis
        + '" class="custom_y_axis"></div><div class="legend_container" id="legend_container'
        + graphId + '"><div id="smoother' + graphId + '" title="Smoothing"></div><div class="legend" id="legend'
        + graphId + '"></div></div><div id="' + chartDiv
        + '" class="custom_rickshaw_graph"></div><div class="custom_x_axis"></div><div id="' + sliderDiv
        + '" class="custom_slider"></div>');

    var graphConfig = {
        element: document.getElementById(chartDiv),
        width: graphWidth,
        height: 50 * chartDataRaw.length,
        strokeWidth: 0.5,
        renderer: 'binary_bar',
        offset: 'zero',
        xScale: d3.time.scale(),
        padding: {top: 0.2, left: 0.02, right: 0.02, bottom: 0},
        series: []
    };

    var k = 0;
    for (var i = 0; i < chartDataRaw.length; i++) {
        var chartData = [];
        if (chartDataRaw[i].stats.length > 0) {
            for (var j = 0; j < chartDataRaw[i].stats.length; j++) {
                chartData.push({
                    x: parseInt(chartDataRaw[i].stats[j].time),
                    y: parseInt(chartDataRaw[i].stats[j].value)
                });
            }
            graphConfig['series'].push({
                'color': color[k],
                'data': summerizeBar(chartData),
                'name': chartDataRaw[i].device
            });
        }
        if (++k == color.length) {
            k = 0;
        }
    }

    if (graphConfig['series'].length == 0) {
        $(chartWrapperElmId).html("No data available...");
        return;
    }

    var graph = new Rickshaw.Graph(graphConfig);

    graph.registerRenderer(new Rickshaw.Graph.Renderer.BinaryBar({graph: graph}));

    graph.render();

    var xAxis = new Rickshaw.Graph.Axis.Time({
        graph: graph
    });

    xAxis.render();

    var yAxis = new Rickshaw.Graph.Axis.Y({
        graph: graph,
        orientation: 'left',
        element: document.getElementById(y_axis),
        width: 40,
        height: 55 * chartDataRaw.length,
        tickFormat: function (y) {
            return '';
        }
    });

    yAxis.render();

    var slider = new Rickshaw.Graph.RangeSlider.Preview({
        graph: graph,
        element: document.getElementById(sliderDiv)
    });

    var legend = new Rickshaw.Graph.Legend({
        graph: graph,
        element: document.getElementById('legend' + graphId)

    });
}

function scaleGraphs() {
    var sliders = $('.right_handle');
    if (sliders.length == 0) {
        return;
    }
    var graphWidth = 0;
    for (var i = 1; i < 10; i++) {
        if ($('#canvas-wrapper' + i).length) {
            graphWidth = $('#canvas-wrapper' + i).width() - 50;
            break;
        }
    }

    if (graphWidth <= 0) {
        return;
    }

    //Scale graphs
    var sliderX = graphWidth * 60 * 60000 / (toDate - fromDate);
    if (sliderX < graphWidth) {
        // fake handle move
        if (sliderX < 50) {
            sliderX = 50;
        }
        var edown = document.createEvent("HTMLEvents");
        edown.initEvent("mousedown", true, true);
        edown.clientX = graphWidth;
        var emove = document.createEvent("HTMLEvents");
        emove.initEvent("mousemove", true, true);
        emove.clientX = sliderX;
        var eup = document.createEvent("HTMLEvents");
        eup.initEvent("mouseup", true, true);
        for (var slider in sliders) {
            sliders[slider].dispatchEvent(edown);
            document.dispatchEvent(emove);
            document.dispatchEvent(eup);
        }
    }
}

function convertDate(date) {
    var month = date.getMonth() + 1;
    var day = date.getDate();
    var hour = date.getHours();
    var minute = date.getMinutes();
    return date.getFullYear() + '-' + (('' + month).length < 2 ? '0' : '')
        + month + '-' + (('' + day).length < 2 ? '0' : '') + day + " " + (('' + hour).length < 2 ? '0' : '')
        + hour + ":" + (('' + minute).length < 2 ? '0' : '') + minute;
}

function summerizeLine(data) {
    if (data.length > 1500) {
        var nData = [];
        var i = 1;
        while (i < data.length) {
            var t_avg = (data[i - 1].x + data[i].x) / 2;
            var v_avg = (data[i - 1].y + data[i].y) / 2;
            nData.push({x: t_avg, y: v_avg});
            i += 2;
        }
        return summerizeLine(nData);
    } else {
        return data;
    }
}

function summerizeBar(data) {
    if (data.length > 1500) {
        var nData = [];
        var i = 1;
        while (i < data.length - 1) {
            var t_avg = (data[i - 1].x + data[i].x) / 2;
            var v_avg = (data[i - 1].y + data[i].y + data[i+1].y) / 3;
            nData.push({x: t_avg, y: Math.round(v_avg)});
            i += 2;
        }
        return summerizeBar(nData);
    } else {
        return data;
    }
}