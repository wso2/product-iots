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

var floorViewGadget = floorViewGadget || {};
floorViewGadget.table = null;
floorViewGadget.polling_task = null;
floorViewGadget.data = [];
floorViewGadget.filter_context = null;
floorViewGadget.filters_meta = {};
floorViewGadget.filters = [];
floorViewGadget.filter_prefix = "g_";
floorViewGadget.selected_filter_groups = [];
floorViewGadget.force_fetch = false;
floorViewGadget.freeze = false;
floorViewGadget.div = "#table";
floorViewGadget.dataSource = gadgetConfig.defaultSource;
floorViewGadget.tableConfiguata = gadgetConfig.tableConfiguration;
floorViewGadget.cellTemplates = {};
floorViewGadget.apiContext = gadgetConfig.apiContext;
floorViewGadget.sensorTypeAnalyticsPages = gadgetConfig.sensorTypeAnalyticsPages;
floorViewGadget.currentStatusOfSensorsSource = gadgetConfig.currentStatusOfSensorsSource;
floorViewGadget.sensorStatusLevel = gadgetConfig.sensorStatusLevel;
floorViewGadget.refreshIntervalId = "";

var analyticsClient = new AnalyticsClient().init(null, null, "/portal/apis/analytics");

floorViewGadget.getSummary = function(callBack) {
    var tableName = floorViewGadget.dataSource;
    var queryParam = gadgetUtils.getQueryString();
    var query = "meta_homeId:" + queryParam["homeId"] + " AND _meta_sensorLocationId:\"" + queryParam["locationId"]
        + "\"";
    var queryInfo = floorViewGadget.queryBuilder(tableName, query, 0, 1000, null);
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
};

floorViewGadget.getCurrentStatus = function(records, queryParam, callBack) {

    var processedTask = 0;
    records.forEach(function (current_value, index) {
        var tableName = floorViewGadget.currentStatusOfSensorsSource;
        var query = "meta_homeId:home1 AND _meta_sensorLocation:\"" + records[index]["values"]["meta_sensorLocationId"]
            + "\" AND _meta_sensorId:\"" + records[index]["values"]["meta_sensorId"]+ "\"" ;
        var queryInfo = floorViewGadget.queryBuilder(tableName, query, 0, 1000, null);
        analyticsClient.search(queryInfo, function (d) {
            if (d["status"] === "success") {
                var sensorInformation = JSON.parse(d["message"]);
                if(sensorInformation && sensorInformation.length > 0){
                    records[index]["values"]["alertLevel"] = sensorInformation[0]["values"]["currentStatus"];
                    records[index]["values"]["message"] = sensorInformation[0]["values"]["message"];
                }else{
                    records[index]["values"]["alertLevel"] = "undefined";
                    records[index]["values"]["message"] = "not yet available";
                }
                processedTask++;
                if (processedTask == records.length) {
                    callBack(records,queryParam);
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

floorViewGadget.queryBuilder= function(tableName, query, start, count, sortBy) {
    return {
        tableName: tableName,
        searchParams: {
            query: query,
            start: start, //starting index of the matching record set
            count: count, //page size for pagination
            sortBy: sortBy
        }
    };
};

floorViewGadget.fetch = function () {
    floorViewGadget.getSummary(function (response, queryParam) {
        floorViewGadget.getCurrentStatus(response, queryParam,function(response, queryParam) {
            var data = response;
            var apiContext = gadgetUtils.getDashboardURL() + floorViewGadget.apiContext;
            if (data && data.length > 0) {
                $("#sensorDetails").empty().append("<br/>");
                for (var i = 0; i < data.length; i++) {
                        var sensorTypePage = apiContext + '/'
                        + floorViewGadget.sensorTypeAnalyticsPages[data[i]["values"]["meta_sensorType"]]
                        + "?homeId=" + queryParam["homeId"] + "&sensorLocationId="
                        + data[i]["values"]["meta_sensorLocationId"] + "&sensorId=" + data[i]["values"]["meta_sensorId"];
                    var newSensorDetails =
                        '<div class="col-xs-3"><a target="_blank" href="' + sensorTypePage + '">' +
                        '<div class="tile '+floorViewGadget.getSensorStatusLevel(data[i]["values"]["alertLevel"])+'">' +
                        '<h3 class="title">' + (data[i]["values"]["meta_sensorType"]).toUpperCase() + '</h3>' +
                        '<span> Sensor Location: ' + data[i]["values"]["meta_sensorLocation"] + '</span><br>' +
                        '<span> Sensor Coordinates:' + data[i]["values"]["sensorCoordinates"] + '</span><br>' +
                        '<span> Groups: ' + data[i]["values"]["groups"] + '</span><br>' +
                        '<span> Sensor Status:' + data[i]["values"]["sensorStatus"] + '</span><br>' +
                        '<span> Message:' + data[i]["values"]["message"] + '</span><br>' +
                        '<span> HomeId: ' + data[i]["values"]["meta_homeId"] + '</span></div></a></div>';
                    $("#sensorDetails").append(newSensorDetails);
                }
            }
        });
    });
};

floorViewGadget.getSensorStatusLevel = function(alertLevel){
    return floorViewGadget.sensorStatusLevel[alertLevel];
};

floorViewGadget.onclick = function (event, item) {

};

floorViewGadget.update = function () {
    floorViewGadget.fetch();
};

floorViewGadget.startPolling = function () {
    if(floorViewGadget.refreshIntervalId){
        clearInterval(floorViewGadget.refreshIntervalId);
    }
    floorViewGadget.refreshIntervalId = setInterval(function () {
        floorViewGadget.update();
    }, gadgetConfig.pollingInterval);
};

$(document).ready(function () {
    floorViewGadget.fetch();
    floorViewGadget.startPolling();
});