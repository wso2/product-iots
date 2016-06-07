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
