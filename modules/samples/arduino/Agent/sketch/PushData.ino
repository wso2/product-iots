#include "ArduinoBoardSketch.h"

/**********************************************************************************************
 This method will traverse the array of digital pins and batch the data from the those pins together.
 It makes a single call to the server and sends all pin values as a batch.
 Server dis-assembles it accordingly and makes multiple publish calls for each sensor type.
 ***********************************************************************************************/

void pushData(){
    String payLoad = "Data";
    payLoad = payLoad +  "\",\"value\":\"";
    
    
    payLoad+=cpuTemperature;
    
    
    payLoad += "\"}";
    
    client.fastrprint(F("POST "));
    client.fastrprint(SERVICE_EPOINT); client.fastrprint(F("pushdata"));
    client.fastrprint(F(" HTTP/1.1")); client.fastrprint(F("\n"));
    client.fastrprint(host.c_str()); client.fastrprint(F("\n"));
    client.fastrprint(F("Content-Type: application/json")); client.fastrprint(F("\n"));
    client.fastrprint(F("Content-Length: "));
    
    int payLength = jsonPayLoad.length() + payLoad.length();
    
    client.fastrprint(String(payLength).c_str()); client.fastrprint(F("\n"));
    client.fastrprint(F("\n"));
    
    if(DEBUG) {
        Serial.print("POST ");
        Serial.print(SERVICE_EPOINT); Serial.print("pushdata");
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




double getBoardTemp(void)
{
    unsigned int wADC;
    double t;
    
    // The internal temperature has to be used
    // with the internal reference of 1.1V.
    // Channel 8 can not be selected with
    // the analogRead function yet.
    
    // Set the internal reference and mux.
    ADMUX = (_BV(REFS1) | _BV(REFS0) | _BV(MUX3));
    ADCSRA |= _BV(ADEN);  // enable the ADC
    
    delay(20);            // wait for voltages to become stable.
    
    ADCSRA |= _BV(ADSC);  // Start the ADC
    
    // Detect end-of-conversion
    while (bit_is_set(ADCSRA,ADSC));
    
    // Reading register "ADCW" takes care of how to read ADCL and ADCH.
    wADC = ADCW;
    
    // The offset of 324.31 could be wrong. It is just an indication.
    t = (wADC - 324.31 ) / 1.22;
    
    // The returned temperature is in degrees Celcius.
    return (t);
}

