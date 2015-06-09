//int motionSense(){
//  int motionDetect = digitalRead(PIR_PIN);
//  if(DEBUG){
//    Serial.print("MOTION : ");
//    Serial.println(motionDetect);
//  }
//  return motionDetect;
//}


//int lightSense(){
//  int lightLevel = analogRead(LDR_PIN);
//  if(DEBUG){
//    Serial.print("LIGHT : ");
//    Serial.println(lightLevel);
//  }
//  return lightLevel;
//}

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
  
  return DHT.temperature;
}


int getSonar()
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
    return -1;
  } else {
    tone(BUZZER, BUZZER_SOUND);
    return cm;
  }  
}

long microsecondsToInches(long microseconds){
    return microseconds / 74 / 2;
}

long microsecondsToCentimeters(long microseconds){
    return microseconds / 29 / 2;
}


