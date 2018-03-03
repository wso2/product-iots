FROM openjdk:8u111-jdk
COPY wso2iot-4.0.0-SNAPSHOT.zip .
RUN apt-get update && apt-get install unzip
RUN unzip wso2iot-4.0.0-SNAPSHOT.zip -d wso2iot
EXPOSE 9090
ENTRYPOINT ["wso2iot/wso2iot-4.0.0-SNAPSHOT/bin/iotserver.sh"]
