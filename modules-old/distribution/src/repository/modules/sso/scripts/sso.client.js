/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
/**
 * Following module act as a client to create a saml request and also to
 * unwrap and return attributes of a returning saml response
 * @type {{}}
 */

var client = {};

(function (client) {

	var Util = Packages.org.jaggeryjs.modules.sso.common.util.Util,
		carbon = require('carbon'),
		log = new Log();

	/**
	 * obtains an encoded saml response and return a decoded/unmarshalled saml obj
	 * @param samlResp
	 * @return {*}
	 */
	client.getSamlObject = function (samlResp) {
		var decodedResp = Util.decode(samlResp);
		return Util.unmarshall(decodedResp);
	};

	/**
	 * validating the signature of the response saml object
	 */
	client.validateSignature = function (samlObj, config) {
		var tDomain = Util.getDomainName(samlObj);
		var tId = carbon.server.tenantId({domain: tDomain});

		return Util.validateSignature(samlObj,
			config.KEY_STORE_NAME, config.KEY_STORE_PASSWORD, config.IDP_ALIAS, tId, tDomain);
	};

	/**
	 * Checking if the request is a logout call
	 */
	client.isLogoutRequest = function (samlObj) {
		return samlObj instanceof Packages.org.opensaml.saml2.core.LogoutRequest;
	};

	/**
	 * Checking if the request is a logout call
	 */
	client.isLogoutResponse = function (samlObj) {
		return samlObj instanceof Packages.org.opensaml.saml2.core.LogoutResponse;
	};

	/**
	 * getting url encoded saml authentication request
	 * @param issuerId
	 */
	client.getEncodedSAMLAuthRequest = function (issuerId) {
		return Util.encode(
			Util.marshall(
				new Packages.org.jaggeryjs.modules.sso.common.builders.AuthReqBuilder().buildAuthenticationRequest(issuerId)
			));
	};

	/**
	 * get url encoded saml logout request
	 */
	client.getEncodedSAMLLogoutRequest = function (user, sessionIndex, issuerId) {
		return Util.encode(
			Util.marshall(
				new Packages.org.jaggeryjs.modules.sso.common.builders.LogoutRequestBuilder().buildLogoutRequest(user, sessionIndex,
					Packages.org.jaggeryjs.modules.sso.common.constants.SSOConstants.LOGOUT_USER,
					issuerId)));
	};

	/**
	 * Reads the returning SAML login response and populates a session info object
	 */
	client.decodeSAMLLoginResponse = function (samlObj, samlResp, sessionId) {
		var samlSessionObj = {
			// sessionId, loggedInUser, sessionIndex, samlToken
		};

		if (samlObj instanceof Packages.org.opensaml.saml2.core.Response) {

			var assertions = samlObj.getAssertions();

			// extract the session index
			if (assertions != null && assertions.size() > 0) {
				var authenticationStatements = assertions.get(0).getAuthnStatements();
				var authnStatement = authenticationStatements.get(0);
				if (authnStatement != null) {
					if (authnStatement.getSessionIndex() != null) {
						samlSessionObj.sessionIndex = authnStatement.getSessionIndex();
					}
				}
			}

			// extract the username
			if (assertions != null && assertions.size() > 0) {
				var subject = assertions.get(0).getSubject();
				if (subject != null) {
					if (subject.getNameID() != null) {
						samlSessionObj.loggedInUser = subject.getNameID().getValue();
					}
				}
			}
			samlSessionObj.sessionId = sessionId;
			samlSessionObj.samlToken = samlResp;
		}

		return samlSessionObj;
	};

	/**
	 * This method is to get the session index when a single logout happens
	 * The IDP sends a logout request to the ACS with the session index, so that
	 * the app can invalidate the associated HTTP Session
	 */
	client.decodeSAMLLogoutRequest = function (samlObj) {
		var sessionIndex = null;

		if (samlObj instanceof org.opensaml.saml2.core.LogoutRequest) {
			var sessionIndexes = samlObj.getSessionIndexes();
			if (sessionIndexes != null && sessionIndexes.size() > 0) {
				sessionIndex = sessionIndexes.get(0).getSessionIndex();
			}
		}

		return sessionIndex;

	};

}(client));