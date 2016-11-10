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

var vb = vb || {};
vb.chart = null;
vb.polling_task = null;
vb.data = [];
vb.filter_context = null;
vb.filters_meta = {};
vb.filters = [];
vb.filter_prefix = "g_";
vb.selected_filter_groups = [];
vb.force_fetch = false;
vb.freeze = false;
vb.div = "#chart";

vb.initialize = function () {
    vb.startPolling();
};

vb.startPolling = function () {
    setTimeout(function () {
        vb.fetch();
        vb.freeze = vb.selected_filter_groups.length > 0;
    }, 500);
    //noinspection JSUnusedGlobalSymbols
    this.polling_task = setInterval(function () {
        vb.fetch();
    }, gadgetConfig.polling_interval);
};


vb.fetch = function () {
    vb.data.length = 0;
    vb.force_fetch = false;

    //noinspection JSUnresolvedVariable
    wso2.gadgets.XMLHttpRequest.get(gadgetConfig.source,
        function (response) {
            // console.log(JSON.stringify(response));
            if (Object.prototype.toString.call(response) === '[object Array]' && response.length === 1) {
                //noinspection JSUnresolvedVariable
                vb.filter_context = response[0].groupingAttribute;
                var data = response[0]["data"];
                if (data && data.length > 0) {
                    //noinspection JSUnresolvedVariable
                    var nonCompliantDeviceCount = data[0].deviceCount;
                    var nonCompliantCountElm = "h1#non-compliant-count";
                    var nonCompliantDivElm = "div#NON_COMPLIANT";
                    if (nonCompliantDeviceCount > 0) {
                        if (nonCompliantDeviceCount > 99) {
                            $(nonCompliantCountElm).html("99<sup>+</sup>");
                        } else {
                            $(nonCompliantCountElm).html(nonCompliantDeviceCount.toString());
                        }
                        $(nonCompliantCountElm).removeClass("text-success");
                        $(nonCompliantCountElm).addClass("text-danger");
                        $(nonCompliantDivElm).css("cursor", "pointer");
                        $(nonCompliantCountElm).attr("onclick", "vb.onclick('" + data[0].group + "')");
                    } else {
                        $(nonCompliantCountElm).removeClass("text-danger");
                        $(nonCompliantCountElm).addClass("text-success");
                        $(nonCompliantDivElm).css("cursor", "default");
                        $(nonCompliantCountElm).removeAttr("onclick");
                    }
                    //noinspection JSUnresolvedVariable
                    var unmonitoredDeviceCount = data[1].deviceCount;
                    var unmonitoredCountElm = "h1#unmonitored-count";
                    var unmonitoredDivElm = "div#UNMONITORED";
                    if (unmonitoredDeviceCount > 0) {
                        if (unmonitoredDeviceCount > 99) {
                            $(unmonitoredCountElm).html("99<sup>+</sup>");
                        } else {
                            $(unmonitoredCountElm).html(unmonitoredDeviceCount.toString());
                        }
                        $(unmonitoredCountElm).removeClass("text-success");
                        $(unmonitoredCountElm).addClass("text-warning");
                        $(unmonitoredDivElm).css("cursor", "pointer");
                        $(unmonitoredCountElm).attr("onclick", "vb.onclick('" + data[1].group + "')");
                    } else {
                        $(unmonitoredCountElm).removeClass("text-warning");
                        $(unmonitoredCountElm).addClass("text-success");
                        $(unmonitoredDivElm).css("cursor", "default");
                        $(unmonitoredCountElm).removeAttr("onclick");
                    }
                }
            } else {
                console.error("Invalid response structure found: " + JSON.stringify(response));
            }
        }, function () {
            console.warn("Error accessing source for : " + gadgetConfig.id);
        });
};

vb.onclick = function (filterGroup) {
    var url = getBaseURL() + "devices?g_" + vb.filter_context + "=" + filterGroup;
    window.open(url);
};

$(document).ready(function () {
    vb.initialize();
});
