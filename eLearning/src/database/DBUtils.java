package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import utils.Grades;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class DBUtils {
	private static HashMap<Integer, JSONArray> sTeachers;

	static {
		sTeachers = new HashMap<Integer, JSONArray>();
	}

	public static JSONObject checkLogin(DBConnection dbConnection,
			String username, String password) {
		Connection connection = dbConnection.getConnection();
		JSONObject result = new JSONObject();

		try {
			Statement statement = connection.createStatement();
			ResultSet resSet = statement.executeQuery("SELECT `semester` FROM "
					+ DBCredentials.SEMESTER_TABLE + " WHERE `id`=1");
			if (resSet.next()) {
				result.put("semester", resSet.getInt("semester"));
			}

			resSet.close();
			statement.close();

			String[] tables = new String[] { DBCredentials.STUDENT_TABLE,
					DBCredentials.TEACHER_TABLE, DBCredentials.AUXILIARY_TABLE,
					DBCredentials.ADMIN_TABLE };

			for (String table : tables) {
				String query = "SELECT `id`, `password`, `firstName`, `lastName` FROM "
						+ table + " WHERE `username`=?";
				String queryAdmin = "SELECT `id`, `password` FROM " + table
						+ " WHERE `username`=?";

				PreparedStatement prepStmt = connection.prepareStatement(query);

				if (table.equals(DBCredentials.ADMIN_TABLE)) {
					prepStmt = connection.prepareStatement(queryAdmin);
				}

				prepStmt.setString(1, username);
				ResultSet resultSet = prepStmt.executeQuery();

				if (resultSet.next()) {
					result.put("login",
							resultSet.getString("password").equals(password));
					result.put("userId", resultSet.getInt("id"));
					result.put("table", table);

					if (!table.equals(DBCredentials.ADMIN_TABLE)) {
						result.put("firstName",
								resultSet.getString("firstName"));
						result.put("lastName", resultSet.getString("lastName"));
					}
				}

				resultSet.close();
				prepStmt.close();
			}
		} catch (Exception e) {
			return result;
		}

		return result;
	}

	public static boolean checkPassword(DBConnection dbConnection,
			String table, int userId, String pass) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT `password` FROM " + table + " WHERE `id`=?";
		boolean match = false;

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, userId);
			ResultSet resultSet = prepStmt.executeQuery();

			if (resultSet.next()) {
				match = pass.equals(resultSet.getString(1));
			}

			resultSet.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return match;
	}

	public static int updateColumn(DBConnection dbConnection, String table,
			int userId, String column, String value) {
		Connection connection = dbConnection.getConnection();

		String query = "UPDATE " + table + " SET `" + column
				+ "`=? WHERE `id`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);

			prepStmt.setString(1, value);
			prepStmt.setInt(2, userId);

			prepStmt.executeUpdate();

			prepStmt.close();
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

		if (table.equals(DBCredentials.STUDENT_TABLE)) {
			query += ",`group`";
		} else if (table.equals(DBCredentials.TEACHER_TABLE)) {
			query += ",`courses`";
		}

		query += " FROM " + table + " WHERE `id`=?";
		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, userId);
			ResultSet rs = prepStmt.executeQuery();

			if (rs.next()) {
				info.put("firstName", rs.getString("firstName"));
				info.put("lastName", rs.getString("lastName"));
				info.put("photo", rs.getString("photo"));
				info.put("description", rs.getString("description"));
				info.put("email", rs.getString("email"));
				info.put("birthdate", rs.getString("birthdate"));

				if (table.equals(DBCredentials.STUDENT_TABLE)) {
					info.put("group",
							DBCommonOperations.getGroupName(rs.getInt("group")));
				} else if (table.equals(DBCredentials.TEACHER_TABLE)) {
					String[] courses = rs.getString("courses").split(",");
					JSONArray coursesList = DBCommonOperations
							.getCoursesInfo(courses);
					info.put("courses", coursesList);
				}

			}

			rs.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return info;
	}

	public static JSONObject getShortInformation(DBConnection dbConnection,
			String table, int userId) {
		Connection connection = dbConnection.getConnection();
		JSONObject info = new JSONObject();

		String query = "SELECT `description`, `email` FROM " + table
				+ " WHERE `id`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, userId);
			ResultSet rs = prepStmt.executeQuery();

			if (rs.next()) {
				info.put("description", rs.getString("description"));
				info.put("email", rs.getString("email"));
			}

			rs.close();
			prepStmt.close();
		} catch (Exception e) {
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
				+ " WHERE `id`=?";
		JSONObject article = new JSONObject();

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, newsArticleId);
			ResultSet resultSet = prepStmt.executeQuery();

			if (resultSet.next()) {
				article.put("id", resultSet.getInt("id"));
				article.put("date", resultSet.getString("date"));
				article.put("title", resultSet.getString("title"));
				article.put("content", resultSet.getString("content"));
			}

			resultSet.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return article;
	}

	public static String getTimetable(DBConnection dbConnection, int classId) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT `timetable` FROM "
				+ DBCredentials.SCHOOL_TIMETABLE_TABLE + " WHERE `classId`=?";
		String timetable = "";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, classId);
			ResultSet resultSet = prepStmt.executeQuery();

			if (resultSet.next()) {
				timetable = resultSet.getString(1);
			}

			resultSet.close();
			prepStmt.close();
		} catch (Exception e) {
			timetable = "{}";
		}

		return timetable;
	}

	public static int getClassIdForUser(DBConnection dbConnection, int userId) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT `group` FROM " + DBCredentials.STUDENT_TABLE
				+ " WHERE `id`=?";
		int classId = -1;

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, userId);
			ResultSet resultSet = prepStmt.executeQuery(query);

			if (resultSet.next()) {
				classId = resultSet.getInt("group");
			}

			resultSet.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return classId;
	}

	public static JSONArray getCoursesList(DBConnection dbConnection, int userId) {
		Connection connection = dbConnection.getConnection();
		String coursesIdsQuery = "SELECT `teacher_course_class_ids` FROM "
				+ DBCredentials.COURSES_LIST_TABLE + " WHERE `studentId`=?";
		String optionalIdsQuery = "SELECT `teacher_course_ids` FROM "
				+ DBCredentials.COURSES_LIST_TABLE + " WHERE `studentId`=?";
		String courseInfoQuery = "SELECT `teacherId`, `courseId`, `semester` FROM "
				+ DBCredentials.TEACHER_COURSE_CLASS_TABLE + " WHERE `id`=?";
		String optionalInfoQuery = "SELECT `teacherId`, `courseId`, `semester` FROM "
				+ DBCredentials.TEACHER_COURSE_TABLE + " WHERE `id`=?";
		JSONArray courses = new JSONArray();

		try {
			// get courses
			PreparedStatement prepStmtCourses = connection
					.prepareStatement(coursesIdsQuery);
			prepStmtCourses.setInt(1, userId);
			ResultSet resultSet = prepStmtCourses.executeQuery();

			if (resultSet.next()) {
				PreparedStatement prepStmt = connection
						.prepareStatement(courseInfoQuery);
				String[] tccIds = resultSet.getString(
						"teacher_course_class_ids").split(",");

				for (String tccId : tccIds) {
					int tcc = Integer.parseInt(tccId);
					prepStmt.setInt(1, tcc);
					ResultSet res = prepStmt.executeQuery();

					if (res.next()) {
						JSONObject course = DBCommonOperations
								.getCourseInfo(res.getInt("courseId"));
						course.put(
								"teacher",
								getTeacher(dbConnection,
										res.getInt("teacherId")));
						course.put("semester", res.getInt("semester"));
						course.put("optional", 0);
						courses.add(course);
					}
				}
				prepStmt.close();
			}
			prepStmtCourses.close();

			// get optionals
			PreparedStatement prepStmtOptionals = connection
					.prepareStatement(optionalIdsQuery);
			prepStmtOptionals.setInt(1, userId);
			resultSet = prepStmtOptionals.executeQuery();

			if (resultSet.next()) {
				PreparedStatement prepStmt = connection
						.prepareStatement(optionalInfoQuery);
				String[] tcIds = resultSet.getString("teacher_course_ids")
						.split(",");
				for (String tcId : tcIds) {
					int tc = Integer.parseInt(tcId);
					prepStmt.setInt(1, tc);
					ResultSet res = prepStmt.executeQuery();
					if (res.next()) {
						JSONObject opt = DBCommonOperations.getCourseInfo(res
								.getInt("courseId"));
						opt.put("teacher",
								getTeacher(dbConnection,
										res.getInt("teacherId")));
						opt.put("semester", res.getInt("semester"));
						opt.put("optional", 1);
						courses.add(opt);
					}
				}
				prepStmt.close();
			}
			prepStmtOptionals.close();

		} catch (Exception e) {
		}

		return courses;
	}

	public static int getTeacherCourseClassId(DBConnection dbConnection,
			int classId, int courseId, int semester) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT `id` FROM "
				+ DBCredentials.TEACHER_COURSE_CLASS_TABLE
				+ " WHERE `courseId`=? AND `classId`=? AND `semester`=?";
		int id = 0;

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, courseId);
			prepStmt.setInt(2, classId);
			prepStmt.setInt(3, semester);
			ResultSet resultSet = prepStmt.executeQuery();

			if (resultSet.next()) {
				id = resultSet.getInt("id");
			}

			resultSet.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return id;
	}

	public static int getTeacherCourseId(DBConnection dbConnection, int userId,
			boolean isStudent, int courseId, int semester) {
		Connection connection = dbConnection.getConnection();
		String tcIdsQuery = "SELECT `teacher_course_ids` FROM "
				+ DBCredentials.COURSES_LIST_TABLE + " WHERE `studentId`=?";
		String tcQuery = "SELECT `id` FROM "
				+ DBCredentials.TEACHER_COURSE_TABLE
				+ " WHERE `courseId`=? AND `semester`=?";
		String teacherTcQuery = tcQuery + " AND `teacherId`=?";
		int id = 0;

		try {
			PreparedStatement prepStmt = connection
					.prepareStatement(teacherTcQuery);
			prepStmt.setInt(1, courseId);
			prepStmt.setInt(2, semester);
			prepStmt.setInt(3, userId);

			if (!isStudent) {
				ResultSet resultSet = prepStmt.executeQuery();
				int tcId = -1;

				if (resultSet.next()) {
					tcId = resultSet.getInt("id");
				}

				resultSet.close();
				prepStmt.close();

				return tcId;
			}

			prepStmt = connection.prepareStatement(tcIdsQuery);
			prepStmt.setInt(1, userId);
			ResultSet resultSet = prepStmt.executeQuery();

			String tcIdsVal = "";
			if (resultSet.next()) {
				tcIdsVal = resultSet.getString("teacher_course_ids");
			}
			String[] tcIdsArray = tcIdsVal.split(",");
			List<Integer> tcIds = new ArrayList<Integer>();
			for (String tcId : tcIdsArray) {
				tcIds.add(Integer.parseInt(tcId));
			}

			prepStmt = connection.prepareStatement(tcQuery);
			prepStmt.setInt(1, courseId);
			prepStmt.setInt(2, semester);
			resultSet = prepStmt.executeQuery();

			boolean found = false;
			while (resultSet.next()) {
				id = resultSet.getInt("id");
				for (int tcId : tcIds) {
					if (tcId == id) {
						found = true;
						break;
					}
				}
				if (found) {
					break;
				} else {
					id = 0;
				}
			}

			prepStmt.close();
		} catch (Exception e) {
		}

		return id;
	}

	public static int getAssocId(DBConnection dbConnection, int userId,
			boolean isStudent, int courseId, int classId, int semester,
			boolean isOptional) {
		return isOptional ? DBUtils.getTeacherCourseId(dbConnection, userId,
				isStudent, courseId, semester) : DBUtils
				.getTeacherCourseClassId(dbConnection, classId, courseId,
						semester);
	}

	public static String getAssocTableName(DBConnection dbConnection,
			int assocTableId) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT `table` FROM " + DBCredentials.ASSOC_TABLE
				+ " WHERE `id`=?";
		String table = "";
		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, assocTableId);
			ResultSet resultSet = prepStmt.executeQuery(query);

			if (resultSet.next()) {
				table = resultSet.getString("table");
			}

			resultSet.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return table;
	}

	public static JSONArray getResources(DBConnection dbConnection,
			int assocId, int assocTableId) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT `content` FROM "
				+ DBCredentials.COURSE_RESOURCES_TABLE
				+ " WHERE `assoc_id`=? AND `assoc_table_id`=?";
		JSONArray resources = new JSONArray();

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, assocId);
			prepStmt.setInt(2, assocTableId);
			ResultSet resultSet = prepStmt.executeQuery();

			if (resultSet.next()) {
				String rawJson = resultSet.getString("content");
				resources = JSONArray.fromObject(rawJson);
			}

			resultSet.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return resources;
	}

	public static JSONObject getHolidayDetails(DBConnection dbConnection,
			int semester) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT * FROM " + DBCredentials.HOLIDAYS_TABLE
				+ " WHERE `semester`=?";
		JSONObject result = new JSONObject();

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, semester);
			ResultSet resultSet = prepStmt.executeQuery();

			if (resultSet.next()) {
				String startingDate = resultSet.getString("starting_date");
				result.put("startingDate", startingDate);

				int weeksNo = resultSet.getInt("total_weeks");
				result.put("totalWeeks", weeksNo);

				String[] holidayWeeks = resultSet.getString("holiday_weeks")
						.split(",");
				JSONArray dates = new JSONArray();
				for (String week : holidayWeeks) {
					dates.add(week);
				}
				result.put("holidayWeeks", dates);
			}

			prepStmt.close();
		} catch (Exception e) {
		}

		return result;
	}

	public static JSONArray getHomework(DBConnection dbConnection,
			int studentId, int assocId, int assocTableId) {
		Connection connection = dbConnection.getConnection();

		String homeworkQuery = "SELECT * FROM " + DBCredentials.HOMEWORK_TABLE
				+ " WHERE `assoc_id`=? AND `assoc_table_id`=?";
		String homeworkResultQuery = "SELECT * FROM "
				+ DBCredentials.HOMEWORK_RESULTS_TABLE
				+ " WHERE `student_id`=? AND `homework_id`=?";
		JSONArray result = new JSONArray();

		try {
			PreparedStatement prepStmtHomework = connection
					.prepareStatement(homeworkQuery);
			prepStmtHomework.setInt(1, assocId);
			prepStmtHomework.setInt(2, assocTableId);
			ResultSet homeworkResultSet = prepStmtHomework.executeQuery();

			while (homeworkResultSet.next()) {
				JSONObject homework = new JSONObject();

				int homeworkId = homeworkResultSet.getInt("id");
				homework.put("id", homeworkId);
				homework.put("name", homeworkResultSet.getString("name"));
				homework.put("text", homeworkResultSet.getString("content"));
				homework.put("deadline",
						homeworkResultSet.getString("deadline"));
				homework.put("resources",
						homeworkResultSet.getString("resources"));
				homework.put("maxGrade",
						homeworkResultSet.getString("maxGrade"));

				PreparedStatement prepStmtResults = connection
						.prepareStatement(homeworkResultQuery);
				prepStmtResults.setInt(1, studentId);
				prepStmtResults.setInt(2, homeworkId);
				ResultSet resultsResultSet = prepStmtResults.executeQuery();

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

				resultsResultSet.close();
				prepStmtResults.close();

				result.add(homework);
			}

			homeworkResultSet.close();
			prepStmtHomework.close();
		} catch (Exception e) {
		}

		return result;
	}

	public static JSONObject getFeedbackRequest(DBConnection dbConnection,
			int assocId, int assocTableId) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT `available`, `aspects` FROM "
				+ DBCredentials.FEEDBACK_REQUEST_TABLE
				+ " WHERE `assoc_id`=? AND `assoc_table_id`=?";
		JSONObject feedback = new JSONObject();

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, assocId);
			prepStmt.setInt(2, assocTableId);
			ResultSet resultSet = prepStmt.executeQuery();

			if (resultSet.next()) {
				feedback.put("isAvailable", resultSet.getInt("available"));
				feedback.put("aspects", resultSet.getString("aspects"));
			}

			resultSet.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return feedback;
	}

	public static JSONArray getForumSummary(DBConnection dbConnection,
			int assocId, int assocTableId, int isAnnouncement) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT * FROM "
				+ DBCredentials.FORUM_SUMMARY_TABLE
				+ " WHERE `assoc_id`=? AND `assoc_table_id`=? AND `is_announcement`=?";
		JSONArray forum = new JSONArray();

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, assocId);
			prepStmt.setInt(2, assocTableId);
			prepStmt.setInt(3, isAnnouncement);
			ResultSet resultSet = prepStmt.executeQuery();

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

			resultSet.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return forum;
	}

	public static JSONArray getForumSubject(DBConnection dbConnection,
			int subjectId) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT * FROM " + DBCredentials.FORUM_SUBJECT_TABLE
				+ " WHERE `subject_id`=?";
		JSONArray forum = new JSONArray();

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, subjectId);
			ResultSet resultSet = prepStmt.executeQuery();

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

			resultSet.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return forum;
	}

	public static JSONArray getMessages(DBConnection dbConnection, int userId,
			String userTable) {
		JSONArray messages = new JSONArray();
		Connection connection = dbConnection.getConnection();
		String query = "SELECT * FROM " + DBCredentials.MESSAGES_TABLE
				+ " WHERE (`initiator_id`=? AND `initiator_table`=?)"
				+ " OR (`responder_id`=? AND `responder_table`=?)";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, userId);
			prepStmt.setString(2, userTable);
			prepStmt.setInt(3, userId);
			prepStmt.setString(4, userTable);
			ResultSet resultSet = prepStmt.executeQuery();

			while (resultSet.next()) {
				JSONObject message = new JSONObject();

				message.put("id", resultSet.getInt("id"));

				String initiatorName = null, initiatorPhoto = null;
				int initiatorId = resultSet.getInt("initiator_id");
				String tableName = resultSet.getString("initiator_table");
				String userQuery = "SELECT `firstName`, `lastName`, `photo` FROM "
						+ tableName + " WHERE `id`=?";

				PreparedStatement prepStmtUser = connection
						.prepareStatement(userQuery);
				prepStmtUser.setInt(1, initiatorId);
				ResultSet resultSetUser = prepStmtUser.executeQuery();
				if (resultSetUser.next()) {
					initiatorName = resultSetUser.getString("firstName") + " "
							+ resultSetUser.getString("lastName");
					initiatorPhoto = resultSetUser.getString("photo");
				}
				resultSetUser.close();
				prepStmtUser.close();

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
						+ tableName + " WHERE `id`=?";

				prepStmtUser = connection.prepareStatement(userQuery);
				prepStmtUser.setInt(1, responderId);
				resultSetUser = prepStmtUser.executeQuery();
				if (resultSetUser.next()) {
					responderName = resultSetUser.getString("firstName") + " "
							+ resultSetUser.getString("lastName");
					responderPhoto = resultSetUser.getString("photo");
				}
				resultSetUser.close();
				prepStmtUser.close();

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

			resultSet.close();
			prepStmt.close();
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

	public static JSONObject getTeacher(DBConnection dbConnection, int teacherId) {
		Connection connection = dbConnection.getConnection();
		String query = "SELECT `firstName`, `lastName`,`title` FROM "
				+ DBCredentials.TEACHER_TABLE + " WHERE `id`=?";

		JSONObject teacher = new JSONObject();
		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, teacherId);
			ResultSet resultSet = prepStmt.executeQuery();

			if (resultSet.next()) {
				teacher.put("firstname", resultSet.getString("firstName"));
				teacher.put("lastname", resultSet.getString("lastName"));
				teacher.put("id", teacherId);
				JSONArray teacherTitles = new JSONArray();

				String[] titles = resultSet.getString("title").split(",");
				JSONArray allTitles = DBCommonOperations.getTitles();
				for (String title : titles) {
					for (int i = 0; i < allTitles.size(); i++) {
						JSONObject t = allTitles.getJSONObject(i);
						if (t.getInt("id") == Integer.parseInt(title)) {
							teacherTitles.add(t.getString("title"));
						}
					}
				}
				teacher.put("titles", teacherTitles);
			}

			resultSet.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return teacher;
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
				+ " WHERE `group`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, classId);
			ResultSet resultSet = prepStmt.executeQuery();

			while (resultSet.next()) {
				JSONObject student = getInformation(dbConnection,
						DBCredentials.STUDENT_TABLE, resultSet.getInt("id"));
				student.put("id", resultSet.getInt("id"));
				students.add(student);
			}

			resultSet.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		JSONArray classStudents = new JSONArray();
		for (int i = 0; i < students.size(); i++) {
			JSONObject student = students.getJSONObject(i);
			JSONObject newStudent = new JSONObject();
			newStudent.put("label", student.getString("firstName") + " "
					+ student.getString("lastName"));

			String description = student.containsKey("description") ? student
					.getString("description") : "";
			newStudent.put("description", description);

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
				+ " WHERE `id`=?";
		String newMessages = "";
		int result = 0;

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, messageId);
			ResultSet resultSet = prepStmt.executeQuery();

			if (resultSet.next()) {
				JSONArray messages = JSONArray.fromObject(resultSet
						.getString("messages"));
				messages.add(messageContent);
				newMessages = messages.toString();
			}

			resultSet.close();
			prepStmt.close();

			query = "UPDATE " + DBCredentials.MESSAGES_TABLE
					+ " SET `messages`=? WHERE `id`=?";

			prepStmt = connection.prepareStatement(query);
			prepStmt.setString(1, newMessages);
			prepStmt.setInt(2, messageId);
			result = prepStmt.executeUpdate();

			prepStmt.close();
		} catch (Exception e) {
		}

		return result;
	}

	public static JSONArray getAllDeadlines(DBConnection dbConnection,
			int userId) {
		Connection connection = dbConnection.getConnection();
		String coursesQuery = "SELECT `teacher_course_class_ids`, `teacher_course_ids` FROM "
				+ DBCredentials.COURSES_LIST_TABLE + " WHERE `studentId`=?";
		JSONArray deadlines = new JSONArray();

		try {
			PreparedStatement prepStmt = connection
					.prepareStatement(coursesQuery);
			prepStmt.setInt(1, userId);
			ResultSet resultSet = prepStmt.executeQuery();

			List<Integer> courseIds = new ArrayList<Integer>();
			if (resultSet.next()) {
				// add course deadlines
				String tccIdsVal = resultSet
						.getString("teacher_course_class_ids");
				String[] tccIds = tccIdsVal.split(",");
				courseIds = getCourseIdsFromAssocIds(dbConnection, tccIdsVal,
						DBCredentials.TEACHER_COURSE_CLASS_TABLE);
				for (int i = 0; i < courseIds.size(); i++) {
					JSONArray courseDeadlines = getAllDeadlines(dbConnection,
							userId, courseIds.get(i),
							Integer.parseInt(tccIds[i]), 1);

					for (int j = 0; j < courseDeadlines.size(); j++) {
						deadlines.add(courseDeadlines.get(j));
					}
				}

				// add optionals deadlines
				String tcIdsVal = resultSet.getString("teacher_course_ids");
				String[] tcIds = tcIdsVal.split(",");
				courseIds = getCourseIdsFromAssocIds(dbConnection, tcIdsVal,
						DBCredentials.TEACHER_COURSE_TABLE);
				for (int i = 0; i < courseIds.size(); i++) {
					JSONArray courseDeadlines = getAllDeadlines(dbConnection,
							userId, courseIds.get(i),
							Integer.parseInt(tcIds[i]), 2);

					for (int j = 0; j < courseDeadlines.size(); j++) {
						deadlines.add(courseDeadlines.get(j));
					}
				}
			}

			resultSet.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return deadlines;
	}

	public static JSONArray getAllDeadlines(DBConnection dbConnection,
			int userId, int courseId, int assocId, int assocTableId) {
		JSONArray deadlines = new JSONArray();
		Connection connection = dbConnection.getConnection();

		String homeworkDeadlineQuery = "SELECT `name`, `deadline` FROM "
				+ DBCredentials.HOMEWORK_TABLE
				+ " WHERE `assoc_id`=? AND `assoc_table_id`=?";
		String simpleDeadlineQuery = "SELECT `date`, `name` FROM "
				+ DBCredentials.DEADLINES_TABLE
				+ " WHERE `assoc_id`=? AND `assoc_table_id`=?";
		String courseName = DBCommonOperations.getCourseInfo(courseId)
				.getString("name");

		try {
			PreparedStatement prepStmt = connection
					.prepareStatement(homeworkDeadlineQuery);
			prepStmt.setInt(1, assocId);
			prepStmt.setInt(2, assocTableId);
			ResultSet resultSet = prepStmt.executeQuery();

			while (resultSet.next()) {
				JSONObject deadline = new JSONObject();

				deadline.put("name", resultSet.getString("name"));
				deadline.put("deadline", resultSet.getString("deadline"));
				deadline.put("course", courseName);

				deadlines.add(deadline);
			}

			prepStmt = connection.prepareStatement(simpleDeadlineQuery);
			prepStmt.setInt(1, assocId);
			prepStmt.setInt(2, assocTableId);
			resultSet = prepStmt.executeQuery();

			while (resultSet.next()) {
				JSONObject deadline = new JSONObject();

				deadline.put("name", resultSet.getString("name"));
				deadline.put("deadline", resultSet.getString("date"));
				deadline.put("course", courseName);

				deadlines.add(deadline);
			}

			resultSet.close();
			prepStmt.close();
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
	public static int uploadForumTopic(DBConnection dbConnection, int assocId,
			int assocTableId, int isAnnouncement, String subject,
			int initiatorId, String initiatorTable, int totalPosts,
			String lastPostDate, int lastPostById, String lastPostByTable) {
		Connection connection = dbConnection.getConnection();
		int primaryKey = -1;
		String query = "INSERT INTO "
				+ DBCredentials.FORUM_SUMMARY_TABLE
				+ " (`assoc_id`, `assoc_table_id`,`is_announcement`, `subject`, `initiator_id`, `initiator_table`, `total_posts`, `last_post_date`, `last_post_by_id`, `last_post_by_table`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, assocId);
			prepStmt.setInt(2, assocTableId);
			prepStmt.setInt(3, isAnnouncement);
			prepStmt.setString(4, subject);
			prepStmt.setInt(5, initiatorId);
			prepStmt.setString(6, initiatorTable);
			prepStmt.setInt(7, totalPosts);
			prepStmt.setString(8, lastPostDate);
			prepStmt.setInt(9, lastPostById);
			prepStmt.setString(10, lastPostByTable);
			int rows = prepStmt.executeUpdate();

			if (rows == 1) {
				// added row, so get the primary key
				query = "SELECT `id` FROM "
						+ DBCredentials.FORUM_SUMMARY_TABLE
						+ " WHERE `assoc_id`=? AND `assoc_table_id`=? AND `is_announcement`=? AND `subject`=? AND `last_post_date`=? AND `initiator_id`=? AND `initiator_table`=?";
				prepStmt = connection.prepareStatement(query);
				prepStmt.setInt(1, assocId);
				prepStmt.setInt(2, assocTableId);
				prepStmt.setInt(3, isAnnouncement);
				prepStmt.setString(4, subject);
				prepStmt.setString(5, lastPostDate);
				prepStmt.setInt(6, initiatorId);
				prepStmt.setString(7, initiatorTable);
				ResultSet result = prepStmt.executeQuery(query);

				if (result.next()) {
					primaryKey = result.getInt("id");
				}

				result.close();
			}

			prepStmt.close();
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
				+ " (`subject_id`, `parent_post_id`, `sender_id`, `sender_table`, `date`, `content`) VALUES (?, -1, ?, ?, ?, ?)";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, forumTopicId);
			prepStmt.setInt(2, senderId);
			prepStmt.setString(3, senderTable);
			prepStmt.setString(4, date);
			prepStmt.setString(5, content);

			rows = prepStmt.executeUpdate();

			prepStmt.close();
		} catch (Exception e) {
		}

		return rows == 1;
	}

	public static void removeForumTopic(DBConnection dbConnection,
			int forumTopicId) {
		Connection connection = dbConnection.getConnection();
		String query = "DELETE FROM " + DBCredentials.FORUM_SUMMARY_TABLE
				+ " WHERE `id`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, forumTopicId);
			prepStmt.executeUpdate();

			prepStmt.close();
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
				+ " (`subject_id`, `parent_post_id`, `sender_id`, `sender_table`, `date`, `content`) VALUES (?, ?, ?, ?, ?, ?)";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, subjectId);
			prepStmt.setInt(2, parentPostId);
			prepStmt.setInt(3, senderId);
			prepStmt.setString(4, senderTable);
			prepStmt.setString(5, date);
			prepStmt.setString(6, content);
			rows = prepStmt.executeUpdate();

			if (rows == 1) {
				// if added the entry, increase the total posts number
				query = "SELECT `total_posts` FROM "
						+ DBCredentials.FORUM_SUMMARY_TABLE + " WHERE `id`=?";
				prepStmt = connection.prepareStatement(query);
				prepStmt.setInt(1, subjectId);
				ResultSet result = prepStmt.executeQuery();

				if (result.next()) {
					int totalPosts = result.getInt("total_posts");

					query = "UPDATE " + DBCredentials.FORUM_SUMMARY_TABLE
							+ " SET `total_posts`=? WHERE `id`=?";
					prepStmt = connection.prepareStatement(query);
					prepStmt.setInt(1, ++totalPosts);
					prepStmt.setInt(2, subjectId);
					prepStmt.executeUpdate(query);
				}

				result.close();
			}

			prepStmt.close();
		} catch (Exception e) {
		}

		return rows == 1;
	}

	public static int getConversationIdBetween(DBConnection dbConnection,
			int firstId, String firstTable, int secondId, String secondTable) {
		int pk = -1;
		Connection connection = dbConnection.getConnection();
		String query = "SELECT `id` FROM "
				+ DBCredentials.MESSAGES_TABLE
				+ " WHERE (`initiator_id`=? AND `initiator_table`=? AND `responder_id`=? AND `responder_table`=?) OR "
				+ "(`initiator_id`=? AND `initiator_table`=? AND `responder_id`=? AND `responder_table`=?)";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, firstId);
			prepStmt.setString(2, firstTable);
			prepStmt.setInt(3, secondId);
			prepStmt.setString(4, secondTable);
			prepStmt.setInt(5, secondId);
			prepStmt.setString(6, secondTable);
			prepStmt.setInt(7, firstId);
			prepStmt.setString(8, firstTable);
			ResultSet result = prepStmt.executeQuery();

			if (result.next()) {
				pk = result.getInt("id");
			}

			result.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return pk;
	}

	public static boolean uploadNewMessage(DBConnection dbConnection,
			int initiatorId, String initiatorTable, int responderId,
			String responderTable, String timestamp, String content) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;

		String messages = "[{\"sender_index\":0, \"timestamp\":\"?\", \"content\":\"?\"}]";
		String query = "INSERT INTO "
				+ DBCredentials.MESSAGES_TABLE
				+ " (`initiator_id`, `initiator_table`, `responder_id`, `responder_table`, `messages`) VALUES (?, ?, ?, ?, '"
				+ messages + "')";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);

			prepStmt.setInt(1, initiatorId);
			prepStmt.setString(2, initiatorTable);
			prepStmt.setInt(3, responderId);
			prepStmt.setString(4, responderTable);
			prepStmt.setString(5, timestamp);
			prepStmt.setString(6, content);

			rows = prepStmt.executeUpdate();

			prepStmt.close();
		} catch (Exception e) {
		}

		return rows == 1;
	}

	public static int getSenderIndexForMessage(DBConnection dbConnection,
			int messageId, int userId, String userTable) {
		int index = 0;
		Connection connection = dbConnection.getConnection();
		String query = "SELECT `initiator_id`, `initiator_table` FROM "
				+ DBCredentials.MESSAGES_TABLE + " WHERE `id`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, messageId);
			ResultSet result = prepStmt.executeQuery();

			if (result.next()) {
				index = (result.getInt("initiator_id") == userId && result
						.getString("initiator_table").equals(userTable)) ? 0
						: 1;
			}

			result.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return index;
	}

	public static boolean uploadHomework(DBConnection dbConnection,
			int homeworkId, int studentId, String archivePath, String date) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;
		String query = "INSERT INTO "
				+ DBCredentials.HOMEWORK_RESULTS_TABLE
				+ " (`homework_id`, `student_id`, `uploaded`, `graded`, `archive`, `upload_time`) VALUES (?, ? , 1, 0, ?, ?)";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, homeworkId);
			prepStmt.setInt(2, studentId);
			prepStmt.setString(3, archivePath);
			prepStmt.setString(4, date);

			rows = prepStmt.executeUpdate();
			prepStmt.close();
		} catch (Exception e) {
		}

		return rows == 1;
	}

	public static int getFeedbackId(DBConnection dbConnection, int assocId,
			int assocTableId) {
		Connection connection = dbConnection.getConnection();

		int id = 0;
		String query = "SELECT `id` FROM "
				+ DBCredentials.FEEDBACK_REQUEST_TABLE
				+ " WHERE `assoc_id`=? AND `assoc_table_id`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, assocId);
			prepStmt.setInt(2, assocTableId);
			ResultSet result = prepStmt.executeQuery();

			if (result.next()) {
				id = result.getInt("id");
			}

			result.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return id;
	}

	public static boolean uploadFeedback(DBConnection dbConnection,
			int feedbackId, int studentId, String opinion) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;
		String query = "INSERT INTO " + DBCredentials.FEEDBACK_TABLE
				+ " (`feedback_id`, `student_id`, `opinion`) VALUES (?, ?, ?)";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, feedbackId);
			prepStmt.setInt(2, studentId);
			prepStmt.setString(3, opinion);

			rows = prepStmt.executeUpdate();
			prepStmt.close();
		} catch (Exception e) {
		}

		return rows == 1;
	}

	public static boolean getFeedbackStatus(DBConnection dbConnection,
			int feedbackId, int studentId) {
		Connection connection = dbConnection.getConnection();

		boolean given = false;
		String query = "SELECT `id` FROM " + DBCredentials.FEEDBACK_TABLE
				+ " WHERE `feedback_id`=? AND `student_id`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, feedbackId);
			prepStmt.setInt(2, studentId);

			ResultSet result = prepStmt.executeQuery();
			given = result.next();

			result.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return given;
	}

	public static JSONArray getTeacherCoursesAssoc(DBConnection dbConnection,
			int teacherId) {
		Connection connection = dbConnection.getConnection();

		String tccQuery = "SELECT `courseId`, `classId`, `semester` FROM "
				+ DBCredentials.TEACHER_COURSE_CLASS_TABLE
				+ " WHERE `teacherId`=?";
		String tcQuery = "SELECT `courseId`, `semester` FROM "
				+ DBCredentials.TEACHER_COURSE_TABLE + " WHERE `teacherId`=?";

		JSONArray semesterAssocs = new JSONArray();

		JSONObject sem1 = new JSONObject();
		sem1.put("semester", 1);
		sem1.put("assocs", new JSONArray());
		semesterAssocs.add(sem1);

		JSONObject sem2 = new JSONObject();
		sem2.put("semester", 2);
		sem2.put("assocs", new JSONArray());
		semesterAssocs.add(sem2);

		try {
			PreparedStatement statement = connection.prepareStatement(tccQuery);
			statement.setInt(1, teacherId);
			ResultSet result = statement.executeQuery();

			// add all courses
			while (result.next()) {
				int classId = result.getInt("classId");
				int courseId = result.getInt("courseId");
				int semester = result.getInt("semester");

				JSONObject obj = new JSONObject();
				obj.put("course", new JSONObject());
				obj.put("class", new JSONObject());

				obj.getJSONObject("course").put("id", courseId);
				JSONObject jsonCourse = DBCommonOperations
						.getCourseInfo(courseId);
				obj.getJSONObject("course").put("name",
						jsonCourse.getString("name"));
				obj.getJSONObject("course").put("control",
						jsonCourse.getInt("control"));

				obj.getJSONObject("class").put("id", classId);
				obj.getJSONObject("class").put("name",
						DBCommonOperations.getGroupName(classId));

				// check if this course was added
				boolean found = false;
				int index = -1;
				JSONArray assocs = semesterAssocs.getJSONObject(semester - 1)
						.getJSONArray("assocs");
				for (int i = 0; i < assocs.size(); i++) {
					int id = assocs.getJSONObject(i).getJSONObject("course")
							.getInt("id");
					if (id == courseId) {
						found = true;
						index = i;
						break;
					}
				}
				if (!found) {
					// add the course and the class
					JSONObject formattedObj = new JSONObject();
					formattedObj.put("course", obj.getJSONObject("course"));
					formattedObj.put("classes", new JSONArray());
					formattedObj.getJSONArray("classes").add(
							obj.getJSONObject("class"));
					assocs.add(formattedObj);
				} else {
					// add only the class
					assocs.getJSONObject(index).getJSONArray("classes")
							.add(obj.getJSONObject("class"));
				}
			}

			// add all optionals
			statement = connection.prepareStatement(tcQuery);
			statement.setInt(1, teacherId);
			result = statement.executeQuery();

			while (result.next()) {
				int courseId = result.getInt("courseId");
				int semester = result.getInt("semester");

				JSONObject obj = new JSONObject();
				obj.put("course", new JSONObject());
				obj.put("class", new JSONObject());

				obj.getJSONObject("course").put("id", courseId);
				JSONObject jsonCourse = DBCommonOperations
						.getCourseInfo(courseId);
				obj.getJSONObject("course").put("name",
						jsonCourse.getString("name"));
				obj.getJSONObject("course").put("control",
						jsonCourse.getInt("control"));

				obj.getJSONObject("class").put("id", -1);
				obj.getJSONObject("class").put("name", "Optional");

				// check if this course was added
				boolean found = false;
				int index = -1;
				JSONArray assocs = semesterAssocs.getJSONObject(semester - 1)
						.getJSONArray("assocs");
				for (int i = 0; i < assocs.size(); i++) {
					int id = assocs.getJSONObject(i).getJSONObject("course")
							.getInt("id");
					if (id == courseId) {
						found = true;
						index = i;
						break;
					}
				}
				if (!found) {
					// add the course and the class
					JSONObject formattedObj = new JSONObject();
					formattedObj.put("course", obj.getJSONObject("course"));
					formattedObj.put("classes", new JSONArray());
					formattedObj.getJSONArray("classes").add(
							obj.getJSONObject("class"));
					assocs.add(formattedObj);
				} else {
					// add only the class
					assocs.getJSONObject(index).getJSONArray("classes")
							.add(obj.getJSONObject("class"));
				}
			}

			result.close();
			statement.close();
		} catch (Exception e) {
		}

		return semesterAssocs;
	}

	public static int getTeacherFeedbackId(DBConnection dbConnection,
			int assocId, int assocTableId) {
		Connection connection = dbConnection.getConnection();

		int id = -1;
		String query = "SELECT `id` FROM "
				+ DBCredentials.FEEDBACK_REQUEST_TABLE
				+ " WHERE `assoc_id`=? AND `assoc_table_id`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, assocId);
			prepStmt.setInt(2, assocTableId);

			ResultSet result = prepStmt.executeQuery();

			if (result.next()) {
				id = result.getInt("id");
			}

			result.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return id;
	}

	public static boolean updateTeacherFeedback(DBConnection dbConnection,
			int feedbackId, int available, String aspects) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;
		String query = "UPDATE " + DBCredentials.FEEDBACK_REQUEST_TABLE
				+ " SET `available`=?, `aspects`=? WHERE `id`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, available);
			prepStmt.setString(2, aspects);
			prepStmt.setInt(3, feedbackId);

			rows = prepStmt.executeUpdate();
			prepStmt.close();
		} catch (Exception e) {
		}

		return rows == 1;
	}

	public static boolean uploadTeacherFeedback(DBConnection dbConnection,
			int assocId, int assocTableId, int available, String aspects) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;
		String query = "INSERT INTO "
				+ DBCredentials.FEEDBACK_REQUEST_TABLE
				+ " (`assoc_id`, `assoc_table_id`, `available`, `aspects`) VALUES (?, ?, ?, ?)";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, assocId);
			prepStmt.setInt(2, assocTableId);
			prepStmt.setInt(3, available);
			prepStmt.setString(4, aspects);

			rows = prepStmt.executeUpdate();
			prepStmt.close();
		} catch (Exception e) {
		}

		return rows == 1;
	}

	public static int uploadTeacherHomework(DBConnection dbConnection,
			int assocId, int assocTableId, String name, String content,
			String deadline, String resources, float maxGrade) {
		Connection connection = dbConnection.getConnection();

		int rows = 0, id = -1;
		String query = "INSERT INTO "
				+ DBCredentials.HOMEWORK_TABLE
				+ " (`assoc_id`, `assoc_table_id`, `name`, `content`, `deadline`, `resources`, `maxGrade`) VALUES (?, ?, ?, ?, ?, ?, ?)";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, assocId);
			prepStmt.setInt(2, assocTableId);
			prepStmt.setString(3, name);
			prepStmt.setString(4, content);
			prepStmt.setString(5, deadline);
			prepStmt.setString(6, resources);
			prepStmt.setFloat(7, maxGrade);

			rows = prepStmt.executeUpdate();

			if (rows == 1) {
				query = "SELECT `id` FROM "
						+ DBCredentials.HOMEWORK_TABLE
						+ " WHERE `assoc_id`=? AND `assoc_table_id`=? AND `name`=? AND `deadline`=?";
				prepStmt = connection.prepareStatement(query);
				prepStmt.setInt(1, assocId);
				prepStmt.setInt(2, assocTableId);
				prepStmt.setString(3, name);
				prepStmt.setString(4, deadline);

				ResultSet result = prepStmt.executeQuery(query);
				if (result.next()) {
					id = result.getInt("id");
				}
				result.close();
			}

			prepStmt.close();
		} catch (Exception e) {
		}

		return id;
	}

	public static boolean uploadTeacherHomeworkResources(
			DBConnection dbConnection, int homeworkId, String resources) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;
		String resourcesQuery = "SELECT `resources` FROM "
				+ DBCredentials.HOMEWORK_TABLE + " WHERE `id`=?";

		try {
			PreparedStatement prepStmt = connection
					.prepareStatement(resourcesQuery);
			prepStmt.setInt(1, homeworkId);

			// first get the resources and keep the links
			ResultSet result = prepStmt.executeQuery();
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
					+ " SET `resources`=? WHERE `id`=?";

			prepStmt = connection.prepareStatement(query);
			prepStmt.setString(1, res.toString());
			prepStmt.setInt(2, homeworkId);

			rows = prepStmt.executeUpdate(query);
			prepStmt.close();
		} catch (Exception e) {
		}

		return rows == 1;
	}

	public static JSONArray getHomeworkListForTeacher(
			DBConnection dbConnection, int assocId, int assocTableId) {
		JSONArray homework = new JSONArray();
		Connection connection = dbConnection.getConnection();

		String query = "SELECT `id`, `name` FROM "
				+ DBCredentials.HOMEWORK_TABLE
				+ " WHERE `assoc_id`=? AND `assoc_table_id`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, assocId);
			prepStmt.setInt(2, assocTableId);

			ResultSet result = prepStmt.executeQuery();

			while (result.next()) {
				JSONObject homewrk = new JSONObject();

				homewrk.put("id", result.getInt("id"));
				homewrk.put("name", result.getString("name"));

				homework.add(homewrk);
			}

			result.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return homework;
	}

	public static JSONObject getTeacherHomework(DBConnection dbConnection,
			int homeworkId) {
		JSONObject homework = new JSONObject();
		Connection connection = dbConnection.getConnection();
		String query = "SELECT `name`, `content`, `deadline`, `resources`, `maxGrade` FROM "
				+ DBCredentials.HOMEWORK_TABLE + " WHERE `id`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, homeworkId);
			ResultSet result = prepStmt.executeQuery();

			if (result.next()) {
				homework.put("name", result.getString("name"));
				homework.put("text", result.getString("content"));
				homework.put("deadline", result.getString("deadline"));
				homework.put("resources", result.getString("resources"));
				homework.put("maxGrade", result.getDouble("maxGrade"));
			}

			prepStmt.close();
		} catch (Exception e) {
		}

		return homework;
	}

	public static int updateTeacherHomework(DBConnection dbConnection,
			int homeworkId, String name, String content, String deadline,
			String resources, float maxGrade) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;
		String query = "UPDATE "
				+ DBCredentials.HOMEWORK_TABLE
				+ " SET `name`=?, `content`=?, `deadline`=?, `resources`=?, `maxGrade`=? WHERE `id`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setString(1, name);
			prepStmt.setString(2, content);
			prepStmt.setString(3, deadline);
			prepStmt.setString(4, resources);
			prepStmt.setFloat(5, maxGrade);
			prepStmt.setInt(6, homeworkId);

			rows = prepStmt.executeUpdate();
			prepStmt.close();
		} catch (Exception e) {
		}

		return rows;
	}

	public static JSONObject getHomeworkNameAndMaxGrade(
			DBConnection dbConnection, int homeworkId) {
		Connection connection = dbConnection.getConnection();

		JSONObject json = new JSONObject();
		String query = "SELECT `name`, `maxGrade`, `deadline` FROM "
				+ DBCredentials.HOMEWORK_TABLE + " WHERE `id`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, homeworkId);
			ResultSet result = prepStmt.executeQuery();

			if (result.next()) {
				json.put("title", result.getString("name"));
				json.put("maxGrade", result.getDouble("maxGrade"));
				json.put("deadline", result.getString("deadline"));
			}

			prepStmt.close();
		} catch (Exception e) {
		}

		return json;
	}

	public static JSONArray getSubmittedHomework(DBConnection dbConnection,
			int homeworkId) {
		Connection connection = dbConnection.getConnection();

		JSONArray json = new JSONArray();
		String query = "SELECT `id`, `student_id`, `uploaded`, `graded`, `grade`, `feedback`, `archive`, `upload_time` FROM "
				+ DBCredentials.HOMEWORK_RESULTS_TABLE
				+ " WHERE `homework_id`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, homeworkId);
			ResultSet result = prepStmt.executeQuery();

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
						+ DBCredentials.STUDENT_TABLE + " WHERE `id`=?";
				PreparedStatement prepStmt2 = connection
						.prepareStatement(query);
				prepStmt2.setInt(1, result.getInt("student_id"));
				ResultSet res = prepStmt2.executeQuery();

				if (res.next()) {
					obj.put("student",
							res.getString("lastName") + " "
									+ res.getString("firstName"));
				}

				json.add(obj);
				res.close();
				prepStmt2.close();
			}

			result.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return json;
	}

	public static int rateHomework(DBConnection dbConnection, int homeworkId,
			int graded, float grade, String feedback) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;
		String query = "UPDATE "
				+ DBCredentials.HOMEWORK_RESULTS_TABLE
				+ " SET `graded`=?, `grade`=?, `feedback`=? WHERE `homework_id`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, graded);
			prepStmt.setFloat(2, grade);
			prepStmt.setString(3, feedback);
			prepStmt.setInt(4, homeworkId);

			rows = prepStmt.executeUpdate();
			prepStmt.close();
		} catch (Exception e) {
		}

		return rows;
	}

	public static JSONObject getWeekResources(DBConnection dbConnection,
			int assocId, int assocTableId, int weekId) {
		Connection connection = dbConnection.getConnection();

		JSONObject resource = new JSONObject();
		String query = "SELECT `content` FROM "
				+ DBCredentials.COURSE_RESOURCES_TABLE
				+ " WHERE `assoc_id`=? AND `assoc_table_id`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, assocId);
			prepStmt.setInt(2, assocTableId);

			ResultSet result = prepStmt.executeQuery();

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

			prepStmt.close();
		} catch (Exception e) {
		}

		return resource;
	}

	public static int uploadCourseWeekInfo(DBConnection dbConnection,
			int assocId, int assocTableId, int weekId, String description,
			String resources) {
		Connection connection = dbConnection.getConnection();

		int rows = 0, id = 0;
		JSONArray allResources = new JSONArray();
		String query = "SELECT `id`, `content` FROM "
				+ DBCredentials.COURSE_RESOURCES_TABLE
				+ " WHERE `assoc_id`=? AND `assoc_table_id`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, assocId);
			prepStmt.setInt(2, assocTableId);

			ResultSet result = prepStmt.executeQuery();

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

			result.close();
			query = "UPDATE " + DBCredentials.COURSE_RESOURCES_TABLE
					+ " SET `content`=? WHERE `id`=?";
			prepStmt = connection.prepareStatement(query);
			prepStmt.setString(1, allResources.toString());
			prepStmt.setInt(2, id);

			rows = prepStmt.executeUpdate(query);

			prepStmt.close();
		} catch (Exception e) {
		}

		return rows;
	}

	public static int uploadCourseWeekResources(DBConnection dbConnection,
			int assocId, int assocTableId, int weekId, JSONArray resources) {
		Connection connection = dbConnection.getConnection();

		int rows = 0, id = 0;
		String query = "SELECT `id`, `content` FROM "
				+ DBCredentials.COURSE_RESOURCES_TABLE
				+ " WHERE `assoc_id`=? AND `assoc_table_id`=?";
		JSONArray allResources = new JSONArray();

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, assocId);
			prepStmt.setInt(2, assocTableId);

			ResultSet result = prepStmt.executeQuery();

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
			result.close();
			query = "UPDATE " + DBCredentials.COURSE_RESOURCES_TABLE
					+ " SET `content`=? WHERE `id`=?";
			prepStmt = connection.prepareStatement(query);
			prepStmt.setString(1, allResources.toString());
			prepStmt.setInt(2, id);

			rows = prepStmt.executeUpdate(query);

			prepStmt.close();
		} catch (Exception e) {
		}

		return rows;
	}

	public static JSONArray getAuxTimetable(DBConnection dbConnection,
			int userId) {
		Connection connection = dbConnection.getConnection();

		JSONArray timetable = new JSONArray();
		String query = "SELECT `timetable` FROM "
				+ DBCredentials.AUXILIARY_TABLE + " WHERE `id`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, userId);
			ResultSet result = prepStmt.executeQuery();

			if (result.next()) {
				timetable = JSONArray.fromObject(result.getString("timetable"));
			}

			result.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return timetable;
	}

	public static int uploadAuxTimetable(DBConnection dbConnection, int userId,
			String timetable) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;
		String query = "UPDATE " + DBCredentials.AUXILIARY_TABLE
				+ " SET `timetable`=? WHERE `id`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setString(1, timetable);
			prepStmt.setInt(2, userId);

			rows = prepStmt.executeUpdate();
			prepStmt.close();
		} catch (Exception e) {
		}

		return rows;
	}

	public static JSONArray getTeacherList(DBConnection dbConnection) {
		Connection connection = dbConnection.getConnection();

		String query = "SELECT `id`, `firstName`, `lastName` FROM "
				+ DBCredentials.TEACHER_TABLE;

		JSONArray teachers = new JSONArray();

		try {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);

			while (result.next()) {
				JSONObject teacher = new JSONObject();

				teacher.put("firstname", result.getString("firstName"));
				teacher.put("lastname", result.getString("lastName"));
				teacher.put("id", result.getInt("id"));

				teachers.add(teacher);
			}

			statement.close();
		} catch (Exception e) {
		}

		return teachers;
	}

	public static JSONArray getAllCoursesList(DBConnection dbConnection) {
		return DBCommonOperations.getAllCourses();
	}

	public static int uploadClassTimetable(DBConnection dbConnection,
			int classId, String timetable) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;
		String query = "UPDATE " + DBCredentials.SCHOOL_TIMETABLE_TABLE
				+ " SET `timetable`=? WHERE `classId`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setString(1, timetable);
			prepStmt.setInt(2, classId);

			rows = prepStmt.executeUpdate();
			prepStmt.close();
		} catch (Exception e) {
		}

		return rows;
	}

	public static int uploadNewsArticle(DBConnection dbConnection, String date,
			String title, String content) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;
		String query = "INSERT INTO " + DBCredentials.SCHOOL_NEWS_TABLE
				+ " (`date`, `title`, `content`) VALUES (?, ?, ?)";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setString(1, date);
			prepStmt.setString(2, title);
			prepStmt.setString(3, content);

			rows = prepStmt.executeUpdate();
			prepStmt.close();
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
		String query = "INSERT INTO "
				+ DBCredentials.STUDENT_TABLE
				+ " (`firstName`, `lastName`, `cnp`, `birthdate`, `photo`, `username`, `password`, `roles`, `group`) VALUES (?, ?, ?, ?, ?, ?, ?, 4, ?)";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);

			for (i = 0; i < students.size(); i++) {
				JSONObject student = students.getJSONObject(i);

				prepStmt.setString(1, student.getString("firstname"));
				prepStmt.setString(2, student.getString("lastname"));
				prepStmt.setString(3, student.getString("cnp"));
				prepStmt.setString(4, student.getString("birthdate"));
				prepStmt.setString(5, student.getString("photo"));
				prepStmt.setString(6, student.getString("username"));
				prepStmt.setString(7, student.getString("password"));
				prepStmt.setInt(8, groupId);

				prepStmt.executeUpdate();
			}

			prepStmt.close();
		} catch (Exception e) {
		}

		return i;
	}

	public static int populateTeachers(DBConnection dbConnection,
			JSONArray teachers) {
		Connection connection = dbConnection.getConnection();

		int i = 0;
		String query = "INSERT INTO "
				+ DBCredentials.TEACHER_TABLE
				+ " (`firstName`, `lastName`, `cnp`, `birthdate`, `photo`, `username`, `password`, `roles`, `title`, `courses`) VALUES (?, ?, ?, ?, ?, ?, ?, 2, ?, ?)";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);

			for (i = 0; i < teachers.size(); i++) {
				JSONObject teacher = teachers.getJSONObject(i);

				prepStmt.setString(1, teacher.getString("firstname"));
				prepStmt.setString(2, teacher.getString("lastname"));
				prepStmt.setString(3, teacher.getString("cnp"));
				prepStmt.setString(4, teacher.getString("birthdate"));
				prepStmt.setString(5, teacher.getString("photo"));
				prepStmt.setString(6, teacher.getString("username"));
				prepStmt.setString(7, teacher.getString("password"));
				prepStmt.setInt(8, teacher.getInt("title"));
				prepStmt.setString(9, teacher.getString("courses"));

				prepStmt.executeUpdate();
			}

			prepStmt.close();
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
		String query = "INSERT INTO "
				+ DBCredentials.AUXILIARY_TABLE
				+ " (`firstName`, `lastName`, `cnp`, `birthdate`, `photo`, `username`, `password`, `function`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);

			for (i = 0; i < auxiliary.size(); i++) {
				JSONObject aux = auxiliary.getJSONObject(i);

				prepStmt.setString(1, aux.getString("firstname"));
				prepStmt.setString(2, aux.getString("lastname"));
				prepStmt.setString(3, aux.getString("cnp"));
				prepStmt.setString(4, aux.getString("birthdate"));
				prepStmt.setString(5, aux.getString("photo"));
				prepStmt.setString(6, aux.getString("username"));
				prepStmt.setString(7, aux.getString("password"));
				prepStmt.setInt(8, aux.getInt("function"));

				prepStmt.executeUpdate(query);
			}

			prepStmt.close();
		} catch (Exception e) {
		}

		return i;
	}

	public static JSONObject getTeacherByCnp(DBConnection dbConnection,
			String cnp) {
		JSONObject teacher = new JSONObject();
		Connection connection = dbConnection.getConnection();

		String query = "SELECT * FROM " + DBCredentials.TEACHER_TABLE
				+ " WHERE `cnp`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setString(1, cnp);

			ResultSet result = prepStmt.executeQuery();

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

			result.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return teacher;
	}

	public static JSONObject getAuxiliaryByCnp(DBConnection dbConnection,
			String cnp) {
		JSONObject teacher = new JSONObject();
		Connection connection = dbConnection.getConnection();

		String query = "SELECT * FROM " + DBCredentials.AUXILIARY_TABLE
				+ " WHERE `cnp`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setString(1, cnp);
			ResultSet result = prepStmt.executeQuery();

			if (result.next()) {
				teacher.put("id", result.getInt("id"));
				teacher.put("firstname", result.getString("firstName"));
				teacher.put("lastname", result.getString("lastName"));
				teacher.put("cnp", result.getString("cnp"));
				teacher.put("birthdate", result.getString("birthdate"));
				teacher.put("username", result.getString("username"));
				teacher.put("function", result.getInt("function"));
			}

			result.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return teacher;
	}

	public static JSONObject getStudentByCnp(DBConnection dbConnection,
			String cnp) {
		JSONObject student = new JSONObject();
		Connection connection = dbConnection.getConnection();

		String query = "SELECT * FROM " + DBCredentials.STUDENT_TABLE
				+ " WHERE `cnp`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setString(1, cnp);
			ResultSet result = prepStmt.executeQuery();

			if (result.next()) {
				student.put("id", result.getInt("id"));
				student.put("firstname", result.getString("firstName"));
				student.put("lastname", result.getString("lastName"));
				student.put("cnp", result.getString("cnp"));
				student.put("birthdate", result.getString("birthdate"));
				student.put("username", result.getString("username"));
				student.put("group", result.getInt("group"));
			}

			result.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return student;
	}

	public static int modifyTeacher(DBConnection dbConnection,
			JSONObject teacher) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;
		String query = "UPDATE "
				+ DBCredentials.TEACHER_TABLE
				+ " SET `firstName`=?, `lastName`=?, `cnp`=?, `birthdate`=?, `username`=?, `title`=?, `courses`=? WHERE `id`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setString(1, teacher.getString("firstname"));
			prepStmt.setString(2, teacher.getString("lastname"));
			prepStmt.setString(3, teacher.getString("cnp"));
			prepStmt.setString(4, teacher.getString("birthdate"));
			prepStmt.setString(5, teacher.getString("username"));
			prepStmt.setInt(6, teacher.getInt("title"));
			prepStmt.setString(7, teacher.getString("courses"));
			prepStmt.setInt(8, teacher.getInt("id"));

			rows = prepStmt.executeUpdate();
			prepStmt.close();
		} catch (Exception e) {
		}

		return rows;
	}

	public static int modifyAuxiliary(DBConnection dbConnection,
			JSONObject auxiliary) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;
		String query = "UPDATE "
				+ DBCredentials.AUXILIARY_TABLE
				+ " SET `firstName`=?, `lastName`=?, `cnp`=?, `birthdate`=?, `username`=?, `function`=? WHERE `id`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setString(1, auxiliary.getString("firstname"));
			prepStmt.setString(2, auxiliary.getString("lastname"));
			prepStmt.setString(3, auxiliary.getString("cnp"));
			prepStmt.setString(4, auxiliary.getString("birthdate"));
			prepStmt.setString(5, auxiliary.getString("username"));
			prepStmt.setInt(6, auxiliary.getInt("job"));
			prepStmt.setInt(7, auxiliary.getInt("id"));

			rows = prepStmt.executeUpdate();
			prepStmt.close();
		} catch (Exception e) {
		}

		return rows;
	}

	public static int removeUser(DBConnection dbConnection, int userId,
			String table) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;
		String query = "DELETE FROM " + table + " WHERE `id`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, userId);

			rows = prepStmt.executeUpdate();

			if (rows == 1) {
				// successful delete

				if (table.equals(DBCredentials.TEACHER_TABLE)) {
					// GET ALL TCC_IDS FOR THIS TEACHER
					query = "SELECT `id` FROM "
							+ DBCredentials.TEACHER_COURSE_CLASS_TABLE
							+ " WHERE `teacherId`=?";
					prepStmt = connection.prepareStatement(query);
					prepStmt.setInt(1, userId);
					ResultSet result = prepStmt.executeQuery();

					List<Integer> tccIds = new ArrayList<Integer>();

					while (result.next()) {
						tccIds.add(result.getInt("id"));
					}

					// remove all tccIds for a teacher removal
					query = "DELETE FROM "
							+ DBCredentials.TEACHER_COURSE_CLASS_TABLE
							+ " WHERE `teacherId`=?";
					prepStmt = connection.prepareStatement(query);
					prepStmt.setInt(1, userId);

					prepStmt.executeUpdate();
					// also remove any feedback request
					for (int tccId : tccIds) {
						query = "DELETE FROM "
								+ DBCredentials.FEEDBACK_REQUEST_TABLE
								+ " WHERE `assoc_id`=? AND `assoc_table_id`=1";
						prepStmt = connection.prepareStatement(query);
						prepStmt.setInt(1, tccId);
						prepStmt.executeUpdate();
					}
					// free to add other deletions, based on tccIds list

					// GET ALL TC_IDS FOR THIS TEACHER
					query = "SELECT `id` FROM "
							+ DBCredentials.TEACHER_COURSE_TABLE
							+ " WHERE `teacherId`=?";
					prepStmt = connection.prepareStatement(query);
					prepStmt.setInt(1, userId);
					result = prepStmt.executeQuery();

					List<Integer> tcIds = new ArrayList<Integer>();

					while (result.next()) {
						tcIds.add(result.getInt("id"));
					}

					// remove all tccIds for a teacher removal
					query = "DELETE FROM " + DBCredentials.TEACHER_COURSE_TABLE
							+ " WHERE `teacherId`=?";
					prepStmt = connection.prepareStatement(query);
					prepStmt.setInt(1, userId);

					prepStmt.executeUpdate();
					// also remove any feedback request
					for (int tcId : tcIds) {
						query = "DELETE FROM "
								+ DBCredentials.FEEDBACK_REQUEST_TABLE
								+ " WHERE `assoc_id`=? AND `assoc_table_id`=2";
						prepStmt = connection.prepareStatement(query);
						prepStmt.setInt(1, tcId);
						prepStmt.executeUpdate();
					}
					// free to add other deletions, based on tccIds list

					result.close();
				}

				// nothing to do for an auxiliary person
			}

			prepStmt.close();
		} catch (Exception e) {
		}

		return rows;
	}

	public static int modifyStudent(DBConnection dbConnection,
			JSONObject student) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;
		String query = "UPDATE "
				+ DBCredentials.STUDENT_TABLE
				+ " SET `firstName`=?, `lastName`=?, `cnp`=?, `birthdate`=?, `username`=?, `group`=? WHERE `id`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setString(1, student.getString("firstname"));
			prepStmt.setString(2, student.getString("lastname"));
			prepStmt.setString(3, student.getString("cnp"));
			prepStmt.setString(4, student.getString("birthdate"));
			prepStmt.setString(5, student.getString("username"));
			prepStmt.setInt(6, student.getInt("group"));
			prepStmt.setInt(7, student.getInt("id"));

			rows = prepStmt.executeUpdate();
			prepStmt.close();
		} catch (Exception e) {
		}

		return rows;
	}

	public static int uploadNewCourses(DBConnection dbConnection,
			JSONArray courses) {
		return DBCommonOperations.addCourses(courses);
	}

	public static int doClassTransitions(DBConnection dbConnection,
			JSONArray classes) {
		Connection connection = dbConnection.getConnection();

		int ok = 0;
		String preparedStudentsQuery = "UPDATE " + DBCredentials.STUDENT_TABLE
				+ " SET `group`=? WHERE `group`=?";

		try {
			PreparedStatement statement = connection
					.prepareStatement(preparedStudentsQuery);

			for (int i = 0; i < classes.size(); i++) {
				JSONObject cls = classes.getJSONObject(i);
				int oldVal = cls.getInt("old");
				int newVal = cls.getInt("new");

				if (oldVal < 0 || newVal < 0) {
					continue;
				}

				statement.setInt(1, newVal);
				statement.setInt(2, oldVal);

				statement.executeUpdate();
			}

			statement.close();
			ok = 1;
		} catch (Exception e) {
		}

		return ok;
	}

	public static int modifyNews(DBConnection dbConnection, String date,
			JSONObject news) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;
		String query = "UPDATE " + DBCredentials.SCHOOL_NEWS_TABLE
				+ " SET `date`=?, `title`=?, `content`=? WHERE `id`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setString(1, date);
			prepStmt.setString(2, news.getString("title"));
			prepStmt.setString(3, news.getString("content"));
			prepStmt.setInt(4, news.getInt("id"));

			rows = prepStmt.executeUpdate();
			prepStmt.close();
		} catch (Exception e) {
		}

		return rows;
	}

	public static int removeNews(DBConnection dbConnection, int newsId) {
		Connection connection = dbConnection.getConnection();

		int rows = 0;
		String query = "DELETE FROM " + DBCredentials.SCHOOL_NEWS_TABLE
				+ " WHERE `id`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, newsId);

			rows = prepStmt.executeUpdate();
			prepStmt.close();
		} catch (Exception e) {
		}

		return rows;
	}

	public static List<Integer> getCourseIdsFromAssocIds(
			DBConnection dbConnection, String ids, String table) {
		Connection connection = dbConnection.getConnection();
		String[] tccIdList = ids.split(",");
		List<Integer> courseIds = new ArrayList<Integer>();
		String query = "SELECT `courseId` FROM " + table + " WHERE `id`=?";

		try {
			PreparedStatement statement = connection.prepareStatement(query);

			for (String tccId : tccIdList) {
				statement.setInt(1, Integer.parseInt(tccId));
				ResultSet result = statement.executeQuery();
				if (result.next()) {
					courseIds.add(result.getInt("courseId"));
				}
			}
			statement.close();
		} catch (Exception e) {
		}

		return courseIds;
	}

	public static int updateYear(DBConnection dbConnection, String year) {
		String query = "UPDATE " + DBCredentials.YEAR_TABLE + " SET `year`='"
				+ year + "' WHERE `id`=1";
		Connection connection = dbConnection.getConnection();
		int rows = 0;

		try {
			Statement statement = connection.createStatement();
			rows = statement.executeUpdate(query);

			statement.close();
		} catch (Exception e) {
		}

		return rows;
	}

	public static int uploadSemesterStructure(DBConnection dbConnection,
			String date1, String week1, String holiday1, String date2,
			String week2, String holiday2) {

		Connection connection = dbConnection.getConnection();
		int rows = 0;
		String query = "SELECT * FROM " + DBCredentials.HOLIDAYS_TABLE
				+ " WHERE `semester`=?";
		String addQuery = "INSERT INTO "
				+ DBCredentials.HOLIDAYS_TABLE
				+ " (`starting_date`, `total_weeks`, `holiday_weeks`, `semester`) VALUES (?, ?, ?, ?)";
		String updateQuery = "UPDATE "
				+ DBCredentials.HOLIDAYS_TABLE
				+ " SET `starting_date`=?, `total_weeks`=?, `holiday_weeks`=? WHERE `semester`=?";

		try {
			PreparedStatement statement = connection.prepareStatement(query);
			PreparedStatement addStmt = connection.prepareStatement(addQuery);
			PreparedStatement updateStmt = connection
					.prepareStatement(updateQuery);

			statement.setInt(1, 1);
			ResultSet result = statement.executeQuery();
			if (result.next()) {
				// update
				updateStmt.setString(1, date1);
				updateStmt.setInt(2, Integer.parseInt(week1));
				updateStmt.setString(3, holiday1);
				updateStmt.setInt(4, 1);
				rows += updateStmt.executeUpdate();
			} else {
				// insert
				addStmt.setString(1, date1);
				addStmt.setInt(2, Integer.parseInt(week1));
				addStmt.setString(3, holiday1);
				addStmt.setInt(4, 1);
				rows += addStmt.executeUpdate();
			}

			statement.setInt(1, 2);
			result = statement.executeQuery();
			if (result.next()) {
				// update
				updateStmt.setString(1, date2);
				updateStmt.setInt(2, Integer.parseInt(week2));
				updateStmt.setString(3, holiday2);
				updateStmt.setInt(4, 2);
				rows += updateStmt.executeUpdate();
			} else {
				// insert
				addStmt.setString(1, date2);
				addStmt.setInt(2, Integer.parseInt(week2));
				addStmt.setString(3, holiday2);
				addStmt.setInt(4, 2);
				rows += addStmt.executeUpdate();
			}

			statement.close();
		} catch (Exception e) {
		}

		return rows;
	}

	public static JSONObject getSemestersStructure(DBConnection dbConnection) {
		Connection connection = dbConnection.getConnection();
		JSONArray semesters = new JSONArray();
		JSONObject resp = new JSONObject();

		String yearQuery = "SELECT `year` FROM " + DBCredentials.YEAR_TABLE
				+ " WHERE `id`=1";
		String semestersQuery = "SELECT * FROM " + DBCredentials.HOLIDAYS_TABLE;

		try {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(semestersQuery);

			while (result.next()) {
				JSONObject semester = new JSONObject();

				semester.put("startingDate", result.getString("starting_date"));
				semester.put("weeks", result.getInt("total_weeks"));
				semester.put("holidays", result.getString("holiday_weeks"));
				semester.put("semester", result.getInt("semester"));

				semesters.add(semester);
			}

			resp.put("semesters", semesters);

			String year = "";
			result = statement.executeQuery(yearQuery);
			if (result.next()) {
				year = result.getString("year");
			}

			resp.put("year", year);

			statement.close();
		} catch (Exception e) {
		}

		return resp;
	}

	public static int getSemestersNumber(DBConnection dbConnection) {
		Connection connection = dbConnection.getConnection();
		int semester = 1;
		String query = "SELECT `semester` FROM " + DBCredentials.SEMESTER_TABLE
				+ " WHERE `id`=1";

		try {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);

			if (result.next()) {
				semester = result.getInt("semester");
			}

			statement.close();
		} catch (Exception e) {
		}

		return semester;
	}

	public static int setSemestersNumber(DBConnection dbConnection,
			int semesterNo) {
		Connection connection = dbConnection.getConnection();
		int rows = 0;
		String checkQuery = "SELECT * FROM " + DBCredentials.SEMESTER_TABLE;
		String insertQuery = "INSERT INTO " + DBCredentials.SEMESTER_TABLE
				+ " (`semester`) VALUES (?)";
		String updateQuery = "UPDATE " + DBCredentials.SEMESTER_TABLE
				+ " SET `semester`=? WHERE `id`=1";

		try {
			Statement statement = connection.createStatement();
			boolean populated = statement.executeQuery(checkQuery).next();
			statement.close();

			PreparedStatement prepStmt = connection
					.prepareStatement(populated ? updateQuery : insertQuery);
			prepStmt.setInt(1, semesterNo);
			rows = prepStmt.executeUpdate();
			statement.close();
		} catch (Exception e) {
		}

		return rows;
	}

	public static JSONArray getTccAssocs(DBConnection dbConnection,
			int classId, int semester, boolean isOptional, int studentId) {
		JSONArray assocs = new JSONArray();
		Connection connection = dbConnection.getConnection();

		String courseQuery = "SELECT `teacherId`, `courseId` FROM "
				+ DBCredentials.TEACHER_COURSE_CLASS_TABLE
				+ " WHERE `semester`=? AND `classId`=?";
		String optionalQuery = "SELECT `teacher_course_ids` FROM "
				+ DBCredentials.COURSES_LIST_TABLE + " WHERE `studentId`=?";
		String tcQuery = "SELECT `teacherId`, `courseId` FROM "
				+ DBCredentials.TEACHER_COURSE_TABLE + " WHERE `id`=?";

		try {
			PreparedStatement prepStmt = connection
					.prepareStatement(courseQuery);
			prepStmt.setInt(1, semester);
			prepStmt.setInt(2, classId);
			ResultSet result = prepStmt.executeQuery();

			while (result.next()) {
				JSONObject assoc = new JSONObject();

				assoc.put("teacherId", result.getInt("teacherId"));
				assoc.put("courseId", result.getInt("courseId"));
				assoc.put("optional", 0);

				assocs.add(assoc);
			}

			if (isOptional) {
				prepStmt = connection.prepareStatement(optionalQuery);
				prepStmt.setInt(1, studentId);
				result = prepStmt.executeQuery();

				if (result.next()) {
					String[] tcIds = result.getString("teacher_course_ids")
							.split(",");
					PreparedStatement prepStatement = connection
							.prepareStatement(tcQuery);
					for (String tcId : tcIds) {
						prepStatement.setInt(1, Integer.parseInt(tcId));
						ResultSet res = prepStatement.executeQuery();
						if (res.next()) {
							JSONObject assoc = new JSONObject();

							assoc.put("teacherId", res.getInt("teacherId"));
							assoc.put("courseId", res.getInt("courseId"));
							assoc.put("optional", 1);

							assocs.add(assoc);
						}
					}
					prepStatement.close();
				}
			}

			prepStmt.close();
		} catch (Exception e) {
		}

		return assocs;
	}

	public static JSONObject getTccAssoc(DBConnection dbConnection,
			int classId, int courseId, int semester, boolean isOptional,
			int studentId) {
		JSONObject assoc = new JSONObject();
		Connection connection = dbConnection.getConnection();

		String query = "SELECT `teacherId` FROM "
				+ DBCredentials.TEACHER_COURSE_CLASS_TABLE
				+ " WHERE `semester`=? AND `classId`=? AND `courseId`=?";
		String optionalQuery = "SELECT `teacher_course_ids` FROM "
				+ DBCredentials.COURSES_LIST_TABLE + " WHERE `studentId`=?";
		String tcQuery = "SELECT `teacherId`, `courseId` FROM "
				+ DBCredentials.TEACHER_COURSE_TABLE + " WHERE `id`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, semester);
			prepStmt.setInt(2, classId);
			prepStmt.setInt(3, courseId);
			ResultSet result = prepStmt.executeQuery();

			if (result.next()) {
				assoc.put("teacherId", result.getInt("teacherId"));
				assoc.put("courseId", courseId);
				assoc.put("optional", 0);
			} else {
				prepStmt = connection.prepareStatement(optionalQuery);
				prepStmt.setInt(1, studentId);
				result = prepStmt.executeQuery();
				if (result.next()) {
					PreparedStatement prepStatement = connection
							.prepareStatement(tcQuery);
					String[] optIds = result.getString("teacher_course_ids")
							.split(",");
					for (String optId : optIds) {
						prepStatement.setInt(1, Integer.parseInt(optId));
						ResultSet res = prepStatement.executeQuery();

						if (res.next()) {
							if (res.getInt("courseId") == courseId) {
								assoc.put("courseId", courseId);
								assoc.put("teacherId", res.getInt("teacherId"));
								assoc.put("optional", 1);
								break;
							}
						}
					}
					prepStatement.close();
				}
			}

			prepStmt.close();
		} catch (Exception e) {
		}

		return assoc;
	}

	public static int getOldTccId(DBConnection dbConnection, int courseId,
			int classId, int studentId, int semester, boolean isOptional) {
		Connection connection = dbConnection.getConnection();
		int id = -1;

		String tccQuery = "SELECT `id` FROM "
				+ DBCredentials.TEACHER_COURSE_CLASS_TABLE
				+ " WHERE `semester`=? AND `courseId`=? AND `classId`=?";
		String tcQuery = "SELECT `id` FROM "
				+ DBCredentials.TEACHER_COURSE_TABLE
				+ " WHERE `semester`=? AND `courseId`=?";
		String optionalsQuery = "SELECT `teacher_course_ids` FROM "
				+ DBCredentials.TEACHER_COURSE_TABLE + " WHERE `studentId`=?";

		try {
			PreparedStatement prepStmt = connection
					.prepareStatement(optionalsQuery);
			prepStmt.setInt(1, studentId);
			ResultSet result = prepStmt.executeQuery();

			String optionals = "";
			if (result.next()) {
				optionals = result.getString("`teacher_course_ids`");
			}
			if (optionals == null) {
				optionals = "";
			}
			String[] optionalList = optionals.split(",");

			prepStmt = connection.prepareStatement(isOptional ? tcQuery
					: tccQuery);
			prepStmt.setInt(1, semester);
			prepStmt.setInt(2, courseId);

			if (!isOptional) {
				prepStmt.setInt(3, classId);
			}

			result = prepStmt.executeQuery();

			while (result.next()) {
				boolean found = false;
				id = result.getInt("id");

				/*
				 * check the id to see if it exists in user's optionals list, as
				 * the optional may be teached by multiple teachers
				 */

				for (String optId : optionalList) {
					if (id == Integer.parseInt(optId)) {
						found = true;
						break;
					}
				}

				if (found) {
					break;
				} else {
					id = -1;
				}
			}

			result.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return id;
	}

	private static List<Integer> studentsFromGrupList(
			DBConnection dbConnection, int classId) {
		Connection connection = dbConnection.getConnection();
		List<Integer> ids = new ArrayList<Integer>();

		String studentsQuery = "SELECT `id` FROM "
				+ DBCredentials.STUDENT_TABLE + " WHERE `group`=?";

		try {
			PreparedStatement prepStmt = connection
					.prepareStatement(studentsQuery);
			prepStmt.setInt(1, classId);
			ResultSet result = prepStmt.executeQuery();

			while (result.next()) {
				ids.add(result.getInt("id"));
			}

			result.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return ids;
	}

	private static boolean tokenAlreadyAdded(String valuesList, int tccId) {
		String[] courses = valuesList.split(",");
		for (String id : courses) {
			if (Integer.parseInt(id) == tccId) {
				return true;
			}
		}

		return false;
	}

	private static String removeFromList(String valuesList, int oldTccId) {
		StringBuffer newCourses = new StringBuffer();
		int index = 0;

		String[] courses = valuesList.split(",");
		for (int i = 0; i < courses.length; i++) {
			if (Integer.parseInt(courses[i]) != oldTccId) {
				if (index > 0) {
					newCourses.append(",");
				}
				newCourses.append(courses[i]);
				index++;
			}
		}

		return newCourses.toString();
	}

	private static void updateStudentCourses(DBConnection dbConnection,
			int studentId, int tccId, int oldTccId) {
		Connection connection = dbConnection.getConnection();

		String getCurrentCoursesQuery = "SELECT `teacher_course_class_ids` FROM "
				+ DBCredentials.COURSES_LIST_TABLE + " WHERE `studentId`=?";
		String updateCoursesQuery = "UPDATE "
				+ DBCredentials.COURSES_LIST_TABLE
				+ " SET `teacher_course_class_ids`=? WHERE `studentId`=?";
		String addCoursesQuery = "INSERT INTO "
				+ DBCredentials.COURSES_LIST_TABLE
				+ " (`studentId`,`teacher_course_class_ids`) VALUES (?, ?)";

		try {
			PreparedStatement currCoursesStmt = connection
					.prepareStatement(getCurrentCoursesQuery);
			currCoursesStmt.setInt(1, studentId);
			ResultSet currCour = currCoursesStmt.executeQuery();
			boolean studentIsAdded = currCour.next();

			if (!studentIsAdded) {
				// add the student
				PreparedStatement addCoursesStmt = connection
						.prepareStatement(addCoursesQuery);
				addCoursesStmt.setInt(1, studentId);
				addCoursesStmt.setString(2, tccId + "");

				addCoursesStmt.executeUpdate();
				addCoursesStmt.close();
			} else {
				// update course list
				String allCourses = currCour
						.getString("teacher_course_class_ids");
				if (allCourses == null) {
					allCourses = "";
				}

				if (tokenAlreadyAdded(allCourses, tccId)) {
					return;
				}

				allCourses = removeFromList(allCourses, oldTccId);

				allCourses += allCourses.isEmpty() ? "" : ",";
				allCourses += tccId;

				PreparedStatement updtCoursesStmt = connection
						.prepareStatement(updateCoursesQuery);
				updtCoursesStmt.setString(1, allCourses);
				updtCoursesStmt.setInt(2, studentId);
				updtCoursesStmt.executeUpdate();

				updtCoursesStmt.close();
			}

			currCour.close();
			currCoursesStmt.close();
		} catch (Exception e) {
		}
	}

	private static void updateStudentOptionals(DBConnection dbConnection,
			int studentId, int tcId, int oldTccId) {
		Connection connection = dbConnection.getConnection();

		String getCurrentOptionalsQuery = "SELECT `teacher_course_ids` FROM "
				+ DBCredentials.COURSES_LIST_TABLE + " WHERE `studentId`=?";
		String updateOptionalsQuery = "UPDATE "
				+ DBCredentials.COURSES_LIST_TABLE
				+ " SET `teacher_course_ids`=? WHERE `studentId`=" + studentId;
		String addOptionalsQuery = "INSERT INTO "
				+ DBCredentials.COURSES_LIST_TABLE
				+ " (`studentId`,`teacher_course_ids`) VALUES (?, ?)";

		try {
			PreparedStatement currOptionalsStmt = connection
					.prepareStatement(getCurrentOptionalsQuery);
			currOptionalsStmt.setInt(1, studentId);
			ResultSet currOpts = currOptionalsStmt.executeQuery();
			boolean studentIsAdded = currOpts.next();

			if (!studentIsAdded) {
				// add the student
				PreparedStatement addCoursesStmt = connection
						.prepareStatement(addOptionalsQuery);
				addCoursesStmt.setInt(1, studentId);
				addCoursesStmt.setString(2, tcId + "");

				addCoursesStmt.executeUpdate();
				addCoursesStmt.close();
			} else {
				// update course list
				String allOptionals = currOpts.getString("teacher_course_ids");
				if (allOptionals == null) {
					allOptionals = "";
				}

				if (tokenAlreadyAdded(allOptionals, tcId)) {
					return;
				}

				allOptionals = removeFromList(allOptionals, oldTccId);

				allOptionals += allOptionals.isEmpty() ? "" : ",";
				allOptionals += tcId;

				PreparedStatement updtOptionalsStmt = connection
						.prepareStatement(updateOptionalsQuery);
				updtOptionalsStmt.setString(1, allOptionals);
				updtOptionalsStmt.executeUpdate();

				updtOptionalsStmt.close();
			}

			currOpts.close();
			currOptionalsStmt.close();
		} catch (Exception e) {
		}
	}

	private static boolean setEmptyResources(DBConnection dbConnection,
			int semester, int assocId, boolean isCourse) {
		Connection connection = dbConnection.getConnection();
		boolean added = false;
		String weeksQuery = "SELECT `total_weeks` FROM "
				+ DBCredentials.HOLIDAYS_TABLE + " WHERE `semester`=?";

		try {
			PreparedStatement prepStmt = connection
					.prepareStatement(weeksQuery);
			prepStmt.setInt(1, semester);

			// get the number of weeks of semester
			ResultSet result = prepStmt.executeQuery();
			if (result.next()) {
				int weeksNo = result.getInt("total_weeks");
				// also create the empty resources for course
				StringBuilder content = new StringBuilder("[");
				for (int i = 1; i <= weeksNo; i++) {
					content.append("{");
					content.append("\"id\":");
					content.append(i);
					content.append("}");

					if (i + 1 <= weeksNo) {
						content.append(",");
					}
				}
				content.append("]");

				String resourcesQuery = "INSERT INTO "
						+ DBCredentials.COURSE_RESOURCES_TABLE
						+ " (`assoc_id`,`assoc_table_id`,`content`) VALUES (?, ?, ?)";
				// add the empty resources into table
				prepStmt = connection.prepareStatement(resourcesQuery);
				prepStmt.setInt(1, assocId);
				prepStmt.setInt(2, isCourse ? 1 : 2);
				prepStmt.setString(3, content.toString());

				added = prepStmt.executeUpdate() == 1;

				prepStmt.close();
			}
		} catch (Exception e) {
		}

		return added;
	}

	public static int createTccAssocForGroups(DBConnection dbConnection,
			int classId, int courseId, int teacherId, int semester, int oldTccId) {
		Connection connection = dbConnection.getConnection();
		int row = 0;

		String selectQuery = "SELECT `id` FROM "
				+ DBCredentials.TEACHER_COURSE_CLASS_TABLE
				+ " WHERE `courseId`=? AND `classId`=? AND `semester`=? AND `teacherId`=?";
		String insertQuery = "INSERT INTO "
				+ DBCredentials.TEACHER_COURSE_CLASS_TABLE
				+ " (`courseId`,`classId`,`teacherId`,`semester`) VALUES (?, ?, ?, ?)";
		// get students ids from class
		List<Integer> studentIds = studentsFromGrupList(dbConnection, classId);

		try {
			PreparedStatement prepStmt = connection
					.prepareStatement(selectQuery);
			prepStmt.setInt(1, courseId);
			prepStmt.setInt(2, classId);
			prepStmt.setInt(3, semester);
			prepStmt.setInt(4, teacherId);

			ResultSet result = prepStmt.executeQuery();
			boolean exists = result.next();

			if (exists) {
				// tccId exists, so add it in the users' list of courses
				int tccId = result.getInt("id");
				for (int studentId : studentIds) {
					updateStudentCourses(dbConnection, studentId, tccId,
							oldTccId);
				}
			} else {
				// insert
				prepStmt = connection.prepareStatement(insertQuery);
				prepStmt.setInt(1, courseId);
				prepStmt.setInt(2, classId);
				prepStmt.setInt(3, teacherId);
				prepStmt.setInt(4, semester);

				row = prepStmt.executeUpdate();
				if (row == 1) {
					// get the id
					prepStmt = connection.prepareStatement(selectQuery);
					prepStmt.setInt(1, courseId);
					prepStmt.setInt(2, classId);
					prepStmt.setInt(3, semester);
					prepStmt.setInt(4, teacherId);

					result = prepStmt.executeQuery();
					int tccId = -1;
					if (result.next()) {
						tccId = result.getInt("id");
					}

					if (tccId > 0) {
						boolean added = setEmptyResources(dbConnection,
								semester, tccId, true);
						if (added) {
							for (int studentId : studentIds) {
								updateStudentCourses(dbConnection, studentId,
										tccId, oldTccId);
							}
						}
					}
				}
			}

			prepStmt.close();
		} catch (Exception e) {
		}

		return row;
	}

	public static int createTccAssocForStudent(DBConnection dbConnection,
			int courseId, int teacherId, int semester, int studentId,
			int oldTccId) {
		Connection connection = dbConnection.getConnection();
		int row = 0;
		String selectQuery = "SELECT `id` FROM "
				+ DBCredentials.TEACHER_COURSE_TABLE
				+ " WHERE `courseId`=? AND `semester`=? AND `teacherId`=?";
		String insertQuery = "INSERT INTO "
				+ DBCredentials.TEACHER_COURSE_TABLE
				+ " (`courseId`,`teacherId`,`semester`) VALUES (?, ?, ?)";

		try {
			PreparedStatement prepStmt = connection
					.prepareStatement(selectQuery);
			prepStmt.setInt(1, courseId);
			prepStmt.setInt(2, semester);
			prepStmt.setInt(3, teacherId);

			ResultSet result = prepStmt.executeQuery();
			boolean exists = result.next();

			if (exists) {
				// tccId exists, so add it in the users' list of courses
				int tccId = result.getInt("id");
				updateStudentOptionals(dbConnection, studentId, tccId, oldTccId);
			} else {
				// insert
				prepStmt = connection.prepareStatement(insertQuery);
				prepStmt.setInt(1, courseId);
				prepStmt.setInt(2, teacherId);
				prepStmt.setInt(3, semester);

				row = prepStmt.executeUpdate();
				if (row == 1) {
					// get the id
					prepStmt = connection.prepareStatement(selectQuery);
					prepStmt.setInt(1, courseId);
					prepStmt.setInt(2, semester);
					prepStmt.setInt(3, teacherId);

					result = prepStmt.executeQuery(selectQuery);
					int tccId = -1;
					if (result.next()) {
						tccId = result.getInt("id");
					}
					if (tccId > 0) {
						boolean added = setEmptyResources(dbConnection,
								semester, tccId, false);
						if (added) {
							updateStudentOptionals(dbConnection, studentId,
									tccId, oldTccId);
						}
					}
				}
			}

			result.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return row;
	}

	public static JSONObject getStudentActivities(DBConnection dbConnection,
			int studentId, int assocId, int assocTableId) {
		JSONObject activities = new JSONObject();
		Connection connection = dbConnection.getConnection();
		String query = "SELECT `id`,`activities`,`grades`,`absences` FROM "
				+ DBCredentials.ACTIVITY_TABLE
				+ " WHERE `assoc_id`=? AND `assoc_table_id`=? AND `student_id`=?";

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, assocId);
			prepStmt.setInt(2, assocTableId);
			prepStmt.setInt(3, studentId);

			ResultSet result = prepStmt.executeQuery();

			if (result.next()) {
				activities.put("id", result.getInt("id"));

				String acts = result.getString("activities");
				activities.put("activities", acts == null ? new JSONArray()
						: JSONArray.fromObject(acts));

				String grds = result.getString("grades");
				activities.put("grades", grds == null ? new JSONArray()
						: JSONArray.fromObject(grds));

				String abs = result.getString("absences");
				activities.put("absences", abs == null ? new JSONArray()
						: JSONArray.fromObject(abs));
			}

			result.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return activities;
	}

	public static void updateSemestrialPaper(DBConnection dbConnection,
			int activityPK, int oldSemPaperId, int newSemPaperId) {
		Connection connection = dbConnection.getConnection();
		String selectQuery = "SELECT `grades` FROM "
				+ DBCredentials.ACTIVITY_TABLE + " WHERE `id`=?";
		String updateQuery = "UPDATE " + DBCredentials.ACTIVITY_TABLE
				+ " SET `grades`= ? WHERE `id`=?";

		try {
			JSONArray grades = new JSONArray();
			PreparedStatement prepStmt = connection
					.prepareStatement(selectQuery);
			prepStmt.setInt(1, activityPK);
			ResultSet result = prepStmt.executeQuery();

			// modify grades
			if (result.next()) {
				grades = JSONArray.fromObject(result.getString("grades"));

				for (int i = 0; i < grades.size(); i++) {
					JSONObject grade = grades.getJSONObject(i);
					if (grade.getInt("id") == oldSemPaperId) {
						grade.put("isSemestrialPaper", 0);
					}
					if (grade.getInt("id") == newSemPaperId) {
						grade.put("isSemestrialPaper", 1);
					}
				}
			}
			result.close();

			// update grades
			prepStmt = connection.prepareStatement(updateQuery);
			prepStmt.setString(1, grades.toString());
			prepStmt.setInt(2, activityPK);

			prepStmt.executeUpdate();
			prepStmt.close();
		} catch (Exception e) {
		}
	}

	public static void updateAbsenceMotivation(DBConnection dbConnection,
			int activityPK, int absenceId, int isMotivated) {
		Connection connection = dbConnection.getConnection();
		String selectQuery = "SELECT `absences` FROM "
				+ DBCredentials.ACTIVITY_TABLE + " WHERE `id`=?";
		String updateQuery = "UPDATE " + DBCredentials.ACTIVITY_TABLE
				+ " SET `absences`= ? WHERE `id`=?";

		try {
			JSONArray absences = new JSONArray();
			PreparedStatement prepStmt = connection
					.prepareStatement(selectQuery);
			prepStmt.setInt(1, activityPK);
			ResultSet result = prepStmt.executeQuery();

			// modify absence
			if (result.next()) {
				absences = JSONArray.fromObject(result.getString("absences"));

				for (int i = 0; i < absences.size(); i++) {
					JSONObject absence = absences.getJSONObject(i);
					if (absence.getInt("id") == absenceId) {
						absence.put("isMotivated", isMotivated);
						break;
					}
				}
			}
			result.close();

			// update grades
			prepStmt = connection.prepareStatement(updateQuery);
			prepStmt.setString(1, absences.toString());
			prepStmt.setInt(2, activityPK);

			prepStmt.executeUpdate();
			prepStmt.close();
		} catch (Exception e) {
		}
	}

	/**
	 * adds an entry in the activities jsonArray
	 * 
	 * @param dbConnection
	 * @param activityPK
	 * @param activity
	 * @return
	 */
	public static int addNewActivity(DBConnection dbConnection, int activityPK,
			String columnName, JSONObject activity) {
		Connection connection = dbConnection.getConnection();
		int rows = 0;

		String selectQuery = "SELECT `" + columnName + "` FROM "
				+ DBCredentials.ACTIVITY_TABLE + " WHERE `id`=?";
		String updateQuery = "UPDATE " + DBCredentials.ACTIVITY_TABLE
				+ " SET `" + columnName + "`=? WHERE `id`=" + activityPK;

		try {
			PreparedStatement prepStmt = connection
					.prepareStatement(selectQuery);
			prepStmt.setInt(1, activityPK);
			ResultSet result = prepStmt.executeQuery();

			if (result.next()) {
				JSONArray allGenericActivities = new JSONArray();
				String currGenericActivities = result.getString(columnName);
				if (currGenericActivities != null) {
					allGenericActivities = JSONArray
							.fromObject(currGenericActivities);
				}
				// remove other semestrial papers, if exist
				if (activity.containsKey("isSemestrialPaper")
						&& activity.getInt("isSemestrialPaper") == 1) {
					for (int i = 0; i < allGenericActivities.size(); i++) {
						JSONObject act = allGenericActivities.getJSONObject(i);
						if (act.getInt("isSemestrialPaper") == 1) {
							act.put("isSemestrialPaper", 0);
							break;
						}
					}
				}
				allGenericActivities.add(activity);

				prepStmt = connection.prepareStatement(updateQuery);
				prepStmt.setString(1, allGenericActivities.toString());
				prepStmt.setInt(2, activityPK);

				rows = prepStmt.executeUpdate();
				prepStmt.close();
			}

			prepStmt.close();
		} catch (Exception e) {
		}

		return rows;
	}

	/**
	 * adds a row in activity table
	 * 
	 * @param dbConnection
	 * @param activity
	 * @return
	 */
	public static int insertNewActivity(DBConnection dbConnection, int assocId,
			int assocTableId, int studentId, String columnName,
			JSONObject activity) {
		Connection connection = dbConnection.getConnection();
		int rows = 0;

		String insertQuery = "INSERT INTO " + DBCredentials.ACTIVITY_TABLE
				+ " (`assoc_id`,`assoc_table_id`,`student_id`,`" + columnName
				+ "`) VALUES (?, ?, ?, '[?]')";
		String selectQuery = "SELECT `id` FROM " + DBCredentials.ACTIVITY_TABLE
				+ " WHERE `assoc_id`=" + assocId + " AND `assoc_table_id`="
				+ assocTableId + " AND `student_id`=" + studentId;

		try {
			PreparedStatement prepStmt = connection
					.prepareStatement(insertQuery);
			prepStmt.setInt(1, assocId);
			prepStmt.setInt(2, assocTableId);
			prepStmt.setInt(3, studentId);
			prepStmt.setString(4, activity.toString());

			rows = prepStmt.executeUpdate();

			if (rows == 1) {
				// added
				prepStmt = connection.prepareStatement(selectQuery);
				prepStmt.setInt(1, assocId);
				prepStmt.setInt(2, assocTableId);
				prepStmt.setInt(3, studentId);

				ResultSet result = prepStmt.executeQuery();
				if (result.next()) {
					// overwrite rows with id
					rows = result.getInt("id");
				}
				result.close();
			}
			prepStmt.close();
		} catch (Exception e) {
		}

		return rows;
	}

	public static int removeActivity(DBConnection dbConnection, int activityPK,
			String columnName, int activityId) {
		Connection connection = dbConnection.getConnection();
		int rows = 0;

		String selectQuery = "SELECT `" + columnName + "` FROM "
				+ DBCredentials.ACTIVITY_TABLE + " WHERE `id`=?";
		String updateQuery = "UPDATE " + DBCredentials.ACTIVITY_TABLE
				+ " SET `" + columnName + "`=? WHERE `id`=?";

		try {
			PreparedStatement prepStmt = connection
					.prepareStatement(selectQuery);
			prepStmt.setInt(1, activityPK);
			ResultSet result = prepStmt.executeQuery();

			if (result.next()) {
				JSONArray genericActivities = JSONArray.fromObject(result
						.getString(columnName));

				for (int i = 0; i < genericActivities.size(); i++) {
					if (genericActivities.getJSONObject(i).getInt("id") == activityId) {
						genericActivities.remove(i);
						break;
					}
				}

				prepStmt = connection.prepareStatement(updateQuery);
				prepStmt.setString(1, genericActivities.toString());
				prepStmt.setInt(2, activityPK);

				rows = prepStmt.executeUpdate();
				prepStmt.close();
			}

			result.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return rows;
	}

	public static int updateActivity(DBConnection dbConnection, int activityPK,
			String column, JSONObject activity) {
		Connection connection = dbConnection.getConnection();
		int rows = 0;

		String selectQuery = "SELECT `" + column + "` FROM "
				+ DBCredentials.ACTIVITY_TABLE + " WHERE `id`=?";
		String updateQuery = "UPDATE " + DBCredentials.ACTIVITY_TABLE
				+ " SET `" + column + "`=? WHERE `id`=?";

		boolean doExclusiveCheck = false;
		final String SENSIBLE_KEY = "isSemestrialPaper";
		if (activity.containsKey(SENSIBLE_KEY)
				&& activity.getInt(SENSIBLE_KEY) == 1) {
			doExclusiveCheck = true;
		}

		try {
			PreparedStatement prepStmt = connection
					.prepareStatement(selectQuery);
			prepStmt.setInt(1, activityPK);
			ResultSet result = prepStmt.executeQuery();

			if (result.next()) {
				JSONArray genericActivities = JSONArray.fromObject(result
						.getString(column));
				int activityId = activity.getInt("id");
				for (int i = 0; i < genericActivities.size(); i++) {
					JSONObject currGenericActivity = genericActivities
							.getJSONObject(i);
					int currGenericActivityId = currGenericActivity
							.getInt("id");
					if (doExclusiveCheck && currGenericActivityId != activityId) {
						if (currGenericActivity.getInt(SENSIBLE_KEY) == 1) {
							currGenericActivity.put(SENSIBLE_KEY, 0);
						}
					}
					if (currGenericActivityId == activityId) {
						genericActivities.remove(i);
						genericActivities.add(i, activity);
					}
				}

				prepStmt = connection.prepareStatement(updateQuery);
				prepStmt.setString(1, genericActivities.toString());
				prepStmt.setInt(2, activityPK);

				rows = prepStmt.executeUpdate();
				prepStmt.close();
			}

			result.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return rows;
	}

	public static JSONArray getFeedbackResponses(DBConnection dbConnection,
			int assocId, int assocTableId) {
		JSONArray responses = new JSONArray();
		Connection connection = dbConnection.getConnection();

		String selectIdQuery = "SELECT `id` FROM "
				+ DBCredentials.FEEDBACK_REQUEST_TABLE
				+ " WHERE `assoc_id`=? AND `assoc_table_id`=?";
		String responsesQuery = "SELECT `opinion` FROM "
				+ DBCredentials.FEEDBACK_TABLE + " WHERE `feedback_id`=?";

		try {
			PreparedStatement prepStmt = connection
					.prepareStatement(selectIdQuery);
			prepStmt.setInt(1, assocId);
			prepStmt.setInt(2, assocTableId);

			ResultSet result = prepStmt.executeQuery();

			if (result.next()) {
				int id = result.getInt("id");
				prepStmt = connection.prepareStatement(responsesQuery);
				prepStmt.setInt(1, id);
				ResultSet resp = prepStmt.executeQuery();

				while (resp.next()) {
					String response = resp.getString("opinion");
					responses.add(response);
				}
			}

			prepStmt.close();
		} catch (Exception e) {
		}

		return responses;
	}

	public static JSONArray getGradesArchive(DBConnection dbConnection,
			int studentId) {
		JSONArray archive = new JSONArray();
		String query = "SELECT `archive` FROM "
				+ DBCredentials.GRADES_ARCHIVE_TABLE + " WHERE `student_id`=?";
		Connection connection = dbConnection.getConnection();

		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, studentId);
			ResultSet result = prepStmt.executeQuery();

			if (result.next()) {
				String rawGrades = result.getString("archive");
				if (rawGrades != null) {
					archive = JSONArray.fromObject(rawGrades);
				}
			}

			result.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return archive;
	}
	

	public static boolean cheangeSemester(DBConnection dbConnection) {
		String selectQuery = "SELECT `semester` FROM "
				+ DBCredentials.SEMESTER_TABLE + " WHERE `id`=1";
		int rows = 0;
		Connection connection = dbConnection.getConnection();
		try {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(selectQuery);

			if (result.next()) {
				int semester = result.getInt("semester");
				rows = statement.executeUpdate("UPDATE "
						+ DBCredentials.SEMESTER_TABLE + " SET `semester`="
						+ ((semester % 2) + 1) + " WHERE `id`=1");
			}

			statement.close();
		} catch (Exception e) {
		}

		return rows == 1;
	}

	public static String getCourseNameFromAssocId(DBConnection dbConnection,
			int assocId, int assocTableId) {
		String table = getAssocTableName(dbConnection, assocTableId);
		String query = "SELECT `courseId` FROM " + table + " WHERE `id`=?";
		String courseName = "";

		Connection connection = dbConnection.getConnection();
		try {
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setInt(1, assocId);
			ResultSet result = prepStmt.executeQuery();

			if (result.next()) {
				int courseId = result.getInt("courseId");
				JSONObject course = DBCommonOperations.getCourseInfo(courseId);
				courseName = course.getString("name");
			}

			result.close();
			prepStmt.close();
		} catch (Exception e) {
		}

		return courseName;
	}

	public static void computeAverage(DBConnection dbConnection) {
		String studentsQuery = "SELECT `id` FROM "
				+ DBCredentials.STUDENT_TABLE;
		String coursesQuery = "SELECT `teacher_course_class_ids`, `teacher_course_ids` FROM "
				+ DBCredentials.COURSES_LIST_TABLE + " WHERE `studentId`=";
		String activitiesQuery = "SELECT `id`, `grades`, `absences` FROM "
				+ DBCredentials.ACTIVITY_TABLE
				+ " WHERE `assoc_id`=? AND `assoc_table_id`=? AND `student_id`=?";
		String removeQuery = "DELETE FROM " + DBCredentials.ACTIVITY_TABLE
				+ " WHERE `student_id`=";

		Connection connection = dbConnection.getConnection();
		try {
			Statement statement = connection.createStatement();
			ResultSet studentIdsResult = statement.executeQuery(studentsQuery);
			PreparedStatement prepStmt = connection
					.prepareStatement(activitiesQuery);

			while (studentIdsResult.next()) {
				int studentId = studentIdsResult.getInt("id");
				ResultSet coursesResult = statement.executeQuery(coursesQuery
						+ studentId);
				if (coursesResult.next()) {
					String rawTccIds = coursesResult
							.getString("teacher_course_class_ids");
					String rawTcIds = coursesResult
							.getString("teacher_course_ids");
					JSONObject semester = new JSONObject();
					JSONArray courses = new JSONArray();
					List<String> absencesDays = new ArrayList<String>();
					List<Integer> motivatedAbsences = new ArrayList<Integer>();
					List<Integer> unmotivatedAbsences = new ArrayList<Integer>();

					if (rawTccIds != null) {
						// courses
						String[] tccIds = rawTccIds.split(",");
						for (String tccId : tccIds) {
							// each course
							int tccPk = Integer.parseInt(tccId);

							prepStmt.setInt(1, tccPk);
							prepStmt.setInt(2, 1);
							prepStmt.setInt(3, studentId);

							ResultSet allActivities = prepStmt.executeQuery();
							if (allActivities.next()) {
								String grades = allActivities
										.getString("grades");
								String absences = allActivities
										.getString("absences");
								if (grades != null) {
									JSONArray gradesJson = JSONArray
											.fromObject(grades);
									JSONObject course = new JSONObject();
									course.put(
											"title",
											getCourseNameFromAssocId(
													dbConnection, tccPk, 1));
									course.put("grades", gradesJson);
									// compute the average
									double avg = 0;
									if (gradesJson.size() == 1) {
										avg = gradesJson.getJSONObject(0)
												.getInt("grade");
									} else {
										boolean hasSemestrialPaper = false;
										int semestrialPaperIndex = -1;
										for (int i = 0; i < gradesJson.size(); i++) {
											JSONObject g = gradesJson
													.getJSONObject(i);

											if (g.getInt("isSemestrialPaper") == 1) {
												semestrialPaperIndex = i;
												hasSemestrialPaper = true;
												continue;
											}

											avg += g.getInt("grade");
										}

										if (!hasSemestrialPaper) {
											avg = avg / gradesJson.size();
										} else {
											int semestrialPaperGrade = gradesJson
													.getJSONObject(
															semestrialPaperIndex)
													.getInt("grade");
											avg = (((avg / (gradesJson.size() - 1)) * 3) + semestrialPaperGrade) / 4;
										}
									}
									course.put("average", Grades.round(avg));

									courses.add(course);
								}
								if (absences != null) {
									JSONArray absencesJson = JSONArray
											.fromObject(absences);
									for (int i = 0; i < absencesJson.size(); i++) {
										JSONObject absence = absencesJson
												.getJSONObject(i);
										boolean found = false;
										for (int j = 0; j < absencesDays.size(); j++) {
											if (absencesDays.get(j).equals(
													absence.getString("date"))) {
												found = true;

												if (absence
														.getInt("isMotivated") == 1) {
													Integer noAbs = motivatedAbsences
															.get(j);
													motivatedAbsences.remove(j);
													motivatedAbsences.add(j,
															noAbs + 1);
												} else {
													Integer noAbs = unmotivatedAbsences
															.get(j);
													unmotivatedAbsences
															.remove(j);
													unmotivatedAbsences.add(j,
															noAbs + 1);
												}

												break;
											}
										}
										if (!found) {
											int index = Math.max(0,
													absencesDays.size() - 1);
											absencesDays.add(index,
													absence.getString("date"));
											if (absence.getInt("isMotivated") == 1) {
												motivatedAbsences.add(index, 1);
											} else {
												unmotivatedAbsences.add(index,
														1);
											}
										}
									}
								}
							}
						}
					}
					if (rawTcIds != null) {
						// optionals
						String[] tcIds = rawTcIds.split(",");
						for (String tcId : tcIds) {
							// each optional
							int tcPk = Integer.parseInt(tcId);

							prepStmt.setInt(1, tcPk);
							prepStmt.setInt(2, 2);
							prepStmt.setInt(3, studentId);

							ResultSet allActivities = prepStmt.executeQuery();
							if (allActivities.next()) {
								String grades = allActivities
										.getString("grades");
								String absences = allActivities
										.getString("absences");
								if (grades != null) {
									JSONArray gradesJson = JSONArray
											.fromObject(grades);
									JSONObject course = new JSONObject();
									course.put(
											"title",
											getCourseNameFromAssocId(
													dbConnection, tcPk, 1));
									course.put("grades", gradesJson);
									// compute the average
									double avg = 0;
									if (gradesJson.size() == 1) {
										avg = gradesJson.getJSONObject(0)
												.getInt("grade");
									} else {
										boolean hasSemestrialPaper = false;
										int semestrialPaperIndex = -1;
										for (int i = 0; i < gradesJson.size(); i++) {
											JSONObject g = gradesJson
													.getJSONObject(i);

											if (g.getInt("isSemestrialPaper") == 1) {
												semestrialPaperIndex = i;
												hasSemestrialPaper = true;
												continue;
											}

											avg += g.getInt("grade");
										}

										if (!hasSemestrialPaper) {
											avg = avg / gradesJson.size();
										} else {
											int semestrialPaperGrade = gradesJson
													.getJSONObject(
															semestrialPaperIndex)
													.getInt("grade");
											avg = (((avg / (gradesJson.size() - 1)) * 3) + semestrialPaperGrade) / 4;
										}
									}
									course.put("average", Grades.round(avg));

									courses.add(course);
								}
								if (absences != null) {
									JSONArray absencesJson = JSONArray
											.fromObject(absences);
									for (int i = 0; i < absencesJson.size(); i++) {
										JSONObject absence = absencesJson
												.getJSONObject(i);
										boolean found = false;
										for (int j = 0; j < absencesDays.size(); j++) {
											if (absencesDays.get(j).equals(
													absence.getString("date"))) {
												found = true;

												if (absence
														.getInt("isMotivated") == 1) {
													Integer noAbs = motivatedAbsences
															.get(j);
													motivatedAbsences.remove(j);
													motivatedAbsences.add(j,
															noAbs + 1);
												} else {
													Integer noAbs = unmotivatedAbsences
															.get(j);
													unmotivatedAbsences
															.remove(j);
													unmotivatedAbsences.add(j,
															noAbs + 1);
												}

												break;
											}
										}
										if (!found) {
											int index = absencesDays.size() - 1;
											absencesDays.add(index,
													absence.getString("date"));
											if (absence.getInt("isMotivated") == 1) {
												motivatedAbsences.add(index, 1);
											} else {
												unmotivatedAbsences.add(index,
														1);
											}
										}
									}
								}
							}
						}
					}
					// compute semester average
					semester.put("courses", courses);
					double average = 0;
					for (int i = 0; i < courses.size(); i++) {
						average += courses.getJSONObject(i).getInt("average");
					}
					average = average / courses.size();
					semester.put("average", Grades.round(average, 2));
					JSONArray absences = new JSONArray();
					for (int i = 0; i < absencesDays.size(); i++) {
						JSONObject absDay = new JSONObject();
						absDay.put("day", absencesDays.get(i));

						int motAbsNo = 0;
						if (i <= (motivatedAbsences.size() - 1)) {
							motAbsNo = motivatedAbsences.get(i);
						}
						absDay.put("motivated", motAbsNo);

						int unmotAbsNo = 0;
						if (i <= (unmotivatedAbsences.size() - 1)) {
							unmotAbsNo = unmotivatedAbsences.get(i);
						}
						absDay.put("unmotivated", unmotAbsNo);
						absences.add(absDay);
					}
					semester.put("absences", absences);

					// here, the semester is completed, for student studentId
					String archiveQuery = "SELECT `id`, `archive` FROM "
							+ DBCredentials.GRADES_ARCHIVE_TABLE
							+ " WHERE `student_id`=" + studentId;
					ResultSet archiveResult = statement
							.executeQuery(archiveQuery);
					boolean isEntry = archiveResult.next();
					boolean operationSuccedded = false;
					String currYear = getYear(dbConnection);

					// archive the semester
					if (!isEntry) {
						JSONArray archive = new JSONArray();
						JSONObject year = new JSONObject();
						JSONArray semesters = new JSONArray();
						semesters.add(semester);

						year.put("year", currYear);
						year.put("average", semester.getDouble("average"));
						year.put("semesters", semesters);

						archive.add(year);

						String insertArchiveQuery = "INSERT INTO "
								+ DBCredentials.GRADES_ARCHIVE_TABLE
								+ " (`student_id`, `archive`) VALUES ("
								+ studentId + ",'" + archive.toString() + "')";
						operationSuccedded = statement
								.executeUpdate(insertArchiveQuery) == 1;
					} else {
						int archiveId = archiveResult.getInt("id");
						JSONArray archive = JSONArray.fromObject(archiveResult
								.getString("archive"));

						// update
						boolean yearFound = false;
						for (int i = 0; i < archive.size(); i++) {
							JSONObject yearArchive = archive.getJSONObject(i);
							if (yearArchive.getString("year").equals(currYear)) {
								yearArchive.getJSONArray("semesters").add(
										semester);
								// compute new year average
								JSONArray sems = yearArchive
										.getJSONArray("semesters");
								double yearAvg = 0;
								for (int j = 0; j < sems.size(); j++) {
									yearAvg += sems.getJSONObject(i).getDouble(
											"average");
								}
								yearAvg = Grades
										.round(yearAvg / sems.size(), 2);
								// update new year average
								yearArchive.put("average", yearAvg);
								yearFound = true;
								break;
							}
						}

						if (!yearFound) {
							// create new year
							JSONObject year = new JSONObject();
							JSONArray semesters = new JSONArray();
							semesters.add(semester);

							year.put("year", currYear);
							year.put("average", semester.getDouble("average"));
							year.put("semesters", semesters);

							archive.add(year);
						}

						String updateArchiveQuery = "UPDATE "
								+ DBCredentials.GRADES_ARCHIVE_TABLE
								+ " SET `archive`='" + archive.toString()
								+ "' WHERE `id`=" + archiveId;
						operationSuccedded = statement
								.executeUpdate(updateArchiveQuery) == 1;
					}
				}

				// remove student activities
				statement.executeUpdate(removeQuery + studentId);
			}
			prepStmt.close();
			statement.close();
		} catch (Exception e) {
		}

	}
	

	public static String getYear(DBConnection dbConnection) {
		String query = "SELECT `year` FROM " + DBCredentials.YEAR_TABLE
				+ " WHERE `id`=1";
		String year = "";

		Connection connection = dbConnection.getConnection();
		try {
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);

			if (result.next()) {
				year = result.getString("year");
			}

			statement.close();
		} catch (Exception e) {
		}

		return year;
	}

	
	public static void removeTimetables(DBConnection dbConnection) {
		String query = "UPDATE " + DBCredentials.SCHOOL_TIMETABLE_TABLE
				+ " SET `timetable`=''";

		Connection connection = dbConnection.getConnection();
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate(query);
			statement.close();
		} catch (Exception e) {
		}
	}
	

	public static void removeAssociations(DBConnection dbConnection) {
		String tccQuery = "TRUNCATE TABLE "
				+ DBCredentials.TEACHER_COURSE_CLASS_TABLE;
		String tcQuery = "TRUNCATE TABLE " + DBCredentials.TEACHER_COURSE_TABLE;

		Connection connection = dbConnection.getConnection();
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate(tccQuery);
			statement.executeUpdate(tcQuery);
			statement.close();
		} catch (Exception e) {
		}
	}

	
	public static void removeCoursesList(DBConnection dbConnection) {
		String query = "TRUNCATE TABLE " + DBCredentials.COURSES_LIST_TABLE;

		Connection connection = dbConnection.getConnection();
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate(query);
			statement.close();
		} catch (Exception e) {
		}
	}

	
	public static void removeHomeworkTables(DBConnection dbConnection) {
		String homeworkResultsQuery = "TRUNCATE TABLE "
				+ DBCredentials.HOMEWORK_RESULTS_TABLE;
		String homeworkQuery = "TRUNCATE TABLE " + DBCredentials.HOMEWORK_TABLE;

		Connection connection = dbConnection.getConnection();
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate(homeworkResultsQuery);
			statement.executeUpdate(homeworkQuery);
			statement.close();
		} catch (Exception e) {
		}
	}

	
	public static void removeResources(DBConnection dbConnection) {
		String query = "TRUNCATE TABLE " + DBCredentials.COURSE_RESOURCES_TABLE;

		Connection connection = dbConnection.getConnection();
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate(query);
			statement.close();
		} catch (Exception e) {
		}
	}

	
	public static void removeHolidays(DBConnection dbConnection) {
		String query = "TRUNCATE TABLE " + DBCredentials.HOLIDAYS_TABLE;

		Connection connection = dbConnection.getConnection();
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate(query);
			statement.close();
		} catch (Exception e) {
		}
	}

	public static void removeLastYearStudents(DBConnection dbConnection) {
		String query = "DELETE FROM " + DBCredentials.STUDENT_TABLE
				+ " WHERE `group`='-'";

		Connection connection = dbConnection.getConnection();
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate(query);
			statement.close();
		} catch (Exception e) {
		}
	}
	

	public static JSONObject getResetInfo(DBConnection dbConnection,
			String email) {
		Connection connection = dbConnection.getConnection();
		JSONObject result = new JSONObject();

		String queryPrefix = "SELECT `id`, `firstName`, `lastName` FROM ";
		String queryPostfix = " WHERE `email` = ?";
		String[] tables = new String[] { DBCredentials.STUDENT_TABLE,
				DBCredentials.TEACHER_TABLE, DBCredentials.AUXILIARY_TABLE };

		try {
			for (String table : tables) {
				String query = queryPrefix + table + queryPostfix;
				PreparedStatement prepStmt = connection.prepareStatement(query);
				prepStmt.setString(1, email);

				ResultSet resultSet = prepStmt.executeQuery();
				boolean found = false;

				if (resultSet.next()) {
					found = true;
					result.put("userTable", table);
					result.put("userId", resultSet.getInt("id"));
					result.put("firstName", resultSet.getString("firstName"));
					result.put("lastName", resultSet.getString("lastName"));
				}

				resultSet.close();

				if (found) {
					break;
				}
			}
		} catch (Exception e) {
			return result;
		}

		return result;
	}

}
