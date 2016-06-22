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

package org.homeautomation.doormanager.plugin.constants;

import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

/**
 * Device type specific constants which includes all transport protocols configurations,
 * stream definition and device specific dome constants
 */
public class DoorManagerConstants {
    public final static String STATE_LOCK = "LOCK";
    public final static String STATE_UNLOCK = "UNLOCK";
    public final static String STATE_BULB_ON = "ON";
    public final static String STATE_BULB_OFF = "OFF";

    //sensor events summerized table name
    public static final String SENSOR_TYPE1_EVENT_TABLE = "ORG_WSO2_IOT_DEVICES_smartLock";
    public static final String SENSOR_TYPE2_EVENT_TABLE = "ORG_WSO2_IOT_DEVICES_smartFan";
    public final static String DEVICE_TYPE_PROVIDER_DOMAIN = "carbon.super";
    public final static String SENSOR_TYPE1 = "smartLock";
    public final static String SENSOR_TYPE2 = "smartFan";

    //mqtt transport related constants
    //mqtt tranport related constants
    public static final String MQTT_ADAPTER_TOPIC_PROPERTY_NAME = "mqtt.adapter.topic";
    public static final String ADAPTER_TOPIC_PROPERTY = "topic";
    public static final String MQTT_TOPIC_FOR_BULB = "/bulb/command";
    public static final String MQTT_TOPIC_FOR_FAN = "/fan/command";
    public static final String MQTT_TOPIC_FOR_LOCK ="/lock/command";

    public final static String DEVICE_TYPE = "doormanager";
    public final static String DEVICE_PLUGIN_DEVICE_NAME = "DEVICE_NAME";
    public final static String DEVICE_PLUGIN_DEVICE_ID = "doormanager_DEVICE_ID";
    public final static String DEVICE_PLUGIN_DEVICE_SERIAL_NUMBER = "SERIAL_NUMBER";
    public final static String DEVICE_PLUGIN_DEVICE_UID_OF_USER = "UID_of_USER";
    public final static String DEVICE_PLUGIN_PROPERTY_ACCESS_TOKEN = "ACCESS_TOKEN";
    public final static String DEVICE_PLUGIN_PROPERTY_REFRESH_TOKEN = "REFRESH_TOKEN";
    public final static String DEVICE_META_INFO_DEVICE_ID = "meta_deviceId:";
    public final static String DEVICE_META_INFO_DEVICE_TYPE = "meta_deviceType:";
    public final static String DEVICE_META_INFO_TIME = "meta_time";
    public final static String DEVICE_SCOPE_INFO_DEVICE_TYPE = "device_type_";
    public final static String DEVICE_SCOPE_INFO_DEVICE_ID = " device_";
}

