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

var analyticsClient = new AnalyticsClient().init(null,null,"/portal/apis/analytics");


var oTable;
var nanoScrollerSelector = $('.nano');

floorViewGadget.meta = {
    "names": floorViewGadget.tableConfiguata.columns,
    "types": floorViewGadget.tableConfiguata.columnTypes
};
floorViewGadget.config = {
    key: floorViewGadget.tableConfiguata.tableKey,
    title:floorViewGadget.tableConfiguata.tableTitle,
    charts: [{
        type: "table",
        columns: floorViewGadget.tableConfiguata.columns,
        columnTitles: floorViewGadget.tableConfiguata.columnTitles
    }],
    width: $(window).width()* 0.95,
    height: $(window).width() * 0.65 > $(window).height() ? $(window).height() : $(window).width() * 0.65,
    padding: { "top": 18, "left": 30, "bottom": 22, "right": 70 }
};

floorViewGadget.loadTemplateValues = function(columns){
    for(i=0; i<columns.length; i++){
        floorViewGadget.cellTemplates[columns[i]] = '{{'+columns[i]+'}}';
    }
}

floorViewGadget.loadSpecificTemplateValues = function(){
    // floorViewGadget.cellTemplates["floorName"]='<a target="_blank" href="'+'{{floor-summary}}'+'">
    // <span class="fw fw-stack"><i class="fw fw-edit fw-stack-1x"></i><i class="fw fw-circle-outline fw-stack-2x">
    // </i></span>{{floorName}}</a>';
}

floorViewGadget.loadTemplateValues(floorViewGadget.tableConfiguata.columns);
floorViewGadget.loadSpecificTemplateValues();

floorViewGadget.initialize = function () {
    floorViewGadget.table = new vizg(
        [
            {
                "metadata": floorViewGadget.meta,
                "data": floorViewGadget.data
            }
        ],
        floorViewGadget.config
    );
    floorViewGadget.table.draw(floorViewGadget.div);
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
    floorViewGadget.startPolling();
};



floorViewGadget.startPolling = function () {
    setTimeout(function () {
        floorViewGadget.update();
    }, 500);
    this.polling_task = setInterval(function () {
        floorViewGadget.update();
    }, gadgetConfig.polling_interval);
};

floorViewGadget.update = function (force) {
    floorViewGadget.force_fetch = !floorViewGadget.force_fetch ? force || false : true;
    if (!floorViewGadget.freeze) {
        document.getElementById("table").innerHTML = "";
        floorViewGadget.data = [];
        floorViewGadget.table = new vizg(
            [
                {
                    "metadata": floorViewGadget.meta,
                    "data": floorViewGadget.data
                }
            ],
            floorViewGadget.config
        );
        floorViewGadget.table.draw(floorViewGadget.div);
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
        floorViewGadget.fetch(function (data) {
            floorViewGadget.table.insert(data);
        });
    }
};

function getSummary(callBack){
    var tableName = floorViewGadget.dataSource;
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


floorViewGadget.fetch = function (cb) {
    floorViewGadget.data.length = 0;
    floorViewGadget.data = [];
    floorViewGadget.force_fetch = false;
    getSummary(function(response){
            var data = response;
            //var floorSummary = gadgetUtils.getDashboardURL() + floorViewGadget.apiContext + "/floor-summary?homeId=";
            if (data && data.length > 0) {mplateInfo = [];
                var templateInfo = [];
                for (var i = 0; i < data.length; i++) {
                   /* for(var k=0; k< Object.keys(data[i]["values"]).length; k++){
                        //var currentValue = data[k]["values"][k];
                        console.log("--------->"+ Object.keys(data[i]["values"])[k]);
                        //templateInfo.push( Mustache.to_html(floorViewGadget.cellTemplates[ currentValue], { currentValue : data[i]["values"]["meta_owner"]}));
                    }
                    console.log("--------------------------------------------");*/
                    //console.log(data[i]["values"]);
                    floorViewGadget.data.push(
                        [
                            Mustache.to_html(floorViewGadget.cellTemplates['meta_owner']
                                , {'meta_owner': data[i]["values"]["meta_owner"]}),
                            Mustache.to_html(floorViewGadget.cellTemplates['meta_deviceType'], {'meta_deviceType': data[i]["values"]["meta_deviceType"]}),
                            Mustache.to_html(floorViewGadget.cellTemplates['meta_deviceId'], {'meta_deviceId': data[i]["values"]["meta_deviceId"]}),
                            Mustache.to_html(floorViewGadget.cellTemplates['meta_floorId'], {'meta_floorId': data[i]["values"]["meta_floorId"]}),
                            Mustache.to_html(floorViewGadget.cellTemplates['meta_homeId'], {'meta_homeId': data[i]["values"]["meta_homeId"]}),
                            Mustache.to_html(floorViewGadget.cellTemplates['sensorId'], {'sensorId': data[i]["values"]["sensorId"]}),
                            Mustache.to_html(floorViewGadget.cellTemplates['sensorType'], {'sensorType': data[i]["values"]["sensorType"]}),
                            Mustache.to_html(floorViewGadget.cellTemplates['sensorCoordinates'], {'sensorCoordinates': data[i]["values"]["sensorCoordinates"]}),
                            Mustache.to_html(floorViewGadget.cellTemplates['groups'], {'groups': data[i]["values"]["groups"]}),
                            Mustache.to_html(floorViewGadget.cellTemplates['sensorStatus'], {'sensorStatus': data[i]["values"]["sensorStatus"]})
                        ]
                    );
                }
                if (floorViewGadget.force_fetch) {
                    floorViewGadget.update();
                } else {
                    cb(floorViewGadget.data);
                }
            }
    });
};

floorViewGadget.subscribe = function (callback) {
    gadgets.HubSettings.onConnect = function () {
        gadgets.Hub.subscribe("subscriber", function (topic, data, subscriber) {
            callback(topic, data)
        });
    };
};

floorViewGadget.onclick = function (event, item) {

};

$(document).ready(function () {
    floorViewGadget.initialize();
});