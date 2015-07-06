var sonarChart;

nv.addGraph(function () {

    sonarChart = nv.models.lineChart()
        .interpolate("linear")
        .options({
            transitionDuration: 300,
            useInteractiveGuideline: true
        })
    ;

    sonarChart.xScale(d3.time.scale());

    // chart sub-models (ie. xAxis, yAxis, etc) when accessed directly, return themselves, not the parent chart, so need to chain separately
    sonarChart.xAxis
        .axisLabel("Date/Time")
        .ticks(d3.time.seconds)
        .tickFormat(function (d) {
            return d3.time.format('%m/%d/%Y %I:%M:%S %p')(new Date(d))
        })
        .staggerLabels(true)
    ;

    sonarChart.yAxis
        .axisLabel('Sonar')
    ;

    d3.select('.chart4 svg')
        .datum(getSonarChartData())
        .call(sonarChart);

    nv.utils.windowResize(sonarChart.update);

    return sonarChart;
});

function getSonarChartData() {

    return [
        {
            area: true,
            values: [],
            key: "Sonar",
            color: "#34500e"
        }
    ];

}

function updateSonarGraph(sonarData) {

    var chartData = getSonarChartData();
    chartData[0]['values'] = sonarData;

    d3.select('.chart4 svg')
        .datum(chartData)
        .transition().duration(500)
        .call(sonarChart);
}