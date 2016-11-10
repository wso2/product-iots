/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * This Javascript module groups utility methods that are being used by all the gadgets in the ESB analytics dashboard
 */

var CONTEXT = "https://localhost:8243/android_sense/1.0.0/devices";
var DASHBOARD_NAME = parent.ues.global.dashboard.id; //"esb-analytics"
var BASE_URL = "/portal/dashboards/" + DASHBOARD_NAME + "/";

var TYPE_DEVICE_LISTING = "device_list";

var ROLE_TPS = "tps";
var ROLE_LATENCY = "latency";
var ROLE_RATE = "rate";

var PARAM_ID = "id";
var PARAM_TYPE = "type";
var PARAM_GADGET_ROLE = "role";

var COLOR_BLUE = "#438CAD";
var COLOR_RED = "#D9534F";
var COLOR_GREEN = "#5CB85C";

var PARENT_WINDOW = window.parent.document;

function GadgetUtil() {
    var DEFAULT_START_TIME = new Date(moment().subtract(29, 'days')).getTime();
    var DEFAULT_END_TIME = new Date(moment()).getTime();

    this.getQueryString = function () {
        var queryStringKeyValue = window.parent.location.search.replace('?', '').split('&');
        var qsJsonObject = {};
        if (queryStringKeyValue != '') {
            for (i = 0; i < queryStringKeyValue.length; i++) {
                qsJsonObject[queryStringKeyValue[i].split('=')[0]] = queryStringKeyValue[i].split('=')[1];
            }
        }
        return qsJsonObject;
    };

    this.getChart = function (chartType) {
        var chart = null;
        charts.forEach(function (item, i) {
            if (item.name === chartType) {
                chart = item;
            }
        });
        return chart;
    };

    this.getCurrentPageName = function () {
        var pageName, type;
        var href = parent.window.location.href;
        var lastSegment = href.substr(href.lastIndexOf('/') + 1);
        if (lastSegment.indexOf('?') == -1) {
            pageName = lastSegment;
        } else {
            pageName = lastSegment.substr(0, lastSegment.indexOf('?'));
        }
        if (!pageName || pageName === DASHBOARD_NAME) {
            pageName = TYPE_LANDING;
        }
        return pageName;
    };

    this.getRequestType = function (pageName, chart) {
        chart.types.forEach(function (item, i) {
            if (item.name === pageName) {
                type = item.type;
            }
        });
        return type;
    };

    this.getGadgetConfig = function (typeName) {
        var config = null;
        configs.forEach(function (item, i) {
            if (item.name === typeName) {
                config = item;
            }
        });
        return config;
    };

    this.getCurrentPage = function () {
        var page, pageName;
        var href = parent.window.location.href;
        var lastSegment = href.substr(href.lastIndexOf('/') + 1);
        if (lastSegment.indexOf('?') == -1) {
            pageName = lastSegment;
        } else {
            pageName = lastSegment.substr(0, lastSegment.indexOf('?'));
        }
        console.log(pageName);
        return this.getGadgetConfig(pageName);
    };

    this.timeFrom = function () {
        var timeFrom = DEFAULT_START_TIME;
        var qs = this.getQueryString();
        if (qs.timeFrom != null) {
            timeFrom = qs.timeFrom;
        }
        return timeFrom;
    };

    this.timeTo = function () {
        var timeTo = DEFAULT_END_TIME;
        var qs = this.getQueryString();
        if (qs.timeTo != null) {
            timeTo = qs.timeTo;
        }
        return timeTo;
    };

    this.fetchData = function (context, params, callback, errorCallback) {
        if (params && params.length !== 0) {
            context += "?";
            for (var param in params) {
                context = context + param + "=" + params[param] + "&";
            }
        }
        console.log(context);
        wso2.gadgets.XMLHttpRequest.get(context,
            function (data) {
                callback(data);
            },
            function (error) {
                errorCallback(error);
            }
        );

    };

    this.getDefaultText = function () {
        return '<div class="status-message">' +
            '<div class="message message-info">' +
            '<h4><i class="icon fw fw-info"></i>No content to display</h4>' +
            '<p>Please select a date range to view stats.</p>' +
            '</div>' +
            '</div>';
    };

    this.getEmptyRecordsText = function () {
        return '<div class="status-message">' +
            '<div class="message message-info">' +
            '<h4><i class="icon fw fw-info"></i>No records found</h4>' +
            '<p>Please select a date range to view stats.</p>' +
            '</div>' +
            '</div>';
    };

    this.getErrorText = function (msg) {
        return '<div class="status-message">' +
            '<div class="message message-danger">' +
            '<h4><i class="icon fw fw-info"></i>Error</h4>' +
            '<p>An error occured while attempting to display this gadget. Error message is: ' + msg.status + ' - ' + msg.statusText + '</p>' +
            '</div>' +
            '</div>';
    };

    this.getCookie = function (cname) {
        var name = cname + "=";
        var ca = parent.document.cookie.split(';');
        for (var i = 0; i < ca.length; i++) {
            var c = ca[i];
            while (c.charAt(0) == ' ') c = c.substring(1);
            if (c.indexOf(name) == 0) return c.substring(name.length, c.length);
        }
        return "";
    };

    this.getGadgetWrapper = function () {
        return $('#' + gadgets.rpc.RPC_ID, PARENT_WINDOW).closest('.gadget-body');
    };

    this.getGadgetParentWrapper = function () {
        return $('#' + gadgets.rpc.RPC_ID, PARENT_WINDOW).closest('.ues-component-box');
    };

    this.getView = function () {
        if ($('#' + gadgets.rpc.RPC_ID, PARENT_WINDOW).closest('.ues-component-box').hasClass('ues-component-fullview')) {
            return 'maximized';
        }
        else {
            return 'minimized';
        }
    };

}

var gadgetUtil = new GadgetUtil();

// Light/Dark Theme Switcher
$(document).ready(function () {

    $(gadgetUtil.getGadgetWrapper()).addClass('loading');

    if ((gadgetUtil.getCookie('dashboardTheme') == 'dark') || gadgetUtil.getCookie('dashboardTheme') == '') {
        $('body').addClass('dark');
    }
    else {
        $('body').removeClass('dark');
    }

    if (typeof $.fn.nanoScroller == 'function') {
        $(".nano").nanoScroller();
    }

});

var readyInterval = setInterval(function () {
    if (document.readyState == "complete") {
        $(gadgetUtil.getGadgetWrapper()).removeClass('loading');
        clearInterval(readyInterval);
    }
}, 100);