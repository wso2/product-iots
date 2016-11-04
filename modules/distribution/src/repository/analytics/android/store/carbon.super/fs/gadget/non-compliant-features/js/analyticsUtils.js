/*
 * Copyright (c)  2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

function addURLParam(key, value) {
    updateURLParam(key, value)
}

function updateURLParam(key, value) {
    if (typeof (history.pushState) === "undefined") {
        console.warn("Browser doesn't support updating the url.");
        return;
    }

    var searchPath = window.parent.location.search,
        replace = new RegExp("(&|\\?)" + key + "=(.*?)(&|$)", "g"),
        urlParams = getURLParams(),
        values = [],
        unfiltered = "?filtered=false";

    if (Object.prototype.toString.call( value ) === '[object Array]') {
        values = value;
    } else if (Object.prototype.toString.call( value ) === "[object String]"){
        values.push(value);
    } else {
        console.error("value should be either an array of strings or a string");
        return;
    }

    if (searchPath.replace(unfiltered, "")) {
        if (key in urlParams) {
            if (values.length > 0) {
                searchPath = searchPath.replace(replace, "$1" + key + "=" + values.toString() + "$3");
            } else {
                if (searchPath.replace(replace, "")) {
                    searchPath = searchPath.replace(replace, "$1").replace(/&$/, '');
                } else {
                    searchPath = unfiltered;
                }
            }
        } else if (values.length > 0) {
            searchPath += "&" + key + "=" + values.toString();
        }
    } else if (values.length > 0) {
        searchPath = searchPath.replace(unfiltered, "");
        searchPath += "?" + key + "=" + values.toString();
    }
    window.parent.history.pushState({}, "", searchPath);
}

function removeURLParam(key) {
    updateURLParam(key, []);
}

function getURLParam(key) {
    var params = getURLParams();
    if (key in params) {
        return params[key];
    } else {
        return null;
    }
}

function getURLParams() {
    var match,
        pl = /\+/g,
        search = /([^&=]+)=?([^&]*)/g,
        decode = function (s) {
            return decodeURIComponent(s.replace(pl, " "));
        },
        query = window.parent.location.search.substring(1),
        urlParams = {};
    while (match = search.exec(query))
        urlParams[decode(match[1])] = decode(match[2]).split(',');
    delete urlParams["filtered"];
    return urlParams;
}


function getBaseURL() {
    var url = window.parent.location.toString();
    url = url.split('?')[0];
    var urlComponents = url.split("/");
    if(urlComponents[urlComponents.length-1] == "landing"){
        url = url.substring(0,url.lastIndexOf("/")+1);
    } else {
        if(url.lastIndexOf("/") != url.length - 1){
            url = url + "/";
        }
    }
    return url;
}
