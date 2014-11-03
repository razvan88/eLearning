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

public class FeedbackStatusResource extends ServerResource {

	@Post
	@Options
	public String uploadFeedback(Representation entity) {
		//do not treat Options requests
		if(this.getRequest().getMethod() == Method.OPTIONS)
			return "";
				
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));
		
		int schoolId = info.getInt("schoolId");
		int userId = info.getInt("userId");
		int courseId = info.getInt("courseId");
		int semester = info.getInt("semester");
		boolean isStudent = info.getInt("student") == 1;
		boolean isOptional = info.getInt("optional") == 1;
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId, database);
		int classId = isStudent ? DBUtils.getClassIdForUser(dbConnection, userId) : -1;
		int assocId = DBUtils.getAssocId(dbConnection, userId, isStudent, courseId, classId, semester, isOptional);
		int assocTableId = isOptional ? 2 : 1;
		int feedbackId = DBUtils.getFeedbackId(dbConnection, assocId, assocTableId);
		boolean given = DBUtils.getFeedbackStatus(dbConnection, feedbackId, userId);
		
		return given ? "1" : "0";
	}
}
