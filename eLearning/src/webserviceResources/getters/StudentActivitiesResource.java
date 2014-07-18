package webserviceResources.getters;

import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import utils.ConfigurationSettings;
import database.DBConnection;
import database.DBConnectionManager;
import database.DBUtils;

public class StudentActivitiesResource extends ServerResource {

	@Post
	public String getStudentActivities(Representation entity) {
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		int schoolId = info.getInt("schoolId");
		int semester = info.getInt("semester");
		int courseId = info.getInt("courseId");
		boolean isOptional = info.getInt("optional") == 1;
		boolean isStudent = info.getInt("student") == 1;
		int classId = info.getInt("classId");
		int userId = info.getInt("userId");
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId,
				database);

		int assocId = DBUtils.getAssocId(dbConnection, userId, isStudent, courseId, classId, semester, isOptional);
		int assocTableId = isOptional ? 2 : 1;
		
		JSONObject activities = DBUtils.getStudentActivities(dbConnection, userId, assocId, assocTableId);
		return activities.toString();
	}
}
