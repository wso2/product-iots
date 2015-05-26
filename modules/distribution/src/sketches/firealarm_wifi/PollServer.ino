String readControls() {
  String responseMsg;
  
  httpClient.fastrprint(F("GET ")); 
  httpClient.fastrprint(SERVICE_EPOINT); httpClient.fastrprint(F("readcontrols/"));
  httpClient.fastrprint(DEVICE_OWNER); httpClient.fastrprint(F("/")); httpClient.fastrprint(DEVICE_ID);
  httpClient.fastrprint(F(" HTTP/1.1")); httpClient.fastrprint(F("\n"));
  httpClient.fastrprint(host.c_str()); httpClient.fastrprint(F("\n"));
  httpClient.println();

  delay(1000);
  
  while (httpClient.available()) {
    char response = httpClient.read();
    responseMsg += response;
  }
  
  if(DEBUG) {
    Serial.print(responseMsg);
    Serial.println();
    Serial.println("-------------------------------");
  }
  
  delay(1000);
  return responseMsg; 
}

void reply() {
  String payLoad = replyMsg + "\"}";
  
  if(DEBUG) {
    Serial.print(jsonPayLoad); Serial.println(payLoad);
  }
     
  httpClient.fastrprint(F("POST ")); 
  httpClient.fastrprint(SERVICE_EPOINT); httpClient.fastrprint(F("reply")); 
  httpClient.fastrprint(F(" HTTP/1.1")); httpClient.fastrprint(F("\n")); 
  httpClient.fastrprint(host.c_str()); httpClient.fastrprint(F("\n"));
  httpClient.fastrprint(F("Content-Type: application/json")); httpClient.fastrprint(F("\n"));
  httpClient.fastrprint(F("Content-Length: "));
  
  int payLength = jsonPayLoad.length() + payLoad.length();
  
  httpClient.fastrprint(String(payLength).c_str()); httpClient.fastrprint(F("\n"));
  httpClient.fastrprint(F("\n")); 
    
  if(DEBUG) {
    Serial.print("POST ");
    Serial.print(SERVICE_EPOINT); Serial.print("reply");
    Serial.print(" HTTP/1.1"); Serial.println();
    Serial.print(host); Serial.println();
    Serial.print("Content-Type: application/json"); Serial.println();
    Serial.print("Content-Length: "); 
    Serial.print(payLength); Serial.println();
    Serial.println();
  }
   
  int chunkSize = 50;
  
  for (int i = 0; i < jsonPayLoad.length(); i++) {
    if ( (i+1)*chunkSize > jsonPayLoad.length()) {
      httpClient.print(jsonPayLoad.substring(i*chunkSize, jsonPayLoad.length()));
      if(DEBUG) Serial.print(jsonPayLoad.substring(i*chunkSize, jsonPayLoad.length()));
      i = jsonPayLoad.length();
    } else {
      httpClient.print(jsonPayLoad.substring(i*chunkSize, (i+1)*chunkSize));
      if(DEBUG) Serial.print(jsonPayLoad.substring(i*chunkSize, (i+1)*chunkSize));
    }
  } 
  
  for (int i = 0; i < payLoad.length(); i++) {
    if ( (i+1)*chunkSize > payLoad.length()) {
      httpClient.print(payLoad.substring(i*chunkSize, payLoad.length()));
      if(DEBUG) Serial.print(payLoad.substring(i*chunkSize, payLoad.length()));
      i = payLoad.length();
    } else {
      httpClient.print(payLoad.substring(i*chunkSize, (i+1)*chunkSize));
      if(DEBUG) Serial.print(payLoad.substring(i*chunkSize, (i+1)*chunkSize));
    }
  }
  
  httpClient.fastrprint(F("\n"));
  if(DEBUG) Serial.println();
    
  delay(1000);
  
  while (httpClient.available()) {
    char response = httpClient.read();
    if(DEBUG) Serial.print(response);
  }
 
  if(DEBUG) {
    Serial.println();
    Serial.println("-------------------------------");
  }
  
  payLoad = "";
  delay(1000);

}




