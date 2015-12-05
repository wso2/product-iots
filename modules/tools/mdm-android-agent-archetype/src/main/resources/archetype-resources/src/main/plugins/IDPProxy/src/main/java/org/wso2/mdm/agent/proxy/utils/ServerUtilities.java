/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.mdm.agent.proxy.utils;

import android.util.Log;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import org.wso2.mdm.agent.proxy.IDPTokenManagerException;
import org.wso2.mdm.agent.proxy.IdentityProxy;
import org.wso2.mdm.agent.proxy.R;
import org.wso2.mdm.agent.proxy.beans.EndPointInfo;
import org.wso2.mdm.agent.proxy.utils.Constants.HTTP_METHODS;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 * Handle network communication between SDK and authorization server
 */
public class ServerUtilities {
	private final static String TAG = "ServerUtilities";
	private static final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss",
	                                                                  Locale.getDefault());

	/**
	 * Validate the token expiration date.
	 *
	 * @param expirationDate - Token expiration date.
	 * @return - Token status.
	 */
	public static boolean isValid(Date expirationDate) {
		Date currentDate = new Date();
		String formattedDate = dateFormat.format(currentDate);
		currentDate = convertDate(formattedDate);

		boolean isExpired = currentDate.after(expirationDate);
		boolean isEqual = currentDate.equals(expirationDate);
		if (isExpired == true || isEqual == true) {
			return true;
		}

		return false;
	}

	/**
	 * Convert the date to the standard format.
	 *
	 * @param date - Date as a string.
	 * @return - Formatted date.
	 */
	public static Date convertDate(String date) {
		Date receivedDate = null;
		try {
			receivedDate = dateFormat.parse(date);
		} catch (ParseException e) {
			Log.e(TAG, "Invalid date format." + e);
		}

		return receivedDate;
	}

	public static String buildPayload(Map<String, String> params) {
		StringBuilder bodyBuilder = new StringBuilder();
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String> param = iterator.next();
			bodyBuilder.append(param.getKey()).append('=').append(param.getValue());
			if (iterator.hasNext()) {
				bodyBuilder.append('&');
			}
		}
		
		return bodyBuilder.toString();
	}

	public static HttpRequestBase buildHeaders(HttpRequestBase httpRequestBase,
	                                           Map<String, String> headers, HTTP_METHODS httpMethod) {
		Iterator<Entry<String, String>> iterator = headers.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String> header = iterator.next();
			httpRequestBase.setHeader(header.getKey(), header.getValue());
		}
		return httpRequestBase;
	}

	public static Map<String, String> postData(EndPointInfo apiUtilities,
	                                           Map<String, String> headers)
			throws IDPTokenManagerException {

		HTTP_METHODS httpMethod = apiUtilities.getHttpMethod();
		String url = apiUtilities.getEndPoint();
		JSONObject params = apiUtilities.getRequestParams();
		Map<String, String> responseParams = new HashMap<String, String>();
		
		switch (httpMethod) {
			case GET:
				responseParams = sendGetRequest(url, params, headers);
				break;
			case POST:
				responseParams = sendPostRequest(url, params, headers);
				break;
		}

		return responseParams;

	}
	
	public static Map<String, String> sendGetRequest(String url, JSONObject params, Map<String, String> headers) throws IDPTokenManagerException{
		HttpGet httpGet = new HttpGet(url);
		HttpGet httpGetWithHeaders = (HttpGet) buildHeaders(httpGet, headers, HTTP_METHODS.GET);
		HttpClient httpClient = getCertifiedHttpClient();
		Map<String, String> responseParams = new HashMap<String, String>();

		try {
			HttpResponse response = httpClient.execute(httpGetWithHeaders);
			responseParams.put(Constants.SERVER_RESPONSE_BODY, getResponseBody(response));
			responseParams.put(Constants.SERVER_RESPONSE_STATUS,
			                   String.valueOf(response.getStatusLine().getStatusCode()));
		} catch (ClientProtocolException e) {
			throw new IDPTokenManagerException("Invalid client protocol.", e);
		} catch (IOException e) {
			responseParams.put(Constants.SERVER_RESPONSE_BODY, "Internal Server Error");
			responseParams.put(Constants.SERVER_RESPONSE_STATUS,
			                   Constants.INTERNAL_SERVER_ERROR);
			throw new IDPTokenManagerException("Server connectivity failure.", e);
		}
		
		return responseParams;
	}
	
	public static Map<String, String> sendPostRequest(String url, JSONObject params, Map<String, String> headers) throws IDPTokenManagerException{
		HttpPost httpPost = new HttpPost(url);
		HttpClient httpClient = getCertifiedHttpClient();
		Map<String, String> responseParams = new HashMap<String, String>();
		
		if (params != null) {
			try {
				httpPost.setEntity(new StringEntity(params.toString()));
			} catch (UnsupportedEncodingException e) {
				throw new IDPTokenManagerException("Invalid encoding type.", e);
			}
		}

		HttpPost httpPostWithHeaders = (HttpPost) buildHeaders(httpPost, headers, HTTP_METHODS.POST);
		try {
			HttpResponse response = httpClient.execute(httpPostWithHeaders);
			String status = String.valueOf(response.getStatusLine().getStatusCode());

			responseParams.put(Constants.SERVER_RESPONSE_BODY, getResponseBody(response));
			responseParams.put(Constants.SERVER_RESPONSE_STATUS, status);

		} catch (ClientProtocolException e) {
			throw new IDPTokenManagerException("Invalid client protocol.", e);
		} catch (IOException e) {
			responseParams.put(Constants.SERVER_RESPONSE_BODY, "Internal Server Error");
			responseParams.put(Constants.SERVER_RESPONSE_STATUS,
			                   Constants.INTERNAL_SERVER_ERROR);
			throw new IDPTokenManagerException("Server connectivity failure.", e);
		}
		
		return responseParams;
	}

	public static Map<String, String> postDataAPI(EndPointInfo apiUtilities,
	                                              Map<String, String> headers)
			throws IDPTokenManagerException {
		HTTP_METHODS httpMethod = apiUtilities.getHttpMethod();
		String url = apiUtilities.getEndPoint();
		Map<String, String> params = apiUtilities.getRequestParamsMap();

		Map<String, String> responseParams = new HashMap<String, String>();
		HttpClient httpclient = getCertifiedHttpClient();
		String payload = buildPayload(params);
		
		if (httpMethod.equals(HTTP_METHODS.POST)) {		
			HttpPost httpPost = new HttpPost(url);
			HttpPost httpPostWithHeaders = (HttpPost) buildHeaders(httpPost, headers, httpMethod);
			byte[] postData = payload.getBytes();
			try {
				httpPostWithHeaders.setEntity(new ByteArrayEntity(postData));
				HttpResponse response = httpclient.execute(httpPostWithHeaders);
				String status = String.valueOf(response.getStatusLine().getStatusCode());

				responseParams.put(Constants.SERVER_RESPONSE_BODY, getResponseBody(response));
				responseParams.put(Constants.SERVER_RESPONSE_STATUS, status);
				return responseParams;
			} catch (ClientProtocolException e) {
				throw new IDPTokenManagerException("Invalid client protocol.", e);
			} catch (IOException e) {
				responseParams.put(Constants.SERVER_RESPONSE_BODY, "Internal Server Error");
				responseParams.put(Constants.SERVER_RESPONSE_STATUS,
				                   Constants.INTERNAL_SERVER_ERROR);
				throw new IDPTokenManagerException("Server connectivity failure.", e);
			}
		}

		return responseParams;
	}

	public static HttpClient getCertifiedHttpClient() throws IDPTokenManagerException {
		HttpClient client = null;
		InputStream inStream = null;
		try {
			if (Constants.SERVER_PROTOCOL.equalsIgnoreCase("https://")) {
				KeyStore localTrustStore = KeyStore.getInstance("BKS");
				inStream =
						IdentityProxy.getInstance().getContext().getResources()
						             .openRawResource(R.raw.emm_truststore);
				localTrustStore.load(inStream, Constants.TRUSTSTORE_PASSWORD.toCharArray());

				SchemeRegistry schemeRegistry = new SchemeRegistry();
				schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(),
				                                   80));
				SSLSocketFactory sslSocketFactory = new SSLSocketFactory(localTrustStore);
				sslSocketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
				schemeRegistry.register(new Scheme("https", sslSocketFactory, 443));
				HttpParams params = new BasicHttpParams();
				ClientConnectionManager connectionManager =
						new ThreadSafeClientConnManager(params,
						                                schemeRegistry);

				client = new DefaultHttpClient(connectionManager, params);

			} else {
				client = new DefaultHttpClient();
			}

		} catch (KeyStoreException e) {
			throw new IDPTokenManagerException("Invalid keystore.", e);
		} catch (CertificateException e) {
			throw new IDPTokenManagerException("Invalid certificate.", e);
		} catch (NoSuchAlgorithmException e) {
			throw new IDPTokenManagerException("Keystore algorithm does not match.");
		} catch (UnrecoverableKeyException e) {
			throw new IDPTokenManagerException("Invalid keystore.", e);
		} catch (KeyManagementException e) {
			throw new IDPTokenManagerException("Invalid keystore.", e);
		} catch (IOException e) {
			throw new IDPTokenManagerException("Trust store failed to load.", e);
		} finally {
			StreamHandlerUtil.closeInputStream(inStream, TAG);
		}

		return client;
	}

	public static String getResponseBody(HttpResponse response) throws IDPTokenManagerException {

		String responseBody = null;
		HttpEntity entity = null;

		try {
			entity = response.getEntity();
			responseBody = getResponseBodyContent(entity);
		} catch (ParseException e) {
			throw new IDPTokenManagerException("Invalid response.", e);
		} catch (IOException e) {
			if (entity != null) {
				try {
					entity.consumeContent();
				} catch (IOException ex) {
					throw new IDPTokenManagerException("HTTP Response failure.", e);
				}
			}
		}

		return responseBody;
	}

	public static String getResponseBodyContent(final HttpEntity entity) throws IOException,
	                                                                            ParseException {

		InputStream instream = entity.getContent();

		if (entity.getContentLength() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("HTTP entity too large to be buffered in memory.");
		}

		String charset = getContentCharSet(entity);

		if (charset == null) {
			charset = HTTP.DEFAULT_CONTENT_CHARSET;
		}

		Reader reader = new InputStreamReader(instream, charset);
		StringBuilder buffer = new StringBuilder();

		try {
			char[] bufferSize = new char[1024];
			int length;

			while ((length = reader.read(bufferSize)) != -1) {
				buffer.append(bufferSize, 0, length);
			}
		} finally {
			reader.close();
		}

		return buffer.toString();

	}

	public static String getContentCharSet(final HttpEntity entity) throws ParseException {

		String charSet = null;

		if (entity.getContentType() != null) {
			HeaderElement values[] = entity.getContentType().getElements();

			if (values.length > 0) {
				NameValuePair param = values[0].getParameterByName("charset");

				if (param != null) {
					charSet = param.getValue();
				}
			}
		}

		return charSet;

	}

}
