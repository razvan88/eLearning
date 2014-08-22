package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.json.JSONObject;

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
			//add the last table model, as it was not added because EOF
			if(model.length() > 0) {
				sTableModels.put(key, model.toString());
			}
			
			reader.close();
		} catch (Exception e) {
			return;
		}
	}
	
	public static JSONObject getTableModel(String tableName) {
		String key = "#" + tableName + "TableModel";
		
		if(sTableModels.containsKey(key)) {
			return JSONObject.fromObject(sTableModels.get(key));
		}
		
		return null;
	}
	
	public static List<JSONObject> getAllTableModels() {
		List<JSONObject> models = new ArrayList<JSONObject>();
		
		for(String key : sTableModels.keySet()) {
			JSONObject model = JSONObject.fromObject(sTableModels.get(key));
			models.add(model);
		}
		
		return models;
	}
	
}
