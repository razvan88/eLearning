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

public class FeedbackRequestResource extends ServerResource {

	@Post
	@Options
	public String getFeedbackRequest(Representation entity) {
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
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId, database);
		int classId = DBUtils.getClassIdForUser(dbConnection, userId);
		int assocId = DBUtils.getAssocId(dbConnection, userId, true, courseId, classId, semester, isOptional);
		
		JSONObject feedback = DBUtils.getFeedbackRequest(dbConnection, assocId, isOptional ? 2 : 1);
		
		return feedback.toString();
	}
}
