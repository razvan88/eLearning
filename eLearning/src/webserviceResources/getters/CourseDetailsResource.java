package webserviceResources.getters;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.resource.Options;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import utils.ConfigurationSettings;
import database.DBCommonOperations;
import database.DBConnection;
import database.DBConnectionManager;
import database.DBUtils;

public class CourseDetailsResource extends ServerResource {

	@Post
	@Options
	public String getCourseDetails(Representation entity) {
		//do not treat Options requests
		if(this.getRequest().getMethod() == Method.OPTIONS)
			return "";
				
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));
		
		int schoolId = info.getInt("schoolId");
		int userId = info.getInt("userId");
		int courseId = info.getInt("courseId");
		int semester = info.getInt("semester");
		boolean isOptional = info.getInt("optional") == 1;
		boolean isStudent = info.getInt("student") == 1;
		
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId, database);
		
		JSONObject courseInfo = DBCommonOperations.getCourseInfo(courseId);
		
		int classId = isStudent ? DBUtils.getClassIdForUser(dbConnection, userId) : info.getInt("classId");
		int assocId = DBUtils.getAssocId(dbConnection, userId, isStudent, courseId, classId, semester, isOptional);
		int assocTableId = isOptional ? 2 : 1;
		
		JSONArray deadlines = DBUtils.getAllDeadlines(dbConnection, userId, courseId, assocId, assocTableId);
		JSONObject holidays = DBUtils.getHolidayDetails(dbConnection, semester);
		JSONArray resources = DBUtils.getResources(dbConnection, assocId, assocTableId);
		
		JSONObject courseDetails = new JSONObject();
		courseDetails.put("name", courseInfo.getString("name"));
		courseDetails.put("holidays", holidays);
		courseDetails.put("resources", resources);
		
		JSONObject allDeadlines = new JSONObject();
		JSONArray allDates = new JSONArray();
		JSONArray allTips = new JSONArray();
		for(int i = 0; i < deadlines.size(); i++) {
			JSONObject deadline = (JSONObject)deadlines.get(i);
			allDates.add(deadline.get("deadline"));
			allTips.add(deadline.get("name"));
		}
		allDeadlines.put("dates", allDates);
		allDeadlines.put("tips", allTips);
		courseDetails.put("deadlines", allDeadlines);
		
		return courseDetails.toString();
	}
}
