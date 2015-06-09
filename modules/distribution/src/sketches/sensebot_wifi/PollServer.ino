boolean readControls() {
//  String responseMsg;
  
  pollClient.fastrprint(F("GET ")); 
  pollClient.fastrprint(SERVICE_EPOINT); pollClient.fastrprint(F("readcontrols/"));
  pollClient.fastrprint(DEVICE_OWNER); pollClient.fastrprint(F("/")); pollClient.fastrprint(DEVICE_ID);
  pollClient.fastrprint(F(" HTTP/1.1")); pollClient.fastrprint(F("\n"));
  pollClient.fastrprint(host.c_str()); pollClient.fastrprint(F("\n"));
  pollClient.println();

  delay(1000);
  
  if (true) {
    while (pollClient.available()) {
      char response = pollClient.read();
      responseMsg += response;
    }
  }
  
  
    int index = responseMsg.lastIndexOf(":");
    int newLine = responseMsg.lastIndexOf("\n");
    subStrn = responseMsg.substring(index + 1);
    responseMsg = responseMsg.substring(newLine + 1, index); 
    
  if(DEBUG) {
    Serial.print(responseMsg);
    Serial.println();
    Serial.println("-------------------------------");
  }
  
  
  if (subStrn.equals("IN")) {
    return false;
  }

  return true; 
}



void reply() {
  String payLoad = replyMsg + "\"}";
  
  if(DEBUG) {
    Serial.print(jsonPayLoad); Serial.println(payLoad);
  }
     
  pollClient.fastrprint(F("POST ")); 
  pollClient.fastrprint(SERVICE_EPOINT); pollClient.fastrprint(F("reply")); 
  pollClient.fastrprint(F(" HTTP/1.1")); pollClient.fastrprint(F("\n")); 
  pollClient.fastrprint(host.c_str()); pollClient.fastrprint(F("\n"));
  pollClient.fastrprint(F("Content-Type: application/json")); pollClient.fastrprint(F("\n"));
  pollClient.fastrprint(F("Content-Length: "));
  
  int payLength = jsonPayLoad.length() + payLoad.length();
  
  pollClient.fastrprint(String(payLength).c_str()); pollClient.fastrprint(F("\n"));
  pollClient.fastrprint(F("\n")); 
    
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
      pollClient.print(jsonPayLoad.substring(i*chunkSize, jsonPayLoad.length()));
      if(DEBUG) Serial.print(jsonPayLoad.substring(i*chunkSize, jsonPayLoad.length()));
      i = jsonPayLoad.length();
    } else {
      pollClient.print(jsonPayLoad.substring(i*chunkSize, (i+1)*chunkSize));
      if(DEBUG) Serial.print(jsonPayLoad.substring(i*chunkSize, (i+1)*chunkSize));
    }
  } 
  
  for (int i = 0; i < payLoad.length(); i++) {
    if ( (i+1)*chunkSize > payLoad.length()) {
      pollClient.print(payLoad.substring(i*chunkSize, payLoad.length()));
      if(DEBUG) Serial.print(payLoad.substring(i*chunkSize, payLoad.length()));
      i = payLoad.length();
    } else {
      pollClient.print(payLoad.substring(i*chunkSize, (i+1)*chunkSize));
      if(DEBUG) Serial.print(payLoad.substring(i*chunkSize, (i+1)*chunkSize));
    }
  }
  
  pollClient.fastrprint(F("\n"));
  if(DEBUG) Serial.println();
    
  delay(1000);
  
  if(true) { 
    while (pollClient.available()) {
      char response = pollClient.read();
      if(DEBUG) Serial.print(response);
    }
  }
  
  if(DEBUG) {
    Serial.println();
    Serial.println("-------------------------------");
  }
  
  payLoad = "";
//  delay(1000);

}




