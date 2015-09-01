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

package org.wso2.carbon.device.mgt.iot.sample.firealarm.plugin.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.iot.common.service.DeviceTypeService;
import org.wso2.carbon.device.mgt.iot.sample.firealarm.plugin.impl.FireAlarmManagerService;


/**
 * @scr.component name="org.wso2.carbon.device.mgt.iot.firealarm.internal.FirealarmManagementServiceComponent"
 * immediate="true"
 * @scr.reference name="wso2.carbon.device.mgt.iot.common.DeviceTypeService"
 * interface="org.wso2.carbon.device.mgt.iot.common.service.DeviceTypeService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDeviceTypeService"
 * unbind="unsetDeviceTypeService"
 */
public class FirealarmManagementServiceComponent {
	

    private ServiceRegistration firealarmServiceRegRef;



    private static final Log log = LogFactory.getLog(FirealarmManagementServiceComponent.class);
    protected void activate(ComponentContext ctx) {
    	if (log.isDebugEnabled()) {
            log.debug("Activating Firealarm Device Management Service Component");
        }
        try {
            BundleContext bundleContext = ctx.getBundleContext();


            firealarmServiceRegRef =
                    bundleContext.registerService(DeviceManagementService.class.getName(),
                                                 new FireAlarmManagerService(),
												  null);



            if (log.isDebugEnabled()) {
                log.debug("Firealarm Device Management Service Component has been successfully activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while activating Firealarm Device Management Service Component", e);
        }
    }

    protected void deactivate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("De-activating Firealarm Device Management Service Component");
        }
        try {
            if (firealarmServiceRegRef != null) {
                firealarmServiceRegRef.unregister();
            }

            if (log.isDebugEnabled()) {
                log.debug(
                        "Firealarm Device Management Service Component has been successfully de-activated");
            }
        } catch (Throwable e) {
            log.error("Error occurred while de-activating Firealarm Device Management bundle", e);
        }
    }

    protected void setDeviceTypeService(DeviceTypeService deviceTypeService) {
		/* This is to avoid this component getting initialized before the
		common registered */
        if (log.isDebugEnabled()) {
            log.debug("Data source service set to mobile service component");
        }
    }

    protected void unsetDeviceTypeService(DeviceTypeService deviceTypeService)  {
        //do nothing
    }


    
}
