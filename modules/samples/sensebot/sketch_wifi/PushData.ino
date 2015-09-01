

void pushData(){
  String payLoad = "Data";
  payLoad = payLoad +  "\",\"value\":\"";
 
 
  payLoad += temperature;
  payLoad += ":"; 
  payLoad += pir;
  payLoad += ":"; 
  payLoad += sonar;   // returns distance if < MAX_DISTANCE else returns -1, 
  sonar=-1;                            // Pushed accordingly inside JAX-RS
  payLoad += ":"; 
  payLoad += ldr;
  payLoad += "\"}";

  client.fastrprint(F("POST "));     
  client.fastrprint(SERVICE_EPOINT); client.fastrprint(F("pushsensordata"));
  client.fastrprint(F(" HTTP/1.1")); client.fastrprint(F("\n"));
  client.fastrprint(host.c_str()); client.fastrprint(F("\n"));    
  client.fastrprint(F("Content-Type: application/json")); client.fastrprint(F("\n"));   
  client.fastrprint(F("Content-Length: "));

  int payLength = jsonPayLoad.length() + payLoad.length() + 2;
    
  client.fastrprint(String(payLength).c_str()); client.fastrprint(F("\n"));
  client.fastrprint(F("\n")); 
       
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
      client.print(jsonPayLoad.substring(i*chunkSize, jsonPayLoad.length()));
      if(DEBUG) Serial.print(jsonPayLoad.substring(i*chunkSize, jsonPayLoad.length()));
      i = jsonPayLoad.length();
    } else {
      client.print(jsonPayLoad.substring(i*chunkSize, (i+1)*chunkSize));
      if(DEBUG) Serial.print(jsonPayLoad.substring(i*chunkSize, (i+1)*chunkSize));
    }
  } 
  
  for (int i = 0; i < payLoad.length(); i++) {
    if ( (i+1)*chunkSize > payLoad.length()) {
      client.print(payLoad.substring(i*chunkSize, payLoad.length()));
      if(DEBUG) Serial.print(payLoad.substring(i*chunkSize, payLoad.length()));
      i = payLoad.length();
    } else {
      client.print(payLoad.substring(i*chunkSize, (i+1)*chunkSize));
      if(DEBUG) Serial.print(payLoad.substring(i*chunkSize, (i+1)*chunkSize));
    }
  } 
  
  client.fastrprint(F("\r\n"));
  
  if(DEBUG) Serial.println();
 
  delay(100);
  client.flush();
  client.close();
  wdt_reset();
//  if(true) { 
//    while (client.available()) {
//      char response = client.read();
//      if(DEBUG) Serial.print(response);
//    }
//  }

  if(DEBUG)  {
    Serial.println();
    Serial.println("-------------------------------");
  }
  
  payLoad = "";
}









double getTemperature(){
  int chk = DHT.read11(TEMP_PIN);
  if(DEBUG){
    Serial.println("-------------------------------");
    Serial.println("Type,\tstatus,\tHumidity (%),\tTemperature (C)");
    Serial.print("DHT11, \t");
    
  switch (chk)
  {
    case DHTLIB_OK:  
		Serial.print("OK,\t"); 
		break;
    case DHTLIB_ERROR_CHECKSUM: 
		Serial.print("Checksum error,\t"); 
		break;
    case DHTLIB_ERROR_TIMEOUT: 
		Serial.print("Time out error,\t"); 
		break;
    case DHTLIB_ERROR_CONNECT:
        Serial.print("Connect error,\t");
        break;
    case DHTLIB_ERROR_ACK_L:
        Serial.print("Ack Low error,\t");
        break;
    case DHTLIB_ERROR_ACK_H:
        Serial.print("Ack High error,\t");
        break;
    default: 
		Serial.print("Unknown error,\t"); 
		break;
  }
    // DISPLAY DATA
    Serial.print("\t");
    Serial.print(DHT.temperature, 1);
    Serial.print(",\t\t");
    Serial.println(DHT.humidity, 1);
    Serial.println("-------------------------------");
  }
  
  temperature= DHT.temperature;
}


void getSonar()
{
  long duration, inches, cm;
  
  pinMode(SONAR_TRIG, OUTPUT);// attach pin 3 to Trig
  digitalWrite(SONAR_TRIG, LOW);
  delayMicroseconds(2);
  digitalWrite(SONAR_TRIG, HIGH);
  delayMicroseconds(5);
  digitalWrite(SONAR_TRIG, LOW);

  pinMode (SONAR_ECHO, INPUT);//attach pin 4 to Echo
  duration = pulseIn(SONAR_ECHO, HIGH);

  // convert the time into a distance
  inches = microsecondsToInches(duration);
  cm = microsecondsToCentimeters(duration);
  
  if(DEBUG){
      Serial.print("SONAR : ");  
      Serial.print(cm);   
      Serial.print(" , ");   
      Serial.println(inches); 
      Serial.println("-----------------------------------");
  }
  
  if (cm > MAX_DISTANCE || cm <= 0){
    //Serial.println("Out of range");
    noTone(BUZZER);
   
    
    
  } else {
    tone(BUZZER, BUZZER_SOUND);
    sonar= (uint8_t)cm;
  }  
}

long microsecondsToInches(long microseconds){
    return microseconds / 74 / 2;
}

long microsecondsToCentimeters(long microseconds){
    return microseconds / 29 / 2;
}


