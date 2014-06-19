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

public class FeedbackStatusResource extends ServerResource {

	@Post
	public String uploadFeedback(Representation entity) {
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));
		
		int schoolId = info.getInt("schoolId");
		int studentId = info.getInt("userId");
		int courseId = info.getInt("courseId");
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId, database);
		int classId = DBUtils.getClassIdForUser(dbConnection, studentId);
		int tccId = DBUtils.getTeachClassCourseId(dbConnection, classId, courseId);
		
		int feedbackId = DBUtils.getFeedbackId(dbConnection, tccId);
		boolean given = DBUtils.getFeedbackStatus(dbConnection, feedbackId, studentId);
		
		return given ? "1" : "0";
	}
}
