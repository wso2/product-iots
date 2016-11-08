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

var gadgetConfig = {
    "id": "sensorDetails-table",
    "pollingInterval": 5000,
    "pub_sub_channel": "filter",
    "defaultSource": "ORG_WSO2_IOT_SMARTHOMEANALYTICS_SENSOR_INFORMATION",
    "currentStatusOfSensorsSource" : "ORG_WSO2_IOT_SMARTHOMEANALYTICS_SENSORS_CURRENTSTATUS",
    "apiContext":"/portal/dashboards/smart-home-automation",
    "domain": "carbon.super",
    "tableConfiguration":{
        "columns": ["meta_homeId", "meta_sensorLocation", "meta_sensorLocationId", "meta_sensorId", "meta_sensorType", "sensorCoordinates", "groups", "sensorStatus"],
        "columnTitles": [ "Home Id", "Sensor Location", "Sensor Location Id", "Sensor Id", "Sensor Type", "Sensor Coordinates", "Groups", "Sensor Status"],
        "columnTypes": ["ordinal", "ordinal", "ordinal", "ordinal", "ordinal", "ordinal", "ordinal", "ordinal"],
        "tableKey":"sensorId",
        "tableTitle":"deviceTable"
    },
    "sensorTypeAnalyticsPages":{
        "motiondetector" : "motiondetector",
        "smartswitch" : "smartswitch",
        "weathermonitoringsensor" : "weathermonitoringsensor",
        "smartmeter" : "smartmeter",
        "smartdoor" : "smartdoor",
        "dhtsensor" : "dhtsensor",
        "furnace" : "furnace",
    },
    "sensorStatusLevel":{
        "ok": "green",
        "warning": "orange",
        "danger": "red",
        "undefined": "blue"
    }
};
