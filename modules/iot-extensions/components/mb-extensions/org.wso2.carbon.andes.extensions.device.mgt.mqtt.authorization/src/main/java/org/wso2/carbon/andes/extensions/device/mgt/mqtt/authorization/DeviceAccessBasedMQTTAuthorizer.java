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
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.List;

/**
 * Authorize the connecting users against Carbon Permission Model. Intended usage is
 * via providing fully qualified class name in broker.xml
 * <p/>
 * This is just a simple authorization model. For dynamic topics use an implementation based on IAuthorizer
 */
public class DeviceAccessBasedMQTTAuthorizer implements IAuthorizer {
    private static final Logger logger = Logger.getLogger(DeviceAccessBasedMQTTAuthorizer.class);
    private static final String CONNECTION_PERMISSION = "/permission/admin/device-mgt/user";
    private static final String ADMIN_PERMISSION = "/permission/admin/device-mgt/admin";
    private static final String SCOPE_IDENTIFIER = "scope";
    private static final String CDMF_SCOPE_PREFIX = "cdmf";
    private static final String CDMF_SCOPE_SEPERATOR = "/";
    private static final String UI_EXECUTE = "ui.execute";

    /**
     * {@inheritDoc} Authorize the user against carbon device mgt model.
     */
    @Override
    public boolean isAuthorizedForTopic(MQTTAuthorizationSubject authorizationSubject, String topic,
                                        MQTTAuthoriztionPermissionLevel permissionLevel) {
        if (isUserAuthorized(authorizationSubject, ADMIN_PERMISSION, UI_EXECUTE)) {
            return true;
        }
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
        List<String> scopes = (List<String>) authorizationSubject.getProperties().get(SCOPE_IDENTIFIER);
        if (scopes != null) {
            for (String scope : scopes) {
                if (scope.startsWith(CDMF_SCOPE_PREFIX)) {
                    String deviceId[] = scope.split(CDMF_SCOPE_SEPERATOR);
                    if (deviceId.length == 3) {
                        if (deviceIdFromTopic.equals(deviceId[2]) && deviceTypeFromTopic.equals(deviceId[1])) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc} Authorized the user against carbon device mgt model.
     */
    @Override
    public boolean isAuthorizedToConnect(MQTTAuthorizationSubject authorizationSubject) {
        return isUserAuthorized(authorizationSubject, CONNECTION_PERMISSION, UI_EXECUTE);
    }

    /**
     * Check whether the client is authorized with the given permission and action.
     *
     * @param authorizationSubject this contains the client information
     * @param permission           Carbon permission that requires for the use
     * @param action               Carbon permission action that requires for the given permission.
     * @return boolean - true if user is authorized else return false.
     */
    private boolean isUserAuthorized(MQTTAuthorizationSubject authorizationSubject, String permission, String action) {
        String username = authorizationSubject.getUsername();
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    authorizationSubject.getTenantDomain(), true);
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            UserRealm userRealm = AuthorizationDataHolder.getInstance().getRealmService()
                    .getTenantUserRealm(tenantId);
            if (userRealm != null && userRealm.getAuthorizationManager() != null) {
                return userRealm.getAuthorizationManager().isUserAuthorized(username, permission, action);
            }
            return false;
        } catch (UserStoreException e) {
            String errorMsg = String.format("Unable to authorize the user : %s", username);
            logger.error(errorMsg, e);
            return false;
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
}