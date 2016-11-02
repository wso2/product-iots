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
            // Update the date to yesterday
            var currentTime = new Date();

            // returns the month (from 0 to 11)
            var month = currentTime.getMonth() + 1

            // returns the day of the month (from 1 to 31)
            var day = currentTime.getDate() - 1     // Yesterday

            // returns the year (four digits)
            var year = currentTime.getFullYear()

            conf['provider-conf'].query = "year: " + year + " AND month: " + month + " AND day: " + day;
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

var getProviderData = function (){
    $.ajax({
        url: gadgetLocation + '/gadget-controller.jag?action=getData',
        method: "POST",
        data: JSON.stringify(conf),
        contentType: "application/json",
        async: false,
        success: function (data) {
            data.forEach(function(entry) {
                entry.duration = entry.duration / 60000;  // Convert to minutes
            });
            providerData = data;
        }
    });
 return providerData;
};


var drawGadget = function (){
    
    draw('#canvas', conf[CHART_CONF], schema, providerData);
    setInterval(function() {
        draw('#canvas', conf[CHART_CONF], schema, getProviderData());
    },pref.getInt(REFRESH_INTERVAL));
    
};

getGadgetLocation(function (gadget_Location) {
    gadgetLocation = gadget_Location;
    init();
    drawGadget();

});
});
