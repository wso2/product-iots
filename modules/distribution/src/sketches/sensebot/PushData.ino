
  /**********************************************************************************************  
    This method will traverse the array of digital pins and batch the data from the those pins together.
    It makes a single call to the server and sends all pin values as a batch.
    Server dis-assembles it accordingly and makes multiple publish calls for each sensor type.
   ***********************************************************************************************/

void pushData(){
  String payLoad = "Data";
  payLoad = payLoad +  "\",\"value\":\"";
 
  int temperature =  (uint8_t)getTemperature();
  payLoad += temperature;
  payLoad += ":"; 
  payLoad += digitalRead(PIR_PIN);
  payLoad += ":"; 
  payLoad += getSonar();   // returns distance if < MAX_DISTANCE else returns -1, 
                           // Pushed accordingly inside JAX-RS
  payLoad += ":"; 
  payLoad += analogRead(LDR_PIN);
  payLoad += "\"}";

  pushClient.fastrprint(F("POST "));     
  pushClient.fastrprint(SERVICE_EPOINT); pushClient.fastrprint(F("pushsensordata"));  
  pushClient.fastrprint(F(" HTTP/1.1")); pushClient.fastrprint(F("\n"));
  pushClient.fastrprint(host.c_str()); pushClient.fastrprint(F("\n"));    
  pushClient.fastrprint(F("Content-Type: application/json")); pushClient.fastrprint(F("\n"));   
  pushClient.fastrprint(F("Content-Length: "));

  int payLength = jsonPayLoad.length() + payLoad.length();
    
  pushClient.fastrprint(String(payLength).c_str()); pushClient.fastrprint(F("\n"));
  pushClient.fastrprint(F("\n")); 
       
  if(DEBUG) {
    Serial.print("POST ");
    Serial.print(SERVICE_EPOINT); Serial.print("pushsensordata");
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




