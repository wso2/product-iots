void messageArrived(MQTT::MessageData& md);
int arrivedcount = 0;
MQTT::Message message;

EthernetClient mqtt; // replace by a YunClient if running on a Yun
IPStack mqttIPStack(mqtt);
MQTT::Client<IPStack, Countdown, 100, 1> mqttClient = MQTT::Client<IPStack, Countdown, 100, 1>(mqttIPStack);


void MQTTConnect() {
  int rc = -1;
  if (!mqttClient.isConnected()) {
    Serial.print("Connecting using Registered mode with clientid : ");
    Serial.println(MQTT_CLIENTID);
    Serial.print("\tto MQTT Broker : ");
    Serial.println(MQTT_PROXY);
    Serial.print("\ton topic : ");
    Serial.println(MQTT_TOPIC);
    while (rc != 0) {
      rc = mqttIPStack.connect(MQTT_PROXY, MQTT_PORT);
//      rc = mqtt.connect(server, MQTT_PORT);
      Serial.print("rc from TCP connect is ");
      Serial.println(rc);
    }
    
    Serial.println("MQTT connecting");
    MQTTPacket_connectData options = MQTTPacket_connectData_initializer;
    options.MQTTVersion = 3;
    options.clientID.cstring = MQTT_CLIENTID;
//    options.username.cstring = "admin";
//    options.password.cstring = "admin";
//    options.keepAliveInterval = 10;
    rc = -1;
    while ((rc = mqttClient.connect(options)) != 0){
      Serial.print("rc from MQTT connect is ");
      Serial.println(rc);
    }
    //unsubscribe the topic, if it had subscribed it before.
    Serial.println("MQTT connected");
    mqttClient.unsubscribe(MQTT_TOPIC);
    //Try to subscribe for commands
    if ((rc = mqttClient.subscribe(MQTT_TOPIC, MQTT::QOS2, messageArrived)) != 0) {
            Serial.print("Subscribe failed with return code : ");
            Serial.println(rc);
    } else {
          Serial.println("Subscribed\n");
    }
    Serial.println("Subscription tried......");
    Serial.println("Connected successfully\n");
    Serial.println("____________________________________________________________________________");
  }
  

}


void messageArrived(MQTT::MessageData& md)
{
  MQTT::Message &message = md.message;
    Serial.println("=======================================");
  Serial.print("Message ");
//  Serial.print(++arrivedcount);
  Serial.print(" arrived: qos ");
  Serial.print(message.qos);
  Serial.print(", retained ");
  Serial.print(message.retained);
  Serial.print(", dup ");
  Serial.print(message.dup);
  Serial.print(", packetid ");
  Serial.println(message.id);
  Serial.print("Payload ");
  Serial.println((char*)message.payload);
    Serial.println("=======================================");
    delay(1000);
}