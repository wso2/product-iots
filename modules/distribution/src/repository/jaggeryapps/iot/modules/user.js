/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var userModule;
userModule = function () {
    var log = new Log("modules/user.js");

    var constants = require("/modules/constants.js");
    var utility = require("/modules/utility.js").utility;

    var userManagementService = utility.getUserManagementService();

    var publicMethods = {};
    var privateMethods = {};

    /**
     * Authenticate a user when he or she attempts to login to DC.
     *
     * @param username Username of the user
     * @param password Password of the user
     * @param successCallback Function to be called at the event of successful authentication
     * @param failureCallback Function to be called at the event of failed authentication
     */
    publicMethods.login = function (username, password, successCallback, failureCallback) {
        var carbonModule = require("carbon");
        var carbonServer = application.get("carbonServer");
        try {
            // get tenant specific full user name.
            username = username + "@" + carbonModule.server.tenantDomain();
            // check if the user is an authenticated user.
            var isAuthenticated = carbonServer.authenticate(username, password);
            if (isAuthenticated) {
                var tenantUser = carbonModule.server.tenantUser(username);
                session.put(constants.USER_SESSION_KEY, tenantUser);
                successCallback(tenantUser);
            } else {
                failureCallback();
            }
        } catch (e) {
            throw e;
        }
    };

    /**
     * Add user to dc-user-store.
     *
     * @param username Username of the user
     * @param firstname First name of the user
     * @param lastname Last name of the user
     * @param emailAddress Email address of the user
     * @param userRoles Roles assigned to the user
     *
     * @returns {number} HTTP Status code 201 if succeeded, 409 if user already exists
     */
    publicMethods.addUser = function (username, firstname, lastname, emailAddress, userRoles) {
        var carbon = require('carbon');
        var tenantId = carbon.server.tenantId();
        var url = carbon.server.address('https') + "/admin/services";
        var server = new carbon.server.Server(url);
        var userManager = new carbon.user.UserManager(server, tenantId);

        try {
            if (userManager.userExists(username)) {
                if (log.isDebugEnabled()) {
                    log.debug("A user with name '" + username + "' already exists.");
                }
                // http status code 409 refers to - conflict.
                return 409;
            } else {
                var initialUserPassword = privateMethods.generateInitialUserPassword();
                var defaultUserClaims = privateMethods.buildDefaultUserClaims(firstname, lastname, emailAddress);

                userManager.addUser(username, initialUserPassword, userRoles, defaultUserClaims, "default");
                privateMethods.inviteUserToEnroll(username, initialUserPassword);
                if (log.isDebugEnabled()) {
                    log.debug("A new user with name '" + username + "' was created.");
                }
                // http status code 201 refers to - created.
                return 201;
            }
        } catch (e) {
            throw e;
        }
    };

    /**
     * Remove an existing user from mdm-user-store.
     *
     * @param username Username of the user
     * @returns {number} HTTP Status code 200 if succeeded, 409 if the user does not exist
     */
    publicMethods.removeUser = function (username) {
        var carbon = require('carbon');
        var tenantId = carbon.server.tenantId();
        var url = carbon.server.address('https') + "/admin/services";
        var server = new carbon.server.Server(url);
        var userManager = new carbon.user.UserManager(server, tenantId);

        try {
            if (userManager.userExists(username)) {
                userManager.removeUser(username);
                if (log.isDebugEnabled()) {
                    log.debug("An existing user with name '" + username + "' was removed.");
                }
                // http status code 200 refers to - success.
                return 200;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("A user with name '" + username + "' does not exist to remove.");
                }
                // http status code 409 refers to - conflict.
                return 409;
            }
        } catch (e) {
            throw e;
        }
    };

    /**
     * Private method to be used by addUser() to
     * generate an initial user password for a user.
     * This will be the password used by a user for his initial login to the system.
     *
     * @returns {string} Initial User Password
     */
    privateMethods.generateInitialUserPassword = function () {
        var passwordLength = 6;
        //defining the pool of characters to be used for initial password generation
        var lowerCaseCharset = "abcdefghijklmnopqrstuvwxyz";
        var upperCaseCharset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        var numericCharset = "0123456789";

        var totalCharset = lowerCaseCharset + upperCaseCharset + numericCharset;
        var totalCharsetLength = totalCharset.length;

        var initialUserPassword = "";
        for (var i = 0; i < passwordLength; ++i) {
            initialUserPassword += totalCharset.charAt(Math.floor(Math.random() * totalCharsetLength));
        }
        if (log.isDebugEnabled()) {
            log.debug("Initial password created for new user : " + initialUserPassword);
        }
        return String(initialUserPassword);
    };

    /**
     * Build default user claims.
     *
     * @param firstname First name of the user
     * @param lastname Last name of the user
     * @param emailAddress Email address of the user
     *
     * @returns {Object} Default user claims to be provided
     */
    privateMethods.buildDefaultUserClaims = function (firstname, lastname, emailAddress) {
        var defaultUserClaims = {
            "http://wso2.org/claims/givenname": firstname,
            "http://wso2.org/claims/lastname": lastname,
            "http://wso2.org/claims/emailaddress": emailAddress
        };
        if (log.isDebugEnabled()) {
            log.debug("ClaimMap created for new user : " + stringify(defaultUserClaims));
        }
        return defaultUserClaims;
    };

    publicMethods.addPermissions = function (permissionList, path, init) {
        var carbonModule = require("carbon");
        var carbonServer = application.get("carbonServer");
        var options = {system: true};
        if (init == "login") {
            var carbonUser = session.get(constants.USER_SESSION_KEY);
            if (carbonUser) {
                options.tenantId = carbonUser.tenantId;
            }
        }
        var registry = new carbonModule.registry.Registry(carbonServer, options);
        var i, permission, resource;
        for (i = 0; i < permissionList.length; i++) {
            permission = permissionList[i];
            resource = {
                collection : true,
                name : permission.name,
                properties : {
                    name : permission.name
                }
            };
            registry.put("/_system/governance/permission/" + path + "/" + permission.key, resource);
        }
    };

    publicMethods.getUsers = function () {
        var carbon = require('carbon');

        var carbonUser = session.get(constants.USER_SESSION_KEY);
        if (!carbonUser) {
            log.error("User object was not found in the session");
            throw constants.ERRORS.USER_NOT_FOUND;
        }

        var userList;
        try{
            userList = userManagementService.getUsersForTenant(carbonUser.tenantId);
        }catch(e){
            log.error("Error occurred while reading all users");
            return [];
        }

        var users = [];
        var i, userObject;
        for (i = 0; i < userList.size(); i++) {
            userObject = userList.get(i);
            users.push({
                "username" : userObject.getUserName(),
                "email" : userObject.getEmail(),
                "name" : userObject.getFirstName() + " " + userObject.getLastName()
            });
        }
        return users;
    };

    publicMethods.isAuthorized = function (permission) {
        var carbonModule = require("carbon");
        var carbonServer = application.get("carbonServer");
        var carbonUser = session.get(constants.USER_SESSION_KEY);
        if (!carbonUser) {
            log.error("User object was not found in the session");
            throw constants.ERRORS.USER_NOT_FOUND;
        }
        var userManager = new carbonModule.user.UserManager(carbonServer, carbonUser.tenantId);
        var user = new carbonModule.user.User(userManager, carbonUser.username);
        return user.isAuthorized(permission, "ui.execute");
    };

    publicMethods.logout = function (successCallback) {
        session.invalidate();
        successCallback();
    };

    return publicMethods;
}();


