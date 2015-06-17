var fanChart;

nv.addGraph(function () {

    fanChart = nv.models.lineChart()
        .interpolate("step-after")
        .options({
            transitionDuration: 300,
            useInteractiveGuideline: true
        })
    ;

    fanChart.xScale(d3.time.scale());

    // chart sub-models (ie. xAxis, yAxis, etc) when accessed directly, return themselves, not the parent chart, so need to chain separately
    fanChart.xAxis
        .axisLabel("Date/Time")
        .ticks(d3.time.seconds)
        .tickFormat(function (d) {
            return d3.time.format('%m/%d/%Y %I:%M:%S %p')(new Date(d))
        })
        .staggerLabels(true)
    ;

    fanChart.yAxis
        .axisLabel('Fan (Status)')
        .tickFormat(function(d) {
            return d == 1 ? 'ON' : 'OFF'
        })
    ;

    d3.select('.chart5 svg')
        .datum(getFanChartData())
        .call(fanChart);

    nv.utils.windowResize(fanChart.update);

    return fanChart;
});

function getFanChartData() {

    return [
        {
            area: true,
            values: [],
            key: "Fan",
            color: "#34500e"
        }
    ];

}

function updateFanGraph(fanData) {

    var chartData = getFanChartData();
    chartData[0]['values'] = fanData;

    d3.select('.chart5 svg')
        .datum(chartData)
        .transition().duration(500)
        .call(fanChart);
}