var fanStatusChart;
nv.addGraph(function () {

    fanStatusChart = nv.models.lineChart()
        .interpolate("step-after")
        .options({
            transitionDuration: 300,
            useInteractiveGuideline: true
        })
    ;

    fanStatusChart.xScale(d3.time.scale());

    // chart sub-models (ie. xAxis, yAxis, etc) when accessed directly, return themselves, not the parent chart, so need to chain separately
    fanStatusChart.xAxis
        .axisLabel("Date/Time")
        .ticks(d3.time.seconds)
        .tickFormat(function (d) {
            return d3.time.format('%m/%d/%Y %I:%M:%S %p')(new Date(d))
        })
        .staggerLabels(true)
    ;

    fanStatusChart.yAxis
        .axisLabel('ON / OFF')
        .tickValues(1)
        .tickFormat(function (d) {
            return d == 1 ? 'ON' : 'OFF'
        })
    ;

    d3.select('.chart2 svg')
        .datum(getFanStatusChartData)
        .call(fanStatusChart);

    nv.utils.windowResize(fanStatusChart.update);

    return fanStatusChart;
});

function getFanStatusChartData() {
    return [
        {
            area: true,
            step: true,
            values: [],
            key: "Fan Status",
            color: "#ff7f0e"
        }
    ];

}

function updateFanStatusGraph(fanStatusData) {

    var chartData = getFanStatusChartData();
    chartData[0]['values'] = fanStatusData;

    d3.select('.chart2 svg')
        .datum(chartData)
        .transition().duration(500)
        .call(fanStatusChart);
}