package org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.service.impl.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.json.JSONObject;
import org.wso2.carbon.certificate.mgt.core.exception.KeystoreException;
import org.wso2.carbon.certificate.mgt.core.service.CertificateManagementService;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.analytics.exception.DataPublisherConfigurationException;
import org.wso2.carbon.device.mgt.analytics.service.DeviceAnalyticsService;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.iot.common.DeviceController;
import org.wso2.carbon.device.mgt.iot.common.controlqueue.xmpp.XmppConfig;
import org.wso2.carbon.device.mgt.iot.common.exception.DeviceControllerException;
import org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.plugin.constants.VirtualFireAlarmConstants;
import org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.service.impl.exception.VirtualFireAlarmException;
import org.wso2.carbon.device.mgt.iot.sample.virtual.firealarm.service.impl.transport.VirtualFireAlarmXMPPConnector;

import javax.ws.rs.HttpMethod;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

public class VirtualFireAlarmServiceUtils {
    private static final Log log = LogFactory.getLog(VirtualFireAlarmServiceUtils.class);

    //TODO; replace this tenant domain
    private static final String SUPER_TENANT = "carbon.super";
    private static final String TEMPERATURE_STREAM_DEFINITION = "org.wso2.iot.devices.temperature";
    private static final String JSON_MESSAGE_KEY = "Msg";
    private static final String JSON_SIGNATURE_KEY = "Sig";

    public static CertificateManagementService getCertificateManagementService() throws
                                                                                 VirtualFireAlarmException {

        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        CertificateManagementService certificateManagementService = (CertificateManagementService)
                ctx.getOSGiService(CertificateManagementService.class, null);

        if (certificateManagementService == null) {
            String msg = "EnrollmentService is not initialized";
            log.error(msg);
            throw new VirtualFireAlarmException(msg);
        }

        return certificateManagementService;
    }


    public static String sendCommandViaHTTP(final String deviceHTTPEndpoint, String urlContext,
                                            boolean fireAndForgot) throws DeviceManagementException {

        String responseMsg = "";
        String urlString = VirtualFireAlarmConstants.URL_PREFIX + deviceHTTPEndpoint + urlContext;

        if (log.isDebugEnabled()) {
            log.debug(urlString);
        }

        if (!fireAndForgot) {
            HttpURLConnection httpConnection = getHttpConnection(urlString);

            try {
                httpConnection.setRequestMethod(HttpMethod.GET);
            } catch (ProtocolException e) {
                String errorMsg =
                        "Protocol specific error occurred when trying to set method to GET" +
                                " for:" + urlString;
                log.error(errorMsg);
                throw new DeviceManagementException(errorMsg, e);
            }

            responseMsg = readResponseFromGetRequest(httpConnection);

        } else {
            CloseableHttpAsyncClient httpclient = null;
            try {

                httpclient = HttpAsyncClients.createDefault();
                httpclient.start();
                HttpGet request = new HttpGet(urlString);
                final CountDownLatch latch = new CountDownLatch(1);
                Future<HttpResponse> future = httpclient.execute(
                        request, new FutureCallback<HttpResponse>() {
                            @Override
                            public void completed(HttpResponse httpResponse) {
                                latch.countDown();
                            }

                            @Override
                            public void failed(Exception e) {
                                latch.countDown();
                            }

                            @Override
                            public void cancelled() {
                                latch.countDown();
                            }
                        });

                latch.await();

            } catch (InterruptedException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Sync Interrupted");
                }
            } finally {
                try {
                    if (httpclient != null) {
                        httpclient.close();

                    }
                } catch (IOException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Failed on close");
                    }
                }
            }
        }

        return responseMsg;
    }


    public static boolean sendCommandViaMQTT(String deviceOwner, String deviceId, String resource,
                                             String state) throws DeviceManagementException {

        boolean result;
        DeviceController deviceController = new DeviceController();

        try {
            PublicKey devicePublicKey = getDevicePublicKey(deviceId);
            PrivateKey serverPrivateKey = VerificationManager.getServerPrivateKey();
            String message = prepareSecurePayLoad(resource, devicePublicKey, serverPrivateKey);

            result = deviceController.publishMqttControl(deviceOwner,
                                                         VirtualFireAlarmConstants.DEVICE_TYPE,
                                                         deviceId, message, state);
        } catch (DeviceControllerException e) {
            String errorMsg = "Error whilst trying to publish to MQTT Queue";
            log.error(errorMsg);
            throw new DeviceManagementException(errorMsg, e);
        } catch (VirtualFireAlarmException e) {
            throw new DeviceManagementException(e);
        }
        return result;
    }

    public static void sendCommandViaXMPP(String deviceOwner, String deviceId, String resource,
                                          String state, VirtualFireAlarmXMPPConnector virtualFireAlarmXMPPConnector)
            throws DeviceManagementException {

        String xmppServerDomain = XmppConfig.getInstance().getXmppEndpoint();
        int indexOfChar = xmppServerDomain.lastIndexOf(File.separator);
        if (indexOfChar != -1) {
            xmppServerDomain = xmppServerDomain.substring((indexOfChar + 1),
                                                          xmppServerDomain.length());
        }

        indexOfChar = xmppServerDomain.indexOf(":");
        if (indexOfChar != -1) {
            xmppServerDomain = xmppServerDomain.substring(0, indexOfChar);
        }

        String clientToConnect = deviceId + "@" + xmppServerDomain + File.separator + deviceOwner;
        String message = resource.replace("/", "") + ":" + state;

        virtualFireAlarmXMPPConnector.sendXMPPMessage(clientToConnect, message, "CONTROL-REQUEST");
    }

	/*	---------------------------------------------------------------------------------------
                    Utility methods relevant to creating and sending http requests
 		---------------------------------------------------------------------------------------	*/

	/* This methods creates and returns a http connection object */

    public static HttpURLConnection getHttpConnection(String urlString) throws
                                                                        DeviceManagementException {

        URL connectionUrl = null;
        HttpURLConnection httpConnection;

        try {
            connectionUrl = new URL(urlString);
            httpConnection = (HttpURLConnection) connectionUrl.openConnection();
        } catch (MalformedURLException e) {
            String errorMsg =
                    "Error occured whilst trying to form HTTP-URL from string: " + urlString;
            log.error(errorMsg);
            throw new DeviceManagementException(errorMsg, e);
        } catch (IOException e) {
            String errorMsg = "Error occured whilst trying to open a connection to: " +
                    connectionUrl.toString();
            log.error(errorMsg);
            throw new DeviceManagementException(errorMsg, e);
        }

        return httpConnection;
    }

	/* This methods reads and returns the response from the connection */

    public static String readResponseFromGetRequest(HttpURLConnection httpConnection)
            throws DeviceManagementException {
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(
                    httpConnection.getInputStream()));
        } catch (IOException e) {
            String errorMsg =
                    "There is an issue with connecting the reader to the input stream at: " +
                            httpConnection.getURL();
            log.error(errorMsg);
            throw new DeviceManagementException(errorMsg, e);
        }

        String responseLine;
        StringBuilder completeResponse = new StringBuilder();

        try {
            while ((responseLine = bufferedReader.readLine()) != null) {
                completeResponse.append(responseLine);
            }
        } catch (IOException e) {
            String errorMsg =
                    "Error occured whilst trying read from the connection stream at: " +
                            httpConnection.getURL();
            log.error(errorMsg);
            throw new DeviceManagementException(errorMsg, e);
        }
        try {
            bufferedReader.close();
        } catch (IOException e) {
            log.error(
                    "Could not succesfully close the bufferedReader to the connection at: " +
                            httpConnection.getURL());
        }

        return completeResponse.toString();
    }

    public static boolean publishToDAS(String owner, String deviceId, float temperature) {
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        ctx.setTenantDomain(SUPER_TENANT, true);
        DeviceAnalyticsService deviceAnalyticsService = (DeviceAnalyticsService) ctx.getOSGiService(
                DeviceAnalyticsService.class, null);
        Object metdaData[] = {owner, VirtualFireAlarmConstants.DEVICE_TYPE, deviceId,
                System.currentTimeMillis()};
        Object payloadData[] = {temperature};

        try {
            deviceAnalyticsService.publishEvent(TEMPERATURE_STREAM_DEFINITION, "1.0.0", metdaData,
                                                new Object[0], payloadData);
        } catch (DataPublisherConfigurationException e) {
            return false;
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return true;
    }



    public static String prepareSecurePayLoad(String message, Key encryptionKey, PrivateKey signatureKey)
            throws VirtualFireAlarmException {
        String encryptedMsg = VerificationManager.encryptMessage(message, encryptionKey);
        String signedPayload = VerificationManager.signMessage(encryptedMsg, signatureKey);

        JSONObject jsonPayload = new JSONObject();
        jsonPayload.append(JSON_MESSAGE_KEY, encryptedMsg);
        jsonPayload.append(JSON_SIGNATURE_KEY, signedPayload);

        return jsonPayload.toString();
    }


    public static String extractMessageFromPayload(String message, Key decryptionKey, PublicKey verifySignatureKey)
            throws VirtualFireAlarmException {
        String actualMessage;

        JSONObject jsonPayload = new JSONObject(message);
        String encryptedMessage = jsonPayload.getString(JSON_MESSAGE_KEY);
        String signedPayload = jsonPayload.getString(JSON_SIGNATURE_KEY);

        if (VerificationManager.verifySignature(encryptedMessage, signedPayload, verifySignatureKey)) {
            actualMessage = VerificationManager.decryptMessage(encryptedMessage, decryptionKey);
        } else {
            String errorMsg = "The message was not signed by a valid client. Could not verify signature on payload";
            throw new VirtualFireAlarmException(errorMsg);
        }

        return actualMessage;
    }


    public static PublicKey getDevicePublicKey(String deviceId) throws VirtualFireAlarmException {
        PublicKey clientPublicKey;
        String alias = "";

        try {
            alias += deviceId.hashCode();

            CertificateManagementService certificateManagementService =
                    VirtualFireAlarmServiceUtils.getCertificateManagementService();
            X509Certificate clientCertificate = (X509Certificate) certificateManagementService.getCertificateByAlias(
                    alias);
            clientPublicKey = clientCertificate.getPublicKey();

        } catch (VirtualFireAlarmException e) {
            String errorMsg = "Could not retrieve CertificateManagementService from the runtime.";
            log.error(errorMsg);
            throw new VirtualFireAlarmException(errorMsg, e);
        } catch (KeystoreException e) {
            String errorMsg = "An error occurred whilst trying to retrieve certificate for deviceId [" + deviceId +
                    "] with alias: [" + alias + "]";
            log.error(errorMsg);
            throw new VirtualFireAlarmException(errorMsg, e);
        }
        return clientPublicKey;
    }

}
