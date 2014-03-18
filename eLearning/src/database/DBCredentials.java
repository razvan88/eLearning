package database;


/**
 * Class used for storing as final strings
 * the credentials used for database operations
 * 
 * @author Razvan Nedelcu
 */
public class DBCredentials {
	public static final String IP = "localhost";
	public static final String USER = "root";
	public static final String PASSWORD = "";
	
	public static final String DRIVER = "com.mysql.jdbc.Driver";
	
	public static String getDefaultLink() {
		return "jdbc:mysql://" + IP + "/" + 
			"?user=" + USER + "&password=" + PASSWORD;
	}
	
	public static String getLink(String databaseName) {
		return "jdbc:mysql://" + IP + "/" + databaseName + 
			"?user=" + USER + "&password=" + PASSWORD;
	}
	
	public static final String DEFAULT_DATABASE = "common";
	public static final String SCHOOLS_TABLE = "school";
	public static final String SCHOOL_TYPES_TABLE = "school_type";
	public static final String CITIES_TABLE = "city";
	public static final String ROLES_TABLE = "role";
	public static final String CLASS_TABLE = "class";
	public static final String COURSES_TABLE = "course";
	
	public static final String SCHOOL_NEWS_TABLE = "school_news";
	public static final String SCHOOL_TIMETABLE = "timetable";
	public static final String COURSES_LIST = "course_list";
	public static final String TEACHER_COURSE_CLASS_TABLE = "teacher_course_class";
	public static final String DEADLINES_TABLE = "deadlines";
	public static final String HOLIDAYS_TABLE = "holiday";
	public static final String RESOURCES_TABLE = "resources";
	public static final String HOMEWORK_TABLE = "homework";
	public static final String HOMEWORK_RESULTS_TABLE = "homework_results";
}