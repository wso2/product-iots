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

package org.homeautomation.digitaldisplay.api;

import org.wso2.carbon.apimgt.annotations.api.API;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.DeviceType;
import org.wso2.carbon.device.mgt.extensions.feature.mgt.annotations.Feature;

import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@API(name = "digital_display", version = "1.0.0", context = "/digital_display", tags = {"digital_display"})
@DeviceType(value = "digital_display")
public interface DigitalDisplayControllerService {

    /**
     * Restart the running browser in the given digital display.
     *
     * @param deviceId  id of the controlling digital display
     * @param sessionId web socket id of the method invoke client
     */
    @Path("device/{deviceId}/restart-browser")
    @POST
    @Feature(code = "restart-browser", name = "Restart Browser", type = "operation",
            description = "Restart Browser in Digital Display")
    Response restartBrowser(@PathParam("deviceId") String deviceId, @HeaderParam("sessionId") String sessionId);

    /**
     * Terminate all running processes. If this execute we have to reboot digital display manually.
     *
     * @param deviceId  id of the controlling digital display
     * @param sessionId web socket id of the method invoke client
     */
    @Path("device/{deviceId}/terminate-display")
    @POST
    @Feature(code = "terminate-display", name = "Terminate Display", type = "operation",
            description = "Terminate all running process in Digital Display")
    Response terminateDisplay(@PathParam("deviceId") String deviceId, @HeaderParam("sessionId") String sessionId);

    /**
     * Reboot running digital display
     *
     * @param deviceId  id of the controlling digital display
     * @param sessionId web socket id of the method invoke client
     */
    @Path("device/{deviceId}/restart-display")
    @POST
    @Feature(code = "restart-display", name = "Restart Display", type = "operation",
            description = "Restart Digital Display")
    Response restartDisplay(@PathParam("deviceId") String deviceId, @HeaderParam("sessionId") String sessionId);

    /**
     * Search through the sequence and edit requested resource
     *
     * @param deviceId  id of the controlling digital display
     * @param sessionId web socket id of the method invoke client
     * @param name      name of page need to change
     * @param attribute this can be path,time or type
     * @param newValue  page is used to replace path
     */
    @Path("device/{deviceId}/edit-sequence")
    @POST
    @Feature(code = "edit-sequence", name = "Edit Sequence", type = "operation",
            description = "Search through the sequence and edit requested resource in Digital Display")
    Response editSequence(@PathParam("deviceId") String deviceId, @FormParam("name") String name,
                          @FormParam("attribute") String attribute, @FormParam("new-value") String newValue,
                          @HeaderParam("sessionId") String sessionId);

    @Path("device/{deviceId}/upload-content")
    @POST
    @Feature(code = "upload-content", name = "Upload Content", type = "operation",
            description = "Search through the sequence and edit requested resource in Digital Display")
    Response uploadContent(@PathParam("deviceId") String deviceId, @FormParam("remote-path") String remotePath,
                           @FormParam("screen-name") String screenName, @HeaderParam("sessionId") String sessionId);

    /**
     * Add new resource end to the existing sequence
     *
     * @param deviceId  id of the controlling digital display
     * @param sessionId web socket id of the method invoke client
     * @param type      type of new resource
     * @param time      new resource visible time
     * @param path      URL of the new resource
     */
    @Path("device/{deviceId}/add-resource")
    @POST
    @Feature(code = "add-resource", name = "Add Resource", type = "operation",
            description = "Add new resource end to the existing sequence in Digital Display")
    Response addNewResource(@PathParam("deviceId") String deviceId, @FormParam("type") String type,
                            @FormParam("time") String time, @FormParam("path") String path,
                            @FormParam("name") String name, @FormParam("position") String position,
                            @HeaderParam("sessionId") String sessionId);

    /**
     * Delete a resource in sequence
     *
     * @param deviceId  id of the controlling digital display
     * @param sessionId web socket id of the method invoke client
     * @param name      name of the page no need to delete
     */
    @Path("device/{deviceId}/remove-resource")
    @POST
    @Feature(code = "remove-resource", name = "Remove Resource", type = "operation",
            description = "Delete a resource from sequence in Digital Display")
    Response removeResource(@PathParam("deviceId") String deviceId, @FormParam("name") String name,
                            @HeaderParam("sessionId") String sessionId);

    /**
     * Restart HTTP in running display
     *
     * @param deviceId  id of the controlling digital display
     * @param sessionId web socket id of the method invoke client
     */
    @Path("device/{deviceId}/restart-server")
    @POST
    @Feature(code = "restart-server", name = "Restart Server", type = "operation",
            description = "Stop HTTP Server running in Digital Display")
    Response restartServer(@PathParam("deviceId") String deviceId, @HeaderParam("sessionId") String sessionId);

    /**
     * Get screenshot of running display
     *
     * @param deviceId  id of the controlling digital display
     * @param sessionId web socket id of the method invoke client
     */
    @Path("device/{deviceId}/screenshot")
    @POST
    @Feature(code = "screenshot", name = "Take Screenshot", type = "operation",
            description = "Show current view in Digital Display")
    Response showScreenshot(@PathParam("deviceId") String deviceId, @HeaderParam("sessionId") String sessionId);

    /**
     * Get statistics of running display
     *
     * @param deviceId  id of the controlling digital display
     * @param sessionId web socket id of the method invoke client
     */
    @Path("device/{deviceId}/get-device-status")
    @POST
    @Feature(code = "get-device-status", name = "Get Device Statistics", type = "operation",
            description = "Current status in Digital Display")
    Response getDevicestatus(@PathParam("deviceId") String deviceId, @HeaderParam("sessionId") String sessionId);

    /**
     * Stop specific display
     *
     * @param deviceId  id of the controlling digital display
     * @param sessionId web socket id of the method invoke client
     */
    @Path("device/{deviceId}/get-content-list")
    @POST
    @Feature(code = "get-content-list", name = "Get Content List", type = "operation",
            description = "Content List in Digital Display")
    Response getResources(@PathParam("deviceId") String deviceId, @HeaderParam("sessionId") String sessionId);

}
