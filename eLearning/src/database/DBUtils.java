package database;

import static database.DBCredentials.DRIVER;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
					"DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci";
		
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
	
	public static String getTimetable(DBConnection dbConnection, int classId) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT `timetable` FROM " + DBCredentials.SCHOOL_TIMETABLE_TABLE + " WHERE `classId`=" + classId;
		String timetable = "";
		
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			
			if(resultSet.next()) {
				timetable = resultSet.getString(1);
			}
			
			statement.close();
		} catch (Exception e) {
			timetable = "{}";
		}
		
		return timetable;
	}
	
	public static int getClassIdForUser(DBConnection dbConnection, int userId) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT `group` FROM student WHERE `id`=" + userId;
		
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			
			if(resultSet.next()) {
				return resultSet.getInt(1);
			}
			
			statement.close();
		} catch (Exception e) { }
		
		return -1;
	}
	
	public static JSONArray getCoursesList(DBConnection dbConnection, int userId) {
		Connection connection = dbConnection.getConnection();
		String coursesIdsQuery = "SELECT `courseIds` FROM " + DBCredentials.COURSES_LIST_TABLE + " WHERE `studentId`=" + userId;
		JSONArray courses = new JSONArray();
		
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(coursesIdsQuery);
			
			if(resultSet.next()) {
				String[] coursesIds = resultSet.getString(1).split(",");
				courses = DBCommonOperations.getCoursesInfo(coursesIds);
			}
			
			statement.close();
		} catch (Exception e) { }
		
		return courses;
	}
	
	public static int getTeachClassCourseId(DBConnection dbConnection, int classId, int courseId) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT `id` FROM " + DBCredentials.TEACHER_COURSE_CLASS_TABLE + 
					" WHERE `courseId`=" + courseId + " AND `classId`=" + classId;
		int id = 0;
		
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			
			if(resultSet.next()) {
				id = resultSet.getInt(1);
			}
			
			statement.close();
		} catch (Exception e) { }
		
		return id;
	}
	
	public static JSONObject getDeadlines(DBConnection dbConnection, int teacherCourseClassId) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT `dates`, `tips` FROM " + DBCredentials.DEADLINES_TABLE + 
					" WHERE `teacher_course_class_id`=" + teacherCourseClassId;
		JSONObject result = new JSONObject();
		
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			
			if(resultSet.next()) {
				String[] array = resultSet.getString("dates").split("#");
				JSONArray dates = new JSONArray();
				for(String date : array) {
					dates.add(date);
				}
				result.put("dates", dates);
				
				array = resultSet.getString("tips").split("#");
				JSONArray tips = new JSONArray();
				for(String tip : array) {
					tips.add(tip);
				}
				result.put("tips", tips);
			}
			
			statement.close();
		} catch (Exception e) { }
		
		return result;
	}
	
	public static JSONArray getResources(DBConnection dbConnection, int teacherCourseClassId) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT `content` FROM " + DBCredentials.RESOURCES_TABLE + 
					" WHERE `teacher_course_class_id`=" + teacherCourseClassId;
		JSONArray resources = new JSONArray();
		
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			
			if(resultSet.next()) {
				String rawJson = resultSet.getString("content");
				resources = JSONArray.fromObject(rawJson);
			}
			
			statement.close();
		} catch (Exception e) { }
		
		return resources;
	}
	
	public static JSONObject getHolidayDetails(DBConnection dbConnection) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT * FROM " + DBCredentials.HOLIDAYS_TABLE;
		JSONObject result = new JSONObject();
		
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			
			if(resultSet.next()) {
				String startingDate = resultSet.getString("starting_date");
				result.put("startingDate", startingDate);
				
				int weeksNo = resultSet.getInt("total_weeks");
				result.put("totalWeeks", weeksNo);
				
				String[] holidayWeeks = resultSet.getString("holiday_weeks").split("#");
				JSONArray dates = new JSONArray();
				for(String week : holidayWeeks) {
					dates.add(week);
				}
				result.put("holidayWeeks", dates);
			}
			
			statement.close();
		} catch (Exception e) { }
		
		return result;
	}
	
	public static JSONArray getHomework(DBConnection dbConnection, int tccId, int studentId) {
		Connection connection = dbConnection.getConnection();
		
		String homeworkQuery = "SELECT * FROM " + DBCredentials.HOMEWORK_TABLE + " WHERE `teacher_course_class_id`=" + tccId;
		String homeworkResultQuery = "SELECT * FROM " + DBCredentials.HOMEWORK_RESULTS_TABLE + 
								" WHERE `teacher_course_class_id`=" + tccId + 
								" AND `student_id`=" + studentId + " AND `homework_id`=?";
		JSONArray result = new JSONArray();
		
		try {
			Statement homeworkStatement = connection.createStatement();
			ResultSet homeworkResultSet = homeworkStatement.executeQuery(homeworkQuery);
			
			while(homeworkResultSet.next()) {
				JSONObject homework = new JSONObject();
				
				homework.put("name", homeworkResultSet.getString("name"));
				homework.put("text", homeworkResultSet.getString("content"));
				homework.put("deadline", homeworkResultSet.getString("deadline"));
				homework.put("resources", homeworkResultSet.getString("resources"));
				homework.put("maxGrade", homeworkResultSet.getString("maxGrade"));
				
				PreparedStatement resultsStatement = connection.prepareStatement(homeworkResultQuery);
				resultsStatement.setInt(1, homeworkResultSet.getInt("id"));
				ResultSet resultsResultSet = resultsStatement.executeQuery();
				if(resultsResultSet.next()) {
					homework.put("uploaded", resultsResultSet.getInt("uploaded"));
					homework.put("graded", resultsResultSet.getInt("graded"));
					homework.put("grade", resultsResultSet.getInt("grade"));
					homework.put("feedback", resultsResultSet.getString("feedback"));
					homework.put("archive", resultsResultSet.getString("archive"));
					homework.put("uploadTime", resultsResultSet.getString("upload_time"));
				}
				resultsStatement.close();
				
				result.add(homework);
			}
			
			homeworkStatement.close();
		} catch (Exception e) { }
		
		return result;
	}
	
	public static JSONArray getCourseClassbook(DBConnection dbConnection, int tccId, int studentId) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT * FROM " + DBCredentials.COURSE_CLASSBOOK_TABLE + 
					" WHERE `teacher_course_class_id`=" + tccId + " AND `student_id`=" + studentId;
		JSONArray result = new JSONArray();
		
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			
			while(resultSet.next()) {
				JSONObject grade = new JSONObject();
				
				grade.put("date", resultSet.getString("date"));
				grade.put("activity", resultSet.getString("activity"));
				grade.put("max", resultSet.getFloat("max"));
				grade.put("grade", resultSet.getFloat("grade"));
				grade.put("notes", resultSet.getString("notes"));
				
				result.add(grade);
			}
			
			statement.close();
		} catch (Exception e) { }
		
		return result;
	}
	
	public static JSONObject getFeedbackRequest(DBConnection dbConnection, int tccId) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT `available`, `aspects` FROM " + DBCredentials.FEEDBACK_REQUEST_TABLE + 
					" WHERE `teacher_course_class_id`=" + tccId;
		JSONObject feedback = new JSONObject();
		
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			
			if(resultSet.next()) {
				feedback.put("isAvailable", resultSet.getInt("available"));
				feedback.put("aspects", resultSet.getString("aspects"));
			}
			
			statement.close();
		} catch (Exception e) { }
		
		return feedback;
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
