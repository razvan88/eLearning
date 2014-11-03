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
import database.DBConnection;
import database.DBConnectionManager;
import database.DBUtils;

public class ForumSummaryResource extends ServerResource {

	@Post
	@Options
	public String getForumSummary(Representation entity) {
		//do not treat Options requests
		if(this.getRequest().getMethod() == Method.OPTIONS)
			return "";
				
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));
		
		int schoolId = info.getInt("schoolId");
		int courseId = info.getInt("courseId");
		int isAnnouncement = info.getInt("isAnnouncement");
		int userId = info.getInt("userId");
		int semester = info.getInt("semester");
		boolean isOptional = info.getInt("optional") == 1;
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId, database);
		
		int classId = DBUtils.getClassIdForUser(dbConnection, userId);
		JSONArray forumSummary = new JSONArray();
		
		int assocId = -1;
		int assocTableId = -1;
		if(courseId > -1) {
			boolean isStudent = info.getInt("student") == 1;
			assocId = DBUtils.getAssocId(dbConnection, userId, isStudent, courseId, classId, semester, isOptional);
			assocTableId = isOptional ? 2 : 1;
		}
		forumSummary = DBUtils.getForumSummary(dbConnection, assocId, assocTableId, isAnnouncement);
		
		return forumSummary.toString();
	}
}
