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

var util = function () {
    var module = {};
    var Base64 = Packages.org.apache.commons.codec.binary.Base64;
    var String = Packages.java.lang.String;
    var log = new Log();

    /**
     * Encode the payload in Base64
     * @param payload
     * @returns {Packages.java.lang.String}
     */
    function encode(payload){
        return new String(Base64.encodeBase64(new String(payload).getBytes()));
    }

    /**
     * Get an AccessToken pair based on username and password
     * @param username
     * @param password
     * @param clientId
     * @param clientSecret
     * @param scope
     * @returns {{accessToken: "", refreshToken: ""}}
     */
    module.getTokenWithPasswordGrantType = function (username, password, clientId, clientSecret, scope) {
        var xhr = new XMLHttpRequest();
        var tokenEndpoint = "https://localhost:9443/oauth2/token";
        var encodedClientKeys = encode(clientId + ":" + clientSecret);
        xhr.open("POST", tokenEndpoint, false);
        xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        xhr.setRequestHeader("Authorization", "Basic " + encodedClientKeys);
        xhr.send("grant_type=password&username=" + username + "&password=" + password + "&scope=" + scope);
        delete password, delete clientSecret, delete encodedClientKeys;
        var tokenPair = {};
        if (xhr.status == 200) {
            var data = parse(xhr.responseText);
            tokenPair.refreshToken = data.refresh_token;
            tokenPair.accessToken = data.access_token;
        } else if (xhr.status == 403) {
            throw "Error in obtaining token with Password Grant Type";
        } else {
            throw "Error in obtaining token with Password Grant Type";
        }
        return tokenPair;
    };
    module.getTokenWithSAMLGrantType = function () {

    };
    module.refreshToken = function(tokenPair){
        var xhr = new XMLHttpRequest();
        var tokenEndpoint = "https://localhost:9443/oauth2/token";
        var encodedClientKeys = encode(clientId + ":" + clientSecret);
        xhr.open("POST", tokenEndpoint, false);
        xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        xhr.setRequestHeader("Authorization", "Basic " + encodedClientKeys);
        xhr.send("grant_type=refresh_token&refresh_token=" + tokenPair.refreshToken + "&scope=" + scope);
        delete password, delete clientSecret, delete encodedClientKeys;
        var tokenPair = {};
        if (xhr.status == 200) {
            var data = parse(xhr.responseText);
            tokenPair.refreshToken = data.refresh_token;
            tokenPair.accessToken = data.access_token;
        } else if (xhr.status == 403) {
            throw "Error in obtaining token with Password Grant Type";
        } else {
            throw "Error in obtaining token with Password Grant Type";
        }
        return tokenPair;
    };
    return module;
}();