var bulbStatusChart;
nv.addGraph(function () {

    bulbStatusChart = nv.models.lineChart()
        .interpolate("step-after")
        .options({
            transitionDuration: 300,
            useInteractiveGuideline: true
        })
    ;

    bulbStatusChart.xScale(d3.time.scale());

    // chart sub-models (ie. xAxis, yAxis, etc) when accessed directly, return themselves, not the parent chart, so need to chain separately
    bulbStatusChart.xAxis
        .axisLabel("Date/Time")
        .ticks(d3.time.seconds)
        .tickFormat(function (d) {
            return d3.time.format('%m/%d/%Y %I:%M:%S %p')(new Date(d))
        })
        .staggerLabels(true)
    ;

    bulbStatusChart.yAxis
        .axisLabel('ON / OFF')
        .tickValues(1)
        .tickFormat(function (d) {
            return d == 1 ? 'ON' : 'OFF'
        })
    ;

    d3.select('.chart3 svg')
        .datum(getBulbStatusChartData)
        .call(bulbStatusChart);

    nv.utils.windowResize(bulbStatusChart.update);

    return bulbStatusChart;
});

function getBulbStatusChartData() {


    return [
        {
            area: true,
            step: true,
            values: [],
            key: "Bulb Status",
            color: "#ff500e"
        }
    ];

}
function updateBulbStatusGraph(bulbStatusData) {

    var chartData = getBulbStatusChartData();
    chartData[0]['values'] = bulbStatusData;

    d3.select('.chart3 svg')
        .datum(chartData)
        .transition().duration(500)
        .call(bulbStatusChart);
}