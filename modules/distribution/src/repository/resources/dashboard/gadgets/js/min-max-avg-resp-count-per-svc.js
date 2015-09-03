// Memory
var minMaxAverageRespTimesGraphPerSvc;

function initStats(memoryXScale) {
    if (memoryXScale != null) {
        initReqCountGraphs(memoryXScale);
    } else {
        initReqCountGraphs(30);
    }
}

function isNumeric(sText){
    var validChars = "0123456789.";
    var isNumber = true;
    var character;
    for (var i = 0; i < sText.length && isNumber == true; i++) {
        character = sText.charAt(i);
        if (validChars.indexOf(character) == -1) {
            isNumber = false;
        }
    }
    return isNumber;
}

function initReqCountGraphs(memoryXScale) {
    if (memoryXScale < 1 || !isNumeric(memoryXScale)) {
        return;
    }
    minMaxAverageRespTimesGraphPerSvc = new minMaxAvgGraph(memoryXScale);
}

