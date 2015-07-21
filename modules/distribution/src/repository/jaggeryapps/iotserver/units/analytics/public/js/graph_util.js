var fromDate;
var toDate;

var currentDay = new Date();
var startDate = new Date(currentDay.getTime() - (60 * 60 * 24 * 100));
var endDate = new Date(currentDay.getTime());

// create a custom bar renderer that has no gaps
Rickshaw.Graph.Renderer.BarNoGap = Rickshaw.Class.create(Rickshaw.Graph.Renderer.Bar, {
    name: 'bar_no_gap',
    barWidth: function (series) {
        var frequentInterval = this._frequentInterval(series.stack);
        var barWidth = this.graph.x(series.stack[0].x + frequentInterval.magnitude * 1);
        return barWidth;
    }
});

function initDate(){
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
    $('#date-range').dateRangePicker(configObject)
        .bind('datepicker-apply', function (event, dateRange) {
            $(this).addClass('active');
            $(this).siblings().removeClass('active');
            fromDate = dateRange.date1 != "Invalid Date" ? dateRange.date1.getTime() / 1000 : null;
            toDate = dateRange.date2 != "Invalid Date" ? dateRange.date2.getTime() / 1000 : null;
            getStats(fromDate, toDate);
        }
    );
});

$(document).ready(function () {
    $('#date-range').html(DateRange);
    $('#date-range').trigger('datepicker-apply',
        {
            'value': DateRange,
            'date1': startDate,
            'date2': endDate
        });
});

//day picker
$('#today-btn').on('click', function () {
    initDate();
    getDateTime(currentDay.getTime() - 86400000, currentDay.getTime());
});

//hour picker
$('#hour-btn').on('click', function () {
    initDate();
    getDateTime(currentDay.getTime() - 3600000, currentDay.getTime());
});

//week picker
$('#week-btn').on('click', function () {
    initDate();
    getDateTime(currentDay.getTime() - 604800000, currentDay.getTime());
});

//month picker
$('#month-btn').on('click', function () {
    initDate();
    getDateTime(currentDay.getTime() - (604800000 * 4), currentDay.getTime());
});

$('body').on('click', '.btn-group button', function (e) {
    $(this).addClass('active');
    $(this).siblings().removeClass('active');
});

function getDateTime(from, to) {
    startDate = new Date(from);
    endDate = new Date(to);
    DateRange = convertDate(startDate) + " " + configObject.separator + " " + convertDate(endDate);
    console.log(DateRange);
    $('#date-range').html(DateRange);
    $('#date-range').trigger('datepicker-apply',
        {
            'value': DateRange,
            'date1': startDate,
            'date2': endDate
        }
    );
    getStats(from / 1000, to / 1000);
}

function getStats(from, to) {
    var deviceId = getUrlParameter('deviceId');
    var deviceType = getUrlParameter('deviceType');

    var requestData = new Object();

    requestData['deviceId'] = deviceId;
    requestData['deviceType'] = deviceType;

    if (from) {
        requestData['from'] = from;
    }

    if (to) {
        requestData['to'] = to;
    }

    var getStatsRequest = $.ajax({
        url: "../api/stats",
        method: "GET",
        data: requestData
    });

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
        updateTemperatureGraph(convertStatsToGraphData(temperatureData));
    } else {
        $('#div-temperatureData').html("");
    }

    var lightData = stats['lightData'];
    if (typeof lightData != 'undefined') {
        $('#div-lightData').html("").html("<div class='row margin-double'><div><h2 class='grey'>Light</h2><hr><div id='canvas-wrapper2'></div></div><hr></div>");
        updateLightGraph(convertStatsToGraphData(lightData));
    } else {
        $('#div-lightData').html("");
    }

    var motionData = stats['motionData'];
    if (typeof motionData != 'undefined') {
        $('#div-motionData').html("").html("<div class='row margin-double'><div><h2 class='grey'>Motion</h2><hr><div id='canvas-wrapper3'></div></div><hr></div>");
        updateMotionGraph(convertStatsToGraphData(motionData));
    } else {
        $('#div-motionData').html("");
    }

    var sonarData = stats['sonarData'];
    if (typeof sonarData != 'undefined') {
        $('#div-sonarData').html("").html("<div class='row margin-double'><div><h2 class='grey'>Sonar</h2><hr><div id='canvas-wrapper4'></div></div><hr></div>");
        updateSonarGraph(convertStatsToGraphData(sonarData));
    } else {
        $('#div-sonarData').html("");
    }

    var fanData = stats['fanData'];
    if (typeof fanData != 'undefined') {
        $('#div-fanData').html("").html("<div class='row margin-double'><div><h2 class='grey'>Fan Status</h2><hr><div id='canvas-wrapper5'></div></div><hr></div>");
        updateFanGraph(convertStateStatsToGraphData(fanData));
    } else {
        $('#div-fanData').html("");
    }

    var bulbData = stats['bulbData'];
    if (typeof bulbData != 'undefined') {
        $('#div-bulbData').html("").html("<div class='row margin-double'><div><h2 class='grey'>Bulb Status</h2><hr><div id='canvas-wrapper6'></div></div><hr></div>");
        updateBulbGraph(convertStateStatsToGraphData(bulbData));
    } else {
        $('#div-bulbData').html("");
    }

    var cpuData = stats['cpuData'];
    if (typeof cpuData != 'undefined') {
        $('#div-CPUData').html("").html("<div class='row margin-double'><div><h2 class='grey'>CPU Load</h2><hr><div id='canvas-wrapper7'></div></div><hr></div>");
        updateCPUGraph(convertStateStatsToGraphData(cpuData));
    } else {
        $('#div-CPUData').html("");
    }

    var ramData = stats['ramData'];
    if (typeof ramData != 'undefined') {
        $('#div-RAMData').html("").html("<div class='row margin-double'><div><h2 class='grey'>RAM Usage</h2><hr><div id='canvas-wrapper8'></div></div><hr></div>");
        updateRAMGraph(convertStateStatsToGraphData(ramData));
    } else {
        $('#div-RAMData').html("");
    }

    var cpuTemperatureData = stats['cpuTemperatureData'];
    if (typeof cpuTemperatureData != 'undefined') {
        $('#div-cpuTemperatureData').html("").html("<div class='row margin-double'><div><h2 class='grey'>CPU Temperature</h2><hr><div id='canvas-wrapper9'></div></div><hr></div>");
        updateCPUTemperatureGraph(convertStatsToGraphData(cpuTemperatureData));
    } else {
        $('#div-cpuTemperatureData').html("");
    }

    scaleGraphs();
}

function scaleGraphs() {
    var sliders = $('.right_handle');
    if (sliders.length == 0) {
        return;
    }
    var graphWidth = 0;
    for (var i = 1; i < 10; i++){
        if ($('#canvas-wrapper' + i).length){
            graphWidth = $('#canvas-wrapper' + i).width() - 50;
            break;
        }
    }

    if (graphWidth <= 0){
        return;
    }

    //Scale graphs
    var sliderX = graphWidth * 60 * 60 / (toDate - fromDate);
    if (sliderX < graphWidth) {
        // fake handle move
        if (sliderX < 100) {
            sliderX = 100;
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

function convertStatsToGraphData(stats) {

    var graphData = new Array();
    if (!stats) {
        return graphData;
    }
    for (var i = 0; i < stats.length; i++) {
        graphData.push({x: parseInt(stats[i]['time']) * 1000, y: stats[i]['value']})
    }

    return graphData;
}


function convertStateStatsToGraphData(stats) {

    var graphData = new Array();
    if (!stats) {
        return graphData;
    }
    var yValue;
    for (var i = 0; i < stats.length; i++) {
        yValue = -1;

        if (stats[i]['value'].toUpperCase() == 'ON') {
            yValue = 1;
        } else if (stats[i]['value'].toUpperCase() == 'OFF') {
            yValue = 0;
        }

        graphData.push({x: parseInt(stats[i]['time']) * 1000, y: yValue})
    }

    return graphData;
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