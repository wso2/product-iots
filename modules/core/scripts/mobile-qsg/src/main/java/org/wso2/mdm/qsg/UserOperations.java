/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.mdm.qsg;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.mdm.qsg.dto.EMMQSGConfig;
import org.wso2.mdm.qsg.dto.HTTPResponse;
import org.wso2.mdm.qsg.utils.Constants;
import org.wso2.mdm.qsg.utils.HTTPInvoker;

import java.util.HashMap;

/**
 * This class holds the user-mgt related operations like user-create, role-create and change-password.
 */
public class UserOperations {

    public static boolean createUser(String username, String email, boolean isAdmin) {
        HashMap<String, String> headers = new HashMap<String, String>();
        String userEndpoint = EMMQSGConfig.getInstance().getEmmHost() + "/api/device-mgt/v1.0/users";
        //Set the user payload
        JSONObject userData = new JSONObject();
        userData.put("username", username);
        userData.put("emailAddress", email);
        JSONArray roles = new JSONArray();
        if (isAdmin) {
            roles.add("admin");
            userData.put("firstname", "Chris");
            userData.put("lastname", "Admin");
        } else {
            userData.put("password", "kimemmtrial");
            userData.put("firstname", "Alex");
            userData.put("lastname", "User");
        }
        userData.put("roles", roles);
        //Set the headers
        headers.put(Constants.Header.CONTENT_TYPE, Constants.ContentType.APPLICATION_JSON);
        HTTPResponse httpResponse = HTTPInvoker
                .sendHTTPPostWithOAuthSecurity(userEndpoint, userData.toJSONString(), headers);
        if (httpResponse.getResponseCode() == Constants.HTTPStatus.CREATED) {
            return true;
        }
        return false;
    }

    public static boolean changePassword(String username, String pwd) {
        HashMap<String, String> headers = new HashMap<String, String>();
        String pwdEndpoint =
                EMMQSGConfig.getInstance().getEmmHost() + "/api/device-mgt/v1.0/admin/users/" + username + "/credentials";
        //Set the password payload
        JSONObject pwdData = new JSONObject();
        pwdData.put("newPassword", pwd);

        //Set the headers
        headers.put(Constants.Header.CONTENT_TYPE, Constants.ContentType.APPLICATION_JSON);
        HTTPResponse httpResponse =
                HTTPInvoker.sendHTTPPostWithOAuthSecurity(pwdEndpoint, pwdData.toJSONString(), headers);
        if (httpResponse.getResponseCode() == Constants.HTTPStatus.OK) {
            return true;
        }
        return false;
    }

    private static String[] getUserPermissions() {
        String permissions = "/permission/admin/device-mgt/certificates/manage," +
                             "/permission/admin/device-mgt/certificates/view," +
                             "/permission/admin/device-mgt/configurations/view," +
                             "/permission/admin/device-mgt/api," +
                             "/permission/admin/device-mgt/devices/enroll," +
                             "/permission/admin/device-mgt/devices/disenroll," +
                             "/permission/admin/device-mgt/devices/enroll/android," +
                             "/permission/admin/device-mgt/devices/enroll/ios," +
                             "/permission/admin/device-mgt/devices/owning-device," +
                             "/permission/admin/device-mgt/devices/owning-device/view," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/android/applications," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/android/blacklist-app," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/android/camera," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/android/change-lock-code," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/android/clear-password," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/android/encrypt," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/android/enterprise-wipe," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/android/info," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/android/install-app," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/android/location," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/android/lock," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/android/logcat," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/android/mute," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/android/password-policy," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/android/ring," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/android/reboot," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/android/send-notification," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/android/uninstall-app," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/android/update-app," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/android/unlock," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/android/upgrade," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/android/vpn," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/android/webclip," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/android/wifi," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/android/wipe," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/ios," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/ios/airplay," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/ios/apn," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/ios/app-list," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/ios/app-lock," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/ios/app-to-per-app-vpn," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/ios/cal-subscription," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/ios/caldav," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/ios/cellular," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/ios/clear-passcode," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/ios/device-info," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/ios/email," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/ios/enterprise-app," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/ios/enterprise-wipe," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/ios/get-restrictions," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/ios/ldap," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/ios/location," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/ios/lock," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/ios/notification," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/ios/passcode-policy," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/ios/per-app-vpn," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/ios/profile-list," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/ios/remove-app," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/ios/remove-profile," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/ios/restriction," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/ios/ring," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/ios/store-app," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/ios/vpn," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/ios/webclip," +
                             "/permission/admin/device-mgt/devices/owning-device/operations/ios/wifi," +
                             "/permission/admin/device-mgt/notifications/view," +
                             "/permission/admin/device-mgt/platform-configurations/view," +
                             "/permission/admin/device-mgt/policies/view," +
                             "/permission/admin/device-mgt/applications/manage," +
                             "/permission/admin/manage/mobileapp/create," +
                             "/permission/admin/manage/mobileapp/install," +
                             "/permission/admin/manage/resources/browse," +
                             "/permission/admin/manage/webapp/subscribe," +
                             "/permission/admin/manage/search/advanced-search," +
                             "/permission/admin/manage/search/resources," +
                             "/permission/admin/manage/resources/govern/mobileapp/list," +
                             "/permission/admin/login";
        return permissions.split(",");
    }

    public static boolean createRole(String roleName, String[] users) {
        HashMap<String, String> headers = new HashMap<String, String>();
        String roleEndpoint = EMMQSGConfig.getInstance().getEmmHost() + "/api/device-mgt/v1.0/roles";
        //Set the role payload
        JSONObject roleData = new JSONObject();
        roleData.put("roleName", roleName);
        JSONArray perms = new JSONArray();
        String[] permissions = getUserPermissions();
        for (String perm : permissions) {
            perms.add(perm);
        }
        roleData.put("permissions", perms);
        JSONArray usrs = new JSONArray();
        for (String usr : users) {
            usrs.add(usr);
        }
        roleData.put("permissions", perms);
        roleData.put("users", usrs);
        //Set the headers
        headers.put(Constants.Header.CONTENT_TYPE, Constants.ContentType.APPLICATION_JSON);
        HTTPResponse httpResponse =
                HTTPInvoker.sendHTTPPostWithOAuthSecurity(roleEndpoint, roleData.toJSONString(), headers);
        if (httpResponse.getResponseCode() == Constants.HTTPStatus.CREATED) {
            return true;
        }
        return false;
    }
}
