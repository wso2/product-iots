/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */




 //this method request starting timestamp and set it in relevant UI components
var GetInitStartTime = function(){
  var endpointUrl = 'http://localhost:9763/location-data-visualizer/service/time/start' ;
  wso2.gadgets.XMLHttpRequest.get(endpointUrl,
    function(data){
      var start =JSON.parse(JSON.stringify(data.stt));
      document.getElementById("stt").value=start;
      var valueSlider =document.getElementById("valueSlider");
      valueSlider.innerHTML=start;
      valueSlider.value=start;
      valueSlider.min=parseInt(start);
      setCalendarDate(start);

    },
    function(error){
        alert(error);
    }
);

}



//this method request ending timestamp and set it in relevant UI components
var GetEndTime = function(){
  var url = 'http://localhost:9763/location-data-visualizer/service/time/end'
  wso2.gadgets.XMLHttpRequest.get(url,
             function(data){

               var end =JSON.parse(JSON.stringify(data.stt));
               document.getElementById("valueSlider").max =end;
               show();

             },
             function(error){
                 alert(error);
             }
 );

}

//when this method is called, it fletch starting time and range from UI components and request triggered sensor data in that range and draw them in the map
var show = function(){

  var start = parseInt(document.getElementById('stt').value);
  document.getElementById('valueSlider').value = start;
  var end = start + parseInt(document.getElementById('gap').value);
  var url ='http://localhost:9763/location-data-visualizer/service/data'+'?start=' + start + '&end=' + end;
  var data2=[];
  wso2.gadgets.XMLHttpRequest.get(url,
             function(data){

               var X = data.X;

               var Y = data.Y;


               for (i = 0; i < X.length; i++) {


                   data2.push({x: X[i], y: Y[i]});
               }


               draw(data2);

             },
             function(error){
               alert(error);

             }
 );

}

//this method send a file to evaluate for typical scenario and save as output.txt
var sendFile = function(){
  var file = document.getElementById('fileupload').files[0];
  var fd = new FormData();
  fd.append( 'file', file);


  var url ='https://localhost:9763/location-data-visualizer/service/file/upload';
  wso2.gadgets.XMLHttpRequest.post(url,fd,
             function(data){

               GetInitStartTime();
               GetEndTime();
               show();
             },
             function(error){
                  alert(error);
             },
             'application/x-www-form-urlencoded; charset=UTF-8'
 );

}




//request dimension of map area. Then set those values in axises
var getDimension = function(){
  var url='http://localhost:9763/location-data-visualizer/service/get/dimension';
  var value;
  wso2.gadgets.XMLHttpRequest.get(url,
             function(data){
               Xmax = JSON.parse(JSON.stringify(data.maxX));
               Ymax = JSON.parse(JSON.stringify(data.maxY));
               Xmin = JSON.parse(JSON.stringify(data.minX));
               Ymin = JSON.parse(JSON.stringify(data.minY));

               x.domain(d3.extent([Xmin,Xmax])).nice();
               y.domain(d3.extent([Ymin,Ymax])).nice();

               return value;

             },
             function(error){
               alert(error);

             }
 );

}

//request data chunk and call play it using doScaledTimeout Method
var play = function(){
  var start=parseInt(document.getElementById('stt').value);
  var chunk =parseInt(document.getElementById('gap').value)
  var end = parseInt(start) + (500*parseInt(chunk));
  var url= 'http://localhost:9763/location-data-visualizer/service/data/chunk'+'?start='+start+'&end='+end;

  wso2.gadgets.XMLHttpRequest.get(url,
             function(data){


               xList=data.X;
               yList=data.Y;
               tList=data.T;
               cList=data.C;
               var i=0;
               while(i<500){
                   var s=parseInt(start)+(i*1000);
                   doScaledTimeout(i,s,tList,chunk,xList,yList,cList);
                   i+=1;
               }

             },
             function(error){
               alert(error);

             }
 );

}


//when a dataset in appropriate format is given to this function, this method will visualize that dataset on the map

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

//request triggering densities and show them with colour variations when respond comes
var showDensity = function(){
  var start = parseInt(document.getElementById('stt').value);
  var data2 = [];
  document.getElementById('valueSlider').value = start;
  var end = start + parseInt(document.getElementById('gap').value);
  var url= 'http://localhost:9763/location-data-visualizer/service/data/density'+'?start=' + parseInt(document.getElementById('stt').value) + '&end=' +(parseInt(document.getElementById('stt').value)+ parseInt(document.getElementById('gap').value));
  wso2.gadgets.XMLHttpRequest.get(url,
             function(data){
               var X = data.X;

               var Y = data.Y;

               var D = data.D;

               var ID = data.ID;
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
             function(error){
               alert(error);

             }
 );

}
//drawing function with color variations
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
//request upcoming frequencies of a particular sensor. Show them in a modal when respond comes. Range in UI will used as range in here too
var getFrequency = function(id){

  var start = parseInt(document.getElementById('stt').value);
  document.getElementById('valueSlider').value = start;
  var gap = parseInt(document.getElementById('gap').value);
  var data1 = [];
  var url='http://localhost:9763/location-data-visualizer/service/data/count'+'?start=' + start + '&gap=' + gap + '&sensor=' + id;


  wso2.gadgets.XMLHttpRequest.get(url,
             function(data){
               var count =data.Count;
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
             function(error){
               alert(error);

             }
 );
  $("#frequency_view").modal("show");
}

var showScene = function(){

  var data2=[];
  var endpointUrl = 'http://localhost:9763/location-data-visualizer/service/typical/scene'+'?hour=' + document.getElementById('time').value + '&day=' +document.getElementById('day').value;
  wso2.gadgets.XMLHttpRequest.get(endpointUrl,
    function(data){
      var X = JSON.parse(JSON.stringify(data.X));

      var Y = JSON.parse(JSON.stringify(data.Y));

      var C = JSON.parse(JSON.stringify(data.C));

      var ID = JSON.parse(JSON.stringify(data.ID));
      for (i = 0; i < X.length; i++) {


          data2.push({x: X[i], y: Y[i], c:C[i], id:ID[i]});
      }

      drawScene(data2);


    },
    function(error){
        alert(error);
    }
);

}
