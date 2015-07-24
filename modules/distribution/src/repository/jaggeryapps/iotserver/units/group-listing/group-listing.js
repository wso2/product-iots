function onRequest(context){
    //var userModule = require("/modules/user.js").userModule;
    var permissions = [];
    //if(userModule.isAuthorized("/permission/device-mgt/admin/devices/list")){
    //    permissions.push("LIST_GROUPS");
    //}else if(userModule.isAuthorized("/permission/device-mgt/user/devices/list")){
    //    permissions.push("LIST_OWN_GROUPS");
    //}
    permissions.push("LIST_GROUPS");
    context.permissions = stringify(permissions);
    return context;
}