package webserviceResources.setters;

import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.resource.Options;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import utils.ConfigurationSettings;
import utils.Date;
import database.DBConnection;
import database.DBConnectionManager;
import database.DBUtils;

public class UploadCourseForumSubjectResource extends ServerResource {

	@Post
	@Options
	public String uploadForumSubject(Representation entity) {
		//do not treat Options requests
		if(this.getRequest().getMethod() == Method.OPTIONS)
			return "";
				
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		int schoolId = info.getInt("schoolId");
		int subjectId = info.getInt("subjectId");
		int senderId = info.getInt("senderId");
		String senderTable = info.getString("senderTable");
		int parentPostId = info.getInt("parentId");
		String content = info.getString("content");

		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId,
				database);

		String sqlDate = Date.getSQLDateNow();		
		boolean added = DBUtils.uploadForumSubjectEntry(dbConnection, subjectId, parentPostId, senderId, senderTable, sqlDate, content);

		return added ? "1" : "0";
	}
}
