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

package org.wso2.agent.main;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.json.JSONObject;
import org.wso2.agent.common.Common;
import org.wso2.agent.config.Configuration;
import org.wso2.agent.datasense.PushSystemUsage;
import org.wso2.agent.resourcemonitor.Monitor;
import org.wso2.agent.resourcemonitor.pojo.SystemSpec;
import org.wso2.agent.resourcemonitor.pojo.SystemUsage;
import org.wso2.agent.store.RecordStore;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class JavaApp {

    final static int TIMEINTERVAL = 10000;//ms
    public static String SUPASSWORD = "";
    static int TIMECOUNTER = 0;// 5 minute interval sender tracker;
    public static String deviceid;

    public static void main(String args[]) {

        System.out.println("Enter Super User Password");
        BufferedReader br = null;
        try {
            Console console = System.console();
            //String username = console.readLine("Username: ");
            char[] password = console.readPassword("Password: ");
            JavaApp.SUPASSWORD = String.valueOf(password);

            //br = new BufferedReader(new InputStreamReader(System.in));
            //JavaApp.SUPASSWORD = String.valueOf(br.readLine());
        } catch (Exception e) {
            System.out.println("Retrieving Password Failed!");
        } finally {
            try {
                br.close();
            } catch (Exception e) {
            }
        }

        if (!new Configuration().checkRegistered()) {
            SystemSpec spec = new Monitor().getSysInfo(JavaApp.SUPASSWORD);
            String devicetoken = new String();
            String devicerefreshtoken = new String();
            Configuration config = new Configuration();
            try {
                String token = String.valueOf(config.getInitProperty("auth-token"));
                String refreshtoken = String.valueOf(config.getInitProperty("refresh-token"));
                String device_name = String.valueOf(config.getInitProperty("device-name"));
                Client client = Client.create(Common.configureClient());
                client.setConnectTimeout(50000);
				String URL= String.valueOf(new Configuration().getInitProperty("https-ep"));
                WebResource webResource = client.resource(URL+"/CONNECTEDLAP/device/register");
                String mac = spec.getMacaddress().replaceAll("\\:", "");
                deviceid = mac;

                String request = "{"
                        + "\"owner\":\"admin\","
                        + "\"deviceId\":\"" + mac + "\","
                        + "\"deviceName\": \"" + device_name + "\","
                        + "\"discSpace\":" + spec.getStorage() + ","
                        + "\"memory\":" + spec.getRamsize() + ","
                        + "\"cpuInfo\":\"" + spec.getProcessor_speed() + "\","
                        + "\"networkType\":\"" + spec.getNetworktype() + "\","
                        + "\"cpucorecount\":\"" + spec.getProcessor_cores() + "\""
                        + "}";

                ClientResponse response = (ClientResponse) webResource.type("application/json").header("Authorization", "Bearer " + token).post(ClientResponse.class, request);
                System.out.println("Output from Server .... \n");
                String output = (String) response.getEntity(String.class);
                System.out.println(output);
                JSONObject outputJson = new JSONObject(output);
                devicetoken = outputJson.getString("accessToken");
                devicerefreshtoken = outputJson.getString("refreshToken");
                if (response.getStatus() != 200) {
                 //   throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
                }
                config.SaveRegistration(spec, devicetoken, devicerefreshtoken);
            } catch (Exception e) {
                //e.printStackTrace();
                //throw e;
            }

        }
        //String deviceid=String.valueOf(new  Configuration().getProperty("deviceid"));
        String owner = String.valueOf(new Configuration().getInitProperty("owner"));
        String devicetoken = String.valueOf(new Configuration().getProperty("devicetoken"));
        String devicerefreshtoken = String.valueOf(new Configuration().getProperty("devicerefreshtoken"));

        while (true) {
            try {
                new RecordStore().saveData(new Monitor().getSysUsage(JavaApp.SUPASSWORD));
                JavaApp.TIMECOUNTER += TIMEINTERVAL;
                if (JavaApp.TIMECOUNTER >= 10000) {
                    ArrayList<SystemUsage> sysUsageList = new RecordStore().readData();
                    for (SystemUsage systemUsage : sysUsageList) {
                        new PushSystemUsage().pushData(systemUsage, deviceid, owner, devicetoken);
                    }
                    File file = new File("usagelog");
                    if (file.delete()) {
                        System.out.println(file.getName() + " is deleted!");
                    } else {
                        System.out.println("Delete operation is failed.");
                    }
                }
                Thread.sleep(JavaApp.TIMEINTERVAL);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
