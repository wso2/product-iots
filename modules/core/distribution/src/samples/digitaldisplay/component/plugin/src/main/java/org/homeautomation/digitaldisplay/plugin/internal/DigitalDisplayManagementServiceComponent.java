/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.homeautomation.digitaldisplay.plugin.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.homeautomation.digitaldisplay.plugin.exception.DigitalDisplayDeviceMgtPluginException;
import org.homeautomation.digitaldisplay.plugin.impl.DigitalDisplayManagerService;
import org.homeautomation.digitaldisplay.plugin.impl.util.DigitalDisplayUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
/**
 * @scr.component name="org.homeautomation.digitaldisplay.plugin.internal.DigitalDisplayManagementServiceComponent"
 * immediate="true"
 */
public class DigitalDisplayManagementServiceComponent {

    private ServiceRegistration digitalDisplayServiceRegRef;
    private static final Log log = LogFactory.getLog(DigitalDisplayManagementServiceComponent.class);

    protected void activate(ComponentContext ctx) {
    	if (log.isDebugEnabled()) {
            log.debug("Activating Digital Display Management Service Component");
        }
        try {
            BundleContext bundleContext = ctx.getBundleContext();
            digitalDisplayServiceRegRef =
                    bundleContext.registerService(DeviceManagementService.class.getName(), new
                            DigitalDisplayManagerService(), null);
            String setupOption = System.getProperty("setup");
            if (setupOption != null) {
                if (log.isDebugEnabled()) {
                    log.debug("-Dsetup is enabled. Iot Device management repository schema initialization is about " +
                                    "to begin");
                }
                try {
                    DigitalDisplayUtils.setupDeviceManagementSchema();
                } catch (DigitalDisplayDeviceMgtPluginException e) {
                    log.error("Exception occurred while initializing device management database schema", e);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Digital Display  Management Service Component has been successfully activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while activating Digital Display Management Service Component", e);
        }
    }

    protected void deactivate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("De-activating DigitalDisplay Management Service Component");
        }
        try {
            if (digitalDisplayServiceRegRef != null) {
                digitalDisplayServiceRegRef.unregister();
            }
            if (log.isDebugEnabled()) {
                log.debug("DigitalDisplay Management Service Component has been successfully de-activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while de-activating Iot Device Management bundle", e);
        }
    }

}
