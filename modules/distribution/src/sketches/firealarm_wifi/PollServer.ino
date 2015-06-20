void registerIP() {
  pushClient.fastrprint(F("GET ")); 
  pushClient.fastrprint(SERVICE_EPOINT); pushClient.fastrprint(F("register/"));
  pushClient.fastrprint(DEVICE_OWNER); pushClient.fastrprint(F("/")); 
  pushClient.fastrprint(DEVICE_ID); pushClient.fastrprint(F("/")); 
  
//  pushClient.fastrprint(deviceIP.c_str());
  pushClient.fastrprint(String((uint8_t)(ipAddress >> 24)).c_str());
  pushClient.fastrprint(".");
  pushClient.fastrprint(String((uint8_t)(ipAddress >> 16)).c_str());
  pushClient.fastrprint(".");
  pushClient.fastrprint(String((uint8_t)(ipAddress >> 8)).c_str());
  pushClient.fastrprint(".");
  pushClient.fastrprint(String((uint8_t)ipAddress).c_str());
  
  pushClient.fastrprint(F(" HTTP/1.1")); pushClient.fastrprint(F("\n"));
  pushClient.fastrprint(host.c_str()); pushClient.fastrprint(F("\n"));
  pushClient.println();

  if (DEBUG) {
    Serial.print("GET "); Serial.print(SERVICE_EPOINT); Serial.print("register/"); 
    Serial.print(DEVICE_OWNER); Serial.print("/"); Serial.print(DEVICE_ID);
    Serial.print("/"); 
//    Serial.print(deviceIP); 
    Serial.print(" HTTP/1.1"); Serial.println();
    Serial.print(host); Serial.println(); Serial.println();
  }
  
  delay(100);

  pushClient.flush();
  pushClient.close();
}
