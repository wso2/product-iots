/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.coffeeking.connectedcup.plugin.impl.dao.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * Contains utility methods used by FireAlarm plugin.
 */
public class ConnectedCupUtils {

    private static Log log = LogFactory.getLog(ConnectedCupUtils.class);

    public static String getDeviceProperty(Map<String, String> deviceProperties, String property) {

        String deviceProperty = deviceProperties.get(property);

        if (deviceProperty == null) {
            return "";
        }

        return deviceProperty;
    }


}
