var motionChart;

nv.addGraph(function () {

    motionChart = nv.models.lineChart()
        .interpolate("linear")
        .options({
            transitionDuration: 300,
            useInteractiveGuideline: true
        })
    ;

    motionChart.xScale(d3.time.scale());

    // chart sub-models (ie. xAxis, yAxis, etc) when accessed directly, return themselves, not the parent chart, so need to chain separately
    motionChart.xAxis
        .axisLabel("Date/Time")
        .ticks(d3.time.seconds)
        .tickFormat(function (d) {
            return d3.time.format('%m/%d/%Y %I:%M:%S %p')(new Date(d))
        })
        .staggerLabels(true)
    ;

    motionChart.yAxis
        .axisLabel('Motion')
    ;

    d3.select('.chart3 svg')
        .datum(getMotionChartData())
        .call(motionChart);

    nv.utils.windowResize(motionChart.update);

    return motionChart;
});

function getMotionChartData() {

    return [
        {
            area: true,
            values: [],
            key: "Motion",
            color: "#34500e"
        }
    ];

}

function updateMotionGraph(motionData) {

    var chartData = getMotionChartData();
    chartData[0]['values'] = motionData;

    d3.select('.chart3 svg')
        .datum(chartData)
        .transition().duration(500)
        .call(motionChart);
}