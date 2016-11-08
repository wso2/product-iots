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

$('.btn-minimize').click(function (e) {
    e.preventDefault();
    var $target = $(this).parent().parent().next('.box-content');
    if ($target.is(':visible')) {
        $('i', $(this)).removeClass('glyphicon-chevron-up').addClass('glyphicon-chevron-down');
        checkAndDisable($(this).parent().attr('id'));
    }
    else {
        $('i', $(this)).removeClass('glyphicon-chevron-down').addClass('glyphicon-chevron-up');
        checkAndEnable($(this).parent().attr('id'));
    }
    $target.slideToggle();
});

function checkAndEnable(id) {
    if (id === "RealtimePlotting") {
        config.modulesStatus.realtimePlotting = true;
    }
    else if (id === "SensorReadings") {
        config.modulesStatus.sensorReadings = true;
    } else if (id === "Rotation") {
        config.modulesStatus.angleOfRotationv1 = true;
    } else if (id === "AngleOfRotation") {
        config.modulesStatus.angleOfRotationv2 = true;
    }
}

function checkAndDisable(id) {
    if (id === "RealtimePlotting") {
        config.modulesStatus.realtimePlotting = false;
    }
    else if (id === "SensorReadings") {
        config.modulesStatus.sensorReadings = false;
    } else if (id === "Rotation") {
        config.modulesStatus.angleOfRotationv1 = false;
    } else if (id === "AngleOfRotation") {
        config.modulesStatus.angleOfRotationv2 = false;
    }
}

function isJSON(data) {
    try {
        return JSON.parse(data);
    }
    catch (error) {
        return null;
    }
}

function Queue() {
    var a = [], b = '';
    this.enqueue = function (b) {
        a.push([this.getLength() - 1 <= 0 ? 0 : this.getLength() - 1, b]);
    };
    this.dequeue = function () {
        if (0 != a.length) {
            var c = a[b];
            2 * ++b >= a.length && (a = a.slice(b), b = 0);
            return c
        }
    };
    this.getLength = function () {
        return a.length - b;
    };
    this.isEmpty = function () {
        return 0 == a.length;
    };
    this.peek = function () {
        return 0 < a.length ? a[b] : void 0
    };
    this.getData = function () {
        return a;
    };
    this.makeFixedSize = function (start, end) {
        a = a.slice(start, end);
    }
}

$("#module_control button").click(function (index) {
    var url = config.drone_control;
    ajax_handler.ajaxRequest(url, config.controlType, {action: $(this).attr('id'), speed: 7, duration: 7},
        config.dataType, function (data, status) {
        }
    );
});



