#ifndef ArduinoWifiAgent_H
#define ArduinoWifiAgent_H

#include "Arduino.h"

// These are the interrupt and control pins
#define ADAFRUIT_CC3000_IRQ   3  // MUST be an interrupt pin!
// These can be any two pins
#define ADAFRUIT_CC3000_VBAT  5
#define ADAFRUIT_CC3000_CS    10

#define WLAN_SSID       "linksys"           // cannot be longer than 32 characters!
#define WLAN_PASS       "ramsgate717"

#define WLAN_SECURITY   WLAN_SEC_WPA
// Security can be WLAN_SEC_UNSEC, WLAN_SEC_WEP, WLAN_SEC_WPA or WLAN_SEC_WPA2
#define IDLE_TIMEOUT_MS  3000

#define DEVICE_OWNER "${DEVICE_OWNER}"          
#define DEVICE_ID "${DEVICE_ID}"              
#define DEVICE_TOKEN "${DEVICE_TOKEN}"

#define SERVICE_PORT 9763
#define SERVICE_EPOINT "/sensebot/controller/"

#define BUZZER A0
#define LDR_PIN  A1
#define TEMP_PIN A2
#define PIR_PIN  A3
#define SONAR_TRIG  A4
#define SONAR_ECHO  A5

#define BUZZER_SOUND 100
#define MAX_DISTANCE 30

#define TURN_DELAY 100

#define PUSH_INTERVAL 30000
#define MAX_ACTION            6      // Maximum length of the HTTP action that can be parsed.

#define MAX_PATH              10
#define BUFFER_SIZE           MAX_ACTION + MAX_PATH + 10  // Size of buffer for incoming request data.

#define TIMEOUT_MS            500 

#define DEBUG false
#define CON_DEBUG false



byte server[4] = { 192, 168, 1, 101 };
String host, jsonPayLoad;
 unsigned long pushTimestamp = 0;
 unsigned long pollTimestamp = 0;

#define LISTEN_PORT           80

byte motion_global = 0;
byte temperature =  0;
int pir =0;
int sonar=0;
int ldr =0;

const byte motor_left[] = {7, 8};
const byte enA = 12;

const byte motor_right[] = {4, 6};
const byte enB = 11;
String urlPath;

#endif



