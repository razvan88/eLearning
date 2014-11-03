package webserviceResources.setters;

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

public class UploadFeedbackResource extends ServerResource {

	@Post
	@Options
	public String uploadFeedback(Representation entity) {
		//do not treat Options requests
		if(this.getRequest().getMethod() == Method.OPTIONS)
			return "";
		
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));
		
		int schoolId = info.getInt("schoolId");
		int studentId = info.getInt("userId");
		int courseId = info.getInt("courseId");
		String opinion = info.getString("feedback");
		int semester = info.getInt("semester");
		boolean isOptional = info.getInt("optional") == 1;
		boolean isStudent = info.getInt("student") == 1;
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId, database);
		int classId = isStudent ? DBUtils.getClassIdForUser(dbConnection, studentId) : -1;
		
		int assocId = DBUtils.getAssocId(dbConnection, studentId, isStudent, courseId, classId, semester, isOptional);
		int assocTableId = isOptional ? 2 : 1;
		int feedbackId = DBUtils.getFeedbackId(dbConnection, assocId, assocTableId);
		boolean added = DBUtils.uploadFeedback(dbConnection, feedbackId, studentId, opinion);
		
		return added ? "1" : "0";
	}
}
