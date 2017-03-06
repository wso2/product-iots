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

package org.wso2.agent.resourcemonitor.pojo;

public class SystemUsage {
	private String macaddress;
	private float processor_usage;
	private float memory_usage;
	private float storage_usage;
	private float battery_percentage;
	private byte battery_pluggedin;
	private float network_traffic;
	
	public float getProcessor_usage() {
		return processor_usage;
	}
	public void setProcessor_usage(float processor_usage) {
		this.processor_usage = processor_usage;
	}
	public float getMemory_usage() {
		return memory_usage;
	}
	public void setMemory_usage(float memory_usage) {
		this.memory_usage = memory_usage;
	}
	public float getStorage_usage() {
		return storage_usage;
	}
	public void setStorage_usage(float storage_usage) {
		this.storage_usage = storage_usage;
	}
	public float getBattery_percentage() {
		return battery_percentage;
	}
	public void setBattery_percentage(float battery_percentage) {
		this.battery_percentage = battery_percentage;
	}
	public byte getBattery_pluggedin() {
		return battery_pluggedin;
	}
	public void setBattery_pluggedin(byte battery_pluggedin) {
		this.battery_pluggedin = battery_pluggedin;
	}
	public float getNetwork_traffic() {
		return network_traffic;
	}
	public void setNetwork_traffic(float network_traffic) {
		this.network_traffic = network_traffic;
	}
	
	public String getMacaddress() {
		return macaddress;
	}
	public void setMacaddress(String macaddress) {
		this.macaddress = macaddress;
	}
	
	@Override
    public String toString() {
        return this.macaddress+","
        		+this.processor_usage+ ","
        		+this.memory_usage+ ","
        		+this.storage_usage+ ","
        		+this.battery_percentage+ ","
        		+this.battery_pluggedin+ ","
        		+this.network_traffic;
    }
	
	public SystemUsage(){};
	
	public SystemUsage(String csvString){
		String [] details = csvString.split(",");
		this.macaddress=details[0].trim();
		this.processor_usage=Float.valueOf(details[1].trim());
		this.memory_usage=Float.valueOf(details[2].trim());
		this.storage_usage=Float.valueOf(details[3].trim());
		this.battery_percentage=Float.valueOf(details[4].trim());
		this.battery_pluggedin=Byte.valueOf(details[5].trim());
		this.network_traffic=Float.valueOf(details[6].trim());
			
	}
	

}
