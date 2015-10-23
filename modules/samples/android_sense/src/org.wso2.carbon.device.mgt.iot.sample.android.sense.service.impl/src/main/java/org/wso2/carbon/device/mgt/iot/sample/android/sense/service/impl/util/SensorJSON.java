package org.wso2.carbon.device.mgt.iot.sample.android.sense.service.impl.util;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement

@JsonIgnoreProperties(ignoreUnknown = true)
public class SensorJSON {

    @XmlElement public Long time;
    @XmlElement public String key;
    @XmlElement public String value;
}
