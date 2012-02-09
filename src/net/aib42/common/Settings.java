package net.aib42.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Reads settings.properties
 */
public class Settings {
	
	private static Settings instance = null;
	private static Properties properties = null;
	
	/**
	 * Instantiates the singleton Setting object.
	 */
	public Settings(){
		refresh();
	}
	
	/**
	 * Gets the singleton instance of Settings.
	 *
	 * @return singleton instance of Settings
	 */
	public static Settings getInstance(){
		if(instance == null){
			instance = new Settings();
		}
		return instance;
	}
	
	/**
	 * Returns the port number the server
	 * is going to listen.
	 *
	 * @return the port number
	 */
	public int getPortNumber(){
		return Integer.parseInt(getValue("portNumber", "6666"));
	}
	
	/**
	 * Gets the value from setting file.
	 *
	 * @param key
	 * @param defaultValue The default value if key 
	 * 		is not presented in settings files
	 * @return the value
	 */
	private String getValue(String key, 
			String defaultValue){
		return getProperties().getProperty(key, defaultValue);
	}
	
	/**
	 * Refreshes static properties instance with
	 * settings file.
	 */
	public void refresh(){
		
		// load from settings file.
		// if fails, ignore and we will work
		// with the defaults
		try {
			getProperties().load(new FileInputStream(
					new File("settings.properties")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the properties.
	 *
	 * @return the properties
	 */
	public Properties getProperties(){
		
		if(properties == null){
			properties = new Properties();
		}
		return properties;
	}
}
