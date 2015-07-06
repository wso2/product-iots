function onRequest(context) {
    // var log = new Log("user-listing");
    var userModule = require("/modules/user.js").userModule;
    var allUsers = userModule.getUsers();
    if (allUsers.length == 0) {
        context.users = [];
        context.listUsersStatus = "Oops, Sorry, No other Users found.";
    } else {
        var i, filteredUserList = [];
        for (i = 0; i < allUsers.length; i++) {
            //if (String(allUsers[i].username) != "admin") {
                filteredUserList.push(allUsers[i]);
            //}
        }
        context.users = filteredUserList;
        context.listUsersStatus = "Total number of Users found : " + filteredUserList.length;
    }
    context.permissions = userModule.getUIPermissions();
    return context;
}