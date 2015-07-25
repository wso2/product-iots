var log = new Log("modules/group-detail.js");

function onRequest(context) {
    var uri = request.getRequestURI();
    var uriMatcher = new URIMatcher(String(uri));
    var isMatched = uriMatcher.match("/{context}/group/{groupId}");
    if (isMatched) {
        var carbon = require('carbon');
        var carbonHttpsServletTransport = carbon.server.address('https');

        var matchedElements = uriMatcher.elements();
        var groupId = matchedElements.groupId;
        var endpoint = carbonHttpsServletTransport + "/" + matchedElements.context + "/api/group/id/" + groupId;
        log.info(endpoint);
        //var result = get(endpoint, {}, "json");
        //if (result){
        //    context.group = result.data;
        //}else{
        //    response.sendError(503);
        //}
    } else {
        response.sendError(404);
    }
    return context;
}