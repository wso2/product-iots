/*
* Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.iot.sample.digitaldisplay.plugin.constants;

public class DigitalDisplayConstants {

    public final static String DEVICE_TYPE = "digital_display";
    public final static String DEVICE_PLUGIN_DEVICE_NAME = "DEVICE_NAME";
    public final static String DEVICE_PLUGIN_DEVICE_ID = "DIGITAL_DISPLAY_DEVICE_ID";

    public final static String MQTT_BROKER_END_POINT = "tcp://204.232.188.214:1883";

    public final static String SHUTDOWN_DISPLAY_CONSTANT = "shutdown_display";
    public final static String RESTART_DISPLAY_CONSTANT = "restart_display";
    public final static String REMOVE_DIRECTORY_CONSTANT = "remove_dir_and_content";
    public final static String REMOVE_CONTENT_CONSTANT = "remove_content";
    public final static String CLOSE_BROWSER_CONSTANT = "close_browser";
    public final static String RESTART_BROWSER_CONSTANT = "restart_browser";
    public final static String TERMINATE_DISPLAY_CONSTANT = "terminate_display";
    public final static String EDIT_SEQUENCE_CONSTANT = "edit_content_config";
    public final static String ADD_NEW_RESOURCE_CONSTANT = "add_new_resource";
    public final static String REMOVE_RESOURCE_CONSTANT = "remove_resources";
    public final static String GET_STATUS_CONSTANT = "get_status";

    public final static String PUBLISH_TOPIC = "wso2/iot/%s/digital_display/%s/digital_display_subscriber";

}
