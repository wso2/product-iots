/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.connectedlap.plugin.impl.feature;

import org.wso2.connectedlap.plugin.constants.DeviceTypeConstants;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.FeatureManager;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.GenericFeatureManager;

import java.util.List;

public class DeviceTypeFeatureManager implements FeatureManager {
	@Override
	public boolean addFeature(Feature feature) throws DeviceManagementException {
		return false;
	}

	@Override
	public boolean addFeatures(List<Feature> features) throws DeviceManagementException {
		return false;
	}

	@Override
	public Feature getFeature(String name) throws DeviceManagementException {
		GenericFeatureManager genericFeatureManager = GenericFeatureManager.getInstance();
		return genericFeatureManager.getFeature(DeviceTypeConstants.DEVICE_TYPE, name);
	}

	@Override
	public List<Feature> getFeatures() throws DeviceManagementException {
		GenericFeatureManager genericFeatureManager = GenericFeatureManager.getInstance();
		return genericFeatureManager.getFeatures(DeviceTypeConstants.DEVICE_TYPE);
	}

	@Override
	public boolean removeFeature(String name) throws DeviceManagementException {
		return false;
	}

	@Override
	public boolean addSupportedFeaturesToDB() throws DeviceManagementException {
		return false;
	}
}
