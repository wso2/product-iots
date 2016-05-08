/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.andes.extensions.device.mgt.mqtt.authorization.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="org.wso2.carbon.devicemgt.policy.manager" immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="org.wso2.carbon.device.access.authorization"
 * interface="org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDeviceAccessAuthorizationService"
 * unbind="unsetDeviceAccessAuthorizationService"
 */
@SuppressWarnings("unused")
public class AuthorizationServiceComponent {

    private static Log log = LogFactory.getLog(AuthorizationServiceComponent.class);

    protected void activate(ComponentContext componentContext) {
    }

    @SuppressWarnings("unused")
    protected void deactivate(ComponentContext componentContext) {
    }


    /**
     * Sets Realm Service
     *
     * @param realmService An instance of RealmService
     */
    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Realm Service");
        }
        AuthorizationDataHolder.getInstance().setRealmService(realmService);
    }

    /**
     * Unsets Realm Service
     *
     * @param realmService An instance of RealmService
     */
    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting Realm Service");
        }
        AuthorizationDataHolder.getInstance().setRealmService(null);
    }

    protected void setDeviceAccessAuthorizationService(DeviceAccessAuthorizationService deviceAccessAuthorizationService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Device Access Authorization Service");
        }
        AuthorizationDataHolder.getInstance().setDeviceAccessAuthorizationService(deviceAccessAuthorizationService);
    }

    protected void unsetDeviceAccessAuthorizationService(DeviceAccessAuthorizationService deviceAccessAuthorizationService) {
        if (log.isDebugEnabled()) {
            log.debug("Removing Device Access Authorization Service");
        }
        AuthorizationDataHolder.getInstance().setDeviceAccessAuthorizationService(null);
    }

}
