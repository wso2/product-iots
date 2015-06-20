#include "ArduinoBoardSketch.h"
#include <Adafruit_CC3000.h>
#include <SPI.h>
#include <avr/wdt.h>
Adafruit_CC3000 cc3000 = Adafruit_CC3000(ADAFRUIT_CC3000_CS, ADAFRUIT_CC3000_IRQ, ADAFRUIT_CC3000_VBAT,
                                         SPI_CLOCK_DIVIDER); // you can change this clock speed

Adafruit_CC3000_Client client;

uint32_t sserver;


void setup()
{
    Serial.begin(9600);
    Serial.println(F("Internal Temperature Sensor"));
    pinMode(6, OUTPUT);
    connectHttp();
    setupResource();
    wdt_enable(WDTO_8S);
}

void loop()
{
    while( !cc3000.checkConnected() ){
        connectHttp();
    
    }
    cpuTemperature=getBoardTemp();


    if(millis() - pushTimestamp > PUSH_INTERVAL){
        while (!client.connected()) {
            setupClient();
        }
        pushData();
        
        pushTimestamp = millis();
    }

    //Serial.println("PUSHED");


    if(millis() - pollTimestamp > POLL_INTERVAL){
        while (!client.connected()) {
            setupClient();
        }
        readControls();
          
        pollTimestamp = millis();
        
    }

    //Serial.println("LOOPING");
    wdt_reset();
}
