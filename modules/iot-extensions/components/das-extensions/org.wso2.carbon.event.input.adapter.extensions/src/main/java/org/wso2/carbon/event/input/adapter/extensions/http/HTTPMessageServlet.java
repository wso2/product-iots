/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.wso2.carbon.event.input.adapter.extensions.http;

import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;
import org.wso2.carbon.event.input.adapter.extensions.ContentInfo;
import org.wso2.carbon.event.input.adapter.extensions.ContentValidator;
import org.wso2.carbon.event.input.adapter.extensions.http.oauth.OAuthTokenValidaterStubFactory;
import org.wso2.carbon.event.input.adapter.extensions.http.util.AuthenticationInfo;
import org.wso2.carbon.event.input.adapter.extensions.http.util.HTTPContentValidator;
import org.wso2.carbon.event.input.adapter.extensions.http.util.HTTPEventAdapterConstants;
import org.wso2.carbon.event.input.adapter.extensions.internal.EventAdapterServiceDataHolder;
import org.wso2.carbon.identity.oauth2.stub.OAuth2TokenValidationServiceStub;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO_OAuth2AccessToken;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationResponseDTO;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This will act as the event reciver.
 */
public class HTTPMessageServlet extends HttpServlet {

	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String AUTH_MESSAGE_STORE_AUTHENTICATION_INFO = "AUTH_MESSAGE_STORE_AUTHENTICATION_INFO";
	private static final String AUTH_FAILURE_RESPONSE = "_AUTH_FAILURE_";
	private static final Pattern PATTERN = Pattern.compile("[B|b]earer\\s");
	private static final String TOKEN_TYPE = "bearer";
	private static String cookie;
	private static Log log = LogFactory.getLog(HTTPMessageServlet.class);
	private GenericObjectPool stubs;

	private InputEventAdapterListener eventAdaptorListener;
	private int tenantId;
	private String exposedTransports;

	public HTTPMessageServlet(InputEventAdapterListener eventAdaptorListener, int tenantId,
							  InputEventAdapterConfiguration eventAdapterConfiguration) {
		this.eventAdaptorListener = eventAdaptorListener;
		this.tenantId = tenantId;
		this.exposedTransports = eventAdapterConfiguration.getProperties().get(
				HTTPEventAdapterConstants.EXPOSED_TRANSPORTS);
		this.stubs = new GenericObjectPool(new OAuthTokenValidaterStubFactory(eventAdapterConfiguration));
	}

	private String getBearerToken(HttpServletRequest request) {
		String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
		if (authorizationHeader != null) {
			Matcher matcher = PATTERN.matcher(authorizationHeader);
			if (matcher.find()) {
				authorizationHeader = authorizationHeader.substring(matcher.end());
			}
		}
		return authorizationHeader;
	}

	private AuthenticationInfo checkAuthentication(HttpServletRequest req) {
		AuthenticationInfo authenticationInfo = (AuthenticationInfo) req.getSession().getAttribute(
				AUTH_MESSAGE_STORE_AUTHENTICATION_INFO);
		if (authenticationInfo != null) {
			return authenticationInfo;
		}
		String bearerToken = getBearerToken(req);
		if (bearerToken == null) {
			return authenticationInfo;
		}

		RealmService realmService = EventAdapterServiceDataHolder.getRealmService();
		try {
			authenticationInfo = validateToken(bearerToken);
			boolean success = authenticationInfo.isAuthenticated();
			if (success) {
				req.getSession().setAttribute(AUTH_MESSAGE_STORE_AUTHENTICATION_INFO, authenticationInfo);
			}
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("checkAuthentication() fail: " + e.getMessage(), e);
			}
		}
		return authenticationInfo;
	}

	/**
	 * This method gets a string accessToken and validates it
	 *
	 * @param token which need to be validated.
	 * @return AuthenticationInfo with the validated results.
	 */
	private AuthenticationInfo validateToken(String token) {
		OAuth2TokenValidationServiceStub tokenValidationServiceStub = null;
		try {
			Object stub = this.stubs.borrowObject();
			if (stub != null) {
				tokenValidationServiceStub = (OAuth2TokenValidationServiceStub) stub;
				if (cookie != null) {
					tokenValidationServiceStub._getServiceClient().getOptions().setProperty(
							HTTPConstants.COOKIE_STRING, cookie);
				}
				return getAuthenticationInfo(token, tokenValidationServiceStub);
			} else {
				log.warn("Stub initialization failed.");
			}
		} catch (RemoteException e) {
			log.error("Error on connecting with the validation endpoint.", e);
		} catch (Exception e) {
			log.error("Error occurred in borrowing an validation stub from the pool.", e);

		} finally {
			try {
				if (tokenValidationServiceStub != null) {
					this.stubs.returnObject(tokenValidationServiceStub);
				}
			} catch (Exception e) {
				log.warn("Error occurred while returning the object back to the oauth token validation service " +
								 "stub pool.", e);
			}
		}
		AuthenticationInfo authenticationInfo = new AuthenticationInfo();
		authenticationInfo.setAuthenticated(false);
		authenticationInfo.setTenantId(-1);
		return authenticationInfo;
	}

	/**
	 * This creates an AuthenticationInfo object that is used for authorization. This method will validate the token
	 * and
	 * sets the required parameters to the object.
	 *
	 * @param token                      that needs to be validated.
	 * @param tokenValidationServiceStub stub that is used to call the external service.
	 * @return AuthenticationInfo This contains the information related to authenticated client.
	 * @throws RemoteException that triggers when failing to call the external service..
	 */
	private AuthenticationInfo getAuthenticationInfo(String token,
													 OAuth2TokenValidationServiceStub tokenValidationServiceStub)
			throws RemoteException, UserStoreException {
		AuthenticationInfo authenticationInfo = new AuthenticationInfo();
		OAuth2TokenValidationRequestDTO validationRequest = new OAuth2TokenValidationRequestDTO();
		OAuth2TokenValidationRequestDTO_OAuth2AccessToken accessToken =
				new OAuth2TokenValidationRequestDTO_OAuth2AccessToken();
		accessToken.setTokenType(TOKEN_TYPE);
		accessToken.setIdentifier(token);
		validationRequest.setAccessToken(accessToken);
		boolean authenticated;
		OAuth2TokenValidationResponseDTO tokenValidationResponse;
		tokenValidationResponse = tokenValidationServiceStub.validate(validationRequest);
		if (tokenValidationResponse == null) {
			authenticationInfo.setAuthenticated(false);
			return authenticationInfo;
		}
		authenticated = tokenValidationResponse.getValid();
		if (authenticated) {
			String authorizedUser = tokenValidationResponse.getAuthorizedUser();
			String username = MultitenantUtils.getTenantAwareUsername(authorizedUser);
			String tenantDomain = MultitenantUtils.getTenantDomain(authorizedUser);
			authenticationInfo.setUsername(username);
			authenticationInfo.setTenantDomain(tenantDomain);
			RealmService realmService = EventAdapterServiceDataHolder.getRealmService();
			int tenantId = realmService.getTenantManager().getTenantId(authenticationInfo.getTenantDomain());
			authenticationInfo.setTenantId(tenantId);
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Token validation failed for token: " + token);
			}
		}
		ServiceContext serviceContext = tokenValidationServiceStub._getServiceClient()
				.getLastOperationContext().getServiceContext();
		cookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
		authenticationInfo.setAuthenticated(authenticated);
		return authenticationInfo;
	}


	private String inputStreamToString(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buff = new byte[1024];
		int i;
		while ((i = in.read(buff)) > 0) {
			out.write(buff, 0, i);
		}
		out.close();
		return out.toString();
	}

	@Override
	protected void doPost(HttpServletRequest req,
						  HttpServletResponse res) throws IOException {

		String data = this.inputStreamToString(req.getInputStream());
		if (data == null) {
			log.warn("Event Object is empty/null");
			return;
		}
		AuthenticationInfo authenticationInfo = null;
		if (exposedTransports.equalsIgnoreCase(HTTPEventAdapterConstants.HTTPS)) {
			if (!req.isSecure()) {
				res.setStatus(403);
				log.error("Only Secured endpoint is enabled for requests");
				return;
			} else {
				authenticationInfo = this.checkAuthentication(req);
				int tenantId = authenticationInfo != null ? authenticationInfo.getTenantId() : -1;
				if (tenantId == -1) {
					res.getOutputStream().write(AUTH_FAILURE_RESPONSE.getBytes());
					res.setStatus(401);
					log.error("Authentication failed for the request");
					return;
				} else if (tenantId != this.tenantId) {
					res.getOutputStream().write(AUTH_FAILURE_RESPONSE.getBytes());
					res.setStatus(401);
					log.error("Authentication failed for the request");
					return;
				}
			}
		} else if (exposedTransports.equalsIgnoreCase(HTTPEventAdapterConstants.HTTP)) {
			if (req.isSecure()) {
				res.setStatus(403);
				log.error("Only unsecured endpoint is enabled for requests");
				return;
			}
		} else {
			if (req.isSecure()) {
				authenticationInfo = this.checkAuthentication(req);
				int tenantId = authenticationInfo != null ? authenticationInfo.getTenantId() : -1;
				if (tenantId == -1) {
					res.getOutputStream().write(AUTH_FAILURE_RESPONSE.getBytes());
					res.setStatus(401);
					log.error("Authentication failed for the request");
					return;
				} else if (tenantId != this.tenantId) {
					res.getOutputStream().write(AUTH_FAILURE_RESPONSE.getBytes());
					res.setStatus(401);
					log.error("Authentication failed for the request");
					return;
				}
			}

		}

		if (log.isDebugEnabled()) {
			log.debug("Message : " + data);
		}

		if (authenticationInfo != null) {
			Map<String, String> paramMap = new HashMap<>();
			Enumeration<String> reqParameterNames = req.getParameterNames();
			while (reqParameterNames.hasMoreElements()) {
				paramMap.put(reqParameterNames.nextElement(), req.getParameter(reqParameterNames.nextElement()));
			}
			paramMap.put(HTTPEventAdapterConstants.USERNAME_TAG, authenticationInfo.getUsername());
			paramMap.put(HTTPEventAdapterConstants.TENANT_DOMAIN_TAG, authenticationInfo.getTenantDomain());
			paramMap.put(HTTPEventAdapterConstants.PAYLOAD_TAG, data);
			ContentValidator contentValidator = new HTTPContentValidator();
			ContentInfo contentInfo = contentValidator.validate(paramMap);
			if (contentInfo != null && contentInfo.isValidContent()) {
				HTTPEventAdapter.executorService.submit(new HTTPRequestProcessor(eventAdaptorListener,
																				 contentInfo.getMsgText(), tenantId));

			}

		}

	}

	@Override
	protected void doGet(HttpServletRequest req,
						 HttpServletResponse res) throws IOException {
		doPost(req, res);
	}

	public class HTTPRequestProcessor implements Runnable {

		private InputEventAdapterListener inputEventAdapterListener;
		private String payload;
		private int tenantId;

		public HTTPRequestProcessor(InputEventAdapterListener inputEventAdapterListener,
									String payload, int tenantId) {
			this.inputEventAdapterListener = inputEventAdapterListener;
			this.payload = payload;
			this.tenantId = tenantId;
		}

		public void run() {
			try {
				PrivilegedCarbonContext.startTenantFlow();
				PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);

				if (log.isDebugEnabled()) {
					log.debug("Event received in HTTP Event Adapter - " + payload);
				}

				if (payload.trim() != null) {
					inputEventAdapterListener.onEvent(payload);
				} else {
					log.warn("Dropping the empty/null event received through http adapter");
				}
			} catch (Exception e) {
				log.error("Error while parsing http request for processing: " + e.getMessage(), e);
			} finally {
				PrivilegedCarbonContext.endTenantFlow();
			}
		}

	}

}
