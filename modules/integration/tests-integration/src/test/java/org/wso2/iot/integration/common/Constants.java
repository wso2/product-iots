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
package org.wso2.iot.integration.common;

import java.io.File;

/**
 * Constants used through out the test suite are defined here.
 */
public final class Constants {

    public static final String DEVICE_ID = "1234";
    public static final String NUMBER_NOT_EQUAL_TO_DEVICE_ID = "1111";
    public static final String DEVICE_IMEI = "123123123";
    public static final String AUTOMATION_CONTEXT = "MDM";
    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_URL_ENCODED = "application/x-www-form-urlencoded";
    public static final String OAUTH_CLIENT_ID = "client_id";
    public static final String OAUTH_CLIENT_SECRET = "client_secret";
    public static final String OAUTH_ACCESS_TOKEN = "access_token";
    public static final String ANDROID_DEVICE_TYPE = "android";
    public static final String HTTP_METHOD_POST = "POST";
    public static final String HTTP_METHOD_PUT = "PUT";
    public static final String HTTP_METHOD_GET = "GET";
    public static final String HTTP_METHOD_DELETE = "DELETE";
    public static final String DEVICE_IDENTIFIER_KEY = "deviceIdentifier";
    public static final String DEVICE_IDENTIFIERS_KEY = "deviceIDs";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String APPLICATION_SOAP_XML = "application/soap+xml; charset=utf-8";
    public static final String UTF8 = "UTF-8";
    public static final String ZERO = "0";
    public static final String NULL = "[]";

    public static final class DynamicClientAuthentication {
        public static final String REGISTRATION_ENDPOINT = "/dynamic-client-web/register";
        public static final String TOKEN_ENDPOINT = "/oauth2/token";
        public static final String OAUTH_TOKEN_PAYLOAD = "grant_type=password&username=admin&password=admin&scope=prod";
        private static StringBuffer dynamicClientPayloadBuffer = new StringBuffer();
        public static final String DYNAMIC_CLIENT_REGISTRATION_PAYLOAD = dynamicClientPayloadBuffer.append("{\"clientName\":").
                append(" \"device\",\"owner\": \"admin\",\"grantType\": \"password\",\"callbackUrl\": \"www.google.lk\"," +
                       "\"saasApp\": \"" + true + "\"}").toString();

        private DynamicClientAuthentication() {
            throw new AssertionError();
        }
    }

    public static final class AndroidEnrollment {
        public static final String ENROLLMENT_PAYLOAD_FILE_NAME = "android-enrollment-payloads.json";
        public static final String ENROLLMENT_RESPONSE_PAYLOAD_FILE_NAME = "android-enrollment-response-payloads.json";
        public static final String ENROLLMENT_ENDPOINT = "/mdm-android-agent/enrollment/";
        public static final String ENROLLMENT_GROUP = "android-enrollment";

        private AndroidEnrollment() {
            throw new AssertionError();
        }
    }

    public static final class AndroidPolicy {
        public static final String POLICY_RESPONSE_PAYLOAD_FILE_NAME = "android-policy-response-payloads.json";
        public static final String POLICY_ENDPOINT = "/mdm-android-agent/policy/";
        public static final String POLICY_GROUP = "android-policy";
        public static final String GET_EFFECTIVE_POLICY = "getEffectivePolicy";

        private AndroidPolicy() {
            throw new AssertionError();
        }
    }

    public static final class WindowsEnrollment {
        public static final String DISCOVERY_GET_URL = "/mdm-windows-agent/services/discovery/get";
        public static final String DISCOVERY_POST_URL = "/mdm-windows-agent/services/discovery/post";
        public static final String BSD_URL = "/mdm-windows-agent/services/federated/bst/authentication";
        public static final String MS_EXCEP = "/mdm-windows-agent/services/certificatepolicy/xcep";
        public static final String WINDOWS_ENROLLMENT_GROUP = "windows-enrollment";
        public static final String WSTEP_URL = "/mdm-windows-agent/services/deviceenrolment/wstep";
        public static final String SYNC_ML_URL = "/mdm-windows-agent/services/syncml/devicemanagement/request";
        public static final String DISCOVERY_POST_FILE = "windows" + File.separator + "enrollment" + File
                .separator + "discovery-post.xml";
        public static final String MS_XCEP_FILE =
                "windows" + File.separator + "enrollment" + File.separator + "ms_xcep.xml";
        public static final String WS_STEP_FILE =
                "windows" + File.separator + "enrollment" + File.separator + "wstep.xml";
        public static final String BSD_PAYLOAD = "{\"credentials\" : {\"username\" : \"admin\", \"email\" : \"admin@wso2.com\", " +
                                                 "\"password\" : \"admin\", \"ownership\" : \"BYOD\", " +
                                                 "\"token\" : \"cbe53efd46ec612c456540f8dfef5428\"}}";

        private WindowsEnrollment() {
            throw new AssertionError();
        }
    }

    public static final class AndroidOperations {
        public static final String OPERATION_PAYLOAD_FILE_NAME = "android-operation-payloads.json";
        public static final String OPERATIONS_GROUP = "operations";
        public static final String COMMAND_OPERATION_PAYLOAD = "[\"" + DEVICE_ID + "\"]";
        public static final String CAMERA_OPERATION = "camera";
        public static final String WIPE_DATA_OPERATION = "wipe_data";
        public static final String INSTALL_APPS_OPERATION = "install_apps";
        public static final String NOTIFICATION_OPERATION = "notification";
        public static final String WIFI_OPERATION = "wifi";
        public static final String ENCRYPT_OPERATION = "encrypt";
        public static final String CHANGE_LOCK_OPERATION = "change_lock";
        public static final String PASSWORD_POLICY_OPERATION = "password_policy";
        public static final String WEB_CLIP_OPERATION = "web_clip";
        public static final String OPERATION_ENDPOINT = "/mdm-android-agent/operation/";
        public static final String LOCK_ENDPOINT = "/mdm-android-agent/operation/lock";
        public static final String LOCATION_ENDPOINT = "/mdm-android-agent/operation/location";
        public static final String CLEAR_PASSWORD_ENDPOINT = "/mdm-android-agent/operation/clear-password";
        public static final String CAMERA_ENDPOINT = "/mdm-android-agent/operation/camera";
        public static final String DEVICE_INFO_ENDPOINT = "/mdm-android-agent/operation/device-info";
        public static final String ENTERPRISE_WIPE_ENDPOINT = "/mdm-android-agent/operation/enterprise-wipe";
        public static final String WIPE_DATA_ENDPOINT = "/mdm-android-agent/operation/wipe-data";
        public static final String APPLICATION_LIST_ENDPOINT = "/mdm-android-agent/operation/application-list";
        public static final String RING_ENDPOINT = "/mdm-android-agent/operation/ring-device";
        public static final String MUTE_ENDPOINT = "/mdm-android-agent/operation/mute";
        public static final String INSTALL_APPS_ENDPOINT = "/mdm-android-agent/operation/install-application";
        public static final String UNINSTALL_APPS_ENDPOINT = "/mdm-android-agent/operation/uninstall-application";
        public static final String BLACKLIST_APPS_ENDPOINT = "/mdm-android-agent/operation/blacklist-applications";
        public static final String NOTIFICATION_ENDPOINT = "/mdm-android-agent/operation/notification";
        public static final String WIFI_ENDPOINT = "/mdm-android-agent/operation/wifi";
        public static final String ENCRYPT_ENDPOINT = "/mdm-android-agent/operation/encrypt";
        public static final String CHANGE_LOCK_ENDPOINT = "/mdm-android-agent/operation/change-lock-code";
        public static final String PASSWORD_POLICY_ENDPOINT = "/mdm-android-agent/operation/password-policy";
        public static final String WEB_CLIP_ENDPOINT = "/mdm-android-agent/operation/webclip";

        private AndroidOperations() {
            throw new AssertionError();
        }
    }

    public static final class AndroidDeviceManagement {
        public static final String DEVICE_MANAGEMENT_GROUP = "device-mgt";
        public static final String KEY_DEVICE_ID = "deviceIdentifier";
        public static final String KEY_DEVICE_NAME = "name";
        public static final String DEVICE_MGT_ENDPOINT = "/mdm-android-agent/device/";
        public static final String LICENSE_SECTION = "This";
        public static final String LICENSE_ENDPOINT = DEVICE_MGT_ENDPOINT + "license";
        public static final String APP_LIST_ENDPOINT = DEVICE_MGT_ENDPOINT + "appList/" +
                                                       Constants.DEVICE_ID;
        public static final String REQUEST_MODIFY_DEVICE_EXPECTED = "{\"responseMessage\":\"Device information " +
                                                                    "has modified successfully.\"}";
        public static final String APPLIST_PAYLOAD = "{\"id\":\"1\"," +
                                                     "\"applicationIdentifier\": \"appid\",\"\"platform\": \"android\"," +
                                                     "\"name\": \"testapp\"}";
        public static final String RESPONSE_PAYLOAD_FILE_NAME = "android-device-mgt-response-payloads.json";

        private AndroidDeviceManagement() {
            throw new AssertionError();
        }
    }

    public static final class AndroidConfigurationManagement {
        public static final String DEVICE_CONFIGURATION_GROUP = "android-config-mgt";
        public static final String CONFIG_MGT_ENDPOINT = "/mdm-android-agent/configuration/";
        public static final String PAYLOAD_FILE_NAME = "android-configuration-payloads.json";
        public static final String RESPONSE_PAYLOAD_FILE_NAME = "android-config-response-payloads.json";

        private AndroidConfigurationManagement() {
            throw new AssertionError();
        }
    }

    public static final class OperationManagement {
        public static final String PATH_APPS = "/apps";
        public static final String OPERATION_MANAGEMENT_GROUP = "api-policy-mgt";
        public static final String GET_DEVICE_APPS_ENDPOINT = "/mdm-admin/operations/android/";
        public static final String GET_DEVICE_OPERATIONS_ENDPOINT = "/mdm-admin/operations/android/";

        private OperationManagement() {
            throw new AssertionError();
        }
    }

    public static final class MobileDeviceManagement {
        public static final String MOBILE_DEVICE_MANAGEMENT_GROUP = "mobile-device-mgt";
        public static final String GET_DEVICE_COUNT_ENDPOINT = "/mdm-admin/devices/count";
        public static final String NO_OF_DEVICES = "1";
        public static final String GET_ALL_DEVICES_ENDPOINT = "/mdm-admin/devices";
        public static final String VIEW_DEVICE_TYPES_ENDPOINT = "/mdm-admin/devices/types";
        public static final String VIEW_DEVICE_RESPONSE_PAYLOAD_FILE_NAME =
                "mobile-device-mgt-view-device-types-response-payloads.json";

        private MobileDeviceManagement() {
            throw new AssertionError();
        }
    }


    public static final class UserManagement {
        public static final String USER_MANAGEMENT_GROUP = "user-mgt";
        public static final String USER_NAME = "username123";
        public static final String USER_ENDPOINT = "/mdm-admin/users";
        public static final String USER_PAYLOAD_FILE_NAME = "user-payloads.json";
        public static final String USER_RESPONSE_PAYLOAD_FILE_NAME = "user-response-payloads.json";
        public static final String VIEW_USER_ENDPOINT = "/mdm-admin/users/view";

        private UserManagement() {
            throw new AssertionError();
        }

    }

    public static final class RoleManagement {
        public static final String ROLE_MANAGEMENT_GROUP = "role-mgt";
        public static final String ADD_ROLE_ENDPOINT = "/mdm-admin/roles";
        public static final String ROLE_PAYLOAD_FILE_NAME = "role-payloads.json";
        public static final String UPDATE_ROLE_PERMISSION_ENDPOINT = "/mdm-admin/roles?rolename=administration";
        public static final String UPDATE_ROLE_PERMISSION_PAYLOAD_FILE_NAME = "role-payloads.json";
        public static final String REMOVE_ROLE_ENDPOINT = "/mdm-admin/roles?rolename=administration";

        private RoleManagement() {
            throw new AssertionError();
        }

    }

    public static final class PolicyManagement {
        public static final String POLICY_MANAGEMENT_GROUP = "policy-mgt";
        public static final String ADD_POLICY_ENDPOINT = "/mdm-admin/policies/active-policy";
        public static final String POLICY_PAYLOAD_FILE_NAME = "policy-payloads.json";
        public static final String POLICY_RESPONSE_PAYLOAD_FILE_NAME = "policy-response-payloads.json";
        public static final String UPDATE_POLICY_ENDPOINT = "/mdm-admin/policies/1";
        public static final String REMOVE_POLICY_ENDPOINT = "/mdm-admin/policies/bulk-remove";
        public static final String REMOVE_POLICY_PAYLOAD_FILE_NAME = "[1]";
        public static final String VIEW_POLICY_LIST_ENDPOINT = "/mdm-admin/policies";

        private PolicyManagement() {
            throw new AssertionError();
        }
    }

    public static final class FeatureManagement {
        public static final String FEATURE_MANAGEMENT_GROUP = "feature-mgt";
        public static final String VIEW_FEATURES_ENDPOINT = "/mdm-admin/features/android";

        private FeatureManagement() {
            throw new AssertionError();
        }
    }

    public static final class LicenseManagement {
        public static final String LICENSE_MANAGEMENT_GROUP = "license-mgt";
        public static final String GET_LICENSE_ENDPOINT = "/mdm-admin/license/android/en_US";
        public static final String LICENSE_RESPONSE_PAYLOAD_FILE_NAME = "license-response-payloads.json";

        private LicenseManagement() {
            throw new AssertionError();
        }

    }

    public static final class ConfigurationManagement {
        public static final String CONFIGURATION_MANAGEMENT_GROUP = "configuration-mgt";
        public static final String CONFIGURATION_ENDPOINT = "/mdm-admin/configuration";
        public static final String CONFIGURATION_PAYLOAD_FILE_NAME = "configuration-payloads.json";
        public static final String CONFIGURATION_RESPONSE_PAYLOAD_FILE_NAME = "configuration-response-payloads.json";

        private ConfigurationManagement() {
            throw new AssertionError();
        }
    }

    public static final class NotificationManagement {
        public static final String NOTIFICATION_MANAGEMENT_GROUP = "notification-mgt";
        public static final String NOTIFICATION_ENDPOINT = "/mdm-admin/notifications";
        public static final String NOTIFICATION_PAYLOAD_FILE_NAME = "notification-payloads.json";
        public static final String NOTIFICATION_RESPONSE_PAYLOAD_FILE_NAME = "notification-response-payloads.json";
        public static final String NOTIFICATION_UPDATE_ENDPOINT = "/mdm-admin/notifications/1234/NEW";

        private NotificationManagement() {
            throw new AssertionError();
        }

    }
}
