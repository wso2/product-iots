/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.agent.store;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.wso2.agent.resourcemonitor.pojo.SystemUsage;

public class RecordStore {

	public void saveData(SystemUsage sysUsage) throws Exception {
		BufferedWriter writer = null;
        try {
            //create a temporary file
            File logFile = new File("usagelog");
            // This will output the full path where the file will be written to...
            //System.out.println(logFile.getCanonicalPath());
            writer = new BufferedWriter(new FileWriter(logFile,true));
            writer.write(sysUsage.toString());
            writer.write("\n");
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            }
        }
		
	}

	public ArrayList<SystemUsage> readData() throws Exception {
		ArrayList<SystemUsage> sysUsageList=new ArrayList<SystemUsage>();
		BufferedReader br = null;
        try {
        	// Open the file
    		FileInputStream fstream = new FileInputStream("usagelog");
    		br = new BufferedReader(new InputStreamReader(fstream));
    		String strLine;
    		//Read File Line By Line
    		while ((strLine = br.readLine()) != null)   {
    		  // Print the content on the console
    		  //System.out.println (strLine);
    		  sysUsageList.add(new SystemUsage(strLine));
    		}
        } catch (Exception e) {
            throw e;
        } finally {
            try {
            	br.close();
            } catch (Exception e) {
            }
        }
		return sysUsageList;
	}
	
	
	
}
