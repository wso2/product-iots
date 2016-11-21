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

var getDateString, getDuration;

(function() {

    getDateString = function (timestamp) {
        var date = new Date();
        date.setTime(timestamp);
        return date.toString();
    };

    getDuration = function (durationInMS) {
        var time = '';
        var date = new Date();
        date.setTime(durationInMS);

        var day = Math.floor(durationInMS/86400000);
        if (day > 0){
            //More than 1 day
            time = day +' day : ';
            durationInMS = durationInMS - (day * 86400000);
        }
        var hour = Math.floor(durationInMS/3600000);
        if (hour > 0){
            //More than 1 hour
            time = time + hour + ' hour : ';
            durationInMS = durationInMS - (hour * 3600000);
        }

        var minutes = Math.floor(durationInMS/60000);
        if (minutes > 0){
            //More than 1 minute
            time = time + minutes + ' minutes : ';
            durationInMS = durationInMS - (minutes * 60000);
        }

        var seconds = Math.ceil(durationInMS/1000);
        if (seconds > 0){
            //More than 1 minute
            time = time + seconds + ' seconds : ';
        }
        time = time.slice(0, -2);
        return time;
    };
}());
