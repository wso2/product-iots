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


function onRequest(context) {
    var log = new Log('detail.js');
    var deviceType = context.uriParams.deviceType;
    var deviceId = request.getParameter('id');

    var devicemgtProps = require('/app/conf/devicemgt-props.js').config();

    if (deviceType && deviceId) {
        var deviceModule = require('/app/modules/device.js').deviceModule;
        var device = deviceModule.viewDevice(deviceType, deviceId);

        if (device && device.status != 'error') {
            var constants = require('/app/modules/constants.js');
            var tokenPair = session.get(constants.ACCESS_TOKEN_PAIR_IDENTIFIER);
            var token = '';
            if (tokenPair) {
                token = tokenPair.accessToken;
            }
            device.accessToken = token;
            device.ip = devicemgtProps['httpsWebURL'];
            return {'device': device.content};
        }
    }
}
