(function (server, user) {

    var log = new Log();

    var processPerms = function (perms, fn) {
        var perm, actions, i, length;
        for (perm in perms) {
            if (perms.hasOwnProperty(perm)) {
                actions = perms[perm];
                length = actions.length;
                for (i = 0; i < length; i++) {
                    fn(perm, actions[i]);
                }
            }
        }
    };

    var UserManager = function (serv, tenantId) {
        this.server = serv;
        this.tenantId = tenantId || server.superTenant.tenantId;
        var realmService = server.osgiService('org.wso2.carbon.user.core.service.RealmService'),
            realm = realmService.getTenantUserRealm(this.tenantId);
        this.manager = realm.getUserStoreManager();
        this.authorizer = realm.getAuthorizationManager();
    };
    user.UserManager = UserManager;

    UserManager.prototype.getUser = function (username) {
        if (!this.manager.isExistingUser(username)) {
            return null;
        }
        return new user.User(this, username);
    };
    UserManager.prototype.getRoleListOfUser = function (username) {
	        return this.manager.getRoleListOfUser(username);
	    };
    UserManager.prototype.addUser = function (username, password, roles, claims, profile) {
        this.manager.addUser(username, password, roles || [], claims || null, profile);
    };
    UserManager.prototype.changePassword = function (username, newPassword, oldPassword) {
        this.manager.updateCredential(username, newPassword, oldPassword);
    };

    UserManager.prototype.removeUser = function (username) {
        this.manager.deleteUser(username);
    };

    UserManager.prototype.userExists = function (username) {
        return this.manager.isExistingUser(username);
    };

    UserManager.prototype.roleExists = function (role) {
        return this.manager.isExistingRole(role);
    };
	UserManager.prototype.updateRole = function (previousRoleName, newRoleName) {
        return this.manager.updateRoleName(previousRoleName, newRoleName);
    };
    UserManager.prototype.getClaims = function (username, profile) {
        return this.manager.getUserClaimValues(username, profile);
    };
	UserManager.prototype.getClaimsForSet = function (username,claims, profile) {
        return this.manager.getUserClaimValues(username,claims, profile);
    };
    UserManager.prototype.getClaim = function (username, claim, profile) {
        return this.manager.getUserClaimValue(username, claim, profile);
    };

    UserManager.prototype.setClaims = function (username, claims, profile) {
        return this.manager.setUserClaimValues(username, claims, profile);
    };

    UserManager.prototype.setClaim = function (username, claim, value, profile) {
        return this.manager.setUserClaimValue(username, claim, value, profile);
    };

    UserManager.prototype.isAuthorized = function (role, permission, action) {
        return this.authorizer.isRoleAuthorized(role, permission, action);
    };
 	UserManager.prototype.updateRoleListOfUser = function(name, deletedRoles, newRoles){
    return this.manager.updateRoleListOfUser(name, deletedRoles, newRoles);
    };
    UserManager.prototype.updateUserListOfRole = function(name, deletedUsers, newUsers){
    return this.manager.updateUserListOfRole(name, deletedUsers, newUsers);
    };
    UserManager.prototype.listUsers = function (filter) {
        if(filter){
            return this.manager.listUsers(filter, -1);
        }else{
            return this.manager.listUsers("*", -1);
        }
    };
    UserManager.prototype.addRole = function (role, users, permissions) {
        var perms = [],
            Permission = Packages.org.wso2.carbon.user.api.Permission;
        processPerms(permissions, function (id, action) {
            perms.push(new Permission(id, action));
        });
        this.manager['addRole(java.lang.String,java.lang.String[],org.wso2.carbon.user.api.Permission[])']
            (role, users, perms);
    };
    UserManager.prototype.clearResourceAuthorizations = function (resource) {
        this.authorizer.clearResourceAuthorizations(resource)
    };

    UserManager.prototype.removeRole = function (role) {
        this.manager.deleteRole(role);
    };

    UserManager.prototype.allRoles = function () {
        return this.manager.getRoleNames();
    };
	UserManager.prototype.getUserListOfRole = function (role) {
        return this.manager.getUserListOfRole(role);
    };
    /**
     * um.authorizeRole('store-admin', '/permissions/mypermission', 'ui-execute');
     *
     * um.authorizeRole('store-admin', {
     *      '/permissions/myperm1' : ['read', 'write'],
     *      '/permissions/myperm2' : ['read', 'write']
     * });
     *
     * @param role
     * @param permission
     * @param action
     */
    UserManager.prototype.authorizeRole = function (role, permission, action) {
        var that = this;
        if (permission instanceof String || typeof permission === 'string') {
            if (!that.isAuthorized(role, permission, action)) {
                that.authorizer.authorizeRole(role, permission, action);
            }
        } else {
            processPerms(permission, function (id, action) {
                if (!that.isAuthorized(role, id, action)) {
                    that.authorizer.authorizeRole(role, id, action);
                    if (log.isDebugEnabled()) {
                        log.debug('permission added(role:permission:action) - ' + role + ':' + id + ':' + action);
                    }
                }
            });
        }
    };

    /**
     * um.denyRole('store-admin', '/permissions/mypermission', 'ui-execute');
     *
     * um.denyRole('store-admin', {
     *      '/permissions/myperm1' : ['read', 'write'],
     *      '/permissions/myperm2' : ['read', 'write']
     * });
     *
     * @param role
     * @param permission
     * @param action
     */
    UserManager.prototype.denyRole = function (role, permission, action) {
        var that = this;
        if (permission instanceof String || typeof permission === 'string') {
            that.authorizer.denyRole(role, permission, action);
        } else {
            processPerms(permission, function (id, action) {
                that.authorizer.denyRole(role, id, action);
            });
        }
    };

    /**
     * getAllowedUIResources      This will return all allowed UI resource for given user
     * @param  userName           User name
     * @param  permissionRootPath User permission root path
     * @return                    Array with permission resource paths
     */
    UserManager.prototype.getAllowedUIResources = function(userName, permissionRootPath){
        var allowedResources = this.authorizer.getAllowedUIResourcesForUser(userName, permissionRootPath);
        return allowedResources;
    };

}(server, user));
