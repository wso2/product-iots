/**
 * Created by dimuth on 10/25/16.
 */
function setCalendarDate(start){
    var calendarTime=timeStampToCalendarDate(start);
    document.getElementById("calenderDate").innerHTML=calendarTime;
}
var showNext =function () {
    var currentStart = parseInt(document.getElementById('stt').value);
    var duration = parseInt(document.getElementById('gap').value);
    document.getElementById("stt").value =currentStart+duration;
    
    show();
};
var showPrevious =function () {
    var currentStart = parseInt(document.getElementById('stt').value);
    var duration = parseInt(document.getElementById('gap').value);
    document.getElementById("stt").value =currentStart-duration;
    show();
};
function outputUpdate(num) {
    document.querySelector('#output').value = num;
    document.getElementById('stt').value =num;
    setCalendarDate(num);
    show();
}
function changeStep() {
    document.getElementById("valueSlider").step =document.getElementById("gap").value;
}
function timeStampToCalendarDate(time) {

    var date = new Date(parseInt(time));
    var Months =["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"];
    return date.getFullYear()+" "+Months[date.getMonth()]+" "+date.getDate()+"      "+date.getHours()+":"+date.getMinutes()+":"+date.getSeconds();
}
function showChunk(start,chunk,tList,xList,yList) {
    var data2=[];
    var gap=document.getElementById("gap").value;
    var e=parseInt(start)+parseInt(chunk);
    for(i=0;i<tList.length;i++ ){
        if(parseInt(tList[i])<e && parseInt(tList[i])>parseInt(start)){
            data2.push({x:xList[i] ,y:yList[i], c:((e-parseInt(tList[i]))*100/parseInt(gap))});
        }
    }
    return data2;
}
function doScaledTimeout(j,start,tList,chunk,xList,yList,cList) {
    setTimeout(function() {
        document.getElementById('stt').value=start;
        document.getElementById('output').value=start;
        setCalendarDate(start);

        var data2=showChunk(start,chunk,tList,xList,yList,cList);

        draw(data2);
    }, j * 300);
}
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
function display() {
    window.location.reload();

};
function loadImage(x,y) {
    var image = d3.select("g").append("svg:image")
        .attr("xlink:href", "img/floor.jpg")
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
colorData = d3.scale.linear().domain([10,0])
    .interpolate(d3.interpolateHcl)
    .range([d3.rgb(125,0,0),d3.rgb(255,0,0)]);