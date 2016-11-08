/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * This Javascript module groups utility methods that are being used by all the gadgets in the loganalyzer dashboard
 */
var PARAM_TYPE = "type";

var serverUrls = [
    { name: "ESB", svrUrl: "/portal/apis/analytics"},
    { name: "APIM", svrUrl: "https://"+location.hostname +":"+ location.port +"/admin/modules/la/log-analyzer-proxy.jag"}

];

function GadgetUtils() {
    var DEFAULT_START_TIME = new Date(moment().subtract(29, 'days')).getTime();
    var DEFAULT_END_TIME = new Date(moment()).getTime();

    this.getQueryString = function() {
        var queryStringKeyValue = window.parent.location.search.replace('?', '').split('&');
        var qsJsonObject = {};
        if (queryStringKeyValue != '') {
            for (i = 0; i < queryStringKeyValue.length; i++) {
                qsJsonObject[queryStringKeyValue[i].split('=')[0]] = queryStringKeyValue[i].split('=')[1];
            }
        }
        return qsJsonObject;
    };

    this.getChart = function(chartType) {
        var gadgetConf = null;
        charts.forEach(function(item, i) {
            if (item.name === chartType) {
                gadgetConf = item;
            }
        });
        return gadgetConf;
    };

      this.getGadgetSvrUrl = function(chartType) {
            var svrUrl = null;
            serverUrls.forEach(function(item, i) {
                if (item.name === chartType) {
                    svrUrl = item;
                }
            });
            return svrUrl.svrUrl;
        };

    this.getTimeUnit = function(fromTime, toTime) {
        var chart = null;
        charts.forEach(function(item, i) {
            if (item.name === chartType) {
                chart = item;
            }
        });
        return chart;
    };

    this.timeFrom = function() {
        var timeFrom = DEFAULT_START_TIME;
        return timeFrom;
    };

    this.timeTo = function() {
        var timeTo = DEFAULT_END_TIME;
        return timeTo;
    };

    this.getDefaultText = function() {
        return '<div class="status-message">'+
                '<div class="message message-info">'+
                    '<h4><i class="icon fw fw-info"></i>No content to display</h4>'+
                    '<p>Please select a date range to view stats.</p>'+
                '</div>'+
            '</div>';
    };

    this.getCustemText = function(title, message) {
        return '<div class="status-message">'+
            '<div class="message message-info">'+
            '<h4><i class="icon fw fw-info"></i>'+title+'</h4>'+
            '<p>'+message+'</p>'+
            '</div>'+
            '</div>';
    };

    this.getEmptyRecordsText = function() {
        return '<div class="status-message">'+
                '<div class="message message-info">'+
                    '<h4><i class="icon fw fw-info"></i>No records found</h4>'+
                    '<p>Please select a date range to view stats.</p>'+
                '</div>'+
            '</div>';
    };

    this.getErrorText = function(msg) {
        return '<div class="status-message">'+
                '<div class="message message-danger">'+
                    '<h4><i class="icon fw fw-info"></i>Error</h4>'+
                    '<p>An error occured while attempting to display this gadget. Error message is: ' + msg.status + ' - ' + msg.message + '</p>'+
                '</div>'+
            '</div>';
    };

    this.getLoadingText = function() {
        return '<div class="status-message">' +
            '<div class="message message-info">' +
            '<h4><i class="icon fw fw-info"></i>Loading...</h4>' +
            '<p>Please wait while data is loading.</p>' +
            '</div>' +
            '</div>';
    };

    this.getDashboardURL=function(){
        return window.location.origin;
    }
}

var gadgetUtils = new GadgetUtils();