

void pushData(){
  String payLoad = "Data";
  payLoad = payLoad +  "\",\"value\":\"";
  
  
  payLoad += (uint8_t)getTemperature();
  payLoad += ":"; 
  payLoad += ( digitalRead(BULB_PIN) == HIGH )?"ON":"OFF";
  payLoad += ":"; 
  payLoad += ( digitalRead(FAN_PIN) == HIGH )?"ON":"OFF";
  payLoad += "\"}";

    pushClient.fastrprint(F("POST "));     
    pushClient.fastrprint(SERVICE_EPOINT); pushClient.fastrprint(F("pushalarmdata"));  
    pushClient.fastrprint(F(" HTTP/1.1")); pushClient.fastrprint(F("\n"));
    pushClient.fastrprint(host.c_str()); pushClient.fastrprint(F("\n"));    
    pushClient.fastrprint(F("Content-Type: application/json")); pushClient.fastrprint(F("\n"));   
    pushClient.fastrprint(F("Content-Length: "));

    int payLength = jsonPayLoad.length() + payLoad.length();
    
    pushClient.fastrprint(String(payLength).c_str()); pushClient.fastrprint(F("\n"));
    pushClient.fastrprint(F("\n")); 
         
    if(DEBUG) {
      Serial.print("POST ");
      Serial.print(SERVICE_EPOINT); Serial.print("pushalarmdata");
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
        pushClient.print(jsonPayLoad.substring(i*chunkSize, jsonPayLoad.length()));
        if(DEBUG) Serial.print(jsonPayLoad.substring(i*chunkSize, jsonPayLoad.length()));
        i = jsonPayLoad.length();
      } else {
        pushClient.print(jsonPayLoad.substring(i*chunkSize, (i+1)*chunkSize));
        if(DEBUG) Serial.print(jsonPayLoad.substring(i*chunkSize, (i+1)*chunkSize));
      }
    } 
    
    for (int i = 0; i < payLoad.length(); i++) {
      if ( (i+1)*chunkSize > payLoad.length()) {
        pushClient.print(payLoad.substring(i*chunkSize, payLoad.length()));
        if(DEBUG) Serial.print(payLoad.substring(i*chunkSize, payLoad.length()));
        i = payLoad.length();
      } else {
        pushClient.print(payLoad.substring(i*chunkSize, (i+1)*chunkSize));
        if(DEBUG) Serial.print(payLoad.substring(i*chunkSize, (i+1)*chunkSize));
      }
    } 
    
    pushClient.fastrprint(F("\n"));
    if(DEBUG) Serial.println();
   
    delay(100);
    
    pushClient.flush();
    pushClient.close();
    wdt_reset();
    
//    if(true) { 
//      while (pushClient.available()) {
//        char response = pushClient.read();
//        if(DEBUG) Serial.print(response);
//      }
//    }

    if(DEBUG)  {
      Serial.println();
      Serial.println("-------------------------------");
    }
    
    payLoad = "";
}

double getTemperature(){
  dht DHT;
  
  if(DEBUG) {
    Serial.println("-------------------------------");
    Serial.println("Type,\tstatus,\tHumidity (%),\tTemperature (C)");
    Serial.print("DHT11, \t");
  }
  int chk = DHT.read11(TEMP_PIN);
  switch (chk)
  {
    case DHTLIB_OK:  
		if(DEBUG)  Serial.print("OK,\t"); 
		break;
    case DHTLIB_ERROR_CHECKSUM: 
		if(DEBUG)  Serial.print("Checksum error,\t"); 
		break;
    case DHTLIB_ERROR_TIMEOUT: 
		if(DEBUG)  Serial.print("Time out error,\t"); 
		break;
    case DHTLIB_ERROR_CONNECT:
                if(DEBUG)  Serial.print("Connect error,\t");
                break;
    case DHTLIB_ERROR_ACK_L:
                if(DEBUG) Serial.print("Ack Low error,\t");
                break;
    case DHTLIB_ERROR_ACK_H:
                if(DEBUG) Serial.print("Ack High error,\t");
                break;
    default: 
		if(DEBUG) Serial.print("Unknown error,\t"); 
		break;
  }
                        // DISPLAY DATA
  if(DEBUG) {
    Serial.print("\t");
    Serial.print(DHT.temperature, 1);
    Serial.print(",\t\t");
    Serial.println(DHT.humidity, 1);
    Serial.println("-------------------------------");
  }
  return DHT.temperature;
}



