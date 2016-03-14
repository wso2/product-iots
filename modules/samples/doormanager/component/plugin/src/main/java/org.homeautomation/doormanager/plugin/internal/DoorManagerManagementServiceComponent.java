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

package org.homeautomation.doormanager.plugin.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.homeautomation.doormanager.plugin.impl.DoorManagerService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;


/**
 * @scr.component name="org.homeautomation.doormanager.plugin.internal.DoorManagerManagementServiceComponent"
 * immediate="true"
 */
public class DoorManagerManagementServiceComponent {

    private static final Log log = LogFactory.getLog(DoorManagerManagementServiceComponent.class);
    private ServiceRegistration automaticDoorLocker;

    protected void activate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("Activating Door Opener Device Management Service Component");
        }
        try {
            BundleContext bundleContext = ctx.getBundleContext();
            automaticDoorLocker =
                    bundleContext.registerService(DeviceManagementService.class.getName(),
                            new DoorManagerService(), null);
            if (log.isDebugEnabled()) {
                log.debug("DoorOpener Device Management Service Component has been successfully activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while activating DoorOpener Device Management Service Component", e);
        }
    }

    protected void deactivate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("De-activating Door Opener Device Management Service Component");
        }
        try {
            if (automaticDoorLocker != null) {
                automaticDoorLocker.unregister();
            }
            if (log.isDebugEnabled()) {
                log.debug(
                        "Door Opener Device Management Service Component has been successfully de-activated");
            }
        } catch (Throwable e) {
            log.error(
                    "Error occurred while de-activating Door Locker Device Management bundle", e);
        }
    }

}
