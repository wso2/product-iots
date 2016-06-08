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

package org.wso2.agent.datasense;

import java.io.IOException;

import org.json.JSONObject;
import org.wso2.agent.common.Common;
import org.wso2.agent.resourcemonitor.pojo.SystemUsage;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;

public class PushSystemUsage {

    public void pushData(SystemUsage usage, String deviceid, String owner, String devicetoken) throws IOException {   
    String output = "";
    	try {
			Client client = Client.create(Common.configureClient());
			client.setConnectTimeout(50000);
			WebResource webResource = client.resource("https://localhost:9443/connected-lap-agent/push_data");
			Form form = new Form();
			form.add("deviceId", deviceid);
			form.add("deviceOwner", owner);

			form.add("batteryusage", usage.getBattery_percentage());
			form.add("chargerpluggedin", usage.getBattery_pluggedin());
			form.add("cpuusage", usage.getProcessor_usage());
			form.add("networktraffic", usage.getNetwork_traffic());
			form.add("memoryusage", usage.getMemory_usage());
			form.add("harddiskusage", usage.getStorage_usage());

			form.add("tenantDomain", "carbon.super");
			form.add("token", devicetoken);

			ClientResponse response = (ClientResponse) webResource.accept("application/json").post(ClientResponse.class, form);
			System.out.println("Output from Server .... \n");
			output = (String) response.getEntity(String.class);
			System.out.println(output);
			JSONObject outputjson= new JSONObject(output);			
			if (response.getStatus() != 200 || outputjson ==null || !outputjson.getBoolean("success")) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
			}
    	}
    	catch (Exception e) {
    		throw e;
    	}
    }
}
