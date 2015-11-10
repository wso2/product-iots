/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 */

package org.wso2.carbon.iot.android.sense.events.input;



import android.content.Context;

import org.wso2.carbon.iot.android.sense.events.input.DataReader;
import org.wso2.carbon.iot.android.sense.events.input.Location.LocationDataReader;
import org.wso2.carbon.iot.android.sense.events.input.Sensor.SensorDataReader;

public class SenseDataCollector {
	public enum DataType {
	    SENSOR ,LOCATION
	};
	
	public SenseDataCollector(Context ctx, DataType dt) {


		try{
			DataReader dr=null;
			
			switch(dt){
				case SENSOR: dr=new SensorDataReader(ctx);
					break;


				case LOCATION:	dr=new LocationDataReader(ctx);
				break;
			}
			
			Thread DataCollector =new Thread(dr);
			DataCollector.start();
		}catch(NullPointerException e){
			
			
			
		}
	}
}
