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

var locationHistory;
var locationFrequency;
var gadgetLocation;

$(function() {
    var conf;
    var schema;
    var pref = new gadgets.Prefs();

    var refreshInterval;
    var providerData;


    var CHART_CONF = 'chart-conf';
    var PROVIDER_CONF = 'provider-conf';

    var REFRESH_INTERVAL = 'refreshInterval';

    var init = function() {
        $.ajax({
            url: gadgetLocation + '/conf.json',
            method: "GET",
            contentType: "application/json",
            async: false,
            success: function(data) {
                conf = JSON.parse(data);
                $.ajax({
                    url: gadgetLocation + '/gadget-controller.jag?action=getSchema',
                    method: "POST",
                    data: JSON.stringify(conf),
                    contentType: "application/json",
                    async: false,
                    success: function(data) {
                        schema = data;
                    }
                });
                // getLocationHistory();
            }
        });
    };


    var drawGadget = function() {
        // Realtime data
        registerCallBackforPush(conf[PROVIDER_CONF], schema, function(data) {
            user = data[0][1];
            deviceId = data[0][2];
            latitude = data[0][4];
            longitude = data[0][5];
            message = "<p align='center'><b>" + user + "</b><br/>[" + latitude + ", " + longitude + "]</p>";
            if (currentView == "current-location") {
                geoMap.showCurrentLocation(deviceId, latitude, longitude, message);
            }
        });
    };

    getGadgetLocation(function(gadget_Location) {
        gadgetLocation = gadget_Location;
        init();
        drawGadget();
    });
});

var getLocationHistory = function() {
    $.ajax({
        url: gadgetLocation + '/gadget-controller.jag?action=getData',
        method: "POST",
        data: '{ "chart-conf": { "chart-name": "number-chart", "gadget-name": "Android Location Map", "maxLength": "10000", "title": "Android Location Map", "x": "duration" }, "provider-conf": { "limit": "10000", "provider-name": "batch", "query": "", "tableName": "ORG_WSO2_IOT_ANDROID_LOCATION" } }',
        contentType: "application/json",
        async: false,
        success: function(data) {
            locationHistory = {
                type: "Feature",
                geometry: {
                    type: "MultiPoint",
                    coordinates: [],
                },
                properties: {
                    time: []
                }
            };
            data.forEach(function(entry) {
                locationHistory.geometry.coordinates.push([entry.longitude, entry.latitude]);
                locationHistory.properties.time.push(entry.meta_timestamp);
            });
        }
    });
    return locationHistory;
};


var getLocationFrequency = function() {
    $.ajax({
        url: gadgetLocation + '/gadget-controller.jag?action=getData',
        method: "POST",
        data: '{ "chart-conf": { "chart-name": "number-chart", "gadget-name": "Android Location Map", "maxLength": "10000", "title": "Android Location Map", "x": "duration" }, "provider-conf": { "limit": "10000", "provider-name": "batch", "query": "", "tableName": "ANDROID_LOCATION_STATS_PER_YEAR" } }',
        contentType: "application/json",
        async: false,
        success: function(data) {
            locationFrequency = [];
            data.forEach(function(entry) {
                locationFrequency.push([entry.latitude, entry.longitude, entry.noOfVisits]);
            });
        }
    });
    return locationFrequency;
};
