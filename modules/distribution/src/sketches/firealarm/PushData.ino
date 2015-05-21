String resource, tempResource, host;
String myIP = "";
String hostIP = "";

void setupResource(){
  
  setHostIP(server);
  String port = String(SERVICE_PORT);
  
  host = "Host: " + hostIP + ":" + port;      
  Serial.println(host);
  
  myIP = String(Ethernet.localIP()[0]);
  
  for ( int index = 1; index < 4; index++) {
    myIP += "." + String(Ethernet.localIP()[index]);
  }
  
  resource = String(SERVICE_EPOINT) + myIP + "/" +  String(DEVICE_OWNER) + "/" + 
                                                    String(DEVICE_TYPE) + "/" + String(DEVICE_ID) + "/";  
      
  resource = resource + 30;
  Serial.println(resource);
  Serial.println("-------------------------------");
}



void setHostIP(byte server[4]){
  hostIP = String(server[0]);
  
  for ( int index = 1; index < 4; index++) {
    hostIP += "." + String(server[index]);
  }
}



void pushDigitalPinData(){
  for ( int pin = 0; pin < (sizeof(digitalPins)/sizeof(int)); pin++) {
    tempResource = " " + resource + "/D" + digitalPins[pin] + "/";

    if ( digitalRead(digitalPins[pin]) == HIGH) {
      tempResource += "HIGH ";
    } else if ( digitalRead(digitalPins[pin]) == LOW) {
      tempResource += "LOW ";
    }

    httpClient.print(HTTP_METHOD);
    httpClient.print(tempResource);
    httpClient.println(HTTP_VERSION);
    httpClient.println(host);
    httpClient.println();
    delay(2000);
    
    while (httpClient.available()) {
      char response = httpClient.read();
      Serial.print(response);
    }
    
    Serial.println();
    Serial.println("-------------------------------");
    tempResource = "";
    delay(2000);
  }
}



void pushAnalogPinData(){
  for ( int pin = 0; pin < (sizeof(analogPins)/sizeof(int)); pin++) {
    tempResource = " " + resource + "/A" + analogPins[pin] + "/" + analogRead(analogPins[pin]) + " ";

    httpClient.print(HTTP_METHOD);
    httpClient.print(tempResource);
    httpClient.println(HTTP_VERSION);
    httpClient.println(host);
    httpClient.println();
    delay(1000);
    
    while (httpClient.available()) {
      char response = httpClient.read();
      Serial.print(response);
    }
    
    Serial.println();
    Serial.println("-------------------------------");
    tempResource = "";
    delay(1000);
  }
}