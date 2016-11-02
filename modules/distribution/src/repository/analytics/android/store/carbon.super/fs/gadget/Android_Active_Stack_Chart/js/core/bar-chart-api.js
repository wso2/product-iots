/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
var getConfig, validate, isProviderRequired, draw, update;

(function() {

    var CHART_LOCATION = '/extensions/chart-templates/';

    /**
     * return the config to be populated in the chart configuration UI
     * @param schema
     */
    getConfig = function(schema) {
        var chartConf = require(CHART_LOCATION + '/bar-chart/config.json').config;
        /*
         dynamic logic goes here
         */

        var columns = [];

        columns.push("None");
        for(var i=0; i < schema.length; i++) {
            columns.push(schema[i]["fieldName"]);
        }

        for(var i=0; i < chartConf.length; i++) {
            if (chartConf[i]["fieldName"] == "color") {
                chartConf[i]["valueSet"] = columns;
                break;
            }
        }

        return chartConf;
    };

    /**
     * validate the user inout for the chart configuration
     * @param chartConfig
     */
    validate = function(chartConfig) {
        return true;
    };

    /**
     * TO be used when provider configuration steps need to be skipped
     */
    isProviderRequired = function() {

    }


    /**
     * return the gadget content
     * @param chartConfig
     * @param schema
     * @param data
     */
    draw = function(placeholder, chartConfig, _schema, data) {
        var schema = toVizGrammarSchema(_schema);
        var view = {
            id: "chart-0",
            schema: schema,
            chartConfig: buildChartConfig(chartConfig),
            data: function() {
                if(data) {
                    var result = [];
                    data.forEach(function(item) {
                        var row = [];
                        schema[0].metadata.names.forEach(function(name) {
                            if(name === chartConfig.x){
                                var date = new Date(item[name]);
                                var month = date.getMonth() + 1;
                                month = ("0" + month).slice(-2);
                                row.push(date.getDate()+'/'+month+'/'+ date.getFullYear());
//                                  row.push(item[name]);
                            } else{
                                if(name === chartConfig.y){
                                     row.push(item[name] / (1000*60*60));
                                } else{
                                    row.push(item[name]);
                                }
                            }
                        });
                        result.push(row);
                    });
                    wso2gadgets.onDataReady(result);
                }
            }

        };

        try {
            wso2gadgets.init(placeholder, view);
            var view = wso2gadgets.load("chart-0");
        } catch (e) {
            console.error(e);
        }

    };

    /**
     *
     * @param data
     */
    update = function(data) {
        wso2gadgets.onDataReady(data,"append");
    }

    buildChartConfig = function (_chartConfig) {
        var conf = {};
        conf.x = _chartConfig.x;
        conf.charts = [];
        conf.maxLength = _chartConfig.maxLength;
        conf.charts[0] = {
            type : "bar",
            y: _chartConfig.y
        };

        if (_chartConfig.color != "None") {
            conf.charts[0].color = _chartConfig.color;
            conf.charts[0].mode = _chartConfig.mode;
        }
        conf.legendOffset = -30;
        conf.padding = {"top": 10, "left": 100, "bottom": 70, "right": 0};
        conf.xTitle = "Date";
        conf.yTitle = "Duration (Hours)";
        conf.xAxisAngle = "true";
        console.log('chart config : ');
        console.log(conf);
        return conf;
    };


}());
