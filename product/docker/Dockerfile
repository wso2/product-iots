FROM openjdk:8u111-jdk
COPY /maven/wso2iot.zip .
RUN apt-get update && apt-get install unzip
RUN unzip wso2iot.zip -d wso2am
EXPOSE 9090
ENTRYPOINT ["wso2iot/WSO2IoT-4.0.0-SNAPSHOT/bin/carbon.sh"]