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

var downloadModule;
var Handlebars = require('../lib/handlebars-v2.0.0.js').Handlebars;
downloadModule = function () {
    var log = new Log("modules/download.js");

    var constants = require("/modules/constants.js");

    var publicMethods = {};
    var privateMethods = {};

    /**
     * Downloading a specified sketch file.
     *
     * @param file File name or file object of the downloading file
     * @param replaceParams
     */
    publicMethods.downloadSketch = function (file, response, replaceParams) {
        var file = new File("../sketch/" + file);

        file.open('r');
        log.debug("Reading file '" + file.getPath() + "'");
        var content = file.readAll().trim();
        file.close();

        var downloadFile = privateMethods.allReplace(content,replaceParams);

        response.contentType = "application/octet-stream";
        response.addHeader("Content-Disposition", "attachment; filename='sketch.hbs'");
        response.addHeader("Content-Length", String(downloadFile.length));
        response.content = downloadFile;
    };

    /**
     * Find and replace all occurrences.
     * @param inStr input string
     * @param replaceParams key value array
     * @returns retStr replaced string
     */
    privateMethods.allReplace = function (inStr, replaceParams) {
        var retStr = inStr;
        for (var x in replaceParams) {
            retStr = retStr.replace(new RegExp(x, 'g'), replaceParams[x])
        }
        return retStr;
    };

    return publicMethods;
}();


