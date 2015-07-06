var operationModule = function () {
    var module = {};

    module.getiOSServiceEndpoint = function(operationName) {
        var featureMap = {
            DEVICE_LOCK: "lock",
            ALARM: "alarm",
            LOCATION: "location",
            AIR_PLAY: "airplay",
            INSTALL_STORE_APPLICATION: "storeapplication",
            INSTALL_ENTERPRISE_APPLICATION: "enterpriseapplication",
            REMOVE_APPLICATION: "removeapplication",
            RESTRICTION: "restriction",
            CELLULAR: "cellular",
            ENTERPRISE_WIPE: "enterprisewipe",
            WIFI: "wifi"
        };
        return "/ios/operation/" + featureMap[operationName];
    }

    function createiOSPayload(operationName, operationData, devices) {
        // Command operations doesn't need a payload
        var payload;
        var operationType = "profile";
        if (operationName == "AIR_PLAY") {
            payload = {
                "operation": {
                    "airPlayDestinations": [
                        operationData.location
                    ],
                    "airPlayCredentials": [{
                        "deviceName": operationData.deviceName,
                        "password": operationData.password
                    }]
                }
            };
        }else if (operationName == "INSTALL_STORE_APPLICATION") {
            payload = {
                "operation": {
                    "identifier": operationData.appIdentifier,
                    "iTunesStoreID": operationData.ituneID,
                    "removeAppUponMDMProfileRemoval": operationData.appRemoval,
                    "preventBackupOfAppData": operationData.backupData,
                    "bundleId": operationData.bundleId
                }
            };
        } else if (operationName == "INSTALL_ENTERPRISE_APPLICATION") {
            payload = {
                "operation": {
                    "identifier": operationData.appIdentifier,
                    "manifestURL": operationData.manifestURL,
                    "removeAppUponMDMProfileRemoval": operationData.appRemoval,
                    "preventBackupOfAppData": operationData.backupData,
                    "bundleId": operationData.bundleId
                }
            };
        } else if (operationName == "REMOVE_APPLICATION"){
            payload = {
                "operation": {
                    "bundleId": operationData.bundleId
                }
            };
        } else if (operationName == "RESTRICTION"){
            payload = {
                "operation": {
                    "allowCamera": operationData.allowCamera,
                    "allowCloudBackup": operationData.allowCloudBackup,
                    "allowSafari": operationData.allowSafari,
                    "allowScreenShot": operationData.allowScreenshot,
                    "allowAirDrop": operationData.allowAirDrop
                }
            };
        }  else if (operationName == "CELLULAR"){
            payload = {
                "operation": {
                    "attachAPNName": null,
                    "authenticationType": null,
                    "username": null,
                    "password": null,
                    "apnConfigurations": [
                        {
                            "configurationName": null,
                            "authenticationType": null,
                            "username": null,
                            "password": null,
                            "proxyServer": null,
                            "proxyPort": 0
                        }
                    ]
                }
            };
        } else if (operationName == "WIFI"){
            payload = {
                "operation": {
                    "hiddenNetwork": operationData.hiddenNetwork,
                    "autoJoin": operationData.autoJoin,
                    "encryptionType": operationData.encryptionType,
                    "hotspot": false,
                    "domainName": null,
                    "serviceProviderRoamingEnabled": false,
                    "displayedOperatorName": null,
                    "proxyType": null,
                    "roamingConsortiumOIs": null,
                    "password": operationData.password,
                    "clientConfiguration": {
                        "username": null,
                        "acceptEAPTypes": null,
                        "userPassword": null,
                        "oneTimePassword": false,
                        "payloadCertificateAnchorUUID": null,
                        "outerIdentity": null,
                        "tlstrustedServerNames": null,
                        "tlsallowTrustExceptions": false,
                        "tlscertificateIsRequired": false,
                        "ttlsinnerAuthentication": null,
                        "eapfastusePAC": false,
                        "eapfastprovisionPAC": false,
                        "eapfastprovisionPACAnonymously": false,
                        "eapsimnumberOfRANDs": 0
                    },
                    "payloadCertificateUUID": null,
                    "proxyServer": null,
                    "proxyPort": 0,
                    "proxyUsername": null,
                    "proxyPassword": null,
                    "proxyPACURL": null,
                    "proxyPACFallbackAllowed": false,
                    "ssid": operationData.ssid,
                    "nairealmNames": null,
                    "mccandMNCs": null
                }
            };
        } else if (operationName == "MAIL"){
            payload = {
                "operation": {
                    "attachAPNName": null,
                    "authenticationType": null,
                    "username": null,
                    "password": null,
                    "apnConfigurations": [
                        {
                            "configurationName": null,
                            "authenticationType": null,
                            "username": null,
                            "password": null,
                            "proxyServer": null,
                            "proxyPort": 0
                        }
                    ]
                }
            };
        } else {
            // The payload of command operations are set as device ids
            payload = devices;
            operationType = "command";
        }
        if (operationType == "profile" && devices) {
            payload.deviceIDs = devices;
        }
        return payload;
    }

    function createAndroidPayload(operationName, operationData, devices) {
        var payload;
        var operationType = "profile";
        if (operationName == "CAMERA") {
            payload = {
                "operation": {
                    "enabled" : operationData.enableCamera
                }
            };
        } else if (operationName == "CHANGE_LOCK_CODE") {
            payload = {
                "operation": {
                    "lockCode" : operationData.lockCode
                }
            };
        } else if (operationName == "ENCRYPT_STORAGE") {
            payload = {
                "operation": {
                    "encrypted" : operationData.enableEncryption
                }
            };
        } else if (operationName == "NOTIFICATION"){
            payload = {
                "deviceIDs": devices,
                "operation": {
                    "message" : operationData.message
                }
            };
        } else if (operationName == "WEBCLIP"){
            payload = {
                "operation": {
                    "identity": operationData.url,
                    "title": operationData.title

                }
            };
        } else if (operationName == "INSTALL_APPLICATION"){
            payload = {
                "operation": {
                    "appIdentifier": operationData.packageName,
                    "type": operationData.type,
                    "url": operationData.url
                }
            };
        } else if (operationName == "UNINSTALL_APPLICATION"){
            payload = {
                "operation": {
                    "appIdentifier": operationData.packageName
                }
            };
        } else if (operationName == "BLACKLIST_APPLICATIONS"){
            payload = {
                "operation": {
                    "appIdentifier": operationData.packageNames
                }
            };
        } else if (operationName == "PASSCODE_POLICY"){
            payload = {
                "operation": {
                    "maxFailedAttempts": operationData.maxFailedAttempts,
                    "minLength": operationData.minLength,
                    "pinHistory": operationData.pinHistory,
                    "minComplexChars": operationData.minComplexChars,
                    "maxPINAgeInDays": operationData.maxPINAgeInDays,
                    "requireAlphanumeric": operationData.requireAlphanumeric,
                    "allowSimple": operationData.allowSimple

                }
            };
        } else if (operationName == "WIFI"){
            payload = {
                "operation": {
                    "ssid": operationData.ssid,
                    "password": operationData.password

                }
            };
        } else {
            operationType = "command";
            payload = devices;
        }
        if (operationType == "profile" && devices) {
            payload.deviceIDs = devices;
        }
        return payload;
    }
    module.getAndroidServiceEndpoint = function(operationName) {
        var featureMap = {
            DEVICE_LOCK: "lock",
            DEVICE_LOCATION: "location",
            CLEAR_PASSWORD: "clear-password",
            CAMERA: "camera",
            ENTERPRISE_WIPE: "enterprise-wipe",
            WIPE_DATA: "wipe-data",
            APPLICATION_LIST: "get-application-list",
            DEVICE_RING: "ring-device",
            DEVICE_MUTE: "mute",
            NOTIFICATION: "notification",
            WIFI: "wifi",
            ENCRYPT_STORAGE: "encrypt",
            CHANGE_LOCK_CODE: "change-lock-code",
            WEBCLIP: "webclip",
            INSTALL_APPLICATION: "install-application",
            UNINSTALL_APPLICATION: "uninstall-application",
            BLACKLIST_APPLICATIONS: "blacklist-applications",
            PASSCODE_POLICY: "password-policy"
        };
        return "/mdm-android-agent/operation/" + featureMap[operationName];
    };
    /**
     * Get the icon for the featureCode
     * @param featureCode
     * @returns icon class
     */
    module.getAndroidIconForFeature = function(featureCode){
        var featureMap = {
            DEVICE_LOCK: "fw-lock",
            DEVICE_LOCATION: "fw-map-location",
            CLEAR_PASSWORD: "fw-key",
            ENTERPRISE_WIPE: "fw-clean",
            WIPE_DATA: "fw-database",
            DEVICE_RING: "fw-dial-up",
            DEVICE_MUTE: "fw-incoming-call",
            NOTIFICATION: "fw-message",
            CHANGE_LOCK_CODE: "fw-padlock"
        };
        return featureMap[featureCode];
    };
    /**
     * Get the icon for the featureCode
     * @param featureCode
     * @returns icon class
     */
    module.getiOSIconForFeature = function(featureCode){
        var featureMap = {
            DEVICE_LOCK: "fw-lock",
            LOCATION: "fw-map-location",
            ENTERPRISE_WIPE: "fw-clean",
            ALARM: "fw-dial-up"
        };
        return featureMap[featureCode];
    };
    /**
     * Filter a list by a data attribute
     * @param prop
     * @param val
     * @returns {Array.<T>|*|array|enumeration}
     */
    $.fn.filterByData = function(prop, val) {
        return this.filter(
            function() { return $(this).data(prop)==val; }
        );
    };

    /*
     @DeviceType = Device Type of the profile
     @operationCode = Feature Codes to generate the profile from
     @DeviceList = Optional device list to include in payload body for operations
     */
    module.generatePayload = function(deviceType, operationCode, deviceList){
        var payload;
        var operationData = {};
        $(".operation-data").filterByData("operation", operationCode).find(".operationDataKeys").each(
            function(index){
                var operationDataObj = $(this);
                var key = operationDataObj.data("key");
                var value = operationDataObj.val();
                if (operationDataObj.is(':checkbox')){
                    value = operationDataObj.is(":checked");
                }else if (operationDataObj.is('select')){
                    var value = operationDataObj.find("option:selected").data("id");
                    if (!value){
                        value = operationDataObj.find("option:selected").text();
                    }
                }
                operationData[key] = value;
            });
        if(deviceType == "ios"){
            payload = createiOSPayload(operationCode, operationData, deviceList);
        }
        if(deviceType == "android"){
            payload = createAndroidPayload(operationCode, operationData, deviceList);
        }
        return payload;
    };

    /*
     @DeviceType = Device Type of the profile
     @FeatureCodes = Feature Codes to generate the profile from
     */
    module.generateProfile = function(deviceType, featureCodes){
        var generatedProfile = {};
        for (var i = 0; i < featureCodes.length; ++i) {
            var featureCode = featureCodes[i];
            var payload = module.generatePayload(deviceType, featureCode);
            generatedProfile[featureCode] = payload.operation;
        }
        return generatedProfile;
    };
    return module;
}();