package webserviceResources.setters;

import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import utils.ConfigurationSettings;
import utils.Date;
import database.DBConnection;
import database.DBConnectionManager;
import database.DBUtils;

public class UploadCourseForumTopicResource extends ServerResource {

	@Post
	public String uploadFourumSubject(Representation entity) {
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		int schoolId = info.getInt("schoolId");
		int courseId = info.getInt("courseId");
		String subject = info.getString("subject");
		int initiatorId = info.getInt("initiatorId");
		int lastPostById = info.getInt("lastPostById");
		String initiatorTable = info.getString("initiatorTable");
		String lastPostByTable = info.getString("lastPostByTable");
		int isAnnouncement = info.getInt("isAnnouncement");
		int totalPosts = info.getInt("totalPosts");
		String content = info.getString("content");

		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId,
				database);

		String sqlDate = Date.getSQLDateNow();
		int primaryKey = DBUtils.uploadForumTopic(dbConnection, courseId,
				isAnnouncement, subject, initiatorId, initiatorTable,
				totalPosts, sqlDate, lastPostById, lastPostByTable);

		if(primaryKey < 0) {
			return "0";
		}
		
		boolean added = DBUtils.uploadFirstEntryForumSubject(dbConnection,
				primaryKey, initiatorId, initiatorTable, sqlDate, content);
		if(!added) {
			DBUtils.removeForumTopic(dbConnection, primaryKey);
			return "0";
		}
		
		return "1";
	}
}
