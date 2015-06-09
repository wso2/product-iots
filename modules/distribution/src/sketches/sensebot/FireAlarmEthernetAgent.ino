#include "FireAlarmEthernetAgent.h"

#include <Ethernet.h>
#include <SPI.h>
#include "dht.h"

    /**********************************************************************************************  
        0. Check with a sample Ethernet code of the Ethernet library to ensure that the sheild is working
        1. Set the ip of the server(byte array below) where the Web-Rest API for the FireAlarm is running
        2. Check whether the "SERVICE_EPOINT" is correct in the 'FireAlarmWifiAgent.h' file
        3. Check whether the "SERVICE_PORT" is the same (9763) for the server running. Change it if needed
        4. Check whether the pins have been attached accordingly in the Arduino
        5. Check whether all reqquired pins are added to the 'digitalPins' array  
    ***********************************************************************************************/

int digitalPins[] = { TEMP_PIN, BULB_PIN, FAN_PIN };
int analogPins[] = { 0, 1, 2, 3, 4, 5 };

EthernetClient httpClient;
String host, jsonPayLoad, replyMsg;

void setup() {
  if(DEBUG) Serial.begin(9600);
  pinMode(BULB_PIN, OUTPUT);
  pinMode(FAN_PIN, OUTPUT);
  connectHttp();
  setupResource();
}

void loop() {
  if (httpClient.connected()) {    
    pushDigitalPinData();
//    pushData();            //  Use this method to batch all data together and send in one call
    
    delay(POLL_INTERVAL);
    
    String responseMsg = readControls();    
    int index = responseMsg.lastIndexOf(":");
    int newLine = responseMsg.lastIndexOf("\n");
    String subStrn = responseMsg.substring(index + 1);

    if (subStrn.equals("IN")) {
      responseMsg = responseMsg.substring(newLine + 1, index); 
      if (responseMsg.equals("TEMPERATURE")) {
        replyMsg = "Temperature is " + String(getTemperature()) + "C.";
        reply(replyMsg);
      } else if (responseMsg.equals("BULB")) {
        replyMsg = "Bulb was switched " + switchBulb();
      } else if (responseMsg.equals("FAN")) {
        replyMsg = "Bulb was switched " + switchFan();
      }    
    } 
  } else {
    if(DEBUG) {
      Serial.println("client not found...");
      Serial.println("disconnecting.");
    }
    httpClient.stop();
    connectHttp();

  }  
}


String getDataType(int pin){
  switch(pin){
    case TEMP_PIN:
      return "Temperature";
    case BULB_PIN:
      return "Bulb";
    case FAN_PIN:
      return "Fan";
    default:
      return String(pin);
  }

}

String switchBulb() {
    if (digitalRead(BULB_PIN) == HIGH) {
      digitalWrite(BULB_PIN, LOW);
      return "OFF";
    } else {
      digitalWrite(BULB_PIN, HIGH);
      return "ON";
    }
}

String switchFan() {
    if (digitalRead(FAN_PIN) == HIGH) {
      digitalWrite(FAN_PIN, LOW);
      return "OFF";
    } else {
      digitalWrite(FAN_PIN, HIGH);
      return "ON";
    }
}


double getTemperature(){
  dht DHT;
  if(DEBUG) {
    Serial.println("-------------------------------");
    Serial.println("Type,\tstatus,\tHumidity (%),\tTemperature (C)");
    Serial.print("DHT11, \t");
  }
  
  int chk = DHT.read11(TEMP_PIN);
  switch (chk)
  {
    case DHTLIB_OK:  
		if(DEBUG) Serial.print("OK,\t"); 
		break;
    case DHTLIB_ERROR_CHECKSUM: 
		if(DEBUG) Serial.print("Checksum error,\t"); 
		break;
    case DHTLIB_ERROR_TIMEOUT: 
		if(DEBUG) Serial.print("Time out error,\t"); 
		break;
    case DHTLIB_ERROR_CONNECT:
                if(DEBUG) Serial.print("Connect error,\t");
                break;
    case DHTLIB_ERROR_ACK_L:
                if(DEBUG) Serial.print("Ack Low error,\t");
                break;
    case DHTLIB_ERROR_ACK_H:
                if(DEBUG) Serial.print("Ack High error,\t");
                break;
    default: 
	        if(DEBUG) Serial.print("Unknown error,\t"); 
		break;
  }
  
                        // DISPLAY DATA
  if(DEBUG) {
    Serial.print("\t");
    Serial.print(DHT.temperature, 1);
    Serial.print(",\t\t");
    Serial.println(DHT.humidity, 1);
    Serial.println("-------------------------------");
  }
  return DHT.temperature;
}
