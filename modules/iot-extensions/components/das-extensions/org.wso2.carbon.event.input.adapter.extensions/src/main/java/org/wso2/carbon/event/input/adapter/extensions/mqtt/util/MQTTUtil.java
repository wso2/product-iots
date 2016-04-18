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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.event.input.adapter.extensions.mqtt.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.jwt.client.extension.service.JWTClientManagerService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * This is the utility class that is used for MQTT input adapater.
 */
public class MQTTUtil {
	private static final String HTTPS_PROTOCOL = "https";
	private static final Log log = LogFactory.getLog(MQTTUtil.class);
	/**
	 * Return a http client instance
	 *
	 * @param protocol- service endpoint protocol http/https
	 * @return
	 */
	public static HttpClient getHttpClient(String protocol)
			throws IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
		HttpClient httpclient;
		if (HTTPS_PROTOCOL.equals(protocol)) {
			SSLContextBuilder builder = new SSLContextBuilder();
			builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
			httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
		} else {
			httpclient = HttpClients.createDefault();
		}
		return httpclient;
	}

	public static String getResponseString(HttpResponse httpResponse) throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
			String readLine;
			String response = "";
			while (((readLine = br.readLine()) != null)) {
				response += readLine;
			}
			return response;
		} finally {
			EntityUtils.consumeQuietly(httpResponse.getEntity());
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					log.warn("Error while closing the connection! " + e.getMessage());
				}
			}
		}
	}

	public static JWTClientManagerService getJWTClientManagerService() {
		PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
		JWTClientManagerService jwtClientManagerService =
				(JWTClientManagerService) ctx.getOSGiService(JWTClientManagerService.class, null);
		if (jwtClientManagerService == null) {
			String msg = "JWT management service has not initialized.";
			log.error(msg);
			throw new IllegalStateException(msg);
		}
		return jwtClientManagerService;
	}
}
