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
                               @Context HttpServletResponse response){

        log.info("Restrat Browser : " + deviceId);

        try {
            boolean result = sendCommandViaMQTT(owner,deviceId,
                                    DigitalDisplayConstants.RESTART_DISPLAY_CONSTANT,"");

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
                             @Context HttpServletResponse response){

        log.info("Close Browser : " + deviceId);

        try {
            boolean result = sendCommandViaMQTT(owner,deviceId,
                                    DigitalDisplayConstants.BROWSER_CONTENT,"CLOSE");

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
                                 @Context HttpServletResponse response){

        log.info("Terminate Display : " + deviceId);

        try {
            boolean result = sendCommandViaMQTT(owner,deviceId,
                                    DigitalDisplayConstants.DISPLAY_CONTENT,"TERMINATE");

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
                               @Context HttpServletResponse response){

        log.info("Restrat Display : " + deviceId);

        try {
            boolean result = sendCommandViaMQTT(owner,deviceId,
                                    DigitalDisplayConstants.DISPLAY_CONTENT,"RESTART");

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

    @Path("/edit-content")
    @PUT
    public void editContent(@QueryParam("deviceId") String deviceId,
                            @QueryParam("owner") String owner,
                            @Context HttpServletResponse response){

        log.info("Edit Content Display Id - " + deviceId + " by " + owner);

    }


    @Path("/add-resource")
    @POST
    public void addNewResource(@QueryParam("deviceId") String deviceId){
        log.info("Add Sequence : " + deviceId);
    }

    @Path("/delete-resource")
    @POST
    public void deleteResource(@QueryParam("deviceId") String deviceId){
        log.info("Delete Resource : " + deviceId);
    }

    @Path("/upload-content")
    @POST
    public void uploadContent(@QueryParam("deviceId") String deviceId){
        log.info("Upload Content : " + deviceId);
    }

    @Path("/remove-content/{directory_name}")
    @POST
    public void removeContent(@PathParam("directory_name") String directoryName,
                              @QueryParam("deviceId") String deviceId ,
                              @QueryParam("owner") String owner,
                              @Context HttpServletResponse response){

        log.info("Remove Content : " + deviceId);
        log.info("Directory Name : " + directoryName);
        try {
            boolean result = sendCommandViaMQTT(owner,deviceId,
                    DigitalDisplayConstants.REMOVE_DIRECTORY_CONSTANT,directoryName);

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
                                @Context HttpServletResponse response){

        log.info("Shut down display : " + deviceId);

        try {
            boolean result = sendCommandViaMQTT(owner,deviceId,
                                    DigitalDisplayConstants.SHUTDOWN_DISPLAY_CONSTANT,"");

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
                                 @Context HttpServletResponse response){

        log.info("Shutdown Displays : " + displays.toString());

    }

    @Path("/check-availability")
    @POST
    public void getStatus(@QueryParam("deviceId") String deviceId,
                          @QueryParam("owner") String owner,
                          @Context HttpServletResponse response){

        log.info("Status : " + deviceId);

        try {
            boolean result = sendCommandViaMQTT(owner,deviceId,
                                DigitalDisplayConstants.SHUTDOWN_DISPLAY_CONSTANT,"");

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
