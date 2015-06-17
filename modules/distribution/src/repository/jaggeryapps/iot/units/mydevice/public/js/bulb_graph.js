var bulbChart;

nv.addGraph(function () {

    bulbChart = nv.models.lineChart()
        .interpolate("step-after")
        .options({
            transitionDuration: 300,
            useInteractiveGuideline: true
        })
    ;

    bulbChart.xScale(d3.time.scale());

    // chart sub-models (ie. xAxis, yAxis, etc) when accessed directly, return themselves, not the parent chart, so need to chain separately
    bulbChart.xAxis
        .axisLabel("Date/Time")
        .ticks(d3.time.seconds)
        .tickFormat(function (d) {
            return d3.time.format('%m/%d/%Y %I:%M:%S %p')(new Date(d))
        })
        .staggerLabels(true)
    ;

    bulbChart.yAxis
        .axisLabel('ON / OFF')
           .tickValues(1)
           .tickFormat(function(d) {
                   return d == 1 ? 'ON' : 'OFF'
               })
    ;

    d3.select('.chart6 svg')
        .datum(getBulbChartData())
        .call(bulbChart);

    nv.utils.windowResize(bulbChart.update);

    return bulbChart;
});

function getBulbChartData() {

    return [
        {
            area: true,
            values: [],
            key: "Bulb",
            color: "#34500e"
        }
    ];

}

function updateBulbGraph(fanData) {

    var chartData = getBulbChartData();
    chartData[0]['values'] = fanData;

    d3.select('.chart6 svg')
        .datum(chartData)
        .transition().duration(500)
        .call(bulbChart);
}