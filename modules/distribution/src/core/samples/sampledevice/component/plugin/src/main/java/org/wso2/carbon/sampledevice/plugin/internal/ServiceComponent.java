/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.sampledevice.plugin.internal;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.sampledevice.plugin.exception.DeviceMgtPluginException;
import org.wso2.carbon.sampledevice.plugin.impl.DeviceTypeManagerService;
import org.wso2.carbon.sampledevice.plugin.impl.util.DeviceTypeUtils;

/**
 * @scr.component name="org.wso2.carbon.sampledevice.plugin.internal.ServiceComponent"
 * immediate="true"
 */

public class ServiceComponent {
    private static final Log log = LogFactory.getLog(ServiceComponent.class);
    private ServiceRegistration serviceRegistration;

    protected void activate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("Activating b Management Service Component");
        }
        try {
            DeviceTypeManagerService deviceTypeManagerService = new DeviceTypeManagerService();
            BundleContext bundleContext = ctx.getBundleContext();
            serviceRegistration =
                    bundleContext.registerService(DeviceManagementService.class.getName(),
                            deviceTypeManagerService, null);
            String setupOption = System.getProperty("setup");
            if (setupOption != null) {
                if (log.isDebugEnabled()) {
                    log.debug("-Dsetup is enabled. Iot Device management repository schema " +
                            "initialization is about to begin");
                }
                try {
                    DeviceTypeUtils.setupDeviceManagementSchema();
                } catch (DeviceMgtPluginException e) {
                    log.error("Exception occurred while initializing device management database " +
                            "schema", e);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("b Management Service Component has been successfully activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while activating Current Sensor Management Service " +
                    "Component", e);
        }
    }

    protected void deactivate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("De-activating b Management Service Component");
        }
        try {
            if (serviceRegistration != null) {
                serviceRegistration.unregister();
            }
            if (log.isDebugEnabled()) {
                log.debug("Current Sensor Management Service Component has been successfully " +
                        "de-activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while de-activating Iot Device Management bundle", e);
        }
    }
}
