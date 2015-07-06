var fromDate;
var toDate;

// create a custom bar renderer that has no gaps
Rickshaw.Graph.Renderer.BarNoGap = Rickshaw.Class.create(Rickshaw.Graph.Renderer.Bar, {
    name: 'bar_no_gap',
    barWidth: function (series) {
        var frequentInterval = this._frequentInterval(series.stack);
        var barWidth = this.graph.x(series.stack[0].x + frequentInterval.magnitude * 1);
        return barWidth;
    }
});


var currentDay = new Date();
var startDate = new Date(currentDay.getTime() - (60 * 60 * 24 * 100));
var endDate = new Date(currentDay.getTime());

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
    getDateTime(currentDay.getTime() - 86400000, currentDay.getTime());
});

//hour picker
$('#hour-btn').on('click', function () {
    getDateTime(currentDay.getTime() - 3600000, currentDay.getTime());
})

//week picker
$('#week-btn').on('click', function () {
    getDateTime(currentDay.getTime() - 604800000, currentDay.getTime());
})

//month picker
$('#month-btn').on('click', function () {
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
    updateTemperatureGraph(convertStatsToGraphData(temperatureData));

    var lightData = stats['lightData'];
    updateLightGraph(convertStatsToGraphData(lightData));

    var motionData = stats['motionData'];
    updateMotionGraph(convertStatsToGraphData(motionData));

    var sonarData = stats['sonarData'];
    updateSonarGraph(convertStatsToGraphData(sonarData));

    var fanData = stats['fanData'];
    updateFanGraph(convertStateStatsToGraphData(fanData));

    var bulbData = stats['bulbData'];
    updateBulbGraph(convertStateStatsToGraphData(bulbData));

    scaleGraphs();
}

function scaleGraphs() {
    var sliders = $('.right_handle');
    if (sliders.length == 0) {
        return;
    }

    var graphWidth = $('#canvas-wrapper1').width() - 50;
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
    var hour=date.getHours();
    var minute=date.getMinutes();
    return date.getFullYear() + '-' + (('' + month).length < 2 ? '0' : '')
        + month + '-' + (('' + day).length < 2 ? '0' : '') + day +" "+ (('' + hour).length < 2 ? '0' : '')
        + hour +":"+(('' + minute).length < 2 ? '0' : '')+ minute;
}