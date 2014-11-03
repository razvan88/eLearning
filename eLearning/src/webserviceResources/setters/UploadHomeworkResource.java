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

public class UploadHomeworkResource extends ServerResource {

	@Post
	@Options
	public String uploadHomework(Representation entity) {
		//do not treat Options requests
		if(this.getRequest().getMethod() == Method.OPTIONS)
			return "";
				
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		int schoolId = info.getInt("schoolId");
		int userId = info.getInt("userId");
		int homeworkId = info.getInt("homeworkId");
		//int courseId = info.getInt("courseId");
		String homeworkPath = info.getString("homeworkPath");
		String homeworkName = info.getString("homeworkName");
		//int semester = info.getInt("semester");
		//boolean isOptional = info.getInt("optional") == 1;
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId,
				database);

		String sqlDate = Date.getSQLDateNow();
		//int classId = DBUtils.getClassIdForUser(dbConnection, userId);
		//int assocId = DBUtils.getAssocId(dbConnection, userId, courseId, classId, semester, isOptional);
		String archive = "{\"name\":\"" + homeworkName + "\",\"location\":\"" + homeworkPath + "\"}";
		boolean added = DBUtils.uploadHomework(dbConnection, homeworkId, userId, archive, sqlDate);

		return added ? "1" : "0";
	}
}
