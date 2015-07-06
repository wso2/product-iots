function onRequest(context){
    var userModule = require("/modules/user.js").userModule;
    var permissions = userModule.getUIPermissions();
    context.permissions = permissions;
    return context;
}
