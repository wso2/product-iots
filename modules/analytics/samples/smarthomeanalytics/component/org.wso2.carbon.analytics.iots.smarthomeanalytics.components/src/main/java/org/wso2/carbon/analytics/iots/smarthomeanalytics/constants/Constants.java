/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.60 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.60
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.iot.smarthomeanalytics.constants;

import java.io.File;

public final class Constants {

    public static final int ADAPTER_MIN_THREAD_POOL_SIZE = 8;
    public static final int ADAPTER_MAX_THREAD_POOL_SIZE = 100;
    public static final int ADAPTER_EXECUTOR_JOB_QUEUE_SIZE = 10000;
    public static final long DEFAULT_KEEP_ALIVE_TIME_IN_MILLS = 20000;
    public static final String SERVER_IP_ADDRESS = "localhost";
    public static final String SERVER_PORT = "7711";
    public static final String USERNAME = "admin";
    public static final String PASSWORD = "admin";
    public static final String TRASPORT_LEVEL_PROTOCOL = "tcp://";
    public static final String DATA_BRIDGE_AGENT_CONFIG = "data-agent-config.xml";
    public static final String CLIENT_TRUST_STORE_NAME = "client-truststore.jks";
    public static final String CLIENT_TRUST_STORE_PASSWORD = "wso2carbon";

    public static final String VERSION = "1.0.0";
    public static final String SMART_DOOR_STREAM_DEFINITION = "org.wso2.iot.smarthomeanalytics.sensors.smartdoor";
    public static final String SMART_DOOR_SENSOR_DATA_RESOURCE = File.separator + "smartDoor.csv";
    public static final int SMART_DOOR_STREAM_DATA_PUSH_INTERVAL = 120;

    public static final String SMART_MOTION_DETECTOR_STREAM_DEFINITION
            = "org.wso2.iot.smarthomeanalytics.sensors.smartmotiondetector";
    public static final String SMART_MOTION_DETECTOR_SENSOR_DATA_RESOURCE = File.separator + "motionDetector.csv";
    public static final int SMART_MOTION_DETECTOR_STREAM_DATA_PUSH_INTERVAL = 120;

    public static final String SMART_DHTSENSOR_STREAM_DEFINITION
            = "org.wso2.iot.smarthomeanalytics.sensors.smartdhtsensor";
    public static final String SMART_DHTSENSOR_SENSOR_DATA_RESOURCE = File.separator + "smartDHTSensor.csv";
    public static final int SMART_DHTSENSOR_STREAM_DATA_PUSH_INTERVAL = 120;

    public static final String SMART_FURNCE_STREAM_DEFINITION = "org.wso2.iot.smarthomeanalytics.sensors.smartfurnce";
    public static final String SMART_FURNCE_SENSOR_DATA_RESOURCE = File.separator + "smartFurnce.csv";
    public static final int SMART_FURNCE_STREAM_DATA_PUSH_INTERVAL = 120;

    public static final String SMART_METER_STREAM_DEFINITION = "org.wso2.iot.smarthomeanalytics.sensors.smartmeter";
    public static final String SMART_METER_SENSOR_DATA_RESOURCE = File.separator + "smartMeter.csv";
    public static final int SMART_METER_STREAM_DATA_PUSH_INTERVAL = 120;

    public static final String SMART_SWITCH_STREAM_DEFINITION = "org.wso2.iot.smarthomeanalytics.sensors.smartswitch";
    public static final String SMART_SWITCH_SENSOR_DATA_RESOURCE = File.separator + "smartSwitch.csv";
    public static final int SMART_SWITCH_STREAM_DATA_PUSH_INTERVAL = 120;

    public static final String SMART_WEATHER_MONITORING_SENSOR_STREAM_DEFINITION
            = "org.wso2.iot.smarthomeanalytics.sensors.smartweathermonitoringsensor";
    public static final String SMART_WEATHER_MONITORING_SENSOR_SENSOR_DATA_RESOURCE
            = File.separator + "smartWeatherMonitoringSensor.csv";
    public static final int SMART_WEATHER_MONITORING_SENSOR_STREAM_DATA_PUSH_INTERVAL = 120;

    public static final String SENSOR_INFORMATION_STREAM_DEFINITION
            = "org.wso2.iot.smarthomeanalytics.sensor.information";
    public static final String SENSOR_INFORMATION_DATA_RESOURCE = File.separator + "sensorInformation.csv";
    public static final int SENSOR_INFORMATION_DATA_PUSH_INTERVAL = 120;

    public static final String LOCATION_STREAM_DEFINITION = "org.wso2.iot.smarthomeanalytics.location.information";
    public static final String LOCATION_DATA_RESOURCE = File.separator + "locationinformation.csv";
    public static final int LOCATION_DATA_PUSH_INTERVAL = 120;

    public static final String HOUSE_STREAM_DEFINITION = "org.wso2.iot.smarthomeanalytics.houses.information";
    public static final String HOUSE_DATA_RESOURCE = File.separator + "housesinformation.csv";
    public static final int HOUSE_DATA_PUSH_INTERVAL = 120;

}
