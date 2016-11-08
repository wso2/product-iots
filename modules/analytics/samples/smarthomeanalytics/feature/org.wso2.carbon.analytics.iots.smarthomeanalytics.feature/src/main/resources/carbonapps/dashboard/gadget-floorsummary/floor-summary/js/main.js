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

var floorSummaryGadget = floorSummaryGadget || {};
floorSummaryGadget.table = null;
floorSummaryGadget.polling_task = null;
floorSummaryGadget.data = [];
floorSummaryGadget.filter_context = null;
floorSummaryGadget.filters_meta = {};
floorSummaryGadget.filters = [];
floorSummaryGadget.filter_prefix = "g_";
floorSummaryGadget.selected_filter_groups = [];
floorSummaryGadget.force_fetch = false;
floorSummaryGadget.freeze = false;
floorSummaryGadget.div = "#table";
floorSummaryGadget.dataSource = gadgetConfig.defaultSource;
floorSummaryGadget.currentStatusOfSensorsSource = gadgetConfig.currentStatusOfSensorsSource;
floorSummaryGadget.tableConfiguata = gadgetConfig.tableConfiguration;
floorSummaryGadget.cellTemplates = {};
floorSummaryGadget.apiContext = gadgetConfig.apiContext;
floorSummaryGadget.sensorStatusLevel = gadgetConfig.sensorStatusLevel;

var analyticsClient = new AnalyticsClient().init(null, null, "/portal/apis/analytics");


var oTable;
var nanoScrollerSelector = $('.nano');

floorSummaryGadget.meta = {
    "names": floorSummaryGadget.tableConfiguata.columns,
    "types": floorSummaryGadget.tableConfiguata.columnTypes
};

floorSummaryGadget.config = {
    key: floorSummaryGadget.tableConfiguata.tableKey,
    title: floorSummaryGadget.tableConfiguata.tableTitle,
    charts: [{
        type: "table",
        columns: floorSummaryGadget.tableConfiguata.columns,
        columnTitles: floorSummaryGadget.tableConfiguata.columnTitles
    }],
    width: $(window).width() * 0.95,
    height: $(window).width() * 0.65 > $(window).height() ? $(window).height() : $(window).width() * 0.65,
    padding: {"top": 18, "left": 30, "bottom": 22, "right": 70}
};

floorSummaryGadget.loadTemplateValues = function (columns) {
    for (i = 0; i < columns.length; i++) {
        floorSummaryGadget.cellTemplates[columns[i]] = '{{' + columns[i] + '}}';
    }
};

floorSummaryGadget.loadSpecificTemplateValues = function () {
    floorSummaryGadget.cellTemplates["locationName"] = '<a target="_blank" href="' + '{{floor-summary}}' + '">' +
        '<span class="fw fw-stack"><i class="fw fw-edit fw-stack-1x"></i><i class="fw fw-circle-outline fw-stack-2x">' +
        '</i></span>{{locationName}}</a>';
};

floorSummaryGadget.loadTemplateValues(floorSummaryGadget.tableConfiguata.columns);
floorSummaryGadget.loadSpecificTemplateValues();

floorSummaryGadget.initialize = function () {
    floorSummaryGadget.table = new vizg(
        [
            {
                "metadata": floorSummaryGadget.meta,
                "data": floorSummaryGadget.data
            }
        ],
        floorSummaryGadget.config
    );
    floorSummaryGadget.table.draw(floorSummaryGadget.div);
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
    floorSummaryGadget.startPolling();
};

floorSummaryGadget.startPolling = function () {
    setTimeout(function () {
        floorSummaryGadget.update();
    }, 500);
    this.polling_task = setInterval(function () {
        floorSummaryGadget.update();
    }, gadgetConfig.polling_interval);
};

floorSummaryGadget.update = function (force) {
    floorSummaryGadget.force_fetch = !floorSummaryGadget.force_fetch ? force || false : true;
    if (!floorSummaryGadget.freeze) {
        //todo: redrawing the data table because there are no clear and insert method
        document.getElementById("table").innerHTML = "";
        floorSummaryGadget.data = [];
        floorSummaryGadget.table = new vizg(
            [
                {
                    "metadata": floorSummaryGadget.meta,
                    "data": floorSummaryGadget.data
                }
            ],
            floorSummaryGadget.config
        );
        floorSummaryGadget.table.draw(floorSummaryGadget.div);
        setTimeout(function () {
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
        floorSummaryGadget.fetch(function (data) {
            floorSummaryGadget.table.insert(data);
        });
    }
};

function getSummary(callBack) {
    var tableName = floorSummaryGadget.dataSource;
    var queryParam = gadgetUtils.getQueryString();
    var query = "_meta_homeId" + queryParam["homeId"];
    query = "";
    var queryInfo = queryBuilder(tableName, query, 0, 1000, null);
    analyticsClient.search(queryInfo, function (d) {
        if (d["status"] === "success") {
            var totalRecords = JSON.parse(d["message"]);
            callBack(totalRecords, queryParam);
        }
    }, function (error) {
        if (error === undefined) {
            console.log("Analytics server not found : Please troubleshoot connection problems.");
        } else {
            error.message = "Internal server error while data indexing.";
            console.log(error);
        }
    });
}

function getCurrentStatus(records,queryParam, callBack) {
    var tableName = floorSummaryGadget.currentStatusOfSensorsSource;
    var processedTask = 0;
    var sensorAlertLevel = 0;
    var locationAlertLevel = "ok";
    records.forEach(function (current_value, index) {
        var query = "meta_homeId:"+ queryParam["homeId"]+ " AND _meta_sensorLocation:\""
            + records[index]["values"]["locationId"] + "\"";
        var queryInfo = queryBuilder(tableName, query, 0, 1000, null);
        analyticsClient.search(queryInfo, function (d) {
            if (d["status"] === "success") {
                var sensorInformation = JSON.parse(d["message"]);
                var messages = "";
                for (var g = 0; g < sensorInformation.length; g++) {
                    messages += sensorInformation[g]["values"]["summary"];
                    if (g != sensorInformation.length - 1) {
                        messages += ", ";
                    }
                    var alertLevel = sensorInformation[g]["values"]["currentStatus"];
                    if (getSensorStatusLevel(alertLevel) > sensorAlertLevel) {
                        sensorAlertLevel = getSensorStatusLevel(alertLevel);
                        locationAlertLevel = alertLevel;
                    }
                }
                if(sensorInformation.length !=0){
                    records[index]["values"]["message"] = messages;
                    records[index]["values"]["alertLevel"] = locationAlertLevel;
                    processedTask++;
                    if (processedTask == records.length) {
                        callBack(records);
                    }
                }
                processedTask++;
                if (processedTask == records.length) {
                    callBack(records);
                }
            }
        }, function (error) {
            if (error === undefined) {
                console.log("Analytics server not found : Please troubleshoot connection problems.");
            } else {
                error.message = "Internal server error while data indexing.";
                console.log(error);
            }
        });
    });

    function getSensorStatusLevel(alertLevel) {
        return floorSummaryGadget.sensorStatusLevel[alertLevel];
    }
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

floorSummaryGadget.fetch = function (cb) {
    floorSummaryGadget.data.length = 0;
    floorSummaryGadget.data = [];
    floorSummaryGadget.force_fetch = false;
    var data;
    getSummary(function (response, queryParam) {
        getCurrentStatus(response, queryParam, function (response) {
            data = response;
            var floorSummary = gadgetUtils.getDashboardURL() + floorSummaryGadget.apiContext
                + "/floor-summary?homeId=";
            if (data && data.length > 0) {
                for (var i = 0; i < data.length; i++) {
                    floorSummaryGadget.data.push(
                        [
                            Mustache.to_html(floorSummaryGadget.cellTemplates['homeId']
                                , {'homeId': data[i]["values"]["homeId"]}),
                            Mustache.to_html(floorSummaryGadget.cellTemplates['locationName']
                                , {
                                    'locationName': data[i]["values"]["locationName"]
                                    , 'floor-summary': floorSummary + data[i]["values"]["homeId"] + "&locationId="
                                    + data[i]["values"]["locationId"]
                                }),
                            Mustache.to_html(floorSummaryGadget.cellTemplates['locationId']
                                , {'locationId': data[i]["values"]["locationId"]}),
                            Mustache.to_html(floorSummaryGadget.cellTemplates['alertLevel']
                                , {'alertLevel': data[i]["values"]["alertLevel"]}),
                            Mustache.to_html(floorSummaryGadget.cellTemplates['message']
                                , {'message': data[i]["values"]["message"]})
                        ]
                    );
                }
                if (floorSummaryGadget.force_fetch) {
                    floorSummaryGadget.update();
                } else {
                    cb(floorSummaryGadget.data);
                }
            }
        });
    });
};

floorSummaryGadget.subscribe = function (callback) {
    gadgets.HubSettings.onConnect = function () {
        gadgets.Hub.subscribe("subscriber", function (topic, data, subscriber) {
            callback(topic, data)
        });
    };
};

floorSummaryGadget.onclick = function (event, item) {

};

$(document).ready(function () {
    floorSummaryGadget.initialize();
});