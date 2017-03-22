/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

var utils = function () {
    var log = new Log("/modules/oauth/token-handler-utils.js");

    var configs = require('/configs/portal.js').config();
    var constants = require("/modules/constants.js");
    var carbon = require("carbon");

    //noinspection JSUnresolvedVariable
    var Base64 = Packages.org.apache.commons.codec.binary.Base64;
    //noinspection JSUnresolvedVariable
    var String = Packages.java.lang.String;

    var publicMethods = {};
    var privateMethods = {};

    publicMethods["encode"] = function (payload) {
        return String(Base64.encodeBase64(String(payload).getBytes()));
    };

    publicMethods["decode"] = function (payload) {
        return String(Base64.decodeBase64(String(payload).getBytes()));
    };

    /**
     * Check whether this application is oauth enable or not
     * @returns boolean if oauth enable
     */
    publicMethods["checkOAuthEnabled"] = function () {
        if (constants.AUTHORIZATION_TYPE_OAUTH === configs["authorization"]["activeMethod"]) {
            return true;
        }
        return false;
    };

    /**
     * Set access token into xml http request header
     * @param xhr     xml http request
     * @returns {*}   xhr which has access token it's header
     */
    publicMethods["setAccessToken"] = function (xhr, callback) {
        var accessToken;
        if (publicMethods.checkOAuthEnabled()) {
            try {
                accessToken = parse(session.get(constants.ACCESS_TOKEN_PAIR_IDENTIFIER_FOR_PORTAL))["accessToken"];
                xhr.setRequestHeader(constants.AUTHORIZATION_HEADER, constants.BEARER_PREFIX + accessToken);
            } catch (exception) {
                log.error("Access token hasn't been set yet, " + exception);
            } finally {
                callback(xhr);
            }
        }
        callback(xhr);
    };

    /**
     * Get access token of current logged user
     * @param callBack response with access token
     */
    publicMethods["getAccessToken"] = function (callBack) {
        var accessToken = null;
        if (publicMethods.checkOAuthEnabled()) {
            try {
                accessToken =  parse(session.get(constants.ACCESS_TOKEN_PAIR_IDENTIFIER_FOR_PORTAL))["accessToken"];
            } catch (exception) {
                log.error("Access token hasn't been set yet, " + exception);
            } finally {
                callBack(accessToken);
            }
        }
        callBack(accessToken);
    };

    /**
     * Create error message which adhere to xml http response object
     * @param statusCode       response status code
     * @param status           response status
     * @param responseText     response message
     * @returns {{statusCode: *, status: *, responseText: *}}
     */
    publicMethods["createXHRObject"] = function (statusCode, status, responseText) {
        return {"statusCode": statusCode, "status": status, "responseText": responseText};
    };

    /**
     * check whether user already logged to system before invoking any apis
     * @param callBack
     */
    publicMethods["isUserAuthorized"] = function (callBack) {
        if (session.get("Loged") !== constants.LOGIN_MESSAGE) {
            callBack(false);
        } else {
            callBack(true);
        }
    };

    /**
     * Get identity provider uir
     * @returns {*}
     */
    publicMethods["getIdPServerURL"] = function () {
        return configs["authorization"]["methods"]["oauth"]["attributes"]["oauthProvider"]["tokenServiceURL"];
    };

    /**
     * Get an Access token pair based on client secret
     * @param encodedClientKeys  {{clientId:"", clientSecret:""}}
     * @param scope              eg: PRODUCTION
     * @param idPServer          identity provider url
     * @returns {{accessToken: *, refreshToken: *}}
     */
    publicMethods["getTokenWithClientSecretType"] = function (encodedClientKeys, scope, idPServer) {
        var xhr = new XMLHttpRequest();
        var tokenEndpoint = idPServer;
        xhr.open(constants.HTTP_POST, tokenEndpoint, false);
        xhr.setRequestHeader(constants.CONTENT_TYPE_IDENTIFIER, constants.APPLICATION_X_WWW_FOR_URLENCODED);
        xhr.setRequestHeader(constants.AUTHORIZATION_HEADER, constants.BASIC_PREFIX + encodedClientKeys);
        xhr.send("grant_type=client_credentials&scope=" + scope);
        var tokenPair = {};
        if (xhr.status == constants.HTTP_ACCEPTED) {
            var data = parse(xhr.responseText);
            tokenPair.refreshToken = data.refresh_token;
            tokenPair.accessToken = data.access_token;
        } else if (xhr.status == constants.HTTP_USER_NOT_AUTHENTICATED) {
            log.error("Error in obtaining token with client secret grant type, You are not authenticated yet");
            return null;
        } else {
            log.error("Error in obtaining token with client secret grant type, This might be a problem with client meta " +
                "data which required for client secret grant type");
            return null;
        }
        return tokenPair;
    };


    /**
     * This will create client id and client secret for a given application
     * @param properties    "callbackUrl": "",
     *                      "clientName": "",
     *                      "owner": "",
     *                      "applicationType": "",
     *                      "grantType": "",
     *                      "saasApp" :"",
     *                      "dynamicClientRegistrationEndPoint" : ""
     *
     * @returns {{clientId:*, clientSecret:*}}
     */
    publicMethods["getDynamicClientAppCredentials"] = function (username) {
        // setting up dynamic client application properties
        var dcAppProperties = {
            "applicationType": configs["authorization"]["methods"]["oauth"]["attributes"]["oauthProvider"]["appRegistration"]["appType"],
            "clientName": configs["authorization"]["methods"]["oauth"]["attributes"]["oauthProvider"]["appRegistration"]["clientName"],
            "owner": configs["authorization"]["methods"]["oauth"]["attributes"]["oauthProvider"]["appRegistration"]["owner"],
            "tokenScope": configs["authorization"]["methods"]["oauth"]["attributes"]["oauthProvider"]["appRegistration"]["tokenScope"],
            "grantType": configs["authorization"]["methods"]["oauth"]["attributes"]["oauthProvider"]["appRegistration"]["grantType"],
            "callbackUrl": configs["authorization"]["methods"]["oauth"]["attributes"]["oauthProvider"]["appRegistration"]["callbackUrl"],
            "saasApp" : configs["authorization"]["methods"]["oauth"]["attributes"]["oauthProvider"]["appRegistration"]["saasApp"]
        };

        var tenantDomain = carbon.server.tenantDomain({username: username});
        if (!tenantDomain) {
            log.error("{/modules/oauth/token-handler-utils.js} Error in retrieving tenant " +
                "based client application credentials. Unable to obtain a valid tenant domain for provided username "+
                username +"- getDynamicClientAppCredentials(x)");
            return null;
        } else {
            var cachedTenantBasedClientAppCredentials = privateMethods.
            getCachedTenantBasedClientAppCredentials(tenantDomain);
            if (cachedTenantBasedClientAppCredentials) {
                return cachedTenantBasedClientAppCredentials;
            } else {
                // calling dynamic client app registration service endpoint
                var requestURL = configs["authorization"]["methods"]["oauth"]["attributes"]["oauthProvider"]["appRegistration"]
                    ["dynamicClientAppRegistrationServiceURL"];
                var requestPayload = dcAppProperties;
                var token = publicMethods.encode(configs["authorization"]["methods"]["oauth"]["attributes"]["oauthProvider"]
                        ["appRegistration"]["owner"] + ":" + configs["authorization"]["methods"]["oauth"]["attributes"]
                        ["oauthProvider"]["appRegistration"]["password"]);
                var xhr = new XMLHttpRequest();
                xhr.open("POST", requestURL, false);
                xhr.setRequestHeader("Content-Type", "application/json");
                xhr.setRequestHeader("Authorization", "Basic "+ token);
                xhr.send(stringify(requestPayload));
                var dynamicClientAppCredentials = {};
                if (xhr["status"] == 201 || xhr["status"] == 200 && xhr["responseText"]) {
                    var responsePayload = parse(xhr["responseText"]);
                    var clientId = responsePayload["client_id"];
                    var clientSecret = responsePayload["client_secret"];
                    if(typeof clientId == "undefined"){
                        clientId = responsePayload["clientId"];
                    }
                    if(typeof clientSecret == "undefined"){
                        clientSecret = responsePayload["clientSecret"];
                    }
                    dynamicClientAppCredentials["clientId"] = clientId;
                    dynamicClientAppCredentials["clientSecret"] = clientSecret;
                    privateMethods.
                    setCachedTenantBasedClientAppCredentials(tenantDomain, dynamicClientAppCredentials);
                } else if (xhr["status"] == 400) {
                    log.error("{/modules/oauth/token-handler-utils.js - getDynamicClientAppCredentials()} " +
                        "Bad request. Invalid data provided as dynamic client application properties.");
                    dynamicClientAppCredentials = null;
                } else {
                    log.error("{/modules/oauth/token-handler-utils.js - getDynamicClientAppCredentials()} " +
                        "Error in retrieving dynamic client credentials.");
                    dynamicClientAppCredentials = null;
                }
                // returning dynamic client credentials
                return dynamicClientAppCredentials;
            }
        }
    };

    /**
     * If gateway is enable, apiManagerClientAppRegistrationServiceURL is used to create oauth application
     * @param username   username of current logged user
     * @returns {{clientId:*, clientSecret:*}}
     */
    publicMethods["getTenantBasedClientAppCredentials"] = function (username) {
        if (!username) {
            log.error("{/modules/oauth/token-handler-utils.js} Error in retrieving tenant " +
                "based client app credentials. No username " +
                "as input - getTenantBasedClientAppCredentials(x)");
            return null;
        } else {
            //noinspection JSUnresolvedFunction, JSUnresolvedVariable
            var tenantDomain = carbon.server.tenantDomain({username: username});

            if (!tenantDomain) {
                log.error("{/modules/oauth/token-handler-utils.js} Error in retrieving tenant " +
                    "based client application credentials. Unable to obtain a valid tenant domain for provided " +
                    "username - getTenantBasedClientAppCredentials(x, y)");
                return null;
            } else {
                var cachedTenantBasedClientAppCredentials = privateMethods.
                getCachedTenantBasedClientAppCredentials(tenantDomain);
                if (cachedTenantBasedClientAppCredentials) {
                    return cachedTenantBasedClientAppCredentials;
                } else {
                    var adminUsername = configs["authorization"]["methods"]["oauth"]["attributes"]["adminUser"];
                    var adminUserTenantId = configs["authorization"]["methods"]["oauth"]["attributes"]
                        ["adminUserTenantId"];
                    //claims required for jwtAuthenticator.
                    var claims = {"http://wso2.org/claims/enduserTenantId": adminUserTenantId,
                        "http://wso2.org/claims/enduser": adminUsername};
                    var jwtToken = publicMethods.getJwtToken(adminUsername, claims);
                    // register a tenant based client app at API Manager
                    var applicationName = configs["authorization"]["methods"]["oauth"]["attributes"]["oauthProvider"]
                            ["appRegistration"]["clientName"] + "_" + tenantDomain;
                    var requestURL = configs["authorization"]["methods"]["oauth"]["attributes"]["oauthProvider"]
                            ["appRegistration"]["apiManagerClientAppRegistrationServiceURL"] +
                        "?tenantDomain=" + tenantDomain + "&applicationName=" + applicationName;
                    var xhr = new XMLHttpRequest();
                    xhr.open("POST", requestURL, false);
                    xhr.setRequestHeader("Content-Type", "application/json");
                    xhr.setRequestHeader("X-JWT-Assertion", "" + jwtToken);
                    xhr.send();
                    if ((xhr["status"] == 201 || xhr["status"] == 200) && xhr["responseText"]) {
                        var responsePayload = parse(xhr["responseText"]);
                        var tenantBasedClientAppCredentials = {};
                        var clientId = responsePayload["client_id"];
                        var clientSecret = responsePayload["client_secret"];
                        if(typeof clientId == "undefined"){
                            clientId = responsePayload["clientId"];
                        }
                        if(typeof clientSecret == "undefined"){
                            clientSecret = responsePayload["clientSecret"];
                        }
                        tenantBasedClientAppCredentials["clientId"] = clientId;
                        tenantBasedClientAppCredentials["clientSecret"] = clientSecret;
                        privateMethods.
                        setCachedTenantBasedClientAppCredentials(tenantDomain, tenantBasedClientAppCredentials);
                        return tenantBasedClientAppCredentials;
                    } else {
                        log.error("{/modules/oauth/token-handler-utils.js} Error in retrieving tenant " +
                            "based client application credentials from API " +
                            "Manager - getTenantBasedClientAppCredentials(x, y)");
                        return null;
                    }
                }
            }
        }
    };

    /**
     * Caching oauth application credentials
     * @param tenantDomain            tenant domain where application is been created
     * @param clientAppCredentials    {{clientId:*, clientSecret:*}}
     */
    privateMethods["setCachedTenantBasedClientAppCredentials"] = function (tenantDomain, clientAppCredentials) {
        var cachedTenantBasedClientAppCredentialsMap = application.get(constants["CACHED_CREDENTIALS_PORTAL_APP"]);
        if (!cachedTenantBasedClientAppCredentialsMap) {
            cachedTenantBasedClientAppCredentialsMap = {};
            cachedTenantBasedClientAppCredentialsMap[tenantDomain] = clientAppCredentials;
            application.put(constants["CACHED_CREDENTIALS_PORTAL_APP"], cachedTenantBasedClientAppCredentialsMap);
        } else if (!cachedTenantBasedClientAppCredentialsMap[tenantDomain]) {
            cachedTenantBasedClientAppCredentialsMap[tenantDomain] = clientAppCredentials;
        }
    };

    /**
     * Get oauth application credentials from cache
     * @param tenantDomain    tenant domain where application is been created
     * @returns {{clientId:*, clientSecret:*}}
     */
    privateMethods["getCachedTenantBasedClientAppCredentials"] = function (tenantDomain) {
        var cachedTenantBasedClientAppCredentialsMap = application.get(constants["CACHED_CREDENTIALS_PORTAL_APP"]);
        if (!cachedTenantBasedClientAppCredentialsMap ||
            !cachedTenantBasedClientAppCredentialsMap[tenantDomain]) {
            return null;
        } else {
            return cachedTenantBasedClientAppCredentialsMap[tenantDomain];
        }
    };

    /**
     * Get access token and refresh token using password grant type
     * @param username                      username of the logged user
     * @param password                      password of the logged user
     * @param encodedClientAppCredentials   {{clientId:*, clientSecret:*}}
     * @param scopes                         scopes list
     * @returns {{accessToken: *, refreshToken: *}}
     */
    publicMethods["getTokenPairAndScopesByPasswordGrantType"] = function (username, password
        , encodedClientAppCredentials, scopes) {
        if (!username || !password || !encodedClientAppCredentials || !scopes) {
            log.error("{/modules/oauth/token-handler-utils.js} Error in retrieving access token by password " +
                "grant type. No username, password, encoded client app credentials or scopes are " +
                "found - getTokenPairAndScopesByPasswordGrantType(a, b, c, d)");
            return null;
        } else {
            // calling oauth provider token service endpoint
            var requestURL = configs["authorization"]["methods"]["oauth"]["attributes"]["oauthProvider"]
                ["tokenServiceURL"];
            var requestPayload = "grant_type=password&username=" +
                username + "&password=" + password + "&scope=" + scopes;

            var xhr = new XMLHttpRequest();
            xhr.open("POST", requestURL, false);
            xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
            xhr.setRequestHeader("Authorization", "Basic " + encodedClientAppCredentials);
            xhr.send(requestPayload);

            if (xhr["status"] == 200 && xhr["responseText"]) {
                var responsePayload = parse(xhr["responseText"]);
                var tokenData = {};
                tokenData["accessToken"] = responsePayload["access_token"];
                tokenData["refreshToken"] = responsePayload["refresh_token"];
                tokenData["scopes"] = responsePayload["scope"];
                return tokenData;
            } else {
                log.error("{/modules/oauth/token-handler-utils.js} Error in retrieving access token " +
                    "by password grant type - getTokenPairAndScopesByPasswordGrantType(a, b, c, d)");
                return null;
            }
        }
    };

	/**
	 * Get access token and refresh token using SAML grant type
	 * @param assertion
	 * @param encodedClientAppCredentials
	 * @param scopes
	 * @returns {{accessToken: *, refreshToken: *}}
	 */
	publicMethods["getTokenPairAndScopesByJWTGrantType"] = function (username, encodedClientAppCredentials, scopes) {
		if (!username || !encodedClientAppCredentials || !scopes) {
			log.error("{/app/modules/oauth/token-handler-utils.js} Error in retrieving access token by jwt " +
			"grant type. No assertion, encoded client app credentials or scopes are " +
			"found - getTokenPairAndScopesByJWTGrantType(x, y, z)");
			return null;
		} else {
			var JWTClientManagerServicePackagePath =
				"org.wso2.carbon.identity.jwt.client.extension.service.JWTClientManagerService";
			//noinspection JSUnresolvedFunction, JSUnresolvedVariable
			var JWTClientManagerService = carbon.server.osgiService(JWTClientManagerServicePackagePath);
			//noinspection JSUnresolvedFunction
			var jwtClient = JWTClientManagerService.getJWTClient();
			// returning access token by JWT grant type
			var tokenInfo = jwtClient.getAccessToken(encodedClientAppCredentials,
				username, scopes);
			var tokenData = {};
			tokenData["accessToken"] = tokenInfo.getAccessToken();
			tokenData["refreshToken"] = tokenInfo.getRefreshToken();
			tokenData["scopes"] = tokenInfo.getScopes();
			return tokenData;
		}
	};

    /**
     * Get access token and refresh token using SAML grant type
     * @param assertion
     * @param encodedClientAppCredentials
     * @param scopes
     * @returns {{accessToken: *, refreshToken: *}}
     */
    publicMethods["getTokenPairAndScopesBySAMLGrantType"] = function (assertion, encodedClientAppCredentials, scopes) {
        if (!assertion || !encodedClientAppCredentials || !scopes) {
            log.error("{/modules/oauth/token-handler-utils.js} Error in retrieving access token by saml " +
                "grant type. No assertion, encoded client app credentials or scopes are " +
                "found - getTokenPairAndScopesBySAMLGrantType(x, y, z)");
            return null;
        } else {

            var assertionXML = publicMethods.decode(assertion);
            /*
             TODO: make assertion extraction with proper parsing.
             Since Jaggery XML parser seem to add formatting which causes signature verification to fail.
             */
            var assertionStartMarker = "<saml2:Assertion";
            var assertionEndMarker = "<\/saml2:Assertion>";
            var assertionStartIndex = assertionXML.indexOf(assertionStartMarker);
            var assertionEndIndex = assertionXML.indexOf(assertionEndMarker);

            var extractedAssertion;
            if (assertionStartIndex == -1 || assertionEndIndex == -1) {
                log.error("{/modules/oauth/token-handler-utils.js} Error in retrieving access token by saml grant " +
                    "type. Issue in assertion format - getTokenPairAndScopesBySAMLGrantType(x, y, z)");
                return null;
            } else {
                extractedAssertion = assertionXML.
                    substring(assertionStartIndex, assertionEndIndex) + assertionEndMarker;
                var encodedAssertion = publicMethods.encode(extractedAssertion);
                // calling oauth provider token service endpoint
                var requestURL = configs["authorization"]["methods"]["oauth"]["attributes"]["oauthProvider"]
                    ["tokenServiceURL"];
                var requestPayload = "grant_type=urn:ietf:params:oauth:grant-type:saml2-bearer&" +
                    "assertion=" + encodeURIComponent(encodedAssertion) + "&scope=" + scopes;
                var xhr = new XMLHttpRequest();
                xhr.open("POST", requestURL, false);
                xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
                xhr.setRequestHeader("Authorization", "Basic " + encodedClientAppCredentials);
                xhr.send(requestPayload);

                if (xhr["status"] == 200 && xhr["responseText"]) {
                    var responsePayload = parse(xhr["responseText"]);
                    var tokenData = {};
                    tokenData["accessToken"] = responsePayload["access_token"];
                    tokenData["refreshToken"] = responsePayload["refresh_token"];
                    tokenData["scopes"] = responsePayload["scope"];
                    return tokenData;
                } else {
                    log.error("{/modules/oauth/token-handler-utils.js} Error in retrieving access token " +
                        "by password grant type - getTokenPairAndScopesBySAMLGrantType(x, y, z)");
                    return null;
                }
            }
        }
    };

    /**
     * If access token is expired, try to refresh it using existing refresh token
     * @param callback
     */
    publicMethods["refreshAccessToken"] = function (callback) {
        try {
            if (publicMethods.checkOAuthEnabled()) {
                var currentTokenPair = parse(session.get(constants["ACCESS_TOKEN_PAIR_IDENTIFIER_FOR_PORTAL"]));
                // currentTokenPair includes current access token as well as current refresh token
                var encodedClientAppCredentials
                    = session.get(constants["ENCODED_TENANT_BASED_CLIENT_APP_CREDENTIALS_PORTAL_APP"]);
                if (!currentTokenPair || !encodedClientAppCredentials) {
                    callback(false);
                    throw new Error("{/modules/oauth/token-handlers.js} Error in refreshing tokens. Either the " +
                        "token pair, encoded client app credentials or both input are not found under " +
                        "session context - refreshTokenPair()");
                } else {
                    var newTokenPair = publicMethods.
                    getNewTokenPairByRefreshToken(currentTokenPair["refreshToken"], encodedClientAppCredentials);
                    if (!newTokenPair) {
                        log.error("{/app/modules/oauth/token-handlers.js} Error in refreshing token pair. " +
                            "Unable to update session context with new access token pair - refreshTokenPair()");
                        callback(false);
                    } else {
                        session.put(constants["ACCESS_TOKEN_PAIR_IDENTIFIER_FOR_PORTAL"], stringify(newTokenPair));
                        callback(true);
                    }
                }
            } else {
                log.error("You have not enable dynamic client yet");
                callback(false);
            }
        } catch (exception) {
            callback(false);
            throw "Error while refreshing existing access token, " + exception;
        }
    };

    /**
     * Get access token and refresh token using refresh token grant type
     * @param refreshToken                  refresh token
     * @param encodedClientAppCredentials   {{clientId:*, clientSecret:*}}
     * @param scopes
     * @returns {{accessToken: *, refreshToken: *}}
     */
    publicMethods["getNewTokenPairByRefreshToken"] = function (refreshToken, encodedClientAppCredentials, scopes) {
        if (!refreshToken || !encodedClientAppCredentials) {
            log.error("{/modules/oauth/token-handler-utils.js} Error in retrieving new access token " +
                "by current refresh token. No refresh token or encoded client app credentials are " +
                "found - getNewTokenPairByRefreshToken(x, y, z)");
            return null;
        } else {
            var requestURL = configs["authorization"]["methods"]["oauth"]["attributes"]["oauthProvider"]
                ["tokenServiceURL"];
            var requestPayload = "grant_type=refresh_token&refresh_token=" + refreshToken;
            if (scopes) {
                requestPayload = requestPayload + "&scope=" + scopes;
            }

            var xhr = new XMLHttpRequest();
            xhr.open("POST", requestURL, false);
            xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
            xhr.setRequestHeader("Authorization", "Basic " + encodedClientAppCredentials);
            xhr.send(requestPayload);

            if (xhr["status"] == 200 && xhr["responseText"]) {
                var responsePayload = parse(xhr["responseText"]);
                var tokenPair = {};
                tokenPair["accessToken"] = responsePayload["access_token"];
                tokenPair["refreshToken"] = responsePayload["refresh_token"];
                return tokenPair;
            } else {
                log.error("{/modules/oauth/token-handler-utils.js} Error in retrieving new access token by " +
                    "current refresh token - getNewTokenPairByRefreshToken(x, y, z)");
                return null;
            }
        }
    };

    /**
     * Get access token using JWT grant type
     * @param clientAppCredentials {{clientId:*, clientSecret:*}}
     * @returns {{accessToken: *, refreshToken: *}}
     */
    publicMethods["getAccessTokenByJWTGrantType"] =  function (clientAppCredentials) {
        if (!clientAppCredentials) {
            log.error("{/modules/oauth/token-handler-utils.js} Error in retrieving new access token " +
                "by current refresh token. No client app credentials are found " +
                "as input - getAccessTokenByJWTGrantType(x)");
            return null;
        } else {
            var JWTClientManagerServicePackagePath =
                "org.wso2.carbon.identity.jwt.client.extension.service.JWTClientManagerService";
            //noinspection JSUnresolvedFunction, JSUnresolvedVariable
            var JWTClientManagerService = carbon.server.osgiService(JWTClientManagerServicePackagePath);
            //noinspection JSUnresolvedFunction
            var jwtClient = JWTClientManagerService.getJWTClient();
            // returning access token by JWT grant type
            return jwtClient.getAccessToken(clientAppCredentials["clientId"], clientAppCredentials["clientSecret"],
                configs["authorization"]["methods"]["oauth"]["attributes"]["oauthProvider"]["appRegistration"]["owner"],
                null)["accessToken"];
        }
    };

    /**
     * Get jwt token
     * @param username  username of logged user
     * @param claims    claims which are required
     * @returns {"jwtToken"}
     */
    publicMethods["getJwtToken"] =  function (username, claims) {
        if (!username) {
            log.error("{/modules/oauth/token-handler-utils.js} Error in retrieving new jwt token");
            return null;
        } else {
            var JWTClientManagerServicePackagePath =
                "org.wso2.carbon.identity.jwt.client.extension.service.JWTClientManagerService";
            //noinspection JSUnresolvedFunction, JSUnresolvedVariable
            var JWTClientManagerService = carbon.server.osgiService(JWTClientManagerServicePackagePath);
            //noinspection JSUnresolvedFunction
            var jwtClient = JWTClientManagerService.getJWTClient();
            // returning access token by JWT grant type
            if (claims) {
                return jwtClient.getJwtToken(username, claims);
            } else {
                return jwtClient.getJwtToken(username);
            }
        }
    };
    return publicMethods;
}();
