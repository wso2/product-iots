package ${groupId}.${rootArtifactId}.api.dto;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@XmlRootElement
/**
 * This stores sensor event data for android sense.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SensorRecord {

    @XmlElementWrapper(required = true, name = "values")
    private Map<String, Object> values;

    /** The id. */
    @XmlElement(required = false, name = "id")
    private String id;

    /**
     * Gets the values.
     * @return the values
     */
    public Map<String, Object> getValues() {
        return values;
    }

    /**
     * Sets the values.
     * @param values the values
     */
    public void setValues(Map<String, Object> values) {
        this.values = values;
    }

    /**
     * Sets the id.
     * @param id the new id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the id.
     * @return the id
     */
    public String getId() {
        return id;
    }

    @Override
    public String toString(){
        List<String> valueList = new ArrayList<String>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            valueList.add(entry.getKey() + ":" + entry.getValue());
        }
        return valueList.toString();

    }

}
