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
    "id": "floorSummary-table",
    "polling_interval": 30000,
    "pub_sub_channel": "filter",
    "defaultSource": "ORG_WSO2_IOT_SMARTHOMEANALYTICS_LOCATION_INFORMATION",
    "currentStatusOfSensorsSource" : "ORG_WSO2_IOT_SMARTHOMEANALYTICS_SENSORS_CURRENTSTATUS",
    "apiContext":"/portal/dashboards/smart-home-automation",
    "domain": "carbon.super",
    "tableConfiguration":{
        "columns": ["homeId", "locationName", "locationId", "alertLevel", "message"],
        "columnTitles": ["Home ID", "Location", "Location Id", "Alert Level", "Message"],
        "columnTypes": ["ordinal", "ordinal", "ordinal", "ordinal", "ordinal"],
        "tableKey":"locationId",
        "tableTitle":"deviceTable"
    },
    "sensorStatusLevel":{
        "ok": 0,
        "warning": 1,
        "danger": 2
    }
};
