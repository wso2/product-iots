#include "IoTArduinoAgent.h"

#include <Ethernet.h>
#include <SPI.h>
#include <IPStack.h>
#include <Countdown.h>
#include <MQTTClient.h>

EthernetClient httpClient;
int digitalPins[] = { 2, 5, 8, 9, 12 };
int analogPins[] = { 0, 1, 2, 3, 4, 5 };

void setup() {
  Serial.begin(9600);
  connectHttp();
  setupResource();
  MQTTConnect();
}

void loop() {
  if (httpClient.connected()) {
//    pushDigitalPinData();
//    pushAnalogPinData();
  } else {
    Serial.println("client not found...");
    Serial.println("disconnecting.");
    httpClient.stop();
//    connectHttp();
//    for(;;)
//      ;
  }  
  delay(PUBLISH_INTERVAL);

}


