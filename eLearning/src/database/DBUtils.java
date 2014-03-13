package database;

import static database.DBCredentials.DRIVER;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
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

	public static JSONObject checkLogin(DBConnection dbConnection, String username, String password) {
		Connection connection = dbConnection.getConnection();
		JSONObject result = new JSONObject();
		
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT `id`, `password`, `firstName`, `lastName` FROM student WHERE `username`='" + username + "'");
			if(resultSet.next()) {
				result.put("login", resultSet.getString(2).equals(password));
				result.put("userId", resultSet.getInt(1));
				result.put("firstName", resultSet.getString(3));
				result.put("lastName", resultSet.getString(4));
				result.put("table", "student");
			} else {
				resultSet = statement.executeQuery("SELECT `id`, `password`, `firstName`, `lastName` FROM teacher WHERE `username`='" + username + "'");
				if(resultSet.next()) {
					result.put("login", resultSet.getString(2).equals(password));
					result.put("userId", resultSet.getInt(1));
					result.put("firstName", resultSet.getString(3));
					result.put("lastName", resultSet.getString(4));
					result.put("table", "teacher");
				} else {
					resultSet = statement.executeQuery("SELECT `id`, `password`, `firstName`, `lastName` FROM auxiliary WHERE `username`='" + username + "'");
					if(resultSet.next()) {
						result.put("login", resultSet.getString(2).equals(password));
						result.put("userId", resultSet.getInt(1));
						result.put("firstName", resultSet.getString(3));
						result.put("lastName", resultSet.getString(4));
						result.put("table", "auxiliary");
					} else {
						resultSet = statement.executeQuery("SELECT `id`, `password` FROM admin WHERE `username`='" + username + "'");
						if(resultSet.next()) {
							result.put("login", resultSet.getString(2).equals(password));
							result.put("userId", resultSet.getInt(1));
							result.put("table", "admin");
						}
					}
				}
			}
			resultSet.close();
		} catch (Exception e) {
			return result;
		}
		
		return result;
	}

	public static boolean checkPassword(DBConnection dbConnection, String table, int userId, String pass) {
		Connection connection = dbConnection.getConnection();
		
		String query = "SELECT `password` FROM " + table + " WHERE `id`=" + userId;
		
		try {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			
			if(result.next()) {
				return pass.equals(result.getString(1));
			}
			
		} catch (Exception e) {
			return false;
		}
		
		return false;
	}
	
	public static int updateColumn(DBConnection dbConnection, String table, int userId, String column, String value) {
		Connection connection = dbConnection.getConnection();
		
		String query = "UPDATE " + table + " SET `" + column + "`='" + value + "' WHERE `id`=" + userId;
		
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate(query);
		} catch (Exception e) {
			return 0;
		}
		
		return 1;
	}
	
	public static JSONObject getInformation(DBConnection dbConnection, String table, int userId) {
		Connection connection = dbConnection.getConnection();
		JSONObject info = new JSONObject();
		
		String query = "SELECT `firstName`, `lastName`, `photo`, `birthdate`, `description`, `group`, `email` FROM " + table + " WHERE `id`=" + userId;
		
		try {
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(query);
			
			if(rs.next()) {
				info.put("firstname", rs.getString("firstname"));
				info.put("lastname", rs.getString("lastname"));
				info.put("photo", rs.getString("photo"));
				info.put("description", rs.getString("description"));
				info.put("email", rs.getString("email"));
				info.put("birthdate", rs.getString("birthdate"));
				info.put("group", DBCommonOperations.getGroupName(rs.getInt("group")));
			}
		} catch (Exception e) {
			return null;
		}
		
		return info;
	}
	
	public static JSONObject getShortInformation(DBConnection dbConnection, String table, int userId) {
		Connection connection = dbConnection.getConnection();
		JSONObject info = new JSONObject();
		
		String query = "SELECT `description`, `email` FROM " + table + " WHERE `id`=" + userId;
		
		try {
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(query);
			
			if(rs.next()) {
				info.put("description", rs.getString("description"));
				info.put("email", rs.getString("email"));
			}
		} catch (Exception e) {
			return null;
		}
		
		return info;
	}
	
	public static JSONArray getSchoolNews(DBConnection dbConnection) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT * FROM " + DBCredentials.SCHOOL_NEWS_TABLE;
		JSONArray news = new JSONArray();
		
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			
			while(resultSet.next()) {
				JSONObject newsArticle = new JSONObject();
				
				newsArticle.put("id", resultSet.getInt("id"));
				newsArticle.put("date", resultSet.getString("date"));
				newsArticle.put("title", resultSet.getString("title"));
				newsArticle.put("content", resultSet.getString("content"));
				
				news.add(newsArticle);
			}
			
			statement.close();
		} catch (Exception e) { }
		
		return news;
	}
	
	public static JSONObject getSchoolNewsArticle(DBConnection dbConnection, int newsArticleId) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT * FROM " + DBCredentials.SCHOOL_NEWS_TABLE + " WHERE `id`=" + newsArticleId;
		JSONObject article = new JSONObject();
		
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			
			if(resultSet.next()) {
				article.put("id", resultSet.getInt("id"));
				article.put("date", resultSet.getString("date"));
				article.put("title", resultSet.getString("title"));
				article.put("content", resultSet.getString("content"));
			}
			
			statement.close();
		} catch (Exception e) { }
		
		return article;
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
