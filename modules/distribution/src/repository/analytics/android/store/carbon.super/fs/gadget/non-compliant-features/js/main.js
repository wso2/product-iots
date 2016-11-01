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

var ncp = ncp || {};
ncp.chart = null;
ncp.polling_task = null;
ncp.data = [];
ncp.filter_context = null;
ncp.filters_meta = {};
ncp.filters = [];
ncp.filter_prefix = "g_";
ncp.selected_filter_groups = [];
ncp.force_fetch = false;
ncp.freeze = false;
ncp.div = "#chart";
ncp.fromIndex = 0;
ncp.count = 5;
ncp.prevTotalPages = -1;
ncp.globalPage = 1;
ncp.filterContextFromURL = "";

ncp.meta = {
    "names": ["id", "name", "Device count"],
    "types": ["ordinal", "ordinal", "linear"]
};

var body = "body";

ncp.config = {
    type: "bar",
    x: "id",
    charts: [
        {
            type: "bar",
            y: "Device count",
            color: "name",
            xTitle: "",
            yTitle: "",
            mode: "group",
            colorScale: ["#D9534F"],
            orientation: "left",
            legend: false
        }
    ],
    //textColor:"#ffffff",
    //text:"count",
    //textAlign:"right",
    //yAxisStrokeSize:0,
    //yAxisFontSize:0,
    grid: false,
    width: $(body).width(),
    height: $(body).height() - 165,
    padding: {"top": 15, "left": 175, "bottom": 30, "right": 25}
};

ncp.initialize = function () {
    //noinspection JSPotentiallyInvalidConstructorUsage
    ncp.chart = new vizg(
        [
            {
                "metadata": ncp.meta,
                "data": ncp.data
            }
        ],
        ncp.config
    );
    ncp.chart.draw("#chart", [
        {
            type: "click",
            callback: ncp.onclick
        }
    ]);
    ncp.startPolling();
};

ncp.changePaginationNumber = function (count) {
    ncp.count = parseInt(count);
    ncp.data = [];
    ncp.initialize();
};

ncp.startPolling = function () {
    setTimeout(function () {
        ncp.update();
        ncp.freeze = ncp.selected_filter_groups.length > 0;
    }, 500);
    //noinspection JSUnusedGlobalSymbols
    this.polling_task = setInterval(function () {
        ncp.update();
    }, gadgetConfig.polling_interval);
};

ncp.update = function () {
    ncp.fetch(function (data) {
        ncp.chart.insert(data);
    });
};

ncp.fetch = function (cb) {
    ncp.data.length = 0;
    ncp.force_fetch = false;
    //noinspection JSUnresolvedVariable
    wso2.gadgets.XMLHttpRequest.get(gadgetConfig.source + "?start=" + ncp.fromIndex + "&length=" + ncp.count,
        function (response) {
            // console.log(JSON.stringify(response));
            if (Object.prototype.toString.call(response) === '[object Array]' && response.length === 1) {
                ncp.filter_context = response[0]["groupingAttribute"];
                var nonCompliantFeatureCount = response[0]["totalRecordCount"];
                var data = response[0]["data"];
                var headerMsgCount = "#header-msg-count";
                var headerMsgText = "#header-msg-text";
                var bodyMsgForCompliance = "#body-msg-for-compliance";
                var chartDiv = "#chart";
                var paginationView = "#pagination-view";
                if (nonCompliantFeatureCount == 0) {
                    $(headerMsgCount).removeClass("label-danger");
                    $(headerMsgCount).addClass("label-success");
                    $(headerMsgCount).addClass("label-zero");
                    $(headerMsgCount).html(nonCompliantFeatureCount.toString());
                    $(headerMsgText).html("non-compliant features found.");
                    $(bodyMsgForCompliance).removeClass("hidden");
                    $(chartDiv).addClass("hidden");
                    $(paginationView).addClass("hidden");
                } else if (data && data.length > 0) {
                    for (var i = 0; i < data.length; i++) {
                        ncp.data.push(
                            [data[i]["group"], data[i]["displayNameForGroup"], data[i]["deviceCount"]]
                        );
                    }
                    if (ncp.force_fetch) {
                        ncp.update();
                    } else {
                        cb(ncp.data);
                    }
                    $(headerMsgCount).removeClass("label-zero");
                    $(headerMsgCount).removeClass("label-success");
                    $(headerMsgCount).addClass("label-danger");
                    $(headerMsgCount).html(nonCompliantFeatureCount.toString());
                    if (nonCompliantFeatureCount == 1) {
                        $(headerMsgText).html("non-compliant feature found.");
                    } else {
                        $(headerMsgText).html("non-compliant features found.");
                    }
                    $(bodyMsgForCompliance).addClass("hidden");
                    ncp.onData(response);
                    $(chartDiv).removeClass("hidden");
                    $(paginationView).removeClass("hidden");
                }
            } else {
                console.error("Invalid response structure found: " + JSON.stringify(response));
            }
        }, function () {
            console.warn("Error accessing source for : " + gadgetConfig.id);
        });
};

ncp.onclick = function (event, item) {
    if (!ncp.filterContextFromURL) {
        var filteringGroup = item.datum[ncp.config.x];
        var url = getBaseURL() + "devices?g_" + ncp.filter_context + "=" + filteringGroup;
        window.open(url);
    }
};

$(document).ready(function () {
    ncp.initialize();
});

ncp.onPaginationClicked = function (e, originalEvent, type, page) {
    ncp.globalPage = page;
    ncp.fromIndex = (page - 1) * ncp.count;
    ncp.globalPage = ncp.fromIndex;
    ncp.data = [];
    //noinspection JSPotentiallyInvalidConstructorUsage
    ncp.chart = new vizg(
        [
            {
                "metadata": ncp.meta,
                "data": ncp.data
            }
        ],
        ncp.config
    );
    ncp.chart.draw("#chart", [
        {
            type: "click",
            callback: ncp.onclick
        }
    ]);

    ncp.update();
};

ncp.onData = function (response) {
    try {
        var allDataCount = response[0]["totalRecordCount"];
        var totalPages = parseInt(allDataCount / ncp.count);
        if (allDataCount % ncp.count != 0) {
            totalPages += 1;
        }
        var options = {
            currentPage: ncp.globalPage,
            totalPages: totalPages,
            onPageClicked: ncp.onPaginationClicked
        };
        $('#idPaginate').bootstrapPaginator(options);
    } catch (e) {
        $('#canvas').html(gadgetUtil.getErrorText(e));
    }
};
