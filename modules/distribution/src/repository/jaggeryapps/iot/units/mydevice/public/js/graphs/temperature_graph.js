//var temperatureChart;
//
//nv.addGraph(function () {
//    var height = 350;
//    temperatureChart = nv.models.lineChart();
//
//    temperatureChart.margin({left: 100}) ; //Adjust chart margins to give the x-axis some breathing room.
//    temperatureChart.useInteractiveGuideline(true) ; //We want nice looking tooltips and a guideline!
//    temperatureChart.showLegend(true)  ;     //Show the legend, allowing users to turn on/off line series.
//    temperatureChart.showYAxis(true) ;       //Show the y-axis
//    temperatureChart.showXAxis(true) ;       //Show the x-axis
//    temperatureChart.height(height) ;       //Show the x-axis
//
//
//    temperatureChart.xScale(d3.time.scale());
//    temperatureChart.forceY([0,100]);
//
//    // chart sub-models (ie. xAxis, yAxis, etc) when accessed directly, return themselves, not the parent chart, so need to chain separately
//    temperatureChart.xAxis
//        .axisLabel("Date/Time")
//        .ticks(d3.time.seconds)
//        .tickFormat(function (d) {
//            return d3.time.format('%m/%d/%Y %I:%M:%S %p')(new Date(d))
//        })
//        .staggerLabels(true)
//    ;
//
//    temperatureChart.yAxis
//        .axisLabel('Temperature (C)')
//    ;
//
//    d3.select('.chart1 svg')
//        .datum(getTemperatureChartData())
//        .call(temperatureChart)
//        .transition().duration(500)
//        .style({'height': height });
//    nv.utils.windowResize(temperatureChart.update);
//
//    return temperatureChart;
//});
//
//function getTemperatureChartData() {
//
//    return [
//        {
//            area: true,
//            values: [],
//            key: "Temperature"
//        }
//    ];
//
//}

function updateTemperatureGraph(temperatureData) {

    //var chartData = getTemperatureChartData();
    //chartData[0]['values'] = temperatureData;
    //
    //d3.select('.chart1 svg')
    //    .datum(chartData)
    //    .transition().duration(500)
    //    .call(temperatureChart);
    //
    //temperatureChart.forceY([0,30]);
    //
    //nv.utils.windowResize(temperatureChart.update);
    console.log("temperatureData");
    renderTemperatureChart(temperatureData);
}

function renderTemperatureChart(chartData){
    var chartWrapperElmId = "#canvas-wrapper1";
    var chartCanvasId="canvas1";

    if(chartData.length==0){
        $(chartWrapperElmId).html("No data available...");
        return;
    }
    var label=[];
    var data=[];
    var maxLabels=20;
    var showLabel=Math.floor(chartData.length/maxLabels);
    for(i=0;i<chartData.length;i++) {
        if(i%showLabel==0) {
            var timeStamp = new Date(chartData[i].x);
            label.push(customFormat(timeStamp, "#DD#/#MM#/#YYYY# #hh#:#mm#:#ss# #ampm#"));
        }else{
            label.push("");
        }
        data.push(chartData[i].y);
    }

    console.log(data);
    console.log(label);

    $(chartWrapperElmId).html("").html('<canvas id="'+chartCanvasId+'" height="350" width="100%"></canvas>');
    var lineChartData = {
        labels : label,
        datasets : [
            {
                fillColor : "rgba(49, 195, 166, 0.2)",
                strokeColor : "rgba(49, 195, 166, 1)",
                pointColor : "rgba(49, 195, 166, 1)",
                pointStrokeColor : "#fff",
                data : data
            }
        ]

    };
    var canvas = document.getElementById(chartCanvasId);
    var ctx = canvas.getContext("2d");

    myLine = new Chart(ctx).Line(lineChartData, {
        responsive: true,
        maintainAspectRatio: false
    });
}

function customFormat(timeStamp, formatString){
    var YYYY,YY,MMMM,MMM,MM,M,DDDD,DDD,DD,D,hhh,hh,h,mm,m,ss,s,ampm,AMPM,dMod,th;
    YY = ((YYYY=timeStamp.getFullYear())+"").slice(-2);
    MM = (M=timeStamp.getMonth()+1)<10?('0'+M):M;
    //MMM = (MMMM=["January","February","March","April","May","June","July","August","September","October","November","December"][M-1]).substring(0,3);
    DD = (D=timeStamp.getDate())<10?('0'+D):D;
    //DDD = (DDDD=["Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"][timeStamp.getDay()]).substring(0,3);
    th=(D>=10&&D<=20)?'th':((dMod=D%10)==1)?'st':(dMod==2)?'nd':(dMod==3)?'rd':'th';
    formatString = formatString.replace("#YYYY#",YYYY).replace("#YY#",YY).replace("#MMMM#",MMMM).replace("#MMM#",MMM).replace("#MM#",MM).replace("#M#",M).replace("#DDDD#",DDDD).replace("#DDD#",DDD).replace("#DD#",DD).replace("#D#",D).replace("#th#",th);

    h=(hhh=timeStamp.getHours());
    if (h==0) h=24;
    if (h>12) h-=12;
    hh = h<10?('0'+h):h;
    AMPM=(ampm=hhh<12?'am':'pm').toUpperCase();
    mm=(m=timeStamp.getMinutes())<10?('0'+m):m;
    ss=(s=timeStamp.getSeconds())<10?('0'+s):s;
    return formatString.replace("#hhh#",hhh).replace("#hh#",hh).replace("#h#",h).replace("#mm#",mm).replace("#m#",m).replace("#ss#",ss).replace("#s#",s).replace("#ampm#",ampm).replace("#AMPM#",AMPM);
}