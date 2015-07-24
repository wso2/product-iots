function onRequest(context){
    var userModule = require("/modules/user.js").userModule;
    var roles = userModule.getRoles(true);
    var users = userModule.getUsers();
    var actions = ["Enforce"];
    context.roles = roles;
    context.users = users;
    context.actions = actions;
    return context;
}