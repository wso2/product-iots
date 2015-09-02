
var countGraph;

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

function initCountGraph(memoryXScale) {
    if (memoryXScale < 1 || !isNumeric(memoryXScale)) {
        return;
    }
    countGraph = new carbonGraph(memoryXScale);
}

