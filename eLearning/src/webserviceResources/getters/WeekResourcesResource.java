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

public class WeekResourcesResource extends ServerResource {

	@Post
	public String getHomeworkNameAndGrade(Representation entity) {
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		int schoolId = info.getInt("schoolId");
		int userId = info.getInt("userId");
		int courseId = info.getInt("courseId");
		int classId = info.getInt("classId");
		int weekId = info.getInt("weekId");
		int semester = info.getInt("semester");
		boolean isOptional = info.getInt("optional") == 1;
		boolean isStudent = info.getInt("student") == 1;
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId,
				database);

		int assocId = DBUtils.getAssocId(dbConnection, userId, isStudent, courseId, classId, semester, isOptional);
		JSONObject weekRes = DBUtils.getWeekResources(dbConnection, assocId, isOptional ? 2 : 1, weekId);
		
		return weekRes.toString();
	}
}
