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

package org.wso2.connectedlap.plugin.impl.util;

import java.lang.Float;
import java.lang.String;

public class ConnectedLapDevice{

    /**
     * custom properties
     * */
    private String id;
    private Float discSpace;
    private Float memory;
    private String cpuInfo;
    private String networkType;
    private int cpuCores;

    public ConnectedLapDevice () {}

    public ConnectedLapDevice(String id ,Float discSpace, Float memory, String LapInfo, String networkType, int cpuCores) {
        this.id = id;
        this.discSpace = discSpace;
        this.memory = memory;
        this.cpuInfo = LapInfo;
        this.networkType = networkType;
        this.cpuCores = cpuCores;
    }

    public String getId() { return id;  }

    public void setId(String id) { this.id = id; }

    public Float getMemory() {
        return memory;
    }

    public void setMemory(Float memory) {
        this.memory = memory;
    }

    public String getCpuInfo() {
        return cpuInfo;
    }

    public void setCpuInfo(String cpuInfo) {
        this.cpuInfo = cpuInfo;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public Float getDiscSpace() {
        return discSpace;
    }

    public void setDiscSpace(Float discSpace) {
        this.discSpace = discSpace;
    }

    public int getCpuCores() {
       return cpuCores;
    }

    public void setCpuCores(int cores) {
        this.cpuCores = cpuCores;
    }

    //TODO: implement this method to show them in excepition msges
    /*public String  toString() {
        return
    } */


}
