/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 */
package org.wso2.carbon.iot.android.sense.util;

import org.wso2.carbon.iot.android.sense.events.input.Location.LocationData;
import org.wso2.carbon.iot.android.sense.events.input.Sensor.SensorData;
import org.wso2.carbon.iot.android.sense.events.input.battery.BatteryData;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DataMap {

    private static List<SensorData> sensorDataMap;
    private static List<BatteryData> batteryDataMap;
    private static List<LocationData> locationDataMap;


    public static List<SensorData> getSensorDataMap(){
        if(sensorDataMap == null){
            sensorDataMap = new CopyOnWriteArrayList<SensorData>();
        }
        return sensorDataMap;
    }

    public static List<BatteryData> getBatteryDataMap(){
        if(batteryDataMap == null){
            batteryDataMap = new CopyOnWriteArrayList<BatteryData>();
        }
        return batteryDataMap;
    }

    public static List<LocationData> getLocationDataMap(){
        if(locationDataMap == null){
            locationDataMap = new CopyOnWriteArrayList<LocationData>();
        }
        return locationDataMap;
    }

    public static void resetSensorDataMap(){
        sensorDataMap = null;
    }

    public static void resetBatteryDataMap(){
        batteryDataMap = null;
    }

    public static void resetLocationDataMap(){
        locationDataMap = null;
    }


}
