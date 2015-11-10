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


import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

public class SenseWakeLock {

    private static PowerManager.WakeLock wakeLock;

    public static void acquireWakeLock(Context context) {

        Log.i(SenseWakeLock.class.getSimpleName(), "Acquire CPU wakeup lock start");

        if (wakeLock == null) {

            Log.i(SenseWakeLock.class.getSimpleName(),"CPU wakeUp log is not null");

            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"SenseWakeLock");
        }

        wakeLock.acquire();

    }

    public static void releaseCPUWakeLock() {

        if (wakeLock != null) {


            wakeLock.release();
            wakeLock = null;
        }

        Log.i(SenseWakeLock.class.getSimpleName(),"Release  wakeup");

    }

}
