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

package org.wso2.carbon.iot.android.sense.scheduler;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.iot.android.sense.events.input.Location.LocationData;
import org.wso2.carbon.iot.android.sense.events.input.Sensor.SensorData;
import org.wso2.carbon.iot.android.sense.events.input.battery.BatteryData;
import org.wso2.carbon.iot.android.sense.util.DataMap;
import org.wso2.carbon.iot.android.sense.util.LocalRegister;
import org.wso2.carbon.iot.android.sense.util.SenseClient;
import java.util.List;


public class DataUploaderService extends Service {

    public static Context context;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = this;

        Log.i("SENDING DATA", "service started");

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                try {

                    JSONObject jsonObject = new JSONObject();
                    JSONArray sensorJsonArray = new JSONArray();

                    jsonObject.put("owner", LocalRegister.getUsername(context));
                    jsonObject.put("deviceId", LocalRegister.getDeviceId(context));

                    boolean noSensorData = false;
                    boolean noBatteryData = false;
                    boolean noLocationData = false;


                    List<SensorData> sensorDataMap = DataMap.getSensorDataMap();
                    if (sensorDataMap.size() <= 0) {
                        noSensorData = true;
                    }
                    for (SensorData sensorData : sensorDataMap) {
                        JSONObject sensorJsonObject = new JSONObject();
                        sensorJsonObject.put("time", "" + sensorData.getCollectTimestamp());
                        sensorJsonObject.put("key", "" + sensorData.getSensorType());
                        sensorJsonObject.put("value", sensorData.getSensorValues());
                        sensorJsonArray.put(sensorJsonObject);
                    }
                    DataMap.resetSensorDataMap();

                    List<BatteryData> batteryDataMap = DataMap.getBatteryDataMap();
                    if (batteryDataMap.size() <= 0) {
                        noBatteryData = true;
                    }
                    for (BatteryData batteryData : batteryDataMap) {
                        JSONObject batteryJsonObject = new JSONObject();
                        batteryJsonObject.put("time", "" + batteryData.getTimestamp());
                        batteryJsonObject.put("key", "battery");
                        batteryJsonObject.put("value", batteryData.getLevel());
                        sensorJsonArray.put(batteryJsonObject);
                    }
                    DataMap.resetBatteryDataMap();

                    List<LocationData> locationDataMap = DataMap.getLocationDataMap();
                    if (locationDataMap.size() <= 0) {
                        noLocationData = true;
                    }
                    for (LocationData locationData : locationDataMap) {
                        JSONObject locationJsonObject = new JSONObject();
                        locationJsonObject.put("time", "" + locationData.getTimeStamp());
                        locationJsonObject.put("key", "GPS");
                        locationJsonObject.put("value", locationData.getLatitude() + "," + locationData.getLongitude());
                        sensorJsonArray.put(locationJsonObject);
                    }
                    DataMap.resetLocationDataMap();

                    jsonObject.put("values", sensorJsonArray);

                    if (!(noSensorData && noBatteryData && noLocationData)) {
                        SenseClient client = new SenseClient(context);
                        client.sendSensorDataToServer(jsonObject.toString());

                    }

                } catch (JSONException e) {
                    Log.i("Data Upload", " Json Data Parsing Exception");
                }
            }
        };

        Thread dataUploaderThread = new Thread(runnable);
        dataUploaderThread.start();

        return Service.START_NOT_STICKY;
    }
}