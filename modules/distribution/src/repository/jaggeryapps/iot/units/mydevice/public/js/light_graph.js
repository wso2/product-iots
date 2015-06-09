var lightChart;

nv.addGraph(function () {

    lightChart = nv.models.lineChart()
        .interpolate("linear")
        .options({
            transitionDuration: 300,
            useInteractiveGuideline: true
        })
    ;

    lightChart.xScale(d3.time.scale());

    // chart sub-models (ie. xAxis, yAxis, etc) when accessed directly, return themselves, not the parent chart, so need to chain separately
    lightChart.xAxis
        .axisLabel("Date/Time")
        .ticks(d3.time.seconds)
        .tickFormat(function (d) {
            return d3.time.format('%m/%d/%Y %I:%M:%S %p')(new Date(d))
        })
        .staggerLabels(true)
    ;

    lightChart.yAxis
        .axisLabel('Light')
    ;

    d3.select('.chart2 svg')
        .datum(getLightChartData())
        .call(lightChart);

    nv.utils.windowResize(lightChart.update);

    return lightChart;
});

function getLightChartData() {

    return [
        {
            area: true,
            values: [],
            key: "Light",
            color: "#34500e"
        }
    ];

}

function updateLightGraph(lightData) {

    var chartData = getLightChartData();
    chartData[0]['values'] = lightData;

    d3.select('.chart2 svg')
        .datum(chartData)
        .transition().duration(500)
        .call(lightChart);
}