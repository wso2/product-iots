package org.wso2.carbon.device.mgt.iot.sample.digitaldisplay.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.sample.digitaldisplay.plugin.constants.DigitalDisplayConstants;
import org.wso2.carbon.device.mgt.iot.sample.digitaldisplay.service.impl.communication.CommunicationHandlerException;
import org.wso2.carbon.device.mgt.iot.sample.digitaldisplay.service.impl.util.DigitalDisplayMqttCommunicationHandler;

import javax.servlet.http.HttpServletResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;

import javax.ws.rs.core.Response;
import java.io.File;
import java.util.List;

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

    @Path("/restart-browser")
    @POST
    public void restartBrowser(@QueryParam("deviceId") String deviceId ,
                               @QueryParam("owner") String owner,
                               @QueryParam("randomId") String randomId,
                               @Context HttpServletResponse response){

        log.info("Restrat Browser : " + deviceId);

        try {
            boolean result = sendCommandViaMQTT(owner,deviceId,
                                    randomId +":" + DigitalDisplayConstants.RESTART_BROWSER_CONSTANT,"");

            if(result){
                response.setStatus(Response.Status.OK.getStatusCode());
            }else {
                response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            }

        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }

    }

    @Path("/close-browser")
    @POST
    public void closeBrowser(@QueryParam("deviceId") String deviceId,
                             @QueryParam("owner") String owner,
                             @QueryParam("randomId") String randomId,
                             @Context HttpServletResponse response){

        log.info("Close Browser : " + deviceId);

        try {
            boolean result = sendCommandViaMQTT(owner,deviceId,
                    randomId +":" + DigitalDisplayConstants.CLOSE_BROWSER_CONSTANT ,"");

            if(result){
                response.setStatus(Response.Status.OK.getStatusCode());
            }else {
                response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            }

        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }

    }

    @Path("/terminate-display")
    @POST
    public void terminateDisplay(@QueryParam("deviceId") String deviceId,
                                 @QueryParam("owner") String owner,
                                 @QueryParam("randomId") String randomId,
                                 @Context HttpServletResponse response){

        log.info("Terminate Display : " + deviceId);

        try {
            boolean result = sendCommandViaMQTT(owner,deviceId,
                    randomId +":" + DigitalDisplayConstants.TERMINATE_DISPLAY_CONSTANT,"");

            if(result){
                response.setStatus(Response.Status.OK.getStatusCode());
            }else {
                response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            }

        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }

    }


    @Path("/restart-display")
    @POST
    public void restartDisplay(@QueryParam("deviceId") String deviceId,
                               @QueryParam("owner") String owner,
                               @QueryParam("randomId") String randomId,
                               @Context HttpServletResponse response){

        log.info("Restrat Display : " + deviceId);

        try {
            boolean result = sendCommandViaMQTT(owner,deviceId,
                    randomId +":" + DigitalDisplayConstants.RESTART_DISPLAY_CONSTANT ,"");

            if(result){
                response.setStatus(Response.Status.OK.getStatusCode());
            }else {
                response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            }

        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }

    }

    @Path("/edit-content/{path}/{attribute}/{new-value}")
    @PUT
    public void editSequence(@QueryParam("deviceId") String deviceId,
                            @QueryParam("owner") String owner,
                            @PathParam("path") String path,
                            @PathParam("attribute") String attribute,
                            @PathParam("new-value") String newValue,
                            @QueryParam("randomId") String randomId,
                            @Context HttpServletResponse response){

        log.info("Edit Content Display Id - " + deviceId + " by " + owner);

        try {

            String params = path + File.separator + attribute + File.separator + newValue;

            boolean result = sendCommandViaMQTT(owner,deviceId,
                    randomId + ":" + DigitalDisplayConstants.EDIT_SEQUENCE_CONSTANT,params);

            if(result){
                response.setStatus(Response.Status.OK.getStatusCode());
            }else {
                response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            }

        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }


    }


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

            boolean result = sendCommandViaMQTT(owner,deviceId,
                    randomId + ":" + DigitalDisplayConstants.ADD_NEW_RESOURCE_CONSTANT,params);

            if(result){
                response.setStatus(Response.Status.OK.getStatusCode());
            }else {
                response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            }

        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }
    }


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

            String params = type + File.separator + time + File.separator + path + File.separator + "before=" + nextPage;

            boolean result = sendCommandViaMQTT(owner,deviceId,
                    randomId + ":" + DigitalDisplayConstants.ADD_NEW_RESOURCE_CONSTANT,params);

            if(result){
                response.setStatus(Response.Status.OK.getStatusCode());
            }else {
                response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            }

        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }
    }

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

            String params = type + File.separator + time + File.separator + path + File.separator + "after=" + beforePage;

            boolean result = sendCommandViaMQTT(owner,deviceId,
                    randomId + ":" + DigitalDisplayConstants.ADD_NEW_RESOURCE_CONSTANT,params);

            if(result){
                response.setStatus(Response.Status.OK.getStatusCode());
            }else {
                response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            }

        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }
    }

    @Path("/remove-resource/{path}")
    @DELETE
    public void removeResource(@QueryParam("deviceId") String deviceId,
                               @QueryParam("owner") String owner,
                               @PathParam("path") String path,
                               @QueryParam("randomId") String randomId,
                               @Context HttpServletResponse response){

        log.info("Remove Resource : " + deviceId);

        try {

            String params = path;

            boolean result = sendCommandViaMQTT(owner,deviceId,
                    randomId + ":" + DigitalDisplayConstants.REMOVE_RESOURCE_CONSTANT,params);

            if(result){
                response.setStatus(Response.Status.OK.getStatusCode());
            }else {
                response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            }

        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }

    }

    @Path("/upload-content")
    @POST
    public void uploadContent(@QueryParam("deviceId") String deviceId){
        log.info("Upload Content : " + deviceId);
    }

    @Path("/remove-directory/{directory_name}")
    @DELETE
    public void removeDirectory(@PathParam("directory_name") String directoryName,
                              @QueryParam("deviceId") String deviceId ,
                              @QueryParam("owner") String owner,
                              @QueryParam("randomId") String randomId,
                              @Context HttpServletResponse response){

        log.info("Remove Directory : " + deviceId);
        try {
            boolean result = sendCommandViaMQTT(owner,deviceId,
                    randomId + ":" + DigitalDisplayConstants.REMOVE_DIRECTORY_CONSTANT,directoryName);

            if(result){
                response.setStatus(Response.Status.OK.getStatusCode());
            }else {
                response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            }

        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }
    }

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

            boolean result = sendCommandViaMQTT(owner,deviceId,
                    randomId + ":" + DigitalDisplayConstants.REMOVE_CONTENT_CONSTANT,param);

            if(result){
                response.setStatus(Response.Status.OK.getStatusCode());
            }else {
                response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            }

        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }
    }

    @Path("/shutdown-display")
    @POST
    public void shutDownDisplay(@QueryParam("deviceId") String deviceId,
                                @QueryParam("owner") String owner,
                                @QueryParam("randomId") String randomId,
                                @Context HttpServletResponse response){

        log.info("Shut down display : " + deviceId);

        try {
            boolean result = sendCommandViaMQTT(owner,deviceId,
                    randomId + ":" + DigitalDisplayConstants.SHUTDOWN_DISPLAY_CONSTANT,"");

            if(result){
                response.setStatus(Response.Status.OK.getStatusCode());
            }else {
                response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            }

        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }

    }

    @Path("/shutdown-displays")
    @POST
    public void shutdownDisplays(@QueryParam("owner") String owner,
                                 @FormParam("display")List<String> displays,
                                 @QueryParam("randomId") String randomId,
                                 @Context HttpServletResponse response){

        log.info("Shutdown Displays : " + displays.toString());

    }

    @Path("/get-status")
    @GET
    public void getStatus(@QueryParam("deviceId") String deviceId,
                          @QueryParam("owner") String owner,
                          @QueryParam("randomId") String randomId,
                          @Context HttpServletResponse response){

        log.info("Status : " + deviceId);

        try {
            boolean result = sendCommandViaMQTT(owner,deviceId,
                    randomId + ":" + DigitalDisplayConstants.GET_STATUS_CONSTANT,"");

            if(result){
                response.setStatus(Response.Status.ACCEPTED.getStatusCode());
            }else {
                response.setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
            }

        } catch (DeviceManagementException e) {
            log.error(e);
            response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }

    }

    private boolean sendCommandViaMQTT(String deviceOwner, String deviceId, String operation,
                                       String param) throws DeviceManagementException {

        log.info(deviceOwner);
        String topic = String.format(DigitalDisplayConstants.PUBLISH_TOPIC , deviceOwner , deviceId);
        String payload = operation + ":" + param;

        try {
            digitalDisplayMqttCommunicationHandler.publishToDigitalDisplay(topic, payload, 0, false);
            return true;
        } catch (CommunicationHandlerException e) {
            log.error("Error occurred publishing data.",e);
            return false;
        }
    }

}
