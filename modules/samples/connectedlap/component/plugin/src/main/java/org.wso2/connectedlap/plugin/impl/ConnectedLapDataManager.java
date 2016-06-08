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

package org.wso2.connectedlap.plugin.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.connectedlap.plugin.exception.DeviceMgtPluginException;
import org.wso2.connectedlap.plugin.impl.dao.DeviceTypeDAO;
import org.wso2.connectedlap.plugin.impl.util.ConnectedLapDevice;


public class ConnectedLapDataManager {

    private static final Log log = LogFactory.getLog(ConnectedLapDataManager.class);
    private static final DeviceTypeDAO deviceTypeDAO = new DeviceTypeDAO();

    public boolean addDeviceInfo (ConnectedLapDevice connectedLapDevice) throws DeviceManagementException {
        boolean status;

        try {
            if (log.isDebugEnabled()) {
                log.debug("Adding info to connectedlap device : " + connectedLapDevice.getId());
            }
            DeviceTypeDAO.beginTransaction();
            status = deviceTypeDAO.getDeviceTypeDAO().addDeviceMetaData(connectedLapDevice);
            DeviceTypeDAO.commitTransaction();
        } catch (DeviceMgtPluginException e) {
            try {
                DeviceTypeDAO.rollbackTransaction();
            } catch (DeviceMgtPluginException iotDAOEx) {
                log.warn("Error occurred while roll back the device enrol transaction :" + connectedLapDevice.toString(), iotDAOEx);
            }

            String msg = "Error while enrolling the connectedlap device : " + connectedLapDevice.getId();
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }

        return status;
    }

}
