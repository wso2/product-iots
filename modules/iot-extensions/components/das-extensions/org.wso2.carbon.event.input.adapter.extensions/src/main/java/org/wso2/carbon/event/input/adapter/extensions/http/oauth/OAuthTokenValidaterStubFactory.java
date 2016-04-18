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
package org.wso2.carbon.event.input.adapter.extensions.http.oauth;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.log4j.Logger;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.extensions.http.oauth.exception.OAuthTokenValidationException;
import org.wso2.carbon.event.input.adapter.extensions.http.util.HTTPEventAdapterConstants;
import org.wso2.carbon.identity.oauth2.stub.OAuth2TokenValidationServiceStub;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;

/**
 * This follows object pool pattern to manage the stub for oauth validation service.
 */
public class OAuthTokenValidaterStubFactory extends BasePoolableObjectFactory {
	private static final Logger log = Logger.getLogger(OAuthTokenValidaterStubFactory.class);
	private HttpClient httpClient;
	InputEventAdapterConfiguration eventAdapterConfiguration;


	public OAuthTokenValidaterStubFactory(InputEventAdapterConfiguration eventAdapterConfiguration) {
		this.eventAdapterConfiguration = eventAdapterConfiguration;
		this.httpClient = createHttpClient();
	}

	/**
	 * This creates a OAuth2TokenValidationServiceStub object to the pool.
	 *
	 * @return an OAuthValidationStub object
	 * @throws Exception thrown when creating the object.
	 */
	@Override
	public Object makeObject() throws Exception {
		return this.generateStub();
	}

	/**
	 * This is used to clean up the OAuth validation stub and releases to the object pool.
	 *
	 * @param o object that needs to be released.
	 * @throws Exception throws when failed to release to the pool
	 */
	@Override
	public void passivateObject(Object o) throws Exception {
		if (o instanceof OAuth2TokenValidationServiceStub) {
			OAuth2TokenValidationServiceStub stub = (OAuth2TokenValidationServiceStub) o;
			stub._getServiceClient().cleanupTransport();
		}
	}

	/**
	 * This is used to create a stub which will be triggered through object pool factory, which will create an
	 * instance of it.
	 *
	 * @return OAuth2TokenValidationServiceStub stub that is used to call an external service.
	 * @throws OAuthTokenValidationException will be thrown when initialization failed.
	 */
	private OAuth2TokenValidationServiceStub generateStub() throws OAuthTokenValidationException {
		OAuth2TokenValidationServiceStub stub;
		try {
			URL hostURL = new URL(eventAdapterConfiguration.getProperties().get(
					HTTPEventAdapterConstants.TOKEN_VALIDATION_ENDPOINT_URL));
			if (hostURL != null) {
				stub = new OAuth2TokenValidationServiceStub(hostURL.toString());
				if (stub != null) {
					ServiceClient client = stub._getServiceClient();
					client.getServiceContext().getConfigurationContext().setProperty(
							HTTPConstants.CACHED_HTTP_CLIENT, httpClient);

					HttpTransportProperties.Authenticator auth =
							new HttpTransportProperties.Authenticator();
					auth.setPreemptiveAuthentication(true);
					String username = eventAdapterConfiguration.getProperties().get(HTTPEventAdapterConstants
																							.USERNAME);
					String password = eventAdapterConfiguration.getProperties().get(HTTPEventAdapterConstants
																							.PASSWORD);
					auth.setPassword(username);
					auth.setUsername(password);
					Options options = client.getOptions();
					options.setProperty(HTTPConstants.AUTHENTICATE, auth);
					options.setProperty(HTTPConstants.REUSE_HTTP_CLIENT, Constants.VALUE_TRUE);
					client.setOptions(options);
					if (hostURL.getProtocol().equals("https")) {
						// set up ssl factory since axis2 https transport is used.
						EasySSLProtocolSocketFactory sslProtocolSocketFactory =
								createProtocolSocketFactory();
						Protocol authhttps = new Protocol(hostURL.getProtocol(),
														  (ProtocolSocketFactory) sslProtocolSocketFactory,
														  hostURL.getPort());
						Protocol.registerProtocol(hostURL.getProtocol(), authhttps);
						options.setProperty(HTTPConstants.CUSTOM_PROTOCOL_HANDLER, authhttps);
					}
				} else {
					String errorMsg = "OAuth Validation instanization failed.";
					throw new OAuthTokenValidationException(errorMsg);
				}
			} else {
				String errorMsg = "host url is invalid";
				throw new OAuthTokenValidationException(errorMsg);
			}
		} catch (AxisFault axisFault) {
			throw new OAuthTokenValidationException(
					"Error occurred while creating the OAuth2TokenValidationServiceStub.", axisFault);
		} catch (MalformedURLException e) {
			throw new OAuthTokenValidationException(
					"Error occurred while parsing token endpoint URL", e);
		}

		return stub;
	}

	/**
	 * This is required to create a trusted connection with the external entity.
	 * Have to manually configure it since we use CommonHTTPTransport(axis2 transport) in axis2.
	 *
	 * @return an EasySSLProtocolSocketFactory for SSL communication.
	 */
	private EasySSLProtocolSocketFactory createProtocolSocketFactory() throws OAuthTokenValidationException {
		try {
			EasySSLProtocolSocketFactory easySSLPSFactory = new EasySSLProtocolSocketFactory();
			return  easySSLPSFactory;
		} catch (IOException e) {
			String errorMsg = "Failed to initiate EasySSLProtocolSocketFactory.";
			throw new OAuthTokenValidationException(errorMsg, e);
		} catch (GeneralSecurityException e) {
			String errorMsg = "Failed to set the key material in easy ssl factory.";
			throw new OAuthTokenValidationException(errorMsg, e);
		}
	}

	/**
	 * This created httpclient pool that can be used to connect to external entity. This connection can be configured
	 * via broker.xml by setting up the required http connection parameters.
	 *
	 * @return an instance of HttpClient that is configured with MultiThreadedHttpConnectionManager
	 */
	private HttpClient createHttpClient() {
		HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		params.setDefaultMaxConnectionsPerHost(Integer.parseInt(eventAdapterConfiguration.getProperties().get(
				HTTPEventAdapterConstants.MAXIMUM_HTTP_CONNECTION_PER_HOST)));
		params.setMaxTotalConnections(Integer.parseInt(eventAdapterConfiguration.getProperties().get(
				HTTPEventAdapterConstants.MAXIMUM_TOTAL_HTTP_CONNECTION)));
		HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		connectionManager.setParams(params);
		return new HttpClient(connectionManager);
	}
}
