#[[
#ifndef WSO2ArduinoAgent_H
#define WSO2ArduinoAgent_H

#if (ARDUINO >= 100)
 #include "Arduino.h"
#else
 #include "WProgram.h"
#endif

#define HTTP_METHOD "POST"
#define HTTP_VERSION "HTTP/1.1"

#define DEVICE_IP "192.168.1.219"
#define DEVICE_TYPE "arduino"
]]#
\#define DEVICE_OWNER "${DEVICE_OWNER}"
\#define DEVICE_ID  "${DEVICE_ID}"
#[[
#define SERVICE_IP "192.168.1.216"
#define SERVICE_PORT 9763 
#define SERVICE_EPOINT "/WSO2ConnectedDevices/DeviceController/pushdata/" 
                                        // {ip}/{owner}/{type}/{mac}/90/{key}/{value}

#define MQTT_PROXY SERVICE_IP
#define MQTT_PORT 1883
#define MQTT_CLIENTID "wso2:iot:arduino:xxxxxxxx"
#define MQTT_TOPIC "wso2/iot/shabirmean/arduino/xxxxxxxx"

#define MQTTCLIENT_QOS2 1
#define PUBLISH_INTERVAL 30000

enum IP_TYPE{
  SERVER,
  DEVICE
};

#endif
]]#
