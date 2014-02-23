package database;

import static database.DBCredentials.DRIVER;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class DBUtils {
	
	public static DBConnection createDatabase(String databaseName) {
		Connection connection = null;
		String link = DBCredentials.getDefaultLink();
		String createQuery = "CREATE DATABASE IF NOT EXISTS `" + databaseName + "` " +
					"DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci";
		
		try {
			Class.forName(DRIVER).newInstance();
			connection = DriverManager.getConnection(link);
			Statement statement = connection.createStatement();
			
			statement.executeUpdate(createQuery);
			statement.close();
		} catch (Exception e) {
			return null;
		}
		
		DBConnection dbConnection = new DBConnection(databaseName);
		dbConnection.openConnection();
		return dbConnection;
	}
	
	private static String getTableCreationQuery(JSONObject structure) {
		List<Integer> pk = new ArrayList<Integer>();
		List<Integer> unq = new ArrayList<Integer>();
		StringBuffer query = new StringBuffer("CREATE TABLE IF NOT EXISTS `");
		String tableName = structure.getString("name");
		query.append(tableName + "` (");
		
		JSONArray columns = structure.getJSONArray("columns");
		for(int i = 0; i < columns.size(); i++) {
			JSONObject column =  columns.getJSONObject(i);
			
			String colName = column.getString("name");
			String colType = column.getString("type");
			int colLength = column.getInt("length");
			int colNull = column.getInt("isNull");
			int colPK = column.getInt("isPrimaryKey");
			int colAI = column.getInt("isAutoIncrement");
			int colUnq = column.getInt("isUnique");
			
			query.append("`" + colName + "` ");
			query.append(colType);
			
			if(colLength > 0) {
				query.append("(" + colLength + ") ");
			} else {
				query.append(" ");
			}
			
			query.append((colNull == 0 ? "NOT " : "") + "NULL");
			query.append(colAI == 0 ? "" : " AUTO_INCREMENT");
			
			if(i + 1 < columns.size()) {
				query.append(", ");
			}
			
			if(colPK == 1) {
				//save primary keys
				pk.add(i);
			}
			if(colUnq == 1) {
				//save unique keys
				unq.add(i);
			}
		}
		
		if(!pk.isEmpty()) {
			//add primary keys
			query.append(", PRIMARY KEY (");
			for(int key = 0; key < pk.size(); key++) {
				String name = columns.getJSONObject(key).getString("name");
				query.append("`" + name + "`");
				if(key + 1 < pk.size()) {
					query.append(", ");
				}
			}
			query.append(")");
		}
		
		if(!unq.isEmpty()) {
			//add unique keys
			query.append(", UNIQUE KEY (");
			for(int key = 0; key < unq.size(); key++) {
				String name = columns.getJSONObject(key).getString("name");
				query.append("`" + name + "`");
				if(key + 1 < unq.size()) {
					query.append(", ");
				}
			}
			query.append(")");
		}
		
		query.append(") ENGINE=InnoDB");
		
		return query.toString();
	}
	
	public static boolean createTable(DBConnection dbConnection, JSONObject structure) {
		String query = getTableCreationQuery(structure);
		
		try {
			Connection connection = dbConnection.getConnection();
			Statement statement = connection.createStatement();
			statement.executeUpdate(query);
			statement.close();
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}

	public static boolean checkLogin(DBConnection dbConnection, String dbName, String username, String password) {
		Connection connection = dbConnection.getConnection();
		boolean result = false;
		
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT password FROM " + dbName + " WHERE username=`" + username + "`");
			if(resultSet.next()) {
				String correctPass = resultSet.getString(1);
				result = correctPass.equals(password);
			}
			resultSet.close();
		} catch (Exception e) {
			return false;
		}
		
		return result;
	}
	
	/*
	public static void main(String[] args) {
		DBConnection conn = DBUtils.createDatabase("licTeorMinuneaNatiuniiBuc");
		DBUtils.createTable(conn, TableModels.getTableModel("student"));
		DBUtils.createTable(conn, TableModels.getTableModel("teacher"));
		DBUtils.createTable(conn, TableModels.getTableModel("auxiliary"));
	}
	*/
}
