var fromDate;
var toDate;

var configObject = {

    format: 'DD.MM.YYYY HH:mm',
    separator: ' to ',
    language: 'auto',
    startOfWeek: 'sunday',// or sunday
    getValue: function () {
        return this.value;
    },
    setValue: function (s) {
        this.value = s;
    },
    startDate: false,
    endDate: false,
    minDays: 0,
    maxDays: 0,
    showShortcuts: true,
    time: {
        enabled: true
    },
    shortcuts: {
        //'prev-days': [1,3,5,7],
        'next-days': [3, 5, 7],
        //'prev' : ['week','month','year'],
        'next': ['week', 'month', 'year']
    },
    customShortcuts: [],
    inline: false,
    container: 'body',
    alwaysOpen: false,
    singleDate: false,
    batchMode: false,
    stickyMonths: false
};

$('#date-range1').dateRangePicker(configObject)
    .bind('datepicker-apply', function (event, dateRange) {
        fromDate = dateRange.date1 != "Invalid Date" ? dateRange.date1.getTime() / 1000 : null;
        toDate = dateRange.date2 != "Invalid Date" ? dateRange.date2.getTime() / 1000 : null;
    });

$('#btn-draw-graphs').on('click', function () {
    var deviceId = $('#device-id').val();
    getStats(deviceId, fromDate, toDate);
})

function getStats(deviceId, from, to) {

    var requestData = new Object();

    requestData['deviceId'] = getUrlParameter('deviceId');

    if (from) {
        requestData['from'] = from;
    }

    if (to) {
        requestData['to'] = to;
    }

    var getStatsRequest = $.ajax({
        url: "api/stats",
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

    var temperatureData = stats['temperatureData'];
    updateTemperatureGraph(convertTemperatureStatsToGraphData(temperatureData));

    var fanStatusData = stats['fanStatusData'];
    updateFanStatusGraph(convertFanStatusStatsToGraphData(fanStatusData));

    var bulbStatusData = stats['bulbStatusData'];
    updateBulbStatusGraph(convertBulbStatusStatsToGraphData(bulbStatusData));

}

function convertTemperatureStatsToGraphData(stats) {

    var graphData = new Array();

    for (var i = 0; i < stats.length; i++) {
        graphData.push({x: parseInt(stats[i]['time']) * 1000, y: stats[i]['value']})
    }

    return graphData;
}

function convertFanStatusStatsToGraphData(stats) {

    var graphData = new Array();

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

function convertBulbStatusStatsToGraphData(stats) {

    var graphData = new Array();

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