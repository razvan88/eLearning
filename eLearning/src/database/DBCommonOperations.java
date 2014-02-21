package database;


import java.sql.ResultSet;
import java.sql.Statement;

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
	
	static {
		sConnection = new DBConnection(DBCredentials.DEFAULT_DATABASE);
		sConnection.openConnection();
	}
	
	private DBCommonOperations() {}
	
	/**
	 * @return json array with school objects, containing id, name, city and type
	 */
	public static JSONArray getJsonSchools() {
		JSONArray schools = new JSONArray();
		
		try{
			Statement statement = sConnection.getConnection().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = statement.executeQuery("SELECT * FROM " + DBCredentials.SCHOOLS_TABLE);
			
			while(rs.next()) {
				JSONObject school = new JSONObject();
				
				school.put("id", rs.getInt("id"));
				school.put("name", rs.getString("name"));
				school.put("city", rs.getString("city"));
				school.put("type", rs.getInt("type"));
				
				schools.add(school);
			}
			
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
}	

/*
String query = "SELECT a.xxx, b.yyy FROM zzz a, www b WHERE b.qqq = ? AND a.rrr = b.rrr";
PreparedStatement statement = sConnection.getConnection().prepareStatement(query);
for(int prod : prods) {
	statement.setInt(1, prod);
	ResultSet resultSet = statement.executeQuery();
	if (resultSet.next()) {
		String category = resultSet.getString("xxx");
	}
}
*/
