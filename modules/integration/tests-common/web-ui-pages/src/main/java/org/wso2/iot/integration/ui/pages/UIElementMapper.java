package org.wso2.iot.integration.ui.pages;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Read the mapper file and load the UI elements into Properties object.
 */
public class UIElementMapper {

    public static final Properties uiPropertiies = new Properties();
    private static final Log log = LogFactory.getLog(UIElementMapper.class);
    private static UIElementMapper instance;

    private UIElementMapper(){

    }

    public static synchronized UIElementMapper getInstance() throws IOException {
        if (instance == null) {
            setStream();
            instance = new UIElementMapper();
        }
        return instance;
    }

    public static Properties setStream() throws IOException {

        InputStream inputStream = UIElementMapper.class.getResourceAsStream("/mapper.properties");

        if (inputStream.available() > 0) {
            uiPropertiies.load(inputStream);
            inputStream.close();
            return uiPropertiies;
        }
        return null;
    }

    public String getElement (String key) {
        if (uiPropertiies != null) {
            return uiPropertiies.getProperty(key);
        }
        return null;
    }


}
