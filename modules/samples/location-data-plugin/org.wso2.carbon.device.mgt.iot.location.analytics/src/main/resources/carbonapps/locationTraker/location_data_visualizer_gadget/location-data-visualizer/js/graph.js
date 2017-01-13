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

//method to set starting time to UI component calender date
function setCalendarDate(start){
    var calendarTime=timeStampToCalendarDate(start);
    document.getElementById("calenderDate").innerHTML=calendarTime;
}

//method that show triggered data in the upcoming range
var showNext =function () {
    var currentStart = parseInt(document.getElementById('stt').value);
    var duration = parseInt(document.getElementById('gap').value);
    document.getElementById("stt").value =currentStart+duration;
    show();
};
//method that show triggered data in the previous range
var showPrevious =function () {
    var currentStart = parseInt(document.getElementById('stt').value);
    var duration = parseInt(document.getElementById('gap').value);
    document.getElementById("stt").value =currentStart-duration;
    show();
};
//method will update output UI component
function outputUpdate(num) {
    document.querySelector('#output').value = num;
    document.getElementById('stt').value =num;
    setCalendarDate(num);
    show();
}
//this will change value slider value according to current gap value
function changeStep() {
    document.getElementById("valueSlider").step =document.getElementById("gap").value;
}
function timeStampToCalendarDate(time) {

    var date = new Date(parseInt(time));
    var Months =["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"];
    return date.getFullYear()+" "+Months[date.getMonth()]+" "+date.getDate()+"      "+date.getHours()+":"+date.getMinutes()+":"+date.getSeconds();
}
//show data chunk
function showChunk(start,chunk,tList,xList,yList) {
    var data2=[];
    var gap=document.getElementById("gap").value;
    var e=parseInt(start)+parseInt(chunk);
    for(i=0;i<tList.length;i=i+1){
        if(parseInt(tList[i])<e && parseInt(tList[i])>parseInt(start)){
            data2.push({x:xList[i] ,y:yList[i], c:((e-parseInt(tList[i]))*100/parseInt(gap))});
        }
    }
    return data2;
}
//show play data
function doScaledTimeout(j,start,tList,chunk,xList,yList,cList) {
    setTimeout(function() {
        document.getElementById('stt').value=start;
        document.getElementById('output').value=start;
        setCalendarDate(start);

        var data2=showChunk(start,chunk,tList,xList,yList,cList);

        draw(data2);
    }, j * 300);
}
//colour schema for
colorRed = d3.scale.linear().domain([20,100])
    .interpolate(d3.interpolateHcl)
    .range([d3.rgb('#F99FA2'),d3.rgb("#F72128"),]);

colorBlue = d3.scale.linear().domain([10,20])
    .interpolate(d3.interpolateHcl)
    .range([d3.rgb("#00C1FF"),d3.rgb('#6670AC')]);

colorGreen = d3.scale.linear().domain([10,0])
    .interpolate(d3.interpolateHcl)
    .range([d3.rgb("#0A7E03"),d3.rgb('#0FF300')]);

var tooltip = d3.select("body").append("div")
    .attr("class", "tooltip")
    .style("opacity", 0);

//timestamp to calendar date
function tsToCal(gap,time) {
    var date = new Date(parseInt(time));
    var Months =["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"];
    if(gap < 60000){
        return date.getMinutes()+":"+date.getSeconds();

    }
    else if(gap <=7200000) {
        return Months[date.getMonth()]+"   "+date.getDate() + "      " + date.getHours() + ":" + date.getMinutes();
    }
    else{
        return date.getFullYear()+" "+Months[date.getMonth()]+" "+date.getDate();
    }

}
//reload UI
function display() {
    window.location.reload();

};

//loading image to svg element
function loadImage(x,y) {
    var image = d3.select("g").append("svg:image")
        .attr("xlink:href", "/portal/store/carbon.super/fs/gadget/location-data-visualizer/img/floor.jpg")
        .attr("width", 1500)
        .attr("height", 699)
        .attr("x",x)
        .attr("y",y);

}
function changeImage(x,y) {
    imageX=imageX+x;
    imageY=imageY+y;
    loadImage(imageX,imageY);

}
//colour schema
colorData = d3.scale.linear().domain([10,0])
    .interpolate(d3.interpolateHcl)
    .range([d3.rgb(125,0,0),d3.rgb(255,0,0)]);
