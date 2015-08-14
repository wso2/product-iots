function onRequest(context) {
    var constants = require("/modules/constants.js");
    var carbonUser = session.get(constants.USER_SESSION_KEY);

    var links = {
        "users": [],
        "policies": [],
        "profiles": [],
        "device-mgt": [],
        "group-mgt": [],
        "store": [],
        "dashboard": [],
        "analytics" : [],
        "events" : []
    };
    var dashboardLink = {
        title: "Go back to Dashboard",
        icon: "fw-left-arrow",
        url: "/iotserver/dashboard"
    };

    var deviceMgtLink = {
        title: "Go back to My Devices",
        icon: "fw-left-arrow",
        url: "/iotserver/devices"
    };

    var groupMgtLink = {
        title: "Go back to Groups",
        icon: "fw-left-arrow",
        url: "/iotserver/groups"
    };

    var storeLink = {
        title: "Go back to Store",
        icon: "fw-left-arrow",
        url: "/iotserver"
    };

    links.users.push(dashboardLink);
    links.policies.push(dashboardLink);
    links.profiles.push(dashboardLink);
    links.events.push(dashboardLink);

    //links.store.push(dashboardLink);
    links.store.push(storeLink);

    links['group-mgt'].push(dashboardLink);
    var groupId = request.getParameter("groupId");
    if (groupId){
        links.analytics.push(groupMgtLink);
        links['device-mgt'].push(groupMgtLink);
    }else{
        links.analytics.push(deviceMgtLink);
        links['device-mgt'].push(dashboardLink);
    }

    if (!carbonUser) {
        //user is not logged in
    }else{
        var userModule = require("/modules/user.js").userModule;
        var permissions = userModule.getUIPermissions();

        context.permissions = permissions;

        //if (permissions.ADD_USER) {
        //    links.users.push({
        //        title: "Add User",
        //        icon: "fw-add-user",
        //        url: "/iotserver/users/add-user"
        //    });
        //}
        if (permissions.ADD_POLICY) {
            links.policies.push({
                title: "Add Policy",
                icon: "fw-policy",
                url: "/iotserver/policies/add-policy"
            });
        }
        //if (permissions.ADD_USER) {
        //    links.profiles.push({
        //        title: "Add Profile",
        //        icon: "fw-settings",
        //        url: "/iotserver/profiles/add-profile"
        //    });
        //}
        if (permissions.ADD_DEVICE) {
            links["device-mgt"].push({
                title: "Add Device",
                icon: "fw-add",
                url: "/iotserver/devices/add-device"
            });
        }
        if (permissions.ADD_DEVICE) {
            links["device-mgt"].push({
                title: "Add Group",
                icon: "fw-add",
                url: "/iotserver/groups/add-group"
            });
            links["group-mgt"].push({
                title: "Add Group",
                icon: "fw-add",
                url: "/iotserver/groups/add-group"
            });
        }
    }// end-if-user

    context.currentActions = links[context.link];
    return context;
}