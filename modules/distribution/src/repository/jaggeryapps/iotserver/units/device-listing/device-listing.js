var log = new Log("modules/device-listing.js");

function onRequest(context){
    var constants = require("/modules/constants.js");
    var userModule = require("/modules/user.js").userModule;
    var permissions = [];
    if(userModule.isAuthorized("/permission/device-mgt/admin/devices/list")){
        permissions.push("LIST_DEVICES");
    }else if(userModule.isAuthorized("/permission/device-mgt/user/devices/list")){
        permissions.push("LIST_OWN_DEVICES");
    }else if(userModule.isAuthorized("/permission/device-mgt/admin/policies/list")){
        permissions.push("LIST_POLICIES");
    }
    permissions.push("LIST_OWN_DEVICES");
    context.permissions = stringify(permissions);

    var groupId = request.getParameter("groupId");
    if (groupId){
        context.groupId = groupId;
    }else{
        context.groupId = 0;
    }
    context.user = session.get(constants.USER_SESSION_KEY).username;
    return context;
}