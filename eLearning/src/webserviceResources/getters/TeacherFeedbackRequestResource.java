package webserviceResources.getters;

import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.resource.Options;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import utils.ConfigurationSettings;
import database.DBConnection;
import database.DBConnectionManager;
import database.DBUtils;

public class TeacherFeedbackRequestResource extends ServerResource {

	@Post
	@Options
	public String getFeedbackRequest(Representation entity) {
		//do not treat Options requests
		if(this.getRequest().getMethod() == Method.OPTIONS)
			return "";
				
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));
		
		int schoolId = info.getInt("schoolId");
		int courseId = info.getInt("courseId");
		int classId = info.getInt("classId");
		boolean isStudent = info.getInt("student") == 1;
		boolean isOptional = info.getInt("optional") == 1;
		int userId = info.getInt("userId");
		int semester = info.getInt("semester");
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId, database);
		
		int assocId = DBUtils.getAssocId(dbConnection, userId, isStudent, courseId, classId, semester, isOptional);
		int assocTableId = isOptional ? 2 : 1;
		JSONObject feedback = DBUtils.getFeedbackRequest(dbConnection, assocId, assocTableId);
		
		return feedback.toString();
	}
}
