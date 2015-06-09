String readControls() {
  String responseMsg;
  String resource = " " + String(SERVICE_EPOINT) + String(READ_CONTROLS) + String(DEVICE_OWNER) + "/" + String(DEVICE_ID) + " ";

  httpClient.print(HTTP_GET);
  httpClient.print(resource);
  httpClient.println(HTTP_VERSION);
  httpClient.println(host);
  httpClient.println();
  
  if(DEBUG) {
    Serial.print(HTTP_GET);
    Serial.print(resource);
    Serial.println(HTTP_VERSION);
    Serial.println(host);
    Serial.println();
  }
  
  delay(1000);
  
  while (httpClient.available()) {
    char response = httpClient.read();
    responseMsg += response;
  }
  
  if(DEBUG) {
    Serial.print(responseMsg);
    Serial.println();
    Serial.println("-------------------------------");
  }
  delay(1000);
  return responseMsg; 
}

void reply(String replyMsg) {
  String resource = " " + String(SERVICE_EPOINT) + String(REPLY) + " ";
  String payLoad = jsonPayLoad + replyMsg + String(END_JSON);
  
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




