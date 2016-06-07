package org.wso2.agent.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.wso2.agent.resourcemonitor.pojo.SystemSpec;

public class Configuration {
	
	public boolean checkRegistered(){
		boolean registered=false;
		
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream("config.properties");
			// load a properties file
			prop.load(input);
			// get the property value and print it out
			registered= Boolean.valueOf(prop.getProperty("registered"));
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}		
		return registered;
	}
	
	
	public Object getProperty(String property){
		Object registered=null;
		
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream("config.properties");
			// load a properties file
			prop.load(input);
			// get the property value and print it out
			registered= prop.getProperty(property);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}		
		return registered;
	}
	
	public Object getInitProperty(String property){
		Object registered=null;
		
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream("deviceConfig.properties");
			// load a properties file
			prop.load(input);
			// get the property value and print it out
			registered= prop.getProperty(property);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}		
		return registered;
	}

	public void SaveRegistration(SystemSpec spec, String devicetoken, String devicerefreshtoken) {
		Properties prop = new Properties();
		OutputStream output = null;
		try {
			output = new FileOutputStream("config.properties");
			// set the properties value
			prop.setProperty("registered", "true");
			prop.setProperty("deviceid", spec.getMacaddress());
			prop.setProperty("devicetoken", devicetoken);
			prop.setProperty("devicerefreshtoken", devicerefreshtoken);
			// save properties to project root folder
			prop.store(output, null);

		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

}
