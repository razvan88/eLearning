package webserviceResources.getters;

import java.util.ArrayList;
import java.util.List;

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
		
		JSONObject courseInfo = DBCommonOperations.getCourseInfo(courseId);
		List<Integer> courseIdList = new ArrayList<Integer>();
		courseIdList.add(courseId);
		
		int classId = DBUtils.getClassIdForUser(dbConnection, userId);
		int tccId = DBUtils.getTeachClassCourseId(dbConnection, classId, courseId);
		JSONArray deadlines = DBUtils.getAllDeadlines(dbConnection, userId, courseIdList);
		JSONObject holidays = DBUtils.getHolidayDetails(dbConnection);
		JSONArray resources = DBUtils.getResources(dbConnection, tccId);
		
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
