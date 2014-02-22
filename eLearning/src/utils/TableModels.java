package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class TableModels {
	private static HashMap<String, String> sTableModels;
	
	static {
		sTableModels = new HashMap<String, String>();
		parseModelFile();
	}
	
	private static void parseModelFile() {
		File file = new File("tableModels");
		if(!file.exists()) {
			return;
		}
		
		try {
			String line = null, key = null;
			StringBuffer model = new StringBuffer();
			BufferedReader reader = new BufferedReader(new FileReader(file));
		
			while((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.startsWith("#")) {
					if(model.length() > 0) {
						sTableModels.put(key, model.toString());
					}
					
					key = line;
					model.delete(0, model.length()); //clear the buffer
					continue;
				}
				
				model.append(line);
			}
		
			reader.close();
		} catch (Exception e) {
			return;
		}
	}
	
	public static JSONObject getTableModel(String tableName) {
		String key = "#" + tableName + "TableModel";
		
		if(sTableModels.containsKey(key)) {
			return (JSONObject)JSONSerializer.toJSON(sTableModels.get(key));
		}
		
		return null;
	}
	
}
