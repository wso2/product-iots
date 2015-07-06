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

//Using https://github.com/js-cookie/js-cookie

var invokerUtil = function () {
    var context="/iotserver";
    var module = {};
    var flagAuth = false; // A flag to be used to test without oAuth
    function requestAccessToken(successCallback, errorCallback){
        $.ajax({
            url: context+"/token",
            type: "GET",
            success: function(){
                successCallback
            }
        }).fail(errorCallback);
    }
    function call(method, url, payload, successCallback, errorCallback){
        var accessToken = Cookies.get('accessToken');
        var execute = function(){
            var data = {
                url: url,
                type: method,
                contentType: "application/json",
                accept: "application/json",
                //dataType: "json",
                success: successCallback
            };
            if (payload){
                data.data = JSON.stringify(payload);
            }

            if (flagAuth){
                accessToken = Cookies.get('accessToken');
                data.headers = {
                    "Authorization": "Bearer " + accessToken
                };
            }
            $.ajax(data).fail(function(jqXHR){
                if(jqXHR.status == "401"){
                    window.location.replace(context);
                }else{
                    errorCallback(jqXHR);
                }
            });
        }
        if (flagAuth){
            if (accessToken){
                $.ajax({
                    url: context+"/token",
                    type: "GET",
                    success: function(){
                        execute();
                    }
                }).fail(errorCallback);
            }
        }else {
            execute();
        }
    }
    module.get = function(url, successCallback, errorCallback){
        var payload = null;
        call("GET", url, payload, successCallback, errorCallback);
    };
    module.post = function(url, payload, successCallback, errorCallback){
        call("POST", url, payload, successCallback, errorCallback);
    };
    module.delete = function(url, successCallback, errorCallback){
        var payload = null;
        call("DELETE", url, payload, successCallback, errorCallback);
    }
    return module;
}();