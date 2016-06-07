package org.wso2.agent.resourcemonitor;

import org.wso2.agent.resourcemonitor.infocollector.Collector;
import org.wso2.agent.resourcemonitor.pojo.SystemSpec;
import org.wso2.agent.resourcemonitor.pojo.SystemUsage;

public class Monitor {

	public SystemSpec getSysInfo(String suPassword) {
		SystemSpec spec=null;
		try {
			Collector collector = new Collector();
			spec=collector.getSysInfo(suPassword);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return spec;
	}

	public SystemUsage getSysUsage(String suPassword) {
		SystemUsage usage=null;
		try {
			Collector collector = new Collector();
			usage=collector.getSysUsage(suPassword);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return usage;
	}
}