require("lib/js/d3.js");
require("js/graph.js");
require("js/traffic.js");
require("lib/js/d3tip.js");
var GetInitStartTime = function () {
        $.ajax({
            url: 'http://localhost:8080/service/time/start',
            type: 'GET',
            dataType:'json',
            success: function(data) {
                var start =JSON.parse(JSON.stringify(data.stt));
                document.getElementById("stt").value=start;
                var valueSlider =document.getElementById("valueSlider");
                valueSlider.innerHTML=start;
                valueSlider.value=start;
                valueSlider.min=parseInt(start);
                setCalendarDate(start);
                //alert(JSON.stringify(data));
            },
            error:function (jqXHR, textStatus, errorThrown) {
                alert(errorThrown);
            }
        });
    };
var GetEndTime = function () {
        $.ajax({
            url: 'http://localhost:8080/service/time/end',
            type: 'GET',
            dataType:'json',
            success: function(data) {
                var end =JSON.parse(JSON.stringify(data.stt));
                document.getElementById("valueSlider").max =end;
                show();
                //alert(JSON.stringify(data));
            },
            error:function (jqXHR, textStatus, errorThrown) {
                alert(errorThrown);
            }

        });
    };
var show = function () {
    var start = parseInt(document.getElementById('stt').value);
    document.getElementById('valueSlider').value = start;
    var end = start + parseInt(document.getElementById('gap').value);
    var data2 = [];
    $.ajax({
        url: 'http://localhost:8080/service/data',
        type: 'GET',
        data: 'start=' + start + '&end=' + end, // or $('#myform').serializeArray()
        dataType: 'json',
        success: function (data) {
            var X = JSON.parse(JSON.stringify(data.map.X.myArrayList));

            var Y = JSON.parse(JSON.stringify(data.map.Y.myArrayList));

            for (i = 0; i < X.length; i++) {

                data2.push({x: X[i], y: Y[i]});
            }
            draw(data2);
            //alert(JSON.stringify(data));
        },
        error: function (jqXHR, textStatus, errorThrown) {
            alert(errorThrown);
        }
    });
};
function sendFile() {
    var file = document.getElementById('fileupload').files[0];
    var fd = new FormData();
    fd.append( 'file', file);

    $.ajax({
        url: 'http://localhost:8080/service/file/upload',
        data: fd,
        processData: false,
        contentType: false,
        type: 'POST',



        success: function(data) {
            alert("file upload sucessfull");
            GetInitStartTime();
            GetEndTime();
            show();
            //alert(JSON.stringify(data));
        },
        error:function (jqXHR, textStatus, errorThrown) {
            alert(errorThrown);
        }
    });
}
function getDimension() {
    var url;
    var value;



    $.ajax({
        url: 'http://localhost:8080/service/get/dimension',
        type: 'GET',
        dataType: 'json',
        success: function (data) {
            Xmax = JSON.parse(JSON.stringify(data.maxX));
            Ymax = JSON.parse(JSON.stringify(data.maxY));
            Xmin = JSON.parse(JSON.stringify(data.minX));
            Ymin = JSON.parse(JSON.stringify(data.minY));

            x.domain(d3.extent([Xmin,Xmax])).nice();
            y.domain(d3.extent([Ymin,Ymax])).nice();

            return value;


        },
        error: function (jqXHR, textStatus, errorThrown) {
            alert(errorThrown);
        }
        })

    x.domain(d3.extent([-50,50])).nice();
    y.domain(d3.extent([-100,100])).nice();

}
function  play(start,chunk) {
    var start=parseInt(document.getElementById('stt').value);
    var chunk =parseInt(document.getElementById('gap').value)
    var end = parseInt(start) + (500*parseInt(chunk));

    // var data2 =[];
    var xList,yList,tList,cList;
    $.ajax({

        url: 'http://localhost:8080/service/data/chunk',
        type: 'GET',
        data: 'start='+start+'&end='+end, // or $('#myform').serializeArray()
        dataType:'json',

        success: function(data) {
            alert("data loaded");
            xList=JSON.parse(JSON.stringify(data.X));
            yList=JSON.parse(JSON.stringify(data.Y));
            tList=JSON.parse(JSON.stringify(data.T));
            cList=JSON.parse(JSON.stringify(data.C));
            var i=0;
            while(i<500){
                var s=parseInt(start)+(i*1000);
                doScaledTimeout(i,s,tList,chunk,xList,yList,cList);
                i+=1;
            }


        },
        error:function (jqXHR, textStatus, errorThrown) {
            alert(errorThrown);
        }
    });
}
var draw=function (data) {

    svg.selectAll(".series").remove();
    svg.selectAll(".g").remove();
    var seriesNames = d3.keys(data[0])
        .filter(function(d) { return d !== "x"; })
        .sort();
    var series = seriesNames.map(function(series) {
        return data.map(function(d) {
            return {x: +parseFloat(d.x), y: +parseFloat(d.y),c:+parseFloat(d.c)};
        });
    });
    // Compute the scales’ domains.


    // Add the x-axis.
    svg.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + height + ")")
        .call(d3.svg.axis().scale(x).orient("bottom"))


    // Add the y-axis.
    svg.append("g")
        .attr("class", "y axis")
        .call(d3.svg.axis().scale(y).orient("left"));

    var div = d3.select("body").append("div")
        .attr("class", "tooltip")
        .style("opacity", 0);

    // Add the points!
    svg.selectAll(".series")
        .data(series)
        .enter().append("g")
        .attr("class", "series")


        .selectAll(".point")
        .data(function(d) {

            return d; })
        .enter().append("circle")
        .attr("class", "point")
        .style("fill", function (d) {
            if(isNaN(d.c)){
                return colorRed(50);
            }
           return d3.rgb(255,d.c*255/100,d.c*255/100);

         })
        .attr("r", 4.5)
        .attr("cx", function(d) {
            return x(parseFloat(d.x));})
        .attr("cy", function(d) {
            return y(parseFloat(d.y));})

};
function require(script) {
    $.ajax({
        url: script,
        dataType: "script",
        async: false,
        success: function () {

        },
        error: function () {
            throw new Error("Could not load script " + script);
        }
    });
}
function  showDensity() {
    var start = parseInt(document.getElementById('stt').value);
    document.getElementById('valueSlider').value = start;
    var end = start + parseInt(document.getElementById('gap').value);
    var data2 = [];
    $.ajax({
        url: 'http://localhost:8080/service/data/density',
        type: 'GET',
        data: 'start=' + parseInt(document.getElementById('stt').value) + '&end=' +(parseInt(document.getElementById('stt').value)+ parseInt(document.getElementById('gap').value)), // or $('#myform').serializeArray()
        dataType: 'json',
        success: function (data) {
            var X = JSON.parse(JSON.stringify(data.map.X.myArrayList));

            var Y = JSON.parse(JSON.stringify(data.map.Y.myArrayList));

            var D = JSON.parse(JSON.stringify(data.map.D.myArrayList));

            var ID = JSON.parse(JSON.stringify(data.map.ID.myArrayList));
            for (i = 0; i < X.length; i++) {


                data2.push({x: X[i], y: Y[i], c:parseInt(D[i]), id:ID[i]});
            }
            var max=D[0];
            var min=D[0];
            for (j=1;j<X.length;j++){
                if(D[j]>max){
                    max=D[j];
                }
                if(D[j]<min){
                    min=D[j];
                }
            }
            drawDensity(data2,max,min);
            //alert(JSON.stringify(data));
        },
        error: function (jqXHR, textStatus, errorThrown) {
            alert(errorThrown);
        }
    });

};
var drawDensity=function (data,max,min) {

    svg.selectAll(".series").remove();
    svg.selectAll(".g").remove();
    var seriesNames = d3.keys(data[0])
        .filter(function(d) { return d !== "x" && d!=="c" && d!="id"; })
        .sort();
    var series = seriesNames.map(function(series) {
        return data.map(function(d) {
            return {x: +parseFloat(d.x), y: +parseFloat(d.y),c:+(parseFloat(d.c)-min)*100/(max-min), id:parseInt(d.id)};
        });
    });
    // Compute the scales’ domains.
    x.domain(d3.extent([-71,40])).nice();
    y.domain(d3.extent([4,65])).nice();

    // Add the x-axis.
    svg.append("g")
        .attr("class", "x axis")
        .attr("transform", "translate(0," + height + ")")
        .call(d3.svg.axis().scale(x).orient("bottom"));
    // Add the y-axis.
    svg.append("g")
        .attr("class", "y axis")
        .call(d3.svg.axis().scale(y).orient("left"));
    // Add the points!
    svg.selectAll(".series")
        .data(series)
        .enter().append("g")
        .attr("class", "series")


        .selectAll(".point")
        .data(function(d) {
            return d; })
        .enter().append("circle")
        .attr("class", "point")
        .style("fill", function (d) {
            if(d.c==0){
                return d3.rgb(255,255,255);
            }
            else if(d.c>20){
                return colorRed(d.c);
            }
            else if(d.c>10){
                return colorBlue(d.c);
            }
            else if(d.c>=0){
                return colorGreen(d.c);
            }
            else{
                return d3.rgb(255,255,255);
            }






        })
        .on("mouseover", function(d) {
            tooltip.transition()
                .duration(200)
                .style("opacity", .9);
            tooltip.html((d.c).toFixed(2) )
                .style("left", (x(parseInt(d.x)) + "px"))
                .style("top", (y(parseInt(d.y)) + "px"));
        })
        .on("mouseout", function(d) {
            tooltip.transition()
                .duration(500)
                .style("opacity", 0);
        })
        .attr("r", 4.5)
        .attr("cx", function(d) {
            return x(parseFloat(d.x));})
        .attr("cy", function(d) {
            return y(parseFloat(d.y));})
        .on("click",function (d){
            var id=d.id;
            getFrequency(id);
        });

};

function  getFrequency(id) {

    var start = parseInt(document.getElementById('stt').value);
    document.getElementById('valueSlider').value = start;
    var gap = parseInt(document.getElementById('gap').value);
    var data1 = [];
    $.ajax({
        url:'http://localhost:8080/service/data/count',
        type: 'GET',
        data: 'start=' + start + '&gap=' + gap + '&sensor=' + id, // or $('#myform').serializeArray()
        dataType: 'json',
        success: function (data) {
            var count = JSON.parse(JSON.stringify(data.map.Count.myArrayList));
            for (i = 0; i < count.length; i++) {
                var gap = parseInt(document.getElementById('gap').value);


                data1.push({x: tsToCal(gap,(gap*i)+start+(gap/2.0)), y: count[i]});
            }
            var seriesNames = d3.keys(data[0])
                .filter(function(d) { return d !== "x" && d!=="c" && d!="id"; })
                .sort();
            var series = seriesNames.map(function(series) {
                return data.map(function(d) {
                    return {x: +i, y: d.y};
                });
            });
            var stt = parseInt(document.getElementById("stt").value);
            var gap = parseInt(document.getElementById("gap").value);

            d3.select("#barChart").remove();
            var margin = {top: 40, right: 20, bottom: 30, left: 40},
                width = 960 - margin.left - margin.right,
                height = 500 - margin.top - margin.bottom;



            var x = d3.scale.ordinal()
                .rangeRoundBands([0, width], .1);

            var y = d3.scale.linear()
                .range([height, 0]);

            var xAxis = d3.svg.axis()
                .scale(x)
                .orient("bottom");


            var yAxis = d3.svg.axis()
                .scale(y)
                .orient("left");
            var svg2 = d3.select("#modal_body").append("svg")
                .attr("id","barChart")
                .attr("width", width + margin.left + margin.right)
                .attr("height", height + margin.top + margin.bottom)
                .append("g")
                .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
            var tip = d3.tip()
                .attr('class', 'd3-tip')
                .offset([-10, 0])
                .html(function (d) {
                    return d.y;

                });
            svg2.call(tip);


                x.domain(data1.map(function(d) {

                    return d.x; }));

                y.domain([0, d3.max(data1, function(d) {

                    return d.y; })]);
                svg2.append("g")
                    .attr("class", "x axis")
                    .attr("transform", "translate(0," + height + ")")
                    .call(xAxis);

                svg2.append("g")
                    .attr("class", "y axis")
                    .call(yAxis)
                    .append("text")
                    .attr("transform", "rotate(-90)")
                    .attr("y", 6)
                    .attr("dy", ".71em")
                    .style("text-anchor", "end")
                    .text("Frequency");

                svg2.selectAll(".bar")
                    .data(data1)
                    .enter().append("rect")
                    .attr("class", "bar")
                    .attr("x", function(d) {return x(d.x); })
                    .attr("width", x.rangeBand())
                    .attr("y", function(d) {return y(d.y); })
                    .attr("height", function(d) { return height - y(d.y); })
                    .on('mouseover', tip.show)
                    .on('mouseout', tip.hide);



            function type(d) {
                d.y = +d.y;
                return d;
            }


        },
        error: function (jqXHR, textStatus, errorThrown) {
            alert(errorThrown);
        }



});
    $("#frequency_view").modal("show");
}


