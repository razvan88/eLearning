package webserviceResources.getters;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import utils.ConfigurationSettings;
import database.DBCommonOperations;
import database.DBConnection;
import database.DBConnectionManager;
import database.DBUtils;

public class CourseDetailsResource extends ServerResource {

	@Post
	public String getCourseDetails(Representation entity) {
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));
		
		int schoolId = info.getInt("schoolId");
		int userId = info.getInt("userId");
		int courseId = info.getInt("courseId");
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId, database);
		
		JSONArray courseInfo = DBCommonOperations.getCoursesInfo(new String[] {"" + courseId});
		
		int classId = DBUtils.getClassIdForUser(dbConnection, userId);
		int tccId = DBUtils.getTeachClassCourseId(dbConnection, classId, courseId);
		JSONObject deadlines = DBUtils.getDeadlines(dbConnection, tccId);
		JSONObject holidays = DBUtils.getHolidayDetails(dbConnection);
		JSONArray resources = DBUtils.getResources(dbConnection, tccId);
		
		JSONObject courseDetails = new JSONObject();
		courseDetails.put("name", ((JSONObject)(courseInfo.get(0))).getString("name"));
		courseDetails.put("deadlines", deadlines);
		courseDetails.put("holidays", holidays);
		courseDetails.put("resources", resources);
		
		return courseDetails.toString();
	}
}
