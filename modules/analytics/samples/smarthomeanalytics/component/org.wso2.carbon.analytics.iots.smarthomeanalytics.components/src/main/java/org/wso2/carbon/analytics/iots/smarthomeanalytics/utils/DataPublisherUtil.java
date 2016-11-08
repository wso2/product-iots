/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.iot.smarthomeanalytics.utils;

import org.wso2.iot.smarthomeanalytics.constants.Constants;

import java.io.File;

public class DataPublisherUtil {

    static File filePath = new File(".." + File.separator + ".." + File.separator + "repository" + File.separator
            + "resources" + File.separator + "security");

    public static void setTrustStoreParams() {
        String trustStore = filePath.getAbsolutePath();
        System.setProperty("javax.net.ssl.trustStore", trustStore + "" + File.separator
                + Constants.CLIENT_TRUST_STORE_NAME);
        System.setProperty("javax.net.ssl.trustStorePassword", Constants.CLIENT_TRUST_STORE_PASSWORD);
    }

    public static String getAgentConfigPath() {
        return ".." + File.separator + ".." + File.separator + "repository" + File.separator
                + "conf" + File.separator + "data-bridge" + File.separator + Constants.DATA_BRIDGE_AGENT_CONFIG;
    }
}