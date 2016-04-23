
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.homeautomation.droneanalyzer.api.util;

import org.apache.commons.logging.LogFactory;
import org.homeautomation.droneanalyzer.api.transport.DroneAnalyzerXMPPConnector;
import org.homeautomation.droneanalyzer.plugin.constants.DroneConstants;
import org.homeautomation.droneanalyzer.plugin.controller.DroneController;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.controlqueue.xmpp.XmppConfig;
import org.wso2.carbon.device.mgt.iot.transport.TransportHandlerException;

import java.io.File;

public class DroneAnalyzerServiceUtils {

    private static org.apache.commons.logging.Log log = LogFactory.getLog(DroneAnalyzerServiceUtils.class);

    public static void sendCommandViaXMPP(String deviceOwner, String deviceId, String resource,
                                          String state, DroneAnalyzerXMPPConnector droneXMPPConnector)
            throws DeviceManagementException, TransportHandlerException {

        String xmppServerDomain = XmppConfig.getInstance().getXmppEndpoint();
        int indexOfChar = xmppServerDomain.lastIndexOf(File.separator);
        if (indexOfChar != -1) {
            xmppServerDomain = xmppServerDomain.substring((indexOfChar + 1), xmppServerDomain.length());
        }
        indexOfChar = xmppServerDomain.indexOf(":");
        if (indexOfChar != -1) {
            xmppServerDomain = xmppServerDomain.substring(0, indexOfChar);
        }
        String clientToConnect = deviceId + "@" + xmppServerDomain + File.separator + deviceOwner;
        String message = resource.replace("/", "") + ":" + state;
        droneXMPPConnector.publishDeviceData(clientToConnect, message, "CONTROL-REQUEST");
    }

    public static boolean sendControlCommand(DroneController controller, String deviceId, String action,
                                             double speed, double duration)
            throws DeviceManagementException {
        boolean controlState = false;
        try{
            switch (action){
                case DroneConstants.TAKE_OFF:
                    controlState = controller.takeoff();
                    break;
                case DroneConstants.LAND:
                    controlState = controller.land();
                    break;
                case DroneConstants.BACK:
                    controlState = controller.back(speed, duration);
                    break;
                case DroneConstants.CLOCK_WISE:
                    controlState = controller.clockwise(speed, duration);
                    break;
                case DroneConstants.COUNTER_CLOCKWISE:
                    controlState = controller.conterClockwise(speed, duration);
                    break;
                case DroneConstants.DOWN:
                    controlState = controller.down(speed, duration);
                    break;
                case DroneConstants.FRONT:
                    controlState = controller.back(speed, duration);
                    break;
                case DroneConstants.FORWARD:
                    controlState = controller.clockwise(speed, duration);
                    break;
                case DroneConstants.UP:
                    controlState = controller.up(speed, duration);
                    break;
                default:
                    log.error("Invalid command");
                    break;
            }
        }catch(Exception e){
            log.error(e.getMessage()+ "\n"+ e);
        }
        return controlState;
    }
}
