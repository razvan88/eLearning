package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
	private static HashMap<Integer, String> sAuxiliaryFunctions;
	private static HashMap<Integer, String> sRoles;
	private static HashMap<Integer, String> sTitles;
	private static HashMap<Integer, JSONObject> sCourses;

	static {
		sConnection = new DBConnection(DBCredentials.DEFAULT_DATABASE);
		sConnection.openConnection();

		sCachedSchoolList = getJsonSchools();

		sHighschoolGroups = new HashMap<Integer, String>();
		sAuxiliaryFunctions = new HashMap<Integer, String>();
		sRoles = new HashMap<Integer, String>();
		sTitles = new HashMap<Integer, String>();
		sCourses = new HashMap<Integer, JSONObject>();

		populateHighschoolGroups();
		populateAuxiliaryFunctions();
		populateRoles();
		populateTitles();
		populateCourses();
	}

	private DBCommonOperations() {
	}

	private static void populateHighschoolGroups() {
		try {
			Connection connection = sConnection.getConnection();
			Statement statement = connection
					.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
			String query = "SELECT `id`, `name` FROM "
					+ DBCredentials.CLASS_TABLE;
			ResultSet result = statement.executeQuery(query);

			while (result.next()) {
				int id = result.getInt("id");
				String name = result.getString("name");

				sHighschoolGroups.put(id, name);
			}
		} catch (Exception e) {

		}
	}

	private static void populateAuxiliaryFunctions() {
		try {
			Connection connection = sConnection.getConnection();
			Statement statement = connection
					.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
			String query = "SELECT `id`, `name` FROM "
					+ DBCredentials.AUXILIARY_TABLE;
			ResultSet result = statement.executeQuery(query);

			while (result.next()) {
				int id = result.getInt("id");
				String name = result.getString("name");

				sAuxiliaryFunctions.put(id, name);
			}
		} catch (Exception e) {

		}
	}

	private static void populateRoles() {
		try {
			Connection connection = sConnection.getConnection();
			Statement statement = connection
					.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
			String query = "SELECT `id`, `name` FROM "
					+ DBCredentials.ROLES_TABLE;
			ResultSet result = statement.executeQuery(query);

			while (result.next()) {
				int id = result.getInt("id");
				String name = result.getString("name");

				sRoles.put(id, name);
			}
		} catch (Exception e) {

		}
	}

	private static void populateTitles() {
		try {
			Connection connection = sConnection.getConnection();
			Statement statement = connection
					.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
			String query = "SELECT `id`, `name` FROM "
					+ DBCredentials.TITLES_TABLE;
			ResultSet result = statement.executeQuery(query);

			while (result.next()) {
				int id = result.getInt("id");
				String name = result.getString("name");

				sTitles.put(id, name);
			}
		} catch (Exception e) {

		}
	}

	public static JSONArray getTitles() {
		JSONArray titles = new JSONArray();

		for (int key : sTitles.keySet()) {
			JSONObject title = new JSONObject();

			title.put("id", key);
			title.put("title", sTitles.get(key));

			titles.add(title);
		}

		return titles;
	}

	private static void populateCourses() {
		try {
			Connection connection = sConnection.getConnection();
			Statement statement = connection
					.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
			String query = "SELECT `id`, `name`, `abbreviation`, `photo`, `control` FROM "
					+ DBCredentials.COURSES_TABLE;
			ResultSet result = statement.executeQuery(query);

			while (result.next()) {
				JSONObject course = new JSONObject();

				int id = result.getInt("id");
				course.put("id", id);
				course.put("name", result.getString("name"));
				course.put("abbreviation", result.getString("abbreviation"));
				course.put("photo", result.getString("photo"));
				course.put("control", result.getInt("control"));

				sCourses.put(id, course);
			}
		} catch (Exception e) {

		}
	}

	public static JSONArray getAllCourses() {
		JSONArray courses = new JSONArray();

		for (JSONObject course : sCourses.values()) {
			JSONObject c = new JSONObject();

			c.put("id", course.getInt("id"));
			c.put("name", course.getString("name"));

			courses.add(c);
		}

		return courses;
	}

	public static int addClass(String className) {
		int id = -1;
		String query = "INSERT INTO " + DBCredentials.CLASS_TABLE
				+ " (`name`) VALUES (?)";

		try {
			Connection connection = sConnection.getConnection();
			PreparedStatement prepStmt = connection.prepareStatement(query);
			prepStmt.setString(1, className);
			int rows = prepStmt.executeUpdate();

			if (rows == 1) {
				query = "SELECT `id` FROM " + DBCredentials.CLASS_TABLE
						+ " WHERE `name`=?";

				prepStmt = connection.prepareStatement(query);
				prepStmt.setString(1, className);
				ResultSet result = prepStmt.executeQuery(query);

				if (result.next()) {
					id = result.getInt("id");

					sHighschoolGroups.put(id, className);
				}

				result.close();
			}

			prepStmt.close();
		} catch (Exception e) {
		}

		return id;
	}

	public static int addCourses(JSONArray courses) {
		int rows = 0;
		String query1 = "INSERT INTO " + DBCredentials.COURSES_TABLE
				+ " (`name`,`abbreviation`,`photo`) VALUES (?,?,?)";
		String query2 = "INSERT INTO " + DBCredentials.COURSES_TABLE
				+ " (`name`,`photo`) VALUES (?,?)";

		try {
			Connection connection = sConnection.getConnection();

			for (int i = 0; i < courses.size(); i++) {
				JSONObject course = courses.getJSONObject(i);

				String courseName = course.getString("name");
				String courseAbbreviation = course.getString("abbreviation");
				String photo = course.getString("photo");
				boolean hasAbbreviation = !courseAbbreviation.isEmpty();

				PreparedStatement prepStmt = null;
				if (hasAbbreviation) {
					prepStmt = connection.prepareStatement(query1);
					prepStmt.setString(1, courseName);
					prepStmt.setString(2, courseAbbreviation);
					prepStmt.setString(3, photo);

				} else {
					prepStmt = connection.prepareStatement(query2);
					prepStmt.setString(1, courseName);
					prepStmt.setString(2, photo);
				}
				int added = prepStmt.executeUpdate();

				if (added == 1) {
					rows++;
					String query = "SELECT `id` FROM "
							+ DBCredentials.COURSES_TABLE + " WHERE `name`=?";
					prepStmt = connection.prepareStatement(query);
					prepStmt.setString(1, courseName);
					ResultSet result = prepStmt.executeQuery();

					if (result.next()) {
						int id = result.getInt("id");

						sCourses.put(id, course);
					}

					result.close();
				} else
					break;

				prepStmt.close();
			}
		} catch (Exception e) {
		}

		return rows;
	}

	public static boolean classExists(int classId, String className) {
		return sHighschoolGroups.containsKey(classId);
	}

	public static String getClassName(int classId) {
		if(sHighschoolGroups.containsKey(classId)) {
			return sHighschoolGroups.get(classId);
		}
		
		return "";
	}
	
	public static JSONArray getAllClasses() {
		JSONArray classes = new JSONArray();

		for (int key : sHighschoolGroups.keySet()) {
			JSONObject aClass = new JSONObject();

			aClass.put("id", key);
			aClass.put("name", sHighschoolGroups.get(key));

			classes.add(aClass);
		}

		return classes;
	}

	/**
	 * @return json array with school objects, containing id, name, city and
	 *         type
	 */
	public static JSONArray getJsonSchools() {
		if (sCachedSchoolList != null) {
			return sCachedSchoolList;
		}

		JSONArray schools = new JSONArray();

		try {
			Connection connection = sConnection.getConnection();
			Statement statement = connection
					.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
			PreparedStatement prepStatementCity = connection
					.prepareStatement("SELECT `name` FROM "
							+ DBCredentials.CITIES_TABLE + " WHERE `id`=?");
			PreparedStatement prepStatementType = connection
					.prepareStatement("SELECT `name` FROM "
							+ DBCredentials.SCHOOL_TYPES_TABLE
							+ " WHERE `id`=?");

			ResultSet resultSet = statement.executeQuery("SELECT * FROM "
					+ DBCredentials.SCHOOLS_TABLE);

			while (resultSet.next()) {
				JSONObject school = new JSONObject();

				school.put("id", resultSet.getInt("id"));
				school.put("name", resultSet.getString("name"));
				school.put("branch", resultSet.getString("branch"));

				int cityId = resultSet.getInt("city");
				prepStatementCity.setInt(1, cityId);
				ResultSet rs = prepStatementCity.executeQuery();
				if (rs.next()) {
					school.put("city", rs.getString(1));
				}

				int schoolTypeId = resultSet.getInt("type");
				prepStatementType.setInt(1, schoolTypeId);
				rs = prepStatementType.executeQuery();
				if (rs.next()) {
					school.put("type", rs.getString(1));
				}

				schools.add(school);
			}

			prepStatementCity.close();
			prepStatementType.close();
			statement.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return schools;
	}

	/**
	 * 
	 * @param schoolId
	 *            school primary key, from database
	 * @return the school's corresponding database name, from the config file
	 */
	public static String getDatabaseName(int schoolId) {
		return ConfigurationSettings.getValue(
				ConfigurationSettings.SCHOOLS_SECTION,
				String.format("%i", schoolId));
	}

	/**
	 * @param schoolId
	 * @return a jsonObject containing school's id, name, branch and city
	 */
	public static JSONObject getSchoolInfo(int schoolId) {
		for (int i = 0; i < sCachedSchoolList.size(); i++) {
			if (((JSONObject) (sCachedSchoolList.get(i))).getInt("id") == schoolId) {
				return (JSONObject) (sCachedSchoolList.get(i));
			}
		}

		return null;
	}

	public static String getGroupName(int groupId) {
		if (sHighschoolGroups.containsKey(groupId)) {
			return sHighschoolGroups.get(groupId);
		}

		return null;
	}

	public static JSONObject getCourseInfo(Integer courseId) {
		JSONObject course = new JSONObject();

		if (sCourses.containsKey(courseId)) {
			course = sCourses.get(courseId);
		}

		return course;
	}

	public static JSONArray getCoursesInfo(List<Integer> coursesIds) {
		JSONArray courses = new JSONArray();

		for (int key : coursesIds) {
			if (sCourses.containsKey(key)) {
				courses.add(sCourses.get(key));
			}
		}

		return courses;
	}

	public static JSONArray getCoursesInfo(String[] coursesIds) {
		JSONArray courses = new JSONArray();

		for (String courseId : coursesIds) {
			int key = Integer.parseInt(courseId);
			if (sCourses.containsKey(key)) {
				courses.add(sCourses.get(key));
			}
		}

		return courses;
	}

	public static List<String> getRolesNames(String[] rolesIds) {
		List<String> roles = new ArrayList<String>();

		for (String roleId : rolesIds) {
			int key = Integer.parseInt(roleId);
			if (sRoles.containsKey(key)) {
				roles.add(sRoles.get(key));
			}
		}

		return roles;
	}

	public static List<String> getAuxiliaryFunctions(
			String[] auxiliaryFunctionsIds) {
		List<String> functions = new ArrayList<String>();

		for (String auxiliaryFunctionId : auxiliaryFunctionsIds) {
			int key = Integer.parseInt(auxiliaryFunctionId);
			if (sAuxiliaryFunctions.containsKey(key)) {
				functions.add(sAuxiliaryFunctions.get(key));
			}
		}

		return functions;
	}

	public static JSONArray getAuxiliaryFunctions() {
		JSONArray functions = new JSONArray();

		for (int key : sAuxiliaryFunctions.keySet()) {
			JSONObject job = new JSONObject();

			job.put("id", key);
			job.put("name", sAuxiliaryFunctions.get(key));

			functions.add(job);
		}

		return functions;
	}
}