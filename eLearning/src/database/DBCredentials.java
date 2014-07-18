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
	public static final String TITLES_TABLE = "title";
	
	public static final String ACTIVITY_TABLE = "activity";
	public static final String ASSOC_TABLE = "assoc_table";
	public static final String TEACHER_TABLE = "teacher";
	public static final String AUXILIARY_TABLE = "auxiliary";
	public static final String STUDENT_TABLE = "student";
	public static final String ADMIN_TABLE = "admin";
	public static final String SCHOOL_NEWS_TABLE = "school_news";
	public static final String SCHOOL_TIMETABLE_TABLE = "timetable";
	public static final String COURSES_LIST_TABLE = "course_list";
	public static final String TEACHER_COURSE_CLASS_TABLE = "teacher_course_class";
	public static final String TEACHER_COURSE_TABLE = "teacher_course";
	public static final String DEADLINES_TABLE = "deadlines";
	public static final String HOLIDAYS_TABLE = "holiday";
	public static final String COURSE_RESOURCES_TABLE = "course_resources";
	public static final String OPTIONAL_RESOURCES_TABLE = "optional_resources";
	public static final String HOMEWORK_TABLE = "homework";
	public static final String HOMEWORK_RESULTS_TABLE = "homework_results";
	public static final String COURSE_CLASSBOOK_TABLE = "classbook_detailed";
	public static final String FEEDBACK_TABLE = "feedback";
	public static final String FEEDBACK_REQUEST_TABLE = "feedback_request";
	public static final String FORUM_SUMMARY_TABLE = "forum_summary";
	public static final String FORUM_SUBJECT_TABLE = "forum_subject";
	public static final String MESSAGES_TABLE = "message";
	public static final String SEMESTER_TABLE = "semester";
}