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

package org.homeautomation.droneanalyzer.plugin.impl.util;

import org.homeautomation.droneanalyzer.plugin.impl.util.DroneAnalyzerUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.ServerStartupObserver;

import java.io.IOException;

/**
 * When device type related plugin is depleting it will initiate mqtt connection with mqtt broker
 */
public class DroneAnalyzerStartupListener implements ServerStartupObserver {
    private static final Log log = LogFactory.getLog(org.homeautomation.droneanalyzer.plugin.impl.util.DroneAnalyzerStartupListener.class);

    @Override
    public void completingServerStartup() {
    }

    @Override
    public void completedServerStartup() {
        try {
            DroneAnalyzerUtils.setupMqttOutputAdapter();
        } catch (IOException e) {
            log.error("Failed to initialize the  drone output adapter", e);
        }
    }
}
