/*This js mainly concern with the data structures related to graphs*/
function minMaxAvgGraph(xscale) {
    this.minArray = new Array();
    this.maxArray = new Array();
    this.avgArray = new Array();

    for (var i = 0; i < xscale; i++) {
        this.minArray[i] = [i, 0.0];
    }
    for (var j = 0; j < xscale; j++) {
        this.maxArray[j] = [j, 0.0];
    }
    for (var k = 0; k < xscale; k++) {
        this.avgArray[k] = [k, 0.0];
    }

    this.xscale = xscale;
}

function getDataMin() {
    return this.minArray;
}
function getDataMax() {
    return this.maxArray;
}
function getDataAvg() {
    return this.avgArray;
}

function addData(valuesString) {
    // First split and get values
    var values = valuesString.split("&");
    var minValue = values[0];
    var maxValue = values[1];
    var avgValue = values[2];

    this.addMin(minValue);
    this.addMax(maxValue);
    this.addAvg(avgValue);
}

function addDataMin(newValue) {

    //shift to left
    for (var i = 0; i < this.xscale - 1; i++) {
        this.minArray[i] = [i,this.minArray[i + 1][1]];  // (x,y)
    }

    //add the value to the last postion
    this.minArray[this.xscale - 1] = [this.xscale - 1,newValue];
}

function addDataMax(newValue) {

    //shift to left
    for (var i = 0; i < this.xscale - 1; i++) {
        this.maxArray[i] = [i,this.maxArray[i + 1][1]];  // (x,y)
    }

    //add the value to the last postion
    this.maxArray[this.xscale - 1] = [this.xscale - 1,newValue];
}

function addDataAvg(newValue) {

    //shift to left
    for (var i = 0; i < this.xscale - 1; i++) {
        this.avgArray[i] = [i,this.avgArray[i + 1][1]];  // (x,y)
    }

    //add the value to the last postion
    this.avgArray[this.xscale - 1] = [this.xscale - 1,newValue];
}

function graphTickGenerator() {
    var tickArray = [];
    var startTick = 10;
    var i = startTick - 1;
    var weight = this.xscale / 10;
    do {
        var t = (startTick - i) * weight - 1;
        var v = i * weight;
        if (v == 0) {
            v = "0";
        }
        tickArray.push([t, v]);
        i--;
    } while (i > -1);
    return tickArray;
}

minMaxAvgGraph.prototype.getMin = getDataMin;
minMaxAvgGraph.prototype.getMax = getDataMax;
minMaxAvgGraph.prototype.getAvg = getDataAvg;
minMaxAvgGraph.prototype.add = addData;
minMaxAvgGraph.prototype.addMin = addDataMin;
minMaxAvgGraph.prototype.addMax = addDataMax;
minMaxAvgGraph.prototype.addAvg = addDataAvg;
minMaxAvgGraph.prototype.tick = graphTickGenerator;

