/**
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
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
**/

#include "CurrentSensor.h"
/**********************************************************************************************
 Use the below variables when required to set a static IP for the WifiSheild
 ***********************************************************************************************/
  
uint32_t ip, ddns, ssubnet, ggateway;
String connecting = "connecting.... ";

void connectHttp() {
    /* Initialise the module */
    if(DEBUG) Serial.println(F("\nInitializing..."));
    if (!cc3000.begin())
    {
        if(DEBUG) Serial.println(F("Couldn't begin()! Check your wiring?"));
        while(1);
    }

    /**********************************************************************************************
     Only required if using static IP for the WifiSheild
     ***********************************************************************************************/
      
      ip = cc3000.IP2U32(deviceIP[0], deviceIP[1], deviceIP[2], deviceIP[3]);
      ddns = cc3000.IP2U32(dns2[0], dns2[1], dns2[2], dns2[3]);
      ssubnet = cc3000.IP2U32(subnet[0], subnet[1], subnet[2], subnet[3]);
      ggateway = cc3000.IP2U32(gateway[0], gateway[1], gateway[2], gateway[3]);
      cc3000.setStaticIPAddress(ip, ssubnet, ggateway, ddns);            // required for setting static IP
    
    /***********************************************************************************************/

    
    sserver = cc3000.IP2U32(server[0], server[1], server[2], server[3]);
    
    if(CON_DEBUG) {
        Serial.print(F("\nAttempting to connect to "));
        Serial.println(WLAN_SSID);
    }

    cc3000.deleteProfiles();
    
    if (!cc3000.connectToAP(WLAN_SSID, WLAN_PASS, WLAN_SECURITY)) {
        if(CON_DEBUG) Serial.println(F("Failed!"));
        while(1);
    }
    
    if(CON_DEBUG) Serial.println(F("Connected to Wifi network!"));
    
    if(CON_DEBUG) Serial.println(F("Request DHCP"));
    while (!cc3000.checkDHCP())
    {
        delay(100);
    }
    
    /* Display the IP address DNS, Gateway, etc. */
    while (! displayConnectionDetails()) {
        delay(1000);
    }

    if (cc3000.checkConnected()) {
        Serial.println("client Connected to AP");
        client = cc3000.connectTCP(sserver, SERVICE_PORT);
        if (client.connected()) {
          if(CON_DEBUG) Serial.println("client Connected to server");
        } else {
          if(CON_DEBUG) Serial.println(F("client Connection failed"));
        }
      } else {
        Serial.println(F("client Connection to AP failed"));
      }

    if(CON_DEBUG) Serial.println(F("-------------------------------------"));
}


void setupResource(){
    String hostIP = getHostIP(server);
    String port = String(SERVICE_PORT);
    
    host = "Host: " + hostIP + ":" + port;
    if(DEBUG) Serial.println(host);
    
    jsonPayLoad = "{\"owner\":\"";
    jsonPayLoad += String(DEVICE_OWNER);
    jsonPayLoad += "\",\"deviceId\":\"";
    jsonPayLoad += String(DEVICE_ID);
    jsonPayLoad += "\",\"reply\":\"";
    
    if(DEBUG) {
        Serial.print("JSON Payload: ");
        Serial.println(jsonPayLoad);
        Serial.println("-------------------------------");
    }
}

String getHostIP(byte server[4]){
    String hostIP = String(server[0]);
    
    for ( int index = 1; index < 4; index++) {
        hostIP += "." + String(server[index]);
    }
    
    return hostIP;
}


bool displayConnectionDetails(void)
{
    uint32_t ipAddress, netmask, gateway, dhcpserv, dnsserv;
    
    if(!cc3000.getIPAddress(&ipAddress, &netmask, &gateway, &dhcpserv, &dnsserv))
    {
        if(DEBUG) Serial.println(F("Unable to retrieve the IP Address!\r\n"));
        return false;
    }
    else
    {
        if(CON_DEBUG) {
            Serial.print(F("\nIP Addr: ")); cc3000.printIPdotsRev(ipAddress);
            Serial.print(F("\nNetmask: ")); cc3000.printIPdotsRev(netmask);
            Serial.print(F("\nGateway: ")); cc3000.printIPdotsRev(gateway);
            Serial.print(F("\nDHCPsrv: ")); cc3000.printIPdotsRev(dhcpserv);
            Serial.print(F("\nDNSserv: ")); cc3000.printIPdotsRev(dnsserv);
            Serial.println();
        }
        return true;
    }
}

void setupClient(){
    client = cc3000.connectTCP(sserver, SERVICE_PORT);  //SERVICE_PORT
    if (client.connected()) {
        if(CON_DEBUG) Serial.println("client Connected to server");
    } else {
        while( !cc3000.checkConnected() ){
        connectHttp();
    
    }
        if(CON_DEBUG) Serial.println(F("client Connection failed"));
    }
}
