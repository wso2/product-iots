package org.wso2.carbon.device.mgt.iot.agent.firealarm.communication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CommunicationUtils {
	private static final Log log = LogFactory.getLog(CommunicationUtils.class);

	public static final int MIN_PORT_NUMBER = 9000;
	public static final int MAX_PORT_NUMBER = 11000;

	/**
	 * Given a server endpoint as a String, this method splits it into Protocol, Host and Port
	 *
	 * @param ipString a network endpoint in the format - '<PROTOCOL>://<HOST>:<PORT>'
	 * @return a map with keys "Protocol", "Host" & "Port" for the related values from the ipString
	 * @throws CommunicationHandlerException
	 */
	public static Map<String, String> getHostAndPort(String ipString)
			throws CommunicationHandlerException {
		Map<String, String> ipPortMap = new HashMap<String, String>();
		String[] ipPortArray = ipString.split(":");

		if (ipPortArray.length != 3) {
			String errorMsg =
					"The IP String - '" + ipString +
							"' is invalid. It needs to be in format '<PROTOCOL>://<HOST>:<PORT>'.";
			log.info(errorMsg);
			throw new CommunicationHandlerException(errorMsg);
		}

		ipPortMap.put("Protocol", ipPortArray[0]);
		ipPortMap.put("Host", ipPortArray[1].replace(File.separator, ""));
		ipPortMap.put("Port", ipPortArray[2]);
		return ipPortMap;
	}

	/**
	 * This method validates whether a specific IP Address is of IPv4 type
	 *
	 * @param ipAddress the IP Address which needs to be validated
	 * @return true if it is of IPv4 type and false otherwise
	 */
	public static boolean validateIPv4(String ipAddress) {
		try {
			if (ipAddress == null || ipAddress.isEmpty()) {
				return false;
			}

			String[] parts = ipAddress.split("\\.");
			if (parts.length != 4) {
				return false;
			}

			for (String s : parts) {
				int i = Integer.parseInt(s);
				if ((i < 0) || (i > 255)) {
					return false;
				}
			}
			return !ipAddress.endsWith(".");

		} catch (NumberFormatException nfe) {
			log.warn("The IP Address: " + ipAddress + " could not " +
					         "be validated against IPv4-style");
			return false;
		}
	}


	public static Map<String, String> getInterfaceIPMap() throws CommunicationHandlerException {

		Map<String, String> interfaceToIPMap = new HashMap<String, String>();
		Enumeration<NetworkInterface> networkInterfaces;
		String networkInterfaceName = "";
		String ipAddress;

		try {
			networkInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException exception) {
			String errorMsg =
					"Error encountered whilst trying to get the list of network-interfaces";
			log.error(errorMsg);
			throw new CommunicationHandlerException(errorMsg, exception);
		}

		try {
			for (; networkInterfaces.hasMoreElements(); ) {
				networkInterfaceName = networkInterfaces.nextElement().getName();

				if (log.isDebugEnabled()) {
					log.debug("Network Interface: " + networkInterfaceName);
					log.debug("------------------------------------------");
				}

				Enumeration<InetAddress> interfaceIPAddresses = NetworkInterface.getByName(
						networkInterfaceName).getInetAddresses();

				for (; interfaceIPAddresses.hasMoreElements(); ) {
					ipAddress = interfaceIPAddresses.nextElement().getHostAddress();

					if (log.isDebugEnabled()) {
						log.debug("IP Address: " + ipAddress);
					}

					if (CommunicationUtils.validateIPv4(ipAddress)) {
						interfaceToIPMap.put(networkInterfaceName, ipAddress);
					}
				}

				if (log.isDebugEnabled()) {
					log.debug("------------------------------------------");
				}
			}
		} catch (SocketException exception) {
			String errorMsg =
					"Error encountered whilst trying to get the IP Addresses of the network " +
							"interface: " + networkInterfaceName;
			log.error(errorMsg);
			throw new CommunicationHandlerException(errorMsg, exception);
		}

		return interfaceToIPMap;
	}


	/**
	 * Attempts to find a free port between the MIN_PORT_NUMBER(9000) and MAX_PORT_NUMBER(11000).
	 * Tries 'RANDOMLY picked' port numbers between this range up-until "randomAttempts" number of
	 * times. If still fails, then tries each port in descending order from the MAX_PORT_NUMBER
	 * whilst skipping already attempted ones via random selection.
	 *
	 * @param randomAttempts no of times to TEST port numbers picked randomly over the given range
	 * @return an available/free port
	 */
	public static synchronized int getAvailablePort(int randomAttempts) {
		ArrayList<Integer> failedPorts = new ArrayList<Integer>(randomAttempts);

		Random randomNum = new Random();
		int randomPort = MAX_PORT_NUMBER;

		while (randomAttempts > 0) {
			randomPort = randomNum.nextInt(MAX_PORT_NUMBER - MIN_PORT_NUMBER) + MIN_PORT_NUMBER;

			if (checkIfPortAvailable(randomPort)) {
				return randomPort;
			}
			failedPorts.add(randomPort);
			randomAttempts--;
		}

		randomPort = MAX_PORT_NUMBER;

		while (true) {
			if (!failedPorts.contains(randomPort) && checkIfPortAvailable(randomPort)) {
				return randomPort;
			}
			randomPort--;
		}
	}


	private static boolean checkIfPortAvailable(int port) {
		ServerSocket tcpSocket = null;
		DatagramSocket udpSocket = null;

		try {
			tcpSocket = new ServerSocket(port);
			tcpSocket.setReuseAddress(true);

			udpSocket = new DatagramSocket(port);
			udpSocket.setReuseAddress(true);
			return true;
		} catch (IOException ex) {
			// denotes the port is in use
		} finally {
			if (tcpSocket != null) {
				try {
					tcpSocket.close();
				} catch (IOException e) {
	                    /* not to be thrown */
				}
			}

			if (udpSocket != null) {
				udpSocket.close();
			}
		}

		return false;
	}

}
