package webserviceResources.setters;

import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import utils.ConfigurationSettings;
import database.DBConnection;
import database.DBConnectionManager;
import database.DBUtils;

public class CourseWeekInfoResource extends ServerResource {

	@Post
	public String uploadCourseDetails(Representation entity) {
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		int schoolId = info.getInt("schoolId");
		int courseId = info.getInt("courseId");
		int classId = info.getInt("classId");
		int weekId = info.getInt("weekId");
		String description = info.getString("description");
		String resources = info.getString("resources");
		boolean isOptional = info.getInt("optional") == 1;
		boolean isStudent = info.getInt("student") == 1;
		int userId = info.getInt("userId");
		int semester = info.getInt("semester");
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId,
				database);

		int assocId = DBUtils.getAssocId(dbConnection, userId, isStudent, courseId, classId, semester, isOptional);
		int assocTableId = isOptional ? 2 : 1;
		int rows = DBUtils.uploadCourseWeekInfo(dbConnection, assocId, assocTableId, weekId, description, resources);
		
		return rows + "";
	}
}
