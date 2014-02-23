package utils;


import java.io.File;
import java.util.HashMap;

import org.ini4j.Ini;

public class ConfigurationSettings {
	public static final String SERVER_SECTION = "server";
	public static final String SCHOOLS_SECTION = "schools";
	
	private static final String configFileName = "config.ini";
	private static Ini iniFile;
	private static HashMap<Integer, String> sSchoolsMap;
	
	static{
		loadConfigFile();
		sSchoolsMap = new HashMap<Integer, String>();
		initSchoolsMap();
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
	
	private static void initSchoolsMap() {
		Ini.Section schools = iniFile.get(SCHOOLS_SECTION);
		for(String key : schools.keySet()) {
			sSchoolsMap.put(Integer.parseInt(key), schools.get(key));
		}
	}
	
	public static String getSchoolDatabaseName(int schoolId) {
		if(sSchoolsMap.containsKey(schoolId)) {
			return sSchoolsMap.get(schoolId);
		}
		
		return null;
	}
	
	public static String getValue(String sectionName, String key) {
		Ini.Section section = iniFile.get(sectionName);
		return section.get(key);
	}
}
