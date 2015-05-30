
  /**********************************************************************************************  
    This method will traverse the array of digital pins and batch the data from the those pins together.
    It makes a single call to the server and sends all pin values as a batch.
    Server dis-assembles it accordingly and makes multiple publish calls for each sensor type.
   ***********************************************************************************************/

void pushData(){
  String resource = " " + String(SERVICE_EPOINT) + String(PUSH_ALARM_DATA) + " ";
  String payLoad = jsonPayLoad + "DigitalPinData";
  payLoad = payLoad + String(TIME_JSON) + "9999";
  payLoad = payLoad + String(KEY_JSON) + "Data";
  payLoad += String(VALUE_JSON);
  
  for ( int pin = 0; pin < (sizeof(digitalPins)/sizeof(int)); pin++) {
    if ( digitalPins[pin] == TEMP_PIN ) {
      payLoad += String(getTemperature());
    } else if ( digitalRead(digitalPins[pin]) == HIGH ) {
      payLoad += "ON";
    } else if ( digitalRead(digitalPins[pin]) == LOW ) {
      payLoad += "OFF";
    }

    if ( ((sizeof(digitalPins)/sizeof(int)) - 1) != pin ) {
      payLoad += "-";
    }
    
  }
  
  payLoad = payLoad + String(END_JSON);
  
    if(DEBUG) Serial.println(payLoad);
    
    httpClient.print(HTTP_POST);
    httpClient.print(resource);
    httpClient.println(HTTP_VERSION);
    httpClient.println(host);
    httpClient.println(HTTP_CONTENT_TYPE);
    httpClient.print(HTTP_CONTENT_LEN);
    httpClient.println(payLoad.length());
    httpClient.println();
    httpClient.println(payLoad);
    httpClient.println();
    
    if(DEBUG)  {
      Serial.print(HTTP_POST);
      Serial.print(resource);
      Serial.println(HTTP_VERSION);
      Serial.println(host);
      Serial.println(HTTP_CONTENT_TYPE);
      Serial.print(HTTP_CONTENT_LEN);
      Serial.println(payLoad.length());
      Serial.println();
      Serial.println(payLoad);
      Serial.println();
    }
    
    delay(1000);
    
    while (httpClient.available()) {
      char response = httpClient.read();
      if(DEBUG) Serial.print(response);
    }
    
  if(DEBUG) {
    Serial.println();
    Serial.println("-------------------------------");
  }
    delay(1000); 
}

  /**********************************************************************************************  
    This method will traverse the array of digital pins and publish the data from the those pins.
    It differs from the above method such that the pin data is published one after the other in
    seperate calls to the server
   ***********************************************************************************************/


void pushDigitalPinData(){
  for ( int pin = 0; pin < (sizeof(digitalPins)/sizeof(int)); pin++) {
    String resource = " " + String(SERVICE_EPOINT) + String(PUSH_ALARM_DATA) + " ";

    String payLoad = jsonPayLoad + "DigitalPinData";
    payLoad = payLoad + String(TIME_JSON) + "9999";
    payLoad = payLoad + String(KEY_JSON) + getDataType(digitalPins[pin]);
    payLoad += String(VALUE_JSON);
    
    
    if ( digitalPins[pin] == TEMP_PIN ) {
      payLoad += String(getTemperature());
    } else if ( digitalRead(digitalPins[pin]) == HIGH ) {
      payLoad += "ON";
    } else if ( digitalRead(digitalPins[pin]) == LOW ) {
      payLoad += "OFF";
    }

    payLoad = payLoad + String(END_JSON);
    
    if(DEBUG) Serial.println(payLoad);
    
    httpClient.print(HTTP_POST);
    httpClient.print(resource);
    httpClient.println(HTTP_VERSION);
    httpClient.println(host);
    httpClient.println(HTTP_CONTENT_TYPE);
    httpClient.print(HTTP_CONTENT_LEN);
    httpClient.println(payLoad.length());
    httpClient.println();
    httpClient.println(payLoad);
    httpClient.println();
    
    if(DEBUG)  {
      Serial.print(HTTP_POST);
      Serial.print(resource);
      Serial.println(HTTP_VERSION);
      Serial.println(host);
      Serial.println(HTTP_CONTENT_TYPE);
      Serial.print(HTTP_CONTENT_LEN);
      Serial.println(payLoad.length());
      Serial.println();
      Serial.println(payLoad);
      Serial.println();
    }
    
    delay(1000);
    
    while (httpClient.available()) {
      char response = httpClient.read();
      if(DEBUG) Serial.print(response);
    }
    
  if(DEBUG) {
    Serial.println();
    Serial.println("-------------------------------");
  }
    delay(1000);
  }
}

    /**********************************************************************************************  
    Only required for cases of reading analog pin values.
    An int Array of analog pins that needs to be read has to be initialised.
    This method will traverse the array and publish the data from the selected pins
    ***********************************************************************************************/


void pushAnalogPinData(){
  for ( int pin = 0; pin < (sizeof(analogPins)/sizeof(int)); pin++) {
    String resource = " " + String(SERVICE_EPOINT) + String(PUSH_ALARM_DATA) + " ";
       
    String payLoad = jsonPayLoad + "AnalogPinData";
    payLoad = payLoad + String(TIME_JSON) + "9999";
    payLoad = payLoad + String(KEY_JSON) + getDataType(analogPins[pin]);
    payLoad = payLoad + String(VALUE_JSON) + analogRead(analogPins[pin]);
    payLoad = payLoad + String(END_JSON);
  
    if(DEBUG) Serial.println(payLoad);
  
    httpClient.print(HTTP_POST);
    httpClient.print(resource);
    httpClient.println(HTTP_VERSION);
    httpClient.println(host);
    httpClient.println(HTTP_CONTENT_TYPE);
    httpClient.print(HTTP_CONTENT_LEN);
    httpClient.println(payLoad.length());
    httpClient.println();
    httpClient.println(payLoad);
    httpClient.println();
    
    if(DEBUG) {
      Serial.print(HTTP_POST);
      Serial.print(resource);
      Serial.println(HTTP_VERSION);
      Serial.println(host);
      Serial.println(HTTP_CONTENT_TYPE);
      Serial.print(HTTP_CONTENT_LEN);
      Serial.println(payLoad.length());
      Serial.println();
      Serial.println(payLoad);
      Serial.println();
    }
    
    delay(1000);
    
    while (httpClient.available()) {
      char response = httpClient.read();
      if(DEBUG) Serial.print(response);
    }
    
  if(DEBUG) {
    Serial.println();
    Serial.println("-------------------------------");
  }
    delay(1000);
  }
}



