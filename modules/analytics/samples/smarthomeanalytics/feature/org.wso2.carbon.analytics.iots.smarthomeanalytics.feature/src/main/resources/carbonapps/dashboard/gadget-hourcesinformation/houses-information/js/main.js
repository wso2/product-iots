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

var houserInformationGadget = houserInformationGadget || {};
houserInformationGadget.table = null;
houserInformationGadget.polling_task = null;
houserInformationGadget.data = [];
houserInformationGadget.filter_context = null;
houserInformationGadget.filters_meta = {};
houserInformationGadget.filters = [];
houserInformationGadget.selected_filter_groups = [];
houserInformationGadget.force_fetch = false;
houserInformationGadget.freeze = false;
houserInformationGadget.div = "#table";
houserInformationGadget.dataSource = gadgetConfig.defaultSource;
houserInformationGadget.tableConfiguata = gadgetConfig.tableConfiguration;
houserInformationGadget.cellTemplates = {};
houserInformationGadget.apiContext = gadgetConfig.apiContext;
houserInformationGadget.houseStatusLevel = gadgetConfig.houseStatusLevel;
houserInformationGadget.refreshIntervalId = "";
houserInformationGadget.currentStatusOfSensorsSource = gadgetConfig.currentStatusOfSensorsSource;
var analyticsClient = new AnalyticsClient().init(null, null, "/portal/apis/analytics");

houserInformationGadget.meta = {
    "names": houserInformationGadget.tableConfiguata.columns,
    "types": houserInformationGadget.tableConfiguata.columnTypes
};

houserInformationGadget.loadTemplateValues = function (columns) {
    for (var i = 0; i < columns.length; i++) {
        houserInformationGadget.cellTemplates[columns[i]] = '{{' + columns[i] + '}}';
    }
};

houserInformationGadget.loadTemplateValues(houserInformationGadget.tableConfiguata.columns);

houserInformationGadget.startPolling = function () {
    if(houserInformationGadget.refreshIntervalId){
        clearInterval(houserInformationGadget.refreshIntervalId);
    }
    houserInformationGadget.refreshIntervalId = setInterval(function () {
        houserInformationGadget.update();
    }, gadgetConfig.pollingInterval);
};

houserInformationGadget.update = function () {
    houserInformationGadget.fetch();
};

houserInformationGadget.getSummary = function (callBack) {
    var tableName = houserInformationGadget.dataSource;
    var query = "";
    var queryInfo = houserInformationGadget.queryBuilder(tableName, query, 0, 1000, null);
    analyticsClient.search(queryInfo, function (d) {
        if (d["status"] === "success") {
            var totalRecords = JSON.parse(d["message"]);
            callBack(totalRecords);
        }
    }, function (error) {
        if (error === undefined) {
            console.log("Analytics server not found : Please troubleshoot connection problems.");
        } else {
            error.message = "Internal server error while data indexing.";
            console.log(error);
        }
    });
};

houserInformationGadget.getCurrentStatus = function (records, queryParam, callBack) {
    var processedTask = 0;
    records.forEach(function (current_value, index) {
        var tableName = houserInformationGadget.currentStatusOfSensorsSource;
        var query = "_meta_homeId:\"" + current_value["values"]["homeId"]+ "\"";
        var queryInfo = houserInformationGadget.queryBuilder(tableName, query, 0, 1000, null);
        analyticsClient.search(queryInfo, function (d) {
            if (d["status"] === "success") {
                var numberOfSensorOkState = 0;
                var numberOfSensorWarningState = 0;
                var numberOfSensorDangerState = 0;
                var sensorInformation = JSON.parse(d["message"]);
                for (var g = 0; g < sensorInformation.length; g++) {
                    if (sensorInformation[g]["values"]["currentStatus"] == "ok") {
                        numberOfSensorOkState++;
                    } else if (sensorInformation[g]["values"]["currentStatus"] == "warning") {
                        numberOfSensorWarningState++;
                    } else if (sensorInformation[g]["values"]["currentStatus"] == "danger") {
                        numberOfSensorDangerState++;
                    }
                }
                records[index]["values"]["message"] = "Current status of sensors: Ok-: " + numberOfSensorOkState
                    + ", Warning-: " + numberOfSensorWarningState + ", Danger-: " + numberOfSensorDangerState;
                if (numberOfSensorDangerState > 0) {
                    records[index]["values"]["alertLevel"] = "danger";
                } else if (numberOfSensorWarningState > 0) {
                    records[index]["values"]["alertLevel"] = "warning";
                } else if (numberOfSensorOkState > 0) {
                    records[index]["values"]["alertLevel"] = "ok";
                } else {
                    records[index]["values"]["alertLevel"] = "undefined";
                }
                processedTask++;
                if (processedTask == records.length) {
                    callBack(records, queryParam);
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
};

houserInformationGadget.queryBuilder = function(tableName, query, start, count, sortBy) {
    return {
        tableName: tableName,
        searchParams: {
            query: query,
            start: start,
            count: count,
            sortBy: sortBy
        }
    };
};

houserInformationGadget.fetch = function () {
    houserInformationGadget.getSummary(function (response, queryParam) {
        houserInformationGadget.getCurrentStatus(response, queryParam, function (response) {
            var data = response;
            if (data && data.length > 0) {
                $("#houseDetails").empty().append("<br/>");
                for (var i = 0; i < data.length; i++) {
                    var homeSummaryPage = gadgetUtils.getDashboardURL() + houserInformationGadget.apiContext + "/home-summary?homeId=" + data[i]["values"]["homeId"];
                    console.log( houserInformationGadget.getHouseStatusLevel(data[i]["values"]["alertLevel"]));
                    console.log( data[i]["values"]["name"]);
                    var newHouseDetails =
                        '<div class="col-xs-3"><a target="_blank" href="' + homeSummaryPage + '">' +
                        '<div class="tile ' + houserInformationGadget.getHouseStatusLevel(data[i]["values"]["alertLevel"]) + '">' +
                        '<h3 class="title">' + data[i]["values"]["name"] + '</h3>' +
                        '<span> Message: ' + data[i]["values"]["message"] + '</span></div></a></div>';
                    $("#houseDetails").append(newHouseDetails);
                }
            }
        });
    });
};

houserInformationGadget.getHouseStatusLevel = function (alertLevel) {
    return houserInformationGadget.houseStatusLevel[alertLevel];
};

houserInformationGadget.subscribe = function (callback) {
    gadgets.HubSettings.onConnect = function () {
        gadgets.Hub.subscribe("subscriber", function (topic, data) {
            callback(topic, data)
        });
    };
};

houserInformationGadget.onclick = function (event, item) {

};

$(document).ready(function () {
    houserInformationGadget.fetch();
    houserInformationGadget.startPolling();
});