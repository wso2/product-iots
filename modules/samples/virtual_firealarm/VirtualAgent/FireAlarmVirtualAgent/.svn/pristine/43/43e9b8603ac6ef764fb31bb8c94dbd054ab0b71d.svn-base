package org.wso2.carbon.device.mgt.iot.agent.firealarm.communication.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Server;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.communication.CommunicationHandler;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.communication.CommunicationHandlerException;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.communication.CommunicationUtils;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.core.AgentConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public abstract class HTTPCommunicationHandler implements CommunicationHandler {
	private static final Log log = LogFactory.getLog(HTTPCommunicationHandler.class);

	protected Server server;
	protected int port;
	protected int timeoutInterval;

	protected HTTPCommunicationHandler() {
		this.port = CommunicationUtils.getAvailablePort(10);
		this.server = new Server(port);
		timeoutInterval = DEFAULT_TIMEOUT_INTERVAL;
	}

	protected HTTPCommunicationHandler(int port) {
		this.port = port;
		this.server = new Server(this.port);
		timeoutInterval = DEFAULT_TIMEOUT_INTERVAL;
	}

	protected HTTPCommunicationHandler(int port, int timeoutInterval) {
		this.port = port;
		this.server = new Server(this.port);
		this.timeoutInterval = timeoutInterval;
	}

	public void setTimeoutInterval(int timeoutInterval) {
		this.timeoutInterval = timeoutInterval;
	}

	/**
	 * Checks whether the HTTP server is up and listening for incoming requests.
	 *
	 * @return true if the server is up & listening for requests, else false.
	 */
	public boolean isConnected() {
		return server.isStarted();
	}


	protected void incrementPort() {
		this.port = this.port + 1;
		server = new Server(port);
	}

	/**
	 * Shuts-down the HTTP Server.
	 */
	public void closeConnection() throws Exception {
		if (server != null && isConnected()) {
			server.stop();
		}
	}

	/**
	 * This is a utility method that creates and returns a HTTP connection object.
	 *
	 * @param urlString the URL pattern to which the connection needs to be created
	 * @return an HTTPConnection object which cn be used to send HTTP requests
	 * @throws CommunicationHandlerException if errors occur when creating the HTTP connection with
	 *                                       the given URL string
	 */
	protected HttpURLConnection getHttpConnection(String urlString) throws
	                                                                CommunicationHandlerException {
		URL connectionUrl;
		HttpURLConnection httpConnection;

		try {
			connectionUrl = new URL(urlString);
			httpConnection = (HttpURLConnection) connectionUrl.openConnection();
		} catch (MalformedURLException e) {
			String errorMsg = AgentConstants.LOG_APPENDER +
					"Error occured whilst trying to form HTTP-URL from string: " + urlString;
			log.error(errorMsg);
			throw new CommunicationHandlerException(errorMsg, e);
		} catch (IOException exception) {
			String errorMsg =
					AgentConstants.LOG_APPENDER + "Error occured whilst trying to open a" +
							" connection to: " + urlString;
			log.error(errorMsg);
			throw new CommunicationHandlerException(errorMsg, exception);
		}
		return httpConnection;
	}

	/**
	 * This is a utility method that reads and returns the response from a HTTP connection
	 *
	 * @param httpConnection the connection from which a response is expected
	 * @return the response (as a string) from the given HTTP connection
	 * @throws CommunicationHandlerException if any errors occur whilst reading the response from
	 *                                       the connection stream
	 */
	protected String readResponseFromHttpRequest(HttpURLConnection httpConnection)
			throws CommunicationHandlerException {
		BufferedReader bufferedReader;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(
					httpConnection.getInputStream(), StandardCharsets.UTF_8));
		} catch (IOException exception) {
			String errorMsg = AgentConstants.LOG_APPENDER +
					"There is an issue with connecting the reader to the input stream at: " +
					httpConnection.getURL();
			log.error(errorMsg);
			throw new CommunicationHandlerException(errorMsg, exception);
		}

		String responseLine;
		StringBuilder completeResponse = new StringBuilder();

		try {
			while ((responseLine = bufferedReader.readLine()) != null) {
				completeResponse.append(responseLine);
			}
		} catch (IOException exception) {
			String errorMsg = AgentConstants.LOG_APPENDER +
					"Error occured whilst trying read from the connection stream at: " +
					httpConnection.getURL();
			log.error(errorMsg);
			throw new CommunicationHandlerException(errorMsg, exception);
		}
		try {
			bufferedReader.close();
		} catch (IOException exception) {
			log.error(AgentConstants.LOG_APPENDER +
					          "Could not succesfully close the bufferedReader to the connection " +
					          "at: " + httpConnection.getURL());
		}
		return completeResponse.toString();
	}

}
