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

package org.homeautomation.firealarm.plugin.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.homeautomation.firealarm.plugin.impl.DeviceTypeManagerService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;

/**
 * @scr.component name="org.homeautomation.firealarm.plugin.internal.ServiceComponent"
 * immediate="true"
 */

public class ServiceComponent {

    private static final Log log = LogFactory.getLog(ServiceComponent.class);
    private ServiceRegistration serviceRegistration;

    protected void activate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("Activating firealarm Management Service Component");
        }
        try {
            BundleContext bundleContext = ctx.getBundleContext();
            serviceRegistration =
                    bundleContext.registerService(DeviceManagementService.class.getName(), new
                            DeviceTypeManagerService(), null);
            if (log.isDebugEnabled()) {
                log.debug("firealarm Management Service Component has been successfully activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while activating Fire Alarm Management Service Component", e);
        }
    }

    protected void deactivate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("De-activating firealarm Management Service Component");
        }
        try {
            if (serviceRegistration != null) {
                serviceRegistration.unregister();
            }
            if (log.isDebugEnabled()) {
                log.debug("Fire Alarm Management Service Component has been successfully de-activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while de-activating Iot Device Management bundle", e);
        }
    }

}
