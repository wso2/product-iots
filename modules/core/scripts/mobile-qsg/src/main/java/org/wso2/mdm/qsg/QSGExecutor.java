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

import org.wso2.mdm.qsg.dto.MobileApplication;
import org.wso2.mdm.qsg.utils.Constants;
import org.wso2.mdm.qsg.utils.HTTPInvoker;
import org.wso2.mdm.qsg.utils.QSGUtils;

import java.util.*;

/**
 * Main class of EMM-QSG module. This class will populate each sample scenario.
 */
public class QSGExecutor {

    public static void main(String[] args) {
        boolean status = false;
        Scanner scanner = new Scanner(System.in);
        //  prompt for the user's name
        System.out.print("Enter your email address and press enter : ");
        String email = scanner.next();
        if (!QSGUtils.isValidEmailAddress(email)) {
            do {
                System.out.print("Please enter a valid email address and press enter : ");
                email = scanner.next();
            } while (!QSGUtils.isValidEmailAddress(email));
        }
        //Setup the OAuth token
        String token = QSGUtils.getOAuthToken();
        if (token == null) {
            System.out.println("Unable to get the OAuth token. Please check the config.properties file.");
            System.exit(0);
        }
        HTTPInvoker.oAuthToken = token;
        //Creates the admin user
        status = UserOperations.createUser("tom", "tom@mobx.com", true);
        if (!status) {
            System.out.println("Unable to create the admin user. Please check the config.properties file.");
            System.exit(0);
        }
        status = UserOperations.changePassword("tom", "tomemm");
        if (!status) {
            System.out.println("Unable to change the password of the admin user. Terminating the EMM QSG now.");
            System.exit(0);
        }
        //Creates the emm user
        status = UserOperations.createUser("kim", email, false);
        if (!status) {
            System.out.println("Unable to create the emm user Kim. Terminating the EMM QSG now.");
            System.exit(0);
        }
        status = UserOperations.changePassword("kim", "kimemm");
        if (!status) {
            System.out.println("Unable to change the password of the emm user. Terminating the EMM QSG now.");
            System.exit(0);
        }
        //Creates the emm-user role
        status = UserOperations.createRole(Constants.EMM_USER_ROLE, new String[] { "kim" });
        if (!status) {
            System.out.println("Unable to create the emm user role. Terminating the EMM QSG now.");
            System.exit(0);
        }
        //Add the android policy
        status = PolicyOperations.createPasscodePolicy("android-passcode-policy1", Constants.DeviceType.ANDROID);
        if (!status) {
            System.out.println("Unable to create the android passcode policy. Terminating the EMM QSG now.");
            System.exit(0);
        }
        //Add the windows policy
        status = PolicyOperations.createPasscodePolicy("windows-passcode-policy1", Constants.DeviceType.WINDOWS);
        if (!status) {
            System.out.println("Unable to create the windows passcode policy. Terminating the EMM QSG now.");
            System.exit(0);
        }
        //Add the iOS policy
        status = PolicyOperations.createPasscodePolicy("ios-passcode-policy1", Constants.DeviceType.IOS);
        if (!status) {
            System.out.println("Unable to create the ios passcode policy. Terminating the EMM QSG now.");
            System.exit(0);
        }
        //Upload the android application
        MobileApplication application = AppOperations.uploadApplication(Constants.DeviceType.ANDROID, "catalog.apk",
                                                                        "application/vnd.android.package-archive");
        if (application == null) {
            System.out.println("Unable to upload the sample android application. Terminating the EMM QSG now.");
            System.exit(0);
        }
        //Upload the assets
        application = AppOperations.uploadAssets(Constants.DeviceType.ANDROID, application);
        if (application == null) {
            System.out.println(
                    "Unable to upload the assets for sample android application. Terminating the EMM QSG now.");
            System.exit(0);
        }
        //Create application entry in publisher
        status = AppOperations.addApplication("Catalog", application, true);
        if (!status) {
            System.out.println("Unable to create the mobile application. Terminating the EMM QSG now.");
            System.exit(0);
        }

        //Upload the ios application
        MobileApplication iOSApplication = AppOperations.uploadApplication(Constants.DeviceType.IOS, "PNDemo.ipa",
                "application/octet-stream");
        iOSApplication.setVersion("1.0.0");
        //Upload the assets
        iOSApplication = AppOperations.uploadAssets(Constants.DeviceType.IOS, iOSApplication);
        if (iOSApplication == null) {
            System.out.println(
                    "Unable to upload the assets for sample iOS application. Terminating the EMM QSG now.");
            System.exit(0);
        }
        //Create application entry in publisher
        status = AppOperations.addApplication("WSO2Con", iOSApplication, true);
        if (!status) {
            System.out.println("Unable to create the mobile application. Terminating the EMM QSG now.");
            System.exit(0);
        }
    }
}
