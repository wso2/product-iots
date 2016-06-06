/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var plotting = function () {
    var api = this;
    api.isDone = false;
    api.currentValue = 0;
    api.finishPlotting = function (callBack) {
        api.isDone = true;
        callBack(true);
    },
    api.initPlotting = function (callback) {
        api.isDone = false;
        callback(true);
    },
    api.forceToRedraw = function (callback) {
        api.isDone == true;
        callback(true);
    },
    api.pushData = function (new_value) {
        api.currentValue = new_value;
    },
    api.realtimePlotting = function (placeholder, placeholderYMin, placeholderYMax, updateInterval, placeholderWidth,
                                      placeholderHeight, placeholderWindowSize, title) {
        $(placeholder).html();
        var initWindow = function () {
            return 0;
        }
        api.data = d3.range(parseInt($(placeholderWindowSize).html())).map(initWindow);
        var margin = {top: 20, right: 20, bottom: 20, left: 40},
            width = placeholderWidth - margin.left - margin.right,
            height = placeholderHeight - margin.top - margin.bottom;
        var x = d3.scale.linear()
            .domain([1, parseInt($(placeholderWindowSize).html()) - 2])
            .range([0, width]);

        var y = d3.scale.linear()
            .domain([parseInt($(placeholderYMin).val()), parseInt($(placeholderYMax).val())])
            .range([height, 0]);
        var line = d3.svg.line()
            .interpolate("basis")
            .x(function (d, i) {
                return x(i);
            })
            .y(function (d, i) {
                return y(d);
            });

        var svg = d3.select(placeholder).append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");
        svg.append("defs").append("clipPath")
            .attr("id", "clip")
            .append("rect")
            .attr("width", width)
            .attr("height", height);
        var axis_x = svg.append("g")
            .attr("class", "x_axis")
            .attr("transform", "translate(0," + y(0) + ")")
            .call(d3.svg.axis().scale(x).orient("bottom"));
        var axis_y = svg.append("g")
            .attr("class", "y_axis")
            .call(d3.svg.axis().scale(y).orient("left"));
        var path = svg.append("g")
            .attr("clip-path", "url(#clip)")
            .append("path")
            .datum(api.data)
            .attr("class", "line")
            .attr("d", line);
        svg.append("text")
            .attr("class", "yaxis_label")
            .attr("transform", "rotate(-90)")
            .attr("y", 0 - margin.left - 4)
            .attr("x", (0 - (height / 2)))
            .attr("dy", "1em")
            .style("text-anchor", "middle")
            .text($(title).val());
        svg.append("text")
            .attr("class", "xaixs_label")
            .attr("transform",
                "translate(" + (width / 2) + " ," +
                (height + margin.bottom) + ")")
            .style("text-anchor", "middle")
            .text("Window Size");
        svg.append("text")
            .attr("class", "title_label")
            .attr("x", (width / 2))
            .attr("y", 0 - (margin.top / 4))
            .attr("text-anchor", "middle")
            .style("font-size", "16px")
            .style("text-decoration", "underline")
            .text($(title).val() + " variation within last " + $(placeholderWindowSize).html() + " frames");

        updateAgain();

        function updateAgain() {
            if (api.isDone)return;
            api.data.push(api.currentValue);
            path.attr("d", line)
                .attr("transform", null)
                .transition()
                .duration($(updateInterval).html())
                .ease("linear")
                .attr("transform", "translate(" + x(0) + ",0)")
                .each("end", updateAgain);
            api.data.shift();
        }

        function rescale() {
            y.domain([parseInt($(placeholderYMin).val()), parseInt($(placeholderYMax).val())])
            svg.select(".title_label")
                .text($(title).val() + " variation within last " + $(placeholderWindowSize).html() + " frames");
            svg.select(".yaxis_label")
                .text($(title).val());
        }

        $("#plottingAttribute").change(function () {
            rescale();
        });
    }
}

