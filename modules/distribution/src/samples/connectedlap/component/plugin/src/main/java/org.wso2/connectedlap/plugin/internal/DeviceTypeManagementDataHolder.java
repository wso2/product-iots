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

package org.wso2.connectedlap.plugin.internal;

import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;

/**
 * DataHolder class of fgfgt plugins component.
 */
public class DeviceTypeManagementDataHolder {

    private static DeviceTypeManagementDataHolder thisInstance = new DeviceTypeManagementDataHolder();
    private OutputEventAdapterService outputEventAdapterService;

    private DeviceTypeManagementDataHolder() {
    }

    public static DeviceTypeManagementDataHolder getInstance() {
        return thisInstance;
    }

    public OutputEventAdapterService getOutputEventAdapterService() {
        return outputEventAdapterService;
    }

    public void setOutputEventAdapterService(
            OutputEventAdapterService outputEventAdapterService) {
        this.outputEventAdapterService = outputEventAdapterService;
    }
}

