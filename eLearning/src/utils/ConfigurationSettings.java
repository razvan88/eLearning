package utils;


import java.io.File;

import org.ini4j.Ini;

public class ConfigurationSettings {
	public static final String SERVER_SECTION = "server";
	public static final String SCHOOLS_SECTION = "schools";
	
	private static final String configFileName = "config.ini";
	private static Ini iniFile;
	
	static{
		loadConfigFile();
	}
	
	private ConfigurationSettings() {}
	
	private static void loadConfigFile() {
		iniFile = new Ini();
		try {
			iniFile.load(new File(configFileName));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getValue(String sectionName, String key) {
		Ini.Section section = iniFile.get(sectionName);
		return section.get(key);
	}
}
