package org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.service.impl.dao;

import javax.xml.bind.annotation.XmlElement;

public class SensorRecord {
	private String sensorValue;
	private long time;

	public SensorRecord(String sensorValue, long time) {
		this.sensorValue = sensorValue;
		this.time = time;
	}

	@XmlElement
	public String getSensorValue() {
		return sensorValue;
	}

	@XmlElement
	public long getTime() {
		return time;
	}

}