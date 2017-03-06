/**
 * Created by dimuth on 11/2/16.
 */

    var drawScene=function (data) {

        svg.selectAll(".series").remove();
        svg.selectAll(".g").remove();
        var seriesNames = d3.keys(data[0])
            .filter(function(d) { return d !== "x" && d!=="c" && d!="id"; })
            .sort();
        var series = seriesNames.map(function(series) {
            return data.map(function(d) {
                return {x: +parseFloat(d.x), y: +parseFloat(d.y),c:+(parseFloat(d.c)), id:parseInt(d.id)};
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
                else if(d.c>10){
                    return colorRed(d.c);
                }
                else if(d.c>3){
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
                return y(parseFloat(d.y));});


    };
function  showScene() {
    var data2 = [];
    $.ajax({
        url: 'http://localhost:8080/service/typical/scene',
        type: 'GET',
        data: 'hour=' + document.getElementById('time').value + '&day=' +document.getElementById('day').value, // or $('#myform').serializeArray()
        dataType: 'json',
        success: function (data) {
            var X = JSON.parse(JSON.stringify(data.X));

            var Y = JSON.parse(JSON.stringify(data.Y));

            var C = JSON.parse(JSON.stringify(data.C));

            var ID = JSON.parse(JSON.stringify(data.ID));
            for (i = 0; i < X.length; i++) {


                data2.push({x: X[i], y: Y[i], c:C[i], id:ID[i]});
            }

            drawScene(data2);
            //alert(JSON.stringify(data));
        },
        error: function (jqXHR, textStatus, errorThrown) {
            alert(errorThrown);
        }
    });

};
colorRed2 = d3.scale.linear().domain([10,100])
    .interpolate(d3.interpolateHcl)
    .range([d3.rgb('#F99FA2'),d3.rgb("#F72128"),]);

colorBlue2 = d3.scale.linear().domain([3,10])
    .interpolate(d3.interpolateHcl)
    .range([d3.rgb("#00C1FF"),d3.rgb('#6670AC')]);

colorGreen2 = d3.scale.linear().domain([3,0])
    .interpolate(d3.interpolateHcl)
    .range([d3.rgb("#0A7E03"),d3.rgb('#0FF300')]);




