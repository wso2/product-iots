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
