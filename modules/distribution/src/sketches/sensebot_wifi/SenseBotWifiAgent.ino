#include "SenseBotWifiAgent.h"
#include <Adafruit_CC3000.h>
#include <SPI.h>
#include <avr/wdt.h>
#include "dht.h"
Adafruit_CC3000 cc3000 = Adafruit_CC3000(ADAFRUIT_CC3000_CS, ADAFRUIT_CC3000_IRQ, ADAFRUIT_CC3000_VBAT,
                                         SPI_CLOCK_DIVIDER); // you can change this clock speed

Adafruit_CC3000_Client client;
Adafruit_CC3000_Server httpServer(LISTEN_PORT);

uint32_t sserver;
dht DHT;

void setup()
{
    if(CON_DEBUG) Serial.begin(9600);
    
    pinMode(PIR_PIN, INPUT);
    pinMode(LDR_PIN, INPUT);
    pinMode(TEMP_PIN, INPUT);
    for(int i = 0; i < 2; i++){
      pinMode(motor_left[i], OUTPUT);
      pinMode(motor_right[i], OUTPUT);
    }
     
    pinMode(enA, OUTPUT);
    pinMode(enB, OUTPUT);
    digitalWrite(enA, 100);
    digitalWrite(enB, 100);
    motor_stop();
  
    do{
       connectHttp();
    }while( !cc3000.checkConnected() );
    
    setupResource();
    httpServer.begin();
    wdt_enable(WDTO_8S);
}

void loop()
{
    while( !cc3000.checkConnected() ){
        connectHttp();
    
    }
    
    if(millis() - pollTimestamp > TURN_DELAY){
      wdt_reset();
        readControls();
        drive();
        pollTimestamp = millis();
        
    }
      //Serial.println(F("looping"));
     getTemperature();
     pir =digitalRead(PIR_PIN);
     getSonar();
     ldr =analogRead(LDR_PIN);
   
    if(millis() - pushTimestamp > PUSH_INTERVAL){
       wdt_reset();
        while (!client.connected()) {
            setupClient();
        }
        pushData();
       // Serial.print("looping");
        pushTimestamp = millis();
    }

    wdt_reset();
}




