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

package org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.service.impl.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement
public class DeviceRecord {
    //all int, float, boolean should be converted into string
    //when saving on the map
    private Map<String, SensorRecord> sensorDataList = new HashMap<>();

    public DeviceRecord(String sensorName, String sensorValue, long time) {
        sensorDataList.put(sensorName, new SensorRecord(sensorValue, time));
    }

    @XmlElement
    public Map<String, SensorRecord> getSensorDataList() {
        return sensorDataList;
    }
}