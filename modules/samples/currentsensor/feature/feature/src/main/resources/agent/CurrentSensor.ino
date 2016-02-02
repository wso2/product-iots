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
#include <Adafruit_CC3000.h>
#include <EmonLib.h>
#include <SPI.h>
#include <avr/wdt.h>
Adafruit_CC3000 cc3000 = Adafruit_CC3000(ADAFRUIT_CC3000_CS, ADAFRUIT_CC3000_IRQ, ADAFRUIT_CC3000_VBAT,
                                         SPI_CLOCK_DIVIDER); // you can change this clock speed

Adafruit_CC3000_Client client;
EnergyMonitor emon1;

uint32_t sserver;


void setup()
{
    Serial.begin(115200);
    Serial.println(F("Current Sensor"));
    emon1.current(1, 111.1);
    connectHttp();
    setupResource();
    wdt_enable(WDTO_8S);
   
}

void loop()
{
  wdt_reset();
    while( !cc3000.checkConnected() ){
        connectHttp();
    
    }

    if(millis() - pushTimestamp > PUSH_INTERVAL){
        while (!client.connected()) {
            setupClient();
        }
        pushData();
        
        pushTimestamp = millis();
    }

    //Serial.println("PUSHED");


      wdt_reset();
    
}
