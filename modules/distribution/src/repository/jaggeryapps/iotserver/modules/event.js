/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var eventModule;
eventModule = function () {
    var log = new Log("modules/event.js");

    var constants = require("/modules/constants.js");
    var utility = require("/modules/utility.js").utility;

    var publicMethods = {};
    var privateMethods = {};

    var statsClient = new Packages.org.wso2.carbon.device.mgt.iot.common.analytics.statistics.IoTEventsStatisticsClient;

    publicMethods.getEventsData = function (username, recordLimit) {
        var fetchedData = null;

        try {
            fetchedData = statsClient.getRecentDeviceStats(username, recordLimit);
        } catch (error) {
            log.error(error);
        }

        var eventsData = [];

        // -- start of dummy data

        var timeInterval = 30;
        var i, rnd;
        var currentDay = new Date();
        var startDate = currentDay.getTime() - (60 * 60 * 24 * 5);
        var endDate = currentDay.getTime();

        var i = parseInt(startDate / 1000);
        while (i < parseInt(endDate / 1000)) {
            rnd = rnd = Math.random() * 50;
            eventsData.push({time: i*1000, deviceName: 'device' + rnd, activity:'Event number ' + rnd});
            i += timeInterval;
        }

        // -- end of dummy data

        //	for (var i = 0; i < fetchedData.size(); i++) {
        //		eventsData.push({
        //			time: fetchedData.get(i).getTime(),
        //			deviceName: fetchedData.get(i).getDeviceName(),
        //			activity: fetchedData.get(i).getDeviceActivity()
        //		});
        //	};

        return eventsData;
    };


    return publicMethods;
}();


