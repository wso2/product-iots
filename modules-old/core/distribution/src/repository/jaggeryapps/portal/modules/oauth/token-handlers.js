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

/**
 * -----------------------------------------------------
 * Following module includes handlers
 * at Jaggery Layer for handling OAuth tokens.
 * -----------------------------------------------------
 */
var handlers = function () {
    var log = new Log("/modules/oauth/token-handlers.js");

    var tokenUtil = require("/modules/oauth/token-handler-utils.js")["utils"];
    var constants = require("/modules/constants.js");
    var configs = require('/configs/portal.js').config();

    var publicMethods = {};
    var privateMethods = {};

    /**
     * Get an AccessToken pair based on username and password
     * @param username   username of the logged user
     * @param password   password of the logged user
     */
    publicMethods["setupTokenPairByPasswordGrantType"] = function (username, password) {
        if (!username || !password) {
            throw new Error("{/modules/oauth/token-handlers.js} Could not set up access token pair by " +
                "password grant type. Either username of logged in user, password or both are missing " +
                    "as input - setupTokenPairByPasswordGrantType(x, y)");
        } else {
            privateMethods.setUpEncodedTenantBasedClientAppCredentials(username);
            var encodedClientAppCredentials =
                session.get(constants["ENCODED_TENANT_BASED_CLIENT_APP_CREDENTIALS_PORTAL_APP"]);
            if (!encodedClientAppCredentials) {
                throw new Error("{/modules/oauth/token-handlers.js} Could not set up access token pair by " +
                    "password grant type. Encoded client credentials are " +
                        "missing - setupTokenPairByPasswordGrantType(x, y)");
            } else {
                var tokenData;
                // tokenPair will include current access token as well as current refresh token
                var arrayOfScopes = configs["authorization"]["methods"]["oauth"]["attributes"]["scopes"];
                var stringOfScopes = "";
                arrayOfScopes.forEach(function (entry) {
                    stringOfScopes += entry + " ";
                });
                tokenData = tokenUtil.
                    getTokenPairAndScopesByPasswordGrantType(username,
                        encodeURIComponent(password), encodedClientAppCredentials, stringOfScopes);
                if (!tokenData) {
                    throw new Error("{/app/modules/oauth/token-handlers.js} Could not set up " +
                        "token pair by password grant type. Error in token " +
                            "retrieval - setupTokenPairByPasswordGrantType(x, y)");
                } else {
                    var tokenPair = {};
                    tokenPair["accessToken"] = tokenData["accessToken"];
                    tokenPair["refreshToken"] = tokenData["refreshToken"];
                    // setting up token pair into session context as a string
                    session.put(constants["ACCESS_TOKEN_PAIR_IDENTIFIER_FOR_PORTAL"], stringify(tokenPair));
                    var scopes = tokenData.scopes.split(" ");
                    // adding allowed scopes to the session
                    session.put(constants["ALLOWED_SCOPES"], scopes);
                }
            }
        }
    };

    /**
     * Get an AccessToken pair based on SAML assertion
     * @param samlToken         SAML assertion
     * @param username          {{clientId:"", clientSecret:""}}
     */
    publicMethods["setupTokenPairBySamlGrantType"] = function (username, samlToken) {
        if (!username || !samlToken) {
            throw new Error("{/modules/oauth/token-handlers.js} Could not set up access token pair by " +
                "saml grant type. Either username of logged in user, samlToken or both are missing " +
                    "as input - setupTokenPairBySamlGrantType(x, y)");
        } else {
            privateMethods.setUpEncodedTenantBasedClientAppCredentials(username);
            var encodedClientAppCredentials =
                session.get(constants["ENCODED_TENANT_BASED_CLIENT_APP_CREDENTIALS_PORTAL_APP"]);
            if (!encodedClientAppCredentials) {
                throw new Error("{/app/modules/oauth/token-handlers.js} Could not set up access token pair " +
                    "by saml grant type. Encoded client credentials are " +
                        "missing - setupTokenPairBySamlGrantType(x, y)");
            } else {
                var tokenData;
                // accessTokenPair will include current access token as well as current refresh token
                tokenData = tokenUtil.
                    getTokenPairAndScopesByJWTGrantType(username, encodedClientAppCredentials, "PRODUCTION");
                if (!tokenData) {
                    throw new Error("{/modules/oauth/token-handlers.js} Could not set up token " +
                        "pair by saml grant type. Error in token " +
                            "retrieval - setupTokenPairBySamlGrantType(x, y)");
                } else {
                    var tokenPair = {};
                    tokenPair["accessToken"] = tokenData["accessToken"];
                    tokenPair["refreshToken"] = tokenData["refreshToken"];
                    // setting up access token pair into session context as a string
                    session.put(constants["ACCESS_TOKEN_PAIR_IDENTIFIER_FOR_PORTAL"], stringify(tokenPair));

                    var scopes = tokenData.scopes.split(" ");
                    // adding allowed scopes to the session
                    session.put(constants["ALLOWED_SCOPES"], scopes);
                }
            }
        }
    };

    /**
     * Set access token and refresh token using refresh token grant type
     */
    publicMethods["refreshTokenPair"] = function () {
        var currentTokenPair = parse(session.get(constants["ACCESS_TOKEN_PAIR_IDENTIFIER_FOR_PORTAL"]));
        // currentTokenPair includes current access token as well as current refresh token
        var encodedClientAppCredentials
            = session.get(constants["ENCODED_TENANT_BASED_CLIENT_APP_CREDENTIALS_PORTAL_APP"]);
        if (!currentTokenPair || !encodedClientAppCredentials) {
            throw new Error("{/modules/oauth/token-handlers.js} Error in refreshing tokens. Either the " +
                "token pair, encoded client app credentials or both input are not found under " +
                    "session context - refreshTokenPair()");
        } else {
            var newTokenPair = tokenUtil.
                getNewTokenPairByRefreshToken(currentTokenPair["refreshToken"], encodedClientAppCredentials);
            if (!newTokenPair) {
                log.error("{/app/modules/oauth/token-handlers.js} Error in refreshing token pair. " +
                    "Unable to update session context with new access token pair - refreshTokenPair()");
            } else {
                session.put(constants["ACCESS_TOKEN_PAIR_IDENTIFIER_FOR_PORTAL"], stringify(newTokenPair));
            }
        }
    };

    /**
     * If gateway is enable, apiManagerClientAppRegistrationServiceURL is used to create an oauth application or
     * else DCR endpoint is used to create an oauth application
     * @param username   username of current logged user
     */
    privateMethods["setUpEncodedTenantBasedClientAppCredentials"] = function (username) {
        if (!username) {
            throw new Error("{/modules/oauth/token-handlers.js} Could not set up encoded tenant based " +
                "client credentials to session context. No username of logged in user is found as " +
                    "input - setUpEncodedTenantBasedClientAppCredentials(x)");
        } else {
            if (configs["authorization"]["methods"]["oauth"]["attributes"]["apimgt-gateway"]) {
				var tenantBasedClientAppCredentials = tokenUtil.getTenantBasedClientAppCredentials(username);
				if (!tenantBasedClientAppCredentials) {
					throw new Error("{/modules/oauth/token-handlers.js} Could not set up encoded tenant " +
					"based client credentials to session context as the server is unable " +
					"to obtain such credentials - setUpEncodedTenantBasedClientAppCredentials(x)");
				} else {
					var encodedTenantBasedClientAppCredentials =
						tokenUtil.encode(tenantBasedClientAppCredentials["clientId"] + ":" +
						tenantBasedClientAppCredentials["clientSecret"]);
					// setting up encoded tenant based client credentials to session context.
					session.put(constants["ENCODED_TENANT_BASED_CLIENT_APP_CREDENTIALS_PORTAL_APP"],
						encodedTenantBasedClientAppCredentials);
				}
            } else {
                var dynamicClientAppCredentials = tokenUtil.getDynamicClientAppCredentials(username);
                if (!dynamicClientAppCredentials) {
                    throw new Error("{/modules/oauth/token-handlers.js} Could not set up encoded tenant based " +
                    "client credentials to session context as the server is unable to obtain " +
                    "dynamic client credentials - setUpEncodedTenantBasedClientAppCredentials(x)");
                }
                var encodedTenantBasedClientAppCredentials =
                    tokenUtil.encode(dynamicClientAppCredentials["clientId"] + ":" +
                    dynamicClientAppCredentials["clientSecret"]);
                // setting up encoded tenant based client credentials to session context.
                session.put(constants["ENCODED_TENANT_BASED_CLIENT_APP_CREDENTIALS_PORTAL_APP"],
                    encodedTenantBasedClientAppCredentials);
            }

        }
    };

    return publicMethods;
}();