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
$(function () {
    var gadgetLocation;
    var conf;
    var schema;
    var pref = new gadgets.Prefs();

    var refreshInterval;
    var providerData;

    var CHART_CONF = 'chart-conf';
    var PROVIDER_CONF = 'provider-conf';

    var REFRESH_INTERVAL = 'refreshInterval';

    var init = function () {
        $.ajax({
            url: gadgetLocation + '/conf.json',
            method: "GET",
            contentType: "application/json",
            async: false,
            success: function (data) {
                conf = JSON.parse(data);
                $.ajax({
                    url: gadgetLocation + '/gadget-controller.jag?action=getSchema',
                    method: "POST",
                    data: JSON.stringify(conf),
                    contentType: "application/json",
                    async: false,
                    success: function (data) {
                        schema = data;
                    }
                });
                getProviderData();
            }
        });
    };

    var getProviderData = function () {
        $.ajax({
            url: gadgetLocation + '/gadget-controller.jag?action=getData',
            method: "POST",
            data: JSON.stringify(conf),
            contentType: "application/json",
            async: false,
            success: function (data) {
                data.forEach(function (entry) {
                    entry.duration = entry.duration / 60000;  // Convert to minutes
                });
                providerData = data;
            }
        });
        return providerData;
    };


    getGadgetLocation(function (gadget_Location) {
        gadgetLocation = gadget_Location;
        init();
        fillDeviceDetails();
    });

    var fillDeviceDetails = function () {
        var deviceDetailsTable = '';
        if (providerData.length === 1 ) {
            var deviceDetails = providerData[0];
            if (deviceDetails.hasOwnProperty('name')){
                deviceDetailsTable += getDeviceDetailTableEntry('Device Name:', deviceDetails.name)
            }
            if (deviceDetails.hasOwnProperty('model')){
                deviceDetailsTable += getDeviceDetailTableEntry('Device Model:', deviceDetails.model)
            }
            if (deviceDetails.hasOwnProperty('manufacturer')){
                deviceDetailsTable += getDeviceDetailTableEntry('Manufacturer:', deviceDetails.manufacturer)
            }
            if (deviceDetails.hasOwnProperty('id')){
                deviceDetailsTable += getDeviceDetailTableEntry('Identifier:', deviceDetails.id)
            }
            if (deviceDetails.hasOwnProperty('meta_username')){
                deviceDetailsTable += getDeviceDetailTableEntry('Owner:', deviceDetails.meta_username)
            }
            if (deviceDetails.hasOwnProperty('andriod_version')){
                deviceDetailsTable += getDeviceDetailTableEntry('Andriod Version:', deviceDetails.andriod_version)
            }
            if (deviceDetails.hasOwnProperty('memory')){
                deviceDetailsTable += getDeviceDetailTableEntry('Memory:', deviceDetails.memory)
            }
            if (deviceDetails.hasOwnProperty('storage')){
                deviceDetailsTable += getDeviceDetailTableEntry('Storage Space:', deviceDetails.storage)
            }
            if (deviceDetails.hasOwnProperty('installed_apps')){
                deviceDetailsTable += getDeviceDetailTableEntry('Number of Applications Installed:', deviceDetails.installed_apps)
            }
            if (deviceDetails.hasOwnProperty('state')){
                deviceDetailsTable += getDeviceDetailTableEntry('State:', deviceDetails.state);
            }
            document.getElementById('device_details').innerHTML = deviceDetailsTable;
        }
    };
    
    var getDeviceDetailTableEntry = function (detailName, detailValue) {
        return '<tr> <td class="withPadding">' + detailName + '</td> <td class="withPadding">'
            + detailValue + '</td></tr>';
    };
});
