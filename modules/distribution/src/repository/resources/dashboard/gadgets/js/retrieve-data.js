function getLifeCycleNames() {

    var xmlHttpReq = createXmlHttpRequest();

                // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getlifeCycleNames" +
                               "&ms=" + new Date().getTime() , false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }

    return false;
}

function getResourceImpact(path) {

    var xmlHttpReq = createXmlHttpRequest();

                // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getResourceImpact" +
                               "&ms=" + new Date().getTime() + "&path=" + path, false);
        xmlHttpReq.send(null);
    
        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }
    
        return false;
    }
    
    return false;
}


function getImpactAnalysisJSON() {

    var xmlHttpReq = createXmlHttpRequest();

                // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getImpactAnalysis" +
                               "&ms=" + new Date().getTime(), false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }

    return false;
}

function getBackendServerUrl() {

    var xmlHttpReq = createXmlHttpRequest();

                // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getBackendServerUrl" +
                               "&ms=" + new Date().getTime(), false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }

    return false;
}

function getAdminConsoleUrl() {

    var xmlHttpReq = createXmlHttpRequest();

                // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getAdminConsoleUrl" +
                               "&ms=" + new Date().getTime(), false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }

    return false;
}

function createXmlHttpRequest() {
    var request;

                // Lets try using ActiveX to instantiate the XMLHttpRequest
				// object
    try {
        request = new ActiveXObject("Microsoft.XMLHTTP");
    } catch(ex1) {
        try {
            request = new ActiveXObject("Msxml2.XMLHTTP");
        } catch(ex2) {
            request = null;
        }
    }

                // If the previous didn't work, lets check if the browser natively support XMLHttpRequest
    if (!request && typeof XMLHttpRequest != "undefined") {
        //The browser does, so lets instantiate the object
        request = new XMLHttpRequest();
    }

    return request;
}

function removeCarriageReturns(string) {
    return string.replace(/\n/g, "");
}

function loadServerListWithServices() {
    var serverListArray = getServerList().split("|");

    // Cleaning up the existing select box
    var selectBoxEl = document.getElementById("server_select_box");
    selectBoxEl.innerHTML = "";

    var newServerSelectHTML = '<select id="serverIDs" onchange="loadServices();"><option value="">--Server--</option>';
    for (var x = 0; x < serverListArray.length; x++) {
	    var _tokens = serverListArray[x].split(",");
        newServerSelectHTML +=
        '<option value="' + _tokens[0] + '">' + _tokens[1] + '</option>';
    }
    newServerSelectHTML += '</select>';

    // Adding the new select to div
    selectBoxEl.innerHTML = newServerSelectHTML;

    if (!isServerExists(serverListArray, serverID)) {
        serverID = "";
        prefs.set("serverID", serverID);
    }

    tabs.setSelectedTab(0);
	drawDiagram();
}

function loadServerList() {
    var serverListArray = getServerList().split("|");

    // Cleaning up the existing select box
    var selectBoxEl = document.getElementById("server_select_box");
    selectBoxEl.innerHTML = "";

    var newServerSelectHTML = '<select id="serverIDs" onchange="refreshDataWithServerID();"><option value="">--Server--</option>';
    for (var x = 0; x < serverListArray.length; x++) {
	    var _tokens = serverListArray[x].split(",");
        newServerSelectHTML +=
        '<option value="' + _tokens[0] + '">' + _tokens[1] + '</option>';
    }
    newServerSelectHTML += '</select>';

    // Adding the new select to div
    selectBoxEl.innerHTML = newServerSelectHTML;

    if (!isServerExists(serverListArray, serverID)) {
        serverID = "";
        prefs.set("serverID", serverID);
    }
	tabs.setSelectedTab(0);
    drawDiagram();
}

function getServiceList() {
    var selectedServerID = document.getElementById('serverIDs').value;

    if (!(selectedServerID == "No Servers Configured")) {
        var xmlHttpReq = createXmlHttpRequest();
        
        // Make sure the XMLHttpRequest object was instantiated
        if (xmlHttpReq)
        {
            // This is a synchronous POST, hence UI blocking.
            xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getServicesList&serverID=" +
                                   selectedServerID + "&ms=" +
                                   new Date().getTime(), false);
            xmlHttpReq.send(null);

            if (xmlHttpReq.status == 200) {
                return removeCarriageReturns(xmlHttpReq.responseText);
            }

            return false;
        }
    }
    return false;
}

function getServerList() {
    var xmlHttpReq = createXmlHttpRequest();
    var serverList = [];
    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getServerList" +
                               "&ms=" + new Date().getTime(), false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

function getlastminuterequestcount(serviceID) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=lastminuterequestcount&serviceID=" + serviceID
                               + "&ms=" + new Date().getTime(), false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

function getminmaxaverageresptimessystem(serverID) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getminmaxaverageresptimessystem&serverID=" +
                               serverID + "&ms=" + new Date().getTime(), false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

function getMinMaxAverageRespTimesService(serviceID) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getminmaxaverageresptimesservice&serviceID=" +
                               serviceID + "&ms=" + new Date().getTime(), false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

function lastminuterequestcountsystem() {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=lastminuterequestcountsystem&serverUrl=" +
                               encodeHex(serverUrl) + "&ms=" + new Date().getTime(), false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

function loadServices() {
    var serviceListArray = getServiceList().split("|");

    // Cleaning up the existing select box
    var selectBoxEl = document.getElementById("service_select_box");
    selectBoxEl.innerHTML = "";

    var newServerSelectHTML = '<select id="services" onchange="refreshData();"><option value="">--Service--</option>';
    for (var x = 0; x < serviceListArray.length; x++) {
	    var _tokens = serviceListArray[x].split(",");
        newServerSelectHTML +=
        '<option value="' + _tokens[0] + '">' + _tokens[1] + '</option>';
    }
    newServerSelectHTML += '</select>';

    // Adding the new select to div
    selectBoxEl.innerHTML = newServerSelectHTML;
}

function refreshData() {
    serverID =
    document.getElementById('serverIDs')[document.getElementById('serverIDs').selectedIndex].value;
    serverURL =
    document.getElementById('serverIDs')[document.getElementById('serverIDs').selectedIndex].text;
    serviceID =
    document.getElementById('services')[document.getElementById('services').selectedIndex].value;
    serviceName =
    document.getElementById('services')[document.getElementById('services').selectedIndex].text;

    if ((serverID != "") && (serverID != "No Servers Configured") &&
        (serviceID != "")) {
        prefs.set("serverID", serverID);
        prefs.set("serverURL", serverURL);
        prefs.set("serviceID", serviceID);
        prefs.set("serviceName", serviceName);
    }
	tabs.setSelectedTab(0);
    drawDiagram();
}

function refreshDataWithServerID() {
    serverID =
    document.getElementById('serverIDs')[document.getElementById('serverIDs').selectedIndex].value;
    serverURL =
    document.getElementById('serverIDs')[document.getElementById('serverIDs').selectedIndex].text;

    if ((serverID != "") && (serverID != "No Servers Configured")) {
        prefs.set("serverID", serverID);
        prefs.set("serverURL", serverURL);
    }

    drawDiagram();
	tabs.setSelectedTab(0); /* it is assumed main_disp is having 0 index */
    document.getElementById('disp_config').style.display = "none";
    document.getElementById('main_disp').style.display = "block";
}

function isServerExists(serverListArray, monitoredServer) {
    for (var x = 0; x < serverListArray.length; x++) {

        var _tokens = serverListArray[x].split(",");

        if (_tokens[0] == monitoredServer) {
            return true
        }
    }

    return false;
}

function getLatestRequestCountForServer(serverID) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getLatestRequestCountForServer&serverID=" + serverID, false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

function getLatestResponseCountForServer(serverID) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpResponse object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getLatestResponseCountForServer&serverID=" + serverID, false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

function getLatestFaultCountForServer(serverID) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpFault object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getLatestFaultCountForServer&serverID=" + serverID, false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

function getLatestRequestCountForService(serviceID) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getLatestRequestCountForService&serviceID=" + serviceID
                               + "&ms=" + new Date().getTime(), false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

function getLatestResponseCountForService(serviceID) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getLatestResponseCountForService&serviceID=" + serviceID
                               + "&ms=" + new Date().getTime(), false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

function getLatestFaultCountForService(serviceID) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getLatestFaultCountForService&serviceID=" + serviceID
                               + "&ms=" + new Date().getTime(), false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

// Endpoint related functions

function loadServerListWithEndpoints() {
    var serverListArray = getServerList().split("|");

    // Cleaning up the existing select box
    var selectBoxEl = document.getElementById("server_select_box");
    selectBoxEl.innerHTML = "";

    var newServerSelectHTML = '<select id="serverIDs" onchange="loadEndpoints();"><option value="">--Server--</option>';
    for (var x = 0; x < serverListArray.length; x++) {
	    var _tokens = serverListArray[x].split(",");
        newServerSelectHTML +=
        '<option value="' + _tokens[0] + '">' + _tokens[1] + '</option>';
    }
    newServerSelectHTML += '</select>';

    // Adding the new select to div
    selectBoxEl.innerHTML = newServerSelectHTML;

    if (!isServerExists(serverListArray, serverID)) {
        serverID = "";
        prefs.set("serverID", serverID);
    }
	tabs.setSelectedTab(0);
    drawDiagram();
}

function loadEndpoints() {
    var epListArray = getEndpointList().split("&");

    // Cleaning up the existing select box
    var selectBoxEl = document.getElementById("endpoint_select_box");
    selectBoxEl.innerHTML = "";

    var newEndpointSelectHTML = '<select id="endpoints" onchange="refreshEndpointData();"><option value="">--Endpoint--</option>';
    for (var x = 0; x < epListArray.length; x++) {
	    //var _tokens = serviceListArray[x].split(",");
        newEndpointSelectHTML +=
        '<option value="' + epListArray[x] + '">' + epListArray[x] + '</option>';
    }
    newEndpointSelectHTML += '</select>';

    // Adding the new select to div
    selectBoxEl.innerHTML = newEndpointSelectHTML;
}

function getEndpointList() {
    var selectedServerID = document.getElementById('serverIDs').value;

    if (!(selectedServerID == "No Servers Configured")) {
        var xmlHttpReq = createXmlHttpRequest();
        
        // Make sure the XMLHttpRequest object was instantiated
        if (xmlHttpReq)
        {
            // This is a synchronous POST, hence UI blocking.
            xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getEndpoints&serverID=" +
                                   selectedServerID + "&ms=" +
                                   new Date().getTime(), false);
            xmlHttpReq.send(null);

            if (xmlHttpReq.status == 200) {
                return removeCarriageReturns(xmlHttpReq.responseText);
            }

            return false;
        }
    }
    return false;
}

function refreshEndpointData() {
    serverID =
    document.getElementById('serverIDs')[document.getElementById('serverIDs').selectedIndex].value;
    serverURL =
    document.getElementById('serverIDs')[document.getElementById('serverIDs').selectedIndex].text;
    endpointID =
    document.getElementById('endpoints')[document.getElementById('endpoints').selectedIndex].value;
    endpointName =
    document.getElementById('endpoints')[document.getElementById('endpoints').selectedIndex].text;

    if ((serverID != "") && (serverID != "No Servers Configured") &&
        (endpointID != "")) {
        prefs.set("serverID", serverID);
        prefs.set("serverURL", serverURL);
        prefs.set("endpointID", endpointID);
        prefs.set("endpointName", endpointName);
    }
	tabs.setSelectedTab(0);
    drawDiagram();
}

function getLatestInCumulativeCountForEndpoint(serverID, endpointName) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getLatestInCumulativeCountForEndpoint&serverID=" +
                               serverID + "&endpointName=" + endpointName + "&ms=" + new Date().getTime(), false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

function getLatestInFaultCountForEndpoint(serverID, endpointName) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getLatestInFaultCountForEndpoint&serverID=" +
                               serverID + "&endpointName=" + endpointName + "&ms=" + new Date().getTime(), false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

// Sequence related functions

function loadServerListWithSequences() {
    var serverListArray = getServerList().split("|");

    // Cleaning up the existing select box
    var selectBoxEl = document.getElementById("server_select_box");
    selectBoxEl.innerHTML = "";

    var newServerSelectHTML = '<select id="serverIDs" onchange="loadSequences();"><option value="">--Server--</option>';
    for (var x = 0; x < serverListArray.length; x++) {
	    var _tokens = serverListArray[x].split(",");
        newServerSelectHTML +=
        '<option value="' + _tokens[0] + '">' + _tokens[1] + '</option>';
    }
    newServerSelectHTML += '</select>';

    // Adding the new select to div
    selectBoxEl.innerHTML = newServerSelectHTML;

    if (!isServerExists(serverListArray, serverID)) {
        serverID = "";
        prefs.set("serverID", serverID);
    }
	tabs.setSelectedTab(0);
    drawDiagram();
}

function loadSequences() {
    var epListArray = getSequenceList().split("&");

    // Cleaning up the existing select box
    var selectBoxEl = document.getElementById("sequence_select_box");
    selectBoxEl.innerHTML = "";

    var newSequenceSelectHTML = '<select id="sequences" onchange="refreshSequenceData();"><option value="">--Sequence--</option>';
    for (var x = 0; x < epListArray.length; x++) {
	    //var _tokens = serviceListArray[x].split(",");
        newSequenceSelectHTML +=
        '<option value="' + epListArray[x] + '">' + epListArray[x] + '</option>';
    }
    newSequenceSelectHTML += '</select>';

    // Adding the new select to div
    selectBoxEl.innerHTML = newSequenceSelectHTML;
}

function getSequenceList() {
    var selectedServerID = document.getElementById('serverIDs').value;

    if (!(selectedServerID == "No Servers Configured")) {
        var xmlHttpReq = createXmlHttpRequest();
        
        // Make sure the XMLHttpRequest object was instantiated
        if (xmlHttpReq)
        {
            // This is a synchronous POST, hence UI blocking.
            xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getSequences&serverID=" +
                                   selectedServerID + "&ms=" +
                                   new Date().getTime(), false);
            xmlHttpReq.send(null);

            if (xmlHttpReq.status == 200) {
                return removeCarriageReturns(xmlHttpReq.responseText);
            }

            return false;
        }
    }
    return false;
}

function refreshSequenceData() {
    serverID =
    document.getElementById('serverIDs')[document.getElementById('serverIDs').selectedIndex].value;
    serverURL =
    document.getElementById('serverIDs')[document.getElementById('serverIDs').selectedIndex].text;
    sequenceID =
    document.getElementById('sequences')[document.getElementById('sequences').selectedIndex].value;
    sequenceName =
    document.getElementById('sequences')[document.getElementById('sequences').selectedIndex].text;

    if ((serverID != "") && (serverID != "No Servers Configured") &&
        (sequenceID != "")) {
        prefs.set("serverID", serverID);
        prefs.set("serverURL", serverURL);
        prefs.set("sequenceID", sequenceID);
        prefs.set("sequenceName", sequenceName);
    }
	tabs.setSelectedTab(0);
    drawDiagram();
}

function getLatestInCumulativeCountForSequence(serverID, sequenceName) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getLatestInCumulativeCountForSequence&serverID=" +
                               serverID + "&sequenceName=" + sequenceName + "&ms=" + new Date().getTime(), false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

function getLatestInFaultCountForSequence(serverID, sequenceName) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getLatestInFaultCountForSequence&serverID=" +
                               serverID + "&sequenceName=" + sequenceName + "&ms=" + new Date().getTime(), false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

// Proxy related functions

function loadServerListWithProxys() {
    var serverListArray = getServerList().split("|");

    // Cleaning up the existing select box
    var selectBoxEl = document.getElementById("server_select_box");
    selectBoxEl.innerHTML = "";

    var newServerSelectHTML = '<select id="serverIDs" onchange="loadProxys();"><option value="">--Server--</option>';
    for (var x = 0; x < serverListArray.length; x++) {
	    var _tokens = serverListArray[x].split(",");
        newServerSelectHTML +=
        '<option value="' + _tokens[0] + '">' + _tokens[1] + '</option>';
    }
    newServerSelectHTML += '</select>';

    // Adding the new select to div
    selectBoxEl.innerHTML = newServerSelectHTML;

    if (!isServerExists(serverListArray, serverID)) {
        serverID = "";
        prefs.set("serverID", serverID);
    }
	tabs.setSelectedTab(0);
    drawDiagram();
}

function loadProxys() {
    var epListArray = getProxyList().split("&");

    // Cleaning up the existing select box
    var selectBoxEl = document.getElementById("proxy_select_box");
    selectBoxEl.innerHTML = "";

    var newProxySelectHTML = '<select id="proxys" onchange="refreshProxyData();"><option value="">--Proxy--</option>';
    for (var x = 0; x < epListArray.length; x++) {
	    //var _tokens = serviceListArray[x].split(",");
        newProxySelectHTML +=
        '<option value="' + epListArray[x] + '">' + epListArray[x] + '</option>';
    }
    newProxySelectHTML += '</select>';

    // Adding the new select to div
    selectBoxEl.innerHTML = newProxySelectHTML;
}

function getProxyList() {
    var selectedServerID = document.getElementById('serverIDs').value;

    if (!(selectedServerID == "No Servers Configured")) {
        var xmlHttpReq = createXmlHttpRequest();
        
        // Make sure the XMLHttpRequest object was instantiated
        if (xmlHttpReq)
        {
            // This is a synchronous POST, hence UI blocking.
            xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getProxyServices&serverID=" +
                                   selectedServerID + "&ms=" +
                                   new Date().getTime(), false);
            xmlHttpReq.send(null);

            if (xmlHttpReq.status == 200) {
                return removeCarriageReturns(xmlHttpReq.responseText);
            }

            return false;
        }
    }
    return false;
}

function refreshProxyData() {
    serverID =
    document.getElementById('serverIDs')[document.getElementById('serverIDs').selectedIndex].value;
    serverURL =
    document.getElementById('serverIDs')[document.getElementById('serverIDs').selectedIndex].text;
    proxyID =
    document.getElementById('proxys')[document.getElementById('proxys').selectedIndex].value;
    proxyName =
    document.getElementById('proxys')[document.getElementById('proxys').selectedIndex].text;

    if ((serverID != "") && (serverID != "No Servers Configured") &&
        (proxyID != "")) {
        prefs.set("serverID", serverID);
        prefs.set("serverURL", serverURL);
        prefs.set("proxyID", proxyID);
        prefs.set("proxyName", proxyName);
    }
	tabs.setSelectedTab(0);
    drawDiagram();
}

function getLatestInCumulativeCountForProxy(serverID, proxyName) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getLatestInCumulativeCountForProxy&serverID=" +
                               serverID + "&proxyName=" + proxyName + "&ms=" + new Date().getTime(), false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

function getLatestInFaultCountForProxy(serverID, proxyName) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getLatestInFaultCountForProxy&serverID=" +
                               serverID + "&proxyName=" + proxyName + "&ms=" + new Date().getTime(), false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

//////////////////////////////////////////// added  activity

function getLatestMaximumOperationsForAnActivityID(activityID) {
    var xmlHttpReq = createXmlHttpRequest();
    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getLatestMaximumOperationsForAnActivityID&activityID=" + activityID, false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}



function loadActivities() {
    var activityListArray = getActivityList().split("|");

    // Cleaning up the existing select box
    var selectBoxEl = document.getElementById("activity_select_box");
    selectBoxEl.innerHTML = "";

    var newServerSelectHTML = '<select id="activities" onchange="refreshActivityData();"><option value="">--Activity--</option>';
    for (var x = 0; x < activityListArray.length; x++) {
	    var _tokens = activityListArray[x].split(",");
        newServerSelectHTML +=
        '<option value="' + _tokens[0] + '">' + _tokens[1] + '</option>';
    }
    newServerSelectHTML += '</select>';

    // Adding the new select to div
    selectBoxEl.innerHTML = newServerSelectHTML;
	
	tabs.setSelectedTab(0);
    drawDiagram();
}


function getActivityList() {
    var xmlHttpReq = createXmlHttpRequest();
    var activityList = [];
    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
         xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getActivityList" +
                "&ms=" + new Date().getTime(), false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

function isActivityExists(activityListArray , activityID) {
    for (var x = 0; x < activityListArray.length; x++) {
        if (activityListArray[x] == activityID) {
            return true
        }
    }

    return false;
}


function refreshActivityData() {
  
    activityID =
    document.getElementById('activities')[document.getElementById('activities').selectedIndex].value;
    activityName =
    document.getElementById('activities')[document.getElementById('activities').selectedIndex].text;

    if (activityID != "" &&(activityID != "No Activities Configured")) {
        prefs.set("activityID", activityID);
        prefs.set("activityName", activityName);
    }

	tabs.setSelectedTab(0);
    drawDiagram();
}


// Operations for a given Service

function getOperationsOfService(serverID, serviceID) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {

        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getOperationsOfService&serverID=" +
                               serverID + "&serviceID=" + serviceID + "&ms=" + new Date().getTime(), false);

        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

function getServerWithData(functionName) {
    var xmlHttpReq = createXmlHttpRequest();

    // Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq)
    {
        // This is a synchronous POST, hence UI blocking.
        xmlHttpReq.open("GET", "carbon/gauges/gadgets/flash/flashdata-ajaxprocessor.jsp?funcName=getServerWithData&function=" + functionName, false);
        xmlHttpReq.send(null);

        if (xmlHttpReq.status == 200) {
            return removeCarriageReturns(xmlHttpReq.responseText);
        }

        return false;
    }
    return false;
}

