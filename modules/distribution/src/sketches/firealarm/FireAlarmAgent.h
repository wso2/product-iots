#[[
#ifndef FireAlarmAgent_H
#define FireAlarmAgent_H

#if (ARDUINO >= 100)
 #include "Arduino.h"
#else
 #include "WProgram.h"
#endif

#define HTTP_POST "POST"
#define HTTP_GET "GET"
#define HTTP_VERSION "HTTP/1.1"
#define HTTP_CONTENT_TYPE "Content-Type: application/json"
#define HTTP_CONTENT_LEN "Content-Length: "
#define DEVICE_TYPE "FireAlarm"
]]#
\#define DEVICE_OWNER "${DEVICE_OWNER}"
\#define DEVICE_ID  "${DEVICE_ID}"
#[[
#define PUSH_ALARM_DATA "pushalarmdata"
#define READ_CONTROLS "readcontrols/"
#define REPLY "reply"

#define OWNER_JSON "{\"owner\":\""
#define DEVICE_ID_JSON "\",\"deviceId\":\""
#define REPLY_JSON "\",\"replyMessage\":\""
#define TIME_JSON "\",\"time\":\""
#define KEY_JSON "\",\"key\":\""
#define VALUE_JSON "\",\"value\":\""
#define END_JSON "\"}"

#define SERVICE_PORT 9763 
#define SERVICE_EPOINT "/WSO2ConnectedDevices/FireAlarmController/" 
                                        // pushalarmdata - application/json - {"owner":"","deviceId":"","replyMessage":"","time":"","key":"","value":""}
                                        // readcontrols/{owner}/{deviceId}
                                        // reply - application/json - {"owner":"","deviceId":"","replyMessage":""}

#define TEMP_PIN 3
#define BULB_PIN 4
#define FAN_PIN 5

#define POLL_INTERVAL 1000

#endif
]]#

