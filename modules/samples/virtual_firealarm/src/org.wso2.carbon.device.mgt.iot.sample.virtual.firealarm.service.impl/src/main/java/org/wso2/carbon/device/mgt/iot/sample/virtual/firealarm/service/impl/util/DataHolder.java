/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.service.impl.util;

import org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.service.impl.dao.DeviceRecord;
import org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.service.impl.dao.SensorRecord;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DataHolder {

    private static DataHolder instance = new DataHolder();
    private Map<String, DeviceRecord> deviceMap = new HashMap<>();

    private DataHolder() {
    }

    public static DataHolder getInstance() {
        return instance;
    }

    public boolean setSensorRecord(String deviceId, String sensorName, String sensorValue, long time){
        DeviceRecord deviceRecord = new DeviceRecord(sensorName, sensorValue, time);
        deviceMap.put(deviceId, deviceRecord);
        return true;
    }

    /**
     * Returns last updated sensor records list for a device
     * @param deviceId
     * @return
     */
    public SensorRecord[] getSensorRecords(String deviceId){
        Collection<SensorRecord> list = deviceMap.get(deviceId).getSensorDataList().values();
        return list.toArray(new SensorRecord[list.size()]);
    }

    /**
     * Returns last updated sensor record for a device's sensor
     * @param deviceId
     * @param sensorName
     * @return
     */
    public SensorRecord getSensorRecord(String deviceId, String sensorName){
        return deviceMap.get(deviceId).getSensorDataList().get(sensorName);
    }

    /**
     * Returns last updated sensor value for a device's sensor
     * @param deviceId
     * @param sensorName
     * @return
     */
    public String getSensorRecordValue(String deviceId, String sensorName){
        return deviceMap.get(deviceId).getSensorDataList().get(sensorName).getSensorValue();
    }

    /**
     * Returns last updated sensor value reading time for a device's sensor
     * @param deviceId
     * @param sensorName
     * @return
     */
    public long getSensorRecordTime(String deviceId, String sensorName){
        return deviceMap.get(deviceId).getSensorDataList().get(sensorName).getTime();
    }

}
