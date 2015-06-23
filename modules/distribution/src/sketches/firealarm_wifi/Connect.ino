
/**********************************************************************************************
 Use the below variables when required to set a static IP for the WifiSheild
 ***********************************************************************************************/
//  byte dns2[] = { 8, 8, 8, 8 };
//  byte subnet[] = { 255, 255, 255, 0 };
//  byte gateway[] = { 10, 100, 9, 254 };
//  byte deviceIP[4] = { 10, 100, 9, 9 };
//  byte gateway[] = { 192, 168, 1, 1 };
//  byte deviceIP[4] = { 192, 168, 1, 219 };

//  uint32_t ip, ddns, ssubnet, ggateway;

 
//mac - c0:4a:00:1a:08:da
//      c0:4a:00:1a:03:f8
//      b8:27:eb:88:37:7a


void connectHttp() {
    /* Initialise the module */
    if(DEBUG) Serial.println(F("\nInitializing..."));
    if (!cc3000.begin())
    {
        if(DEBUG) Serial.println(F("Couldn't begin()! Check your wiring?"));
        while(1);
    }
    
     cc3000.setMacAddress(mac);         //  Set your own mac and print it to re-check
    //    uint8_t address[6];
    //    cc3000.getMacAddress(address);
    //    if(DEBUG){
    //      Serial.print(address[0], HEX); Serial.print(":");
    //      Serial.print(address[1], HEX); Serial.print(":");
    //      Serial.print(address[2], HEX); Serial.print(":");
    //      Serial.print(address[3], HEX); Serial.print(":");
    //      Serial.print(address[4], HEX); Serial.print(":");
    //      Serial.println(address[5], HEX);
    //    }
    //  }
    
    /**********************************************************************************************
     Only required if using static IP for the WifiSheild
     ***********************************************************************************************/
    
    //  ip = cc3000.IP2U32(deviceIP[0], deviceIP[1], deviceIP[2], deviceIP[3]);
    //  ddns = cc3000.IP2U32(dns2[0], dns2[1], dns2[2], dns2[3]);
    //  ssubnet = cc3000.IP2U32(subnet[0], subnet[1], subnet[2], subnet[3]);
    //  ggateway = cc3000.IP2U32(gateway[0], gateway[1], gateway[2], gateway[3]);
    //  cc3000.setStaticIPAddress(ip, ssubnet, ggateway, ddns);            // required for setting static IP
    
    /***********************************************************************************************/
    
    sserver = cc3000.IP2U32(server[0], server[1], server[2], server[3]);
    
    if(CON_DEBUG) {
        Serial.print(F("\nAttempting to connect to "));
        Serial.println(WLAN_SSID);
    }
    
    if (!cc3000.connectToAP(WLAN_SSID, WLAN_PASS, WLAN_SECURITY)) {
        if(CON_DEBUG) Serial.println(F("Failed!"));
        while(1);
    }
    
    if(CON_DEBUG) Serial.println(F("Connected to Wifi network!"));
    
    if(true) Serial.println(F("Request DHCP"));
    while (!cc3000.checkDHCP())
    {
        delay(100); // ToDo: Insert a DHCP timeout!
    }
    
    /* Display the IP address DNS, Gateway, etc. */
    while (! displayConnectionDetails()) {
        delay(1000);
    }
    
    pushClient = cc3000.connectTCP(sserver, SERVICE_PORT);  //SERVICE_PORT
    if (pushClient.connected()) {
        if(true) Serial.println(F("pushClient Connected to server"));
    } else {
        if(CON_DEBUG) Serial.println(F("pushClient Connection failed"));
    }
    
    
    
    
    if(CON_DEBUG) Serial.println(F("-------------------------------------"));
}


void setupResource(){
    String hostIP = getHostIP(server);
    String port = String(SERVICE_PORT);
//    
    host = "Host: " + hostIP + ":" + port;
//    if(DEBUG) Serial.println(host);
    
    jsonPayLoad = "{\"owner\":\"";
    jsonPayLoad += DEVICE_OWNER;
    jsonPayLoad += "\",\"deviceId\":\"";
    jsonPayLoad += DEVICE_ID;
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
    uint32_t  netmask, gateway, dhcpserv, dnsserv;
    
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
    pushClient = cc3000.connectTCP(sserver, SERVICE_PORT);  //SERVICE_PORT
    if (pushClient.connected()) {
        if(CON_DEBUG) Serial.println(F("client Connected to server"));
    } else {
        while( !cc3000.checkConnected() ){
        connectHttp();
    
    }
        if(CON_DEBUG) Serial.println(F("client Connection failed"));
    }
}
