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

var dt = dt || {};
dt.table = null;
dt.polling_task = null;
dt.data = [];
dt.filter_context = null;
dt.filters_meta = {};
dt.filters = [];
dt.filter_prefix = "g_";
dt.selected_filter_groups = [];
dt.force_fetch = false;
dt.freeze = false;
dt.div = "#table";
dt.gadgetUrl = gadgetConfig.defaultSource;
dt.filterUrl = "";
dt.API_CHANGING_PARAMETER = "non-compliant-feature-code";
dt.deviceManageUrl = "";
var analyticsClient = new AnalyticsClient().init(null,null,"/portal/apis/analytics");


var oTable;
var nanoScrollerSelector = $('.nano');

dt.meta = {
    "names": ["homeId", "name", "floorNumber", "message", "summary", "group"],
    "types": ["ordinal", "ordinal", "ordinal", "ordinal", "ordinal", "ordinal"]
};

dt.config = {
    key: "homeId",
    title:"deviceTable",
    charts: [{
        type: "table",
        columns: ["homeId", "name", "floorNumber", "message", "summary", "group"],
        columnTitles: ["Home ID", "Name", "Floor Number", "Message", "Summary", "Group"]
    }],
    width: $(window).width()* 0.95,
    height: $(window).width() * 0.65 > $(window).height() ? $(window).height() : $(window).width() * 0.65,
    padding: { "top": 18, "left": 30, "bottom": 22, "right": 70 }
};

dt.cell_templates = {
    'homeId' : '{{homeId}}',
    'name' : '{{name}}',
    'floorNumber' : '{{floorNumber}}',
    'message' : '{{message}}',
    'summary' : '{{summary}}',
    'group' : '{{group}}'
};

dt.initialize = function () {
    dt.table = new vizg(
        [
            {
                "metadata": dt.meta,
                "data": dt.data
            }
        ],
        dt.config
    );
    dt.table.draw(dt.div);
    setTimeout(function () {
        oTable = $("#deviceTable").DataTable({
            dom: '<"dataTablesTop"' +
            'f' +
            '<"dataTables_toolbar">' +
            '>' +
            'rt' +
            '<"dataTablesBottom"' +
            'lip' +
            '>'
        });
        nanoScrollerSelector[0].nanoscroller.reset();
    }, 1000);
    dt.startPolling();
};



dt.startPolling = function () {
    setTimeout(function () {
        dt.update();
    }, 500);
    this.polling_task = setInterval(function () {
        dt.update();
    }, gadgetConfig.polling_interval);
};

dt.update = function (force) {
    dt.force_fetch = !dt.force_fetch ? force || false : true;
    if (!dt.freeze) {
        //todo: redrawing the data table because there are no clear and insert method
        document.getElementById("table").innerHTML = "";
        dt.data = [];
        dt.table = new vizg(
            [
                {
                    "metadata": dt.meta,
                    "data": dt.data
                }
            ],
            dt.config
        );
        dt.table.draw(dt.div);
        setTimeout(function(){
            oTable.destroy();
            oTable = $("#deviceTable").DataTable({
                dom: '<"dataTablesTop"' +
                'f' +
                '<"dataTables_toolbar">' +
                '>' +
                'rt' +
                '<"dataTablesBottom"' +
                'lip' +
                '>'
            });
            nanoScrollerSelector[0].nanoscroller.reset();
        }, 1000);
        dt.fetch(function (data) {
            dt.table.insert(data);
        });
    }
};

function getSummary(callBack){
    var tableName = "ORG_WSO2_IOT_HOMEAUTOMATION_SUMMARY";
    var query = "";
    var queryInfo = queryBuilder(tableName, query, 0, 1000, null);
    analyticsClient.search(queryInfo, function (d) {
        if (d["status"] === "success") {
            var totalRecords = JSON.parse(d["message"]);
            callBack(totalRecords);
        }
    }, function (error) {
        if(error === undefined){
            onErrorCustom("Analytics server not found.", "Please troubleshoot connection problems.");
            console.log("Analytics server not found : Please troubleshoot connection problems.");
        }else{
            error.message = "Internal server error while data indexing.";
            onError(error);
            console.log(error);
        }
    });
}

function queryBuilder(tableName, query, start, count, sortBy) {
    return {
        tableName: tableName,
        searchParams: {
            query: query,
            start: start, //starting index of the matching record set
            count: count, //page size for pagination
            sortBy: sortBy
        }
    };
}

dt.fetch = function (cb) {
    dt.data.length = 0;
    dt.data = [];
    dt.force_fetch = false;
    console.log("Home automation summary...");
    getSummary(function(response){
            dt.filter_context = "Filtered-home-with-details";
            var data = response;
            if (data && data.length > 0) {
                    for (var i = 0; i < data.length; i++) {
                    dt.data.push(
                        [
                            Mustache.to_html(dt.cell_templates['homeId'], {'homeId': data[i]["values"]["homeId"]}),
                            Mustache.to_html(dt.cell_templates['name'], {'name': data[i]["values"]["name"]}),
                            Mustache.to_html(dt.cell_templates['floorNumber'], {'floorNumber': data[i]["values"]["floorNumber"]}),
                            Mustache.to_html(dt.cell_templates['message'], {'message': data[i]["values"]["message"]}),
                            Mustache.to_html(dt.cell_templates['summary'], {'summary': data[i]["values"]["summary"]}),
                            Mustache.to_html(dt.cell_templates['group'], {'group': data[i]["values"]["group"]})
                        ]
                    );
                }
                if (dt.force_fetch) {
                    dt.update();
                } else {
                    cb(dt.data);
                }
            }
    });
};

dt.subscribe = function (callback) {
    console.log("subscribed to filter-groups2: ");
    gadgets.HubSettings.onConnect = function () {
        gadgets.Hub.subscribe("subscriber", function (topic, data, subscriber) {
            callback(topic, data)
        });
    };
};

dt.onclick = function (event, item) {

};

dt.updateFilters = function (data) {
    var updated = false;
    dt.filterUrl = getFilteringUrl();
    if (typeof data != "undefined" && data != null) {
        if (typeof data.filteringGroups === "undefined"
            || data.filteringGroups === null
            || Object.prototype.toString.call(data.filteringGroups) !== '[object Array]'
            || data.filteringGroups.length === 0) {
            if (dt.filters_meta.hasOwnProperty(data.filteringContext)) {
                delete dt.filters_meta[data.filteringContext];
                updated = true;
            }
        } else {
            if (typeof data.filteringContext != "undefined"
                && data.filteringContext != null
                && typeof data.filteringGroups != "undefined"
                && data.filteringGroups != null
                && Object.prototype.toString.call(data.filteringGroups) === '[object Array]'
                && data.filteringGroups.length > 0) {
                dt.filters_meta[data.filteringContext] = data;
                updated = true;
            }
        }
    }
    if (updated) {
        dt.filters.length = 0;
        for (var i in dt.filters_meta) {
            if (dt.filters_meta.hasOwnProperty(i)) {
                dt.filters.push(dt.filters_meta[i]);
            }
        }
        dt.update(true);
    }
};

dt.subscribe(function (topic, data) {
    dt.updateFilters(data);
});

$(document).ready(function () {
    dt.initialize();
});