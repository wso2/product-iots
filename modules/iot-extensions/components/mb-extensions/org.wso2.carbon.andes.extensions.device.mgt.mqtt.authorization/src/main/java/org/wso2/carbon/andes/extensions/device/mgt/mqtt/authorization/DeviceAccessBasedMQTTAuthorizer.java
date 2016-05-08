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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.andes.extensions.device.mgt.mqtt.authorization;

import org.apache.log4j.Logger;
import org.dna.mqtt.moquette.server.IAuthorizer;
import org.wso2.andes.configuration.enums.MQTTAuthoriztionPermissionLevel;
import org.wso2.andes.mqtt.MQTTAuthorizationSubject;
import org.wso2.carbon.andes.extensions.device.mgt.mqtt.authorization.internal.AuthorizationDataHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;

/**
 * Authorize the connecting users against Carbon Permission Model. Intended usage is
 * via providing fully qualified class name in broker.xml
 * <p/>
 * This is just a simple authorization model. For dynamic topics use an implementation based on IAuthorizer
 */
public class DeviceAccessBasedMQTTAuthorizer implements IAuthorizer {
    private static final Logger logger = Logger.getLogger(DeviceAccessBasedMQTTAuthorizer.class);
    /**
     * {@inheritDoc} Authorize the user against carbon device mgt model.
     */
    @Override
    public boolean isAuthorizedForTopic(MQTTAuthorizationSubject authorizationSubject, String topic,
                                        MQTTAuthoriztionPermissionLevel permissionLevel) {
        try {
            String topics[] = topic.split("/");
            if (topics.length < 3) {
                return false;
            }
            String tenantIdFromTopic = topics[0];
            if (!tenantIdFromTopic.equals(authorizationSubject.getTenantDomain())) {
                return false;
            }
            String deviceTypeFromTopic = topics[1];
            String deviceIdFromTopic = topics[2];
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    authorizationSubject.getTenantDomain(), true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(authorizationSubject.getUsername());
            return AuthorizationDataHolder.getInstance().getDeviceAccessAuthorizationService().isUserAuthorized(
                    new DeviceIdentifier(deviceIdFromTopic, deviceTypeFromTopic));
        } catch (DeviceAccessAuthorizationException e) {
            logger.error("Failed on Device Access Authorization for user " + authorizationSubject.getUsername(), e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return false;
    }

    /**
     * {@inheritDoc} Authorized the user against carbon device mgt model.
     */
    @Override
    public boolean isAuthorizedToConnect(MQTTAuthorizationSubject authorizationSubject) {
        return true;
    }
}