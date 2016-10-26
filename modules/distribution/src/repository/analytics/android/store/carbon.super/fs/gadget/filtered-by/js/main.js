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

var fb = fb || {};
fb.polling_task = null;
fb.filter_prefix = "g_";
fb.filters_meta = {};
fb.filters = [];
fb.breadcrumbs = {};
fb.force_fetch = false;
fb.data = {
    'filteredCount': 0,
    'totalCount': 0
};
fb.filterUrl = "";
fb.gadgetUrl = gadgetConfig.defaultSource;
fb.API_CHANGING_PARAMETER = "non-compliant-feature-code";
fb.devices_template = '' +
'<span class="deviceCount">{{filtered}}</span> ' +
'out of <span class="totalDevices">{{total}}</span>';

fb.breadcrumb_template = '' +
'<span id="{{id}}" class="label label-primary">' +
'<span>{{label}}</span>' +
'</span>';

fb.initialize = function () {
    $("div#breadcrumbs").on('click', 'i.remove', function () {
        var filter = $(this).closest('.breadcrumb').attr('id').split('_');
        if (filter.length === 2) {
            fb.removeBreadcrumb(filter[0], filter[1]);
        }
    });
    fb.loadFiltersFromURL();
    fb.startPolling();
};

fb.loadFiltersFromURL = function () {
    var urlParams = getURLParams();
    for (var filter in urlParams) {
        if (urlParams.hasOwnProperty(filter) && filter.lastIndexOf(fb.filter_prefix, 0) === 0) {
            var filter_context = filter.substring(fb.filter_prefix.length);
            if(filter_context == fb.API_CHANGING_PARAMETER){
                fb.gadgetUrl = gadgetConfig.featureSource;
            }
            fb.updateBreadcrumbs({
                filteringContext: filter_context,
                filteringGroups: urlParams[filter]
            });
        }
    }
};

fb.startPolling = function () {
    fb.updateDeviceCount();
    this.polling_task = setInterval(function () {
        fb.updateDeviceCount();
    }, gadgetConfig.polling_interval);
};

fb.updateDeviceCount = function (force) {
    fb.force_fetch = !fb.force_fetch ? force || false : true;
    if(fb.filterUrl != null){
        fb.fetch(function (data) {
            var html = Mustache.to_html(fb.devices_template, data);
            $('#devices').html(html);
        });
    }
};

fb.fetch = function (cb) {
    fb.force_fetch = false;
    var endpointUrl = fb.gadgetUrl;
    fb.filterUrl = getFilteringUrl();
    if(fb.filterUrl != ""){
        endpointUrl = endpointUrl + "?" + getFilteringUrl();
    }
    wso2.gadgets.XMLHttpRequest.get(endpointUrl,
        function(response){
            if (Object.prototype.toString.call(response) === '[object Array]' && response.length === 1) {
                var results = response[0].data;
                for (var data = {}, i = 0; i < results.length; i++) {
                    if(results[i]["group"] != "total"){
                        data["filtered"] = results[i]["deviceCount"];
                    } else {
                        data["total"] = results[i]["deviceCount"];
                    }
                }
                if (data) {
                    fb.data.filtered = data["filtered"] ? data["filtered"] : fb.data.filtered;
                    fb.data.total = data["total"] ? data["total"] : fb.data.total;
                    if (fb.force_fetch) {
                        fb.updateDeviceCount();
                    } else {
                        cb(fb.data);
                    }
                }
            } else {
                console.error("Invalid response structure found: " + JSON.stringify(response));
            }
        }, function(){
            console.warn("Error accessing source for : " + gadgetConfig.id);
        });
};

fb.updateURL = function (filterKey, selectedFilters) {
    updateURLParam(fb.filter_prefix + filterKey, selectedFilters);
};

fb.subscribe = function (callback) {
    gadgets.HubSettings.onConnect = function () {
        gadgets.Hub.subscribe("subscriber", function (topic, data, subscriber) {
            callback(topic, data)
        });
    };
};

fb.publish = function (data) {
    console.log(data);
    gadgets.Hub.publish("publisher", data);
};

fb.addBreadcrumb = function (filterKey, selectedFilters) {
    for (var i = 0; i < selectedFilters.length; i++) {
        var breadcrumbKey = filterKey + '_' + selectedFilters[i];
        var selectedFilters2 = selectedFilters[i].toString().toLowerCase();
        var breadcrumbLable = filterKey.split(/(?=[A-Z])/).join(' ') + ':' + selectedFilters2.split(/(?=[A-Z])/).join(' ');
        breadcrumbLable = breadcrumbLable.replace(/\w\S*/g, function (txt) {
            return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
        });
        if (Object.prototype.toString.call(fb.breadcrumbs[filterKey]) !== '[object Array]') fb.breadcrumbs[filterKey] = [];
        var index = fb.breadcrumbs[filterKey].indexOf(selectedFilters[i]);
        if (index === -1) {
            fb.breadcrumbs[filterKey].push(selectedFilters[i]);
            var html = Mustache.to_html(fb.breadcrumb_template, {'id': breadcrumbKey, 'label': breadcrumbLable});
            $('#breadcrumbs').append(html);
        }
    }
};

fb.removeBreadcrumb = function (filteringContext, filteringGroups) {
    var breadcrumbId = filteringContext + '_' + filteringGroups;
    var currentFilters = fb.filters_meta[filteringContext]['filteringGroups'];
    if (currentFilters) {
        var fIndex = currentFilters.indexOf(filteringGroups);
        if (fIndex !== -1) {
            currentFilters.splice(fIndex, 1);
            if (Object.prototype.toString.call(fb.breadcrumbs[filteringContext]) === '[object Array]') {
                var bIndex = fb.breadcrumbs[filteringContext].indexOf(filteringGroups);
                if (bIndex !== -1) {
                    fb.breadcrumbs[filteringContext].splice(bIndex, 1);
                    fb.publish({
                        "filteringContext": filteringContext,
                        "filteringGroups": currentFilters
                    });
                    $("span#" + breadcrumbId).remove();
                    fb.updateURL(filteringContext, currentFilters);
                    fb.updateBreadcrumbs(null, true);
                }
            }
        }
    }
};

fb.updateBreadcrumbs = function (data, force_update) {
    var updated = false;
    if (data) {
        if (!data.filteringGroups
            || Object.prototype.toString.call(data.filteringGroups) !== '[object Array]'
            || data.filteringGroups.length === 0) {
            if (fb.filters_meta.hasOwnProperty(data.filteringContext)) {
                var cs = fb.filters_meta[data.filteringContext]['filteringGroups'];
                for (var i = 0; i < cs.length; i++) {
                    fb.removeBreadcrumb(data.filteringContext, cs[i]);
                }
                delete fb.filters_meta[data.filteringContext];
                updated = true;
            }
        } else if (data.filteringContext
            && data.filteringGroups
            && Object.prototype.toString.call(data.filteringGroups) === '[object Array]'
            && data.filteringGroups.length > 0) {
            if (!fb.filters_meta[data.filteringContext] ||
                fb.filters_meta[data.filteringContext]['filteringGroups'].length < data['filteringGroups'].length) {
                fb.filters_meta[data.filteringContext] = data;
                fb.addBreadcrumb(data.filteringContext, data.filteringGroups);
            }
            if (Object.prototype.toString.call(fb.breadcrumbs[data.filteringContext]) === '[object Array]') {
                for (var j = 0; j < fb.breadcrumbs[data.filteringContext].length; j++) {
                    var index = data.filteringGroups.indexOf(fb.breadcrumbs[data.filteringContext][j]);
                    if (index === -1) {
                        fb.removeBreadcrumb(data.filteringContext, fb.breadcrumbs[data.filteringContext][j]);
                    }
                }
            }
            updated = true;
        }
    }
    if (updated || force_update) {
        fb.filters.length = 0;
        for (var k in fb.filters_meta) {
            if (fb.filters_meta.hasOwnProperty(k)) {
                fb.filters.push(fb.filters_meta[k]);
            }
        }
        if ($('#breadcrumbs').html()) {
            $('#filterMsg').html("Devices are filtered by : ");
        } else {
            $('#filterMsg').html("Click on charts to filter devices.");
        }
        fb.updateDeviceCount(true);
    }
};

fb.subscribe(function (topic, data) {
    fb.updateBreadcrumbs({
        filteringContext: data.filteringContext,
        filteringGroups: data.filteringGroups.slice()
    });
});

$(document).ready(function () {
    fb.initialize();
});