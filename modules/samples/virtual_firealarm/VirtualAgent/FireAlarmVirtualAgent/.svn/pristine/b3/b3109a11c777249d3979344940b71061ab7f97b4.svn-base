package org.wso2.carbon.device.mgt.iot.agent.firealarm.utils.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.communication.CommunicationHandlerException;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.communication.CommunicationUtils;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.communication.http.HTTPCommunicationHandler;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.core.AgentConstants;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.core.AgentManager;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.exception.AgentCoreOperationException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class HTTPCommunicationHandlerImpl extends HTTPCommunicationHandler {
    private static final Log log = LogFactory.getLog(HTTPCommunicationHandlerImpl.class);

    private ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
    private ScheduledFuture<?> dataPushServiceHandler;
    private ScheduledFuture<?> connectorServiceHandler;

    public HTTPCommunicationHandlerImpl() {
        super();
    }

    public HTTPCommunicationHandlerImpl(int port) {
        super(port);
    }

    public HTTPCommunicationHandlerImpl(int port, int reconnectionInterval) {
        super(port, reconnectionInterval);
    }

    public ScheduledFuture<?> getDataPushServiceHandler() {
        return dataPushServiceHandler;
    }

    public void connect() {
        Runnable connect = new Runnable() {
            public void run() {
                if (!isConnected()) {
                    try {
                        processIncomingMessage();
                        server.start();
                        registerThisDevice();
                        publishDeviceData(AgentManager.getInstance().getPushInterval());
                        log.info("HTTP Server started at port: " + port);

                    } catch (Exception e) {
                        if (log.isDebugEnabled()) {
                            log.warn("Unable to 'START' HTTP server. Will retry after " +
                                     timeoutInterval / 1000 + " seconds.");
                        }
                    }
                }
            }
        };

        connectorServiceHandler = service.scheduleAtFixedRate(connect, 0, timeoutInterval,
                                                              TimeUnit.MILLISECONDS);
    }


    @Override
    public void processIncomingMessage() {
        server.setHandler(new AbstractHandler() {
            public void handle(String s, Request request, HttpServletRequest
                    httpServletRequest,
                               HttpServletResponse httpServletResponse)
                    throws IOException, ServletException {
                httpServletResponse.setContentType("text/html;charset=utf-8");
                httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                request.setHandled(true);

                AgentManager agentManager = AgentManager.getInstance();
                String pathContext = request.getPathInfo();
                String separator = File.separator;

                if (pathContext.toUpperCase().contains(
                        separator + AgentConstants.TEMPERATURE_CONTROL)) {
                    httpServletResponse.getWriter().println(
                            agentManager.getTemperature());

                } else if (pathContext.toUpperCase().contains(
                        separator + AgentConstants.HUMIDITY_CONTROL)) {
                    httpServletResponse.getWriter().println(
                            agentManager.getHumidity());

                } else if (pathContext.toUpperCase().contains(
                        separator + AgentConstants.BULB_CONTROL)) {
                    String[] pathVariables = pathContext.split(separator);

                    if (pathVariables.length != 3) {
                        httpServletResponse.getWriter().println(
                                "Invalid BULB-control received by the device. Need to be in " +
                                "'{host}:{port}/BULB/{ON|OFF}' format.");
                        return;
                    }

                    String switchState = pathVariables[2];

                    if (switchState == null) {
                        httpServletResponse.getWriter().println(
                                "Please specify switch-status of the BULB.");
                    } else {
                        boolean status = switchState.toUpperCase().equals(
                                AgentConstants.CONTROL_ON);
                        agentManager.changeAlarmStatus(status);
                        httpServletResponse.getWriter().println("Bulb is " + (status ?
                                                                              AgentConstants.CONTROL_ON : AgentConstants.CONTROL_OFF));
                    }
                } else {
                    httpServletResponse.getWriter().println(
                            "Invalid control command received by the device.");
                }
            }
        });
    }

    @Override
    public void publishDeviceData(int publishInterval) {
        final AgentManager agentManager = AgentManager.getInstance();
        final String deviceOwner = agentManager.getAgentConfigs().getDeviceOwner();
        final String deviceID = agentManager.getAgentConfigs().getDeviceId();
        boolean simulationMode = false;
        int duration = 2 * 60;
        int frequency = 5;

        Runnable pushDataRunnable = new Runnable() {
            @Override
            public void run() {

                String pushDataPayload = String.format(AgentConstants.PUSH_DATA_PAYLOAD, deviceOwner,
                                                       deviceID, (agentManager.getDeviceIP() + ":" + port),
                                                       agentManager.getTemperature());
                executeDataPush(pushDataPayload);
            }
        };

        if (!simulationMode) {
            dataPushServiceHandler = service.scheduleAtFixedRate(pushDataRunnable, publishInterval,
                                                                 publishInterval,
                                                                 TimeUnit.SECONDS);
        } else {
            String pushDataPayload = String.format(AgentConstants.PUSH_SIMULATION_DATA_PAYLOAD, deviceOwner,
                                                   deviceID, (agentManager.getDeviceIP() + ":" + port),
                                                   agentManager.getTemperature(), true, duration, frequency);
            executeDataPush(pushDataPayload);

        }
    }

    private void executeDataPush(String pushDataPayload) {
        AgentManager agentManager = AgentManager.getInstance();
        int responseCode = -1;
        String pushDataEndPointURL = agentManager.getPushDataAPIEP();
        HttpURLConnection httpConnection = null;

        try {
            httpConnection = getHttpConnection(agentManager.getPushDataAPIEP());
            httpConnection.setRequestMethod(AgentConstants.HTTP_POST);
            httpConnection.setRequestProperty("Authorization", "Bearer " +
                                                               agentManager.getAgentConfigs().getAuthToken());
            httpConnection.setRequestProperty("Content-Type",
                                              AgentConstants.APPLICATION_JSON_TYPE);

            httpConnection.setDoOutput(true);
            DataOutputStream dataOutPutWriter = new DataOutputStream(
                    httpConnection.getOutputStream());
            dataOutPutWriter.writeBytes(pushDataPayload);
            dataOutPutWriter.flush();
            dataOutPutWriter.close();

            responseCode = httpConnection.getResponseCode();
            httpConnection.disconnect();

            log.info(AgentConstants.LOG_APPENDER + "Message - '" + pushDataPayload +
                     "' was published to server at: " + httpConnection.getURL());

        } catch (ProtocolException exception) {
            String errorMsg =
                    "Protocol specific error occurred when trying to set method to " +
                    AgentConstants.HTTP_POST + " for:" + pushDataEndPointURL;
            log.error(AgentConstants.LOG_APPENDER + errorMsg);

        } catch (IOException exception) {
            String errorMsg =
                    "An IO error occurred whilst trying to get the response code from: " +
                    pushDataEndPointURL + " for a " + AgentConstants.HTTP_POST +
                    " " + "method.";
            log.error(AgentConstants.LOG_APPENDER + errorMsg);

        } catch (CommunicationHandlerException exception) {
            log.error(AgentConstants.LOG_APPENDER +
                      "Error encountered whilst trying to create HTTP-Connection " +
                      "to IoT-Server EP at: " +
                      pushDataEndPointURL);
        }

        if (responseCode == HttpStatus.CONFLICT_409 ||
            responseCode == HttpStatus.PRECONDITION_FAILED_412) {
            log.warn(AgentConstants.LOG_APPENDER +
                     "DeviceIP is being Re-Registered due to Push-Data failure " +
                     "with response code: " +
                     responseCode);
            registerThisDevice();

        } else if (responseCode != HttpStatus.NO_CONTENT_204) {
            if (log.isDebugEnabled()) {
                log.error(AgentConstants.LOG_APPENDER + "Status Code: " + responseCode +
                          " encountered whilst trying to Push-Device-Data to IoT " +
                          "Server at: " +
                          agentManager.getPushDataAPIEP());
            }
            agentManager.updateAgentStatus(AgentConstants.SERVER_NOT_RESPONDING);
        }

        if (log.isDebugEnabled()) {
            log.debug(AgentConstants.LOG_APPENDER + "Push-Data call with payload - " +
                      pushDataPayload + ", to IoT Server returned status " +
                      responseCode);
        }
    }

    @Override
    public void disconnect() {
        Runnable stopConnection = new Runnable() {
            public void run() {
                while (isConnected()) {
                    try {
                        dataPushServiceHandler.cancel(true);
                        connectorServiceHandler.cancel(true);
                        closeConnection();
                    } catch (Exception e) {
                        if (log.isDebugEnabled()) {
                            log.warn(AgentConstants.LOG_APPENDER +
                                     "Unable to 'STOP' HTTP server at port: " + port);
                        }

                        try {
                            Thread.sleep(timeoutInterval);
                        } catch (InterruptedException e1) {
                            log.error(AgentConstants.LOG_APPENDER +
                                      "HTTP-Termination: Thread Sleep Interrupt " +
                                      "Exception");
                        }
                    }
                }
            }
        };

        Thread terminatorThread = new Thread(stopConnection);
        terminatorThread.setDaemon(true);
        terminatorThread.start();
    }

    @Override
    public void processIncomingMessage(Object message) {

    }


    public void registerThisDevice() {
        final AgentManager agentManager = AgentManager.getInstance();
        agentManager.updateAgentStatus("Registering...");

        final Runnable ipRegistration = new Runnable() {
            @Override
            public void run() {
                while (isConnected()) {
                    try {
                        int responseCode = registerDeviceIP(
                                agentManager.getAgentConfigs().getDeviceOwner(),
                                agentManager.getAgentConfigs().getDeviceId());

                        if (responseCode == HttpStatus.OK_200) {
                            agentManager.updateAgentStatus(AgentConstants.REGISTERED);
                            break;
                        } else {
                            log.error(AgentConstants.LOG_APPENDER +
                                      "Device Registration with IoT Server at:" + " " +
                                      agentManager.getIpRegistrationEP() +
                                      " failed with response - '" + responseCode + ":" +
                                      HttpStatus.getMessage(responseCode) + "'");
                            agentManager.updateAgentStatus(AgentConstants.RETRYING_TO_REGISTER);
                        }
                    } catch (AgentCoreOperationException exception) {
                        log.error(AgentConstants.LOG_APPENDER +
                                  "Error encountered whilst trying to register the " +
                                  "Device's IP at: " +
                                  agentManager.getIpRegistrationEP() +
                                  ".\nCheck whether the network-interface provided is " +
                                  "accurate");
                        agentManager.updateAgentStatus(AgentConstants.REGISTRATION_FAILED);
                    }

                    try {
                        Thread.sleep(timeoutInterval);
                    } catch (InterruptedException e1) {
                        log.error(AgentConstants.LOG_APPENDER +
                                  "Device Registration: Thread Sleep Interrupt Exception");
                    }
                }
            }
        };

        Thread ipRegisterThread = new Thread(ipRegistration);
        ipRegisterThread.setDaemon(true);
        ipRegisterThread.start();
    }


    /**
     * This method calls the "Register-API" of the IoT Server in order to register the device's IP
     * against its ID.
     *
     * @param deviceOwner the owner of the device by whose name the agent was downloaded.
     *                    (Read from configuration file)
     * @param deviceID    the deviceId that is auto-generated whilst downloading the agent.
     *                    (Read from configuration file)
     * @return the status code of the HTTP-Post call to the Register-API of the IoT-Server
     * @throws AgentCoreOperationException if any errors occur when an HTTPConnection session is
     *                                     created
     */
    private int registerDeviceIP(String deviceOwner, String deviceID)
            throws AgentCoreOperationException {
        int responseCode = -1;
        final AgentManager agentManager = AgentManager.getInstance();

        String networkInterface = agentManager.getNetworkInterface();
        String deviceIPAddress = getDeviceIP(networkInterface);

        if (deviceIPAddress == null) {
            throw new AgentCoreOperationException(
                    "An IP address could not be retrieved for the selected network interface - '" +
                    networkInterface + ".");
        }

        agentManager.setDeviceIP(deviceIPAddress);
        log.info(AgentConstants.LOG_APPENDER + "Device IP Address: " + deviceIPAddress);

        String deviceIPRegistrationEP = agentManager.getIpRegistrationEP();
        String registerEndpointURLString =
                deviceIPRegistrationEP + File.separator + deviceOwner + File.separator + deviceID +
                File.separator + deviceIPAddress + File.separator + port;

        if (log.isDebugEnabled()) {
            log.debug(AgentConstants.LOG_APPENDER + "DeviceIP Registration EndPoint: " +
                      registerEndpointURLString);
        }

        HttpURLConnection httpConnection;
        try {
            httpConnection = getHttpConnection(registerEndpointURLString);
        } catch (CommunicationHandlerException e) {
            String errorMsg =
                    "Protocol specific error occurred when trying to fetch an HTTPConnection to:" +
                    " " +
                    registerEndpointURLString;
            log.error(AgentConstants.LOG_APPENDER + errorMsg);
            throw new AgentCoreOperationException();
        }

        try {
            httpConnection.setRequestMethod(AgentConstants.HTTP_POST);
            httpConnection.setRequestProperty("Authorization", "Bearer " +
                                                               agentManager.getAgentConfigs().getAuthToken());
            httpConnection.setDoOutput(true);
            responseCode = httpConnection.getResponseCode();

        } catch (ProtocolException exception) {
            String errorMsg = "Protocol specific error occurred when trying to set method to " +
                              AgentConstants.HTTP_POST + " for:" + registerEndpointURLString;
            log.error(AgentConstants.LOG_APPENDER + errorMsg);
            throw new AgentCoreOperationException(errorMsg, exception);

        } catch (IOException exception) {
            String errorMsg = "An IO error occurred whilst trying to get the response code from:" +
                              " " +
                              registerEndpointURLString + " for a " + AgentConstants.HTTP_POST + " method.";
            log.error(AgentConstants.LOG_APPENDER + errorMsg);
            throw new AgentCoreOperationException(errorMsg, exception);
        }

        log.info(AgentConstants.LOG_APPENDER + "DeviceIP - " + deviceIPAddress +
                 ", registration with IoT Server at : " +
                 agentManager.getAgentConfigs().getHTTPS_ServerEndpoint() +
                 " returned status " +
                 responseCode);

        return responseCode;
    }

	/*------------------------------------------------------------------------------------------*/
    /* 		Utility methods relevant to creating and sending HTTP requests to the Iot-Server 	*/
	/*------------------------------------------------------------------------------------------*/

    /**
     * This method is used to get the IP of the device in which the agent is run on.
     *
     * @return the IP Address of the device
     * @throws AgentCoreOperationException if any errors occur whilst trying to get the IP address
     */
    private String getDeviceIP() throws AgentCoreOperationException {
        try {
            return Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            String errorMsg = "Error encountered whilst trying to get the device IP address.";
            log.error(AgentConstants.LOG_APPENDER + errorMsg);
            throw new AgentCoreOperationException(errorMsg, e);
        }
    }

    /**
     * This is an overloaded method that fetches the public IPv4 address of the given network
     * interface
     *
     * @param networkInterfaceName the network-interface of whose IPv4 address is to be retrieved
     * @return the IP Address iof the device
     * @throws AgentCoreOperationException if any errors occur whilst trying to get details of the
     *                                     given network interface
     */
    private String getDeviceIP(String networkInterfaceName) throws
                                                            AgentCoreOperationException {
        String ipAddress = null;
        try {
            Enumeration<InetAddress> interfaceIPAddresses = NetworkInterface.getByName(
                    networkInterfaceName).getInetAddresses();
            for (; interfaceIPAddresses.hasMoreElements(); ) {
                InetAddress ip = interfaceIPAddresses.nextElement();
                ipAddress = ip.getHostAddress();
                if (log.isDebugEnabled()) {
                    log.debug(AgentConstants.LOG_APPENDER + "IP Address: " + ipAddress);
                }

                if (CommunicationUtils.validateIPv4(ipAddress)) {
                    return ipAddress;
                }
            }
        } catch (SocketException | NullPointerException exception) {
            String errorMsg =
                    "Error encountered whilst trying to get IP Addresses of the network interface: " +
                    networkInterfaceName +
                    ".\nPlease check whether the name of the network interface used is correct";
            log.error(AgentConstants.LOG_APPENDER + errorMsg);
            throw new AgentCoreOperationException(errorMsg, exception);
        }

        return ipAddress;
    }

}
