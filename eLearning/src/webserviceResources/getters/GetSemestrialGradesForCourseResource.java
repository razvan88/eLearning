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

public class GetSemestrialGradesForCourseResource extends ServerResource{

	@Post
	@Options
	public String getSemestrialGradesForCourse(Representation entity) {
		//do not treat Options requests
		if(this.getRequest().getMethod() == Method.OPTIONS)
			return "";
				
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		int schoolId = info.getInt("schoolId");
		int classId = info.getInt("classId");
		int courseId = info.getInt("courseId");
		int semester = info.getInt("semester");
		boolean isOptional = info.getInt("optional") == 1;
		int studentId = info.getInt("studentId");
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId,
				database);

		int assocId = DBUtils.getAssocId(dbConnection, studentId, true, courseId, classId, semester, isOptional);
		int assocTableId = isOptional ? 2 : 1;
		
		JSONArray grades = DBUtils.getSemestrialGradesForCourse(dbConnection, assocId, assocTableId, studentId);
		String courseName = DBCommonOperations.getCourseInfo(courseId).getString("name");
		
		JSONObject obj = new JSONObject();
		obj.put(courseName, grades);
		
		return obj.toString();
	}
}
