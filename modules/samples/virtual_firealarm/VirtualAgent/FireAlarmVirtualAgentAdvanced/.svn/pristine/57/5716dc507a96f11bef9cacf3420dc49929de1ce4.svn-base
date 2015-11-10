/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.device.mgt.iot.agent.firealarm.sidhdhi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.core.AgentConstants;
import org.wso2.carbon.device.mgt.iot.agent.firealarm.core.AgentManager;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This class reads the sonar reading and injects values
 * to the siddhiEngine for processing on a routine basis
 * also if the siddhiquery is updated the class takes
 * care of re-initializing same.
 */
public class SidhdhiQuery implements Runnable {
	private static final Log log = LogFactory.getLog(SidhdhiQuery.class);
	final AgentConstants constants = new AgentConstants();

	//Bam data push client
	private static SiddhiManager siddhiManager = new SiddhiManager();

	public static SiddhiManager getSiddhiManager() {
		return siddhiManager;
	}

	public static void setSiddhiManager(SiddhiManager siddhiManager) {
		SidhdhiQuery.siddhiManager = siddhiManager;
	}

	public void run() {

		//Start the execution plan with pre-defined or previously persisted Siddhi query

		String sidhdhiQueryPath =
				AgentManager.getInstance().getRootPath() + AgentConstants.CEP_FILE_NAME;

		File f = new File(sidhdhiQueryPath);
		if (!f.exists()) {
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(sidhdhiQueryPath);
				out.write(AgentConstants.CEP_QUERY.getBytes(StandardCharsets.UTF_8));
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
                if (out != null){
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

		StartExecutionPlan startExecutionPlan = new StartExecutionPlan().invoke();

		while (true) {

			//Check if there is new policy update available
			if (AgentManager.isUpdated()) {
				System.out.print("### Policy Update Detected!");
				//Restart execution plan with new query
				restartSiddhi();
				startExecutionPlan = new StartExecutionPlan().invoke();
			}
			InputHandler inputHandler = startExecutionPlan.getInputHandler();

			//Sending events to Siddhi
			try {
				int sonarReading = AgentManager.getInstance().getTemperature();
				inputHandler.send(new Object[]{"FIRE_1", sonarReading});
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
		}
	}

	/**
	 * Re-Initialize SiddhiManager
	 */
	private void restartSiddhi() {
		siddhiManager.shutdown();
		siddhiManager = new SiddhiManager();
	}


	/**
	 * Read content from a given file and return as a string
	 *
	 * @param path
	 * @param encoding
	 * @return
	 */
	public static String readFile(String path, Charset encoding) {
		byte[] encoded = new byte[0];
		try {
			encoded = Files.readAllBytes(Paths.get(path));
		} catch (IOException e) {
			log.error("Error reading Sidhdhi query from file.");
		}
		return new String(encoded, encoding);
	}


	/**
	 * Read sonar data from API URL
	 *
	 * @param sonarAPIUrl
	 * @return
	 */
	private String readSonarData(String sonarAPIUrl) {
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(sonarAPIUrl);
		String responseStr = null;
		try {
			HttpResponse response = client.execute(request);
			log.debug("Response Code : " + response);
			InputStream input = response.getEntity().getContent();
			BufferedReader br = new BufferedReader(new InputStreamReader(input, "UTF-8"));
			responseStr = String.valueOf(br.readLine());
			br.close();

		} catch (IOException e) {
			//log.error("Exception encountered while trying to make get request.");
			log.error("Error while reading sonar reading from file!");
			return responseStr;
		}
		return responseStr;
	}

	/**
	 * Initialize SiddhiExecution plan
	 */
	private static class StartExecutionPlan {
		private InputHandler inputHandler;

		public InputHandler getInputHandler() {
			return inputHandler;
		}

		public StartExecutionPlan invoke() {
			String executionPlan;

			String sidhdhiQueryPath =
					AgentManager.getInstance().getRootPath() + AgentConstants.CEP_FILE_NAME;
			executionPlan = readFile(sidhdhiQueryPath, StandardCharsets.UTF_8);

			//Generating runtime
			siddhiManager.addExecutionPlan(executionPlan);

			siddhiManager.addCallback("bulbOnStream", new StreamCallback() {
				@Override
				public void receive(Event[] events) {
					System.out.println("Bulb on Event Fired!");
					if (events.length > 0) {
						if (!AgentManager.getInstance().isAlarmOn()) {
							AgentManager.getInstance().changeAlarmStatus(true);
							System.out.println("#### Performed HTTP call! ON.");
						}
					}
				}
			});

			siddhiManager.addCallback("bulbOffStream", new StreamCallback() {
				@Override
				public void receive(Event[] inEvents) {
					System.out.println("Bulb off Event Fired");
					if (AgentManager.getInstance().isAlarmOn()) {
						AgentManager.getInstance().changeAlarmStatus(false);
						System.out.println("#### Performed HTTP call! OFF.");
					}
				}

			});

			//Retrieving InputHandler to push events into Siddhi
			inputHandler = siddhiManager.getInputHandler("fireAlarmEventStream");

			//Starting event processing
			System.out.println("Execution Plan Started!");
			return this;
		}
	}
}
