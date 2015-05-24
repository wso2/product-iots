
historicalBarChart = [
    {
        key: "Cumulative Return",
        values: [
            {
                "label" : "A" ,
                "value" : 1
            } ,
            {
                "label" : "B" ,
                "value" : 0
            } ,
            {
                "label" : "C" ,
                "value" : 1
            } ,
            {
                "label" : "D" ,
                "value" : 1
            } ,
            {
                "label" : "E" ,
                "value" : 1
            } ,
            {
                "label" : "F" ,
                "value" : 1
            } ,
            {
                "label" : "G" ,
                "value" : 0
            } ,
            {
                "label" : "H" ,
                "value" : 1
            }
        ]
    }
];

nv.addGraph(function() {

    var chart = nv.models.discreteBarChart()
            .x(function(d) { return d.label })
            .y(function(d) { return d.value })
            .height(300)
            .staggerLabels(true)
            //.staggerLabels(historicalBarChart[0].values.length > 8)
            .tooltips(false)
            .showValues(true)
            .duration(250)
            .color(['#5799c7'])

    //.xRange([0,500])
        ;

    d3.select('.chart2 svg')
        .datum(historicalBarChart)
        .call(chart);

    nv.utils.windowResize(chart.update);
    return chart;

});