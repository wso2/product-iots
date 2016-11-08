/**
  * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  *
  * WSO2 Inc. licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
**/

#include "CurrentSensor.h"

/**********************************************************************************************
 This method will traverse the array of digital pins and batch the data from the those pins together.
 It makes a single call to the server and sends all pin values as a batch.
 Server dis-assembles it accordingly and makes multiple publish calls for each sensor type.
 ***********************************************************************************************/

void pushData(){
    String payLoad = "Current";
    payLoad = payLoad +  "\",\"value\":\"";
    
    double irms = emon1.calcIrms(1460);
    payLoad+=irms;
    
    
    payLoad += "\"}";
    
    client.fastrprint(F("POST "));
    client.fastrprint(SERVICE_EPOINT); client.fastrprint(F("pushcurrent"));
    client.fastrprint(F(" HTTP/1.1")); client.fastrprint(F("\n"));
    client.fastrprint(host.c_str()); client.fastrprint(F("\n"));
    client.fastrprint(F("Content-Type: application/json")); client.fastrprint(F("\n"));
    client.fastrprint(F("Content-Length: "));
    
    int payLength = jsonPayLoad.length() + payLoad.length();
    
    client.fastrprint(String(payLength).c_str()); client.fastrprint(F("\n"));
    client.fastrprint(F("\n"));
    
    if(DEBUG) {
        Serial.print("POST ");
        Serial.print(SERVICE_EPOINT);
        Serial.print("pushcurrent");
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
    
    client.fastrprint(F("\n"));
    if(DEBUG) Serial.println();
    
    delay(1000);
    
    
    while (client.available()) {
        char response = client.read();
        if(DEBUG) Serial.print(response);
    }
    
    
    if(DEBUG)  {
        Serial.println();
        Serial.println("-------------------------------");
    }
    
    payLoad = "";
}

