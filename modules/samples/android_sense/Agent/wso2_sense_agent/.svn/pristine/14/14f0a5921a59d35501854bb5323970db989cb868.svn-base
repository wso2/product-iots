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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.wso2.carbon.iot.android.sense.util.DataMap;

import java.util.Vector;

/**
 * Whenever battery level changes This receiver will be triggered
 */
public class BatteryDataReceiver extends BroadcastReceiver {

    private Vector<BatteryData> batteryDatas = new Vector<BatteryData>();

    @Override
    public void onReceive(Context context, Intent intent) {


        BatteryData bt = new BatteryData(intent);
        batteryDatas.add(bt);

        for (BatteryData data : batteryDatas) {
            DataMap.getBatteryDataMap().add(data);
        }
    }

}
