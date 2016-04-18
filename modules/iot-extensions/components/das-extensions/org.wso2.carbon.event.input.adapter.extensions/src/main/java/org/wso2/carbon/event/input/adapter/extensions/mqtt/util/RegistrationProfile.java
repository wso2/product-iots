package org.wso2.carbon.event.input.adapter.extensions.mqtt.util;

/**
 * This class represents the data that are required to register
 * the oauth application.
 */
public class RegistrationProfile {

	private String callbackUrl;
	private String clientName;
	private String tokenScope;
	private String owner;
	private String grantType;
	private String applicationType;

	private static final String TAG = RegistrationProfile.class.getSimpleName();

	public String getCallbackUrl() {
		return callbackUrl;
	}

	public void setCallbackUrl(String callBackUrl) {
		this.callbackUrl = callBackUrl;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getTokenScope() {
		return tokenScope;
	}

	public void setTokenScope(String tokenScope) {
		this.tokenScope = tokenScope;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getGrantType() {
		return grantType;
	}

	public void setGrantType(String grantType) {
		this.grantType = grantType;
	}

	public String getApplicationType() {
		return applicationType;
	}

	public void setApplicationType(String applicationType) {
		this.applicationType = applicationType;
	}

	public String toJSON() {
		String jsonString =
				"{\"callbackUrl\": \"" + callbackUrl + "\",\"clientName\": \"" + clientName + "\", \"tokenScope\": " +
						"\"" + tokenScope + "\", \"owner\": \"" + owner + "\"," + "\"grantType\": \"" + grantType +
						"\", \"saasApp\" :false }\n";
		return jsonString;
	}
}