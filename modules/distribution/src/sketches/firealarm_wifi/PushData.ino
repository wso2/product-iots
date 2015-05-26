void pushDigitalPinData(){
  for ( int pin = 0; pin < (sizeof(digitalPins)/sizeof(int)); pin++) {
    String payLoad = jsonPayLoad + "DigitalPinData";
    payLoad = payLoad + "\",\"time\":\"" + "9999";
    payLoad = payLoad + "\",\"key\":\"" + getDataType(digitalPins[pin]);
    payLoad = payLoad +  "\",\"value\":\"";
    
    if ( digitalPins[pin] == TEMP_PIN ) {
      int temperature =  (uint8_t)getTemperature();
      payLoad += temperature;
    } else if ( digitalRead(digitalPins[pin]) == HIGH ) {
      payLoad += "ON";
    } else if ( digitalRead(digitalPins[pin]) == LOW ) {
      payLoad += "OFF";
    }

    payLoad += "\"}";

    httpClient.fastrprint(F("POST "));     
    httpClient.fastrprint(SERVICE_EPOINT); httpClient.fastrprint(F("pushalarmdata"));  
    httpClient.fastrprint(F(" HTTP/1.1")); httpClient.fastrprint(F("\n"));
    httpClient.fastrprint(host.c_str()); httpClient.fastrprint(F("\n"));    
    httpClient.fastrprint(F("Content-Type: application/json")); httpClient.fastrprint(F("\n"));   
    httpClient.fastrprint(F("Content-Length: "));

    int payLength = payLoad.length();
    
    httpClient.fastrprint(String(payLength).c_str()); httpClient.fastrprint(F("\n"));
    httpClient.fastrprint(F("\n")); 
         
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
    
    for (int i = 0; i < payLength; i++) {
      if ( (i+1)*chunkSize > payLength) {
        httpClient.print(payLoad.substring(i*chunkSize, payLength));
        if(DEBUG) Serial.print(payLoad.substring(i*chunkSize, payLength));
        i = payLength;
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

    if(DEBUG)  {
      Serial.println();
      Serial.println("-------------------------------");
    }
    
    payLoad = "";
    delay(1000);
  }
}


void pushAnalogPinData(){
  for ( int pin = 0; pin < (sizeof(analogPins)/sizeof(int)); pin++) {
    String payLoad = jsonPayLoad + "AnalogPinData";
    payLoad = payLoad + "\",\"time\":\"" + "9999";
    payLoad = payLoad + "\",\"key\":\"" + getDataType(analogPins[pin]);
    payLoad = payLoad +  "\",\"value\":\"" + analogRead(analogPins[pin]);
    payLoad = payLoad + "\"}";

    httpClient.fastrprint(F("POST "));
    httpClient.fastrprint(SERVICE_EPOINT); httpClient.fastrprint(F("pushalarmdata")); 
    httpClient.fastrprint(F(" HTTP/1.1")); httpClient.fastrprint(F("\n"));
    httpClient.fastrprint(host.c_str()); httpClient.fastrprint(F("\n"));
    httpClient.fastrprint(F("Content-Type: application/json")); httpClient.fastrprint(F("\n"));
    httpClient.fastrprint(F("Content-Length: "));
    
    int payLength = payLoad.length();
    
    httpClient.fastrprint(String(payLength).c_str()); httpClient.fastrprint(F("\n"));
    httpClient.fastrprint(F("\n")); 
    
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
    
    for (int i = 0; i < payLength; i++) {
      if ( (i+1)*chunkSize > payLength) {
        httpClient.print(payLoad.substring(i*chunkSize, payLength));
        if(DEBUG) Serial.print(payLoad.substring(i*chunkSize, payLength));
        i = payLength;
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
    
    if(DEBUG)  {
      Serial.println();
      Serial.println("-------------------------------");
    }
    
    payLoad = "";
    delay(1000);
  }
}



