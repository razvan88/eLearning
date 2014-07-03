package database;

import static database.DBCredentials.DRIVER;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class DBUtils {
	private static HashMap<Integer, JSONArray> sTeachers;

	static {
		sTeachers = new HashMap<Integer, JSONArray>();
	}

	public static DBConnection createDatabase(String databaseName) {
		Connection connection = null;
		String link = DBCredentials.getDefaultLink();
		String createQuery = "CREATE DATABASE IF NOT EXISTS `" + databaseName
				+ "` " + "DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci";

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
		for (int i = 0; i < columns.size(); i++) {
			JSONObject column = columns.getJSONObject(i);

			String colName = column.getString("name");
			String colType = column.getString("type");
			int colLength = column.getInt("length");
			int colNull = column.getInt("isNull");
			int colPK = column.getInt("isPrimaryKey");
			int colAI = column.getInt("isAutoIncrement");
			int colUnq = column.getInt("isUnique");

			query.append("`" + colName + "` ");
			query.append(colType);

			if (colLength > 0) {
				query.append("(" + colLength + ") ");
			} else {
				query.append(" ");
			}

			query.append((colNull == 0 ? "NOT " : "") + "NULL");
			query.append(colAI == 0 ? "" : " AUTO_INCREMENT");

			if (i + 1 < columns.size()) {
				query.append(", ");
			}

			if (colPK == 1) {
				// save primary keys
				pk.add(i);
			}
			if (colUnq == 1) {
				// save unique keys
				unq.add(i);
			}
		}

		if (!pk.isEmpty()) {
			// add primary keys
			query.append(", PRIMARY KEY (");
			for (int key = 0; key < pk.size(); key++) {
				String name = columns.getJSONObject(key).getString("name");
				query.append("`" + name + "`");
				if (key + 1 < pk.size()) {
					query.append(", ");
				}
			}
			query.append(")");
		}

		if (!unq.isEmpty()) {
			// add unique keys
			query.append(", UNIQUE KEY (");
			for (int key = 0; key < unq.size(); key++) {
				String name = columns.getJSONObject(key).getString("name");
				query.append("`" + name + "`");
				if (key + 1 < unq.size()) {
					query.append(", ");
				}
			}
			query.append(")");
		}

		query.append(") ENGINE=InnoDB");

		return query.toString();
	}

	public static boolean createTable(DBConnection dbConnection,
			JSONObject structure) {
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

	public static JSONObject checkLogin(DBConnection dbConnection,
			String username, String password) {
		Connection connection = dbConnection.getConnection();
		JSONObject result = new JSONObject();

		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement
					.executeQuery("SELECT `id`, `password`, `firstName`, `lastName` FROM "
							+ DBCredentials.STUDENT_TABLE
							+ " WHERE `username`='" + username + "'");
			if (resultSet.next()) {
				result.put("login", resultSet.getString(2).equals(password));
				result.put("userId", resultSet.getInt(1));
				result.put("firstName", resultSet.getString(3));
				result.put("lastName", resultSet.getString(4));
				result.put("table", "student");
			} else {
				resultSet = statement
						.executeQuery("SELECT `id`, `password`, `firstName`, `lastName` FROM "
								+ DBCredentials.TEACHER_TABLE
								+ " WHERE `username`='" + username + "'");
				if (resultSet.next()) {
					result.put("login", resultSet.getString(2).equals(password));
					result.put("userId", resultSet.getInt(1));
					result.put("firstName", resultSet.getString(3));
					result.put("lastName", resultSet.getString(4));
					result.put("table", "teacher");
				} else {
					resultSet = statement
							.executeQuery("SELECT `id`, `password`, `firstName`, `lastName` FROM "
									+ DBCredentials.AUXILIARY_TABLE
									+ " WHERE `username`='" + username + "'");
					if (resultSet.next()) {
						result.put("login",
								resultSet.getString(2).equals(password));
						result.put("userId", resultSet.getInt(1));
						result.put("firstName", resultSet.getString(3));
						result.put("lastName", resultSet.getString(4));
						result.put("table", "auxiliary");
					} else {
						resultSet = statement
								.executeQuery("SELECT `id`, `password` FROM "
										+ DBCredentials.ADMIN_TABLE
										+ " WHERE `username`='" + username
										+ "'");
						if (resultSet.next()) {
							result.put("login",
									resultSet.getString(2).equals(password));
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

	public static boolean checkPassword(DBConnection dbConnection,
			String table, int userId, String pass) {
		Connection connection = dbConnection.getConnection();

		String query = "SELECT `password` FROM " + table + " WHERE `id`="
				+ userId;

		try {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);

			if (result.next()) {
				return pass.equals(result.getString(1));
			}

		} catch (Exception e) {
			return false;
		}

		return false;
	}

	public static int updateColumn(DBConnection dbConnection, String table,
			int userId, String column, String value) {
		Connection connection = dbConnection.getConnection();

		String query = "UPDATE " + table + " SET `" + column + "`='" + value
				+ "' WHERE `id`=" + userId;

		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate(query);
		} catch (Exception e) {
			return 0;
		}

		return 1;
	}

	public static JSONObject getInformation(DBConnection dbConnection,
			String table, int userId) {
		Connection connection = dbConnection.getConnection();
		JSONObject info = new JSONObject();

		String query = "SELECT `firstName`, `lastName`, `photo`, `birthdate`, `description`, `email`";

		if (table.equals("student")) {
			query += ",`group`";
		} else if (table.equals("teacher")) {
			query += ",`courses`";
		}

		query += " FROM " + table + " WHERE `id`=" + userId;
		try {
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(query);

			if (rs.next()) {
				info.put("firstName", rs.getString("firstName"));
				info.put("lastName", rs.getString("lastName"));
				info.put("photo", rs.getString("photo"));
				info.put("description", rs.getString("description"));
				info.put("email", rs.getString("email"));
				info.put("birthdate", rs.getString("birthdate"));

				if (table.equals("student")) {
					info.put("group",
							DBCommonOperations.getGroupName(rs.getInt("group")));
				} else if (table.equals("teacher")) {
					String[] courses = rs.getString("courses").split(",");
					JSONArray coursesList = DBCommonOperations
							.getCoursesInfo(courses);
					info.put("courses", coursesList);
				}

			}
		} catch (Exception e) {
			return null;
		}

		return info;
	}

	public static JSONObject getShortInformation(DBConnection dbConnection,
			String table, int userId) {
		Connection connection = dbConnection.getConnection();
		JSONObject info = new JSONObject();

		String query = "SELECT `description`, `email` FROM " + table
				+ " WHERE `id`=" + userId;

		try {
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(query);

			if (rs.next()) {
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

			while (resultSet.next()) {
				JSONObject newsArticle = new JSONObject();

				newsArticle.put("id", resultSet.getInt("id"));
				newsArticle.put("date", resultSet.getString("date"));
				newsArticle.put("title", resultSet.getString("title"));
				newsArticle.put("content", resultSet.getString("content"));

				news.add(newsArticle);
			}

			statement.close();
		} catch (Exception e) {
		}

		return news;
	}

	public static JSONObject getSchoolNewsArticle(DBConnection dbConnection,
			int newsArticleId) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT * FROM " + DBCredentials.SCHOOL_NEWS_TABLE
				+ " WHERE `id`=" + newsArticleId;
		JSONObject article = new JSONObject();

		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			if (resultSet.next()) {
				article.put("id", resultSet.getInt("id"));
				article.put("date", resultSet.getString("date"));
				article.put("title", resultSet.getString("title"));
				article.put("content", resultSet.getString("content"));
			}

			statement.close();
		} catch (Exception e) {
		}

		return article;
	}

	public static String getTimetable(DBConnection dbConnection, int classId) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT `timetable` FROM "
				+ DBCredentials.SCHOOL_TIMETABLE_TABLE + " WHERE `classId`="
				+ classId;
		String timetable = "";

		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			if (resultSet.next()) {
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
		String query = "SELECT `group` FROM " + DBCredentials.STUDENT_TABLE
				+ " WHERE `id`=" + userId;

		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			if (resultSet.next()) {
				return resultSet.getInt(1);
			}

			statement.close();
		} catch (Exception e) {
		}

		return -1;
	}

	public static JSONArray getCoursesList(DBConnection dbConnection, int userId) {
		Connection connection = dbConnection.getConnection();
		String coursesIdsQuery = "SELECT `courseIds` FROM "
				+ DBCredentials.COURSES_LIST_TABLE + " WHERE `studentId`="
				+ userId;
		JSONArray courses = new JSONArray();

		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(coursesIdsQuery);

			if (resultSet.next()) {
				String[] coursesIds = resultSet.getString(1).split(",");
				courses = DBCommonOperations.getCoursesInfo(coursesIds);
			}

			statement.close();
		} catch (Exception e) {
		}

		return courses;
	}

	public static int getTeachClassCourseId(DBConnection dbConnection,
			int classId, int courseId) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT `id` FROM "
				+ DBCredentials.TEACHER_COURSE_CLASS_TABLE
				+ " WHERE `courseId`=" + courseId + " AND `classId`=" + classId;
		int id = 0;

		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			if (resultSet.next()) {
				id = resultSet.getInt(1);
			}

			statement.close();
		} catch (Exception e) {
		}

		return id;
	}

	public static JSONArray getResources(DBConnection dbConnection,
			int teacherCourseClassId) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT `content` FROM " + DBCredentials.RESOURCES_TABLE
				+ " WHERE `teacher_course_class_id`=" + teacherCourseClassId;
		JSONArray resources = new JSONArray();

		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			if (resultSet.next()) {
				String rawJson = resultSet.getString("content");
				resources = JSONArray.fromObject(rawJson);
			}

			statement.close();
		} catch (Exception e) {
		}

		return resources;
	}

	public static JSONObject getHolidayDetails(DBConnection dbConnection) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT * FROM " + DBCredentials.HOLIDAYS_TABLE;
		JSONObject result = new JSONObject();

		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			if (resultSet.next()) {
				String startingDate = resultSet.getString("starting_date");
				result.put("startingDate", startingDate);

				int weeksNo = resultSet.getInt("total_weeks");
				result.put("totalWeeks", weeksNo);

				String[] holidayWeeks = resultSet.getString("holiday_weeks")
						.split("#");
				JSONArray dates = new JSONArray();
				for (String week : holidayWeeks) {
					dates.add(week);
				}
				result.put("holidayWeeks", dates);
			}

			statement.close();
		} catch (Exception e) {
		}

		return result;
	}

	public static JSONArray getHomework(DBConnection dbConnection, int tccId,
			int studentId) {
		Connection connection = dbConnection.getConnection();

		String homeworkQuery = "SELECT * FROM " + DBCredentials.HOMEWORK_TABLE
				+ " WHERE `teacher_course_class_id`=" + tccId;
		String homeworkResultQuery = "SELECT * FROM "
				+ DBCredentials.HOMEWORK_RESULTS_TABLE
				+ " WHERE `teacher_course_class_id`=" + tccId
				+ " AND `student_id`=" + studentId + " AND `homework_id`=?";
		JSONArray result = new JSONArray();

		try {
			Statement homeworkStatement = connection.createStatement();
			ResultSet homeworkResultSet = homeworkStatement
					.executeQuery(homeworkQuery);

			while (homeworkResultSet.next()) {
				JSONObject homework = new JSONObject();

				homework.put("id", homeworkResultSet.getInt("id"));
				homework.put("name", homeworkResultSet.getString("name"));
				homework.put("text", homeworkResultSet.getString("content"));
				homework.put("deadline",
						homeworkResultSet.getString("deadline"));
				homework.put("resources",
						homeworkResultSet.getString("resources"));
				homework.put("maxGrade",
						homeworkResultSet.getString("maxGrade"));

				PreparedStatement resultsStatement = connection
						.prepareStatement(homeworkResultQuery);
				resultsStatement.setInt(1, homeworkResultSet.getInt("id"));
				ResultSet resultsResultSet = resultsStatement.executeQuery();

				if (resultsResultSet.next()) {
					homework.put("uploaded",
							resultsResultSet.getInt("uploaded"));
					homework.put("graded", resultsResultSet.getInt("graded"));
					homework.put("grade", resultsResultSet.getInt("grade"));
					homework.put("feedback",
							resultsResultSet.getString("feedback"));
					homework.put("archive",
							resultsResultSet.getString("archive"));
					homework.put("uploadTime",
							resultsResultSet.getString("upload_time"));
				} else {
					// not yet uploaded
					homework.put("uploaded", 0);
				}

				resultsStatement.close();

				result.add(homework);
			}

			homeworkStatement.close();
		} catch (Exception e) {
		}

		return result;
	}

	public static JSONArray getCourseClassbook(DBConnection dbConnection,
			int tccId, int studentId) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT * FROM " + DBCredentials.COURSE_CLASSBOOK_TABLE
				+ " WHERE `teacher_course_class_id`=" + tccId
				+ " AND `student_id`=" + studentId;
		JSONArray result = new JSONArray();

		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			while (resultSet.next()) {
				JSONObject grade = new JSONObject();

				grade.put("date", resultSet.getString("date"));
				grade.put("activity", resultSet.getString("activity"));
				grade.put("max", resultSet.getFloat("max"));
				grade.put("grade", resultSet.getFloat("grade"));
				grade.put("notes", resultSet.getString("notes"));

				result.add(grade);
			}

			statement.close();
		} catch (Exception e) {
		}

		return result;
	}

	public static JSONObject getFeedbackRequest(DBConnection dbConnection,
			int tccId) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT `available`, `aspects` FROM "
				+ DBCredentials.FEEDBACK_REQUEST_TABLE
				+ " WHERE `teacher_course_class_id`=" + tccId;
		JSONObject feedback = new JSONObject();

		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			if (resultSet.next()) {
				feedback.put("isAvailable", resultSet.getInt("available"));
				feedback.put("aspects", resultSet.getString("aspects"));
			}

			statement.close();
		} catch (Exception e) {
		}

		return feedback;
	}

	public static JSONArray getForumSummary(DBConnection dbConnection,
			int courseId, int isAnnouncement) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT * FROM " + DBCredentials.FORUM_SUMMARY_TABLE
				+ " WHERE `course_id`=" + courseId + " AND `is_announcement`="
				+ isAnnouncement;
		JSONArray forum = new JSONArray();

		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			while (resultSet.next()) {
				JSONObject subject = new JSONObject();

				subject.put("id", resultSet.getInt("id"));
				subject.put("subject", resultSet.getString("subject"));
				subject.put("totalPosts", resultSet.getInt("total_posts"));
				subject.put("lastPostDate",
						resultSet.getString("last_post_date"));

				int userId = resultSet.getInt("initiator_id");
				String tableName = resultSet.getString("initiator_table");
				String userQuery = "SELECT `firstName`, `lastName` FROM "
						+ tableName + " WHERE `id`=" + userId;
				Statement userStmt = connection.createStatement();
				ResultSet userResultSet = userStmt.executeQuery(userQuery);
				if (userResultSet.next()) {
					subject.put("initiator",
							userResultSet.getString("firstName") + " "
									+ userResultSet.getString("lastName"));
				}

				userId = resultSet.getInt("last_post_by_id");
				tableName = resultSet.getString("last_post_by_table");
				userQuery = "SELECT `firstName`, `lastName` FROM " + tableName
						+ " WHERE `id`=" + userId;
				userResultSet = userStmt.executeQuery(userQuery);
				if (userResultSet.next()) {
					subject.put("lastPostBy",
							userResultSet.getString("firstName") + " "
									+ userResultSet.getString("lastName"));
				}

				userStmt.close();

				forum.add(subject);
			}

			statement.close();
		} catch (Exception e) {
		}

		return forum;
	}

	public static JSONArray getForumSubject(DBConnection dbConnection,
			int subjectId) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT * FROM " + DBCredentials.FORUM_SUBJECT_TABLE
				+ " WHERE `subject_id`=" + subjectId;
		JSONArray forum = new JSONArray();

		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			while (resultSet.next()) {
				JSONObject subject = new JSONObject();

				subject.put("id", resultSet.getInt("id"));
				subject.put("parentId", resultSet.getInt("parent_post_id"));
				subject.put("timestamp", resultSet.getString("date"));
				subject.put("content", resultSet.getString("content"));

				int userId = resultSet.getInt("sender_id");
				String tableName = resultSet.getString("sender_table");
				String userQuery = "SELECT `firstName`, `lastName`, `photo` FROM "
						+ tableName + " WHERE `id`=" + userId;
				Statement userStmt = connection.createStatement();
				ResultSet userResultSet = userStmt.executeQuery(userQuery);
				if (userResultSet.next()) {
					subject.put("sender", userResultSet.getString("firstName")
							+ " " + userResultSet.getString("lastName"));
					subject.put("photo", userResultSet.getString("photo"));
				}

				userStmt.close();
				forum.add(subject);
			}

			statement.close();
		} catch (Exception e) {
		}

		return forum;
	}

	public static JSONArray getMessages(DBConnection dbConnection, int userId,
			String userTable) {
		JSONArray messages = new JSONArray();
		Connection connection = dbConnection.getConnection();
		String query = "SELECT * FROM " + DBCredentials.MESSAGES_TABLE
				+ " WHERE (`initiator_id`=" + userId
				+ " AND `initiator_table`='" + userTable + "')"
				+ " OR (`responder_id`=" + userId + " AND `responder_table`='"
				+ userTable + "')";

		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			while (resultSet.next()) {
				JSONObject message = new JSONObject();

				message.put("id", resultSet.getInt("id"));

				String initiatorName = null, initiatorPhoto = null;
				int initiatorId = resultSet.getInt("initiator_id");
				String tableName = resultSet.getString("initiator_table");
				String userQuery = "SELECT `firstName`, `lastName`, `photo` FROM "
						+ tableName + " WHERE `id`=" + initiatorId;
				Statement userStmt = connection.createStatement();
				ResultSet userResultSet = userStmt.executeQuery(userQuery);
				if (userResultSet.next()) {
					initiatorName = userResultSet.getString("firstName") + " "
							+ userResultSet.getString("lastName");
					initiatorPhoto = userResultSet.getString("photo");
				}
				userStmt.close();

				int selfIndex = 1;
				if (initiatorId == userId && tableName.equals(userTable)) {
					selfIndex = 0;
				}

				message.put("initiator_id", initiatorId);
				message.put("initiator_table", tableName);

				String responderName = null, responderPhoto = null;
				int responderId = resultSet.getInt("responder_id");
				tableName = resultSet.getString("responder_table");
				userQuery = "SELECT `firstName`, `lastName`, `photo` FROM "
						+ tableName + " WHERE `id`=" + responderId;
				userStmt = connection.createStatement();
				userResultSet = userStmt.executeQuery(userQuery);
				if (userResultSet.next()) {
					responderName = userResultSet.getString("firstName") + " "
							+ userResultSet.getString("lastName");
					responderPhoto = userResultSet.getString("photo");
				}
				userStmt.close();

				message.put("with", selfIndex == 0 ? responderName
						: initiatorName);
				message.put("photo", selfIndex == 0 ? responderPhoto
						: initiatorPhoto);
				message.put("personalPhoto", selfIndex == 0 ? initiatorPhoto
						: responderPhoto);

				JSONArray conversations = JSONArray.fromObject(resultSet
						.getString("messages"));
				for (int i = 0; i < conversations.size(); i++) {
					int senderIndex = ((JSONObject) (conversations.get(i)))
							.getInt("sender_index");

					((JSONObject) (conversations.get(i))).put("sender",
							senderIndex == 0 ? initiatorName : responderName);
				}
				message.put("messages", conversations);

				messages.add(message);
			}

			statement.close();
		} catch (Exception e) {
		}

		return messages;
	}

	public static JSONArray getSchoolTeam(DBConnection dbConnection,
			int schoolId) {
		if (sTeachers.containsKey(schoolId)) {
			return sTeachers.get(schoolId);
		}

		JSONArray team = new JSONArray();
		Connection connection = dbConnection.getConnection();
		String teacherQuery = "SELECT `id`, `firstName`, `lastName`, `photo`, `description`, `roles`, `courses` FROM "
				+ DBCredentials.TEACHER_TABLE;
		String auxiliaryQuery = "SELECT `id`, `firstName`, `lastName`, `photo`, `description`, `function`, `timetable` FROM "
				+ DBCredentials.AUXILIARY_TABLE;

		// teachers
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(teacherQuery);

			while (resultSet.next()) {
				JSONObject member = new JSONObject();

				member.put("id", resultSet.getInt("id"));
				member.put("name", resultSet.getString("firstName") + " "
						+ resultSet.getString("lastName"));
				member.put("photo", resultSet.getString("photo"));
				member.put("description", resultSet.getString("description"));
				member.put("table", DBCredentials.TEACHER_TABLE);

				String[] roles = resultSet.getString("roles").split(",");
				List<String> roleNames = DBCommonOperations
						.getRolesNames(roles);

				StringBuffer entireRole = new StringBuffer();

				for (String role : roleNames) {
					entireRole.append(role);

					if (role.equalsIgnoreCase("profesor")) {
						entireRole.append(" de ");

						String[] courses = resultSet.getString("courses")
								.split(",");
						JSONArray jsonCourses = DBCommonOperations
								.getCoursesInfo(courses);

						for (int i = 0; i < jsonCourses.size(); i++) {
							entireRole
									.append(((JSONObject) (jsonCourses.get(i)))
											.getString("name"));
							if (i + 1 < jsonCourses.size()) {
								entireRole.append(", ");
							}
						}
					}

					entireRole.append(", ");
				}

				entireRole.deleteCharAt(entireRole.length() - 2);
				member.put("role", entireRole.toString());

				team.add(member);
			}

			// auxiliary
			resultSet.close();
			resultSet = statement.executeQuery(auxiliaryQuery);

			while (resultSet.next()) {
				JSONObject member = new JSONObject();

				member.put("id", resultSet.getInt("id"));
				member.put("name", resultSet.getString("firstName") + " "
						+ resultSet.getString("lastName"));
				member.put("photo", resultSet.getString("photo"));
				member.put("description", resultSet.getString("description"));
				member.put("timetable", resultSet.getString("timetable"));
				member.put("table", DBCredentials.AUXILIARY_TABLE);

				String[] functions = resultSet.getString("function").split(",");
				List<String> functionNames = DBCommonOperations
						.getAuxiliaryFunctions(functions);

				StringBuffer entireFunctions = new StringBuffer();
				for (String function : functionNames) {
					entireFunctions.append(function);
					entireFunctions.append(", ");
				}
				entireFunctions.deleteCharAt(entireFunctions.length() - 2);
				member.put("role", entireFunctions.toString());

				team.add(member);
			}

			statement.close();
		} catch (Exception e) {
		}

		sTeachers.put(schoolId, team);
		return team;
	}

	public static JSONObject getTeacher(DBConnection dbConnection,
			int schoolId, String table, int userId) {
		if (!sTeachers.containsKey(schoolId)) {
			getSchoolTeam(dbConnection, schoolId);
		}

		JSONArray team = sTeachers.get(schoolId);
		for (int i = 0; i < team.size(); i++) {
			JSONObject person = team.getJSONObject(i);
			if (person.getInt("id") == userId
					&& person.getString("table").equals(table)) {
				return person;
			}
		}

		return new JSONObject();
	}

	public static JSONArray getAvailableClasses(DBConnection dbConnection) {
		JSONArray classes = new JSONArray();
		Connection connection = dbConnection.getConnection();
		String query = "SELECT DISTINCT(`group`) FROM "
				+ DBCredentials.STUDENT_TABLE;

		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			while (resultSet.next()) {
				JSONObject group = new JSONObject();

				int id = resultSet.getInt("group");
				group.put("id", id);
				group.put("name", DBCommonOperations.getGroupName(id));

				classes.add(group);
			}

			statement.close();
		} catch (Exception e) {
		}

		return classes;
	}

	public static JSONArray getClassStudents(DBConnection dbConnection,
			int classId) {
		JSONArray students = new JSONArray();
		Connection connection = dbConnection.getConnection();
		String query = "SELECT `id` FROM " + DBCredentials.STUDENT_TABLE
				+ " WHERE `group`=" + classId;

		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			while (resultSet.next()) {
				JSONObject student = getInformation(dbConnection,
						DBCredentials.STUDENT_TABLE, resultSet.getInt("id"));
				student.put("id", resultSet.getInt("id"));
				students.add(student);
			}

			statement.close();
		} catch (Exception e) {
		}

		JSONArray classStudents = new JSONArray();
		for (int i = 0; i < students.size(); i++) {
			JSONObject student = students.getJSONObject(i);
			JSONObject newStudent = new JSONObject();
			newStudent.put("label", student.getString("firstName") + " "
					+ student.getString("lastName"));
			newStudent.put("description", student.getString("description"));
			newStudent.put("photo", student.getString("photo"));
			newStudent.put("id", student.getInt("id"));

			classStudents.add(newStudent);
		}

		return classStudents;
	}

	public static JSONArray getAllStudents(DBConnection dbConnection) {
		JSONArray students = new JSONArray();
		Connection connection = dbConnection.getConnection();
		String query = "SELECT `id` FROM " + DBCredentials.STUDENT_TABLE;

		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			while (resultSet.next()) {
				JSONObject student = getInformation(dbConnection,
						DBCredentials.STUDENT_TABLE, resultSet.getInt("id"));

				JSONObject minInfoStudent = new JSONObject();
				minInfoStudent.put("id", resultSet.getInt("id"));
				minInfoStudent.put("label", student.getString("firstName")
						+ " " + student.getString("lastName"));
				minInfoStudent.put("photo", student.getString("photo"));
				minInfoStudent.put("description",
						student.getString("description"));

				students.add(minInfoStudent);
			}

			statement.close();
		} catch (Exception e) {
		}

		return students;
	}

	public static int uploadMessage(DBConnection dbConnection, int messageId,
			JSONObject messageContent) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT `messages` FROM " + DBCredentials.MESSAGES_TABLE
				+ " WHERE `id`=" + messageId;
		String newMessages = "";
		int result = 0;

		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			if (resultSet.next()) {
				JSONArray messages = JSONArray.fromObject(resultSet
						.getString(1));
				messages.add(messageContent);
				newMessages = messages.toString();
			}

			statement.close();

			query = "UPDATE " + DBCredentials.MESSAGES_TABLE
					+ " SET `messages`='" + newMessages + "' WHERE `id`="
					+ messageId;

			statement = connection.createStatement();
			result = statement.executeUpdate(query);

			statement.close();
		} catch (Exception e) {
		}

		return result;
	}

	public static JSONArray getAllDeadlines(DBConnection dbConnection,
			int userId) {
		String coursesQuery = "SELECT `courseIds` FROM "
				+ DBCredentials.COURSES_LIST_TABLE + " WHERE `studentId`="
				+ userId;
		String courses[] = new String[0];
		JSONArray deadlines = new JSONArray();

		try {
			Connection connection = dbConnection.getConnection();
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(coursesQuery);

			if (resultSet.next()) {
				courses = resultSet.getString("courseIds").split(",");
			}

			deadlines = getAllDeadlines(dbConnection, userId, courses);

			statement.close();
		} catch (Exception e) {
		}

		return deadlines;
	}

	public static JSONArray getAllDeadlines(DBConnection dbConnection,
			int userId, String[] courseIds) {
		JSONArray deadlines = new JSONArray();
		Connection connection = dbConnection.getConnection();

		int classId = -1;

		String classQuery = "SELECT `group` FROM "
				+ DBCredentials.STUDENT_TABLE + " WHERE `id`=" + userId;
		String homeworkDeadlineQuery = "SELECT `name`, `deadline` FROM "
				+ DBCredentials.HOMEWORK_TABLE
				+ " WHERE `teacher_course_class_id`=?";
		String simpleDeadlineQuery = "SELECT `date`, `name` FROM "
				+ DBCredentials.DEADLINES_TABLE
				+ " WHERE `teacher_course_class_id`=?";

		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(classQuery);
			if (resultSet.next()) {
				classId = resultSet.getInt("group");
			}

			String tccQuery = "SELECT `id` FROM "
					+ DBCredentials.TEACHER_COURSE_CLASS_TABLE
					+ " WHERE `courseId`=? AND `classId`=" + classId;

			PreparedStatement tccStmt = connection.prepareStatement(tccQuery);
			PreparedStatement homeworkDeadlineStmt = connection
					.prepareStatement(homeworkDeadlineQuery);
			PreparedStatement simpleDeadlineStmt = connection
					.prepareStatement(simpleDeadlineQuery);

			for (int i = 0; i < courseIds.length; i++) {
				int courseId = Integer.parseInt(courseIds[i]);
				int tccId = -1;
				String courseName = (String) ((JSONObject) (DBCommonOperations
						.getCoursesInfo(new String[] { courseId + "" }).get(0)))
						.get("name");

				tccStmt.setInt(1, courseId);

				ResultSet res = tccStmt.executeQuery();
				if (res.next()) {
					tccId = res.getInt("id");
				}

				if (tccId == -1) {
					continue;
				}

				homeworkDeadlineStmt.setInt(1, tccId);
				res = homeworkDeadlineStmt.executeQuery();
				while (res.next()) {
					JSONObject deadline = new JSONObject();

					deadline.put("name", res.getString("name"));
					deadline.put("deadline", res.getString("deadline"));
					deadline.put("course", courseName);

					deadlines.add(deadline);
				}

				simpleDeadlineStmt.setInt(1, tccId);
				res = simpleDeadlineStmt.executeQuery();
				while (res.next()) {
					JSONObject deadline = new JSONObject();

					deadline.put("name", res.getString("name"));
					deadline.put("deadline", res.getString("date"));
					deadline.put("course", courseName);

					deadlines.add(deadline);
				}

				res.close();
			}

			statement.close();
		} catch (Exception e) {
		}

		return deadlines;
	}

	/**
	 * @param dbConnection
	 * @param courseId
	 * @param isAnnouncement
	 * @param subject
	 * @param initiatorId
	 * @param initiatorTable
	 * @param totalPosts
	 * @param lastPostDate
	 * @param lastPostById
	 * @param lastPostByTable
	 * @return the primary key given to inserted row
	 */
	public static int uploadForumTopic(DBConnection dbConnection, int courseId,
			int isAnnouncement, String subject, int initiatorId,
			String initiatorTable, int totalPosts, String lastPostDate,
			int lastPostById, String lastPostByTable) {
		Connection connection = dbConnection.getConnection();
		int primaryKey = -1;
		String query = "INSERT INTO "
				+ DBCredentials.FORUM_SUMMARY_TABLE
				+ " (`course_id`, `is_announcement`, `subject`, `initiator_id`, `initiator_table`, `total_posts`, `last_post_date`, `last_post_by_id`, `last_post_by_table`) VALUES ("
				+ courseId + "," + isAnnouncement + ",'" + subject + "',"
				+ initiatorId + ",'" + initiatorTable + "'," + totalPosts
				+ ",'" + lastPostDate + "'," + lastPostById + ",'"
				+ lastPostByTable + "')";

		try {
			Statement statement = connection.createStatement();
			int rows = statement.executeUpdate(query);

			if (rows == 1) {
				// added row, so get the primary key
				query = "SELECT `id` FROM " + DBCredentials.FORUM_SUMMARY_TABLE
						+ " WHERE `course_id`=" + courseId
						+ " AND `is_announcement`=" + isAnnouncement
						+ " AND `subject`='" + subject + "'"
						+ " AND `last_post_date`='" + lastPostDate + "'"
						+ " AND `initiator_id`=" + initiatorId
						+ " AND `initiator_table`='" + initiatorTable + "'";
				ResultSet result = statement.executeQuery(query);
				if (result.next()) {
					primaryKey = result.getInt("id");
				}
			}

			statement.close();
		} catch (Exception e) {
		}

		return primaryKey;
	}

	public static boolean uploadFirstEntryForumSubject(
			DBConnection dbConnection, int forumTopicId, int senderId,
			String senderTable, String date, String content) {
		int rows = 0;
		Connection connection = dbConnection.getConnection();
		String query = "INSERT INTO "
				+ DBCredentials.FORUM_SUBJECT_TABLE
				+ " (`subject_id`, `parent_post_id`, `sender_id`, `sender_table`, `date`, `content`) VALUES ("
				+ forumTopicId + ",-1," + senderId + ",'" + senderTable + "','"
				+ date + "','" + content + "')";

		try {
			Statement statement = connection.createStatement();
			rows = statement.executeUpdate(query);

			statement.close();
		} catch (Exception e) {
		}

		return rows == 1;
	}

	public static void removeForumTopic(DBConnection dbConnection,
			int forumTopicId) {
		Connection connection = dbConnection.getConnection();
		String query = "DELETE FROM " + DBCredentials.FORUM_SUMMARY_TABLE
				+ " WHERE `id`=" + forumTopicId;

		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate(query);

			statement.close();
		} catch (Exception e) {
		}
	}

	public static boolean uploadForumSubjectEntry(DBConnection dbConnection,
			int subjectId, int parentPostId, int senderId, String senderTable,
			String date, String content) {
		Connection connection = dbConnection.getConnection();
		int rows = 0;
		String query = "INSERT INTO "
				+ DBCredentials.FORUM_SUBJECT_TABLE
				+ " (`subject_id`, `parent_post_id`, `sender_id`, `sender_table`, `date`, `content`) VALUES ("
				+ subjectId + "," + parentPostId + "," + senderId + ",'"
				+ senderTable + "','" + date + "','" + content + "')";

		try {
			Statement statement = connection.createStatement();
			rows = statement.executeUpdate(query);

			if (rows == 1) {
				// if added the entry, increase the total posts number
				query = "SELECT `total_posts` FROM "
						+ DBCredentials.FORUM_SUMMARY_TABLE + " WHERE `id`="
						+ subjectId;
				ResultSet result = statement.executeQuery(query);
				if (result.next()) {
					int totalPosts = result.getInt("total_posts");

					query = "UPDATE " + DBCredentials.FORUM_SUMMARY_TABLE
							+ " SET `total_posts`=" + (++totalPosts)
							+ " WHERE `id`=" + subjectId;
					statement.executeUpdate(query);
				}
			}

			statement.close();
		} catch (Exception e) {
		}

		return rows == 1;
	}

	public static int getConversationIdBetween(DBConnection dbConnection,
			int firstId, String firstTable, int secondId, String secondTable) {
		int pk = -1;
		Connection connection = dbConnection.getConnection();
		String query = "SELECT `id` FROM " + DBCredentials.MESSAGES_TABLE
				+ " WHERE (`initiator_id`=" + firstId
				+ " AND `initiator_table`='" + firstTable
				+ "' AND `responder_id`=" + secondId
				+ " AND `responder_table`='" + secondTable + "') OR "
				+ "(`initiator_id`=" + secondId + " AND `initiator_table`='"
				+ secondTable + "' AND `responder_id`=" + firstId
				+ " AND `responder_table`='" + firstTable + "')";

		try {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);

			if (result.next()) {
				pk = result.getInt("id");
			}

			statement.close();
		} catch (Exception e) {
		}

		return pk;
	}

	public static boolean uploadNewMessage(DBConnection dbConnection,
			int initiatorId, String initiatorTable, int responderId,
			String responderTable, String timestamp, String content) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;

		String messages = "[{\"sender_index\":0, \"timestamp\":\"" + timestamp
				+ "\", \"content\":\"" + content + "\"}]";
		String query = "INSERT INTO "
				+ DBCredentials.MESSAGES_TABLE
				+ " (`initiator_id`, `initiator_table`, `responder_id`, `responder_table`, `messages`) VALUES ("
				+ initiatorId + ",'" + initiatorTable + "'," + responderId
				+ ",'" + responderTable + "','" + messages + "')";

		try {
			Statement statement = connection.createStatement();
			rows = statement.executeUpdate(query);
			statement.close();
		} catch (Exception e) {
		}

		return rows == 1;
	}

	public static int getSenderIndexForMessage(DBConnection dbConnection,
			int messageId, int userId, String userTable) {
		int index = 0;
		Connection connection = dbConnection.getConnection();
		String query = "SELECT `initiator_id`, `initiator_table` FROM "
				+ DBCredentials.MESSAGES_TABLE + " WHERE `id`=" + messageId;

		try {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);

			if (result.next()) {
				index = (result.getInt("initiator_id") == userId && result
						.getString("initiator_table") == userTable) ? 0 : 1;
			}

			statement.close();
		} catch (Exception e) {
		}

		return index;
	}

	public static boolean uploadHomework(DBConnection dbConnection,
			int homeworkId, int tccId, int studentId, String archivePath,
			String date) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;
		String query = "INSERT INTO "
				+ DBCredentials.HOMEWORK_RESULTS_TABLE
				+ " (`homework_id`, `teacher_course_class_id`, `student_id`, `uploaded`, `graded`, `archive`, `upload_time`) VALUES ("
				+ homeworkId + "," + tccId + "," + studentId + ",1,0,'"
				+ archivePath + "','" + date + "')";

		try {
			Statement statement = connection.createStatement();
			rows = statement.executeUpdate(query);
			statement.close();
		} catch (Exception e) {
		}

		return rows == 1;
	}

	public static int getFeedbackId(DBConnection dbConnection, int tccId) {
		Connection connection = dbConnection.getConnection();

		int id = 0;
		String query = "SELECT `id` FROM "
				+ DBCredentials.FEEDBACK_REQUEST_TABLE
				+ " WHERE `teacher_course_class_id`=" + tccId;

		try {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);

			if (result.next()) {
				id = result.getInt("id");
			}

			statement.close();
		} catch (Exception e) {
		}

		return id;
	}

	public static boolean uploadFeedback(DBConnection dbConnection,
			int feedbackId, int studentId, String opinion) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;
		String query = "INSERT INTO " + DBCredentials.FEEDBACK_TABLE
				+ " (`feedback_id`, `student_id`, `opinion`) VALUES ("
				+ feedbackId + "," + studentId + ",'" + opinion + "')";

		try {
			Statement statement = connection.createStatement();
			rows = statement.executeUpdate(query);
			statement.close();
		} catch (Exception e) {
		}

		return rows == 1;
	}

	public static boolean getFeedbackStatus(DBConnection dbConnection,
			int feedbackId, int studentId) {
		Connection connection = dbConnection.getConnection();

		boolean given = false;
		String query = "SELECT `id` FROM " + DBCredentials.FEEDBACK_TABLE
				+ " WHERE `feedback_Id`=" + feedbackId + " AND `student_id`="
				+ studentId;

		try {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);

			given = result.next();

			statement.close();
		} catch (Exception e) {
		}

		return given;
	}

	public static JSONArray getTeacherCoursesAndClasses(
			DBConnection dbConnection, int teacherId) {
		Connection connection = dbConnection.getConnection();
		JSONArray coursesClasses = new JSONArray();
		String query = "SELECT `courses` FROM " + DBCredentials.TEACHER_TABLE
				+ " WHERE `id`=" + teacherId;
		String sql = "SELECT * FROM " + DBCredentials.COURSES_LIST_TABLE;
		List<Integer> classesAlreadyAdded = new ArrayList<Integer>();

		try {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);

			if (result.next()) {
				Statement stmt = connection.createStatement();
				ResultSet res = stmt.executeQuery(sql);

				String[] courses = result.getString("courses").split(",");
				JSONArray coursesInfo = DBCommonOperations
						.getCoursesInfo(courses);
				for (int i = 0; i < coursesInfo.size(); i++) {
					classesAlreadyAdded.clear();

					JSONObject course = (JSONObject) coursesInfo.get(i);
					JSONObject courseClassObject = new JSONObject();

					int id = course.getInt("id");
					String name = course.getString("name");
					courseClassObject.put("id", id);
					courseClassObject.put("course", name);

					JSONArray courseClassesArray = new JSONArray();

					while (res.next()) {
						int studentId = res.getInt("studentId");

						// check student's class
						int classId = DBUtils.getClassIdForUser(dbConnection,
								studentId);
						boolean found = false;
						// check if his class was already added
						for (int addedClass : classesAlreadyAdded) {
							if (addedClass == classId) {
								found = true;
								break;
							}
						}

						// class already added, skip
						if (found)
							continue;

						String[] coursesIds = res.getString("courseIds").split(
								",");
						for (String courseId : coursesIds) {
							if (Integer.parseInt(courseId) == id) {
								// class was not added, so add it
								classesAlreadyAdded.add(classId);

								JSONObject teachedClass = new JSONObject();
								teachedClass.put("id", classId);
								String className = DBCommonOperations
										.getGroupName(classId);
								teachedClass.put("className", className);

								courseClassesArray.add(teachedClass);
							}
						}
					}

					courseClassObject.put("classes", courseClassesArray);
					coursesClasses.add(courseClassObject);
				}

			}

			statement.close();
		} catch (Exception e) {
		}

		return coursesClasses;
	}

	public static int getTeacherFeedbackId(DBConnection dbConnection, int tccId) {
		Connection connection = dbConnection.getConnection();

		int id = -1;
		String query = "SELECT `id` FROM "
				+ DBCredentials.FEEDBACK_REQUEST_TABLE
				+ " WHERE `teacher_course_class_id`=" + tccId;

		try {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);

			if (result.next()) {
				id = result.getInt("id");
			}

			statement.close();
		} catch (Exception e) {
		}

		return id;
	}

	public static boolean updateTeacherFeedback(DBConnection dbConnection,
			int feedbackId, int available, String aspects) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;
		String query = "UPDATE " + DBCredentials.FEEDBACK_REQUEST_TABLE
				+ " SET `available`=" + available + ", `aspects`='" + aspects
				+ "' WHERE `id`=" + feedbackId;

		try {
			Statement statement = connection.createStatement();
			rows = statement.executeUpdate(query);
			statement.close();
		} catch (Exception e) {
		}

		return rows == 1;
	}

	public static boolean uploadTeacherFeedback(DBConnection dbConnection,
			int tccId, int available, String aspects) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;
		String query = "INSERT INTO "
				+ DBCredentials.FEEDBACK_REQUEST_TABLE
				+ " (`teacher_course_class_id`, `available`, `aspects`) VALUES ("
				+ tccId + "," + available + ",'" + aspects + "')";

		try {
			Statement statement = connection.createStatement();
			rows = statement.executeUpdate(query);
			statement.close();
		} catch (Exception e) {
		}

		return rows == 1;
	}

	public static int uploadTeacherHomework(DBConnection dbConnection,
			int tccId, String name, String content, String deadline,
			String resources, float maxGrade) {
		Connection connection = dbConnection.getConnection();

		int rows = 0, id = -1;
		String query = "INSERT INTO "
				+ DBCredentials.HOMEWORK_TABLE
				+ " (`teacher_course_class_id`, `name`, `content`, `deadline`, `resources`, `maxGrade`) VALUES ("
				+ tccId + ",'" + name + "','" + content + "','" + deadline
				+ "','" + resources + "'," + maxGrade + ")";

		try {
			Statement statement = connection.createStatement();
			rows = statement.executeUpdate(query);

			if (rows == 1) {
				query = "SELECT `id` FROM " + DBCredentials.HOMEWORK_TABLE
						+ " WHERE `teacher_course_class_id`=" + tccId
						+ " AND `name`='" + name + "' and `deadline`='"
						+ deadline + "'";
				ResultSet result = statement.executeQuery(query);
				if (result.next()) {
					id = result.getInt("id");
				}
			}

			statement.close();
		} catch (Exception e) {
		}

		return id;
	}

	public static boolean uploadTeacherHomeworkResources(
			DBConnection dbConnection, int homeworkId, String resources) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;
		String resourcesQuery = "SELECT `resources` FROM "
				+ DBCredentials.HOMEWORK_TABLE + " WHERE `id`=" + homeworkId;

		try {
			Statement statement = connection.createStatement();

			// first get the resources and keep the links
			ResultSet result = statement.executeQuery(resourcesQuery);
			JSONArray links = new JSONArray();
			JSONArray res = JSONArray.fromObject(resources);
			if (result.next()) {
				links = JSONArray.fromObject(result.getString("resources"));
			}

			// add all resources together
			for (int i = 0; i < links.size(); i++) {
				res.add(links.getJSONObject(i));
			}

			String query = "UPDATE " + DBCredentials.HOMEWORK_TABLE
					+ " SET `resources`='" + res.toString() + "' WHERE `id`="
					+ homeworkId;
			rows = statement.executeUpdate(query);
			statement.close();
		} catch (Exception e) {
		}

		return rows == 1;
	}

	public static JSONArray getHomeworkListForTeacher(
			DBConnection dbConnection, int tccId) {
		JSONArray homework = new JSONArray();
		Connection connection = dbConnection.getConnection();
		String query = "SELECT `id`, `name` FROM "
				+ DBCredentials.HOMEWORK_TABLE
				+ " WHERE `teacher_course_class_id`=" + tccId;

		try {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);

			while (result.next()) {
				JSONObject homewrk = new JSONObject();

				homewrk.put("id", result.getInt("id"));
				homewrk.put("name", result.getString("name"));

				homework.add(homewrk);
			}

			statement.close();
		} catch (Exception e) {
		}

		return homework;
	}

	public static JSONObject getTeacherHomework(DBConnection dbConnection,
			int homeworkId) {
		JSONObject homework = new JSONObject();
		Connection connection = dbConnection.getConnection();
		String query = "SELECT `name`, `content`, `deadline`, `resources`, `maxGrade` FROM "
				+ DBCredentials.HOMEWORK_TABLE + " WHERE `id`=" + homeworkId;

		try {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);

			if (result.next()) {
				homework.put("name", result.getString("name"));
				homework.put("text", result.getString("content"));
				homework.put("deadline", result.getString("deadline"));
				homework.put("resources", result.getString("resources"));
				homework.put("maxGrade", result.getDouble("maxGrade"));
			}

			statement.close();
		} catch (Exception e) {
		}

		return homework;
	}

	public static int updateTeacherHomework(DBConnection dbConnection,
			int homeworkId, String name, String content, String deadline,
			String resources, float maxGrade) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;
		String query = "UPDATE " + DBCredentials.HOMEWORK_TABLE
				+ " SET `name`='" + name + "', `content`='" + content
				+ "', `deadline`='" + deadline + "', `resources`='" + resources
				+ "', `maxGrade`=" + maxGrade + " WHERE `id`=" + homeworkId;

		try {
			Statement statement = connection.createStatement();
			rows = statement.executeUpdate(query);
			statement.close();
		} catch (Exception e) {
		}

		return rows;
	}

	public static JSONObject getHomeworkNameAndMaxGrade(
			DBConnection dbConnection, int homeworkId) {
		Connection connection = dbConnection.getConnection();

		JSONObject json = new JSONObject();
		String query = "SELECT `name`, `maxGrade`, `deadline` FROM "
				+ DBCredentials.HOMEWORK_TABLE + " WHERE `id`=" + homeworkId;

		try {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);

			if (result.next()) {
				json.put("title", result.getString("name"));
				json.put("maxGrade", result.getDouble("maxGrade"));
				json.put("deadline", result.getString("deadline"));
			}

			statement.close();
		} catch (Exception e) {
		}

		return json;
	}

	public static JSONArray getSubmittedHomework(DBConnection dbConnection,
			int homeworkId, int tccId) {
		Connection connection = dbConnection.getConnection();

		JSONArray json = new JSONArray();
		String query = "SELECT `id`, `student_id`, `uploaded`, `graded`, `grade`, `feedback`, `archive`, `upload_time` FROM "
				+ DBCredentials.HOMEWORK_RESULTS_TABLE
				+ " WHERE `homework_id`="
				+ homeworkId
				+ " AND `teacher_course_class_id`=" + tccId;

		try {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);

			while (result.next()) {
				JSONObject obj = new JSONObject();

				obj.put("id", result.getInt("id"));

				JSONObject archive = JSONObject.fromObject(result
						.getString("archive"));
				obj.put("name", archive.getString("name"));
				obj.put("location", archive.getString("location"));

				String[] uploadTime = result.getString("upload_time")
						.split(" ");
				obj.put("date", uploadTime[0]);
				obj.put("time", uploadTime[1]);

				int graded = result.getInt("graded");
				obj.put("graded", graded);

				obj.put("grade", graded == 1 ? result.getDouble("grade") : "");

				String feedback = result.getString("feedback");
				obj.put("feedback", feedback != null ? feedback : "");

				query = "SELECT `firstName`, `lastName` FROM "
						+ DBCredentials.STUDENT_TABLE + " WHERE `id`="
						+ result.getInt("student_id");
				Statement stmt = connection.createStatement();
				ResultSet res = stmt.executeQuery(query);

				if (res.next()) {
					obj.put("student",
							res.getString("lastName") + " "
									+ res.getString("firstName"));
				}

				json.add(obj);
				stmt.close();
			}

			statement.close();
		} catch (Exception e) {
		}

		return json;
	}

	public static int rateHomework(DBConnection dbConnection, int homeworkId,
			int graded, int grade, String feedback) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;
		String query = "UPDATE " + DBCredentials.HOMEWORK_RESULTS_TABLE
				+ " SET `graded`=" + graded + ", `grade`=" + grade
				+ ", `feedback`='" + feedback + "' WHERE `id`=" + homeworkId;

		try {
			Statement statement = connection.createStatement();
			rows = statement.executeUpdate(query);
			statement.close();
		} catch (Exception e) {
		}

		return rows;
	}

	public static JSONObject getWeekResources(DBConnection dbConnection,
			int tccId, int weekId) {
		Connection connection = dbConnection.getConnection();

		JSONObject resource = new JSONObject();
		String query = "SELECT `content` FROM " + DBCredentials.RESOURCES_TABLE
				+ " WHERE `teacher_course_class_id`=" + tccId;

		try {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);

			if (result.next()) {
				JSONArray array = JSONArray.fromObject(result
						.getString("content"));
				for (int i = 0; i < array.size(); i++) {
					JSONObject obj = array.getJSONObject(i);
					if (obj.getInt("id") == weekId) {
						resource = obj;
						break;
					}
				}
			}

			statement.close();
		} catch (Exception e) {
		}

		return resource;
	}

	public static int uploadCourseWeekInfo(DBConnection dbConnection,
			int tccId, int weekId, String description, String resources) {
		Connection connection = dbConnection.getConnection();

		int rows = 0, id = 0;
		String query = "SELECT `id`, `content` FROM "
				+ DBCredentials.RESOURCES_TABLE
				+ " WHERE `teacher_course_class_id`=" + tccId;
		JSONArray allResources = new JSONArray();

		try {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);

			if (result.next()) {
				id = result.getInt("id");
				allResources = JSONArray
						.fromObject(result.getString("content"));
				for (int i = 0; i < allResources.size(); i++) {
					JSONObject obj = allResources.getJSONObject(i);
					if (obj.getInt("id") == weekId) {
						obj.put("description", description);
						obj.put("resources", resources);
						break;
					}
				}
			}

			query = "UPDATE " + DBCredentials.RESOURCES_TABLE
					+ " SET `content`='" + allResources.toString()
					+ "' WHERE `id`=" + id;
			rows = statement.executeUpdate(query);

			statement.close();
		} catch (Exception e) {
		}

		return rows;
	}

	public static int uploadCourseWeekResources(DBConnection dbConnection,
			int tccId, int weekId, JSONArray resources) {
		Connection connection = dbConnection.getConnection();

		int rows = 0, id = 0;
		String query = "SELECT `id`, `content` FROM "
				+ DBCredentials.RESOURCES_TABLE
				+ " WHERE `teacher_course_class_id`=" + tccId;
		JSONArray allResources = new JSONArray();

		try {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);

			if (result.next()) {
				id = result.getInt("id");
				allResources = JSONArray
						.fromObject(result.getString("content"));
				for (int i = 0; i < allResources.size(); i++) {
					JSONObject obj = allResources.getJSONObject(i);
					if (obj.getInt("id") == weekId) {

						JSONArray oldRes = JSONArray.fromObject(obj
								.getString("resources"));
						for (int j = 0; j < oldRes.size(); j++) {
							resources.add(oldRes.getJSONObject(j));
						}
						// remove old resources
						obj.remove("resources");
						// add new resources
						obj.put("resources", resources);
						break;
					}
				}
			}

			query = "UPDATE " + DBCredentials.RESOURCES_TABLE
					+ " SET `content`='" + allResources.toString()
					+ "' WHERE `id`=" + id;
			rows = statement.executeUpdate(query);

			statement.close();
		} catch (Exception e) {
		}

		return rows;
	}

	public static JSONArray getAuxTimetable(DBConnection dbConnection,
			int userId) {
		Connection connection = dbConnection.getConnection();

		JSONArray timetable = new JSONArray();
		String query = "SELECT `timetable` FROM "
				+ DBCredentials.AUXILIARY_TABLE + " WHERE `id`=" + userId;

		try {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);

			if (result.next()) {
				timetable = JSONArray.fromObject(result.getString("timetable"));
			}

			statement.close();
		} catch (Exception e) {
		}

		return timetable;
	}

	public static int uploadAuxTimetable(DBConnection dbConnection, int userId,
			String timetable) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;
		String query = "UPDATE " + DBCredentials.AUXILIARY_TABLE
				+ " SET `timetable`='" + timetable + "' WHERE `id`=" + userId;

		try {
			Statement statement = connection.createStatement();
			rows = statement.executeUpdate(query);
			statement.close();
		} catch (Exception e) {
		}

		return rows;
	}

	public static String getTeacherList(DBConnection dbConnection) {
		Connection connection = dbConnection.getConnection();

		StringBuffer list = new StringBuffer();
		list.append("[");
		boolean firstEntry = true;
		String query = "SELECT `firstName`, `lastName` FROM "
				+ DBCredentials.TEACHER_TABLE;

		try {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);

			while (result.next()) {
				String teacherName = result.getString("lastName") + " "
						+ result.getString("firstName");

				if (!firstEntry) {
					list.append(",");
				} else {
					firstEntry = false;
				}

				list.append("\"");
				list.append(teacherName);
				list.append("\"");
			}

			statement.close();
		} catch (Exception e) {
		}

		list.append("]");

		return list.toString();
	}

	public static JSONArray getAllCoursesList(DBConnection dbConnection) {
		return DBCommonOperations.getAllCourses();
	}

	public static int uploadClassTimetable(DBConnection dbConnection,
			int classId, String timetable) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;
		String query = "UPDATE " + DBCredentials.SCHOOL_TIMETABLE_TABLE
				+ " SET `timetable`='" + timetable + "' WHERE `classId`="
				+ classId;

		try {
			Statement statement = connection.createStatement();
			rows = statement.executeUpdate(query);
			statement.close();
		} catch (Exception e) {
		}

		return rows;
	}

	public static int uploadNewsArticle(DBConnection dbConnection, String date,
			String title, String content) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;
		String query = "INSERT INTO " + DBCredentials.SCHOOL_NEWS_TABLE
				+ " (`date`, `title`, `content`) VALUES ('" + date + "','"
				+ title + "','" + content + "')";

		try {
			Statement statement = connection.createStatement();
			rows = statement.executeUpdate(query);
			statement.close();
		} catch (Exception e) {
		}

		return rows;
	}

	public static int checkForDuplicateUsernames(DBConnection dbConnection,
			JSONArray usernames) {
		Connection connection = dbConnection.getConnection();

		int index = -1;
		boolean duplicate = false;
		String query = "SELECT `username` FROM ";
		String[] tables = new String[] { DBCredentials.STUDENT_TABLE,
				DBCredentials.TEACHER_TABLE, DBCredentials.AUXILIARY_TABLE };

		try {
			Statement statement = connection.createStatement();

			for (String table : tables) {
				String sql = query + table;
				ResultSet result = statement.executeQuery(sql);

				while (result.next()) {
					for (int i = 0; i < usernames.size(); i++) {
						if (usernames.getString(i).equals(
								result.getString("username"))) {
							index = i;
							duplicate = true;
							break;
						}
					}
					if (duplicate) {
						break;
					}
				}

				if (duplicate) {
					break;
				}
			}

			statement.close();
		} catch (Exception e) {
		}

		return index;
	}

	public static boolean checkForDuplicateUsernamesWithId(
			DBConnection dbConnection, String username, int id, String table) {
		Connection connection = dbConnection.getConnection();

		int userId = -1;
		String userTable = "";
		boolean duplicate = false;
		String query = "SELECT `id`, `username` FROM ";
		String[] tables = new String[] { DBCredentials.STUDENT_TABLE,
				DBCredentials.TEACHER_TABLE, DBCredentials.AUXILIARY_TABLE };

		try {
			Statement statement = connection.createStatement();

			for (String tbl : tables) {
				String sql = query + tbl;
				ResultSet result = statement.executeQuery(sql);

				while (result.next()) {
					if (username.equals(result.getString("username"))) {
						userId = result.getInt("id");
						userTable = tbl;
						duplicate = true;
						break;
					}
					if (duplicate) {
						break;
					}
				}

				if (duplicate) {
					break;
				}
			}

			statement.close();
		} catch (Exception e) {
		}

		if (userId == -1 || (table.equals(userTable) && id == userId)) {
			return false;
		}

		return true;
	}

	public static int checkClassExists(DBConnection dbConnection, int classId,
			String className) {
		boolean exists = DBCommonOperations.classExists(classId, className);
		if (!exists) {
			// add class
			classId = DBCommonOperations.addClass(className);
		}

		return classId;
	}

	/**
	 * 
	 * @param dbConnection
	 * @param groupId
	 * @param students
	 * @return the first index that was not added (0 based) if case
	 */
	public static int populateStudents(DBConnection dbConnection, int groupId,
			JSONArray students) {
		Connection connection = dbConnection.getConnection();

		int i = 0;
		String queryPart = "INSERT INTO "
				+ DBCredentials.STUDENT_TABLE
				+ " (`firstName`, `lastName`, `cnp`, `birthdate`, `photo`, `username`, `password`, `roles`, `group`) VALUES ";

		try {
			Statement statement = connection.createStatement();

			for (i = 0; i < students.size(); i++) {
				JSONObject student = students.getJSONObject(i);
				String query = queryPart + "('"
						+ student.getString("firstname") + "','"
						+ student.getString("lastname") + "','"
						+ student.getString("cnp") + "','"
						+ student.getString("birthdate") + "','"
						+ student.getString("photo") + "','"
						+ student.getString("username") + "','"
						+ student.getString("password") + "',4," + groupId
						+ ")";
				statement.executeUpdate(query);
			}

			statement.close();
		} catch (Exception e) {
		}

		return i;
	}

	public static int populateTeachers(DBConnection dbConnection,
			JSONArray teachers) {
		Connection connection = dbConnection.getConnection();

		int i = 0;
		String queryPart = "INSERT INTO "
				+ DBCredentials.TEACHER_TABLE
				+ " (`firstName`, `lastName`, `cnp`, `birthdate`, `photo`, `username`, `password`, `roles`, `title`, `courses`) VALUES ";

		try {
			Statement statement = connection.createStatement();

			for (i = 0; i < teachers.size(); i++) {
				JSONObject teacher = teachers.getJSONObject(i);
				String query = queryPart + "('"
						+ teacher.getString("firstname") + "','"
						+ teacher.getString("lastname") + "','"
						+ teacher.getString("cnp") + "','"
						+ teacher.getString("birthdate") + "','"
						+ teacher.getString("photo") + "','"
						+ teacher.getString("username") + "','"
						+ teacher.getString("password") + "',2,"
						+ teacher.getInt("title") + ",'"
						+ teacher.getString("courses") + "')";
				statement.executeUpdate(query);
			}

			statement.close();
		} catch (Exception e) {
		}

		return i;
	}

	public static JSONArray getOtherJobs(DBConnection dbConnection) {
		return DBCommonOperations.getAuxiliaryFunctions();
	}

	public static int populateAuxiliary(DBConnection dbConnection,
			JSONArray auxiliary) {
		Connection connection = dbConnection.getConnection();

		int i = 0;
		String queryPart = "INSERT INTO "
				+ DBCredentials.AUXILIARY_TABLE
				+ " (`firstName`, `lastName`, `cnp`, `birthdate`, `photo`, `username`, `password`, `function`) VALUES ";

		try {
			Statement statement = connection.createStatement();

			for (i = 0; i < auxiliary.size(); i++) {
				JSONObject aux = auxiliary.getJSONObject(i);
				String query = queryPart + "('" + aux.getString("firstname")
						+ "','" + aux.getString("lastname") + "','"
						+ aux.getString("cnp") + "','"
						+ aux.getString("birthdate") + "','"
						+ aux.getString("photo") + "','"
						+ aux.getString("username") + "','"
						+ aux.getString("password") + "',"
						+ aux.getInt("function") + ")";
				statement.executeUpdate(query);
			}

			statement.close();
		} catch (Exception e) {
		}

		return i;
	}

	public static JSONObject getTeacherByCnp(DBConnection dbConnection,
			String cnp) {
		JSONObject teacher = new JSONObject();
		Connection connection = dbConnection.getConnection();

		String query = "SELECT * FROM " + DBCredentials.TEACHER_TABLE
				+ " WHERE `cnp`='" + cnp + "'";

		try {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);

			if (result.next()) {
				teacher.put("id", result.getInt("id"));
				teacher.put("firstname", result.getString("firstName"));
				teacher.put("lastname", result.getString("lastName"));
				teacher.put("cnp", result.getString("cnp"));
				teacher.put("birthdate", result.getString("birthdate"));
				teacher.put("username", result.getString("username"));
				teacher.put("title", result.getInt("title"));
				teacher.put("courses", result.getString("courses"));
			}

			statement.close();
		} catch (Exception e) {
		}

		return teacher;
	}

	public static JSONObject getAuxiliaryByCnp(DBConnection dbConnection,
			String cnp) {
		JSONObject teacher = new JSONObject();
		Connection connection = dbConnection.getConnection();

		String query = "SELECT * FROM " + DBCredentials.AUXILIARY_TABLE
				+ " WHERE `cnp`='" + cnp + "'";

		try {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);

			if (result.next()) {
				teacher.put("id", result.getInt("id"));
				teacher.put("firstname", result.getString("firstName"));
				teacher.put("lastname", result.getString("lastName"));
				teacher.put("cnp", result.getString("cnp"));
				teacher.put("birthdate", result.getString("birthdate"));
				teacher.put("username", result.getString("username"));
				teacher.put("function", result.getInt("function"));
			}

			statement.close();
		} catch (Exception e) {
		}

		return teacher;
	}

	public static int modifyTeacher(DBConnection dbConnection,
			JSONObject teacher) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;
		String query = "UPDATE " + DBCredentials.TEACHER_TABLE
				+ " SET `firstName`='" + teacher.getString("firstname")
				+ "', `lastName`='" + teacher.getString("lastname")
				+ "', `cnp`='" + teacher.getString("cnp") + "', `birthdate`='"
				+ teacher.getString("birthdate") + "', `username`='"
				+ teacher.getString("username") + "', `title`="
				+ teacher.getInt("title") + ", `courses`='"
				+ teacher.getString("courses") + "' WHERE `id`="
				+ teacher.getInt("id");

		try {
			Statement statement = connection.createStatement();
			rows = statement.executeUpdate(query);
			statement.close();
		} catch (Exception e) {
		}

		return rows;
	}

	public static int modifyAuxiliary(DBConnection dbConnection,
			JSONObject auxiliary) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;
		String query = "UPDATE " + DBCredentials.AUXILIARY_TABLE
				+ " SET `firstName`='" + auxiliary.getString("firstname")
				+ "', `lastName`='" + auxiliary.getString("lastname")
				+ "', `cnp`='" + auxiliary.getString("cnp")
				+ "', `birthdate`='" + auxiliary.getString("birthdate")
				+ "', `username`='" + auxiliary.getString("username")
				+ "', `function`=" + auxiliary.getInt("job")
				+ " WHERE `id`=" + auxiliary.getInt("id");

		try {
			Statement statement = connection.createStatement();
			rows = statement.executeUpdate(query);
			statement.close();
		} catch (Exception e) {
		}

		return rows;
	}

	public static int removeUser(DBConnection dbConnection, int userId,
			String table) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;
		String query = "DELETE FROM " + table + " WHERE `id`=" + userId;

		try {
			Statement statement = connection.createStatement();
			rows = statement.executeUpdate(query);

			if (rows == 1) {
				// successful delete

				if (table.equals("teacher")) {
					// get all tccIds for this teacher
					query = "SELECT `id` FROM "
							+ DBCredentials.TEACHER_COURSE_CLASS_TABLE
							+ " WHERE `teacherId`=" + userId;
					List<Integer> tccIds = new ArrayList<Integer>();
					ResultSet result = statement.executeQuery(query);
					while (result.next()) {
						tccIds.add(result.getInt("id"));
					}
					// remove all tccIds for a teacher removal
					query = "DELETE FROM "
							+ DBCredentials.TEACHER_COURSE_CLASS_TABLE
							+ " WHERE `teacherId`=" + userId;
					statement.executeUpdate(query);
					// also remove any feedback request
					for (int tccId : tccIds) {
						query = "DELETE FROM "
								+ DBCredentials.FEEDBACK_REQUEST_TABLE
								+ " WHERE `teacher_course_id`=" + tccId;
						statement.executeUpdate(query);
					}
					// free to add other deletions, based on tccIds list
				}
				
				//nothing to do for an auxiliary person
			}

			statement.close();
		} catch (Exception e) {
		}

		return rows;
	}
	/*
	 * public static void main(String[] args) { DBConnection conn =
	 * DBUtils.createDatabase("licTeorMinuneaNatiuniiBuc");
	 * DBUtils.createTable(conn, TableModels.getTableModel("student"));
	 * DBUtils.createTable(conn, TableModels.getTableModel("teacher"));
	 * DBUtils.createTable(conn, TableModels.getTableModel("auxiliary")); }
	 */
}
