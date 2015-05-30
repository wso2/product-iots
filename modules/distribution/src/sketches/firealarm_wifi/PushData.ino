
  /**********************************************************************************************  
    This method will traverse the array of digital pins and batch the data from the those pins together.
    It makes a single call to the server and sends all pin values as a batch.
    Server dis-assembles it accordingly and makes multiple publish calls for each sensor type.
   ***********************************************************************************************/

void pushData(){
  String payLoad = "Data";
  payLoad = payLoad +  "\",\"value\":\"";
  
  for ( int pin = 0; pin < (sizeof(digitalPins)/sizeof(int)); pin++) {  
    if ( digitalPins[pin] == TEMP_PIN ) {
      int temperature =  (uint8_t)getTemperature();
      payLoad += temperature;
    } else if ( digitalRead(digitalPins[pin]) == HIGH ) {
      payLoad += "ON";
    } else if ( digitalRead(digitalPins[pin]) == LOW ) {
      payLoad += "OFF";
    }
    
    if ( ((sizeof(digitalPins)/sizeof(int)) - 1) != pin ) {
      payLoad += "-";
    }
    
  }
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
   
    delay(1000);

    if(true) { 
      while (pushClient.available()) {
        char response = pushClient.read();
        if(DEBUG) Serial.print(response);
      }
    }

    if(DEBUG)  {
      Serial.println();
      Serial.println("-------------------------------");
    }
    
    payLoad = "";
}


  /**********************************************************************************************  
    This method will traverse the array of digital pins and publish the data from the those pins.
    It differs from the above method such that the pin data is published one after the other in
    seperate calls to the server
   ***********************************************************************************************/


//void pushDigitalPinData(){
//  for ( int pin = 0; pin < (sizeof(digitalPins)/sizeof(int)); pin++) {
//    String payLoad = getDataType(digitalPins[pin]);
//    payLoad = payLoad +  "\",\"value\":\"";
//    
//    if ( digitalPins[pin] == TEMP_PIN ) {
//      int temperature =  (uint8_t)getTemperature();
//      payLoad += temperature;
//    } else if ( digitalRead(digitalPins[pin]) == HIGH ) {
//      payLoad += "ON";
//    } else if ( digitalRead(digitalPins[pin]) == LOW ) {
//      payLoad += "OFF";
//    }
//    
//    payLoad += "\"}";
//
//    pushClient.fastrprint(F("POST "));     
//    pushClient.fastrprint(SERVICE_EPOINT); pushClient.fastrprint(F("pushalarmdata"));  
//    pushClient.fastrprint(F(" HTTP/1.1")); pushClient.fastrprint(F("\n"));
//    pushClient.fastrprint(host.c_str()); pushClient.fastrprint(F("\n"));    
//    pushClient.fastrprint(F("Content-Type: application/json")); pushClient.fastrprint(F("\n"));   
//    pushClient.fastrprint(F("Content-Length: "));
//
//    int payLength = jsonPayLoad.length() + payLoad.length();
//    
//    pushClient.fastrprint(String(payLength).c_str()); pushClient.fastrprint(F("\n"));
//    pushClient.fastrprint(F("\n")); 
//         
//    if(DEBUG) {
//      Serial.print("POST ");
//      Serial.print(SERVICE_EPOINT); Serial.print("pushalarmdata");
//      Serial.print(" HTTP/1.1"); Serial.println();
//      Serial.print(host); Serial.println();
//      Serial.print("Content-Type: application/json"); Serial.println();
//      Serial.print("Content-Length: "); 
//      Serial.print(payLength); Serial.println();
//      Serial.println();
//    }
//    
//    int chunkSize = 50;
//    
//    for (int i = 0; i < jsonPayLoad.length(); i++) {
//      if ( (i+1)*chunkSize > jsonPayLoad.length()) {
//        pushClient.print(jsonPayLoad.substring(i*chunkSize, jsonPayLoad.length()));
//        if(DEBUG) Serial.print(jsonPayLoad.substring(i*chunkSize, jsonPayLoad.length()));
//        i = jsonPayLoad.length();
//      } else {
//        pushClient.print(jsonPayLoad.substring(i*chunkSize, (i+1)*chunkSize));
//        if(DEBUG) Serial.print(jsonPayLoad.substring(i*chunkSize, (i+1)*chunkSize));
//      }
//    } 
//    
//    for (int i = 0; i < payLoad.length(); i++) {
//      if ( (i+1)*chunkSize > payLoad.length()) {
//        pushClient.print(payLoad.substring(i*chunkSize, payLoad.length()));
//        if(DEBUG) Serial.print(payLoad.substring(i*chunkSize, payLoad.length()));
//        i = payLoad.length();
//      } else {
//        pushClient.print(payLoad.substring(i*chunkSize, (i+1)*chunkSize));
//        if(DEBUG) Serial.print(payLoad.substring(i*chunkSize, (i+1)*chunkSize));
//      }
//    } 
//    
//    pushClient.fastrprint(F("\n"));
//    if(DEBUG) Serial.println();
//   
//    delay(1000);
//
//    if(true) {     
//      while (pushClient.available()) {
//        char response = pushClient.read();
//        if(DEBUG) Serial.print(response);
//      }
//    }
//
//    if(DEBUG)  {
//      Serial.println();
//      Serial.println("-------------------------------");
//    }
//    
//    payLoad = "";
////    delay(1000);
//  }
//}

    /**********************************************************************************************  
    Only required for cases of reading analog pin values.
    An int Array of analog pins that needs to be read has to be initialised.
    This method will traverse the array and publish the data from the selected pins
    ***********************************************************************************************/

//void pushAnalogPinData(){
//  for ( int pin = 0; pin < (sizeof(analogPins)/sizeof(int)); pin++) {
//    String payLoad = jsonPayLoad + "AnalogPinData";
//    payLoad = payLoad + "\",\"time\":\"" + "9999";
//    payLoad = payLoad + "\",\"key\":\"" + getDataType(analogPins[pin]);
//    payLoad = payLoad +  "\",\"value\":\"" + analogRead(analogPins[pin]);
//    payLoad = payLoad + "\"}";
//
//    pushClient.fastrprint(F("POST "));
//    pushClient.fastrprint(SERVICE_EPOINT); pushClient.fastrprint(F("pushalarmdata")); 
//    pushClient.fastrprint(F(" HTTP/1.1")); pushClient.fastrprint(F("\n"));
//    pushClient.fastrprint(host.c_str()); pushClient.fastrprint(F("\n"));
//    pushClient.fastrprint(F("Content-Type: application/json")); pushClient.fastrprint(F("\n"));
//    pushClient.fastrprint(F("Content-Length: "));
//    
//    int payLength = payLoad.length();
//    
//    pushClient.fastrprint(String(payLength).c_str()); pushClient.fastrprint(F("\n"));
//    pushClient.fastrprint(F("\n")); 
//    
//    if(DEBUG) {
//      Serial.print("POST ");
//      Serial.print(SERVICE_EPOINT); Serial.print("pushalarmdata");
//      Serial.print(" HTTP/1.1"); Serial.println();
//      Serial.print(host); Serial.println();
//      Serial.print("Content-Type: application/json"); Serial.println();
//      Serial.print("Content-Length: "); 
//      Serial.print(payLength); Serial.println();
//      Serial.println();
//    }
//  
//    int chunkSize = 50;
//    
//    for (int i = 0; i < payLength; i++) {
//      if ( (i+1)*chunkSize > payLength) {
//        pushClient.print(payLoad.substring(i*chunkSize, payLength));
//        if(DEBUG) Serial.print(payLoad.substring(i*chunkSize, payLength));
//        i = payLength;
//      } else {
//        pushClient.print(payLoad.substring(i*chunkSize, (i+1)*chunkSize));
//        if(DEBUG) Serial.print(payLoad.substring(i*chunkSize, (i+1)*chunkSize));
//      }
//    }
//    
//    pushClient.fastrprint(F("\n"));
//    if(DEBUG) Serial.println();
//    
//    delay(1000);
//    
//    if(true) {
//      while (pushClient.available()) {
//        char response = pushClient.read();
//        if(DEBUG) Serial.print(response);
//      }
//    }
//    
//    if(DEBUG)  {
//      Serial.println();
//      Serial.println("-------------------------------");
//    }
//    
//    payLoad = "";
//    delay(1000);
//  }
//}




String getDataType(int pin){
  switch(pin){
    case TEMP_PIN:
      return "TEMP";
    case BULB_PIN:
      return "BULB";
    case FAN_PIN:
      return "FAN";
    default:
      return String(pin);
  }

}



