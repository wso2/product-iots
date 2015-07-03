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

var now = new Date();
var startDate = new Date(now.getTime() - (60*60*24*100));
var endDate = new Date(now.getTime());

var DateRange = customFormatDate(startDate,configObject.format) +" "+ configObject.separator +" "+ customFormatDate(endDate,configObject.format);
console.log(DateRange);

$( document ).ready(function() {
    $('#date-range1').val(DateRange);
    $('#date-range1').trigger('datepicker-apply',
        {
            'value': DateRange,
            'date1' : startDate,
            'date2' : endDate
        });

    $('#btn-draw-graphs').trigger("click");
});

//26.04.2015 08:46 to 22.07.2015 08:46

$('#btn-draw-graphs').on('click', function () {
    var deviceId = getUrlParameter('deviceId');
    var deviceType = getUrlParameter('deviceType');
    console.log("device id:"+deviceId);
    getStats(deviceId, deviceType, fromDate, toDate);
});

function getStats(deviceId, deviceType, from, to) {

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
    console.log("bulbData...");
    console.log(bulbData);
    updateBulbGraph(convertStateStatsToGraphData(bulbData));
    scaleGraphs();
}

function scaleGraphs() {
    //Scale graphs
    var sliderX = 1110 * 60 * 60 / (toDate - fromDate);
    if (sliderX < 1110) {
        // fake handle move
        if (sliderX < 100) {
            sliderX = 100;
        }
        var edown = document.createEvent("HTMLEvents");
        edown.initEvent("mousedown", true, true);
        edown.clientX = 1160;
        var emove = document.createEvent("HTMLEvents");
        emove.initEvent("mousemove", true, true);
        emove.clientX = sliderX;
        var eup = document.createEvent("HTMLEvents");
        eup.initEvent("mouseup", true, true);
        var sliders = $('.right_handle');
        for (var slider in sliders) {
            sliders[slider].dispatchEvent(edown);
            document.dispatchEvent(emove);
            document.dispatchEvent(eup);
        }
    }
}

function convertStatsToGraphData(stats) {

    var graphData = new Array();
    if(!stats){return graphData;}
    for (var i = 0; i < stats.length; i++) {
        graphData.push({x: parseInt(stats[i]['time']) * 1000, y: stats[i]['value']})
    }

    return graphData;
}



function convertStateStatsToGraphData(stats){

    var graphData = new Array();
    if(!stats){return graphData;}
    var yValue;
	for(var i = 0; i < stats.length; i++){
    		yValue = -1;

        		if(stats[i]['value'].toUpperCase() == 'ON'){
        			yValue  = 1;
        		}else if(stats[i]['value'].toUpperCase() == 'OFF'){
        			yValue = 0;
        		}

        		graphData.push({x: parseInt(stats[i]['time']) * 1000, y: yValue})
    	}

    	return graphData;
}

function arrayMin(arr) {
    var len = arr.length, min = Infinity;
    while (len--) {
        if (arr[len] < min) {
            min = arr[len];
        }
    }
    return min;
};

function arrayMax(arr) {
    var len = arr.length, max = -Infinity;
    while (len--) {
        if (arr[len] > max) {
            max = arr[len];
        }
    }
    return max;
};


function customFormatDate(timeStamp, formatString){
    console.log("came"+formatString);
    var YYYY,YY,MMMM,MMM,MM,M,DDDD,DDD,DD,D,hhh,hh,h,mm,m,ss,s,ampm,AMPM,dMod,th;
    YYYY=timeStamp.getFullYear();
    MM = (M=timeStamp.getMonth()+1)<10?('0'+M):M;
    //MMM = (MMMM=["January","February","March","April","May","June","July","August","September","October","November","December"][M-1]).substring(0,3);
    DD = (D=timeStamp.getDate())<10?('0'+D):D;
    //DDD = (DDDD=["Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"][timeStamp.getDay()]).substring(0,3);
    formatString = formatString.replace("YYYY",YYYY).replace("MM",MM).replace("DD",DD).replace("D",D);
    console.log(formatString);

    h=(hhh=timeStamp.getHours());
    if (h==0) h=24;
    if (h>12) h-=12;
    hh = h<10?('0'+h):h;
    AMPM=(ampm=hhh<12?'am':'pm').toUpperCase();
    mm=(m=timeStamp.getMinutes())<10?('0'+m):m;
    ss=(s=timeStamp.getSeconds())<10?('0'+s):s;
    return formatString.replace("hhh",hhh).replace("HH",hh).replace("h",h).replace("mm",mm).replace("m",m).replace("ss",ss).replace("s",s).replace("ampm",ampm).replace("AMPM",AMPM);
}