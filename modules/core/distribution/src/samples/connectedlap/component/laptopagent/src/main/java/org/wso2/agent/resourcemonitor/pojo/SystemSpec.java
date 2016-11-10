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

public class SystemSpec {
	private String macaddress;
	private String processor_speed;// GHz
	private int processor_cores;// int
	private float ramsize;// GB
	private float storage;// GB
	private float display_size;//inches
	private String networktype;//description

	public String getProcessor_speed() {
		return processor_speed;
	}

	public void setProcessor_speed(String processor_speed) {
		this.processor_speed = processor_speed;
	}

	public int getProcessor_cores() {
		return processor_cores;
	}

	public void setProcessor_cores(int processor_cores) {
		this.processor_cores = processor_cores;
	}

	public float getRamsize() {
		return ramsize;
	}

	public void setRamsize(float ramsize) {
		this.ramsize = ramsize;
	}

	public float getStorage() {
		return storage;
	}

	public void setStorage(float storage) {
		this.storage = storage;
	}

	public String getNetworktype() {
		return networktype;
	}

	public void setNetworktype(String networktype) {
		this.networktype = networktype;
	}

	public float getDisplay_size() {
		return display_size;
	}

	public void setDisplay_size(float display_size) {
		this.display_size = display_size;
	}

	public String getMacaddress() {
		return macaddress;
	}

	public void setMacaddress(String macaddress) {
		macaddress = macaddress.replaceAll("\\:", "");
        this.macaddress = macaddress;
	}

	

}
