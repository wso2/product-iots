package org.wso2.carbon.device.mgt.iot.sample.digitaldisplay.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.sample.digitaldisplay.plugin.constants.DigitalDisplayConstants;
import org.wso2.carbon.device.mgt.iot.sample.digitaldisplay.plugin.exception.DigitalDisplayException;
import org.wso2.carbon.device.mgt.iot.sample.digitaldisplay.service.impl.communication.CommunicationHandlerException;
import org.wso2.carbon.device.mgt.iot.sample.digitaldisplay.service.impl.util.DigitalDisplayMqttCommunicationHandler;

import javax.servlet.http.HttpServletResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;

import javax.ws.rs.core.Response;
import java.io.File;

/**
 * Created by nuwan on 11/13/15.
 */

public class DigitalDisplayControllerService {


    private static Log log = LogFactory.getLog(DigitalDisplayControllerService.class);

    private static DigitalDisplayMqttCommunicationHandler digitalDisplayMqttCommunicationHandler;

    public void setDigitalDisplayMqttCommunicationHandler(DigitalDisplayMqttCommunicationHandler digitalDisplayMqttCommunicationHandler){
        DigitalDisplayControllerService.digitalDisplayMqttCommunicationHandler = digitalDisplayMqttCommunicationHandler;

        digitalDisplayMqttCommunicationHandler.connect();

    }

    public DigitalDisplayMqttCommunicationHandler getDigitalDisplayMqttCommunicationHandler(){
        return DigitalDisplayControllerService.digitalDisplayMqttCommunicationHandler;
    }


    /**
     * Restart the running browser in the given digital display.
     *
     * @param deviceId id of the controlling digital display
     * @param owner owner of the digital display
     * @param randomId web socket id of the method invoke client
     * @param response response type of the method
     */
    @Path("/restart-browser")
    @POST
    public void restartBrowser(@QueryParam("deviceId") String deviceId ,
                               @QueryParam("owner") String owner,
                               @QueryParam("randomId") String randomId,
                               @Context HttpServletResponse response){

        log.info("Restrat Browser : " + deviceId);

        try {
            sendCommandViaMQTT(owner,deviceId,randomId +":" + DigitalDisplayConstants.RESTART_BROWSER_CONSTANT,"");
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }catch (DigitalDisplayException e){
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

        }

    }

    /**
     * Close the running browser in the given digital display.
     *
     * @param deviceId id of the controlling digital display
     * @param owner owner of the digital display
     * @param randomId web socket id of the method invoke client
     * @param response response type of the method
     */
    @Path("/close-browser")
    @POST
    public void closeBrowser(@QueryParam("deviceId") String deviceId,
                             @QueryParam("owner") String owner,
                             @QueryParam("randomId") String randomId,
                             @Context HttpServletResponse response){

        log.info("Close Browser : " + deviceId);

        try {
            sendCommandViaMQTT(owner,deviceId,randomId +":" + DigitalDisplayConstants.CLOSE_BROWSER_CONSTANT ,"");
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }catch (DigitalDisplayException e){
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }

    /**
     * Terminate all running processes. If this execute we have to reboot digital display manually.
     *
     * @param deviceId id of the controlling digital display
     * @param owner owner of the digital display
     * @param randomId web socket id of the method invoke client
     * @param response response type of the method
     */
    @Path("/terminate-display")
    @POST
    public void terminateDisplay(@QueryParam("deviceId") String deviceId,
                                 @QueryParam("owner") String owner,
                                 @QueryParam("randomId") String randomId,
                                 @Context HttpServletResponse response){

        log.info("Terminate Display : " + deviceId);

        try {
            sendCommandViaMQTT(owner,deviceId,randomId +":" + DigitalDisplayConstants.TERMINATE_DISPLAY_CONSTANT,"");
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }catch (DigitalDisplayException e){
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }

    /**
     * Restart python server in given digital display
     *
     * @param deviceId id of the controlling digital display
     * @param owner owner of the digital display
     * @param randomId web socket id of the method invoke client
     * @param response response type of the method
     */
    @Path("/restart-display")
    @POST
    public void restartDisplay(@QueryParam("deviceId") String deviceId,
                               @QueryParam("owner") String owner,
                               @QueryParam("randomId") String randomId,
                               @Context HttpServletResponse response){

        log.info("Restrat Display : " + deviceId);

        try {
            sendCommandViaMQTT(owner,deviceId,randomId +":" + DigitalDisplayConstants.RESTART_DISPLAY_CONSTANT ,"");
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }catch (DigitalDisplayException e){
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }

    /**
     * Search through the sequence and edit requested resource
     *
     * @param deviceId id of the controlling digital display
     * @param owner owner of the digital display
     * @param randomId web socket id of the method invoke client
     * @param response response type of the method
     * @param path page no need to change
     * @param attribute this can be path,time or type
     * @param newValue page is used to replace path
     */
    @Path("/edit-content/{path}/{attribute}/{new-value}")
    @PUT
    public void editContent(@QueryParam("deviceId") String deviceId,
                            @QueryParam("owner") String owner,
                            @PathParam("path") String path,
                            @PathParam("attribute") String attribute,
                            @PathParam("new-value") String newValue,
                            @QueryParam("randomId") String randomId,
                            @Context HttpServletResponse response){

        log.info("Edit Content Display Id - " + deviceId + " by " + owner);

        try {
            String params = path + File.separator + attribute + File.separator + newValue;
            sendCommandViaMQTT(owner,deviceId,randomId + ":" + DigitalDisplayConstants.EDIT_SEQUENCE_CONSTANT,params);
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }catch (DigitalDisplayException e){
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }

    /**
     * Add new resource end to the existing sequence
     *
     * @param deviceId id of the controlling digital display
     * @param owner owner of the digital display
     * @param randomId web socket id of the method invoke client
     * @param response response type of the method
     * @param type type of new resource
     * @param time new resource visible time
     * @param path URL of the new resource
     */
    @Path("/add-resource/{type}/{time}/{path}")
    @PUT
    public void addNewResource(@QueryParam("deviceId") String deviceId,
                               @QueryParam("owner") String owner,
                               @PathParam("type") String type,
                               @PathParam("time") String time,
                               @PathParam("path") String path,
                               @QueryParam("randomId") String randomId,
                               @Context HttpServletResponse response){

        log.info("Add Sequence : " + deviceId);

        try {
            String params = type + File.separator + time + File.separator + path;
            sendCommandViaMQTT(owner,deviceId,randomId + ":" +
                            DigitalDisplayConstants.ADD_NEW_RESOURCE_CONSTANT,params);
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }catch (DigitalDisplayException e){
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
    }

    /**
     * Add new resource to sequence before given page no
     *
     * @param deviceId id of the controlling digital display
     * @param owner owner of the digital display
     * @param randomId web socket id of the method invoke client
     * @param response response type of the method
     * @param type type of the new resource
     * @param time new resource visible time
     * @param path URL of the new resource
     * @param nextPage next page no of after adding new resource
     */
    @Path("/add-resource-before/{type}/{time}/{path}/{next-page}")
    @PUT
    public void addNewResourceBefore(@QueryParam("deviceId") String deviceId,
                               @QueryParam("owner") String owner,
                               @QueryParam("randomId") String randomId,
                               @PathParam("type") String type,
                               @PathParam("time") String time,
                               @PathParam("path") String path,
                               @PathParam("next-page") String nextPage,
                               @Context HttpServletResponse response){

        log.info("Add Sequence : " + deviceId);

        try {
            String params = type + File.separator + time + File.separator + path +
                    File.separator + "before=" + nextPage;
            sendCommandViaMQTT(owner,deviceId,randomId + ":" +
                    DigitalDisplayConstants.ADD_NEW_RESOURCE_CONSTANT,params);
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }catch (DigitalDisplayException e){
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
    }


    /**
     * Add new resource to sequence after given page
     *
     * @param deviceId id of the controlling digital display
     * @param owner owner of the digital display
     * @param randomId web socket id of the method invoke client
     * @param response response type of the method
     * @param type type of the new resource
     * @param time new resource visible time
     * @param path URL of the new resource
     * @param beforePage before page no of after adding new resource
     */
    @Path("/add-resource-next/{type}/{time}/{path}/{before-page}")
    @PUT
    public void addNewResourceAfter(@QueryParam("deviceId") String deviceId,
                                     @QueryParam("owner") String owner,
                                     @PathParam("type") String type,
                                     @PathParam("time") String time,
                                     @PathParam("path") String path,
                                     @PathParam("before-page") String beforePage,
                                     @QueryParam("randomId") String randomId,
                                     @Context HttpServletResponse response){

        log.info("Add Sequence : " + deviceId);

        try {
            String params = type + File.separator + time + File.separator + path +
                    File.separator + "after=" + beforePage;
            sendCommandViaMQTT(owner,deviceId,randomId + ":" +
                    DigitalDisplayConstants.ADD_NEW_RESOURCE_CONSTANT,params);
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }catch (DigitalDisplayException e){
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }
    }

    /**
     * Delete a resource in sequence
     *
     * @param deviceId id of the controlling digital display
     * @param owner owner of the digital display
     * @param randomId web socket id of the method invoke client
     * @param response response type of the method
     * @param path path of the page no need to delete
     */
    @Path("/remove-resource/{path}")
    @DELETE
    public void removeResource(@QueryParam("deviceId") String deviceId,
                               @QueryParam("owner") String owner,
                               @PathParam("path") String path,
                               @QueryParam("randomId") String randomId,
                               @Context HttpServletResponse response){

        log.info("Remove Resource : " + deviceId);

        try {
            sendCommandViaMQTT(owner,deviceId,randomId + ":" +
                    DigitalDisplayConstants.REMOVE_RESOURCE_CONSTANT,path);
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }catch (DigitalDisplayException e){
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }

    /**
     * Remove directory and whole content
     *
     * @param deviceId id of the controlling digital display
     * @param owner owner of the digital display
     * @param randomId web socket id of the method invoke client
     * @param response response type of the method
     * @param directoryName path of the folder need to delete
     */
    @Path("/remove-directory/{directory-name}")
    @DELETE
    public void removeDirectory(@PathParam("directory-name") String directoryName,
                              @QueryParam("deviceId") String deviceId ,
                              @QueryParam("owner") String owner,
                              @QueryParam("randomId") String randomId,
                              @Context HttpServletResponse response){

        log.info("Remove Directory : " + deviceId);
        try {
            sendCommandViaMQTT(owner,deviceId,randomId + ":" +
                    DigitalDisplayConstants.REMOVE_DIRECTORY_CONSTANT,directoryName);
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }catch (DigitalDisplayException e){
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }

    /**
     * Remove content from www folder
     *
     * @param deviceId id of the controlling digital display
     * @param owner owner of the digital display
     * @param randomId web socket id of the method invoke client
     * @param directoryName path of directory of request file contain
     * @param content file name of need to delete
     * @param response response type of the method
     */
    @Path("/remove-content/{directory_name}/{content}")
    @DELETE
    public void removeContent(@PathParam("directory_name") String directoryName,
                              @PathParam("content") String content,
                              @QueryParam("deviceId") String deviceId ,
                              @QueryParam("owner") String owner,
                              @QueryParam("randomId") String randomId,
                              @Context HttpServletResponse response){

        log.info("Remove Content : " + deviceId);
        try {
            String param = directoryName + File.separator + content;
            sendCommandViaMQTT(owner,deviceId,randomId + ":" + DigitalDisplayConstants.REMOVE_CONTENT_CONSTANT,param);
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }catch (DigitalDisplayException e){
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }

    /**
     * Stop specific display
     *
     * @param deviceId id of the controlling digital display
     * @param owner owner of the digital display
     * @param randomId web socket id of the method invoke client
     * @param response response type of the method
     */
    @Path("/shutdown-display")
    @POST
    public void shutDownDisplay(@QueryParam("deviceId") String deviceId,
                                @QueryParam("owner") String owner,
                                @QueryParam("randomId") String randomId,
                                @Context HttpServletResponse response){

        log.info("Shut down display : " + deviceId);

        try {
            sendCommandViaMQTT(owner,deviceId,randomId + ":" + DigitalDisplayConstants.SHUTDOWN_DISPLAY_CONSTANT,"");
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }catch (DigitalDisplayException e){
            log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }

    /**
     * Check specific digital display power ON of OFF
     *
     * @param deviceId id of the controlling digital display
     * @param owner owner of the digital display
     * @param randomId web socket id of the method invoke client
     * @param response response type of the method
     */
    @Path("/get-status")
    @GET
    public void getStatus(@QueryParam("deviceId") String deviceId,
                          @QueryParam("owner") String owner,
                          @QueryParam("randomId") String randomId,
                          @Context HttpServletResponse response){

        log.info("Status : " + deviceId);

        try {
            sendCommandViaMQTT(owner,deviceId,randomId + ":" + DigitalDisplayConstants.GET_STATUS_CONSTANT,"");
            response.setStatus(Response.Status.OK.getStatusCode());
        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }catch (DigitalDisplayException e){
             log.error(e);
            response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        }

    }

    /**
     * Send message via MQTT protocol
     *
     * @param deviceOwner owner of target digital display
     * @param deviceId id of the target digital display
     * @param operation operation need to execute
     * @param param parameters need to given operation
     * @throws DeviceManagementException
     * @throws DigitalDisplayException
     */

    private void sendCommandViaMQTT(String deviceOwner, String deviceId, String operation,
                                       String param) throws DeviceManagementException, DigitalDisplayException {

        log.info(deviceOwner);
        String topic = String.format(DigitalDisplayConstants.PUBLISH_TOPIC , deviceOwner , deviceId);
        String payload = operation + ":" + param;

        try {
            digitalDisplayMqttCommunicationHandler.publishToDigitalDisplay(topic, payload, 0, false);
        } catch (CommunicationHandlerException e) {
            String errorMessage = "Error publishing data to device with ID " + deviceId;
            throw new DigitalDisplayException(errorMessage,e);
        }
    }

}
