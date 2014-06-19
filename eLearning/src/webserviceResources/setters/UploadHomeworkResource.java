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

public class UploadHomeworkResource extends ServerResource {

	@Post
	public String uploadHomework(Representation entity) {
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		int schoolId = info.getInt("schoolId");
		int userId = info.getInt("userId");
		int homeworkId = info.getInt("homeworkId");
		int courseId = info.getInt("courseId");
		String homeworkPath = info.getString("homeworkPath");
		String homeworkName = info.getString("homeworkName");
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId,
				database);

		String sqlDate = Date.getSQLDateNow();
		int classId = DBUtils.getClassIdForUser(dbConnection, userId);
		int tccId = DBUtils.getTeachClassCourseId(dbConnection, classId, courseId);
		String archive = "{\"name\":\"" + homeworkName + "\",\"location\":\"" + homeworkPath + "\"}";
		boolean added = DBUtils.uploadHomework(dbConnection, homeworkId, tccId, userId, archive, sqlDate);

		return added ? "1" : "0";
	}
}
