#include "Arduinoboardwifi.h"

#include <Adafruit_CC3000.h>
#include <SPI.h>

Adafruit_CC3000 cc3000 = Adafruit_CC3000(ADAFRUIT_CC3000_CS, ADAFRUIT_CC3000_IRQ, ADAFRUIT_CC3000_VBAT,
                                         SPI_CLOCK_DIVIDER); // you can change this clock speed

Adafruit_CC3000_Client pushClient;
Adafruit_CC3000_Client pollClient;

uint32_t sserver;

    /**********************************************************************************************  
        0. Check with a sample Wifi code of the Adafruit_CC3000 library to ensure that the sheild is working
        1. Set the ip of the server(byte array below) where the Web-Rest API for the FireAlarm is running
        2. Check whether the "SERVICE_EPOINT" is correct in the 'FireAlarmWifiAgent.h' file
        3. Check whether the "SERVICE_PORT" is the same (9763) for the server running. Change it if needed
        4. Check whether the pins have been attached accordingly in the Arduino
        5. Check whether all reqquired pins are added to the 'digitalPins' array  
    ***********************************************************************************************/

byte server[4] = { 192, 168, 1, 101 };
String host, jsonPayLoad, replyMsg;
String responseMsg, subStrn;

void setup()
{
  Serial.begin(9600);

  Serial.println(F("Internal Temperature Sensor"));
  pinMode(6, OUTPUT);
  connectHttp();
  setupResource();
}

void loop()
{
  
  if (pushClient.connected() && pollClient.connected()) {   
    pushData();                    
    delay(POLL_INTERVAL);
    
    boolean valid = readControls();
    
    responseMsg="";
  } else {
    if(DEBUG) {
      Serial.println("client not found...");
      Serial.println("disconnecting.");
    }
    pushClient.close();
    pollClient.close();
    cc3000.disconnect();  
   
    connectHttp();
  }  
  
 
  delay(1000);
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


