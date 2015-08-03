/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var deviceModule;
deviceModule = function () {
    var log = new Log("modules/device.js");

    var constants = require("/modules/constants.js");
    var utility = require("/modules/utility.js").utility;

    var carbon = require('carbon');
    var carbonHttpServletTransport = carbon.server.address('http');
    var carbonHttpsServletTransport = carbon.server.address('https');

    var ArrayList = Packages.java.util.ArrayList;
    var Properties = Packages.java.util.Properties;
    var DeviceIdentifier = Packages.org.wso2.carbon.device.mgt.common.DeviceIdentifier;
    var DeviceManagerUtil = Packages.org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;
    var SimpleOperation = Packages.org.wso2.carbon.device.mgt.core.operation.mgt.SimpleOperation;
    var ConfigOperation = Packages.org.wso2.carbon.device.mgt.core.operation.mgt.ConfigOperation;
    var CommandOperation = Packages.org.wso2.carbon.device.mgt.core.operation.mgt.CommandOperation;
    var deviceManagementDAOFactory = Packages.org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;

    var deviceManagementService = utility.getDeviceManagementService();

    var publicMethods = {};
    var privateMethods = {};

    privateMethods.validateAndReturn = function (value) {
        return (value == undefined || value == null) ? constants.UNSPECIFIED : value;
    };

    privateMethods.getStoreDefinition = function (deviceTypeStr) {
        var storeJSON = new File(constants.DEVICES_UNIT_PATH + deviceTypeStr + "/public/store.json");
        if (storeJSON.isExists()) {
            storeJSON.open('r');
            log.debug('reading file "' + storeJSON.getPath() + '"');
            var content = storeJSON.readAll().trim();
            storeJSON.close();
            return parse(content);
        }
        return null;
    };

    publicMethods.listDevices = function () {
        var devices = deviceManagementService.getAllDevices();
        var deviceList = [];
        var i, device, propertiesList, deviceObject;
        for (i = 0; i < devices.size(); i++) {
            device = devices.get(i);
            propertiesList = DeviceManagerUtil.convertDevicePropertiesToMap(device.getProperties());

            deviceObject = {};
            deviceObject[constants.DEVICE_IDENTIFIER] =
                privateMethods.validateAndReturn(device.getDeviceIdentifier());
            deviceObject[constants.DEVICE_NAME] =
                privateMethods.validateAndReturn(device.getName());
            deviceObject[constants.DEVICE_OWNERSHIP] =
                privateMethods.validateAndReturn(device.getOwnership());
            deviceObject[constants.DEVICE_OWNER] =
                privateMethods.validateAndReturn(device.getOwner());
            deviceObject[constants.DEVICE_TYPE] =
                privateMethods.validateAndReturn(device.getType());
            deviceObject[constants.DEVICE_PROPERTIES] = {};
            if (device.getType() == constants.PLATFORM_IOS) {
                deviceObject[constants.DEVICE_PROPERTIES][constants.DEVICE_MODEL] =
                    privateMethods.validateAndReturn(propertiesList.get(constants.DEVICE_PRODUCT));
                deviceObject[constants.DEVICE_PROPERTIES][constants.DEVICE_VENDOR] = constants.VENDOR_APPLE;
            } else {
                deviceObject[constants.DEVICE_PROPERTIES][constants.DEVICE_MODEL] =
                    privateMethods.validateAndReturn(propertiesList.get(constants.DEVICE_MODEL));
                deviceObject[constants.DEVICE_PROPERTIES][constants.DEVICE_VENDOR] =
                    privateMethods.validateAndReturn(propertiesList.get(constants.DEVICE_VENDOR));
            }
            deviceObject[constants.DEVICE_PROPERTIES][constants.DEVICE_OS_VERSION] =
                privateMethods.validateAndReturn(propertiesList.get(constants.DEVICE_OS_VERSION));

            deviceList.push(deviceObject);
        }
        return deviceList;
    };

    publicMethods.listDevicesForUser = function (username) {
        var devices = deviceManagementService.getDeviceListOfUser(username);
        var deviceList = [];
        var i, device, propertiesList, deviceObject;
        for (i = 0; i < devices.size(); i++) {
            device = devices.get(i);
            propertiesList = DeviceManagerUtil.convertDevicePropertiesToMap(device.getProperties());

            deviceObject = {};
            deviceObject[constants.DEVICE_IDENTIFIER] =
                privateMethods.validateAndReturn(device.getDeviceIdentifier());
            deviceObject[constants.DEVICE_NAME] =
                privateMethods.validateAndReturn(device.getName());
            deviceObject[constants.DEVICE_OWNERSHIP] =
                privateMethods.validateAndReturn(device.getOwnership());
            deviceObject[constants.DEVICE_OWNER] =
                privateMethods.validateAndReturn(device.getOwner());
            deviceObject[constants.DEVICE_TYPE] =
                privateMethods.validateAndReturn(device.getType());
            deviceObject[constants.DEVICE_PROPERTIES] = {};
            if (device.getType() == constants.PLATFORM_IOS) {
                deviceObject[constants.DEVICE_PROPERTIES][constants.DEVICE_MODEL] =
                    privateMethods.validateAndReturn(propertiesList.get(constants.DEVICE_PRODUCT));
                deviceObject[constants.DEVICE_PROPERTIES][constants.DEVICE_VENDOR] = constants.VENDOR_APPLE;
            } else {
                deviceObject[constants.DEVICE_PROPERTIES][constants.DEVICE_MODEL] =
                    privateMethods.validateAndReturn(propertiesList.get(constants.DEVICE_MODEL));
                deviceObject[constants.DEVICE_PROPERTIES][constants.DEVICE_VENDOR] =
                    privateMethods.validateAndReturn(propertiesList.get(constants.DEVICE_VENDOR));
            }
            deviceObject[constants.DEVICE_PROPERTIES][constants.DEVICE_OS_VERSION] =
                privateMethods.validateAndReturn(propertiesList.get(constants.DEVICE_OS_VERSION));

            deviceList.push(deviceObject);
        }
        return deviceList;
    };

    /**
     * Return list of device types.
     * Device types should be registered in CDMF and specific unit with name of '{devicetype}' should be presented.
     * @returns {Array}
     */
    publicMethods.listDeviceTypes = function () {
        var deviceTypes = deviceManagementDAOFactory.getDeviceTypeDAO().getDeviceTypes();
        var deviceTypesList = [];
        var i, deviceType, deviceTypeObject;
        for (i = 0; i < deviceTypes.size(); i++) {
            deviceType = deviceTypes.get(i);
            var deviceUnit = new File(constants.DEVICES_UNIT_PATH + deviceType.getName());
            if (deviceUnit.isExists()) {
                deviceTypeObject = {};
                deviceTypeObject["name"] = deviceType.getName();
                deviceTypeObject["id"] = deviceType.getId();
                var storeProperties = privateMethods.getStoreDefinition(deviceType.getName());
                if (storeProperties) {
                    deviceTypeObject["storeTitle"] = storeProperties.title;
                    deviceTypeObject["storeDescription"] = storeProperties.description;
                }
                deviceTypesList.push(deviceTypeObject);
            } else {
                log.warn("Device type `" + deviceType.getName() + "` is missing unit implementation at: " + constants.DEVICES_UNIT_PATH);
            }
        }
        return deviceTypesList;
    };

    publicMethods.removeDevice = function (deviceType, deviceId) {
        //URL: https://localhost:9443/{deviceType}/manager/device/remove/{deviceId}
        var deviceCloudService = carbonHttpsServletTransport + "/" + deviceType + "/manager",
            removeDeviceEndpoint = deviceCloudService + "/device/remove/" + deviceId;

        var data = {};
        //XMLHTTPRequest's GET
        //log.info(removeDeviceEndpoint);
        return del(removeDeviceEndpoint, data, "text");
    };

    publicMethods.updateDevice = function (deviceType, deviceId, device) {
        //URL: https://localhost:9443/{deviceType}/manager/device/update/{deviceId}
        var deviceCloudService = carbonHttpsServletTransport + "/" + deviceType + "/manager",
            updateDeviceEndpoint = deviceCloudService + "/device/update/" + deviceId;

        var data = {};
        //XMLHTTPRequest's POST
        //log.info(updateDeviceEndpoint+ "?name="+device.name);
        return post(updateDeviceEndpoint + "?name=" + encodeURIComponent(device.name), data, "text");
    };

    /*
     Get the supported features by the device type
     */
    publicMethods.getFeatures = function (deviceType) {
        var features = deviceManagementService.getFeatureManager(deviceType).getFeatures();
        var featuresConverted = {};

        if (features) {
            var i, feature, featureObject;
            for (i = 0; i < features.size(); i++) {
                feature = features.get(i);
                featureObject = {};
                featureObject[constants.FEATURE_NAME] = feature.getName();
                featureObject[constants.FEATURE_DESCRIPTION] = feature.getDescription();
                featuresConverted[feature.getName()] = featureObject;
            }
        }
        return featuresConverted;
    };

    publicMethods.performOperation = function (devices, operation) {
        var operationInstance;
        if (operation.type == "COMMAND") {
            operationInstance = new CommandOperation();
        } else if (operation.type == "CONFIG") {
            operationInstance = new ConfigOperation();
        } else {
            operationInstance = new SimpleOperation();
        }
        operationInstance.setCode(operation.featureName);
        var props = new Properties();
        var i, object;
        for (i = 0; i < operation.properties.length; i++) {
            object = properties[i];
            props.setProperty(object.key, object.value);
        }
        operationInstance.setProperties(props);
        var deviceList = new ArrayList();
        var j, device, deviceIdentifier;
        for (j = 0; j < devices.length; i++) {
            device = devices[j];
            deviceIdentifier = new DeviceIdentifier();
            deviceIdentifier.setId(device.id);
            deviceIdentifier.setType(device.type);
            deviceList.add(deviceIdentifier);
        }
        deviceManagementService.addOperation(operationInstance, deviceList);
    };

    publicMethods.getDevice = function (type, deviceId) {
        var deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setType(type);
        deviceIdentifier.setId(deviceId);
        return deviceManagementService.getDevice(deviceIdentifier);
    };

    publicMethods.viewDevice = function (type, deviceId) {
        var device = publicMethods.getDevice(type, deviceId);
        if (device) {
            var propertiesList = DeviceManagerUtil.convertDevicePropertiesToMap(device.getProperties());
            var entries = propertiesList.entrySet();
            var iterator = entries.iterator();
            var properties = {};
            var entry, key, value;

            while (iterator.hasNext()) {
                entry = iterator.next();
                key = entry.getKey();
                value = entry.getValue();
                properties[key] = privateMethods.validateAndReturn(value);
            }

            var deviceObject = {};
            deviceObject[constants.DEVICE_IDENTIFIER] = device.getDeviceIdentifier();
            deviceObject[constants.DEVICE_NAME] = privateMethods.validateAndReturn(device.getName());
            deviceObject[constants.DEVICE_OWNERSHIP] = privateMethods.validateAndReturn(device.getEnrolmentInfo().getOwnership());
            deviceObject[constants.DEVICE_OWNER] = device.getEnrolmentInfo().getOwner();
            deviceObject[constants.DEVICE_TYPE] = device.getType();
            if (device.getType() == constants.PLATFORM_IOS) {
                properties[constants.DEVICE_MODEL] = properties[constants.DEVICE_PRODUCT];
                delete properties[constants.DEVICE_PRODUCT];
                properties[constants.DEVICE_VENDOR] = constants.VENDOR_APPLE;
            }
            deviceObject[constants.DEVICE_PROPERTIES] = properties;
            deviceObject[constants.DEVICE_ENROLLMENT] = device.getEnrolmentInfo().getDateOfEnrolment();
            return deviceObject;
        }
    };

    return publicMethods;

}();
