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

var groupSummaryGadget = groupSummaryGadget || {};
groupSummaryGadget.table = null;
groupSummaryGadget.polling_task = null;
groupSummaryGadget.data = [];
groupSummaryGadget.filter_context = null;
groupSummaryGadget.filters_meta = {};
groupSummaryGadget.filters = [];
groupSummaryGadget.filter_prefix = "g_";
groupSummaryGadget.selected_filter_groups = [];
groupSummaryGadget.force_fetch = false;
groupSummaryGadget.freeze = false;
groupSummaryGadget.div = "#table";
groupSummaryGadget.dataSource = gadgetConfig.defaultSource;
groupSummaryGadget.tableConfiguata = gadgetConfig.tableConfiguration;
groupSummaryGadget.cellTemplates = {};
groupSummaryGadget.apiContext = gadgetConfig.apiContext;


var analyticsClient = new AnalyticsClient().init(null,null,"/portal/apis/analytics");

var oTable;
var nanoScrollerSelector = $('.nano');

groupSummaryGadget.meta = {
    "names": groupSummaryGadget.tableConfiguata.columns,
    "types": groupSummaryGadget.tableConfiguata.columnTypes
};
groupSummaryGadget.config = {
    key: groupSummaryGadget.tableConfiguata.tableKey,
    title: groupSummaryGadget.tableConfiguata.tableTitle,
    charts: [{
        type: "table",
        columns: groupSummaryGadget.tableConfiguata.columns,
        columnTitles: groupSummaryGadget.tableConfiguata.columnTitles
    }],
    width: $(window).width()* 0.95,
    height: $(window).width() * 0.65 > $(window).height() ? $(window).height() : $(window).width() * 0.65,
    padding: { "top": 18, "left": 30, "bottom": 22, "right": 70 }
};

groupSummaryGadget.loadTemplateValues = function(columns){
    for(i=0; i<columns.length; i++){
        groupSummaryGadget.cellTemplates[columns[i]] = '{{'+columns[i]+'}}';
    }
}

groupSummaryGadget.loadSpecificTemplateValues = function(){
    groupSummaryGadget.cellTemplates["groupName"]='<a target="_blank" href="'+'{{group-summary}}'+'"><span class="fw fw-stack"><i class="fw fw-edit fw-stack-1x"></i><i class="fw fw-circle-outline fw-stack-2x"></i></span>{{groupName}}</a>';
}

groupSummaryGadget.loadTemplateValues(groupSummaryGadget.tableConfiguata.columns);
groupSummaryGadget.loadSpecificTemplateValues();

groupSummaryGadget.initialize = function () {
    groupSummaryGadget.table = new vizg(
        [
            {
                "metadata": groupSummaryGadget.meta,
                "data": groupSummaryGadget.data
            }
        ],
        groupSummaryGadget.config
    );
    groupSummaryGadget.table.draw(groupSummaryGadget.div);
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
    groupSummaryGadget.startPolling();
};



groupSummaryGadget.startPolling = function () {
    setTimeout(function () {
        groupSummaryGadget.update();
    }, 500);
    this.polling_task = setInterval(function () {
        groupSummaryGadget.update();
    }, gadgetConfig.polling_interval);
};

groupSummaryGadget.update = function (force) {
    groupSummaryGadget.force_fetch = !groupSummaryGadget.force_fetch ? force || false : true;
    if (!groupSummaryGadget.freeze) {
        document.getElementById("table").innerHTML = "";
        groupSummaryGadget.data = [];
        groupSummaryGadget.table = new vizg(
            [
                {
                    "metadata": groupSummaryGadget.meta,
                    "data": groupSummaryGadget.data
                }
            ],
            groupSummaryGadget.config
        );
        groupSummaryGadget.table.draw(groupSummaryGadget.div);
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
        groupSummaryGadget.fetch(function (data) {
            groupSummaryGadget.table.insert(data);
        });
    }
};

function getSummary(callBack){
    var tableName = groupSummaryGadget.dataSource;
    var queryParam = gadgetUtils.getQueryString();
    //var query = "homeId : " + queryParam["homeId"];
    //var query = "year :29016 AND " + queryParam["homeId"];
    var query = "";
    var queryInfo = queryBuilder(tableName, query, 0, 1000, null);
    analyticsClient.search(queryInfo, function (d) {
        if (d["status"] === "success") {
            var totalRecords = JSON.parse(d["message"]);
            callBack(totalRecords);
        }
    }, function (error) {
        if(error === undefined){
            console.log("Analytics server not found : Please troubleshoot connection problems.");
        }else{
            error.message = "Internal server error while data indexing.";
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


groupSummaryGadget.fetch = function (cb) {
    groupSummaryGadget.data.length = 0;
    groupSummaryGadget.data = [];
    groupSummaryGadget.force_fetch = false;
    getSummary(function(response){
            var data = response;
            var groupSummary = gadgetUtils.getDashboardURL() + groupSummaryGadget.apiContext + "/group-summary?homeId=";
            if (data && data.length > 0) {
                    for (var i = 0; i < data.length; i++) {
                    groupSummaryGadget.data.push(
                        [
                            Mustache.to_html(groupSummaryGadget.cellTemplates['homeId'], {'homeId': data[i]["values"]["homeId"]}),
                            Mustache.to_html(groupSummaryGadget.cellTemplates['groupName'], {'groupName': data[i]["values"]["groupName"]
                                , 'group-summary': groupSummary + data[i]["values"]["homeId"]+"&groupId=" + data[i]["values"]["groupId"]}),
                            Mustache.to_html(groupSummaryGadget.cellTemplates['groupId'], {'groupId': data[i]["values"]["groupId"]}),
                            Mustache.to_html(groupSummaryGadget.cellTemplates['alertLevel'], {'alertLevel': data[i]["values"]["alertLevel"]}),
                            Mustache.to_html(groupSummaryGadget.cellTemplates['message'], {'message': data[i]["values"]["message"]}),
                            Mustache.to_html(groupSummaryGadget.cellTemplates['summary'], {'summary': data[i]["values"]["summary"]})
                        ]
                    );
                }
                if (groupSummaryGadget.force_fetch) {
                    groupSummaryGadget.update();
                } else {
                    cb(groupSummaryGadget.data);
                }
            }
    });
};

groupSummaryGadget.subscribe = function (callback) {
    gadgets.HubSettings.onConnect = function () {
        gadgets.Hub.subscribe("subscriber", function (topic, data, subscriber) {
            callback(topic, data)
        });
    };
};

groupSummaryGadget.onclick = function (event, item) {

};

$(document).ready(function () {
    groupSummaryGadget.initialize();
});