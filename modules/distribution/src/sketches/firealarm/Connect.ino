byte mac[6] = { 0x90, 0xA2, 0xDA, 0x0D, 0x30, 0xD7};  //mac - 90a2da0d30d7
byte dns2[] = { 8, 8, 8, 8 };
byte subnet[] = { 255, 255, 255, 0 };
byte gateway[] = { 192, 168, 1, 1 };

byte deviceIP[4] = { 192, 168, 1, 219 };
byte server[4] = { 192, 168, 1, 216 };

String connecting = "connecting.... ";

void connectHttp() {
//  Serial.println("-------------------------------");
  
  Ethernet.begin(mac, deviceIP, dns2, gateway, subnet);
  delay(2000);
  
//  Serial.print("My IP: ");
//  Serial.println(Ethernet.localIP());
  
  connecting += httpClient.connect(server, SERVICE_PORT);
  delay(2000);
//  Serial.println(connecting);
  
  if (httpClient.connected()) {
//    Serial.println("connected");
  } else {
//    Serial.println("connection failed");
   
    while(!httpClient.connected()){
//      Serial.println("retrying to connect......");
      httpClient.connect(server, SERVICE_PORT);
      delay(2000);
    }
    
//    Serial.println("connected to server!");
  }
//  Serial.println("-------------------------------");
}


void setupResource(){ 
  String hostIP = getHostIP(server);
  String port = String(SERVICE_PORT);
  
  host = "Host: " + hostIP + ":" + port;      
//  Serial.println(host);
  
  jsonPayLoad = String(OWNER_JSON);
  jsonPayLoad += String(DEVICE_OWNER);
  jsonPayLoad += String(DEVICE_ID_JSON);
  jsonPayLoad += String(DEVICE_ID);
  jsonPayLoad += String(REPLY_JSON);

//  Serial.print("JSON Payload: ");
//  Serial.println(jsonPayLoad);
//  Serial.println("-------------------------------");
}


String getMyIP(){
  String myIP = "";
  myIP = String(Ethernet.localIP()[0]);
  
  for ( int index = 1; index < 4; index++) {
    myIP += "." + String(Ethernet.localIP()[index]);
  }  
  return myIP;
}


String getHostIP(byte server[4]){
  String hostIP = String(server[0]);
  
  for ( int index = 1; index < 4; index++) {
    hostIP += "." + String(server[index]);
  }
  
  return hostIP;
}
