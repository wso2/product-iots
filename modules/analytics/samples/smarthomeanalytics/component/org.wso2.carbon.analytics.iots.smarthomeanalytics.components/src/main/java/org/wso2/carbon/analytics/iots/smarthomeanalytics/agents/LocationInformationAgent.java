/*
  ~ Copyright (c) 2016  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file except
  ~ in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied. See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
*/

package org.wso2.iot.smarthomeanalytics.agents;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.iot.smarthomeanalytics.SensorAgentService;
import org.wso2.iot.smarthomeanalytics.constants.Constants;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class LocationInformationAgent implements Runnable {
    private static final Log log = LogFactory.getLog(SensorAgentService.class);
    private final CountDownLatch countDownLatch;
    private String streamId;
    private DataPublisher dataPublisher;
    private String sensorDataResource;
    private int sensorDataPushInterval;

    public LocationInformationAgent(DataPublisher dataPublisher, CountDownLatch countDownLatch
            , String sensorDataResource, String streamDefinition, int sensorDataPushInterval) {
        this.streamId = DataBridgeCommonsUtils.generateStreamId(streamDefinition, Constants.VERSION);
        this.dataPublisher = dataPublisher;
        this.sensorDataResource = this.getClass().getResource(sensorDataResource).getPath();
        this.sensorDataPushInterval = sensorDataPushInterval;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        Scanner scanner = null;
        try {
            scanner = new Scanner(new FileInputStream(sensorDataResource));
            while (scanner.hasNextLine()) {
                String[] sensorReading = scanner.nextLine().split(",");
                String[] metaInfo = sensorReading[0].split(":");
                Object[] payLoad = new Object[]{metaInfo[0], metaInfo[1], metaInfo[0] + "-" + metaInfo[1]
                        , sensorReading[1], sensorReading[2], sensorReading[3]};
                Event event = new Event(streamId, System.currentTimeMillis(), null, null, payLoad);
                dataPublisher.publish(event);
                Thread.sleep(sensorDataPushInterval);
            }
            countDownLatch.countDown();
        } catch (InterruptedException e) {
            log.error("Error has been occurred while pushing data to server, " + e);
        } catch (FileNotFoundException e) {
            log.error("Requested resource: " + sensorDataResource + " does not exist, " + e);
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }
}
