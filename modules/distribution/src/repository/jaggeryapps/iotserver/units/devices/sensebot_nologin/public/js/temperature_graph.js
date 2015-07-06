var temperatureChart;

nv.addGraph(function () {

    temperatureChart = nv.models.lineChart()
        .interpolate("linear")
        .options({
            transitionDuration: 300,
            useInteractiveGuideline: true
        })
    ;

    temperatureChart.xScale(d3.time.scale());

    // chart sub-models (ie. xAxis, yAxis, etc) when accessed directly, return themselves, not the parent chart, so need to chain separately
    temperatureChart.xAxis
        .axisLabel("Date/Time")
        .ticks(d3.time.seconds)
        .tickFormat(function (d) {
            return d3.time.format('%m/%d/%Y %I:%M:%S %p')(new Date(d))
        })
        .staggerLabels(true)
    ;

    temperatureChart.yAxis
        .axisLabel('Temperature (C)')
    ;

    d3.select('.chart1 svg')
        .datum(getTemperatureChartData())
        .call(temperatureChart);

    nv.utils.windowResize(temperatureChart.update);

    return temperatureChart;
});

function getTemperatureChartData() {

    return [
        {
            area: true,
            values: [],
            key: "Temperature",
            color: "#34500e"
        }
    ];

}

function updateTemperatureGraph(temperatureData) {

    var chartData = getTemperatureChartData();
    chartData[0]['values'] = temperatureData;

    d3.select('.chart1 svg')
        .datum(chartData)
        .transition().duration(500)
        .call(temperatureChart);
}