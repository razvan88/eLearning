package webserviceResources.getters;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import utils.ConfigurationSettings;
import database.DBConnection;
import database.DBConnectionManager;
import database.DBUtils;

public class CourseClassbookResource extends ServerResource {

	@Post
	public String getHomework(Representation entity) {
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));
		
		int schoolId = info.getInt("schoolId");
		int userId = info.getInt("userId");
		int courseId = info.getInt("courseId");
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId, database);
		int classId = DBUtils.getClassIdForUser(dbConnection, userId);
		int tccId = DBUtils.getTeachClassCourseId(dbConnection, classId, courseId);
		
		JSONArray courseClassbook = DBUtils.getCourseClassbook(dbConnection, tccId, userId);
		
		return courseClassbook.toString();
	}
}
