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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by nuwan on 11/13/15.
 */

public class DigitalDisplayControllerService {


    private static Log log = LogFactory.getLog(DigitalDisplayControllerService.class);

    private static ConcurrentHashMap<String, String> deviceToIpMap =
            new ConcurrentHashMap<String, String>();

    private static MqttDigitalDisplaySubscriber mqttDigitalDisplaySubscriber;

    public void setMqttDigitalDisplaySubscriber(MqttDigitalDisplaySubscriber mqttDigitalDisplaySubscriber){
        DigitalDisplayControllerService.mqttDigitalDisplaySubscriber = mqttDigitalDisplaySubscriber;


        try {
            mqttDigitalDisplaySubscriber.connectAndSubscribe();
        } catch (DeviceManagementException e) {
            log.error(e.getErrorMessage());
        }
    }

    public MqttDigitalDisplaySubscriber getMqttDigitalDisplaySubscriber(){
        return mqttDigitalDisplaySubscriber;
    }

    @Path("/register/{owner}/{deviceId}/{ip}/{port}")
    @POST
    public String registerDeviceIP(@PathParam("owner") String owner,
                                   @PathParam("deviceId") String deviceId,
                                   @PathParam("ip") String deviceIP,
                                   @PathParam("port") String devicePort,
                                   @Context HttpServletResponse response,
                                   @Context HttpServletRequest request) {

        //TODO:: Need to get IP from the request itself
        String result;

        log.info("Got register call from IP: " + deviceIP + " for Device ID: " + deviceId +
                " of owner: " + owner);

        String deviceHttpEndpoint = deviceIP + ":" + devicePort;
        deviceToIpMap.put(deviceId, deviceHttpEndpoint);

        result = "Device-IP Registered";
        response.setStatus(Response.Status.OK.getStatusCode());

        if (log.isDebugEnabled()) {
            log.debug(result);
        }

        return result;
    }


    @Path("/update")
    @POST
    public boolean updateDevice(){

		/*String deviceHttpEndPoint = deviceToIpMap.get(deviceId);
		String[] deviceEndPontDetails = deviceHttpEndPoint.split(":");
		String deviceIp = deviceEndPontDetails[0];
		int port = Integer.parseInt(deviceEndPontDetails[1]);
		*/
        //Call publishDataTOQueue(topic,message)
        log.info("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa ");
        return false;

    }

    public static String getEndPoint(String deviceId){
        return deviceToIpMap.get(deviceId);
    }


    private boolean sendCommandViaMQTT(String deviceOwner, String deviceId, String resource,
                                       String state) throws DeviceManagementException {

        boolean result = false;
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
