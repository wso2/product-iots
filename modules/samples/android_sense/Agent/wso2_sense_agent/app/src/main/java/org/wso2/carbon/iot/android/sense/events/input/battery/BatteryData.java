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

package org.wso2.carbon.iot.android.sense.events.input.battery;

import java.util.Calendar;

import android.content.Intent;
import android.os.BatteryManager;

public class BatteryData {

    private int health;
    private int level;
    private int plugged;
    private int present;
    private int scale;
    private int status;
    private int temperature;
    private int voltage;
    private String timestamp;

    BatteryData(Intent intent) {
        timestamp = "" + Calendar.getInstance().getTimeInMillis();
        health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
        level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
        present = intent.getExtras().getBoolean(BatteryManager.EXTRA_PRESENT) ? 1 : 0;
        scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
        status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
        String technology = intent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
        temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
        voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);

    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getPlugged() {
        return plugged;
    }

    public void setPlugged(int plugged) {
        this.plugged = plugged;
    }

    public int getPresent() {
        return present;
    }

    public void setPresent(int present) {
        this.present = present;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getVoltage() {
        return voltage;
    }

    public void setVoltage(int voltage) {
        this.voltage = voltage;
    }


    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
