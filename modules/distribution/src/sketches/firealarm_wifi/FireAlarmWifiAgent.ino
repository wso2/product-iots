#include "FireAlarmWifiAgent.h"

#include <Adafruit_CC3000.h>
#include <avr/wdt.h>
#include <SPI.h>
#include "dht.h"

Adafruit_CC3000 cc3000 = Adafruit_CC3000(ADAFRUIT_CC3000_CS, ADAFRUIT_CC3000_IRQ, ADAFRUIT_CC3000_VBAT,
                                         SPI_CLOCK_DIVIDER); // you can change this clock speed

Adafruit_CC3000_Client pushClient;
Adafruit_CC3000_Server httpServer(LISTEN_PORT);
uint32_t sserver;

/**********************************************************************************************  
    0. Check with a sample Wifi code of the Adafruit_CC3000 library to ensure that the sheild is working
    1. Set the ip of the server(byte array below) where the Web-Rest API for the FireAlarm is running
    2. Check whether the "SERVICE_EPOINT" is correct in the 'FireAlarmWifiAgent.h' file
    3. Check whether the "SERVICE_PORT" is the same (9763) for the server running. Change it if needed
    4. Check whether the pins have been attached accordingly in the Arduino
    5. Check whether all reqquired pins are added to the 'digitalPins' array  
***********************************************************************************************/
void setup() {
    if (CON_DEBUG) Serial.begin(9600);

    pinMode(BULB_PIN, OUTPUT);
    pinMode(FAN_PIN, OUTPUT);
    pinMode(TEMP_PIN, INPUT);
    setupResource();

    do {
        connectHttp();
    } while (!cc3000.checkConnected());

    while (!pushClient.connected()) {
        setupClient();
    }

    registerIP();
    
    httpServer.begin();
    wdt_enable(WDTO_8S);
}

void loop() {


    while (!cc3000.checkConnected()) {
        connectHttp();

    }
    wdt_reset();

    if (millis() - pushTimestamp > PUSH_INTERVAL) {
        wdt_reset();
        while (!pushClient.connected()) {
            setupClient();
        }
        pushData();
        pushTimestamp = millis();
    }
    readControls();
    wdt_reset();

    if (subStrn.equals("ON")) {
        if (responseMsg.equals("BULB")) {
            digitalWrite(BULB_PIN, HIGH);
        } else if (responseMsg.equals("FAN")) {
            digitalWrite(FAN_PIN, HIGH);
        }
    } else if (subStrn.equals("OFF")) {
        if (responseMsg.equals("BULB")) {
            digitalWrite(BULB_PIN, LOW);
        } else if (responseMsg.equals("FAN")) {
            digitalWrite(FAN_PIN, LOW);
        }
    }
    
    wdt_reset();

}
