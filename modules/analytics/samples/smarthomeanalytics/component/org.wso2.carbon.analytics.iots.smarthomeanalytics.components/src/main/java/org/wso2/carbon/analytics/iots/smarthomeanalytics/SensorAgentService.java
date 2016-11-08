/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.iot.smarthomeanalytics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.iot.smarthomeanalytics.agents.*;
import org.wso2.iot.smarthomeanalytics.constants.Constants;
import org.wso2.iot.smarthomeanalytics.utils.DataPublisherUtil;
import org.wso2.carbon.databridge.agent.AgentHolder;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.*;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.*;

public class SensorAgentService {
    private static final Log log = LogFactory.getLog(SensorAgentService.class);
    public static volatile ExecutorService executorService;
    public static volatile DataPublisher dataPublisher;

    public static void main(String[] args) {

        DataPublisherUtil.setTrustStoreParams();
        AgentHolder.setConfigPath(DataPublisherUtil.getAgentConfigPath());
        initialization();
        try {
            log.info("Started pushing sensor meta information...");
            int numberOfMetaInformationThreads = 3;
            final CountDownLatch countDownLatchForMetaInformation = new CountDownLatch(numberOfMetaInformationThreads);
            executorService.submit(new LocationInformationAgent(dataPublisher, countDownLatchForMetaInformation
                    , Constants.LOCATION_DATA_RESOURCE, Constants.LOCATION_STREAM_DEFINITION
                    , Constants.LOCATION_DATA_PUSH_INTERVAL));
            executorService.submit(
                    new SensorMetaInformationAgent(dataPublisher, countDownLatchForMetaInformation
                            , Constants.SENSOR_INFORMATION_DATA_RESOURCE, Constants.SENSOR_INFORMATION_STREAM_DEFINITION
                            , Constants.SENSOR_INFORMATION_DATA_PUSH_INTERVAL));
            executorService.submit(new HousesInformationAgent(dataPublisher, countDownLatchForMetaInformation
                    , Constants.HOUSE_DATA_RESOURCE, Constants.HOUSE_STREAM_DEFINITION
                    , Constants.HOUSE_DATA_PUSH_INTERVAL));
            countDownLatchForMetaInformation.await();
            log.info("Started pushing sensor reading...");
            int numberOfSensorAgentThreads = 7;
            final CountDownLatch countDownLatchForSensorAgents = new CountDownLatch(numberOfSensorAgentThreads);
            executorService.submit(new SmartDoorSensorAgent(dataPublisher, countDownLatchForSensorAgents
                    , Constants.SMART_DOOR_SENSOR_DATA_RESOURCE
                    , Constants.SMART_DOOR_STREAM_DEFINITION
                    , Constants.SMART_DOOR_STREAM_DATA_PUSH_INTERVAL));
            executorService.submit(new SmartSwitchSensorAgent(dataPublisher, countDownLatchForSensorAgents
                    , Constants.SMART_SWITCH_SENSOR_DATA_RESOURCE
                    , Constants.SMART_SWITCH_STREAM_DEFINITION
                    , Constants.SMART_SWITCH_STREAM_DATA_PUSH_INTERVAL));
            executorService.submit(new SmartWeatherMonitoringSensorAgent(dataPublisher, countDownLatchForSensorAgents
                    , Constants.SMART_WEATHER_MONITORING_SENSOR_SENSOR_DATA_RESOURCE
                    , Constants.SMART_WEATHER_MONITORING_SENSOR_STREAM_DEFINITION
                    , Constants.SMART_WEATHER_MONITORING_SENSOR_STREAM_DATA_PUSH_INTERVAL));
            executorService.submit(new SmartMotionDetectorSensorAgent(dataPublisher, countDownLatchForSensorAgents
                    , Constants.SMART_MOTION_DETECTOR_SENSOR_DATA_RESOURCE
                    , Constants.SMART_MOTION_DETECTOR_STREAM_DEFINITION
                    , Constants.SMART_MOTION_DETECTOR_STREAM_DATA_PUSH_INTERVAL));
            executorService.submit(new SmartFurnaceSensorAgent(dataPublisher, countDownLatchForSensorAgents
                    , Constants.SMART_FURNCE_SENSOR_DATA_RESOURCE
                    , Constants.SMART_FURNCE_STREAM_DEFINITION
                    , Constants.SMART_FURNCE_STREAM_DATA_PUSH_INTERVAL));
            executorService.submit(new SmartDHTSensorAgent(dataPublisher, countDownLatchForSensorAgents
                    , Constants.SMART_DHTSENSOR_SENSOR_DATA_RESOURCE
                    , Constants.SMART_DHTSENSOR_STREAM_DEFINITION
                    , Constants.SMART_DHTSENSOR_STREAM_DATA_PUSH_INTERVAL));
            executorService.submit(new SmartMeterSensorAgent(dataPublisher, countDownLatchForSensorAgents
                    , Constants.SMART_METER_SENSOR_DATA_RESOURCE
                    , Constants.SMART_METER_STREAM_DEFINITION
                    , Constants.SMART_METER_STREAM_DATA_PUSH_INTERVAL));
            countDownLatchForSensorAgents.await();
            log.warn("===============================END=====================================");
        } catch (InterruptedException e) {
            log.error("Error while executing sensor agents, ", e);
        } catch (NullPointerException e) {
            log.error("Sensor data set can't be empty ", e);
        } finally {
            try {
                executorService.shutdown();
                dataPublisher.shutdownWithAgent();
            } catch (DataEndpointException e) {
                log.error("Error while shutting down data publisher, ", e);
            }
        }
    }

    public static void initialization() {
        try {
            dataPublisher = new DataPublisher(Constants.TRASPORT_LEVEL_PROTOCOL + Constants.SERVER_IP_ADDRESS + ":"
                    + Constants.SERVER_PORT, Constants.USERNAME, Constants.PASSWORD);
            if (executorService == null) {
                RejectedExecutionHandler rejectedExecutionHandler = new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        try {
                            executor.getQueue().put(r);
                        } catch (InterruptedException e) {
                            log.error("Exception while adding event to executor queue : " + e.getMessage(), e);
                        }
                    }
                };
                executorService = new ThreadPoolExecutor(Constants.ADAPTER_MIN_THREAD_POOL_SIZE
                        , Constants.ADAPTER_MAX_THREAD_POOL_SIZE, Constants.DEFAULT_KEEP_ALIVE_TIME_IN_MILLS
                        , TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(Constants
                        .ADAPTER_EXECUTOR_JOB_QUEUE_SIZE), rejectedExecutionHandler);
            }
        } catch (DataEndpointConfigurationException e) {
            log.error("Required fields are missing in Data endpoint configuration file ", e);
        } catch (DataEndpointException e) {
            log.error("Error while connecting to configured endpoint ", e);
        } catch (DataEndpointAgentConfigurationException e) {
            log.error("Required fields are missing in Data endpoint agent configuration file ", e);
        } catch (TransportException e) {
            log.error("Error while connecting to server through Thrift", e);
        } catch (DataEndpointAuthenticationException e) {
            log.error("Please check whether user name and password is correct,", e);
        }
    }
}
