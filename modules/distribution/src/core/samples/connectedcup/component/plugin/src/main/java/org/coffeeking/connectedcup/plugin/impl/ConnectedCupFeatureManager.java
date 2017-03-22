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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.coffeeking.connectedcup.plugin.impl;

import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.FeatureManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This hold the feature manager implementation for Connected CUp.
 */
public class ConnectedCupFeatureManager implements FeatureManager{
    private static Feature feature = new Feature();
    private static final String METHOD = "method";
    private static final String URI = "uri";
    private static final String CONTENT_TYPE = "contentType";
    private static final String PATH_PARAMS = "pathParams";
    private static final String QUERY_PARAMS = "queryParams";
    private static final String FORM_PARAMS = "formParams";

    public ConnectedCupFeatureManager() {

        feature.setCode("ordercoffee");
        feature.setName("Order Coffee");
        feature.setDescription("Order coffee cup");

        Map<String, Object> apiParams = new HashMap<>();
        apiParams.put(METHOD, "POST");
        apiParams.put(URI, "/connectedcup/device/ordercoffee");
        List<String> pathParams = new ArrayList<>();
        List<String> queryParams = new ArrayList<>();
        List<String> formParams = new ArrayList<>();
        apiParams.put(PATH_PARAMS, pathParams);
        queryParams.add("deviceId");
        apiParams.put(QUERY_PARAMS, queryParams);
        apiParams.put(FORM_PARAMS, formParams);
        List<Feature.MetadataEntry> metadataEntries = new ArrayList<>();
        Feature.MetadataEntry metadataEntry = new Feature.MetadataEntry();
        metadataEntry.setId(-1);
        metadataEntry.setValue(apiParams);
        metadataEntries.add(metadataEntry);
        feature.setMetadataEntries(metadataEntries);
    }

    @Override
    public boolean addFeature(Feature feature) throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean addFeatures(List<Feature> list) throws DeviceManagementException {
        return false;
    }

    @Override
    public Feature getFeature(String code) throws DeviceManagementException {
        if (code.equals(feature.getCode())) {
           return  feature;
        }
        return null;
    }

    @Override
    public List<Feature> getFeatures() throws DeviceManagementException {
        List<Feature> features = new ArrayList<>();
        features.add(feature);
        return features;
    }

    @Override
    public boolean removeFeature(String s) throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean addSupportedFeaturesToDB() throws DeviceManagementException {
        return false;
    }
}
