function onRequest(context){
    context.sketchPath = "api/device/sketch";
    var groupId = request.getParameter("groupId");
    if (groupId){
        context.groupId = groupId;
        context.title = "Group Analytics";
    }else{
        context.groupId = 0;
        context.title = "Device Analytics";
    }

    return context;
}
