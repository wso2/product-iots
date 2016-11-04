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

var dg = dg || {};
dg.chart = null;
dg.polling_task = null;
dg.data = [];
dg.sum = 0;
dg.filter_contexts = [];
dg.filters_meta = {};
dg.filters = [];
dg.filter_prefix = "g_";
dg.selected_filter_groups = {};
dg.force_fetch = false;
dg.freeze = false;
dg.div = "#chart";
dg.filterContextFromURL = false; //to enable inter-gadget communication
dg.selected_filter_context_from_url = "";
dg.selected_filter_from_url = [];
dg.gadgetUrl = gadgetConfig.defaultSource;
dg.filterUrl="";
dg.API_CHANGING_PARAMETER = "non-compliant-feature-code";

dg.meta = {
    "names": ["context", "group", "name", "percentage"],
    "types": ["ordinal", "ordinal", "ordinal", "linear"]
};

var body = "body";

dg.config = {
    type: "bar",
    x: "context",
    legend: false,
    legendTitleColor: "white",
    charts: [
        {
            type: "stack",
            y: "percentage",
            color: "name",
            xTitle: "",
            yTitle: "",
            mode: "stack"
        }
    ],
    width: $(body).width(),
    height: $(body).height(),
    padding: {"top": 25, "left": 25, "bottom": 25, "right": 25}
};

dg.initialize = function () {
    //noinspection JSPotentiallyInvalidConstructorUsage
    dg.chart = new vizg(
        [
            {
                "metadata": dg.meta,
                "data": dg.data
            }
        ],
        dg.config
    );
    dg.chart.draw("#chart", [
        {
            type: "click",
            callback: dg.onclick
        }
    ]);
    dg.loadFiltersFromURL();
    dg.startPolling();
};

dg.loadFiltersFromURL = function () {
    var urlParams = getURLParams();
    if(isFilteredDashboard()){
        dg.filterContextFromURL = true;
        dg.filterUrl = getFilteringUrl();
        for (var filter in urlParams) {
            if (urlParams.hasOwnProperty(filter) && filter.lastIndexOf(dg.filter_prefix, 0) === 0) {
                var filter_context = filter.substring(dg.filter_prefix.length);
                if(filter_context == dg.API_CHANGING_PARAMETER){
                    dg.gadgetUrl = gadgetConfig.featureSource;
                }
            }
        }
    }
    var endpointUrl = dg.gadgetUrl;
    if(dg.filterUrl != ""){
        endpointUrl = endpointUrl + "?" + dg.filterUrl;
    }

    //noinspection JSUnresolvedVariable
    wso2.gadgets.XMLHttpRequest.get(
        endpointUrl,
        function (response) {
            // console.log(JSON.stringify(response));
            for (var i = 0; i < response.length; i++) {
                //noinspection JSUnresolvedVariable
                dg.filter_contexts.push(response[i].groupingAttribute);
                //noinspection JSUnresolvedVariable
                dg.selected_filter_groups[response[i].groupingAttribute] = [];
            }
            for (var filter in urlParams) {
                if (urlParams.hasOwnProperty(filter) && filter.lastIndexOf(dg.filter_prefix, 0) === 0) {
                    var filter_context = filter.substring(dg.filter_prefix.length);
                    dg.filterContextFromURL = true;
                    if (dg.filter_contexts.indexOf(filter_context) !== -1) {
                        //adding another gadgets filter to its own
                        dg.selected_filter_groups[filter_context] = urlParams[filter];
                        dg.filters.push({
                            filteringContext: filter_context,
                            filteringGroups: urlParams[filter]
                        });
                    } else {
                        //sending own filter to the subscribers
                        dg.updateFilters({
                            filteringContext: filter_context,
                            filteringGroups: urlParams[filter]
                        });
                    }
                }
            }
        }, function () {
            console.warn("Error accessing source for : " + gadgetConfig.id);
        }
    );
};

dg.startPolling = function () {
    setTimeout(function () {
        dg.update();
        dg.freeze = dg.isFreeze();
    }, 500);
    //noinspection JSUnusedGlobalSymbols
    this.polling_task = setInterval(function () {
        dg.update();
    }, gadgetConfig.polling_interval);
};

dg.isFreeze = function () {
    for (var i in dg.selected_filter_groups) {
        if (dg.selected_filter_groups.hasOwnProperty(i)) {
            if (dg.selected_filter_groups[i].length > 0) {
                return true;
            }
        }
    }
    return false;
};

dg.update = function (force) {
    dg.force_fetch = !dg.force_fetch ? force || false : true;
    if (!dg.freeze) {
        dg.fetch(function (data) {
            dg.chart.insert(data);
        });
    }
};

dg.fetch = function (cb) {
    dg.data.length = 0;
    dg.force_fetch = false;
    var endpointUrl = dg.gadgetUrl;
    if (dg.filterUrl != "") {
        endpointUrl = endpointUrl + "?" + dg.filterUrl;
    }
    //noinspection JSUnresolvedVariable
    wso2.gadgets.XMLHttpRequest.get(endpointUrl,
        function (response) {
            if (Object.prototype.toString.call(response) === '[object Array]') {
                var devicesAvailable = false;
                for (var i = 0; i < response.length; i++) {
                    dg.sum = 0;
                    var context = response[i]["groupingAttribute"];
                    var data = response[i]["data"];
                    if (context && data) {
                        if (dg.filter_contexts.indexOf(context) === -1) {
                            dg.filter_contexts.push(context);
                            dg.selected_filter_groups[context] = [];
                        }
                        if (data.length > 0) {
                            devicesAvailable = true;
                            for (var j = 0; j < data.length; j++){
                                dg.sum += data[j]["deviceCount"];
                            }
                            for (j = 0; j < data.length; j++) {
                                dg.data.push(
                                    [
                                        context,
                                        data[j]["group"],
                                            data[j]["displayNameForGroup"] + " Count: " +
                                            data[j]["deviceCount"],
                                            (data[j]["deviceCount"] / dg.sum) * 100
                                    ]
                                );
                            }
                        }
                    }
                }
                var headerMsgForZeroDevices = "#header-msg-for-zero-devices";
                var bodyMsgForZeroDevices = "#body-msg-for-zero-devices";
                var deviceGroupingChartView = "#chart";
                if (devicesAvailable) {
                    $(headerMsgForZeroDevices).addClass("hidden");
                    $(bodyMsgForZeroDevices).addClass("hidden");
                    $(deviceGroupingChartView).removeClass("hidden");
                } else {
                    $(headerMsgForZeroDevices).removeClass("hidden");
                    $(bodyMsgForZeroDevices).removeClass("hidden");
                    $(deviceGroupingChartView).addClass("hidden");
                }
                if (dg.force_fetch) {
                    dg.update();
                } else {
                    cb(dg.data);
                }
            } else {
                console.error("Invalid response structure found: " + JSON.stringify(response));
            }
        }, function () {
            console.warn("Error accessing source for : " + gadgetConfig.id);
        }
    );
};

dg.updateURL = function () {
    for (var i in dg.selected_filter_groups) {
        if (dg.selected_filter_groups.hasOwnProperty(i)) {
            if (dg.selected_filter_groups[i].length > 0) {
                updateURLParam(dg.filter_prefix + i, dg.selected_filter_groups[i]);
            }
        }
    }

};

dg.subscribe = function (callback) {
    //noinspection JSUnresolvedVariable
    gadgets.HubSettings.onConnect = function () {
        //noinspection JSUnresolvedVariable, JSUnusedLocalSymbols
        gadgets.Hub.subscribe("subscriber", function (topic, data, subscriber) {
            callback(topic, data)
        });
    };
};

dg.publish = function (data) {
    //noinspection JSUnresolvedVariable
    gadgets.Hub.publish("publisher", data);
};

dg.onclick = function (event, item) {
    var filteringContext = item.datum.context;
    var filteringGroup = item.datum.group;
    if(!dg.filterContextFromURL){
        var url = getBaseURL() + "devices?g_"+filteringContext+"="+filteringGroup;
        window.open(url);
    } else {
        if (item != null) {
            var index = dg.selected_filter_groups[filteringContext].indexOf(filteringGroup);
            if (index !== -1) {
                dg.selected_filter_groups[filteringContext].splice(index, 1);
            } else {
                dg.selected_filter_groups[filteringContext].push(filteringGroup);
            }
            dg.publish({
                "filteringContext": filteringContext,
                "filteringGroups": dg.selected_filter_groups[filteringContext]
            });
            dg.freeze = dg.isFreeze();
            dg.updateURL();
            dg.update(true);
        }
    }
};

dg.updateFilters = function (data) {
    var reload = false;
    var update = false;
    if (data) {
        if (data.filteringContext
            && data.filteringGroups
            && Object.prototype.toString.call(data.filteringGroups) === '[object Array]'
            && dg.filter_contexts.indexOf(data.filteringContext) !== -1) {
            dg.selected_filter_groups[data.filteringContext] = data.filteringGroups.slice();
            dg.freeze = dg.isFreeze();
            reload = true;
        } else if (!data.filteringGroups
            || Object.prototype.toString.call(data.filteringGroups) !== '[object Array]'
            || data.filteringGroups.length === 0) {
            if (dg.filters_meta.hasOwnProperty(data.filteringContext)) {
                delete dg.filters_meta[data.filteringContext];
                reload = true;
                update = true;
            }
        } else if (data.filteringContext
            && data.filteringGroups
            && Object.prototype.toString.call(data.filteringGroups) === '[object Array]'
            && data.filteringGroups.length > 0) {
            dg.filters_meta[data.filteringContext] = data;
            reload = true;
            update = true;
        }
    }
    if (update) {
        dg.filters.length = 0;
        for (var i in dg.filters_meta) {
            if (dg.filters_meta.hasOwnProperty(i)) {
                dg.filters.push(dg.filters_meta[i]);
            }
        }
    }
    if (reload) dg.update(true);
};

dg.subscribe(function (topic, data) {
    dg.updateFilters(data);
    dg.filterUrl = getFilteringUrl();
});

$(document).ready(function () {
    dg.initialize();
});
