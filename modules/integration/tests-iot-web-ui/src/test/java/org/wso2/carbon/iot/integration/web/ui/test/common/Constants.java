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
package org.wso2.carbon.iot.integration.web.ui.test.common;

public class Constants {
    public static final String IOT_LOGIN_PATH = "/devicemgt/login";
    public static final String IOT_USER_REGISTER_URL = "/devicemgt/register";
    public static final String IOT_USER_ADD_URL = "/devicemgt/user/add";
    public static final String IOT_GROUP_ADD_URL = "/devicemgt/group/add";
    public static final String IOT_HOME_URL = "/devicemgt/";
    public static final String IOT_ENROLL_CONNECTED_CUP = "/device/connectedcup/enroll";
    public static final String IOT_DEVICES_URL = "/devicemgt/devices";
    public static final String IOT_CONNECTED_CUP_NAME = "testDevice";

    public static final String GROUP_NAME_FIELD_ERROR = "Group Name is a required field. It cannot be empty.";
    public static final String GROUP_NAME = "group1";
    public static final String GROUP_DESCRIPTION = "This is a test group";

    public static final String ALERT_NOT_PRESENT = "Alert is not present.";

    public static final String CARBON_HOME = "carbon.home";

    public static final String OS_NAME = "os.name";
    public static final String BUILD_SUCCESS_MSG = "BUILD SUCCESS";

    public static final int IOT_RESTART_THREAD_TIMEOUT = 30;

    public static final String IOT_GRAPH_X_AXIS = "x";
    public static final String IOT_GRAPH_Y_AXIS = "y";

    public static class ConnectedCup {

        public static final String COFFEE_LEVEl = "35";

        public static final String TEMPERATURE = "53";
        public static final String COFFEE_LEVEL_ID = "Coffee Level";

        public static final String COFFEE_LEVEL_LEGEND = "Coffee Level";
        public static final String COFFEE_LEVEL_GRAPH_ID = "coffeelevel";
        public static final String COFFEE_LEVEL_Y_AXIS = "Coffeelevel";
        public static final String COFFEE_LEVEL_X_AXIS = "time";
        public static final String TEMPERATURE_ID = "Temperature";

        public static final String TEMPERATURE_LEGEND = "Temperature";
        public static final String TEMPERATURE_GRAPH_ID = "temperature";
        public static final String TEMPERATURE_Y_AXIS = "Temperature";
        public static final String TEMPERATURE_X_AXIS = "time";

    }

    public static class User {

        public static class Login {

            public static final String USER_NAME_ERROR = "Please enter a username";
            public static final String PASSWORD_ERROR = "Please provide a password";
            public static final String FAILED_ERROR = "Incorrect username or password.!";
            public static final String SHORT_PASSWORD_ERROR = "Your password must be at least 3 characters long";
            public static final String WRONG_USER_NAME = "admnnn";
            public static final String WRONG_USER_PASSWORD = "admmmm";
            public static final String SHORT_PASSWORD = "ad";
            public static final String PAGE_TITLE = "Login | IoT Server";

        }

        public static class Add {

            public static final String FIRST_NAME = "User";
            public static final String LAST_NAME = "User";
            public static final String EMAIL = "user@wso2.com";
            public static final String EMAIL_ERROR = "user.com";
            public static final String USER_NAME = "user";
            public static final String SHORT_USER_NAME = "us";
            public static final String SHORT_USER_NAME_ERROR_MSG = "Username must be between 3 and 30 characters long.";
            public static final String FIRST_NAME_ERROR_MSG = "Firstname is a required field. It cannot be empty.";
            public static final String LAST_NAME_ERROR_MSG = "Lastname is a required field. It cannot be empty.";
            public static final String NO_EMAIL_ERROR_MSG = "Email is a required field. It cannot be empty.";
            public static final String WRONG_EMAIL_ERROR_MSG = "Provided email is invalid. Please check.";

        }

        public static class Register {

            public static final String FIRST_NAME = "Firstname";
            public static final String LAST_NAME = "Lastname";
            public static final String USER_NAME = "testUser";
            public static final String PASSWORD = "testPassword";
            public static final String CONFIRM_PASSWORD = "testPassword";
            public static final String EMAIL = "userName@wso2.com";

        }

    }

    public static class TestSample {

        public static final String SAMPLE_INSTALL = "iot.sample";
        public static final String INSTALL_VERIFY = "iot.sample.install";
        public static final String VERIFY = "iot.sample.install.verify";
        public static final String ENROLL = "iot.sample.enroll";
        public static final String ENROLL_VERIFY = "iot.sample.enroll.verify";
        public static final String TEMPERATURE = "iot.sample.temp";
        public static final String COFFEE_LEVEL = "iot.sample.level";

    }

}
