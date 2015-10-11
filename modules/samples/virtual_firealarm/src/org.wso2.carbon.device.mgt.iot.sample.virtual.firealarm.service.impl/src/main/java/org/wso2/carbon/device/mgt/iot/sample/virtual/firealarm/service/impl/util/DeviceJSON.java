package org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.service.impl.util;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceJSON {
    @XmlElement(required = true) public String owner;
    @XmlElement(required = true) public String deviceId;
    @XmlElement(required = true) public String reply;
    @XmlElement public Long time;
    @XmlElement public String key;
    @XmlElement public float value;
}
