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

package org.deviceautomation.geolocationTracker.plugin.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deviceautomation.geolocationTracker.plugin.exception.DeviceMgtPluginException;
import org.deviceautomation.geolocationTracker.plugin.impl.GeoLocationTrackerManagerService;
import org.deviceautomation.geolocationTracker.plugin.impl.util.GeoLocationTrackerUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.iot.devicetype.DeviceTypeConfigService;
import org.wso2.carbon.ndatasource.core.DataSourceService;

/**
 * @scr.component name="org.deviceautomation.geolocationTracker.plugin.internal.ServiceComponent"
 * immediate="true"
 * @scr.reference name="org.wso2.carbon.ndatasource"
 * interface="org.wso2.carbon.ndatasource.core.DataSourceService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDataSourceService"
 * unbind="unsetDataSourceService"
 * @scr.reference name="devicetype.configuration.service"
 * interface="org.wso2.carbon.device.mgt.iot.devicetype.DeviceTypeConfigService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDeviceTypeConfigService"
 * unbind="unsetDeviceTypeConfigService"
 */

public class ServiceComponent {
    private static final Log log = LogFactory.getLog(ServiceComponent.class);
    private ServiceRegistration serviceRegistration;

    protected void activate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("Activating b Management Service Component");
        }
        try {
            GeoLocationTrackerManagerService deviceTypeManagerService = new GeoLocationTrackerManagerService();
            BundleContext bundleContext = ctx.getBundleContext();
            serviceRegistration =
                    bundleContext.registerService(DeviceManagementService.class.getName(),
                            deviceTypeManagerService, null);
            String setupOption = System.getProperty("setup");
            if (setupOption != null) {
                if (log.isDebugEnabled()) {
                    log.debug("-Dsetup is enabled. Iot Device management repository schema initialization is about " +
                            "to begin");
                }
                try {
                    GeoLocationTrackerUtils.setupDeviceManagementSchema();
                } catch (DeviceMgtPluginException e) {
                    log.error("Exception occurred while initializing device management database schema", e);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("b Management Service Component has been successfully activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while activating Current Sensor Management Service Component", e);
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
                log.debug("Current Sensor Management Service Component has been successfully de-activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while de-activating Iot Device Management bundle", e);
        }
    }

    protected void setDataSourceService(DataSourceService dataSourceService) {
        /* This is to avoid mobile device management component getting initialized before the underlying datasources
        are registered */
        if (log.isDebugEnabled()) {
            log.debug("Data source service set to service component");
        }
    }

    protected void unsetDataSourceService(DataSourceService dataSourceService) {
        //do nothing
    }

    protected void setDeviceTypeConfigService(DeviceTypeConfigService deviceTypeConfigService) {
        GeoLocationTrackerManagementDataHolder.getInstance().setDeviceTypeConfigService(deviceTypeConfigService);
    }

    protected void unsetDeviceTypeConfigService(DeviceTypeConfigService deviceTypeConfigService) {
        GeoLocationTrackerManagementDataHolder.getInstance().setDeviceTypeConfigService(null);
    }
}
