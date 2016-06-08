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

package org.wso2.agent.resourcemonitor.infocollector;

import javax.management.OperationsException;

import org.wso2.agent.resourcemonitor.pojo.SystemSpec;
import org.wso2.agent.resourcemonitor.pojo.SystemUsage;
import org.wso2.agent.resourcemonitor.systembridge.BashRunner;

public class Collector {
	private String OS = "LINUX";

	public Collector() throws Exception {
		String os = System.getProperty("os.name");
		if (os != null && !os.isEmpty()) {
			this.OS = os.toUpperCase();
			//System.out.println(this.OS);
		} else {
			throw new OperationsException("Operating System Unidentified");
		}
	}

	public String getOS() {
		return OS;
	}

	public void setOS(String oS) {
		OS = oS;
	}

	public SystemSpec getSysInfo(String suPassword) throws Exception {
		SystemSpec spec = new SystemSpec();
		BashRunner bashRunner = new BashRunner();
		String macaddress = bashRunner.runScript(this.OS, 0,suPassword);
		if (macaddress != null && !macaddress.trim().isEmpty()){
			spec.setMacaddress(macaddress);
		}else{
			throw new OperationsException("Mac address not found, Please check your Network Connection.");
		}
		spec.setProcessor_speed(bashRunner.runScript(this.OS, 1,suPassword));
		spec.setProcessor_cores(Integer.valueOf(bashRunner.runScript(this.OS, 2,suPassword)));
		spec.setRamsize(Float.valueOf(bashRunner.runScript(this.OS, 3,suPassword)));
		spec.setStorage(Float.valueOf(bashRunner.runScript(this.OS, 4,suPassword)));
		// spec.setDisplay_size(Float.valueOf(bashRunner.runScript(this.OS,5)));

		return spec;
	}

	public SystemUsage getSysUsage(String suPassword) throws NumberFormatException, Exception {
		SystemUsage systemUsage = new SystemUsage();
		BashRunner bashRunner = new BashRunner();
		systemUsage.setMacaddress(bashRunner.runScript(this.OS, 0,suPassword));
		systemUsage.setProcessor_usage(Float.valueOf(bashRunner.runScript(this.OS, 10,suPassword)));
		systemUsage.setMemory_usage(Float.valueOf(bashRunner.runScript(this.OS, 11,suPassword)));
		systemUsage.setStorage_usage(Float.valueOf(bashRunner.runScript(this.OS, 12,suPassword)));
		systemUsage.setBattery_percentage(Float.valueOf(bashRunner.runScript(this.OS, 13,suPassword)));
		systemUsage.setBattery_pluggedin(Byte.valueOf(bashRunner.runScript(this.OS, 14,suPassword)));

		return systemUsage;
	}

}
