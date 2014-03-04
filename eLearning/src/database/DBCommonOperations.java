package database;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

import utils.ConfigurationSettings;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


/**
 * Singleton class used for basic database operations
 * 
 * @author Razvan Nedelcu
 */
public class DBCommonOperations {
	private static DBConnection sConnection;
	private static JSONArray sCachedSchoolList;
	private static HashMap<Integer, String> sHighschoolGroups;
	
	static {
		sConnection = new DBConnection(DBCredentials.DEFAULT_DATABASE);
		sConnection.openConnection();
		sCachedSchoolList = getJsonSchools();
		sHighschoolGroups = new HashMap<Integer, String>();
		populateHighschoolGroups();
	}
	
	private DBCommonOperations() {}
	
	private static void populateHighschoolGroups() {
		try {
			Connection connection = sConnection.getConnection();
			Statement statement = connection.createStatement();
			
			ResultSet result = statement.executeQuery("SELECT * FROM group");
			
			while(result.next()) {
				int id = result.getInt("id");
				String name = result.getString("name");
				
				sHighschoolGroups.put(id, name);
			}
		} catch(Exception e) {
			
		}
	}
	
	/**
	 * @return json array with school objects, containing id, name, city and type
	 */
	public static JSONArray getJsonSchools() {
		if(sCachedSchoolList != null) {
			return sCachedSchoolList;
		}
		
		JSONArray schools = new JSONArray();
		
		try{
			Connection connection = sConnection.getConnection();
			Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			PreparedStatement prepStatement = connection.prepareStatement("SELECT `name` FROM " + DBCredentials.CITIES_TABLE + " WHERE `id`=?");
			
			ResultSet resultSet = statement.executeQuery("SELECT * FROM " + DBCredentials.SCHOOLS_TABLE);
			
			while(resultSet.next()) {
				JSONObject school = new JSONObject();
				
				school.put("id", resultSet.getInt("id"));
				school.put("name", resultSet.getString("name"));
				school.put("branch", resultSet.getString("branch"));
				
				int cityId = resultSet.getInt("city");
				prepStatement.setInt(1, cityId);
				ResultSet rs = prepStatement.executeQuery();
				if (rs.next()) {
					school.put("city", rs.getString(1));
				}
				
				schools.add(school);
			}
			
			prepStatement.close();
			statement.close();
			
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return schools;
	}
	
	/**
	 * 
	 * @param schoolId school primary key, from database
	 * @return the school's corresponding database name, from the config file
	 */
	public static String getDatabaseName(int schoolId) {
		return ConfigurationSettings.getValue(
				ConfigurationSettings.SCHOOLS_SECTION, String.format("%i", schoolId));
	}
	
	/**
	 * @param schoolId
	 * @return a jsonObject containing school's id, name, branch and city
	 */
	public static JSONObject getSchoolInfo(int schoolId) {
		for(int i = 0; i < sCachedSchoolList.size(); i++) {
			if( ((JSONObject)(sCachedSchoolList.get(i))).getInt("id") == schoolId){
				return (JSONObject)(sCachedSchoolList.get(i));
			}
		}
		
		return null;
	}
	
	public static String getGroupName(int groupId) {
		if(sHighschoolGroups.containsKey(groupId)) {
			return sHighschoolGroups.get(groupId);
		}
		
		return null;
	}
}