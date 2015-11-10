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

package org.wso2.carbon.iot.android.sense.events.input.Sensor;

import java.util.Calendar;

import android.hardware.SensorEvent;

public class SensorData {
    private int sensorType;
    private String sensorName;
    private String sensorVendor;
    private String sensorValues;
    private int accuracyStatus;
    private String timestamp;
    private String collectTimestamp;

    SensorData(SensorEvent event) {
        sensorValues = "";
        for (int m = 0; m < event.values.length; m++) {
            sensorValues += event.values[m] + ",";

        }
        sensorValues = sensorValues.substring(0, sensorValues.length() - 1);
        accuracyStatus = event.accuracy;

        collectTimestamp = "" + event.timestamp;
        timestamp = "" + Calendar.getInstance().getTimeInMillis();
        sensorName = event.sensor.getName();
        sensorVendor = event.sensor.getVendor();
        sensorType = event.sensor.getType();

    }

    public int getSensorType() {
        return sensorType;
    }

    public void setSensorType(int sensorType) {
        this.sensorType = sensorType;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public String getSensorVendor() {
        return sensorVendor;
    }

    public void setSensorVendor(String sensorVendor) {
        this.sensorVendor = sensorVendor;
    }

    public String getSensorValues() {
        return sensorValues;
    }

    public void setSensorValues(String sensorValues) {
        this.sensorValues = sensorValues;
    }

    public int getAccuracyStatus() {
        return accuracyStatus;
    }

    public void setAccuracyStatus(int accuracyStatus) {
        this.accuracyStatus = accuracyStatus;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getCollectTimestamp() {
        return collectTimestamp;
    }

    public void setCollectTimestamp(String collectTimestamp) {
        this.collectTimestamp = collectTimestamp;
    }
}
