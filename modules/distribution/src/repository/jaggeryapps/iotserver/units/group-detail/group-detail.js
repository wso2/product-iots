var log = new Log("modules/group-detail.js");

function onRequest(context) {
    var uri = request.getRequestURI();
    var uriMatcher = new URIMatcher(String(uri));
    var isMatched = uriMatcher.match("/{context}/group/{groupId}");
    if (isMatched) {
        var group = {};
        group.id = uriMatcher.elements().groupId;
        group.name = request.getParameter("name");
        group.deviceCount = request.getParameter("deviceCount");
        group.dateOfCreation = request.getParameter("dateOfCreation");
        group.dateOfLastUpdate = request.getParameter("dateOfLastUpdate");
        group.description = request.getParameter("description");
        group.users = request.getParameter("users");
        context.group =group;
    } else {
        response.sendError(404);
    }
    return context;
}