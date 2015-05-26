#ifndef FireAlarmWifiAgent_H
#define FireAlarmWifiAgent_H

#if (ARDUINO >= 100)
 #include "Arduino.h"
#else
 #include "WProgram.h"
#endif

// These are the interrupt and control pins
#define ADAFRUIT_CC3000_IRQ   3  // MUST be an interrupt pin!
// These can be any two pins
#define ADAFRUIT_CC3000_VBAT  5
#define ADAFRUIT_CC3000_CS    10

#define WLAN_SSID       "Dialog 4G"     // cannot be longer than 32 characters!
#define WLAN_PASS       "FA09C543"
#define WLAN_SECURITY   WLAN_SEC_WPA2
                           // Security can be WLAN_SEC_UNSEC, WLAN_SEC_WEP, WLAN_SEC_WPA or WLAN_SEC_WPA2
#define IDLE_TIMEOUT_MS  3000      

#define DEVICE_OWNER "${DEVICE_OWNER}"
#define DEVICE_TYPE "FireAlarm"
#define DEVICE_ID  "${DEVICE_ID}"

#define SERVICE_PORT 9763 
#define SERVICE_EPOINT "/WSO2ConnectedDevices/FireAlarmController/" 
                                        // pushalarmdata - application/json - {"owner":"","deviceId":"","replyMessage":"","time":"","key":"","value":""}
                                        // readcontrols/{owner}/{deviceId}
                                        // reply - application/json - {"owner":"","deviceId":"","replyMessage":""}

#define TEMP_PIN 6
#define BULB_PIN 7
#define FAN_PIN 8

#define POLL_INTERVAL 1000
#define DEBUG false

#endif


