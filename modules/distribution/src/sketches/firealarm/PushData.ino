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
    delay(1000);
    
    while (httpClient.available()) {
      char response = httpClient.read();
//      Serial.print(response);
    }
    
//    Serial.println();
//    Serial.println("-------------------------------");
    delay(1000);
  }
}


void pushAnalogPinData(){
  for ( int pin = 0; pin < (sizeof(analogPins)/sizeof(int)); pin++) {
    String resource = " " + String(SERVICE_EPOINT) + String(PUSH_ALARM_DATA) + " ";
       
    String payLoad = jsonPayLoad + "AnalogPinData";
    payLoad = payLoad + String(TIME_JSON) + "9999";
    payLoad = payLoad + String(KEY_JSON) + getDataType(analogPins[pin]);
    payLoad = payLoad + String(VALUE_JSON) + analogRead(analogPins[pin]);
    payLoad = payLoad + String(END_JSON);

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
    delay(1000);
    
    while (httpClient.available()) {
      char response = httpClient.read();
//      Serial.print(response);
    }
    
//    Serial.println();
//    Serial.println("-------------------------------");
    delay(1000);
  }
}



