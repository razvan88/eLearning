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
import database.DBCredentials;
import database.DBUtils;

public class GetSemestrialGradesForCourseForGroupResource extends ServerResource {

	@Post
	@Options
	public String getSemestrialGradesForGroup(Representation entity) {
		//do not treat Options requests
		if(this.getRequest().getMethod() == Method.OPTIONS)
			return "";
				
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		int schoolId = info.getInt("schoolId");
		int classId = info.getInt("classId");
		int courseId = info.getInt("courseId");
		//boolean isOptional = info.getInt("optional") == 1;
		int semester = info.getInt("semester");
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId,
				database);

		JSONArray courseGrades = new JSONArray();
			
		// For now this feature works only for non-optional courses
		int assocId = DBUtils.getTeacherCourseClassId(dbConnection, classId, courseId,semester);
		int assocTableId = 1;
		
		JSONArray people = DBUtils.getSemestrialGradesForCourseForGroup(dbConnection, assocId, assocTableId);
		String courseName = DBCommonOperations.getCourseInfo(courseId).getString("name");
		
		for(int i = 0; i < people.size(); i++) {
			JSONObject person = people.getJSONObject(i);
			int studentId = person.getInt("student");
			JSONArray grades = person.getJSONArray("grades");
			
			JSONObject infoObj = DBUtils.getInformation(dbConnection, DBCredentials.STUDENT_TABLE, studentId);
			String studentName = infoObj.getString("firstName") + " " + infoObj.getString("lastName");
			
			JSONObject temp = new JSONObject();
			temp.put("student", studentName);
			temp.put("grades", grades);
			
			courseGrades.add(temp);
		}
		
		JSONObject result = new JSONObject();
		result.put("courseName", courseName);
		result.put("courseGrades", courseGrades);
		
		return result.toString();
	}
}
