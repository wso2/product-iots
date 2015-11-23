package org.wso2.carbon.device.mgt.iot.sample.digitaldisplay.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.common.DeviceController;
import org.wso2.carbon.device.mgt.iot.common.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.sample.digitaldisplay.plugin.constants.DigitalDisplayConstants;
import org.wso2.carbon.device.mgt.iot.sample.digitaldisplay.service.impl.util.MqttDigitalDisplaySubscriber;

import javax.servlet.http.HttpServletRequest;
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

    private static MqttDigitalDisplaySubscriber mqttDigitalDisplaySubscriber;

    public void setMqttDigitalDisplaySubscriber(MqttDigitalDisplaySubscriber mqttDigitalDisplaySubscriber){
        DigitalDisplayControllerService.mqttDigitalDisplaySubscriber = mqttDigitalDisplaySubscriber;

        try {
            mqttDigitalDisplaySubscriber.connectAndSubscribe();
        } catch (DeviceManagementException e) {
            log.error(e.getErrorMessage());
        }
    }

    @Path("/restart-browser")
    @POST
    public void restartBrowser(@QueryParam("deviceId") String deviceId ,
                               @QueryParam("owner") String owner,
                               @Context HttpServletResponse response,
                               @Context HttpServletRequest request){

        log.info("Restrat Browser : " + deviceId);

        String sessionId = request.getSession().getId();

        try {
            boolean result = sendCommandViaMQTT(owner,deviceId,sessionId +":" +
                                    DigitalDisplayConstants.BROWSER_CONTENT,"RESTART");

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
                             @Context HttpServletResponse response,
                             @Context HttpServletRequest request){

        log.info("Close Browser : " + deviceId);

        String sessionId = request.getSession().getId();

        try {
            boolean result = sendCommandViaMQTT(owner,deviceId,sessionId + ":" +
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
                                 @Context HttpServletResponse response,
                                 @Context HttpServletRequest request){

        log.info("Terminate Display : " + deviceId);

        String sessionId = request.getSession().getId();

        try {
            boolean result = sendCommandViaMQTT(owner,deviceId,sessionId +":" +
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
                               @Context HttpServletResponse response,
                               @Context HttpServletRequest request){

        log.info("Restrat Display : " + deviceId);

        String sessionId = request.getSession().getId();

        try {
            boolean result = sendCommandViaMQTT(owner,deviceId,sessionId +":" +
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

    @Path("/edit-sequence")
    @POST
    public void editSequence(@QueryParam("deviceId") String deviceId){

        log.info("Edit Sequence : " + deviceId);
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

    @Path("/remove-content")
    @POST
    public void removeContent(@QueryParam("deviceId") String deviceId){
        log.info("Remove Content : " + deviceId);
    }

    @Path("/shutdown-display")
    @POST
    public void shutDownDisplay(@QueryParam("deviceId") String deviceId,
                                @QueryParam("owner") String owner,
                                @Context HttpServletResponse response,
                                @Context HttpServletRequest request){

        log.info("Shut down display : " + deviceId);

        String sessionId = request.getSession().getId();

        try {
            boolean result = sendCommandViaMQTT(owner,deviceId,sessionId +":" +
                                    DigitalDisplayConstants.DISPLAY_CONTENT,"SHUTDOWN");

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
                          @Context HttpServletResponse response,
                          @Context HttpServletRequest request){

        log.info("Status : " + deviceId);

        String sessionId = request.getSession().getId();

        try {
            boolean result = sendCommandViaMQTT(owner,deviceId,sessionId + ":" +
                                DigitalDisplayConstants.DISPLAY_CONTENT,"AVAILABLE");

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

    private boolean sendCommandViaMQTT(String deviceOwner, String deviceId, String resource,
                                       String state) throws DeviceManagementException {

        boolean result;
        DeviceController deviceController = new DeviceController();

        try {
            result = deviceController.publishMqttControl(deviceOwner,
                    DigitalDisplayConstants.DEVICE_TYPE,
                    deviceId, resource, state);
        } catch (DeviceControllerException e) {
            String errorMsg = "Error whilst trying to publish to MQTT Queue";
            log.error(errorMsg);

            throw new DeviceManagementException(errorMsg, e);
        }
        return result;
    }

}
